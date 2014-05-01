package com.billybyte.dse.var.stressinputqueries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import Jama.Matrix;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.dse.var.McVar.StressInputQueryBlock;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class AtmStressInputQuery implements QueryInterface<StressInputQueryBlock,Map<String,double[]>> {
	private final DseInputQuery<BigDecimal> corrQuery;
	private final DseInputQuery<BigDecimal> atmQuery;
	private final DseInputQuery<BigDecimal> volQuery;
	private final DerivativeSetEngine dse;
	
	public AtmStressInputQuery(
			DerivativeSetEngine dse) {
		super();
		this.dse = dse;
		this.corrQuery = dse.getQueryManager().getQuery(new CorrDiot());
		this.atmQuery = dse.getQueryManager().getQuery(new AtmDiot());
		this.volQuery = dse.getQueryManager().getQuery(new VolDiot()); 
	}


	@Override
	public Map<String, double[]> get(StressInputQueryBlock inputBlock, int timeoutValue,
			TimeUnit timeUnitType) {
		Set<String> derivSnSet = inputBlock.getDerivSns();
		Map<String,List<String>> underlyingNamesMap = 
				MarketDataComLib.getUnderlyingsPerDerivName(derivSnSet, dse, timeoutValue, timeUnitType);
		Set<String> underlyingNames = new HashSet<String>();
		for(Entry<String, List<String>> entry:underlyingNamesMap.entrySet()){
			underlyingNames.addAll(entry.getValue());
		}
		int trials = inputBlock.getNumTrials();
		Map<String, ComplexQueryResult<BigDecimal>> corrMap =
				corrQuery.get(underlyingNames, 20, TimeUnit.SECONDS);
		String mess = MarketDataComLib.CqrInvalidResultListString(corrMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);

		Map<String, ComplexQueryResult<BigDecimal>> atmMap  = 
				atmQuery.get(underlyingNames, 30, TimeUnit.SECONDS);
		mess = MarketDataComLib.CqrInvalidResultListString(atmMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);
		Map<String,ComplexQueryResult<BigDecimal>>  volMap = 
				volQuery.get(underlyingNames, 30, TimeUnit.SECONDS);
		mess = MarketDataComLib.CqrInvalidResultListString(volMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);
		return getAtmTrialValues(underlyingNames,corrMap,atmMap,volMap,1,trials);
	}

	/**
	 * This method gets correlated random Atm values from a
	 *   corrMap, an atmMap, a volMap and the number of trials
	 *   The method getCorrelatedValues will create correlated random prices
	 *     that can be used as Atm value by an options model.
	 * @param snSet
	 * @param corrMap
	 * @param atmMap
	 * @param volMap
	 * @param daysOfVar
	 * @param trials
	 * @return
	 */
//	private static Map<String,double[]> getAtmTrialValues(
	private Map<String,double[]> getAtmTrialValues(
			Set<String> snSet,
			Map<String, ComplexQueryResult<BigDecimal>> corrMap,
			Map<String, ComplexQueryResult<BigDecimal>> atmMap,
			Map<String, ComplexQueryResult<BigDecimal>> volMap,
			int daysOfVar,
			int trials){
		
	
		// make a Matrix out of the corrMap
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(snSet, corrMap));
		// make vectors from the atmMap and volMap
		Matrix atmVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(snSet, atmMap),snSet.size());
		
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(snSet, volMap),snSet.size());
		volVector = volVector.times(Math.sqrt(daysOfVar/252.0));

		// create correlated random Atm values for all shortNames, and all trials
		//  the array of corrPrices will have a number or rows  = trials, and
		//   a number of colums = to snSet.size().
		double[][] corrPrices =MathStuff.getCorrelatedValues(trials, volVector, atmVector, corrMatrix).transpose().getArray();
		Map<String,double[]> ret = new HashMap<String, double[]>();
		List<String> orderedList = 
				new ArrayList<String>(new TreeSet<String>(snSet));
		
		// for each shortName, put the row vector of correlated randoms Atm's in
		//   the map that you return to the caller.
		for(int i = 0;i<orderedList.size();i++){
			ret.put(orderedList.get(i),corrPrices[i]);
		}
		return ret;
	
	}

//	static Set<String> getUnderlyingsPerDerivName(
//			Set<String> derivSnSet, 
//			DerivativeSetEngine dse,
//			int timeoutValue,
//			TimeUnit timeUnitType){
//		Set<String> underlyingNames = new HashSet<String>();
//		for(String derivSn:derivSnSet){ // populate underlyingNames
//			List<SecDef> sdList = dse.getQueryManager().getUnderlyingSecDefs(derivSn, timeoutValue, timeUnitType);
//			for(SecDef sd:sdList){
//				underlyingNames.add(sd.getShortName());
//			}
//		}
//		return underlyingNames;
//
//	}

}
