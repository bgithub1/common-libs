package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.PriceDisplayImmute;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.queries.ComplexQueryResult;

/**
 * extends YahooFinanceCsvAbstractGenericQuery<V> with <<ComplexQueryResult<PriceDisplayInterface>>
 * implements QueryInterface<Set<String>,
 * 		Map<String, ComplexQueryResult<PriceDisplayInterface>>> 
 * Takes full shortNames in the Set<String> like IBM.STK.SMART
 * 
 * To get Security data from yahoo.
 * 
 * @author bperlman1
 *
 */
public class YahooFinanceCsvPdiWithFutQuery extends  YahooFinanceCsvAbstractGenericQuery<ComplexQueryResult<PriceDisplayInterface>> {

	public YahooFinanceCsvPdiWithFutQuery(Map<String, String> exchMap,
			Map<String, String> prodAssocMap) {
		super(exchMap, prodAssocMap, YfFields);
		// TODO Auto-generated constructor stub
	}

	private static String[] YfFields = {"s","b","a","l1","p"};

	@Override
	public ComplexQueryResult<PriceDisplayInterface> getValue(String shortName,
			String[] line) {
		int i = 0;// this is for the shortName column
		BigDecimal bid=null;
		BigDecimal offer=null;
		BigDecimal last=null;
		BigDecimal close=null;
		try {
			i += 1;
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
			e.printStackTrace();
		}
		
		long yyyyMmDd = Dates.getYyyyMmDdFromCalendar(Calendar.getInstance());
		PriceDisplayImmute pdi = 
				new PriceDisplayImmute(shortName, bid, 1, offer, 1, last, 1, close, yyyyMmDd);
		ComplexQueryResult<PriceDisplayInterface> cqr = 
				new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
		
		return cqr;
	}

	@Override
	public ComplexQueryResult<PriceDisplayInterface> getNotFound(
			String shortName) {
		ComplexQueryResult<PriceDisplayInterface> err = 
				MarketDataComLib.errorRet(shortName+ " Not found in Yahoo finance");
						 
		return err;
	}

	public static void main(String[] args) {
		// create a query that access bid/offer/last from yahoo
		@SuppressWarnings("unchecked")
		final YahooFinanceCsvPdiWithFutQuery yqlStkQuery = 
				new YahooFinanceCsvPdiWithFutQuery(
						(Map<String,String>)Utils.getXmlData(Map.class,YahooFinanceCsvPdiWithFutQuery.class, "yahooExchangeMap.xml"),
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
				Utils.prtObErrMess(YahooFinanceCsvPdiWithFutQuery.class,sn+" has not pdi return from Yahoo");
			}
			
		}
	}
	
	
}
