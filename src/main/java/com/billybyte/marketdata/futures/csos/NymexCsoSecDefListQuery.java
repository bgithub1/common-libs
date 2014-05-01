package com.billybyte.marketdata.futures.csos;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

public class NymexCsoSecDefListQuery implements QueryInterface<String, List<SecDef>> {
	NymexCsoSecDefQuery nymCsoSdArrayQuery;
	public NymexCsoSecDefListQuery(QueryInterface<String, SecDef> sdQuery){
		nymCsoSdArrayQuery=  new NymexCsoSecDefQuery(sdQuery);
	}
	@Override
	public List<SecDef> get(String key, int timeoutValue, TimeUnit timeUnitType) {
		ComplexQueryResult<SecDef[]>result = nymCsoSdArrayQuery.get(key, timeoutValue, timeUnitType);
		if(result==null || !result.isValidResult())return null;
		return Arrays.asList(result.getResult());
	}
}
