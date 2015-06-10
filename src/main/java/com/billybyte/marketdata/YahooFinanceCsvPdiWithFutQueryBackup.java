package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
//import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.PriceDisplayImmute;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.queries.ComplexQueryResult;

/**
 * implements QueryInterface<Set<String>,
 * 		Map<String, ComplexQueryResult<PriceDisplayInterface>>> 
 * Takes full shortNames in the Set<String> like IBM.STK.SMART
 * 
 * To get Security data from yahoo.
 * 
 * @author bperlman1
 *
 */
public class YahooFinanceCsvPdiWithFutQueryBackup implements QueryInterface<Set<String>,Map<String, ComplexQueryResult<PriceDisplayInterface>>> {
	public static final String YAHOO_EXCH_DELIM = ".";
	public static final String SYSTEM_YAHOO_EXCHANGE_DELIM = "_";
	static final int MAX_GETS_BEFORE_WAIT = 25;
	private final Map<String,String> exchMap;
	private final Map<String,String> prodAssocMap;
//	private static final int SLEEP_TIME_BETWEEN_YQL_QUERIES = 250;

	private static final String STK_SUFFIX = "." + SecSymbolType.STK.toString() + "." + SecExchange.SMART.toString();
//	private static final String OPT_SUFFIX = "." + SecSymbolType.OPT.toString() + "." + SecExchange.SMART.toString();
	private static final String FUT_SUFFIX = "." + SecSymbolType.FUT.toString() + ".";	
	private static final String REPLACE_STRING = "STOCK_SYMBOLS_WITH_PLUS_SIGNS";
	//IBM130420C00120000
	//http://finance.yahoo.com/d/quotes.csv?s=IBM130420C00120000&f=sbal1p
	private static final String YFQ_QUERY_STRING = 
			"http://finance.yahoo.com/d/quotes.csv?s=STOCK_SYMBOLS_WITH_PLUS_SIGNS&f=FIELDS";
	private static String[] YfFields = {"s","b","a","l1","p"};
	private static String FIELD_REPLACE_STRING = "FIELDS";
	
	public YahooFinanceCsvPdiWithFutQueryBackup(Map<String,String> exchMap, Map<String,String> prodAssocMap){
		this.exchMap = exchMap;
		this.prodAssocMap = prodAssocMap;
	}
	
	@Override
	public Map<String, ComplexQueryResult<PriceDisplayInterface>> get(Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<PriceDisplayInterface>> ret = new HashMap<String, ComplexQueryResult<PriceDisplayInterface>>();
		
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

	private Map<String, ComplexQueryResult<PriceDisplayInterface>> getShortSet(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType){
		long yyyyMmDd = Dates.getYyyyMmDdFromCalendar(Calendar.getInstance());
		Map<String,ComplexQueryResult<PriceDisplayInterface>> ret = 
				new HashMap<String, ComplexQueryResult<PriceDisplayInterface>>();
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
//			String shortName = line[i]+STK_SUFFIX;
			String shortName = "";
			if(convMap.containsKey(line[i])){
				shortName = convMap.get(line[i]);
			} else {
				Utils.prtObErrMess(this.getClass(), "No key in conv map for "+line[i]);
			}
			i += 1;
			BigDecimal bid=null;
			BigDecimal offer=null;
			BigDecimal last=null;
			BigDecimal close=null;
			try {
				bid = (line[i]==null || !RegexMethods.isNumber(line[i])) ? null : 
						new BigDecimal(line[i]);
				i += 1;
				offer = (line[i]==null || !RegexMethods.isNumber(line[i])) ? null : 
					new BigDecimal(line[i]);
				i += 1;
				last = (line[i]==null || !RegexMethods.isNumber(line[i])) ? null : 
					new BigDecimal(line[i]);
				i += 1;
				close = (line[i]==null || !RegexMethods.isNumber(line[i])) ? null : 
					new BigDecimal(line[i]);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			PriceDisplayImmute pdi = 
					new PriceDisplayImmute(shortName, bid, 1, offer, 1, last, 1, close, yyyyMmDd);
			ComplexQueryResult<PriceDisplayInterface> cqr = 
					new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
			
			
			ret.put(shortName, cqr);
		
		}
		
		// now put in bad keys that were not found
		for(String key:keySet){
			if(ret.containsKey(key) || !key.contains(STK_SUFFIX))continue;
			ComplexQueryResult<PriceDisplayInterface> err = 
					MarketDataComLib.errorRet(key+ " Not found in Yahoo finance");
			ret.put(key, err);				 
		}
		
		return ret;

	}

	/**
	 * 
	 * @param args "keys=IBM.STK.SMART,AAPL.STK.SMART,CL.FUT.NYMEX.USD.201512"
	 */
	public static void main(String[] args) {
		// create a query that access bid/offer/last from yahoo
		@SuppressWarnings("unchecked")
		final YahooFinanceCsvPdiWithFutQueryBackup yqlStkQuery = 
				new YahooFinanceCsvPdiWithFutQueryBackup(
						(Map<String,String>)Utils.getXmlData(Map.class,YahooFinanceCsvPdiWithFutQueryBackup.class, "yahooExchangeMap.xml"),
						new HashMap<String,String>());
		// get securities that you want to access from command line
		Map<String, String> argPairs =
				Utils.getArgPairsSeparatedByChar(args, "=");
		CollectionsStaticMethods.prtMapItems(argPairs);
		String commaSepKeys = argPairs.get("keys");
		String[] keys = commaSepKeys.split(",");
		Set<String> keySet = new HashSet<String>();
		
		// loop through securities to create a keyset
		for(String key : keys){
			keySet.add(key);
		}
		
		// access Yahoo with the keyset
		Map<String,ComplexQueryResult<PriceDisplayInterface>> ret =
				yqlStkQuery.get(keySet, 10, TimeUnit.SECONDS);
		// loop through the returned map.  For each entry
		//   check if you got a good or bad return, and print somthing
		for(Entry<String,ComplexQueryResult<PriceDisplayInterface>> entry: ret.entrySet()){
			String sn = entry.getKey();
			ComplexQueryResult<PriceDisplayInterface> cqr = 
					entry.getValue();
			PriceDisplayInterface pdi = null;
			if(cqr.isValidResult()){
				pdi = cqr.getResult();
				// print the security price info
				Utils.prt(pdi.toString());
			}else{
				// print to the error console that you couldn't get this security
				Utils.prtObErrMess(YahooFinanceCsvPdiWithFutQueryBackup.class,sn+" has not pdi return from Yahoo");
			}
			
		}
	}
}
