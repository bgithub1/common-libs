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
	private static final String PREVCLOSE_STRING = "Prev Close:</th><td class=\"yfnc_tabledata1\">[0-9]{1,}\\.[0-9]{0,}</td>";
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
			for(String s : results){
				String regex = "((l10)|([bal]00))_" +  yahooSn.toLowerCase() + "\">[0-9]{1,}\\.[0-9]{0,}</span>" ;
				List<String> l1 = RegexMethods.getRegexMatches(regex, s);
				if(l1!=null & l1.size()>0){
					List<BigDecimal> bidasklastclose = new ArrayList<BigDecimal>();
					for(String s2 : l1){
						List<String> l2 = RegexMethods.getRegexMatches(DEC_STRING,s2);
						BigDecimal value = new BigDecimal(l2.get(0));
						bidasklastclose.add(value);
					}
					
					String closestring1 = RegexMethods.getRegexMatches(PREVCLOSE_STRING,s).get(0);
					String closestring2 = RegexMethods.getRegexMatches(DEC_STRING,closestring1 ).get(0);
					
					BigDecimal close = new BigDecimal(closestring2);
					PriceDisplayImmute pdi = 
							new PriceDisplayImmute(
									sd.getShortName(), 
									bidasklastclose.get(0),1,
									bidasklastclose.get(1),1,
									bidasklastclose.get(2),1, 
									close,Dates.getToday() );
					ComplexQueryResult<PriceDisplayInterface> cqr = 
							new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
					ret.put(key, cqr);
							
				}
			}

		}
		return ret;
	}
	
	public static void main(String[] args) {
		String sn = "IBM.OPT.SMART.USD.20150605.C.170.00";
		SecDefQueryAllMarkets sdQuery = 
				new SecDefQueryAllMarkets();
		YahooOptionCqrPdiQuery q = 
				new YahooOptionCqrPdiQuery(sdQuery);
		Set<String> snSet = new HashSet<String>();
		snSet.add("IBM.OPT.SMART.USD.20150605.C.170.00");
		snSet.add("IBM.OPT.SMART.USD.20150605.C.165.00");
		snSet.add("MSFT.OPT.SMART.USD.20150605.C.45.00");
		Map<String, ComplexQueryResult<PriceDisplayInterface>> results = 
				q.get(snSet, 1, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(results);
	}
	
}
