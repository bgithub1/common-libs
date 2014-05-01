package com.billybyte.dse.queries;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

public class SettlesOfCalSwapUnderlyingsQuery implements QueryInterface<
		Set<String>,Map<String,ComplexQueryResult<SettlementDataInterface>>>{

	private final QueryInterface<Set<String>,Map<String,ComplexQueryResult<SettlementDataInterface>>> baseSettleQuery;
	private final QueryInterface<String, List<SecDef>> underlyingSecDefsQuery;
	
	public SettlesOfCalSwapUnderlyingsQuery(
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<SettlementDataInterface>>> baseSettleQuery,
			QueryInterface<String, List<SecDef>> underlyingSecDefsQuery) {
		super();
		this.baseSettleQuery = baseSettleQuery;
		this.underlyingSecDefsQuery = underlyingSecDefsQuery;
	}

	@Override
	public Map<String, ComplexQueryResult<SettlementDataInterface>> get(Set<String> key, int timeoutValue, TimeUnit timeUnitType) {
		Set<String> shortNameList = new HashSet<String>(key);
		for(String shortName:key){
			List<SecDef> sdList = underlyingSecDefsQuery.get(shortName, timeoutValue, timeUnitType);
			for(SecDef sd:sdList){
				shortNameList.add(sd.getShortName());
			}
		}
		return baseSettleQuery.get(shortNameList, timeoutValue, timeUnitType);
	}

}
