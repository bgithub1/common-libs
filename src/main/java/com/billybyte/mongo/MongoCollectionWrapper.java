package com.billybyte.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.billybyte.commonstaticmethods.Utils;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MongoCollectionWrapper {
	private final MongoWrapper mongoWrapper;
	private final DBCollection collection;
	private final DB db;
	public MongoCollectionWrapper(String ip, Integer port,String dbName, String collectionName) {
		try {
			this.mongoWrapper = new MongoWrapper(ip, port);
			this.db = mongoWrapper.getDB(dbName);
			this.collection = db.getCollection(collectionName);
		} catch (UnknownHostException e) {
			throw Utils.IllState(e);
		} 
	}
	public MongoWrapper getMongoWrapper() {
		return mongoWrapper;
	}
	public DBCollection getCollection() {
		return collection;
	}
	public DB getDb() {
		return db;
	}
	
	public <T extends MongoBaseAbstract<T>> List<T> getList(
			Class<T> clazz,DBObject singleSearch){
		List<DBObject> dboList = collection.find(singleSearch).toArray();
		List<T> ret = new ArrayList<T>();
		for(DBObject dbo : dboList){
			T t = MongoBaseAbstract.fromDbo(clazz, dbo);
			ret.add(t);
		}
		return ret;
	}

	public <T extends MongoBaseAbstract<T>> List<DBObject> toDboList(List<T> tList){
		List<DBObject> ret = new ArrayList<DBObject>();
		for(T t : tList){
			ret.add(t.toDBObject());
		}
		return ret;
	}
}
