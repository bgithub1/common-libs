package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;

import java.util.Set;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.deprecated.CommodCorrelMatrix;
import com.billybyte.deprecated.CommodToStockEtfConversionQuery;
//import com.billybyte.commonstaticmethods.MarketDataComLib;

import com.billybyte.mongo.QueryFromMongoXml;
import com.billybyte.queries.ComplexQueryResult;

public class CorrelationSetQueryFromMongoCmcDiffMapUsingPairStrings implements QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> {
	private final QueryFromMongoXml<CorrPair> mongoQuery;
	private final CommodCorrelMatrix cmcMatrix;
	private final CommodToStockEtfConversionQuery convertQuery = 
			new CommodToStockEtfConversionQuery();
	private final Map<String,BigDecimal> cache = new ConcurrentHashMap<String, BigDecimal>();
	
	
	/**
	 * THE KEYS IN THIS QUERY ARE KEYS OF PAIRS, LIKE:
	 * 	CL.FUT.NYMEX.USD.201212__CL.FUT.NYMEX.USD.201301
	 * 
	 * @param ipOfMongo
	 * @param portOfMongo
	 * @param databaseName
	 * @param collectionName
	 * @param commodCorrQuery - accept the commodities query as an arg.
	 */
	public CorrelationSetQueryFromMongoCmcDiffMapUsingPairStrings(
			String ipOfMongo, 
			Integer portOfMongo, 
			String databaseName, 
			String collectionName,
			String cmcDiffMapPath, String commodProdCorrPath){
		QueryFromMongoXml<CorrPair> mongoQuery=null;
		try {
			mongoQuery  = new QueryFromMongoXml<CorrPair>(ipOfMongo, portOfMongo, databaseName, collectionName);
		} catch (Exception e) {
		}
		this.mongoQuery = mongoQuery;
		this.cmcMatrix = new CommodCorrelMatrix(
				cmcDiffMapPath,commodProdCorrPath,new ArrayList<SecDef>());
	}

	public CorrelationSetQueryFromMongoCmcDiffMapUsingPairStrings(
			String ipOfMongo, 
			Integer portOfMongo, 
			String databaseName, 
			String collectionName){
		QueryFromMongoXml<CorrPair> mongoQuery=null;
		try {
			mongoQuery  = new QueryFromMongoXml<CorrPair>(ipOfMongo, portOfMongo, databaseName, collectionName);
		} catch (Exception e) {
		}
		this.mongoQuery = mongoQuery;
		this.cmcMatrix = new CommodCorrelMatrix(
				new ArrayList<SecDef>());
	}
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> key,
			int timeoutValue, TimeUnit timeUnitType) {
		
		Map<String,ComplexQueryResult<BigDecimal>> cachedRet = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		Map<String,ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		
		// get all good pairs
		Set<String> goodPairs = new TreeSet<String>();
		for(String pair:key){
			String[] shortNames= getShortNamesFromPair(pair);
			if(shortNames!=null){
				if(cache.containsKey(pair)){
					BigDecimal value = cache.get(pair);
					cachedRet.put(pair, new ComplexQueryResult<BigDecimal>(null,value));
				}else{
					goodPairs.add(pair);
				}
			}else{
				ComplexQueryResult<BigDecimal> cqrErr= MarketDataComLib.errorRet(pair+" bad PairName");
				ret.put(pair, cqrErr);	
			}
		}
		
		// ************** code that checks for mongo ********************
		//**************** this code makes sure that, if any pair is in mongo, you
		// ************* use mongo ***********************************************
		// see which ones are in mongo
		// if they are, then get them into ret, and take them out of goodPairs
		Set<String> remaining = new HashSet<String>(goodPairs);
		for(String pair : goodPairs){
			Map<String,CorrPair> cpMap = mongoQuery.get(pair, timeoutValue, timeUnitType);
			if(cpMap!=null && cpMap.size()>0 && cpMap.containsKey(pair)){
				ret.put(pair, new ComplexQueryResult<BigDecimal>(null, new BigDecimal(cpMap.get(pair).getCorr())));
				remaining.remove(pair);
				continue;
			}
		}
		// replace goodPairs with remaining
		goodPairs = remaining;
		// ************** end of code that checks for mongo ********************
		
		
		// get all pairs that are just stocks
		Set<String> nonFutSet = new TreeSet<String>();
		Set<String> futSet = new TreeSet<String>();
		Set<String> mixedSet = new TreeSet<String>();
		for(String pair:goodPairs){
			String[] shortNames= getShortNamesFromPair(pair);
			if(shortNames!=null){
				String sn0 = shortNames[0];
				String sn1 = shortNames[1];
				// NOT FUTURES
				if(!sn0.contains(".FUT.") && !sn1.contains(".FUT.")){
					nonFutSet.add(pair);
					continue;
				}			
				// ONLY FUTURES
				if(sn0.contains(".FUT.") && sn1.contains(".FUT.")){
					futSet.add(pair);
					continue;
				}
				// MIXED
				mixedSet.add(pair);
			}
		}
		
		// the nonFutSet should be empty b/c we previous got everyThing that was in Mongo
		//  which included all of the stocks
		for(String pair:nonFutSet){
			Map<String,CorrPair> cpMap = mongoQuery.get(pair, timeoutValue, timeUnitType);
			if(cpMap!=null && cpMap.size()>0 && cpMap.containsKey(pair)){
				ret.put(pair, new ComplexQueryResult<BigDecimal>(null, new BigDecimal(cpMap.get(pair).getCorr())));
				continue;
			}
		}

		for(String pair:futSet){
			try {
				double correlation =cmcMatrix.getCorrelationCoefficientFromShortNames(pair);
				ComplexQueryResult<BigDecimal> cqr = new ComplexQueryResult<BigDecimal>(null, new BigDecimal(correlation));
				ret.put(pair, cqr);
			} catch (Exception e) {
				ComplexQueryResult<BigDecimal> cqrErr= MarketDataComLib.errorRet(pair+" "+e.getMessage());
				ret.put(pair, cqrErr);
			}
		}
		
		for(String pair:mixedSet){
			String synthPair = convertQuery.get(pair, 1, TimeUnit.SECONDS);
			if(synthPair!=null){
				Map<String,CorrPair> cpSynthMap = mongoQuery.get(synthPair, timeoutValue, timeUnitType);
				if(cpSynthMap!=null && cpSynthMap.size()>0 && cpSynthMap.containsKey(synthPair)){
					CorrPair cp = cpSynthMap.get(synthPair);
					ret.put(pair, new ComplexQueryResult<BigDecimal>(null, new BigDecimal(cp.getCorr())));
					continue;
				}else{
					ComplexQueryResult<BigDecimal> cqrErr= MarketDataComLib.errorRet(pair+" bad PairName");
					ret.put(pair, cqrErr);	
					continue;
				}
			}
		}
		
		for(Entry<String,ComplexQueryResult<BigDecimal>>entry:ret.entrySet()){
			if(entry.getValue().isValidResult()){
				this.cache.put(entry.getKey(),entry.getValue().getResult());
			}
		}
		ret.putAll(cachedRet);
		return ret;
	}

	
	String[] getShortNamesFromPair(String pair){
		String[] shortNames= pair.split(CollectionsStaticMethods.DEFAULT_CORRELATION_SEPARATOR);
		if(shortNames.length!=2)return null;
		return shortNames;

	}
}
