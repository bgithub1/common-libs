package com.billybyte.dse.var.stressinputqueries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.var.McVar.StressInputQueryBlock;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class RepeatInputQuery<T extends Number> implements QueryInterface<StressInputQueryBlock,Map<String,double[]>>{
	private final DerivativeSetEngine dse;
	
	public RepeatInputQuery(
			DioType<T> dioType,
			DerivativeSetEngine dse) {
		super();
		this.dse = dse;
		this.dioType = dioType;
	}
	
	private final DioType<T> dioType;
	@Override
	public Map<String, double[]> get(StressInputQueryBlock inputBlock, int timeoutValue,
			TimeUnit timeUnitType) {
		Set<String> snSet = inputBlock.getDerivSns();
		int trials = inputBlock.getNumTrials();
		Map<String, ComplexQueryResult<T>> r = 
				dse.getQueryManager().getDioInputs(dioType, snSet, timeoutValue,timeUnitType);
		List<String> orderedList = 
				new ArrayList<String>(new TreeSet<String>(snSet));
		int len = orderedList.size();
		double[] vecDouble = new double[len];
		List<Exception> excepts = new ArrayList<Exception>();
		for(int i = 0;i<len;i++){
			String sn = orderedList.get(i);
			ComplexQueryResult<T> cqr = r.get(sn);
			if(!cqr.isValidResult()){
				excepts.add(cqr.getException());
				continue;
			}
			vecDouble[i] = cqr.getResult().doubleValue();
		}
		if(excepts.size()>0){
			throw Utils.illStateWithExceptList(this.getClass(),  excepts);
		}
		
//		double[][] retDouble = MathStuff.generateDuplicateRowMatrix(trials,new Matrix(vecDouble,len)).transpose().getArray();
		double[][] retDouble = MathStuff.generateDuplicateRowMatrix(trials,vecDouble).transpose().getArray();
		Map<String,double[]> ret = new HashMap<String, double[]>();
		
		for(int i = 0;i<len;i++){
			ret.put(orderedList.get(i),retDouble[i]);
		}
		return ret;
	}
}
