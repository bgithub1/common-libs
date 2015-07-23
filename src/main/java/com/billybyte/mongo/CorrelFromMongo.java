package com.billybyte.mongo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.futures.SpanHist;
import com.billybyte.mathstuff.MathStuff;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 *  Correlation records
 * @author bperlman1
 *
 */
public class CorrelFromMongo extends MongoBaseAbstract<CorrelFromMongo>{
	private static final String SEP = MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR;
	private static final String REGEX_SEP = "\\"+SEP;
	private static final String DB_NAME = MongoDatabaseNames.CORREL_DB;
	private static final String COLL_NAME = MongoDatabaseNames.CORREL_CL;
	private static final int MIN_DAYS = 20;
	private final String _id;
	private final BigDecimal value;
	
	public CorrelFromMongo(){
		this(null,null);
	}
	
	public CorrelFromMongo(String _id, BigDecimal value) {
		super();
		this._id = _id;
		this.value = value;
	}
	
	




	@SuppressWarnings("rawtypes")
	@Override
	protected CorrelFromMongo fromMongoDbo(DBObject dbo) {
		Map m = dbo.toMap();
		String id = m.get("_id").toString();
		Object o = m.get("value");
		String valueFromDb = "";
		BigDecimal value=null;
		if(o!=null){
			valueFromDb = o.toString();
			if(valueFromDb.compareTo(" ")>0){
				value = new BigDecimal(valueFromDb);	
			}
		}
		
		return new CorrelFromMongo(id, value);
	}

	@Override
	public DBObject toDBObject() {
		DBObject ret = new BasicDBObject();
		ret.put("_id", this._id);
		String valueToWrite = "";
		if(this.value!=null)valueToWrite = this.value.toString();
		ret.put("value", valueToWrite);
		return ret;
	}

	
	
	public String get_id() {
		return _id;
	}






	public BigDecimal getValue() {
		return value;
	}






	@Override
	protected String getDatabaseName() {
		return DB_NAME;
	}

	@Override
	protected String getCollName() {
		return COLL_NAME;
	}
	
	public String[] getPairNames(){
		return _id.split(REGEX_SEP);
	}

	public static String makePairName(			
			String sec1,
			String sec2){
		return sec1+SEP+sec2;
	}

	public static CorrelFromMongo getFromHist(
			MongoCollectionWrapper mcw,
			String pairName){
		
		String[] secNames = pairName.split(REGEX_SEP);
		String sec1 = secNames[0];
		String sec2 = secNames[1];
		DBObject search = new BasicDBObject();
		search.put("shortName", sec1);
		List<SpanHist> hist1List = mcw.getList(SpanHist.class, search);
		search.put("shortName", sec2);
		List<SpanHist> hist2List = mcw.getList(SpanHist.class, search);
		TreeMap<Integer, BigDecimal> tm1 = new TreeMap<Integer, BigDecimal>();
		for(SpanHist sh : hist1List){
			tm1.put(sh.getYyyyMmDd(),sh.getSettle());
		}
		TreeMap<Integer, BigDecimal> tm2= new TreeMap<Integer, BigDecimal>();
		for(SpanHist sh : hist2List){
			tm2.put(sh.getYyyyMmDd(),sh.getSettle());
		}
		TreeSet<Integer> commonDates = 
				new TreeSet<Integer>(tm1.keySet());
		
		
		commonDates.retainAll(tm2.keySet());
		
		if(commonDates.size()<MIN_DAYS){
			return null;
		}
		List<BigDecimal> l1 = new ArrayList<BigDecimal>();
		List<BigDecimal> l2 = new ArrayList<BigDecimal>();

		for(Integer date : commonDates){
			l1.add(tm1.get(date));
			l2.add(tm2.get(date));
		}

		BigDecimal corr = null;
		try {
			corr = MathStuff.calcCorrelation(l1, l2, 5);
		} catch (Exception e) {
		}
		CorrelFromMongo ret = 
				new CorrelFromMongo(pairName, corr);
		return ret;
	}

}
