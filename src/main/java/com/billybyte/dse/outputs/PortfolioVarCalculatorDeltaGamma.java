package com.billybyte.dse.outputs;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.portfolio.PositionData;
import com.billybyte.portfolio.PositionData.TradeTypeEnum;
import com.billybyte.queries.ComplexQueryResult;
/**
 * Calculate Portfolio DeltaGamma VaR
 * This VaR calculator will turn all securities that are not priced using a vanilla model
 *   into a set of "psuedo" securities that can be priced using a vanilla model.
 * @author bperlman1
 *
 */
public class PortfolioVarCalculatorDeltaGamma {
	private final BigDecimal DEFAULT_CORRELATION = BigDecimal.ZERO;
	private final String STOCKOPT_ID = MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR.toString() + SecSymbolType.OPT + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR;
	private final BigDecimal DEFAULT_STOCK_OPTION_MULTIPLIER = new BigDecimal("100");
	private final int DEFAULT_QTY_SCALE_OF_NEW_POSITIONS = 7;
	private final DirectionalVarDerSen dirVarSen = new  DirectionalVarDerSen();
	private final DeltaNeutralVarDerSen delNeuVarSen = new DeltaNeutralVarDerSen();
	private final boolean debug; // = true;
	private final BigDecimal two = new BigDecimal("2");
	private final DerivativeSetEngine de;
//	private final QueryInterface<Set<String>,
//		Map<String,ComplexQueryResult<BigDecimal>>> corrPairQuery;
	private final QueryInterface<Set<String>,
		Map<String,ComplexQueryResult<BigDecimal>>> corrQuery;
	private final Map<String, BigDecimal> unitDirectionalVarCache = new ConcurrentHashMap<String, BigDecimal>();
	private final Map<String, BigDecimal> unitDeltaNeutrallVarCache = new ConcurrentHashMap<String, BigDecimal>();
	private final Map<DerivativeSensitivityTypeInterface,
		Map<String, BigDecimal>> unitVarCache;
	private final QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>> volQuery ;
	private final Map<String,List<SecDef>> underSdMap = new ConcurrentHashMap<String, List<SecDef>>();



	
	
	/**
	 * 
	 * @param de DerivativeSetEngine
	 */
	public PortfolioVarCalculatorDeltaGamma(
			DerivativeSetEngine de){
		
		this.de = de;
		this.volQuery = de.getQueryManager().getQuery(new VolDiot());
		this.debug =false;
//		Map<String, DerivativeModelInterface> modelMapForVar = new HashMap<String, DerivativeModelInterface>();
//		modelMapForVar.put("\\.((FUT)|(STK)|(FOP)|(OPT))\\.", new VanillaForVar(de.getEvaluationDate()));
//		de.replaceAllModels(modelMapForVar);
		corrQuery =
				de.getQueryManager().getQuery(new CorrDiot());
		unitVarCache = new ConcurrentHashMap<DerivativeSensitivityTypeInterface, Map<String,BigDecimal>>();
		unitVarCache.put(dirVarSen, unitDirectionalVarCache);
		unitVarCache.put(delNeuVarSen, unitDeltaNeutrallVarCache);
		
	}

	/**
	 * 
	 * @param de DerivativeSetEngine
	 */
	public PortfolioVarCalculatorDeltaGamma(
			DerivativeSetEngine de, Boolean debug){
		
		this.de = de;
		this.volQuery = de.getQueryManager().getQuery(new VolDiot());
		this.debug = debug;
//		Map<String, DerivativeModelInterface> modelMapForVar = new HashMap<String, DerivativeModelInterface>();
//		modelMapForVar.put("\\.((FUT)|(STK)|(FOP)|(OPT))\\.", new VanillaForVar(de.getEvaluationDate()));
//		de.replaceAllModels(modelMapForVar);
		corrQuery =
				de.getQueryManager().getQuery(new CorrDiot());
		unitVarCache = new ConcurrentHashMap<DerivativeSensitivityTypeInterface, Map<String,BigDecimal>>();
		unitVarCache.put(dirVarSen, unitDirectionalVarCache);
		unitVarCache.put(delNeuVarSen, unitDeltaNeutrallVarCache);
		
	}

