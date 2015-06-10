package com.billybyte.dse.queries;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.queries.ComplexQueryResult;

public class PdiDseQueryFromCqrPdiQuery extends DseInputQuery<PriceDisplayInterface> {
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> innerPdiCqrQuery;

	public PdiDseQueryFromCqrPdiQuery(
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> innerPdiCqrQuery) {
		super();
		this.innerPdiCqrQuery = innerPdiCqrQuery;
	}

	@Override
	public Map<String, ComplexQueryResult<PriceDisplayInterface>> get(
			Set<String> key, int timeoutValue, TimeUnit timeUnitType) {
		return innerPdiCqrQuery.get(key, timeoutValue, timeUnitType);
	}
	
}
