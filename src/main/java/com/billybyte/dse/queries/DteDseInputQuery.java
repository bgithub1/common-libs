package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;


public class DteDseInputQuery extends DseInputQuery<BigDecimal>{
	private final QueryInterface<String, SecDef> sdQuery ;
	public DteDseInputQuery(QueryInterface<String, SecDef> sdQuery) {
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
			BigDecimal dte =  new BigDecimal(MarketDataComLib.getDteFromSd(Calendar.getInstance(), sd));
			ret.put(sn,new ComplexQueryResult<BigDecimal>(null,dte));
		}
		return ret;
	}
}
