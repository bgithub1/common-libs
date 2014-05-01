package com.billybyte.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.thoughtworks.xstream.XStream;

public class MongoXml<T> {
	final DBCollection dbColl;
	public static final String KEY_FIELD = "_id";
	public static final String VALUE_FIELD = "value";

	private final XStream xs = new XStream();
	private final int BATCH_WRITE_MAX_SIZE = 1024*1024;
//	private final String ipOfMongo;
//	private final Integer portOfMongo;
	private final String databaseName;
	private final String collectionName;
	
	
	public MongoXml(String ipOfMongo, Integer portOfMongo,
			String databaseName, String collectionName) throws IllegalStateException{
//	public MongoXml(String ipOfMongo, Integer portOfMongo,
//			String databaseName, String collectionName) throws UnknownHostException, MongoException{
//		this.ipOfMongo = ipOfMongo;
//		this.portOfMongo = portOfMongo;
		this.databaseName = databaseName;
		this.collectionName = collectionName;

		MongoWrapper m = null;
		if(portOfMongo!=null){
			try {
				m = new MongoWrapper(ipOfMongo, portOfMongo);
			} catch (UnknownHostException e) {
				throw Utils.IllState(e);
			} catch (MongoException e) {
				throw Utils.IllState(e);
			}
		}else{
			try {
				m = new MongoWrapper(ipOfMongo, 27017);
			} catch (UnknownHostException e) {
				throw Utils.IllState(e);
			} catch (MongoException e) {
				throw Utils.IllState(e);
			}
		}
		DB db = m.getDB(databaseName);
		this.dbColl = db.getCollection(collectionName);
		BasicDBObject dbo = new  BasicDBObject();
		dbo.put(KEY_FIELD, 1);
		this.dbColl.ensureIndex(dbo);
	}

	public MongoXml(String ipOfMongo, Integer portOfMongo,
			String databaseName, String collectionName, String username, String password) throws IllegalStateException{
//	public MongoXml(String ipOfMongo, Integer portOfMongo,
//			String databaseName, String collectionName) throws UnknownHostException, MongoException{
//		this.ipOfMongo = ipOfMongo;
//		this.portOfMongo = portOfMongo;
		this.databaseName = databaseName;
		this.collectionName = collectionName;

		MongoWrapper  m;
		if(portOfMongo!=null){
			try {
				m = new MongoWrapper(ipOfMongo, portOfMongo);
			} catch (UnknownHostException e) {
				throw Utils.IllState(e);
			} catch (MongoException e) {
				throw Utils.IllState(e);
			}
		}else{
			try {
				m = new MongoWrapper(ipOfMongo, 27017);
			} catch (UnknownHostException e) {
				throw Utils.IllState(e);
			} catch (MongoException e) {
				throw Utils.IllState(e);
			}
		}
		DB db = m.getAuthDB(databaseName, username, password.toLowerCase());
//		if(!db.authenticate(username, password.toCharArray())) {
//			Utils.prtObErrMess(this.getClass(), "Unable to authenticate DB connection");
//		}
		this.dbColl = db.getCollection(collectionName);
//		BasicDBObject dbo = new  BasicDBObject(); // mongo automatically indexes _id field
//		dbo.put("_id", 1);
//		this.dbColl.ensureIndex(dbo);
	}
	
	/**
	 * Create MongoXml from MongoWrapper
	 * @param wrapper
	 * @param dbName
	 * @param collName
	 */
	public MongoXml(MongoWrapper wrapper, String dbName, String collName) {
		
		this.databaseName = dbName;
		this.collectionName = collName;
		
		DB db = wrapper.getDB(dbName);
		this.dbColl = db.getCollection(collName);
		
	}

	@Deprecated
	public void writeMap(Map<String, T> objectsToWrite){
		
		for(Entry<String, T>entry:objectsToWrite.entrySet()){
			BasicDBObject dbo = new BasicDBObject();
			dbo.put(KEY_FIELD,entry.getKey());
			dbo.put(VALUE_FIELD, xs.toXML(entry.getValue()));
//			dbo.put(KEY_FIELD,entry.getKey());
//			dbo.put(VALUE_FIELD, xs.toXML(entry.getValue()));
			this.dbColl.insert(dbo);
		}
	}
	
	public  Map<String,T> getByRegex(String key){
		if(key==null){
			return new HashMap<String, T>();
		}
		BasicDBObject dboInner = new BasicDBObject();
		dboInner.put("$regex", key);
		BasicDBObject dboOuter = new BasicDBObject();
		dboOuter.put(KEY_FIELD, dboInner);
		return readDbo(dboOuter);
	}

