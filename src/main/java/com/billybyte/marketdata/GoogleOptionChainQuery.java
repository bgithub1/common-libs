package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

/**
 * Return a map of SecDefs per Expiry and Strike for an option series
 * @author bperlman1
 *
 */
public class GoogleOptionChainQuery implements QueryInterface<String, TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>>>{
	private static final int BASE_YEAR=2000;
	private final String OPTION_CHAIN = 
//			"http://www.google.com/finance/option_chain?q=STOCKNAME&expm=MM&expy=YYYY&output=json";
			"http://www.google.com/finance/option_chain?q=STOCKNAME&expy=YYYY&output=json";
	private final Calendar evaluationDate;
	private final int daysToAddFromEvaluationDate;
	private final QueryInterface<String, SecDef> sdQuery;
	
	private static final BigDecimal THOUSAND = new BigDecimal("1000");
	private static final DecimalFormat dfYear = 
			new DecimalFormat("0000");
	private static final DecimalFormat dfMonth = 
			new DecimalFormat("00");
	private static final DecimalFormat dfTwoDec = 
			new DecimalFormat("0.00");
	//IBM150612C00115000
	private static final String REGEX_OPTION_SYMBOL = 
			"{1,}[0-9]{2,2}[01][0-9][0123][0-9][CP][0-9]{8,8}";

	
	
	/**
	 * 
	 * @param evaluationDate
	 * @param daysToAddFromEvaluationDate
	 * @param sdQuery
	 */
	public GoogleOptionChainQuery(Calendar evaluationDate,
			int daysToAddFromEvaluationDate,
			QueryInterface<String, SecDef> sdQuery) {
		super();
		this.evaluationDate = evaluationDate;
		this.daysToAddFromEvaluationDate = daysToAddFromEvaluationDate;
		this.sdQuery = sdQuery;
	}

	@Override
	public TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>> get(String key, int timeoutValue,
			TimeUnit timeUnitType) {
		// get year to look for
		String symbol = key.split("\\"+MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR)[0];
		Calendar firstOptionDate =
				Dates.addToCalendar(evaluationDate, daysToAddFromEvaluationDate, Calendar.DAY_OF_YEAR, true);
		long firstOptionYyyyMmDd = 
				Dates.getYyyyMmDdFromCalendar(firstOptionDate);
		int year = firstOptionDate.get(Calendar.YEAR);
		int month = firstOptionDate.get(Calendar.MONTH)+1;
		// get regex by looping through months
		List<String> matches = null;
		int thisMonth = month-1;
		for(int i=0;i<6;i++){
			thisMonth+=1;
			if(thisMonth>12){
				thisMonth=1;
				year = year+1;
			}
			// build google finance url to get options chains
			String yearString = dfYear.format(year);
			String monthString = dfMonth.format(thisMonth);
			String chainUrl = OPTION_CHAIN.replace("STOCKNAME",symbol);
			chainUrl = chainUrl.replace("YYYY",yearString);
			chainUrl = chainUrl.replace("MM",monthString);
			// get option chain
			List<String[]> lines = Utils.getCSVData( chainUrl);
			// combine lines if they come in as multiple lines
			String mergedLines = "";
			for(String[] line:lines){
				for(String s : line){
					mergedLines += s;
				}
			}
			// get all options symbols (CBOE/YAHOO format)
			String regex = symbol + REGEX_OPTION_SYMBOL;
			matches = 
					RegexMethods.getRegexMatches(regex, mergedLines);
			if(matches!=null && matches.size()>0){
				break;
			}
		}
		if(matches==null || matches.size()<1){
			return null;
		}
		// parse into secdefs
		TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>> ret = 
				new TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>>();
		for(String match : matches){
			String withoutSymbol = RegexMethods.getRegexMatches(REGEX_OPTION_SYMBOL, match).get(0);
			int y = new Integer(withoutSymbol.substring(0,2)) + BASE_YEAR;
			int m = new Integer(withoutSymbol.substring(2,4));
			int d = new Integer(withoutSymbol.substring(4,6));
			String ys = dfYear.format(y);
			String ms = dfMonth.format(m);
			String ds = dfMonth.format(d);
			String right = withoutSymbol.substring(6,7);
			BigDecimal strike = 
					new BigDecimal(withoutSymbol.substring(7,15)).divide(THOUSAND);
			String sp = MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR;
			String shortName = symbol+sp+SecSymbolType.OPT+sp+SecExchange.SMART+sp+SecCurrency.USD;
			shortName += sp+ys+ms+ds+sp+right+sp+dfTwoDec.format(strike);
			SecDef sd = sdQuery.get(shortName, 1, TimeUnit.SECONDS);
			long expiry = sd.getExpiryYear()*100*100 + sd.getExpiryMonth()*100+sd.getExpiryDay();

			// check to see if we want to include this expiry
			if(expiry<firstOptionYyyyMmDd)continue;
			
			if(!ret.containsKey(expiry)){
				TreeMap<String,TreeMap<BigDecimal, SecDef>> newTm = new TreeMap<String,TreeMap<BigDecimal, SecDef>>();
				ret.put(expiry, newTm);
			}
			TreeMap<String,TreeMap<BigDecimal, SecDef>> outerTm = ret.get(expiry);
			
			if(!outerTm.containsKey(right)){
				TreeMap<BigDecimal, SecDef> newInnerTm = new TreeMap<BigDecimal, SecDef>();
				outerTm.put(right, newInnerTm);
			}
			TreeMap<BigDecimal, SecDef> innerTm = outerTm.get(right);
			innerTm.put(strike, sd);
					
		}
		return ret;
	}
	
	/**
	 * print the inner tree maps of the results of this query
	 * @param queryResults
	 */
	public static final void printOutput(TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>> queryResults)
	{
		for(Entry<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>> entry:queryResults.entrySet()){
			for(Entry<String, TreeMap<BigDecimal, SecDef>> innerEntry : entry.getValue().entrySet())
			CollectionsStaticMethods.prtMapItems(innerEntry.getValue());
		}
	}
	
	public static final TreeMap<String,TreeMap<BigDecimal, SecDef>> getLastestExpiryMap(TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>> queryResults){
		Long[] exp1 = queryResults.keySet().toArray(new Long[]{});
		long maxExpiry = exp1[exp1.length-1];
		TreeMap<String,TreeMap<BigDecimal, SecDef>> mapPerMaxExpiry = queryResults.get(maxExpiry);
		return mapPerMaxExpiry;
	}
	
	public static void main(String[] args) {
		GoogleOptionChainQuery chainQuery = 
				new GoogleOptionChainQuery(Calendar.getInstance(),
						60, new SecDefQueryAllMarkets());
		String[] sns = {
				"IBM.STK.SMART",
				"AAPL.STK.SMART",
				"MSFT.STK.SMART",
				"GOOG.STK.SMART",
				};
		for(String sn : sns){
			TreeMap<Long,TreeMap<String,TreeMap<BigDecimal, SecDef>>> results = 
					chainQuery.get(sn, 10, TimeUnit.SECONDS);
			if(results==null){
				Utils.prtObErrMess(GoogleOptionChainQuery.class, "no chains for "+sn);
			}else{
				GoogleOptionChainQuery.printOutput(results);
				
			}
		}
	}

}
