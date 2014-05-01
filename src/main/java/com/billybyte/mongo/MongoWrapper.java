package com.billybyte.mongo;

import java.net.UnknownHostException;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class MongoWrapper {

	private final Mongo mongo;

	public static final String ID_FIELD = "_id";
	
	public MongoWrapper(String ip, Integer port) throws UnknownHostException {
		this.mongo = new Mongo(ip, port);
		this.mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);
	}

	
	public MongoWrapper(
			String ip, 
			Integer port,
			Boolean autoConnectRetry,
			Boolean socketKeepAlive,
			Integer connectTimeout,
			Integer socketTimeout,
			Integer maxAutoConnectRetryTime,
			Integer maxWaitTime) throws UnknownHostException {
		this.mongo = new Mongo(ip, port);
		this.mongo.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		// TODO debug, to check to see if EC2 connection gets stale
		this.mongo.getMongoOptions().setAutoConnectRetry(autoConnectRetry);
		this.mongo.getMongoOptions().setSocketKeepAlive(socketKeepAlive);
		this.mongo.getMongoOptions().setConnectTimeout(connectTimeout);
		this.mongo.getMongoOptions().setSocketTimeout(socketTimeout);
		this.mongo.getMongoOptions().setMaxAutoConnectRetryTime(maxAutoConnectRetryTime);
		this.mongo.getMongoOptions().setMaxWaitTime(maxWaitTime);
	}

	
	public DB getDB(String dbName) {
		return mongo.getDB(dbName);
	}

	public DB getAuthDB(String dbName, String user, String password) throws SecurityException {
		DB db = getDB(dbName);
		if(!db.authenticate(user, password.toCharArray())) {
			throw new SecurityException("Mongo authentication failure - Invalid credentials");
		}
		return db;
	}
	
	public void close() {
		this.mongo.close();
	}

	public static DBCursor findFromKeySet(DBCollection dbColl,Set<String> keySet,String keyField){
		DBObject innerObj = new BasicDBObject();
		innerObj.put("$in", keySet.toArray(new String[]{}));
		DBObject filtObj = new BasicDBObject();
		filtObj.put(keyField, innerObj);
		
		DBCursor cursor = dbColl.find(filtObj);
		return cursor;
	}

}
