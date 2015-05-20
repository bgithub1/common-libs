package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import com.billybyte.commoninterfaces.QueryInterface;

import com.billybyte.queries.ComplexQueryResult;
/**
 * get excepts a set of shortNames, creates pair names from elements of the set
 * 	and then returns a Map<String,ComplexQueryResult<BigDecimal>> of results
 * @author bperlman1
 *
 */
public class CorrelationSetQueryFromMongoCmcDiffMap implements QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> {
	private final CorrelationSetQueryFromMongoCmcDiffMapUsingPairStrings pairQuery ;
	private final String CORR_SEP = MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR;

	
	
	/**
	 * 
	 * @param ipOfMongo
	 * @param portOfMongo
	 * @param databaseName
	 * @param collectionName
	 * @param commodCorrQuery - accept the commodities query as an arg.
	 */
	public CorrelationSetQueryFromMongoCmcDiffMap(
			String ipOfMongo, 
			Integer portOfMongo, 
			String databaseName, 
			String collectionName,
			String cmcDiffMapPath, String commodProdCorrPath){
		this.pairQuery = 
				new CorrelationSetQueryFromMongoCmcDiffMapUsingPairStrings(ipOfMongo, portOfMongo, databaseName, collectionName, cmcDiffMapPath, commodProdCorrPath);
	}
	
	/**
	 * Constructor using cmcDiffMapPath and  commodProdCorrPath as Resources in the
	 * 	package of this class.
	 * @param ipOfMongo
	 * @param portOfMongo
	 * @param databaseName
	 * @param collectionName
	 */
	public CorrelationSetQueryFromMongoCmcDiffMap(
			String ipOfMongo, 
			Integer portOfMongo, 
			String databaseName, 
			String collectionName){
		this.pairQuery = 
				new CorrelationSetQueryFromMongoCmcDiffMapUsingPairStrings(ipOfMongo, portOfMongo, databaseName, collectionName);
		
	}
	
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> key,
			int timeoutValue, TimeUnit timeUnitType) {
		
		Set<String> ordered = new TreeSet<String>(key);
		Set<String> pairSet = new HashSet<String>();
		for(String sn0:ordered){
			for(String sn1:ordered){
				String pair = makePair(sn0,sn1);
				pairSet.add(pair);
			}
		}
		// change code
		Map<String, ComplexQueryResult<BigDecimal>> pairNoRepeats = 
				this.pairQuery.get(pairSet, timeoutValue, timeUnitType);
		Map<String, ComplexQueryResult<BigDecimal>> ret = new TreeMap<String, ComplexQueryResult<BigDecimal>>();
		for(Entry<String, ComplexQueryResult<BigDecimal>> entry:pairNoRepeats.entrySet()){
			ret.put(entry.getKey(),entry.getValue());
			ret.put(reversePair(entry.getKey()),entry.getValue());
		}
		// end change code
//		return this.pairQuery.get(pairSet, timeoutValue, timeUnitType);
		return ret;
	}

	static boolean xOr(boolean x, boolean y) {
	    return ( ( x || y ) && ! ( x && y ) );
	}
	
	String makePair(String sn0,String sn1){
		String pair = sn0+CORR_SEP+sn1;
		if(sn0.compareTo(sn1)>0){
			pair = sn1+CORR_SEP+sn0;;
		}
		return pair;

	}

	String reversePair(String pair){
		String[] names = pair.split(CORR_SEP);
		String sn0 = names[0];
		String sn1 = names[1];
		if(sn0.compareTo(sn1)<0){
			pair = sn1+CORR_SEP+sn0;;
		}
		return pair;

	}

}
