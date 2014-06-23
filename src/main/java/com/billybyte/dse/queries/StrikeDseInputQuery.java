package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

public class StrikeDseInputQuery extends DseInputQuery<BigDecimal>{
	private final QueryInterface<String, SecDef> sdQuery ;
	public StrikeDseInputQuery(QueryInterface<String, SecDef> sdQuery) {
		this.sdQuery = sdQuery;
	}

	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<BigDecimal>> ret = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(String sn:keySet){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			if(sd==null){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(sn+" has no SecDef");
				ret.put(sn, errCqr);
				continue;
			}
			BigDecimal strike =  sd.getStrike()==null?BigDecimal.ZERO : sd.getStrike();
			ret.put(sn,new ComplexQueryResult<BigDecimal>(null,strike));
		}
		return ret;
	}
	
}