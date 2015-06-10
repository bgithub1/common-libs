package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.queries.ComplexQueryResult;

public class TreasuryRateQueryFromTreasuryRateSingle extends DseInputQuery<BigDecimal> {
	private final TreasuryRateSingleQuery single ;

	public TreasuryRateQueryFromTreasuryRateSingle(
			QueryInterface<String, SecDef> sdQuery,
			TreeMap<Integer, BigDecimal> rateTable){
		single = new TreasuryRateSingleQuery(sdQuery, rateTable);
		
	}
	
	public TreasuryRateQueryFromTreasuryRateSingle(
			QueryInterface<String, SecDef> sdQuery,
			String rateTablePathOrFileNameIfResource,
			Class<?> classOfPackage){
		single = new TreasuryRateSingleQuery(sdQuery, rateTablePathOrFileNameIfResource,
				classOfPackage);
		
	}
	
	public TreasuryRateQueryFromTreasuryRateSingle(){
		single = new TreasuryRateSingleQuery();
	}

	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(String key : keySet){
			ret.put(key, single.get(key, timeoutValue, timeUnitType));
		}
		return ret;
	}
	
}
