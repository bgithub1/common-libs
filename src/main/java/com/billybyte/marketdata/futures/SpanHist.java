package com.billybyte.marketdata.futures;

import java.math.BigDecimal;
import java.util.Map;

import com.billybyte.mongo.MongoBaseAbstract;
import com.billybyte.mongo.MongoDatabaseNames;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SpanHist extends MongoBaseAbstract<SpanHist>{
	private static final String ID_FIELD = "_id";
	public static final String SHORTNAME_FIELD = "shortName";
	public static final String YYYYMMDD_FIELD = "yyyyMmdd";
	private static final String SETTLE_FIELD = "settle";
	private final String _id;
	private final String shortName;
	private final Integer yyyyMmDd;
	private final BigDecimal settle;
	
	
	public SpanHist(String shortName, Integer yyyyMmDd, BigDecimal settle) {
		super();
		this.shortName = shortName;
		this.yyyyMmDd = yyyyMmDd;
		this.settle = settle;
		if(shortName==null || yyyyMmDd==null){
			this._id = null;
		}else{
			this._id = makeId(shortName, yyyyMmDd);
		}
	}
	
	public SpanHist(){
		this(null,null,null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected SpanHist fromMongoDbo(DBObject dbo) {
		Map<String, Object> m = dbo.toMap();
		String sn = m.get(SHORTNAME_FIELD).toString();
		Integer yyyyMmDd = new Integer(m.get(YYYYMMDD_FIELD).toString());
		BigDecimal settle = new BigDecimal(m.get(SETTLE_FIELD).toString());
		return new SpanHist(sn, yyyyMmDd, settle);
	}

	@Override
	public DBObject toDBObject() {
		DBObject ret = new BasicDBObject();
		ret.put(ID_FIELD, get_id());
		ret.put(SHORTNAME_FIELD,getShortName());
		ret.put(YYYYMMDD_FIELD, getYyyyMmDd());
		ret.put(SETTLE_FIELD,getSettle().toString());
		return ret;
	}

	@Override
	protected String getDatabaseName() {
		
		return MongoDatabaseNames.SPAN_HIST_DB;
	}

	@Override
	protected String getCollName() {
		return MongoDatabaseNames.SPAN_HIST_CL;
	}
	
	private final static String makeId(String shortName,Integer yyyyMmDd){
		return shortName+"_"+yyyyMmDd;
	}

	public String get_id() {
		return _id;
	}

	public String getShortName() {
		return shortName;
	}

	public Integer getYyyyMmDd() {
		return yyyyMmDd;
	}

	public BigDecimal getSettle() {
		return settle;
	}

	@Override
	public String toString() {
		return shortName + ", " + yyyyMmDd + ", " + settle;
	}
	

}