	/**
	 * Calculate DirectionalVar + DeltaNeutralVar
	 * @param pds List<PositionData> - list of PositionData 
	 * @param timeoutValue int 
	 * @param timeUnitType TimeUnit
	 * @return  ComplexQueryResult<BigDecimal> DirectionalVar + DeltaNeutralVar
	 */
	public ComplexQueryResult<BigDecimal> getVar(
			List<PositionData> pds,
			int timeoutValue,
			TimeUnit timeUnitType){
	
	
		ComplexQueryResult<BigDecimal> directVarCqr = getVar(new DirectionalVarDerSen(), pds, 10, TimeUnit.SECONDS);
		ComplexQueryResult<BigDecimal> deltaNeutralVarCqr = getVar(new DeltaNeutralVarDerSen(), pds, 10, TimeUnit.SECONDS);
		if(directVarCqr.isValidResult() && deltaNeutralVarCqr.isValidResult()){
			BigDecimal total = directVarCqr.getResult().add(deltaNeutralVarCqr.getResult());
			ComplexQueryResult<BigDecimal> ret = 
					new ComplexQueryResult<BigDecimal>(null, total);
			return ret;
		} else {
			ComplexQueryResult<BigDecimal> err;
			if(
				directVarCqr.getException()!=null&&
				directVarCqr.getException().getMessage()!=null&&
				deltaNeutralVarCqr.getException()!=null&&
				deltaNeutralVarCqr.getException().getMessage()!=null) {
				err = 
						MarketDataComLib.errorRet(directVarCqr.getException().getMessage() + " : " + 
								deltaNeutralVarCqr.getException().getMessage());
			} else {
				err = MarketDataComLib.errorRet("Null exception message returned from PortfolioVarCalculatorDeltaGamma");
			}
			return err;
		}
		
	}
	
	/**
	 * 
	 * @param varType
	 * @param pds
	 * @param timeoutValue
	 * @param timeUnitType
	 * 
	 * @return
	 * 
	 */
	public ComplexQueryResult<BigDecimal> getVar(
			DerivativeSensitivityTypeInterface varType,
			List<PositionData> pds,
			int timeoutValue,
			TimeUnit timeUnitType){
		
		try {
			Map<String, BigDecimal> totalUnderlyingVarMap=null;
			try {
				totalUnderlyingVarMap = getTotalUnderlyingVarMap(varType,pds, timeoutValue, timeUnitType);
			} catch (Exception e1) {
				
				if(debug) {
					e1.printStackTrace();
				}
				String errMess = e1.getMessage();
				if(errMess==null){
					errMess = Utils.stackTraceAsString(e1);
				}
				ComplexQueryResult<BigDecimal> err = 
						MarketDataComLib.errorRet(
								" Exception processing totalUnderlyingVars : " + errMess);
				return err;
			}
			if(debug){
				CollectionsStaticMethods.prtMapItems(totalUnderlyingVarMap);
			}
			// total up squares of each total
			BigDecimal totalVarSquares = BigDecimal.ZERO;
			for (Entry<String, BigDecimal> entry : totalUnderlyingVarMap.entrySet()) {
				BigDecimal var = entry.getValue();
				totalVarSquares = totalVarSquares.add(var.pow(2));
			}
			
			// total up inner product * corrCoef
			TreeSet<String> ts = new TreeSet<String>(totalUnderlyingVarMap.keySet());
			List<String> snList = new ArrayList<String>(ts);
			int size = snList.size();
			
			
			Map<String,ComplexQueryResult<BigDecimal>> corrCoefMap=null;
			Set<String> snSet = new HashSet<String>(snList);
			try {
				corrCoefMap =
						corrQuery.get(snSet, timeoutValue, timeUnitType);
			} catch (Exception e) {
				Utils.prtObErrMess(this.getClass(),"error getting correlations: "+ e.getMessage());
				corrCoefMap = new HashMap<String, ComplexQueryResult<BigDecimal>>();
			}				
			
			
			// now do inner product
			Set<String> missingPairs = new HashSet<String>();
			for (int i = 0; i < size; i++) {
				for (int j = i + 1; j < size; j++) {
					// get coeff
					String sn0 = snList.get(i);
					String sn1 = snList.get(j);
					String pair = sn0+"__"+sn1;
//					if(cqr == null || !cqr.isValidResult()){
//						missingPairs.add(pair);
//						continue;
//					}
//					BigDecimal coef = cqr.getResult();

					BigDecimal coef = DEFAULT_CORRELATION;
					ComplexQueryResult<BigDecimal> cqr = corrCoefMap.get(pair);
					if(cqr!=null && cqr.isValidResult()){
						coef = cqr.getResult();
					}
					BigDecimal var0 = totalUnderlyingVarMap.get(sn0);
					BigDecimal var1 = totalUnderlyingVarMap.get(sn1);
					totalVarSquares = totalVarSquares.add(
							var0.multiply(var1).multiply(coef).multiply(two));
				}
			}
			
			if(missingPairs.size()>0){
				String missPairMess = "";
				for(String missPair : missingPairs){
					missPairMess += missPairMess + missPair + " ";
				}
				ComplexQueryResult<BigDecimal> cqr = 
						MarketDataComLib.errorRet("CORRELATON PAIRS ARE MISSING FROM corrQuery: " +missPairMess);
				return cqr;
			}
			
			if(totalVarSquares.compareTo(BigDecimal.ZERO)<0){
				Utils.prtObMess(PortfolioVarCalculatorDeltaGamma.class, " variance is less than zero.  Will use abs val of it");
				totalVarSquares = totalVarSquares.multiply(new BigDecimal("-1"));
			}
			BigDecimal finalVar = new BigDecimal(Math.sqrt(totalVarSquares.doubleValue())).setScale(10,RoundingMode.HALF_EVEN);
			ComplexQueryResult<BigDecimal> ret = 
					new ComplexQueryResult<BigDecimal>(null, finalVar);
			return ret;
			
		} catch (Exception e) {
			ComplexQueryResult<BigDecimal> cqr = 
					MarketDataComLib.errorRet(e);
			return cqr;
		}
		
	}
	
	
	
