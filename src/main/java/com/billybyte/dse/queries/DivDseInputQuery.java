package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.YahooFinanceDivYieldQuery;
import com.billybyte.queries.ComplexQueryResult;

public class DivDseInputQuery extends DseInputQuery<BigDecimal>{
	private final DseInputQuery<BigDecimal> treasuryRateQueryAsProxy;
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> divQuery;

	
	
	public DivDseInputQuery(
			DseInputQuery<BigDecimal> treasuryRateQueryAsProxy,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> divQuery) {
		super();
		this.treasuryRateQueryAsProxy = treasuryRateQueryAsProxy;
		this.divQuery = divQuery;
	}

	public DivDseInputQuery() {
		super();
		this.treasuryRateQueryAsProxy = null;
		this.divQuery = new YahooFinanceDivYieldQuery();
	}



	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> key,
			int timeoutValue, TimeUnit timeUnitType) {
		if(divQuery!=null){
			return divQuery.get(key, timeoutValue, timeUnitType);
		}
		return treasuryRateQueryAsProxy.get(key, timeoutValue, timeUnitType);
	}

	
	
}
