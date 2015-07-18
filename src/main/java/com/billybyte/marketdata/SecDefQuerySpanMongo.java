package com.billybyte.marketdata;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.mongo.MongoWrapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class SecDefQuerySpanMongo  implements QueryInterface<String, SecDef>{
	private static final Map<String, SecDef> cacheMap = new HashMap<String, SecDef>();
	private static final AtomicReference<Map<String, SecDef>> cacheMapRef = 
			new AtomicReference<Map<String,SecDef>>(cacheMap);
	private static final AtomicReference<DBCollection> secDefCollection = new AtomicReference<DBCollection>();;
	
	public SecDefQuerySpanMongo(String ip, Integer port,Boolean preLoad){
		if(secDefCollection.get()!=null)return;
		String mip = ip==null ? "127.0.0.1" : ip;
		int mport = port==null ? 27022 : port;
		try {
			MongoWrapper mw = new MongoWrapper(mip, mport);
			DB mdb = mw.getDB("spanSecDefDb");
			DBCollection coll = mdb.getCollection("spanSecDefColl");
			secDefCollection.set(coll);
			if(preLoad!=null && preLoad==true){
				DBObject searchObj = new BasicDBObject();
				List<DBObject> dboList = secDefCollection.get().find(searchObj).toArray();
				if(dboList!=null && dboList.size()>0){
					for(DBObject dbo : dboList){
						SecDef value = new SecDefFromSpanMongo(dbo);
						cacheMapRef.get().put(value.getShortName(), value);	
					}
					
				}
			}
		} catch (UnknownHostException e) {
			throw Utils.IllState(e);
		}
	}

	@Override
	public SecDef get(String key, int timeoutValue, TimeUnit timeUnitType) {
		SecDef ret = null;
		ret = cacheMap.get(key);
		if(ret!=null)return ret;
		DBObject searchObj = new BasicDBObject();
		searchObj.put("_id",key);
		List<DBObject> dboList = secDefCollection.get().find(searchObj).toArray();
		if(dboList==null || dboList.size()<1)return null;
		ret = new SecDefFromSpanMongo(dboList.get(0));
		if(ret!=null){
			cacheMap.put(key, ret);
		}
		return ret;
	}
	
	public static void main(String[] args) {
		// Get secdef names from the spanSecDefData base for testing
		DBCollection coll =null;
		try {
			MongoWrapper mw = new MongoWrapper("127.0.0.1", 27022);
			DB mdb = mw.getDB("spanSecDefDb");
			coll = mdb.getCollection("spanSecDefColl");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw Utils.IllState(e);
		}
		
		DBObject searchDbo = new BasicDBObject();
		DBObject regexInner = new BasicDBObject();
		regexInner.put("$regex", "^LO[12345]");
		searchDbo.put("_id",regexInner);
		List<DBObject> dboList = coll.find(searchDbo).toArray();
		List<String> snList = new ArrayList<String>();
		for(DBObject dbo : dboList){
			Map<String,Object> keyValuePairMapFromMongo = dbo.toMap();
			String sn = (String)keyValuePairMapFromMongo.get("_id");
			snList.add(sn);
		}
		
		// get SecDefs from SecDefQuerySpanMongo directly
		Utils.prt("getting SecDefs directly from "+SecDefQuerySpanMongo.class.getSimpleName());
		QueryInterface<String, SecDef> sdQuery = 
				new SecDefQuerySpanMongo(null, null,true);
		Map<String,SecDef> results = new HashMap<String,SecDef>();
		for(String sn : snList){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			if(sd==null)continue;
			results.put(sn,sd);
		}
		if(results==null || results.size()<1){
			Utils.prtObErrMess(SecDefQuerySpanMongo.class, "no secdefs found");
			return ;
		}
		CollectionsStaticMethods.prtMapItems(results);
		for(String sn : snList){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			results.put(sn,sd);
		}
		CollectionsStaticMethods.prtMapItems(results);
		
		results.clear();
		Utils.prt("getting SecDefs indirectly via "+SecDefQueryAllMarkets.class.getSimpleName());

		sdQuery = new SecDefQueryAllMarkets();
		for(String sn : snList){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			if(sd==null)continue;
			results.put(sn,sd);
		}
		if(results==null || results.size()<1){
			Utils.prtObErrMess(SecDefQuerySpanMongo.class, "no secdefs found");
			return ;
		}
		CollectionsStaticMethods.prtMapItems(results);
		for(String sn : snList){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			results.put(sn,sd);
		}
		CollectionsStaticMethods.prtMapItems(results);
		
	}
}
