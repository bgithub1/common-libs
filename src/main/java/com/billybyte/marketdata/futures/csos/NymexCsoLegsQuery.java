package com.billybyte.marketdata.futures.csos;

import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

public class NymexCsoLegsQuery implements QueryInterface<String, ComplexQueryResult<Tuple<SecDef[], Double[]>>>{
	private final NymexCsoSecDefQuery apoLegsQuery ;

	public NymexCsoLegsQuery(QueryInterface<String, SecDef> sdQuery){
		this.apoLegsQuery = new NymexCsoSecDefQuery(sdQuery);
	}
	
	
	@Override
	public ComplexQueryResult<Tuple<SecDef[], Double[]>> get(String key, int timeoutValue,
			TimeUnit timeUnitType) {
		
		SecDef actualSd = apoLegsQuery.getActualSecDef(key, timeoutValue, timeUnitType);
		if(actualSd==null){
			return errRet("no secdef for: "+key);
		}
		ComplexQueryResult<SecDef[]> sds = apoLegsQuery.get(key, timeoutValue, timeUnitType);
		if(sds.getException()!=null){
			return errRet("no secDef legs for: "+key);
		}
		Double[] weights = {1.0,-1.0};
		return new ComplexQueryResult<Tuple<SecDef[], Double[]>>(null,
				new Tuple<SecDef[], Double[]>(sds.getResult(), weights));
	}
	
	private ComplexQueryResult<Tuple<SecDef[], Double[]>> errRet(String s){
		Exception e = Utils.IllState(this.getClass(), s);
		ComplexQueryResult<Tuple<SecDef[], Double[]>> ret = 
				new ComplexQueryResult<Tuple<SecDef[],Double[]>>(e, null);
		return ret;
	}
	
	
	
}
