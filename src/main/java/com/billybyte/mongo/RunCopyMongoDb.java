package com.billybyte.mongo;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.billybyte.commonstaticmethods.Utils;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class RunCopyMongoDb {
	/**
	 * 
	 * @param args arg-pairs:
	 * 	example
	 *  sourceIp=127.0.0.1
	 *  sourcePort=27022
	 *  sourceDb=testSendDb
	 *  sourceColl=testSendColl
	 *  destIp=192.168.1.128
	 *  destPort=27022
	 *  destDb=testSendDb
	 *  destColl=testSendColl
	 */
	public static void main(String[] args) {
		Map<String, String> ap= Utils.getArgPairsSeparatedByChar(args, "=");
		String sourceIp = ap.get("sourceIp");
		Integer sourcePort =ap.get("sourcePort")==null? 27017 : new Integer(ap.get("sourcePort"));	
		String sourceDb = ap.get("sourceDb");
		String sourceColl = ap.get("sourceColl");

		String destIp = ap.get("destIp");
		Integer destPort =ap.get("destPort")==null? 27017 : new Integer(ap.get("destPort"));	
		String destDb = ap.get("destDb");
		String destColl = ap.get("destColl");
		
		MongoWrapper sourceMw=null;
		try {
			sourceMw = new MongoWrapper(sourceIp, sourcePort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MongoXml<Object> source = new MongoXml<Object>(sourceMw, sourceDb, sourceColl);

		MongoWrapper destMw=null;
		try {
			destMw = new MongoWrapper(destIp, destPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MongoXml<Object> dest = new MongoXml<Object>(destMw, destDb, destColl);
		copyMongo(source,dest);
		
	}
	
	private static <T> void copyMongo(
			MongoXml<T> source, 
			MongoXml<T> destination){
//		destination.multiUpsert(source.getByRegex(regexExpressionForSourceData));
		batchCopy(source,destination);
	}
	
	private static final int BATCH_WRITE_MAX_SIZE = 1024*1024;
	private static final String KEY_FIELD="_id";
	
	private static <T> void  batchCopy(MongoXml<T> source,MongoXml<T> dest){
		DBCollection destColl = dest.getCollection();
		DBObject dboRet = source.dbColl.findOne(new BasicDBObject());
		int recLen = dboRet.toString().length();
		int recsPerBatch = BATCH_WRITE_MAX_SIZE / recLen;
		int count = 0;
		// get cursor again
		DBCursor newCursor = source.dbColl.find(new BasicDBObject());
		int totalRecs = newCursor.count();
		Utils.prt("about to copy: "+totalRecs);
		int totalCount = 0;
		List<DBObject> dboList = new ArrayList<DBObject>();
		List<String> keyList = new ArrayList<String>();
		while(newCursor.hasNext()){
			dboRet = newCursor.next();
			String key = (String)dboRet.get(KEY_FIELD);
			keyList.add(key);
			dboList.add(dboRet);
			
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
			destColl.remove(new BasicDBObject(KEY_FIELD,
					new BasicDBObject("$in",keyList.toArray(new String[]{}))));
			destColl.insert(dboList);
			Utils.prt("replaced " +count+ " for total of "+totalCount);
		}
		newCursor.close();
	}


}
