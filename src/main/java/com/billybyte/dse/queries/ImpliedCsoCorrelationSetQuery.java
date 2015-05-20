package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;

//import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commoninterfaces.QueryInterface;
//import com.billybyte.commonstaticmethods.WebServiceComLib;
import com.billybyte.csvprocessing.GenericMapFromCsv;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.mongo.MongoXml;
//import com.billybyte.marketdata.futures.csos.NymexCsoSecDefQuery;
import com.billybyte.queries.ComplexQueryResult;
import com.billybyte.queries.QueryFromMap;


public class ImpliedCsoCorrelationSetQuery implements QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>> {
	public static final String REGEX_GET_STRING = "((G[234567])|(G(3B)|(4X)|(6B)))\\.FOP\\.NYMEX";
	// ************** CREATE query for CSO options **************************
	private final QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>> imliedCorrSetQuery;
	
	public ImpliedCsoCorrelationSetQuery(String impliedCorrelationCsvPath){
		imliedCorrSetQuery = createImliedCorrSetQuery(impliedCorrelationCsvPath);
	}
	
	public ImpliedCsoCorrelationSetQuery(MongoXml<BigDecimal> mongoImpliedCso){
		final MongoXml<BigDecimal> finalMongoImpliedCso = mongoImpliedCso;
		imliedCorrSetQuery = new QueryInterface<Set<String>, Map<String,ComplexQueryResult<BigDecimal>>>() {
			
			@Override
			public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
					int timeoutValue, TimeUnit timeUnitType) {
				Map<String, BigDecimal> map = finalMongoImpliedCso.findFromSet(keySet);
				Map<String, ComplexQueryResult<BigDecimal>> ret = 
						new HashMap<String, ComplexQueryResult<BigDecimal>>();
				
				for(String sn : keySet){
					ComplexQueryResult<BigDecimal> cqr = null;
					if(!map.containsKey(sn)){
						cqr = MarketDataComLib.errorRet(sn+" Can't find");
					}else{
						cqr = new ComplexQueryResult<BigDecimal>(null, map.get(sn));
					}
					ret.put(sn, cqr);
				}
				return ret;
			}
		};
	}
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> key,
			int timeoutValue, TimeUnit timeUnitType) {
		
		return imliedCorrSetQuery.get(key, timeoutValue, timeUnitType);
	}

	QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>> createImliedCorrSetQuery(String impliedCsoCorrelationsPath){
		// create an implied CSO correlation query from a csv file in PortolioData
		final GenericMapFromCsv<String, BigDecimal> impliedMap = 
				new GenericMapFromCsv<String, BigDecimal>(impliedCsoCorrelationsPath,true) {
					
					@Override
					protected com.billybyte.commoncollections.Tuple<String, BigDecimal> getKeyAndData(
							int lineNum) {
						String shortName = getLine(lineNum)[0];
						BigDecimal bd = new BigDecimal(getLine(lineNum)[1]);
						return new com.billybyte.commoncollections.Tuple<String, BigDecimal>(shortName,bd);
					}
				}
		;
		
		final QueryFromMap<String, BigDecimal> queryFromMap = new QueryFromMap<String, BigDecimal>(impliedMap);
		QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>> imliedCorrSetQuery = 
				new QueryInterface<Set<String>, Map<String,ComplexQueryResult<BigDecimal>>>() {

					@Override
					public Map<String, ComplexQueryResult<BigDecimal>> get(
							Set<String> key, int timeoutValue, TimeUnit timeUnitType) {
						Map<String,ComplexQueryResult<BigDecimal>> ret = new HashMap<String, ComplexQueryResult<BigDecimal>>();
						if(key==null)return ret;
						for(String sn:key){
							BigDecimal bd = queryFromMap.get(sn, timeoutValue, timeUnitType);
							if(bd==null){
								ComplexQueryResult<BigDecimal> cqr =MarketDataComLib.errorRet(sn+" can't find implied Correlation"); 
								ret.put(sn, cqr);
							}else{
								ComplexQueryResult<BigDecimal> cqr = new ComplexQueryResult<BigDecimal>(null, bd);
								ret.put(sn, cqr);
							}
						}
						return ret;
					}
			
				}
		;
		return imliedCorrSetQuery;
	}
}