	public  T getFirstRegexMatch(String regex){
		if(regex==null){
			return null;
		}
		BasicDBObject dboInner = new BasicDBObject();
		dboInner.put("$regex", regex);
		BasicDBObject dboOuter = new BasicDBObject();
		dboOuter.put(KEY_FIELD, dboInner);
		DBObject ret = dbColl.findOne(dboOuter);
		if(ret==null) {
			return null;
		} else {
			Object o = ret.toMap().get(VALUE_FIELD);
			T t = (T)xs.fromXML(o.toString());
			return t;
		}
		
	}

	public Map<String,T> getAll(){
		DBCursor cursor = dbColl.find();
		return readCursor(cursor);
	}
	
	public void deleteAll(){
		dbColl.remove(new BasicDBObject());
	}
	
	public Map<String,T> read(String key){
		BasicDBObject dbo = new BasicDBObject();
		dbo.put(KEY_FIELD, key);
		return readDbo(dbo);
	}
	
	protected Map<String,T> readDbo(DBObject dbo){
		DBCursor cursor = dbColl.find(dbo);
		return readCursor(cursor);
	}
	
//	private Map<String,T> readCursor(DBCursor cursor){
//		List<DBObject> dbObjList = cursor.toArray();
//		Map<String,T> ret = new HashMap<String,T>();
//		for(DBObject dboRet:dbObjList){
//			Map<String,Object> keyValuePairMapFromMongo = dboRet.toMap();
//			Object o = keyValuePairMapFromMongo.get("value");
//			T t = (T)xs.fromXML(o.toString());
//			String retKey = (String)keyValuePairMapFromMongo.get("_id");
//			ret.put(retKey, t);
//		}
//		return ret;	
//	}

	
	private Map<String,T> readCursor(DBCursor cursor){
		Map<String,T> ret = new HashMap<String,T>();
		try{
			while(cursor.hasNext()){
				DBObject dboRet = cursor.next();
				Map<String,Object> keyValuePairMapFromMongo = dboRet.toMap();
				Object o = keyValuePairMapFromMongo.get(VALUE_FIELD);
				T t = (T)xs.fromXML(o.toString());
				String retKey = (String)keyValuePairMapFromMongo.get(KEY_FIELD);
				ret.put(retKey, t);
			}
		}finally{
			cursor.close();
		}
		return ret;	
	}

	/**
	 * Inserts a single document
	 * @param key
	 * @param value
	 */
	public void insertSingleEntry(String key, T value){
		BasicDBObject dbo = new BasicDBObject();
		dbo.put(KEY_FIELD, key);
		dbo.put(VALUE_FIELD, xs.toXML(value));
		this.dbColl.insert(dbo);
	}
	
	
	/**
	 * Perform a batch insert on the collection
	 * @param insertMap
	 */
	public void batchInsert(Map<String,T> insertMap) throws MongoException {
		// new routine that calculates batchCount on the fly
		List<String> keyList = new ArrayList<String>(insertMap.keySet());
		if(keyList.size()<1)return;
		T t = insertMap.get(keyList.get(0));
		String serialized = xs.toXML(t);
		int recLen = serialized.length();
		long totalMapSerializedSize = recLen * insertMap.size();
		int batchCount = new Long(totalMapSerializedSize  / BATCH_WRITE_MAX_SIZE + 1).intValue();
		int itemsPerLoop = insertMap.size() / batchCount;
		batchInsert(insertMap,itemsPerLoop);
		
//		Map<String,T> newMap = new HashMap<String, T>();
//		for(int l = 0;l<batchCount;l++){
//			for(int j = l*itemsPerLoop;j<(l+1)*itemsPerLoop;j++){
//				String key = keyList.get(j);
//				newMap.put(key, insertMap.get(key));
//			}
//			batchInsert(newMap,batchCount);
////			batchInsert(newMap,itemsPerLoop);
//		}
		
	}