	/**
	 * If you have already created a corrCoefMap, and a totalUnderlyingVarMap, then you
	 *   can use this method to aviod the cost of creating these maps (calls to DSE, etc).
	 *   
	 * @param snList
	 * @param corrCoefMap
	 * @param totalUnderlyingVarMap
	 * @return
	 */
	public ComplexQueryResult<BigDecimal> getVarFromPreviousMaps(
			Map<String,ComplexQueryResult<BigDecimal>> corrCoefMap,
			Map<String, BigDecimal> totalUnderlyingVarMap){
		BigDecimal totalVarSquares = BigDecimal.ZERO;
		for (Entry<String, BigDecimal> entry : totalUnderlyingVarMap.entrySet()) {
			BigDecimal var = entry.getValue();
			totalVarSquares = totalVarSquares.add(var.pow(2));
		}
		
		// total up inner product * corrCoef
		TreeSet<String> ts = new TreeSet<String>(totalUnderlyingVarMap.keySet());
		List<String> snList = new ArrayList<String>(ts);
		int size = snList.size();
		Set<String> missingPairs = new HashSet<String>();
		for (int i = 0; i < size; i++) {
			for (int j = i + 1; j < size; j++) {
				// get coeff
				String sn0 = snList.get(i);
				String sn1 = snList.get(j);
				String pair = sn0+"__"+sn1;
//				if(cqr == null || !cqr.isValidResult()){
//					missingPairs.add(pair);
//					continue;
//				}
//				BigDecimal coef = cqr.getResult();

				BigDecimal coef = DEFAULT_CORRELATION;
				ComplexQueryResult<BigDecimal> cqr = corrCoefMap.get(pair);
				if(cqr!=null && cqr.isValidResult()){
					coef = cqr.getResult();
				}
				BigDecimal var0 = totalUnderlyingVarMap.get(sn0);
				BigDecimal var1 = totalUnderlyingVarMap.get(sn1);
				totalVarSquares = totalVarSquares.add(
						var0.multiply(var1).multiply(coef).multiply(two));
			}
		}
		
		if(missingPairs.size()>0){
			String missPairMess = "";
			for(String missPair : missingPairs){
				missPairMess += missPairMess + missPair + " ";
			}
			ComplexQueryResult<BigDecimal> cqr = 
					MarketDataComLib.errorRet("CORRELATON PAIRS ARE MISSING FROM corrQuery: " +missPairMess);
			return cqr;
		}
		
		if(totalVarSquares.compareTo(BigDecimal.ZERO)<0){
			Utils.prtObMess(PortfolioVarCalculatorDeltaGamma.class, " variance is less than zero.  Will use abs val of it");
			totalVarSquares = totalVarSquares.multiply(new BigDecimal("-1"));
		}
		BigDecimal finalVar = new BigDecimal(Math.sqrt(totalVarSquares.doubleValue())).setScale(10,RoundingMode.HALF_EVEN);
		ComplexQueryResult<BigDecimal> ret = 
				new ComplexQueryResult<BigDecimal>(null, finalVar);
		return ret;

	}

	
	/**
	 * Aggregate UnitVar * qty by underlying
	 * If type is OPT, then multiply by 100
	 * 
	 * If a position has a security like a CSO (e.g G3.FOP.NYMEX.USD.201310.P.-0.500),
	 *    Then turn that position into it's "psuedo positions".
	 *    Psuedo positions are positions that have  quantities equal to
	 *    the quantity of the CSO * the delta of each leg.
	 *    
	 * 
	 * @param pds
	 * @param timeoutValue
	 * @param timeUnitType
	 * @return
	 */
	public Map<String, BigDecimal> getTotalUnderlyingVarMap(
			DerivativeSensitivityTypeInterface varSense,
			List<PositionData> pdList,
			int timeoutValue,
			TimeUnit timeUnitType){
		
		//******************** check to see if this is a valid Var DerivativeSensitivityTypeInterface instance *******************
		if(!unitVarCache.containsKey(varSense)){
			throw Utils.IllState(this.getClass(),varSense + " : is not supported for VaR in class " + this.getClass().getCanonicalName());
		}
		
		Set<String> snSet = new HashSet<String>();
		
		
		Set<String> missingSecDefs = new HashSet<String>();
		List<String> underList = new ArrayList<String>();
		Set<String> snSetOfMultiUnders = new HashSet<String>();
		List<PositionData> multis = new ArrayList<PositionData>();
		List<PositionData> pds = new ArrayList<PositionData>();
		
		//********************** check to see if all of the pds have valid underlyings  ************************
		// ***************** and build the List<PostionData> pds with PositionData instances that have *********
		//   ******** securities that are "single" underlying ******************************
		//   Further below, we will build "pseudo" positions where we break up the "multi" underlying security postions
		//    into multiple PositionData instances with qty's = qty * delta.
		for(PositionData pd : pdList){
			// check shortName
			String sn = pd.getShortName();
			if(sn==null){
				missingSecDefs.add(sn);
				continue;
			}

			// you can validate an option partially if it has an underlyingshortName
			List<SecDef> sds = de.getQueryManager().getUnderlyingSecDefs(
					sn, timeoutValue, timeUnitType);
			// see if underlying sds are ok
			if(sds == null || sds.size()<1){
				missingSecDefs.add(sn);
				continue;
			}
			// if more than one underlying, create entries in underSdMap for each 
			//   underlying sd, so that it can be used in the pseudo positions that get
			//   created below.
			if(sds.size()>1){
				// come here for multi underlying positions.  Don't add the pd to the pds list YET.
				//   add the underlying shortNames to the underSdMap, and the snSetOfMultiUnders.
				//   Then add the pd to the multis list, which we will process after this logic, below.
				snSetOfMultiUnders.add(sn);
				multis.add(pd);
				for(SecDef sd :sds){
					String underSn = sd.getShortName();
					if(!underSdMap.containsKey(underSn)){
						List<SecDef> sdListOfOneSecDef = 
								CollectionsStaticMethods.listFromArray(new SecDef[]{sd});
						underSdMap.put(underSn, sdListOfOneSecDef);
					}
					
				}
			}else{
				// this is a single underlying pd.  process normally.
				pds.add(pd);
				underSdMap.put(sn, sds);
				snSet.add(sn);
				for(SecDef sd: sds){
					underList.add(sd.getShortName());
				}
			}


		}
		
		//*********************** if any missing secDefNames = stop here ***********************
		if(missingSecDefs.size()>0){
			String missingSecDefNames = "";
			if(missingSecDefs.size()>0){
				for(String s : missingSecDefs){
					missingSecDefNames += s + "\n";
				}
			}
			String errorMess = 
					" missing Underlying SecDefs : " + missingSecDefNames;
			throw Utils.IllState(this.getClass(),errorMess);

		}
		
		Map<String,DerivativeReturn> invalidVarDrs = new HashMap<String,DerivativeReturn>();

		
		// *********************** now add in positions with multi underlyings as single underlying positions with their
		//   sizes multiplied by their deltas
		Map<String, DerivativeReturn[]> underlyingDeltas = 
				de.getSensitivity(new DeltaDerSen(), snSetOfMultiUnders);
		for(PositionData pd:multis){
			String sn = pd.getShortName();
			DerivativeReturn[] drs = underlyingDeltas.get(sn);
			for(DerivativeReturn dr:drs){
				if(dr.isValidReturn()){
					// create a synthetic position
					String shortName = dr.getWithRespectToShortName();
					String accountId = pd.getAccountId();
					String traderId = pd.getTraderId();
					TradeTypeEnum tradeType = pd.getTradeType();
					BigDecimal execPrice = pd.getExecPrice();
					BigDecimal refPrice = pd.getRefPrice();
					Long date = pd.getDate();
					BigDecimal multiplier = new BigDecimal(dr.getValue().toString()).setScale(
							DEFAULT_QTY_SCALE_OF_NEW_POSITIONS,RoundingMode.HALF_EVEN);
					BigDecimal qty = pd.getQty().multiply(multiplier);
					PositionData newPd = 
							new PositionData(shortName, accountId, traderId, tradeType, execPrice, refPrice, date, qty);
							
					pds.add(newPd);
					snSet.add(shortName);
				}else{
					invalidVarDrs.put(sn, dr);
				}
			}
		}

		
		//*************************** all is well so far *******************************
		Map<String, DerivativeReturn[]> drMap = new HashMap<String, DerivativeReturn[]>();
		if(snSet.size()>0){
			drMap =	de.getSensitivity(varSense, snSet);
		}
		Set<String> allNames = new HashSet<String>(snSet);
		allNames.addAll(underList);
		
		Map<String,ComplexQueryResult<BigDecimal>> volMap = 
				volQuery.get(allNames, 10, TimeUnit.SECONDS);
		
		Map<String, BigDecimal> ret = 
				new HashMap<String, BigDecimal>();
		
		List<Integer> badPds = new ArrayList<Integer>();
		Set<String> missingVarNames = new HashSet<String>();
	
		QueryInterface<String, SecDef> sdQuery = de.getSdQuery();
		for(int i = 0; i<pds.size() ; i++){
			//******** validate positionData *************
			PositionData pd = pds.get(i);
			if(pd==null || pd.getShortName() == null || pd.getQty() == null){
				badPds.add(i);
				continue;
			}
			String sn = pd.getShortName();
			//*************** get underlying ***********************
//			List<SecDef> sds = de.getQueryManager().getUnderlyingSecDefs(
//					sn, timeoutValue, timeUnitType);
			

			BigDecimal unitVar = null;
			// ****************   do we already have this unit var? *********************
			if(unitVarCache.get(varSense).containsKey(sn)){
				unitVar = unitVarCache.get(varSense).get(sn);
			}else
			{
				//***************** caculate and cache unit var *****************************
				//***************** make sure var sensitivity was returned *********************
				if(!drMap.containsKey(sn)){
					missingVarNames.add(sn);
					continue;
				}
				// *************** make sure a secdef exists **************
				SecDef derivSecDef = sdQuery.get(sn, timeoutValue, timeUnitType); 
				if(derivSecDef==null){
					derivSecDef = sdQuery.get(sn, timeoutValue, timeUnitType);
					missingSecDefs.add(sn);
					continue;
				}
				BigDecimal notionalMultiplier = derivSecDef.getMultiplier();
				//**************** validate De var response ********************
				DerivativeReturn[] drArr = drMap.get(sn);
				if(drArr==null || drArr.length<1){
					missingVarNames.add(sn);
					continue;
				}
				DerivativeReturn dr = drArr[0];
				if(!dr.isValidReturn()){
					invalidVarDrs.put(sn,dr);
					continue;
				}
				
				
				// all is well if you get here
				if(debug){
					Utils.prt(dr.toString());
				}

				unitVar = 
						new BigDecimal(dr.getValue().doubleValue()).setScale(
								6,RoundingMode.HALF_EVEN).multiply(notionalMultiplier);
				
				// ************ FOR DIRECTIONAL VAR  -  do scaling of options var 
				//    back to vol of underlying, so that converstions and reversals 
				//    end up not having any var
				//  ONLY DO THIS FOR OPTIONS WITH ONE UNDERLYING
				if(varSense.equals(dirVarSen) && derivSecDef.getStrike()!=null){
					List<SecDef> sds = underSdMap.get(sn);
					if(sds == null || sds.size()<1){
						missingSecDefs.add(sn);
						continue;
					}

					if(sds.size()==1){
						ComplexQueryResult<BigDecimal> underVolCqr = volMap.get(sds.get(0).getShortName());
						if(!underVolCqr.isValidResult()){
							missingVarNames.add(sn);
							continue;
						}
						BigDecimal underVol = underVolCqr.getResult();
						// assume that you can get this vol
						BigDecimal derivVol = volMap.get(sn).getResult();
						// get the ratil of underVol to derivVol
						BigDecimal ratio = underVol.divide(derivVol,15,RoundingMode.HALF_EVEN);
						unitVar = unitVar.multiply(ratio);
					}
				}
				unitVarCache.get(varSense).put(sn, unitVar);
				//***************** END caculate and cache unit var *****************************
			}
			// ****************  END do we already have this unit var? *********************

			
			// *************** now multiply unit var time quantity ***************
			BigDecimal qty = pd.getQty();
			// hacksville - should use secdef
			if(sn.contains(STOCKOPT_ID)){
					qty = qty.multiply(DEFAULT_STOCK_OPTION_MULTIPLIER);
			}
			// now get the totals by underlyingSn
			List<SecDef> sds = underSdMap.get(sn);
			String underlyingSn = sds.get(0).getShortName();
			if(!ret.containsKey(underlyingSn)){
				ret.put(underlyingSn, BigDecimal.ZERO);
			}
			
			BigDecimal currTotal = ret.get(underlyingSn);
			currTotal = currTotal.add(unitVar.multiply(qty));
			ret.put(underlyingSn, currTotal);
			
		}
		
		//********************** do final error checking ********************************
		if(missingVarNames.size()>0 || 
				invalidVarDrs.size()>0 || 
				badPds.size()>0 ||
				missingSecDefs.size()>0){
			
			String missingVars = "";
			if(missingVarNames.size()>0){
				for(String s : missingVarNames){
					missingVars += s + "\n";
				}
			}
			
			String missingSecDefNames = "";
			if(missingSecDefs.size()>0){
				for(String s : missingSecDefs){
					missingSecDefNames += s + "\n";
				}
			}

			String invalidVars = "";
			if(invalidVarDrs.size()>0){
				for(String s : invalidVarDrs.keySet()){
					DerivativeReturn dr = invalidVarDrs.get(s);
					if(!dr.isValidReturn()){
						invalidVars += s + " : " + invalidVarDrs.get(s).getException().toString() + " ; ";	
					}else{
						invalidVars += s + " unknown cause ;";
					}
					
				}
			}
			String invalidPds = "";
			if(badPds.size()>0){
				for(Integer badRow: badPds){
					invalidPds += badRow + " ";
				}
			}
			String errorMess = "missingVars : " + missingVars + 
					" invalidVars : " + invalidVars + 
					" bad Pd Rows : " + invalidPds +
					" missing SecDefs : " + missingSecDefNames;
			throw Utils.IllState(this.getClass(),errorMess);
		}
		
		return ret;
	}
	
	public Map<String,ComplexQueryResult<BigDecimal>> getCorrCoefficientMap(Set<String> snSet,int timeoutValue,TimeUnit timeUnitType){
		Map<String,ComplexQueryResult<BigDecimal>> corrCoefMap =
				corrQuery.get(snSet, timeoutValue, timeUnitType);
		return corrCoefMap;
	}

}
