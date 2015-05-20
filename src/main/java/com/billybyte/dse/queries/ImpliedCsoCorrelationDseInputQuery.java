package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.queries.ComplexQueryResult;


public class ImpliedCsoCorrelationDseInputQuery extends DseInputQuery<BigDecimal>{
	private final ImpliedCsoCorrelationSetQuery impliedCsoCorrQuery;
	public ImpliedCsoCorrelationDseInputQuery(
			ImpliedCsoCorrelationSetQuery impliedCsoCorrQuery) {
		this.impliedCsoCorrQuery = impliedCsoCorrQuery;
	}


	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<BigDecimal>> ret = 
				impliedCsoCorrQuery.get(keySet, timeoutValue, timeUnitType);
		return ret;
	}
}
