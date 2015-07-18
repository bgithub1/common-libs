package com.billybyte.marketdata;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.mongo.MongoBaseAbstract;
import com.billybyte.mongo.MongoCollectionWrapper;
import com.billybyte.mongo.MongoDatabaseNames;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UnderlyingInfo extends MongoBaseAbstract<UnderlyingInfo> {
	
	private final String _id;
	private final String underlyingShortName;
	public UnderlyingInfo(String _id, String underlyingShortName) {
		super();
		this._id = _id;
		this.underlyingShortName = underlyingShortName;
	}
	public String get_id() {
		return _id;
	}
	public String getUnderlyingShorName() {
		return underlyingShortName;
	}
	
	public UnderlyingInfo(){
		this(null,null);
	}
	
	@Override
	protected String getDatabaseName() {
		return MongoDatabaseNames.SPAN_UNDER_SNINFO_DB;
	}
	@Override
	protected String getCollName() {
		return MongoDatabaseNames.SPAN_UNDER_SNINFO_CL;
	}
	
	
	@Override
	public String toString() {
		return _id + ", " + underlyingShortName;
	}
	@Override
	protected UnderlyingInfo fromMongoDbo(DBObject dbo) {
		String id = dbo.toMap().get("_id").toString();
		String underSn = dbo.toMap().get("underlyingShortName").toString();
		return new UnderlyingInfo(id, underSn);
	}

	
	@Override
	public DBObject toDBObject() {
		DBObject dbo = new BasicDBObject();
		dbo.put("_id", this._id);
		dbo.put("underlyingShortName",this.underlyingShortName);
		return dbo;
	}
	
	public static void main(String[] args) {
		MongoCollectionWrapper mcw = new MongoCollectionWrapper(
				"127.0.0.1", 27022, 
				MongoDatabaseNames.SPAN_UNDER_SNINFO_DB, 
				MongoDatabaseNames.SPAN_UNDER_SNINFO_CL);
		BasicDBObject searchAll = new BasicDBObject();
		List<UnderlyingInfo> uiList = mcw.getList(UnderlyingInfo.class, searchAll);
		for(UnderlyingInfo ui : uiList){
			Utils.prt(ui.toString());
		}
		
		// now get all of these keys from MarketDataComLib
		QueryInterface<String , SecDef> sdQuery = new SecDefQueryAllMarkets();
		for(UnderlyingInfo ui : uiList){
			SecDef uiSd = sdQuery.get(ui.get_id(), 1, TimeUnit.SECONDS);
			SecDef uiUnder = MarketDataComLib.getUnderylingSecDefFromOptionSecDef(uiSd, sdQuery, null, null, 1, null);
			if(uiUnder==null || uiUnder.getShortName().compareTo(ui.getUnderlyingShorName())!=0){
				Utils.prtObErrMess(UnderlyingInfo.class,"MISSMATCH: Expected: "+ui.underlyingShortName + "  Returned : "+ (uiUnder==null ? "null" : uiUnder.getShortName()));
			}else{
				Utils.prt("success from MarketDataComLib.getUnderylingSecDefFromOptionSecDef: " + ui.getUnderlyingShorName());
			}
		}
		
	}
}
