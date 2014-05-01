package com.billybyte.dse.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;

import com.billybyte.queries.ComplexQueryResult;

public class CallPutInputQuery extends DseInputQuery<Double> {
	private  final QueryInterface<String, SecDef> sdQuery ;
	
	public CallPutInputQuery(QueryInterface<String, SecDef> sdQuery) {
		super();
		this.sdQuery = sdQuery;
	}

	@Override
	public Map<String, ComplexQueryResult<Double>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<Double>> ret = 
				new HashMap<String, ComplexQueryResult<Double>>();
		for(String key : keySet){
			SecDef sd = sdQuery.get(key, 1, TimeUnit.SECONDS);
			if(sd==null){
				ComplexQueryResult<Double> err = 
						MarketDataComLib.errorRet(key+", can't find secdef");
				ret.put(key, err);
				continue;
			}
			Boolean isCp = MarketDataComLib.isCall(sd);
			Double oneisput = (isCp!=null && !isCp ) ? 1.0 : 0.0; // default to call
			ComplexQueryResult<Double> val = 
					new ComplexQueryResult<Double>(null, oneisput);
			ret.put(key, val);
		}
		return ret;
	}

}
