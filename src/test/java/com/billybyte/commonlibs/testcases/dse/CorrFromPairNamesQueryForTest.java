package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.MapFromMap;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.queries.ComplexQueryResult;

public class CorrFromPairNamesQueryForTest extends DseInputQuery<BigDecimal> {
	private final Map<String, BigDecimal> corrPairMap = 
			new MapFromMap<String, BigDecimal>(
					"testCorrelationPairs.csv", CorrFromPairNamesQueryForTest.class, 
					"pairName", String.class, 
					"corr", BigDecimal.class);
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<BigDecimal>>  ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		
		for(String key:keySet ){
			BigDecimal val = corrPairMap.get(key);
			if(val==null){
				ComplexQueryResult<BigDecimal> err =
						MarketDataComLib.errorRet("cannot find corr pair: " + key);
				ret.put(key, err);
				continue;
			}
			ComplexQueryResult<BigDecimal> cqr = 
					new ComplexQueryResult<BigDecimal>(null, val);
			ret.put(key, cqr);
		}
		
		return ret;
	}

}
