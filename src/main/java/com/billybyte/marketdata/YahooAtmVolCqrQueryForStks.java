package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.models.vanilla.AnalyticFormulas;
import com.billybyte.dse.models.vanilla.BawAmerican;
import com.billybyte.dse.models.vanilla.DerivativeModel;
import com.billybyte.dse.models.vanilla.DerivativeModel.Sensitivity;
import com.billybyte.dse.queries.DivDseInputQuery;
import com.billybyte.dse.queries.TreasuryRateQueryFromTreasuryRateSingle;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecRight;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Get vols for Underlyings by using the implied vol of
 *   the nearest to the money option as a proxy
 *    
 * @author bperlman1
 *
 */
public class YahooAtmVolCqrQueryForStks implements QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>>{
	private final BawAmerican model = new BawAmerican();
	private final double seedVol = .2; 
	private final QueryInterface<String, SecDef> sdQuery = new SecDefQueryAllMarkets();
	private final double daysInYear;
	private final GoogleOptionChainQuery chainQuery;

	
	private final BigDecimal maxPercDiffBtwSettleAndOpStrikeToAllow;
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery;
	private final QueryInterface<Set<String>, 
		Map<String, ComplexQueryResult<BigDecimal>>> rateQuery;
	private final QueryInterface<Set<String>, 
		Map<String, ComplexQueryResult<BigDecimal>>> divQuery; 
	private final Calendar evalDate;
	
	
	
	/**
	 * 
	 * @param maxPercDiffBtwSettleAndOpStrikeToAllow
	 * @param pdiQuery
	 * @param evalDate
	 */
	public YahooAtmVolCqrQueryForStks(
			BigDecimal maxPercDiffBtwSettleAndOpStrikeToAllow,
			QueryInterface<Set<String>, 
				Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			QueryInterface<Set<String>, 
				Map<String, ComplexQueryResult<BigDecimal>>> rateQuery,
				QueryInterface<Set<String>, 
				Map<String, ComplexQueryResult<BigDecimal>>> divQuery,
				Calendar evalDate) {

		super();
		this.maxPercDiffBtwSettleAndOpStrikeToAllow = maxPercDiffBtwSettleAndOpStrikeToAllow;
		if(pdiQuery==null){
			this.pdiQuery = 
					new YahooCombinedStkOptPdiSetCqrRetQuery();
		}else{
			this.pdiQuery = pdiQuery;
		}
		if(rateQuery==null){
			this.rateQuery = new TreasuryRateQueryFromTreasuryRateSingle();
		}else{
			this.rateQuery = rateQuery;
		}
		if(divQuery==null){
			this.divQuery = new DivDseInputQuery();
		}else{
			this.divQuery = divQuery;
		}
		this.evalDate = evalDate;
		this.chainQuery = 
				 new GoogleOptionChainQuery(evalDate, 60, sdQuery);
		this.daysInYear = 365;
	}
	
	
	public YahooAtmVolCqrQueryForStks(){
		this(BigDecimal.ONE,
				new YahooCombinedStkOptPdiSetCqrRetQuery(),
				null,null,Calendar.getInstance());
		
	}
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		
		// Create return object
		Map<String, ComplexQueryResult<BigDecimal>> ret = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		// Get rates and div rates
		Map<String, ComplexQueryResult<BigDecimal>> ratesMap = 
				rateQuery.get(keySet, timeoutValue, timeUnitType);
		Map<String, ComplexQueryResult<BigDecimal>> divsMap = 
				divQuery.get(keySet, timeoutValue, timeUnitType);
		
		// Get settle pdi's
		Map<String, ComplexQueryResult<PriceDisplayInterface>> atmPdis = 
				pdiQuery.get(keySet, 10, TimeUnit.SECONDS);
		
		// Build map of derivative shortname to underlying shortname
		// If the atm price was not found above, add an error cqr to
		//   the ret (the map that gets returned to the caller of this get method)
		//   the map that we return to the caller of this get method.
		
		// Derivaitve shortName to Underlying shortname map
		Map<String,String> derivToUnderMap = new HashMap<String,String>();
		
