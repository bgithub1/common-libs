package com.billybyte.dse.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;



import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.mongo.MongoXml;
import com.billybyte.queries.ComplexQueryResult;

public class MongoXmlCqrSetQuery<V> extends DseInputQuery<V>{
	private final MongoXml<V> mongoQuery;
	private final Map<String, V> cache = 
		new HashMap<String, V>();

	/**
	 * 
	 * @param mongoQuery
	 */
	public MongoXmlCqrSetQuery(MongoXml<V> mongoQuery) {
		super();
		this.mongoQuery = mongoQuery;
	}
	
	/**
	 * 
	 * @param ipOfMongo - like "127.0.0.1"
	 * @param portOfMongo - like 27017
	 * @param mongoDbName - 
	 * @param mongoCollName
	 * @param regexToPreFetch
	 */
	public MongoXmlCqrSetQuery(
			String ipOfMongo,
			Integer portOfMongo,
			String mongoDbName,
			String mongoCollName,
			String regexToPreFetch){
		
		mongoQuery = new MongoXml<V>(ipOfMongo, portOfMongo, mongoDbName, mongoCollName);
		
	}
	
	
	@Override
	public Map<String, ComplexQueryResult<V>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<V>> ret = 
				new HashMap<String, ComplexQueryResult<V>>();
		for(String key:keySet){
			if(cache.containsKey(key)){
				ret.put(key, new ComplexQueryResult<V>(null,cache.get(key)));
			}else{
				Map<String,V> map = 
						mongoQuery.read(key);
				if(map.containsKey(key)){
					ret.put(key, new ComplexQueryResult<V>(null,map.get(key)));
				}else{
					ComplexQueryResult<V> errCqr = 
							MarketDataComLib.errorRet(key+" not found in mongo");
					ret.put(key, errCqr);
				}
			}
		}
		return ret;
	}

	


}
