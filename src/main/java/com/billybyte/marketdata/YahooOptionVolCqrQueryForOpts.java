package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.models.vanilla.BawAmerican;
import com.billybyte.dse.queries.DivDseInputQuery;
import com.billybyte.dse.queries.TreasuryRateQueryFromTreasuryRateSingle;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.marketdata.YahooAtmVolCqrQueryForStks.ImplieDVolInputs;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Get vols for Options 
 *    
 * @author bperlman1
 *
 */
public class YahooOptionVolCqrQueryForOpts implements QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>>{
	private final BawAmerican model = new BawAmerican();
	private final double seedVol = .2; 
	private final QueryInterface<String, SecDef> sdQuery = new SecDefQueryAllMarkets();
	private final double daysInYear;

	private final ConcurrentHashMap<String, ComplexQueryResult<BigDecimal>> cache = 
			new ConcurrentHashMap<String, ComplexQueryResult<BigDecimal>>();
	
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
	public YahooOptionVolCqrQueryForOpts(
			QueryInterface<Set<String>, 
				Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			QueryInterface<Set<String>, 
				Map<String, ComplexQueryResult<BigDecimal>>> rateQuery,
				QueryInterface<Set<String>, 
				Map<String, ComplexQueryResult<BigDecimal>>> divQuery,
				Calendar evalDate) {

		super();
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
		this.daysInYear = 365;
	}
	
	
	public YahooOptionVolCqrQueryForOpts(){
		this(
				new YahooCombinedStkOptPdiSetCqrRetQuery(),
				null,null,Calendar.getInstance());
		
	}
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> optKeySet,
			int timeoutValue, TimeUnit timeUnitType) {
		// Create return object
		Map<String, ComplexQueryResult<BigDecimal>> ret = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		// anything cached?
		Set<String> keySet = new HashSet<String>();
		for(String optKeySn:optKeySet){
			if(cache.containsKey(optKeySn)){
				ret.put(optKeySn, cache.get(optKeySn));
			}else{
				keySet.add(optKeySn);
			}
		}
		if(keySet.size()<1){
			return ret;
		}
		
		// Derivative shortName to Underlying shortname map
		Map<String,String> derivToUnderMap = new HashMap<String,String>();
		for(String optSn:keySet){
			SecDef sd = sdQuery.get(optSn, timeoutValue, timeUnitType);
			if(sd.getSymbolType()!=SecSymbolType.OPT){
				ret.put(optSn,errRet(optSn+" not a stock option"));
				continue;
			}
			String sp = MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR;
			String underSn = sd.getSymbol()+sp+SecSymbolType.STK+sp+sd.getExchange().toString();
			SecDef underSd = sdQuery.get(underSn, timeoutValue, timeUnitType);
			if(underSd==null){
				ret.put(optSn,errRet(optSn+" no valid underling SecDef"));
				continue;
			}
			derivToUnderMap.put(optSn,underSd.getShortName());
		}
		
		
		
		// Get rates and div rates
		Set<String> underSnSet = new HashSet<String>(derivToUnderMap.values());
		Map<String, ComplexQueryResult<BigDecimal>> ratesMap = 
				rateQuery.get(underSnSet, timeoutValue, timeUnitType);
		Map<String, ComplexQueryResult<BigDecimal>> divsMap = 
				divQuery.get(underSnSet, timeoutValue, timeUnitType);
		
		// Get settle pdi's
		Map<String, ComplexQueryResult<PriceDisplayInterface>> atmPdis = 
				pdiQuery.get(underSnSet, 10, TimeUnit.SECONDS);
		
		// Get deriviative pdi's
		Map<String, ComplexQueryResult<PriceDisplayInterface>> derivPdis = 
				pdiQuery.get(derivToUnderMap.keySet(), 10, TimeUnit.SECONDS);

		// Process vols of derivatives
		Map<String, BigDecimal> volMap = 
				getImpliedVol(atmPdis, derivPdis, ratesMap, divsMap, derivToUnderMap);
		
		
		
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
		
		cache.putAll(ret);
		return ret;
	}
	

	
	
	
	private final Map<String,BigDecimal> getImpliedVol(
			Map<String, ComplexQueryResult<PriceDisplayInterface>> underlyingPdiMap,
			Map<String, ComplexQueryResult<PriceDisplayInterface>> optionPdiMap,
			Map<String, ComplexQueryResult<BigDecimal>> ratesMap,
			Map<String, ComplexQueryResult<BigDecimal>> divsMap,
			Map<String,String> optToUnderlyingSnMap){
		
		
		Map<String,ImplieDVolInputs> impliedInputsMap = new HashMap<String, ImplieDVolInputs>();
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
				impliedInputsMap.put(optSn, null);
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
					new ImplieDVolInputs(underSn, optSn, optSd.getRight(), underPrice, strike, dte,rate,div, optPrice,model);
			impliedInputsMap.put(optSn, impIns);
		}

		Map<String,BigDecimal> ret = new HashMap<String, BigDecimal>();
		for(ImplieDVolInputs impIn : impliedInputsMap.values()){
			double callIsZero = 0;
			if(impIn.right.compareTo("C")!=0){
				callIsZero = 1;
			}
			double dteAsPercOfYear = impIn.dte/daysInYear;
			BigDecimal vol = YahooAtmVolCqrQueryForStks.impliedVol(
					callIsZero,impIn.atm,impIn.strike,seedVol,
					dteAsPercOfYear,impIn.rate,impIn.div,impIn.optPrice,model);
			ret.put(impIn.closestOptionSn, vol);
		}
		return ret;	
	}
	
	
	
	private final ComplexQueryResult<BigDecimal> errRet(String mess){
		Exception e =  Utils.IllState(this.getClass(), mess);
		return new ComplexQueryResult<BigDecimal>(e, null);
	}

	public static void main(String[] args) {
		
		YahooOptionVolCqrQueryForOpts q = 
				new YahooOptionVolCqrQueryForOpts();
		Set<String> snSet = new HashSet<String>();
		snSet.add("IBM.OPT.SMART.USD.20170120.C.170.00");
		snSet.add("IBM.OPT.SMART.USD.20170120.C.200.00");
		snSet.add("IBM.OPT.SMART.USD.20170120.C.165.00");
		snSet.add("MSFT.OPT.SMART.USD.20170120.C.45.00");
		Set<String> optionSnSet = 
				CollectionsStaticMethods.setFromArray(snSet);
		Map<String, ComplexQueryResult<BigDecimal>> volCqrMap = 
				q.get(optionSnSet, 10, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(volCqrMap);
		// do it again to see if cache works
		volCqrMap = 
				q.get(optionSnSet, 10, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(volCqrMap);

	}

}