		// check atmPdis for errors
		for(Entry<String, ComplexQueryResult<PriceDisplayInterface>> entry : atmPdis.entrySet()){
			String atmSn = entry.getKey();
			ComplexQueryResult<PriceDisplayInterface> atmCqr = entry.getValue();
			Exception e = null;
			if(atmCqr==null){
				e = Utils.IllState(this.getClass(), atmSn + " no atm cqr");
			}
			
			if(!atmCqr.isValidResult()){
				e = atmCqr.getException();
			}
			if(e!=null){
				// You had a problem getting the the atm price.  Report that
				//  to the caller
				ComplexQueryResult<BigDecimal> volCqr = new ComplexQueryResult<BigDecimal>(e, null);
				ret.put(atmSn, volCqr);
				continue;
			}

			PriceDisplayInterface atmPdi = atmCqr.getResult();
			//  Get the option that is closest to the money for this underlying
			SecDef optSd = getBestStrike(atmPdi, maxPercDiffBtwSettleAndOpStrikeToAllow);
			if(optSd!=null){
				derivToUnderMap.put(optSd.getShortName(),atmSn);
			}else{
				ret.put(atmSn, 
						new ComplexQueryResult<BigDecimal>(
								Utils.IllState(this.getClass(), atmSn+" can't find best option SecDef "), 
								null));
			}
		}
		
		// Get deriviative pdi's
		Map<String, ComplexQueryResult<PriceDisplayInterface>> derivPdis = 
				pdiQuery.get(derivToUnderMap.keySet(), 10, TimeUnit.SECONDS);
		
		// Get implied vols here
		Map<String,BigDecimal> volMap = 
				getImpliedVolPerUnderlying(atmPdis, derivPdis, ratesMap,divsMap,derivToUnderMap);
		// Check for null returns of implied vols.
		for(Entry<String, BigDecimal> entry:volMap.entrySet()){
			String underSn = entry.getKey();
			BigDecimal vol = entry.getValue();
			if(vol==null){
				Exception e = Utils.IllState(this.getClass(), underSn+" can't compute vol");
				ret.put(underSn, new ComplexQueryResult<BigDecimal>(e, null));
				continue;
			}
			ret.put(underSn, new ComplexQueryResult<BigDecimal>(null, vol));
		}
		
