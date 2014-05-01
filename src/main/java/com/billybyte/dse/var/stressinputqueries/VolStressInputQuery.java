package com.billybyte.dse.var.stressinputqueries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import Jama.Matrix;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.dse.var.McVar.StressInputQueryBlock;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class VolStressInputQuery  implements QueryInterface<StressInputQueryBlock,Map<String,double[]>>{
	private final DseInputQuery<BigDecimal> corrQuery;
	private final DseInputQuery<BigDecimal> volQuery;
	private final DerivativeSetEngine dse;
	

	public VolStressInputQuery(
			DerivativeSetEngine dse) {
		super();
		this.corrQuery = dse.getQueryManager().getQuery(new CorrDiot());
		this.volQuery = dse.getQueryManager().getQuery(new VolDiot());
		this.dse = dse;
	}

	@Override
	public Map<String, double[]> get(
			StressInputQueryBlock inputBlock, int timeoutValue,
			TimeUnit timeUnitType) {
		Set<String> snSet = inputBlock.getDerivSns();
		int trials = inputBlock.getNumTrials();
		Set<String> underlyingNames=  
				MarketDataComLib.getUnderlyingNamesSet(snSet, dse, timeoutValue, timeUnitType);
		Map<String, ComplexQueryResult<BigDecimal>> corrMap =
				corrQuery.get(underlyingNames, 20, TimeUnit.SECONDS);
		String mess = MarketDataComLib.CqrInvalidResultListString(corrMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);
		Map<String, ComplexQueryResult<BigDecimal>> volMap = 
				volQuery.get(snSet, 1, TimeUnit.SECONDS);
		return getVolTrialValues(snSet,corrMap,volMap,dse,trials,timeoutValue,timeUnitType);
	}

	
	/**
	 * This method will use the volatilies of the actual derivatives
	 *   in conjunction with the correlations of their underlyings to
	 *   create correlated vol changes
	 * @param snSet
	 * @param corrMap
	 * @param volMap
	 * @param daysOfVar
	 * @param trials
	 * @return
	 */
	private static Map<String,double[]> getVolTrialValues(
			Set<String> snSet,
			Map<String, ComplexQueryResult<BigDecimal>> corrMap,
			Map<String, ComplexQueryResult<BigDecimal>> volMap,
			DerivativeSetEngine dse,
			int trials,
			int timeunitValue,
			TimeUnit timeUnit){
		
		List<String> orderedList = new ArrayList<String>(new TreeSet<String>(snSet));
		Map<String, List<String>> derivSnToUnderlyingSnList = 
				MarketDataComLib.getUnderlyingsPerDerivName(snSet,dse,timeunitValue,timeUnit);
		
		// get underlying names in the same order as the derivative name
		List<String> underList = new ArrayList<String>();
		for(String derivName:orderedList){
			underList.add(derivSnToUnderlyingSnList.get(derivName).get(0));
		}
		
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(underList, corrMap));
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(snSet, volMap),snSet.size());
		
		Matrix psuedoVolVector = new Matrix(snSet.size(),1,0.10);

		double[][] corrVols =MathStuff.getCorrelatedValues(trials, psuedoVolVector, volVector, corrMatrix).transpose().getArray();
		Map<String,double[]> ret = new HashMap<String, double[]>();
		
		for(int i = 0;i<orderedList.size();i++){
			ret.put(orderedList.get(i),corrVols[i]);
		}
		return ret;
		
	}

}
