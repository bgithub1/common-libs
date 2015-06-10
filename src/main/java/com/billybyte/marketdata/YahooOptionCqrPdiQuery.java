package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.queries.ComplexQueryResult;

public class YahooOptionCqrPdiQuery implements QueryInterface<Set<String>,Map<String, ComplexQueryResult<PriceDisplayInterface>>>{
	private static final String FIRST_PART = "http://finance.yahoo.com/q?s=";
	private static final String DEC_STRING = "[0-9]{1,}\\.[0-9]{0,}";
	private static final String BID_STRING = "b00_YAHOOLOWERCASEOPTIONNAME\">[0-9]{1,}\\.[0-9]{0,}</span>";
	private static final String ASK_STRING = "a00_YAHOOLOWERCASEOPTIONNAME\">[0-9]{1,}\\.[0-9]{0,}</span>";
	private static final String LAST_STRING = "l10_YAHOOLOWERCASEOPTIONNAME\">[0-9]{1,}\\.[0-9]{0,}</span>";
	private static final String PREVCLOSE_STRING = "Prev Close:</th><td class=\"yfnc_tabledata1\">[0-9]{1,}\\.[0-9]{0,}</td>";
	private static final int PRICE_SIZE=1;
	//Prev Close:</th><td class=\"yfnc_tabledata1\">[0-9]{1,}\\.[0-9]{0,}</td>
	private final QueryInterface<String, SecDef> sdQuery;
	
	public YahooOptionCqrPdiQuery(QueryInterface<String, SecDef> sdQuery) {
		super();
		this.sdQuery = sdQuery;
	}
	@Override
	public Map<String, ComplexQueryResult<PriceDisplayInterface>> get(
			Set<String> shortNameSet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String, ComplexQueryResult<PriceDisplayInterface>> ret = 
				new HashMap<String, ComplexQueryResult<PriceDisplayInterface>>();
		// loop through all shortNames
		for(String key : shortNameSet){
			if(!key.contains(".OPT.")){
				Exception e = Utils.IllState(this.getClass(), key + " is not an OPT contract");
				ComplexQueryResult<PriceDisplayInterface> cqr = 
						new ComplexQueryResult<PriceDisplayInterface>(e, null);
				ret.put(key,cqr);
				continue;
			}
			
			SecDef sd = sdQuery.get(key, 1, TimeUnit.SECONDS);
			int year = sd.getContractYear() % 100;
			int month = sd.getContractMonth();
			int day = sd.getContractDay();
			String yymmdd = new Integer(year*100*100+month*100+day).toString();
			String right = sd.getRight();
			DecimalFormat nf = new DecimalFormat("#00000000");
			String strike = nf.format(sd.getStrike().multiply(new BigDecimal("1000")));
			String yahooSn = sd.getSymbol()+yymmdd+right+strike;
			String yahooUrl = FIRST_PART + yahooSn;
			List<String> results = 
					Utils.readHttp(yahooUrl);
			BigDecimal bid=null;
			BigDecimal ask=null;
			BigDecimal last=null;
			BigDecimal pclose=null;
			for(String s : results){
				// do bid
				BigDecimal bidPrice = getPriceFromHtml(yahooSn,s,BID_STRING);
				if(bidPrice!=null){
					bid=bidPrice;
				}
				// do ask
				BigDecimal askPrice = getPriceFromHtml(yahooSn,s,ASK_STRING);
				if(askPrice!=null){
					ask=askPrice;
				}
				// do last
				BigDecimal lastPrice = getPriceFromHtml(yahooSn,s,LAST_STRING);
				if(lastPrice!=null){
					last=lastPrice;
				}
				// do pclose
				BigDecimal pclosePrice = getPriceFromHtml(yahooSn,s,PREVCLOSE_STRING);
				if(pclosePrice!=null){
					pclose=pclosePrice;
				}
				
//				String regex = "((l10)|([bal]00))_" +  yahooSn.toLowerCase() + "\">[0-9]{1,}\\.[0-9]{0,}</span>" ;
//				List<String> l1 = RegexMethods.getRegexMatches(regex, s);
//				
//				if(l1!=null & l1.size()>0){
//					List<BigDecimal> bidasklastclose = new ArrayList<BigDecimal>();
//					for(String s2 : l1){
//						List<String> l2 = RegexMethods.getRegexMatches(DEC_STRING,s2);
//						BigDecimal value = new BigDecimal(l2.get(0));
//						bidasklastclose.add(value);
//					}
//					
//					String closestring1 = RegexMethods.getRegexMatches(PREVCLOSE_STRING,s).get(0);
//					String closestring2 = RegexMethods.getRegexMatches(DEC_STRING,closestring1 ).get(0);
//					
//					BigDecimal close = new BigDecimal(closestring2);
//					PriceDisplayImmute pdi = 
//							new PriceDisplayImmute(
//									sd.getShortName(), 
//									bidasklastclose.get(0),1,
//									bidasklastclose.get(1),1,
//									bidasklastclose.get(2),1, 
//									close,Dates.getToday() );
//					ComplexQueryResult<PriceDisplayInterface> cqr = 
//							new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
//					ret.put(key, cqr);
//							
//				}
			}
			
			// now create cqr to return to user
			ComplexQueryResult<PriceDisplayInterface> cqr = null;
			if(pclose==null){
				// you've got a problem
				Exception e = Utils.IllState(this.getClass(), "Can't get prices for "+ key);
				cqr = 
						new ComplexQueryResult<PriceDisplayInterface>(e, null);
			}else{
				// good return
				PriceDisplayImmute pdi = 
						new PriceDisplayImmute(
								sd.getShortName(), 
								bid,PRICE_SIZE,
								ask,PRICE_SIZE,
								last,PRICE_SIZE,
								pclose,Dates.getToday() );
				cqr = 
						new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
			}
			ret.put(key, cqr);

		}
		return ret;
	}
	
	/**
	 * find the price field in an html string for things like bid, ask and last prices
	 * @param yahooSn
	 * @param htmlString
	 * @param regexString
	 * @return
	 */
	private final BigDecimal getPriceFromHtml(String yahooSn,String htmlString,String regexString){
		String regex = regexString.replace("YAHOOLOWERCASEOPTIONNAME", yahooSn.toLowerCase());
		List<String> l1 = RegexMethods.getRegexMatches(regex, htmlString);
		if(l1!=null & l1.size()>0){
			// found field
			String s1 = l1.get(0);
			List<String> l2 = RegexMethods.getRegexMatches(DEC_STRING,s1);
			if(l2!=null & l2.size()>0){
				// found price
				String s2 = l2.get(0);	
				return new BigDecimal(s2);
			}else{
				return null;
			}
		}else{
			return null;
		}
	}
	
	public static void main(String[] args) {
		SecDefQueryAllMarkets sdQuery = 
				new SecDefQueryAllMarkets();
		YahooOptionCqrPdiQuery q = 
				new YahooOptionCqrPdiQuery(sdQuery);
		Set<String> snSet = new HashSet<String>();
		snSet.add("IBM.OPT.SMART.USD.20170120.C.170.00");
		snSet.add("IBM.OPT.SMART.USD.20170120.C.200.00");
		snSet.add("IBM.OPT.SMART.USD.20170120.C.165.00");
		snSet.add("MSFT.OPT.SMART.USD.20170120.C.45.00");
		Map<String, ComplexQueryResult<PriceDisplayInterface>> results = 
				q.get(snSet, 1, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(results);
	}
	
}
