package com.billybyte.dse.outputs;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.InBlk;

import com.billybyte.marketdata.SecDef;
import com.billybyte.portfolio.PositionData;
import com.billybyte.queries.ComplexQueryResult;

public class GreeksOutput {
	private final Map<DerivativeSensitivityTypeInterface,Map<String,Map<String,BigDecimal>>> greeksMap = 
			new HashMap<DerivativeSensitivityTypeInterface, Map<String,Map<String,BigDecimal>>>();
//	private final Map<DerivativeSensitivityTypeInterface,Map<String,Map<String,Exception>>> greeksExceptionMap = 
//			new HashMap<DerivativeSensitivityTypeInterface, Map<String,Map<String,Exception>>>();
	
	/**
	 * Create  a greeksMap and a greeksExceptionMap
	 * @param sensitivities
	 * @param pdList
	 * @param de
	 */
	public GreeksOutput(
			DerivativeSensitivityTypeInterface[] sensitivities,
			List<PositionData>pdList,
			DerivativeSetEngine de,
			BigDecimal greeksExceptionValue){
		
		Set<String> snSet = new HashSet<String>();
		for(PositionData pd:pdList){
			snSet.add(pd.getShortName());
		}
		Map<String, ComplexQueryResult<InBlk>> inputs =de.getInputs(snSet);
		for(DerivativeSensitivityTypeInterface sense:sensitivities){
			//******* !!!!!!!! HERE'S THE CALL TO DerivativeSetEngine !!!*************
//			Map<String, DerivativeReturn[]> derivRetMap = 
//					de.getSensitivity(sense, snSet);
			Map<String, DerivativeReturn[]> derivRetMap = 
					de.getSensitivity(sense, snSet,inputs);

			// now do all of the work getting the results into the greeksMap
			for(String derivativeShortName:snSet){
				DerivativeReturn[] drArr= derivRetMap.get(derivativeShortName);

				for(DerivativeReturn dr: drArr){
					putToGreeksMap(dr, derivativeShortName,greeksExceptionValue);

//					DerivativeSensitivityTypeInterface senseForDr = dr.getSensitivityId();
//					if(!greeksMap.containsKey(senseForDr)){
//						greeksMap.put(senseForDr,new HashMap<String,Map<String,BigDecimal>>());
//					}
//					Map<String,Map<String,BigDecimal>> mapOuter = greeksMap.get(senseForDr);
//					if(!mapOuter.containsKey(derivativeShortName)){
//						mapOuter.put(derivativeShortName, new HashMap<String, BigDecimal>());
//					}
//					Map<String, BigDecimal> map = mapOuter.get(derivativeShortName);
//					String withRespectToShortName = dr.getWithRespectToShortName();
//					BigDecimal value = BigDecimal.ZERO;
//					if(dr.isValidReturn()) {
//						Utils.prtObMess(this.getClass(), dr.toString());
//						try {
//							value = new BigDecimal(dr.getValue().toString());
//							map.put(withRespectToShortName, value);
//						} catch (NumberFormatException e) {
//							e.printStackTrace();
//						}
//					}else{
//						
//					}
				}
			}
		}
	}
	
//	private void putToExceptionMap(DerivativeReturn dr, String derivativeShortName){
//		DerivativeSensitivityTypeInterface senseForDr = dr.getSensitivityId();
//		
//		if(!greeksExceptionMap.containsKey(senseForDr)){
//			greeksExceptionMap.put(senseForDr,new HashMap<String,Map<String,Exception>>());
//		}
//		Map<String,Map<String,Exception>> mapOuter = greeksExceptionMap.get(senseForDr);
//		if(!mapOuter.containsKey(derivativeShortName)){
//			mapOuter.put(derivativeShortName, new HashMap<String, Exception>());
//		}
//		Map<String, Exception> map = mapOuter.get(derivativeShortName);
//		String withRespectToShortName = dr.getWithRespectToShortName();
//		Utils.prtObErrMess(this.getClass(), dr.toString());
//		map.put(withRespectToShortName, dr.getException());
//	}

	/**
	 * Main routine that populates a map of 3 levels
	 * level0 = DerivativeSensitivityTypeInterface
	 * level1 = derivativeShortName
	 * level2 = withRespectToShortName
	 * value = BigDecimal greek value or exception value
	 * 
	 * @param dr = DerivativeReturn
	 * @param derivativeShortName = String
	 * @param greeksExceptionValue = BigDecimal
	 */
	private void putToGreeksMap(
			DerivativeReturn dr, 
			String derivativeShortName,
			BigDecimal greeksExceptionValue){
		
		DerivativeSensitivityTypeInterface senseForDr = dr.getSensitivityId();
		
		if(!greeksMap.containsKey(senseForDr)){
			greeksMap.put(senseForDr,new HashMap<String,Map<String,BigDecimal>>());
		}
		Map<String,Map<String,BigDecimal>> mapOuter = greeksMap.get(senseForDr);
		if(!mapOuter.containsKey(derivativeShortName)){
			mapOuter.put(derivativeShortName, new HashMap<String, BigDecimal>());
		}
		Map<String, BigDecimal> map = mapOuter.get(derivativeShortName);
		String withRespectToShortName = dr.getWithRespectToShortName();
//		BigDecimal value = BigDecimal.ZERO;
		Utils.prtObMess(this.getClass(), dr.toString());
		if(dr.isValidReturn()){
			try {
				BigDecimal value = new BigDecimal(dr.getValue().toString());
				map.put(withRespectToShortName, value);
			} catch (NumberFormatException e) {
				map.put(withRespectToShortName, greeksExceptionValue.setScale(5,RoundingMode.HALF_EVEN));			
			}
		}else{
			map.put(withRespectToShortName, greeksExceptionValue.setScale(5,RoundingMode.HALF_EVEN));			
		}

	}

	
//	public Map<DerivativeSensitivityTypeInterface,Map<String,Map<String,Exception>>> getGreeksExceptionMap(){
//		return this.greeksExceptionMap;
//	}

