package com.billybyte.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Main Yahoo Pdi query that 
 * implements QueryInterface<Set<String>,
 * 	Map<String, ComplexQueryResult<PriceDisplayInterface>>> {
 * @author bperlman1
 *
 */
public class YahooCombinedStkOptPdiSetCqrRetQuery implements QueryInterface<Set<String>,Map<String, ComplexQueryResult<PriceDisplayInterface>>> {
//	private final YahooFinanceCsvPdiQuery yqlStkQuery = new YahooFinanceCsvPdiQuery();
	private final YahooFinanceCsvPdiWithFutQuery yqlStkQuery = 
			new YahooFinanceCsvPdiWithFutQuery(
					(Map<String,String>)Utils.getXmlData(Map.class, this.getClass(), "yahooExchangeMap.xml"),
					new HashMap<String,String>());
	private final YahooOptionCqrPdiQuery yahooOptQuery = new YahooOptionCqrPdiQuery(new SecDefQueryAllMarkets());

	@Override
	public Map<String, ComplexQueryResult<PriceDisplayInterface>> get(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<PriceDisplayInterface>> ret =
				yqlStkQuery.get(keySet, 10, TimeUnit.SECONDS);
		Map<String, ComplexQueryResult<PriceDisplayInterface>> optPdiMap = 
				yahooOptQuery.get(keySet, 10, TimeUnit.SECONDS);
		for(Entry<String,ComplexQueryResult<PriceDisplayInterface>> cqrEntry : optPdiMap.entrySet()){
			String thisKey = cqrEntry.getKey();
			if(ret.containsKey(thisKey))continue;
			ret.put(thisKey, cqrEntry.getValue());
		}
//		ret.putAll(yahooOptQuery.get(keySet, 10, TimeUnit.SECONDS));
		return ret;
	}

}
