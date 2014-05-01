package com.billybyte.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SettlementDataImmute;
import com.billybyte.mongo.MongoDatabaseNames;
import com.billybyte.mongo.MongoXml;
/**
 * Get Map<String, ComplexQueryResult<SettlementDataInterface>> data from Mongo db 
 * @author bperlman1
 *
 */
public class SettleQueryFromMongo implements QueryInterface<Set<String>, Map<String, ComplexQueryResult<SettlementDataInterface>>> {
	private final MongoXml<SettlementDataImmute> mongoSet;
	private final Map<String, SettlementDataInterface> setCache = 
			new HashMap<String, SettlementDataInterface>();
	
	public SettleQueryFromMongo(
			MongoXml<SettlementDataImmute> mongoSet,
			String regexToPreFetch){
		
		this.mongoSet = mongoSet;
		if(regexToPreFetch!=null){
			setCache.putAll(mongoSet.getByRegex(regexToPreFetch));
		}
	}
	
	/**
	 * 
	 * @param ipOfMongo
	 * @param portOfMongo
	 * @param regexToPreFetch - use null if you don't want a pre-fetch
	 */
	public SettleQueryFromMongo(
			String ipOfMongo,
			Integer portOfMongo,
			String regexToPreFetch) {
		this(new MongoXml<SettlementDataImmute>(ipOfMongo, portOfMongo,
				MongoDatabaseNames.SETTLEMENT_DB, MongoDatabaseNames.SETTLEMENT_CL),regexToPreFetch);
//		super();
//		this.mongoSet = new MongoXml<SettlementDataImmute>(ipOfMongo, portOfMongo,
//				MongoDatabaseNames.SETTLEMENT_DB, MongoDatabaseNames.SETTLEMENT_CL);
//		if(regexToPreFetch!=null){
//			setCache.putAll(mongoSet.getByRegex(regexToPreFetch));
//		}
	}

	@Override
	public Map<String, ComplexQueryResult<SettlementDataInterface>> get(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<SettlementDataInterface>> ret = 
				new HashMap<String, ComplexQueryResult<SettlementDataInterface>>();
		for(String key:keySet){
			if(setCache.containsKey(key)){
				ret.put(key, new ComplexQueryResult<SettlementDataInterface>(null,setCache.get(key)));
			}else{
				Map<String,SettlementDataImmute> settleMap = 
						mongoSet.read(key);
				if(settleMap.containsKey(key)){
					ret.put(key, new ComplexQueryResult<SettlementDataInterface>(null,settleMap.get(key)));
				}else{
					ComplexQueryResult<SettlementDataInterface> errCqr = 
							MarketDataComLib.errorRet(key+" not found in mongo");
					ret.put(key, errCqr);
				}
			}
		}
		return ret;
	}

}
