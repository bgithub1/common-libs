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

public class CorrFromSnSetQueryForTest extends DseInputQuery<BigDecimal> {
	private final Map<String, BigDecimal> corrPairMap = 
			new MapFromMap<String, BigDecimal>(
					"testCorrelationPairs.csv", CorrFromSnSetQueryForTest.class, 
					"pairName", String.class, 
					"corr", BigDecimal.class);
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<BigDecimal>>  ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		
		Set<String> pairSet = MarketDataComLib.getPairNameSet(keySet, null);
		for(String key:pairSet ){
			BigDecimal val = corrPairMap.get(key);
			if(val==null){
				// is it 1???
				String[] split = key.split(MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR);
				if(split[0].compareTo(split[1])==0){
					ComplexQueryResult<BigDecimal> cqr = 
							new ComplexQueryResult<BigDecimal>(null, BigDecimal.ONE);
					ret.put(key, cqr);
					continue;
				}
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