	/**
	 * Perform a batch insert on the collection in pieces according to the batch size
	 * @param insertMap
	 */
	public void batchInsert(Map<String,T> insertMap, int batchSize) throws MongoException {
		List<DBObject> batchList = new ArrayList<DBObject>();
		for(Entry<String,T> entry:insertMap.entrySet()) {
			BasicDBObject dbo = new BasicDBObject();
			dbo.put(KEY_FIELD, entry.getKey());
			dbo.put(VALUE_FIELD, xs.toXML(entry.getValue()));
			batchList.add(dbo);
			
			if(batchList.size()==batchSize) {
//				try {
//					this.dbColl.insert(batchList);
//				} catch (MongoException e) {
//					Utils.prtObErrMess(this.getClass(), "Batch insert exception, aborting insertion, REASON: "+e.getLocalizedMessage());
//					return;
//				}
				this.dbColl.insert(batchList);
				batchList.clear();
			}
		}
		if(!batchList.isEmpty()) {
			this.dbColl.insert(batchList);
		}
	}

	
	
	/**
	 * Inserts multiple documents
	 * @param newEntriesMap
	 */
	@Deprecated
	public void insertMultipleEntries(Map<String,T> newEntriesMap){
		writeMap(newEntriesMap);
	}
	
	/**
	 * Updates any document in the collection with the provided key with the provided value
	 * @param key
	 * @param value
	 */
	public void updateSingleEntry(String key, T value){
		DBObject filt = new BasicDBObject();
		filt.put(KEY_FIELD, key);
		DBObject updObj = new BasicDBObject();
		updObj.put(KEY_FIELD, key);
		updObj.put(VALUE_FIELD,xs.toXML(value));
		dbColl.update(filt, updObj);
	}

	/**
	 * Update for many documents
	 * @param updateEntriesMap
	 */
	public void updateMultipleEntries(Map<String,T> updateEntriesMap){
		for(Entry<String,T> entry:updateEntriesMap.entrySet()){
			updateSingleEntry(entry.getKey(),entry.getValue());
		}
	}
	

	/**
	 * Removes all documents in the collection with the specified key
	 * @param key
	 */
	public void removeSingleEntry(String key){
		DBObject remObj = new BasicDBObject();
		remObj.put(KEY_FIELD, key);
		dbColl.remove(remObj);
	}
	
	/**
	 * Removes all documents in the collection that match any key in the specified set
	 * @param keys
	 */
	public void removeMultipleEntries(Set<String> keys){
		for(String key:keys){
			removeSingleEntry(key);
		}
	}
	
	/**
	 * Checks to see if the key is already present in the collection, if it is, updates the document, if not, inserts
	 * @param key
	 * @param value
	 */
	public void upsert(String key, T value){
		DBObject query = new BasicDBObject();
		query.put(KEY_FIELD, key);
		if(dbColl.count(query)==0){;
			insertSingleEntry(key, value);
		} else {
			updateSingleEntry(key, value);
		}
	}
	
