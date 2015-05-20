package com.billybyte.dse.queries;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.queries.ComplexQueryResult;


public class SettlementDseInputQuery extends DseInputQuery<SettlementDataInterface>{
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<SettlementDataInterface>>> mongoSettleQuery;

	public SettlementDseInputQuery(
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<SettlementDataInterface>>> mongoSettleQuery) {
		this.mongoSettleQuery = mongoSettleQuery;
	}


	@Override
	public Map<String, ComplexQueryResult<SettlementDataInterface>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<SettlementDataInterface>> ret = 
				mongoSettleQuery.get(keySet, timeoutValue, timeUnitType);
		return ret;
	}
}
