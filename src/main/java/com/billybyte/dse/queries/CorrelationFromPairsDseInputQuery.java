package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.futures.SpanHist;
import com.billybyte.mongo.CorrelFromMongo;
import com.billybyte.mongo.MongoCollectionWrapper;
import com.billybyte.mongo.MongoDatabaseNames;
import com.billybyte.queries.ComplexQueryResult;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class CorrelationFromPairsDseInputQuery  extends DseInputQuery<BigDecimal>{
	private final Object correlCacheLock = new Object();
	private final HashMap<String, BigDecimal> correlCache = new HashMap<String, BigDecimal>();
	private final MongoCollectionWrapper spanHistMcw;
	private final MongoCollectionWrapper correlMcw;
	
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		
		
		
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		List<CorrelFromMongo> listToWriteToMongo = new ArrayList<CorrelFromMongo>();
		
		for(String pairName : keySet){
			
			//**  try cache **
			synchronized (correlCacheLock) {
				if (correlCache.containsKey(pairName)) {
					BigDecimal cachebd = correlCache.get(pairName);
					if (cachebd != null) {
						ret.put(pairName,
								new ComplexQueryResult<BigDecimal>(null,
										cachebd));
						continue;
					}else{
						Exception e = Utils.IllState(this.getClass(), pairName
								+ " has no correlation");
						ret.put(pairName, new ComplexQueryResult<BigDecimal>(e,
								null));
						continue;
					}
				}
			}
			
			
			// **** not in cache, try db *****
			DBObject search = new BasicDBObject();
			search.put("_id", pairName);
			List<CorrelFromMongo> corList = correlMcw.getList(
					CorrelFromMongo.class, search);
			if (corList != null && corList.size() > 0) {
				// *** use db version ***
				BigDecimal dbbd = corList.get(0).getValue();
				ret.put(pairName, new ComplexQueryResult<BigDecimal>(null,
						dbbd));
				synchronized (correlCacheLock) {
					correlCache.put(pairName, dbbd);
				}
				continue;
			}
			
			//*** get from hist ***
			CorrelFromMongo corr = CorrelFromMongo.getFromHist(spanHistMcw,
					pairName);
			if (corr != null) {
				BigDecimal corrbd = corr.getValue();
				ret.put(pairName, new ComplexQueryResult<BigDecimal>(null,
						corrbd));
				synchronized (correlCacheLock) {
					correlCache.put(pairName, corrbd);
					listToWriteToMongo.add(corr);
				}
				continue;
			}
			
			
			// ** you get here - you have an issue.  report it to caller **
			Exception e = Utils.IllState(this.getClass(), pairName
					+ " has no correlation");
			ret.put(pairName, new ComplexQueryResult<BigDecimal>(e,
					null));
			synchronized (correlCacheLock) {
				correlCache.put(pairName, null);
			}

			
		}
		
		// ** write to correl db stuff that we figured out from hist dbs 
		if(listToWriteToMongo.size()>1){
			List<DBObject> dboList = correlMcw.toDboList(listToWriteToMongo);
			correlMcw.getCollection().insert(dboList);
		}
		return ret;
	}

	public CorrelationFromPairsDseInputQuery(MongoCollectionWrapper spanHistMcw,
			MongoCollectionWrapper correlMcw) {
		super();
		this.spanHistMcw = spanHistMcw;
		this.correlMcw = correlMcw;
	}
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		MongoCollectionWrapper spanHistMcw = 
				new MongoCollectionWrapper("127.0.0.1", 27022, 
						MongoDatabaseNames.SPAN_HIST_DB, MongoDatabaseNames.SPAN_HIST_CL);
		MongoCollectionWrapper correlMcw = 
				new MongoCollectionWrapper("127.0.0.1", 27017, 
						MongoDatabaseNames.CORREL_DB, MongoDatabaseNames.CORREL_CL);
		
		CorrelationFromPairsDseInputQuery query = 
				new CorrelationFromPairsDseInputQuery(spanHistMcw, correlMcw);
		
		// do a regex find of some test data
		String regex = "^((CL)|(HO)|(RB)|(NG)).FUT.NYMEX";
		DBObject regexInnerKey = new BasicDBObject();
		regexInnerKey.put("$regex",regex);
		DBObject regexOuterKey = new BasicDBObject();
		regexOuterKey.put("shortName",regexInnerKey);
		List<SpanHist> shList = spanHistMcw.getList(SpanHist.class, regexOuterKey);
		TreeSet<String> orderedNames = new TreeSet<String>();
		for(SpanHist sh : shList){
			orderedNames.add(sh.getShortName());
		}
		List<String> orderedList = new ArrayList<String>(orderedNames);
		Set<String> pairNames = new TreeSet<String>();
		for(int i = 0;i<orderedList.size();i++){
			String sec1 = orderedList.get(i);
			for(int j = i;j<orderedList.size();j++){
				String sec2 = orderedList.get(j);
				String pairName = 
						CorrelFromMongo.makePairName(sec1, sec2);
				pairNames.add(pairName);
			}
		}
		
		Map<String, ComplexQueryResult<BigDecimal>> results =
				query.get(pairNames, 10, TimeUnit.SECONDS);
		for(Entry<String, ComplexQueryResult<BigDecimal>> entry : results.entrySet()){
			Utils.prt(entry.getKey() + "," + entry.getValue().toString());
			
		}
		
	}

}
