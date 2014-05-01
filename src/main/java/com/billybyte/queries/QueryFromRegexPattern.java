package com.billybyte.queries;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.billybyte.commoncollections.Tuple;
//import com.billybyte.collectionextentions.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.RegexMethods;


/**
 * Query which returns a value V based on the following logic:
 * 	1. A list of regex patterns and associated values gets loaded on construction or by calling
 *     addPvPairExclusive/addPvPairInclusive.
 *  2. During the get(K,int,TimeUnit), the method RegexMethods.getRegexBestMatch 
 *  	is called to find the best against K.toString().
 *  3. The index of the best match is used to get the value which is associated with
 *       that index in the valueList.
 *  
 * @author bperlman1
 *
 * @param <K> - key type
 * @param <V>  - value type
 */
public class QueryFromRegexPattern<K,V> implements QueryInterface<K, V> {
//	private final List<Tuple<Pattern, V>> patternList;
	private final Object list_Lock = new Object(); 
	private final List<Pattern> pList;
	private final List<V> valueList;
	
	private static final <E> List<Tuple<String,E>> makeTupleList (String[] patterns,List<E> values){
		List<Tuple<String,E>> pList = new ArrayList<Tuple<String,E>>();
		for(int i = 0;i< Math.min(patterns.length, values.size());i++){
			Tuple<String, E> tup = 
					new Tuple<String, E>(patterns[i], values.get(i));
			pList.add(tup);
		}
		return pList;
	}
	
	public QueryFromRegexPattern(String[] patterns,V[] values) {
		this(makeTupleList(patterns,Arrays.asList(values)));
	}
	
	public QueryFromRegexPattern(String[] patterns,List<V> values) {
		this(makeTupleList(patterns,values));
	}
	
	
	private static final <V>String[] alphabeticArray(Map<String, V> patternToValueMap){
		return new TreeSet<String>(patternToValueMap.keySet()).toArray(new String[]{});
	}
	private static final <V> List<V> alphabeticListMap(Map<String, V> patternToValueMap){
		String[] ordered = alphabeticArray(patternToValueMap);
		List<V> ret = new ArrayList<V>();
		for(String s:ordered){
			ret.add(patternToValueMap.get(s));
		}
		return ret;
	}
	
	public QueryFromRegexPattern(Map<String, V> patternToValueMap){
		this(alphabeticArray(patternToValueMap),alphabeticListMap(patternToValueMap));
	}

	public QueryFromRegexPattern(List<Tuple<String, V>> patternList) {
		super();
		this.pList = new CopyOnWriteArrayList<Pattern>();
		this.valueList = new CopyOnWriteArrayList<V>();
		for(Tuple<String,V> tuple:patternList){
			Pattern p = Pattern.compile(tuple.getT1_instance());
			addToLists(p, tuple.getT2_instance());
		}
	}

	@Override
	public V get(K key, int timeoutValue, TimeUnit timeUnitType) {
		synchronized (list_Lock) {
			int indexOfBestMatch = RegexMethods.getRegexBestMatch(pList,
					key.toString());
			if (indexOfBestMatch < 0)
				return null;
			return valueList.get(indexOfBestMatch);
		}
	}

	/**
	 * Add a pattern/value pair, and keep all other pattern/value pairs that might also
	 *    yield a more specific match.
	 * @param regexString
	 * @param value
	 */
	public void addPvPairInclusive(String regexString,V value){
		synchronized (list_Lock) {
			Pattern p = Pattern.compile(regexString);
			addToLists(p, value);
		}
	}
	
	/**
	 * THIS LOGIC ONLY WORKS FOR KEYS LIKE PARTIAL SHORTNAMES (SEE THE ONES BELOW)
	 * FOR MORE COMPLEX REGEX PATTERNS, IT DOES NOT WORK!!!!!!!!!
	 * Add a pattern/value pair, and eliminate all other pattern/value pairs that yield
	 *     more specific matches.
	 *     example:
	 *     		addPvPairExclusive("NG.FOP.NYMEX.USD.201210",myValue);  will add the pair
	 *     		"NG.FOP.NYMEX.USD.201210",myValue and eliminate other pairs such as
	 *     		"NG.FOP.NYMEX.USD.201210.C",olderValue or
	 *     		"NG.FOP.NYMEX.USD.201210.C.3.000",olderValue .
	 *     
	 *     In other words, a more "general" pattern will replace all more specific patterns.
	 *     
	 * @param partialShortName - new partial shortName string like "NG.FOP.NYMEX.USD.201210"
	 * @param value - value to add
	 */
	public void addPvPairExclusive(String partialShortName,V value){
		if(partialShortName.contains("(") || 
			partialShortName.contains("|") ||
			partialShortName.contains("[") ){
			addPvPairInclusive(partialShortName, value);
			return;
		}
		Pattern patternToAdd = Pattern.compile(partialShortName);
		int currIndexToPlist =0;
		int originalSize = pList.size();
		synchronized (list_Lock) {
			for (int i = 0; i < originalSize; i++) {
				String patternFromPlist = pList.get(currIndexToPlist).pattern().replace("\\", "");
				List<String> matchTokens = RegexMethods.getRegexMatches(
						patternToAdd, patternFromPlist);
				if (matchTokens != null && matchTokens.size() > 0) {
					// must eliminate this pattern/value pair
					removeFromLists(currIndexToPlist);
				} else {
					currIndexToPlist = currIndexToPlist + 1;
				}
				if (currIndexToPlist >= pList.size())
					break;
			}
			addToLists(patternToAdd, value);
		}
	}
	
	/**
	 * Use this routine to add to the internal lists
	 * @param patternToAdd
	 * @param value
	 */
	protected final void addToLists(Pattern patternToAdd,V value){
		synchronized (list_Lock) {
			pList.add(patternToAdd);
			valueList.add(value);
		}
	}
	
	public void replacePatterns(Map<String, V> patternToValueMap){
		removeAllPatterns();
		List<Tuple<String, V>> patternList = 
				makeTupleList(alphabeticArray(patternToValueMap),alphabeticListMap(patternToValueMap));
		for(Tuple<String,V> tuple:patternList){
			Pattern p = Pattern.compile(tuple.getT1_instance());
			addToLists(p, tuple.getT2_instance());
		}
	}

	
	/**
	 * Use this routine to remove from internal lists 
	 * @param i
	 */
	protected final void removeFromLists(int i){
		synchronized (list_Lock) {
			pList.remove(i);
			valueList.remove(i);
		}
	}
	
	public void removePattern(String patternToRemove){
		synchronized (list_Lock) {
			int indexOfPattern = -1;
			for (int i = 0; i < pList.size(); i++) {
				String patternFromPlist = pList.get(i).pattern();
				if (patternToRemove.compareTo(patternFromPlist) == 0) {
					indexOfPattern = i;
					break;
				}
			}
			if (indexOfPattern > -1) {
				removeFromLists(indexOfPattern);
			}
		}
	}
	
	public void removeAllPatterns(){
		synchronized (list_Lock) {
			pList.clear();
			valueList.clear();
		}
	}
}
