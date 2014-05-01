package com.billybyte.mongo;


import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.mongo.MongoXml;
import com.mongodb.MongoException;

public class QueryFromMongoXml<V> implements QueryInterface<String, Map<String,V>> {
	private final MongoXml<V> mongoXml ;

	public QueryFromMongoXml(
			String ipOfMongo, 
			Integer portOfMongo, 
			String databaseName, 
			String collectionName){
		try {
			mongoXml = new MongoXml<V>(
					ipOfMongo, portOfMongo, databaseName, collectionName);
//		} catch (UnknownHostException e) {
		} catch (IllegalStateException e) {
			throw Utils.IllState(e);
		} catch (MongoException e) {
			throw Utils.IllState(e);
		}
	}
	@Override
	public Map<String,V> get(String key, int timeoutValue, TimeUnit timeUnitType) {
		Map<String,V> ret = mongoXml.read(key);
		return ret;
	}
	
	
}
