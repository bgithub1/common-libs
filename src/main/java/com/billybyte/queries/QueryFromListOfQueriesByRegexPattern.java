package com.billybyte.queries;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
/**
 * A query from a List of Queries where the query that gets selected is
 *  the determined by the best matching regex pattern against the key.
 *  You then use this query to satisfy the call to get(key,timeoutValue,timeUnitType).
 *  
 * @author bperlman1
 *
 * @param <V>
 */
public class QueryFromListOfQueriesByRegexPattern<V> implements 
		QueryInterface<String,V>{
	private final QueryFromRegexPattern<String, QueryInterface<String, V>> queryOfQueries;
	
	public QueryFromListOfQueriesByRegexPattern(String[] patterns,
			List<QueryInterface<String, V>> values) {
		this.queryOfQueries = 
				new QueryFromRegexPattern<String, QueryInterface<String,V>>(
						patterns,values);
	}
	@Override
	public V get(String key, int timeoutValue, TimeUnit timeUnitType) {
		// first get the query
		QueryInterface<String,V> queryToUse = 
				queryOfQueries.get(key,timeoutValue,timeUnitType);
		if(queryToUse==null)return null;
		// now get the value
		V ret = queryToUse.get(key, timeoutValue, timeUnitType);
		return ret;
	}
	
	/**
	 * This method will execute an "exclusive add" of a new query to the list of queries
	 *   that are used to fetch a value.  The exclusive add will add this query, but also,
	 *   eliminate any other queries that are more specific.
	 *   See {@code QueryFromRegexPattern.addPvPairExclusive}
	 * @param newQuery
	 */
	public void addNewQueryExclusive(String regexString,QueryInterface<String, V> newQuery){
		this.queryOfQueries.addPvPairExclusive(regexString, newQuery);
	}

	/**
	 * This method will execute an "inclusive add" of a new query to the list of queries
	 *   that are used to fetch a value.  The inclusive add will add this query, and
	 *   NOT eliminate any other queries that are more specific.
	 *   See {@code QueryFromRegexPattern.addPvPairExclusive}
	 * @param newQuery
	 */
	public void addNewQueryInclusive(String regexString,QueryInterface<String, V> newQuery){
		this.queryOfQueries.addPvPairInclusive(regexString, newQuery);
	}

	public QueryInterface<String, V> getQueryToUse(String key, int timeoutValue, TimeUnit timeUnitType){
		QueryInterface<String,V> queryToUse = 
				queryOfQueries.get(key,timeoutValue,timeUnitType);
		return queryToUse;
	}

}
