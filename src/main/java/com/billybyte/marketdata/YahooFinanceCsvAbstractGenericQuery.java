package com.billybyte.marketdata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

/**
 *	Abstract class that fetches prices from a csv file that Yahoo returns when calling the Yahoo finance api
 *  You need to implement 2 abstract methods:
 *  1.  V getValue(String shortName,String[] line);  // which gets the value that you want to return to the caller of the query
 *  2.  V getNotFound(String shortName);  // which returns something in the case that yahoo did not find that shortName
 *  
 * @author bperlman1
 *
 * @param <V>
 */
public abstract class  YahooFinanceCsvAbstractGenericQuery<V> implements QueryInterface<Set<String>,Map<String, V>> {
	// one abstract method to implement
	public abstract V getValue(String shortName,String[] line);
	public abstract V getNotFound(String shortName);

	public static final String YAHOO_EXCH_DELIM = ".";
	public static final String SYSTEM_YAHOO_EXCHANGE_DELIM = "_";
	static final int MAX_GETS_BEFORE_WAIT = 25;
	private final Map<String,String> exchMap;
	private final Map<String,String> prodAssocMap;

	private static final String STK_SUFFIX = "." + SecSymbolType.STK.toString() + "." + SecExchange.SMART.toString();
	private static final String FUT_SUFFIX = "." + SecSymbolType.FUT.toString() + ".";	
	private static final String REPLACE_STRING = "STOCK_SYMBOLS_WITH_PLUS_SIGNS";
	//http://finance.yahoo.com/d/quotes.csv?s=IBM&f=sbal1p
	private static final String YFQ_QUERY_STRING = 
			"http://finance.yahoo.com/d/quotes.csv?s=STOCK_SYMBOLS_WITH_PLUS_SIGNS&f=FIELDS";
	private final String[] YfFields ;//= {"s","b","a","l1","p"};
	private static String FIELD_REPLACE_STRING = "FIELDS";
	
	
	public YahooFinanceCsvAbstractGenericQuery(
			Map<String,String> exchMap, 
			Map<String,String> prodAssocMap,
			String[] yahooFields){
		this.exchMap = exchMap;
		this.prodAssocMap = prodAssocMap;
		this.YfFields = yahooFields;
	}

	@SuppressWarnings("unchecked")
	public YahooFinanceCsvAbstractGenericQuery(String[] yahooFields){
		this((Map<String,String>)Utils.getXmlData(Map.class,YahooFinanceCsvPdiWithFutQuery.class,"yahooExchangeMap.xml"),new HashMap<String,String>(),yahooFields);
	}
	
	@Override
	public Map<String, V> get(Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String,V> ret = new HashMap<String, V>();
		
		List<String> keyList = new ArrayList<String>(keySet);
		for(int i = 0;i<keyList.size();i+=MAX_GETS_BEFORE_WAIT){
			Set<String> shortKeySet = new HashSet<String>();
			for(int j = 0;j<MAX_GETS_BEFORE_WAIT;j++){
				int index = i + j;
				if(index>=keyList.size())break;
				shortKeySet.add(keyList.get(index));
			}
			ret.putAll(getShortSet(shortKeySet, timeoutValue, timeUnitType));
			try {
				Thread.sleep(MAX_GETS_BEFORE_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return ret;
		
	}
	
	/**
	 * Create lists of keys where each list has up to MAX_GETS_BEFORE_WAIT number
	 *   of symbols.  Use these lists to form stock groups to send to Yahoo
	 * @return
	 */
	public static List<List<String>> createKeyLists(
			Set<String> keySet,
			int maxElementsPerList){
		List<List<String>> ret = new ArrayList<List<String>>();
		List<String> keyList = new ArrayList<String>(keySet);

		for(int i = 0;i<keyList.size();i+=maxElementsPerList){
			List<String> innerList = new ArrayList<String>();
			for(int j = 0;j<maxElementsPerList;j++){
				int index = i + j;
				if(index>=keyList.size())break;
				innerList.add(keyList.get(index));
			}
			ret.add(innerList);
		}
		return ret;

	}

	private Map<String, V> getShortSet(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType){
		Map<String,V> ret = 
				new HashMap<String, V>();
		String replaceString = "";
		Map<String,String> convMap = new HashMap<String,String>();
		for(String keyFromCaller:keySet){
			String key = keyFromCaller;
			if(key.contains(STK_SUFFIX)){
				String symbol = key.split("\\.")[0];
				if(symbol.contains(SYSTEM_YAHOO_EXCHANGE_DELIM)){
					symbol = symbol.replace(SYSTEM_YAHOO_EXCHANGE_DELIM,YAHOO_EXCH_DELIM);
				}
				convMap.put(symbol, key);
				replaceString += symbol+"+";
			}else if(key.contains(FUT_SUFFIX)){
				String[] split = key.split("\\.");
				String symbol = split[0];
				String exch = split[2];
				if(exchMap.containsKey(exch)){
					String prodAssocKey = symbol+FUT_SUFFIX+exch;
					if(prodAssocMap.containsKey(prodAssocKey)) {
						symbol = prodAssocMap.get(prodAssocKey);
					}
					String yahooExch = exchMap.get(exch);
					String monthYear = Dates.getMYYfromYYYYMM(split[4]);
					String yahooFormat = symbol+monthYear+"."+yahooExch;
					convMap.put(yahooFormat, key);
					replaceString += yahooFormat+"+";
				} else {
					Utils.prtObErrMess(this.getClass(), "Yahoo does not support exchange "+exch);
					continue;
				}
			} else {
				replaceString += key+"+";
				continue;
			}
		}
		if(replaceString.length()<1)return ret;
		replaceString = replaceString.substring(0,replaceString.length()-1);
		String fieldsToSend = "";
		for(String field:YfFields){
			fieldsToSend += field;
		}
		String queryString = 
				YFQ_QUERY_STRING.replace(REPLACE_STRING, replaceString);
		 queryString = 
					queryString.replace(FIELD_REPLACE_STRING, fieldsToSend);
		
		List<String[]> csvData = Utils.getCSVData(queryString);
		for(String[] line:csvData){
			if(line.length<YfFields.length || line[0].compareTo("  ")<=0){
				continue;
			}
			int i = 0;
			String shortName = "";
			if(convMap.containsKey(line[i])){
				shortName = convMap.get(line[i]);
				ret.put(shortName, getValue(shortName,line));
			} else {
				Utils.prtObErrMess(this.getClass(), "No key in conv map for "+line[i]);
			}
		}
		
		// now put in bad keys that were not found
		for(String key:keySet){
			if(ret.containsKey(key) || !key.contains(STK_SUFFIX))continue;
			V err = getNotFound(key);
			ret.put(key, err);				 
		}
		
		return ret;

	}


}
