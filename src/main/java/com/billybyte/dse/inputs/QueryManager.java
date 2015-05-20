package com.billybyte.dse.inputs;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.TypedMap;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.queries.DseInputQuery;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;
import com.billybyte.queries.QueryFromListOfQueriesByRegexPattern;

/**
 * QueryManager manages access to Queries that the DerivativeSetEngine needs
 *   in order to feed inputs to its models.
 *   
 * @author bperlman1
 *
 */
public class QueryManager {
	// get secdefs by mapping a SecDef shortName to a regex string
	private transient final QueryFromListOfQueriesByRegexPattern<List<SecDef>> underlyingQueries = 
			new QueryFromListOfQueriesByRegexPattern<List<SecDef>>(new String[]{}, new ArrayList<QueryInterface<String,List<SecDef>>>());
	private transient final Object underlyingQueries_Lock = new Object();

	// this heterogenous map holds queries per type of query
	private transient final TypedMap queryPerTypeMap = 
			new TypedMap();
	private transient final Object query_Lock = new Object();

	
	public QueryManager() {
		super();
	}

	public QueryManager(
			Map<DioType<?>, DseInputQuery<?>> dseInputQueryMap,
			Map<String,QueryInterface<String, List<SecDef>>> regexSearchableUnderlyingQueries){
		// register queries per DioType
		for(Entry<DioType<?>, DseInputQuery<?>> modelEntry:dseInputQueryMap.entrySet()){
			registerDioTypePrivate(modelEntry.getKey(), modelEntry.getValue());
		}
		if(regexSearchableUnderlyingQueries!=null && regexSearchableUnderlyingQueries.size()>0){
			for(Entry<String,QueryInterface<String, List<SecDef>>> sdQueryEntry:regexSearchableUnderlyingQueries.entrySet()){
				addUnderlyingSecDefQuery(sdQueryEntry.getKey(), sdQueryEntry.getValue());
			}
		}
		// hack to replace corrQuery
		registerDioType(
				new CorrDiot(), 
				new CorrQueryReplace(getQuery(new CorrDiot())));
	}
	
	/**
	 * 
	 * @param diType DiType<T> like new AtmDit(), or new SecDefDit()
	 * @param queryToPut - examples:
	 * 	  for AtmDit:
	 * 		QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>>
	 */
	public <I> void registerDioType(DioType<I> dioType,
			DseInputQuery<I> queryToPut){
		synchronized (query_Lock) {
			queryPerTypeMap.put(dioType, queryToPut);
		}
	}

	/**
	 * 
	 * @param diType DiType<T> like new AtmDit(), or new SecDefDit()
	 * @param queryToPut - examples:
	 * 	  for AtmDit:
	 * 		QueryInterface<Set<String>,Map<String,ComplexQueryResult<BigDecimal>>>
	 */
	private void registerDioTypePrivate(DioType dioType,
			DseInputQuery queryToPut){
		synchronized (query_Lock) {
			queryPerTypeMap.put(dioType, queryToPut);
		}
	}

	
	public void addUnderlyingSecDefQuery(String regexPattern, QueryInterface<String, List<SecDef>> underlyingSecDefQuery){
		synchronized (underlyingQueries_Lock) {
			this.underlyingQueries.addNewQueryInclusive(regexPattern,
					underlyingSecDefQuery);
		}
	}


	/**
	 * Get a map of String (for the derivative shortName) and List<I> (for the multiple
	 *   inputs that can be associated with input of type <I>).
	 *   
	 * @param dioType - like AtmDiot, etc
	 * @param shortNameSetMap - a Map<String,Set<String>> where the key
	 * 		to the map is the derivative shortName, and the inner Set<String>
	 * 		is a set of shortNames (or other identifiers) associated with 
	 * 		the derivative shortName - for this input <I>.
	 * @param timeoutValue
	 * @param timeUnitType
	 * @return Map<String, ComplexQueryResult<List<I>>> 
	 */
	public <I> Map<String, ComplexQueryResult<I>> getDioInputs(
			DioType<I> dioType,Set<String> shortNameSetMap,
			int timeoutValue, TimeUnit timeUnitType){
		synchronized (query_Lock) {
			if (!queryPerTypeMap.containsKey(dioType))
				return null;
			DseInputQuery<I> q = queryPerTypeMap.get(dioType);
			if(q==null){
				return null;
			}
			Map<String, ComplexQueryResult<I>> ret = q.get(shortNameSetMap, timeoutValue, timeUnitType);
			if (ret == null)
				return null;
			return ret;
		}
	}
	