	/**
	 * Upsert but for multiple documents
	 * @param upMap
	 */
	public void multiUpsert(Map<String,T> upMap){
		Map<String,T> existingMap = findFromSet(upMap.keySet());
		Set<String> existingSet = existingMap.keySet();
		for(Entry<String,T> entry:upMap.entrySet()){
			if(existingSet.contains(entry.getKey())){
				updateSingleEntry(entry.getKey(), entry.getValue());
			} else {
				insertSingleEntry(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/**
	 * Performs a find on the collection using multiple keys
	 * @param keySet
	 * @return
	 */
	public Map<String,T> findFromSet(Set<String> keySet){
		DBObject innerObj = new BasicDBObject();
		innerObj.put("$in", keySet.toArray(new String[]{}));
		DBObject filtObj = new BasicDBObject();
		filtObj.put(KEY_FIELD, innerObj);
		dbColl.find(filtObj);
		return readDbo(filtObj);
	}
	
	
	public Set<String> findKeysFromRegex(String regexString){
		DBObject innerObj = new BasicDBObject();
		innerObj.put("$regex", regexString);
		DBObject filtObj = new BasicDBObject();
		filtObj.put(KEY_FIELD, innerObj);
		DBObject projection = new BasicDBObject();
		projection.put(KEY_FIELD, 1);
		DBCursor cursor =  dbColl.find(filtObj,projection);
		
		Set<String> ret = new HashSet<String>();
		try{
			while(cursor.hasNext()){
				DBObject dboRet = cursor.next();
				Map<String,Object> keyValuePairMapFromMongo = dboRet.toMap();
				String retKey = (String)keyValuePairMapFromMongo.get(KEY_FIELD);
				ret.add(retKey);
			}
		}finally{
			cursor.close();
		}
		return ret;	
	}
	
	
	public Set<String> findKeysFromSet(Set<String> keySet){
		DBObject innerObj = new BasicDBObject();
		String[] arr = keySet.toArray(new String[]{});
		innerObj.put("$in", arr);
		DBObject filtObj = new BasicDBObject();
		filtObj.put(KEY_FIELD, innerObj);
		DBObject projection = new BasicDBObject();
		projection.put(KEY_FIELD, 1);
		DBCursor cursor =  dbColl.find(filtObj,projection);
		
		Set<String> ret = new HashSet<String>();
		try{
			while(cursor.hasNext()){
				DBObject dboRet = cursor.next();
				Map<String,Object> keyValuePairMapFromMongo = dboRet.toMap();
				String retKey = (String)keyValuePairMapFromMongo.get(KEY_FIELD);
				ret.add(retKey);
			}
		}finally{
			cursor.close();
		}
		return ret;	
	}
	

	/**
	 * Performs a find on the collection using multiple keys
	 * @param list
	 * @return
	 */
	public Map<String,T> findFromList(List<String> keySet){
		DBObject innerObj = new BasicDBObject();
		innerObj.put("$in", keySet.toArray(new String[]{}));
		DBObject filtObj = new BasicDBObject();
		filtObj.put(KEY_FIELD, innerObj);
		dbColl.find(filtObj);
		return readDbo(filtObj);
	}

	
	/**
	 * Finds the first document that has the specified key (quicker than a normal Find that returns a cursor)
	 * @param key
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T findOne(String key){
		DBObject query = new BasicDBObject();
		query.put(KEY_FIELD, key);
		DBObject ret = dbColl.findOne(query);
		if(ret==null)return null;
		Map<String,Object> keyValuePairMapFromMongo = ret.toMap();
		if(keyValuePairMapFromMongo.isEmpty())return null;
		Object o = keyValuePairMapFromMongo.get(VALUE_FIELD);
		return (T)xs.fromXML(o.toString());
	}
	
	public T findOneByRegex(String regexString){
		DBObject regexPart = new BasicDBObject("$regex",regexString);
		DBObject query = new BasicDBObject(KEY_FIELD,regexPart);
		DBObject ret = dbColl.findOne(query);
		if(ret==null)return null;
		Map<String,Object> keyValuePairMapFromMongo = ret.toMap();
		if(keyValuePairMapFromMongo.isEmpty())return null;
		Object o = keyValuePairMapFromMongo.get(VALUE_FIELD);
		return (T)xs.fromXML(o.toString());
	}
	
	
	public void removeIndices() {
		this.dbColl.dropIndexes();
	}
	
	public DBCollection getCollection(){
		return dbColl;
	}
	
	public  int getBatchWriteMaxSize(){
		return BATCH_WRITE_MAX_SIZE;
	}
	
	public void batchCopy(MongoXml<T> dest){
		DBCursor cursor = this.dbColl.find(new BasicDBObject(),new BasicDBObject(KEY_FIELD, 0));
		if(cursor.hasNext()){
			DBCollection destColl = dest.getCollection();
			// get an example
			DBObject dboRet = cursor.next();
			cursor.close();
			Map<String,Object> keyValuePairMapFromMongo = dboRet.toMap();
			Object o = keyValuePairMapFromMongo.get(VALUE_FIELD);
			int recLen = o.toString().length();
			int recsPerBatch = BATCH_WRITE_MAX_SIZE / recLen;
			int count = 0;
			// get cursor again
//			DBCursor newCursor = this.dbColl.find(new BasicDBObject(),new BasicDBObject("_id", 0));
			DBCursor newCursor = this.dbColl.find();
			int totalRecs = newCursor.count();
			Utils.prt("about to copy: "+totalRecs);
			int totalCount = 0;
			List<DBObject> dboList = new ArrayList<DBObject>();
			List<String> keyList = new ArrayList<String>();
			while(newCursor.hasNext()){
				dboRet = newCursor.next();
				String key = (String)dboRet.get(KEY_FIELD);
				keyList.add(key);
				Map<String, T> newMap = dboRet.toMap();
				BasicDBObject dbo = new BasicDBObject();
				dbo.put(KEY_FIELD, key);
				dbo.put(VALUE_FIELD, newMap.get(VALUE_FIELD));
				
				dboList.add(dbo);
				
				count+=1;
				totalCount+=1;
				if(count>=recsPerBatch){
					destColl.remove(new BasicDBObject(KEY_FIELD,
							new BasicDBObject("$in",keyList.toArray(new String[]{}))));
					destColl.insert(dboList);
					Utils.prt("replaced " +count+ " for total of "+totalCount);
					dboList.clear();
					keyList.clear();
					count = 0;
				}
			}
			if(count>0){
				destColl.remove(new BasicDBObject(KEY_FIELD,keyList.toArray(new String[]{})));
				destColl.insert(dboList);
				Utils.prt("replaced " +count+ " for total of "+totalCount);
			}
			newCursor.close();
			
		}
	}

	void copyOldKeyDataToNewKeyData(
			String mongoOldIp,
			Integer mongoOldPort){
		String oldKey = "key";
		MongoWrapper mOld;
		try {
			mOld = new MongoWrapper(mongoOldIp, mongoOldPort);
		} catch (UnknownHostException e) {
			throw Utils.IllState(e);
		} catch (MongoException e) {
			throw Utils.IllState(e);
		}
		DB dbOld = mOld.getDB(databaseName);
		DBCollection dbCollOld = dbOld.getCollection(collectionName);
//		BasicDBObject dbo = new  BasicDBObject();
//		dbo.put(oldKey, 1);
//		dbCollOld.ensureIndex(dbo);

		// get cursor using the "_id" field
		DBCursor cursor = dbCollOld.find(new BasicDBObject(),new BasicDBObject(KEY_FIELD, 0));
		if(cursor.hasNext()){
			DBCollection destColl = this.dbColl;
			// get an example
			DBObject dboRet = cursor.next();
			cursor.close();
			Map<String,Object> keyValuePairMapFromMongo = dboRet.toMap();
			Object o = keyValuePairMapFromMongo.get(VALUE_FIELD);
			int recLen = o.toString().length();
			int recsPerBatch = BATCH_WRITE_MAX_SIZE / recLen;
			int count = 0;
			// get cursor again
			DBCursor newCursor = dbCollOld.find(new BasicDBObject(),new BasicDBObject(KEY_FIELD, 0));
			int totalRecs = newCursor.count();
			Utils.prt("about to copy: "+totalRecs);
			int totalCount = 0;
			List<DBObject> dboList = new ArrayList<DBObject>();
			List<String> keyList = new ArrayList<String>();
			while(newCursor.hasNext()){
				dboRet = newCursor.next();
				String key = (String)dboRet.get(oldKey);
				keyList.add(key);
				Map<String, T> newMap = dboRet.toMap();
				BasicDBObject dbo = new BasicDBObject();
				dbo.put(KEY_FIELD, key);
				dbo.put(VALUE_FIELD, newMap.get(VALUE_FIELD));
				
				dboList.add(dbo);
				
				count+=1;
				totalCount+=1;
				if(count>=recsPerBatch){
					destColl.remove(new BasicDBObject(KEY_FIELD,
							new BasicDBObject("$in",keyList.toArray(new String[]{}))));
					destColl.insert(dboList);
					Utils.prt("replaced " +count+ " for total of "+totalCount);
					dboList.clear();
					keyList.clear();
					count = 0;
				}
			}
			if(count>0){
				destColl.remove(new BasicDBObject(KEY_FIELD,keyList.toArray(new String[]{})));
				destColl.insert(dboList);
				Utils.prt("replaced " +count+ " for total of "+totalCount);
			}
			newCursor.close();
			
		}

		
	}
	
	public void close(){
		this.dbColl.getDB().getMongo().close();
	}

	
	public Set<String> getMissingKeys(Set<String> keySet){
		Set<String> existingKeys = findKeysFromSet(keySet);
		Set<String> ret = new HashSet<String>(keySet);
		ret.removeAll(existingKeys);
		return ret;
	}

	public Set<String> getOptionsKeys(String stkKey){
		if(stkKey.contains(SecSymbolType.FOP+".") || stkKey.contains(SecSymbolType.OPT+".")){
			return CollectionsStaticMethods.setFromArray(new String[]{stkKey});
		}
		if(stkKey.contains(SecSymbolType.STK.toString()+".")){
			String optPartial = stkKey.replace(SecSymbolType.STK+".", SecSymbolType.OPT+".");
			Set<String> optKeys = findKeysFromRegex("^"+optPartial.replace(".", "\\."));
			return optKeys;
		}
		// try fop's
		if(stkKey.contains(SecSymbolType.FUT.toString()+".")){
			String fopPartial = stkKey.replace(SecSymbolType.FUT+".", SecSymbolType.FOP+".");
			Set<String> optKeys = findKeysFromRegex("^"+fopPartial.replace(".", "\\."));
			return optKeys;
		}
		return new HashSet<String>();
	}
}
	