		return ret;
	}
	

	private SecDef getBestStrike(
			 PriceDisplayInterface underlyingPdi,
			BigDecimal maxPercDiffBtwSettleAndOpStrikeToAllow){
		
		String shortName = underlyingPdi.getShortName();
		
		SecDef bestOptionSecDef  = null;
		
		// Get option chains
		TreeMap<Long, TreeMap<String,TreeMap<BigDecimal, SecDef>>> chains = 
				chainQuery.get(shortName, 40, TimeUnit.SECONDS);
		if(chains==null || chains.size()<1)return null;
		
		/// You've got options chains.  Now get the calls of the one with the lastest expiry
		TreeMap<String,TreeMap<BigDecimal, SecDef>> tm = 
				GoogleOptionChainQuery.getLastestExpiryMap(chains);
		if(tm==null || tm.size()<1)return null;
		// are there calls, try that first
		BigDecimal settle  = underlyingPdi.getSettlement();
		if(tm.containsKey(SecRight.C.toString())){
			TreeMap<BigDecimal, SecDef> strikeToSecDef = tm.get(SecRight.C.toString());
			// return call
			// take closest to the money atm call option (lowest strike that is still >= settle
			BigDecimal bestExpiry = strikeToSecDef.ceilingKey(settle);
			bestOptionSecDef = strikeToSecDef.get(bestExpiry);
		}else if(tm.containsKey(SecRight.P.toString())){
			// try puts
			TreeMap<BigDecimal, SecDef> strikeToSecDef = tm.get(SecRight.P.toString());
			// return put
			// take closest to the money atm call option (highest strike that is still <= settle
			BigDecimal bestExpiry = strikeToSecDef.floorKey(settle);
			bestOptionSecDef = strikeToSecDef.get(bestExpiry);
		}
		return bestOptionSecDef;
	}
	
	
	
	
	private final Map<String,BigDecimal> getImpliedVolPerUnderlying(
			Map<String, ComplexQueryResult<PriceDisplayInterface>> underlyingPdiMap,
			Map<String, ComplexQueryResult<PriceDisplayInterface>> optionPdiMap,
			Map<String, ComplexQueryResult<BigDecimal>> ratesMap,
			Map<String, ComplexQueryResult<BigDecimal>> divsMap,
			Map<String,String> optToUnderlyingSnMap){
		Map<String,ImplieDVolInputs> impliedInputsMap = new HashMap<String, YahooAtmVolCqrQueryForStks.ImplieDVolInputs>();
		for(ComplexQueryResult<PriceDisplayInterface> optCqr : optionPdiMap.values()){
			if(optCqr==null || !optCqr.isValidResult()){
				continue;
			}
			// get option info
			PriceDisplayInterface optPdi = optCqr.getResult();
			String optSn = optPdi.getShortName();
			double optPrice = optPdi.getSettlement().doubleValue();
			String underSn = optToUnderlyingSnMap.get(optSn);
			
			// get underlying info
			ComplexQueryResult<PriceDisplayInterface> underCqr = 
					underlyingPdiMap.get(underSn);
			if(underCqr==null || !underCqr.isValidResult()){
				impliedInputsMap.put(underSn, null);
				continue;
			}
			PriceDisplayInterface underPdi = underCqr.getResult(); 
			double underPrice = underPdi.getSettlement().doubleValue();
			SecDef optSd = sdQuery.get(optPdi.getShortName(),1, TimeUnit.SECONDS);
			long dte = MarketDataComLib.getDaysToExpirationFromSd(evalDate, optSd);
			double strike = optSd.getStrike().doubleValue();
			ComplexQueryResult<BigDecimal> rateCqr = ratesMap.get(underSn);
			double rate = Double.NaN;
			if(rateCqr!=null && rateCqr.isValidResult()){
				rate = rateCqr.getResult().doubleValue();
			}
			ComplexQueryResult<BigDecimal> divCqr = divsMap.get(underSn);
			double div = Double.NaN;
			if(divCqr!=null && divCqr.isValidResult()){
				div = divCqr.getResult().doubleValue();
			}
			ImplieDVolInputs impIns = 
					new ImplieDVolInputs(underSn, optSn, optSd.getRight(), underPrice, strike, dte,rate,div, optPrice);
			impliedInputsMap.put(underSn, impIns);
		}

		Map<String,BigDecimal> ret = new HashMap<String, BigDecimal>();
		for(ImplieDVolInputs impIn : impliedInputsMap.values()){
			double callIsZero = 0;
			if(impIn.right.compareTo("C")!=0){
				callIsZero = 1;
			}
			double dteAsPercOfYear = impIn.dte/daysInYear;
			BigDecimal vol = impliedVol(callIsZero,impIn.atm,impIn.strike,seedVol,
					dteAsPercOfYear,impIn.rate,impIn.div,impIn.optPrice);
			ret.put(impIn.underlyingSn, vol);
		}
		return ret;	
	}
	
	
	private final BigDecimal impliedVol(
			double callIsZero,
			double atm,
			double strike,
			double seedVol,
			double dte,
			double rate,
			double div,
			double optPrice){
		double iopt  = callIsZero !=0 ? 1 : 0; // make option type param default to 1 for anything that's not zero (call)
		Number[] params = {iopt,atm,strike,dte,seedVol,rate,div};
		double impVol = DerivativeModel.impliedVol(0, params, 4, 0, optPrice, model,seedVol);
		return new BigDecimal(impVol);
		
//		double vol = AnalyticFormulas.blackScholesOptionImpliedVolatility(
//				atm,
//				dte,
//				strike,
//				1,
//				optPrice);
//
//		return new BigDecimal(vol);
	}
	
	
	private static final class ImplieDVolInputs{
		String underlyingSn;
		@SuppressWarnings("unused")
		String closestOptionSn;
		String right;
		double atm;
		double strike;
		double dte;
		double rate=.01;
		double div=.01;
		double optPrice;
		private ImplieDVolInputs(String underlyingSn, String closestOptionSn,
				String right,double atm, double strike,double dte, 
				double rate, double div,double optPrice) {
			super();
			this.underlyingSn = underlyingSn;
			this.closestOptionSn = closestOptionSn;
			this.right = right;
			this.atm = atm;
			this.strike = strike;
			this.rate = rate;
			this.div = div;
			this.dte = dte;
			this.optPrice = optPrice;
		}
		
	}

	public static void main(String[] args) {
		
		YahooAtmVolCqrQueryForStks q = 
				new YahooAtmVolCqrQueryForStks();
		String[] stkArray = 
			{
				"IBM.STK.SMART",
				"AAPL.STK.SMART",
				"MSFT.STK.SMART",
				"GOOG.STK.SMART",
			};
		Set<String> underlyingSnSet = 
				CollectionsStaticMethods.setFromArray(stkArray);
		Map<String, ComplexQueryResult<BigDecimal>> volCqrMap = 
				q.get(underlyingSnSet, 10, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(volCqrMap);

	}

}