	private static final Map<String, ComplexQueryResult<?>> COMPAREMAP = 
			new HashMap<String, ComplexQueryResult<?>>();
	@SuppressWarnings("unchecked")
	public Map<String, ComplexQueryResult<?>> getDioInputsWithUnSpecifiedType(
			Set<String> shortNameSetMap,DioType<?> dioType,
			int timeoutValue, TimeUnit timeUnitType){
		synchronized (query_Lock) {
			if (!queryPerTypeMap.containsKey(dioType))
				return null;
			DseInputQuery<?> q = queryPerTypeMap.get(dioType);
			if(q==null){
				return null;
			}
			Object o = q.get(shortNameSetMap, timeoutValue, timeUnitType);
			Map<String, ComplexQueryResult<?>> ret = null;
			if(COMPAREMAP.getClass().isAssignableFrom(o.getClass())){
				ret = (Map<String, ComplexQueryResult<?>>)o;
			}
			if (ret == null)
				return null;
			return ret;
		}
	}
	
	
	
	public <I>  DseInputQuery<I> getQuery(DioType<I>dioType){
		if (!queryPerTypeMap.containsKey(dioType.name()))
			return null;
		DseInputQuery<I> q = queryPerTypeMap.get(dioType);
		return q;
	}
	
	public List<SecDef> getUnderlyingSecDefs(String derivativeShortName,
			int timeoutValue, TimeUnit timeUnitType){
		synchronized (underlyingQueries_Lock) {
			return this.underlyingQueries.get(derivativeShortName,
					timeoutValue, timeUnitType);
		}
	}
	
//	public QueryInterface<String, List<SecDef>> getBaseUnderlyingQuery(){
//		return this.underlyingQueries.getQueryToUse("", 1, TimeUnit.SECONDS);
//	}
	
	public QueryInterface<String, List<SecDef>> getUnderlyingQueryFromRegex(String regex){
		return this.underlyingQueries.getQueryToUse(regex, 1, TimeUnit.SECONDS);
	}
	
	private  class CorrQueryReplace extends DseInputQuery<BigDecimal>{
		private final DseInputQuery<BigDecimal> originalCorrQuery;
		CorrQueryReplace(DseInputQuery<BigDecimal> originalCorrQuery){
			this.originalCorrQuery=originalCorrQuery;
		}
		@Override
		public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> derivNames,
				int timeoutValue, TimeUnit timeUnitType) {
			// TODO Auto-generated method stub
			return corrFromDerivNameSet(derivNames, timeoutValue, timeUnitType,
					originalCorrQuery);
		}
		
	}
	
	private static final DioType<BigDecimal> diotForCorrQuery = new CorrDiot();
	private Map<String, ComplexQueryResult<BigDecimal>> corrFromDerivNameSet(
				Set<String> derivNames, int timeoutValue, TimeUnit timeUnitType,
				DseInputQuery<BigDecimal> underlyingCorrQuery) {
		//DseInputQuery<BigDecimal> underlyingCorrQuery = getQuery(diotForCorrQuery);
		// replace all keyset values with their underlyings.  create map of keySet key to underlying
		Map<String, List<String>> derivToUnderNameList =
				getUnderlyingsPerDerivName(derivNames);
		Map<String,String> underNamePerDn = new HashMap<String, String>();
		Set<String> underSet = new HashSet<String>();
		for(Entry<String, List<String>> entry:derivToUnderNameList.entrySet()){
			String underName = entry.getValue().get(0);
			String derivName = entry.getKey();
			underNamePerDn.put(derivName,underName);
			underSet.add(underName);
		}
		//  call yourself
		Map<String,ComplexQueryResult<BigDecimal>> underCorrsMap =
				underlyingCorrQuery.get(underSet, timeoutValue, timeUnitType);
		// build new key pairs out of original derivNames
		List<String> ordered = new ArrayList<String>(new TreeSet<String>(derivNames));
		Map<String,ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(String derivSn0:ordered){
			for(String derivSn1:ordered){
				String derivCorrKey = derivSn0+MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR+derivSn1;
				String underCorrKey = 
						underNamePerDn.get(derivSn0)+
						MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR+
						underNamePerDn.get(derivSn1);
				// get underCorr
				ComplexQueryResult<BigDecimal> underCorr = 
						underCorrsMap.get(underCorrKey);
				if(underCorr!=null)ret.put(derivCorrKey, underCorr);
			}
			
		}
		
		return ret;
	}
		

	
	private  Map<String,List<String>> getUnderlyingsPerDerivName(
			Set<String> derivNames){
		Map<String,List<String>> ret = 
				new HashMap<String, List<String>>();
		
		
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Set<String> underDiotNames = new HashSet<String>();
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			List<String> underList = new ArrayList<String>();
			List<SecDef> sdList = getUnderlyingSecDefs(derivName, 1, TimeUnit.SECONDS);
			if(sdList==null){
				throw Utils.IllState(this.getClass(), derivName + " : can't find SecDef");
			}
			for(SecDef sd:sdList){
				String underName = sd.getShortName();
				underList.add(underName);
				underDiotNames.add(sd.getShortName());
			}
			ret.put(derivName,underList);
		}
		return ret;
	}

}
