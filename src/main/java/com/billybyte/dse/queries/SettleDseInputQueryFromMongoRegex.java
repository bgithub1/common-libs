package com.billybyte.dse.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.marketdata.SettlementDataImmute;
import com.billybyte.mongo.MongoDatabaseNames;
import com.billybyte.mongo.MongoWrapper;
import com.billybyte.mongo.MongoXml;
import com.billybyte.queries.ComplexQueryResult;
/**
 * Use regex strings to get regex based returns
 * @author bperlman1
 *
 */
public class SettleDseInputQueryFromMongoRegex extends DseInputQuery<SettlementDataInterface> {
	private final MongoXml<SettlementDataImmute> mongoSet;
	private final Map<String, SettlementDataInterface> setCache = 
			new HashMap<String, SettlementDataInterface>();
	
	/**
	 * 
	 * @param ipOfMongo
	 * @param portOfMongo
	 * @param regexToPreFetch - make this null if you don't want to do a prefetch
	 */
	public SettleDseInputQueryFromMongoRegex(String ipOfMongo,Integer portOfMongo,
			String regexToPreFetch) {
		super();
		this.mongoSet = new MongoXml<SettlementDataImmute>(ipOfMongo, portOfMongo,
				MongoDatabaseNames.SETTLEMENT_DB, MongoDatabaseNames.SETTLEMENT_CL);
	}

	public SettleDseInputQueryFromMongoRegex(MongoWrapper mongoWrapper) {
		
		this.mongoSet = new MongoXml<SettlementDataImmute>(
				mongoWrapper, MongoDatabaseNames.SETTLEMENT_DB, MongoDatabaseNames.SETTLEMENT_CL); 
		
	}
	
	@Override
	public Map<String, ComplexQueryResult<SettlementDataInterface>> get(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<SettlementDataInterface>> ret = 
				new HashMap<String, ComplexQueryResult<SettlementDataInterface>>();
		for(String key:keySet){
			Map<String,SettlementDataImmute> settleMap = 
					mongoSet.getByRegex(key);
			for(Entry<String,SettlementDataImmute>entry:settleMap.entrySet()){
				ret.put(entry.getKey(),new ComplexQueryResult<SettlementDataInterface>(null,entry.getValue()));
			}
		}
		return ret;
	}

}
