package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.queries.ComplexQueryResult;

public class BdDseQueryFromCqrBdQuery extends DseInputQuery<BigDecimal> {
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> innerPdiCqrQuery;

	public BdDseQueryFromCqrBdQuery(
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> innerPdiCqrQuery) {
		super();
		this.innerPdiCqrQuery = innerPdiCqrQuery;
	}

	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(
			Set<String> key, int timeoutValue, TimeUnit timeUnitType) {
		return innerPdiCqrQuery.get(key, timeoutValue, timeUnitType);
	}
	
}