	public Map<DerivativeSensitivityTypeInterface, Map<String,Map<String, BigDecimal>>> getGreeksMap() {
		return greeksMap;
	}
	/**
	 * This method creates a 3 level map:
	 * 	level 1 key = derivativeShortName;
	 *  level 2 key = underlyingShortName;
	 *  level 3 key = DerivativeSensitivityTypeInterface
	 *  The value is the greek value.
	 * @return
	 */
	public Map<String,Map<String,Map<DerivativeSensitivityTypeInterface,BigDecimal>>> getUnwrappedGreeksMap() {
		Map<String,Map<String,Map<DerivativeSensitivityTypeInterface,BigDecimal>>> retMap = 
				new HashMap<String,Map<String,Map<DerivativeSensitivityTypeInterface,BigDecimal>>>();
		for(Entry<DerivativeSensitivityTypeInterface,Map<String,Map<String,BigDecimal>>> entry:greeksMap.entrySet()){
			DerivativeSensitivityTypeInterface sense = entry.getKey();
			for(Entry<String,Map<String,BigDecimal>> subEntry:entry.getValue().entrySet()){
				String derivSn = subEntry.getKey();
				for(Entry<String,BigDecimal> subSubEntry:subEntry.getValue().entrySet()){
					String withRespectTo = subSubEntry.getKey();
					BigDecimal senseValue = subSubEntry.getValue();
					if(retMap.containsKey(derivSn)){
						if(retMap.get(derivSn).containsKey(withRespectTo)){
							retMap.get(derivSn).get(withRespectTo).put(sense, senseValue);
						}else{
							Map<DerivativeSensitivityTypeInterface,BigDecimal> sensValMap = new 
									HashMap<DerivativeSensitivityTypeInterface,BigDecimal>();
							sensValMap.put(sense, senseValue);
							retMap.get(derivSn).put(withRespectTo, sensValMap);
						}
					}else{
						Map<DerivativeSensitivityTypeInterface,BigDecimal> sensValMap = new 
								HashMap<DerivativeSensitivityTypeInterface,BigDecimal>();
						sensValMap.put(sense, senseValue);
						Map<String,Map<DerivativeSensitivityTypeInterface,BigDecimal>> withRespectToMap =
								new HashMap<String,Map<DerivativeSensitivityTypeInterface,BigDecimal>>();
						withRespectToMap.put(withRespectTo, sensValMap);
						retMap.put(derivSn, withRespectToMap);
					}
				}
			}
		}
		return retMap;
	}
	
	/**
	 * This method gets a map of Map<DerivativeSensitivityTypeInterface, BigDecimal> 
	 *   for each pair of derivShortName and underlyingName.
	 * @param derivShortName
	 * @param underlyingName
	 * @return
	 */
	public Map<DerivativeSensitivityTypeInterface, BigDecimal>  getGreeks(String derivShortName, String underlyingName){
		if(!this.getUnwrappedGreeksMap().containsKey(derivShortName)){
			return null;
		}
		Map<String, Map<DerivativeSensitivityTypeInterface, BigDecimal>> level2 = this.getUnwrappedGreeksMap().get(derivShortName);
		if(!level2.containsKey(underlyingName)){
			return null;
		}
		Map<DerivativeSensitivityTypeInterface, BigDecimal> level3 = 
				new HashMap<DerivativeSensitivityTypeInterface, BigDecimal>(
						level2.get(underlyingName));
		if(level2.containsKey(derivShortName)){
			level3.putAll(level2.get(derivShortName));
		}
		return level3;
	}
	
	public List<PositDataAndGreeks> getPositDataAndGreeksListSinglePd(
			PositionData pd, DerivativeSetEngine de, int timeoutValue, TimeUnit timeUnitType){
		List<SecDef> underlyingSds = de.getQueryManager().getUnderlyingSecDefs(
				pd.getShortName(), timeoutValue, timeUnitType);
		List<PositDataAndGreeks> ret = new ArrayList<PositDataAndGreeks>();
		for(SecDef sd:underlyingSds){
			Map<DerivativeSensitivityTypeInterface, BigDecimal> greekMap = 
					getGreeks(pd.getShortName(),sd.getShortName());
			if(greekMap==null){
				greekMap = 
						getGreeks(pd.getShortName(),sd.getShortName());
				IllegalStateException e = Utils.IllState(this.getClass(),pd.toString()+ " Can not create a greekMap (Map<DerivativeSensitivityTypeInterface, BigDecimal>) for this PositionData record"); 
				throw e;
			}
			PositDataAndGreeks pdg = 
			 	new PositDataAndGreeks(pd, greekMap, sd.getShortName());
			ret.add(pdg);
		}
		return ret;
	}
	
	public List<PositDataAndGreeks> getPositDataAndGreeksListAllPds(
			List<PositionData> pdList,DerivativeSetEngine de, int timeoutValue, TimeUnit timeUnitType){
		List<PositDataAndGreeks> ret = new ArrayList<PositDataAndGreeks>();
		for(PositionData pd:pdList){
			List<PositDataAndGreeks> pdSubList=null;
			try {
				pdSubList = getPositDataAndGreeksListSinglePd(pd, de, timeoutValue, timeUnitType);
				ret.addAll(pdSubList);
			} catch (Exception e) {
				e.printStackTrace();
				PositDataAndGreeks errorPdg = new PositDataAndGreeks(pd, null, null);
				ret.add(errorPdg);
			}
		}
		return ret;
	}
	
}
