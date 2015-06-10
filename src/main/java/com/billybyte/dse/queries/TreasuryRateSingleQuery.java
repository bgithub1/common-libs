package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.InterestRates;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.queries.ComplexQueryResult;

public class TreasuryRateSingleQuery implements QueryInterface<String,ComplexQueryResult<BigDecimal>>{
	private static final BigDecimal HUNDRED = new BigDecimal("100");
	private final QueryInterface<String,SecDef> sdQuery;
	private final TreeMap<Integer,BigDecimal> rateTable;
	private static final String TREASURY_XML_URL = 
			"http://data.treasury.gov/feed.svc/DailyTreasuryYieldCurveRateData?$filter=month(NEW_DATE)%20eq%206%20and%20year(NEW_DATE)%20eq%202015";
	
	public TreasuryRateSingleQuery(QueryInterface<String,SecDef> sdQuery, TreeMap<Integer,BigDecimal> rateTable){
		this.sdQuery = sdQuery;
		if(rateTable==null){
			this.rateTable = rateTableFromTreasuryGov();
		}else{
			this.rateTable = rateTable;
		}		
	}
	
	public TreasuryRateSingleQuery(){
		this(new SecDefQueryAllMarkets(),rateTableFromTreasuryGov());
	}

	/**
	 * 
	 * @param sdQuery
	 * @param rateTablePathOrFileNameIfResource - if the file is in the File System : 
	 * 		rateTablePathOrFileNameIfResource is a full path and
	 * 		classOfPackage = null;
	 * @param classOfPackage if the file is NOT in the File System : 
	 * 		rateTablePathOrFileNameIfResource a fileName (no other path info) and
	 * 		classOfPackage = any class that is the same package as the rateTable.xml file;
	 */
	@SuppressWarnings("unchecked")
	public TreasuryRateSingleQuery(
			QueryInterface<String,SecDef> sdQuery, 
			String rateTablePathOrFileNameIfResource,
			Class<?> classOfPackage){
		this.sdQuery = sdQuery;
		if(rateTablePathOrFileNameIfResource!=null){
			this.rateTable = Utils.getXmlData(TreeMap.class, classOfPackage, rateTablePathOrFileNameIfResource);
		}else{
			this.rateTable = rateTableFromTreasuryGov();
		}
	}

	
	@Override
	public ComplexQueryResult<BigDecimal> get(String key, int timeoutValue, TimeUnit timeUnitType) {
		SecDef sd = sdQuery.get(key, timeoutValue, timeUnitType);
		if(sd!=null){
			// TODO making new calendar instance here, should this be passed in??
			Long longDays =  sd.getSymbolType()==SecSymbolType.STK ? 1 : 
					MarketDataComLib.getDaysToExpirationFromSd( Calendar.getInstance(),sd);
			Integer days = Integer.parseInt(longDays.toString());
			double rate = InterestRates.interpolateLinearFromRateTable(rateTable, days);
			return new ComplexQueryResult<BigDecimal>(null, new BigDecimal(rate));
		} else {
			Exception e = Utils.IllArg(this.getClass(), "Could not find SecDef for "+key);
			return new ComplexQueryResult<BigDecimal>(e, null);
		}
	}

	
	/*
       <d:NEW_DATE m:type="Edm.DateTime">2015-06-04T00:00:00</d:NEW_DATE>
        <d:BC_1MONTH m:type="Edm.Double">0.02</d:BC_1MONTH>
        <d:BC_3MONTH m:type="Edm.Double">0.02</d:BC_3MONTH>
        <d:BC_6MONTH m:type="Edm.Double">0.08</d:BC_6MONTH>
        <d:BC_1YEAR m:type="Edm.Double">0.27</d:BC_1YEAR>
        <d:BC_2YEAR m:type="Edm.Double">0.66</d:BC_2YEAR>
        <d:BC_3YEAR m:type="Edm.Double">1.04</d:BC_3YEAR>
        <d:BC_5YEAR m:type="Edm.Double">1.65</d:BC_5YEAR>
        <d:BC_7YEAR m:type="Edm.Double">2.05</d:BC_7YEAR>
        <d:BC_10YEAR m:type="Edm.Double">2.31</d:BC_10YEAR>
        <d:BC_20YEAR m:type="Edm.Double">2.78</d:BC_20YEAR>
        <d:BC_30YEAR m:type="Edm.Double">3.03</d:BC_30YEAR>
  
	 * 
	 */
	private static final TreeMap<Integer,BigDecimal> rateTableFromTreasuryGov(){
		// get xml as csv
		List<String[]> pseudoCsv = 
				Utils.getCSVData(TREASURY_XML_URL);
		// combine lines
		String allLines = "";
		for(String[] line:pseudoCsv){
			for(String column : line){
				allLines += column;
			}
		}
		String TREASURY_DATA_LINE_REGEX = 
				"<d:BC_[0-9]{1,2}((MONTH)|(YEAR)) m:type=\"Edm.Double\">[0-9]{1,3}\\.[0-9]{0,2}</d:BC_";
		String MONTH_YEAR_REGEX = "<d:BC_[0-9]{1,2}((MONTH)|(YEAR))";
		String TIME_REGEX = "[0-9]{1,2}";
		String MONTH_OR_YEAR_REGEX = "((MONTH)|(YEAR))";
		String RATE_REGEX = "[0-9]{1,3}\\.[0-9]{0,2}";
		double MONTH_MULTIPLIER = 31;
		double YEAR_MULTIPLIER = 365;
		List<String> rateLines = RegexMethods.getRegexMatches(
				TREASURY_DATA_LINE_REGEX, allLines);
		List<BigDecimal[]> expiries = new ArrayList<BigDecimal[]>();
		for(String rateLine:rateLines){
			List<String> monthYearStrings = RegexMethods.getRegexMatches(
					MONTH_YEAR_REGEX, rateLine);
			String ratePhrase = monthYearStrings.get(0);			
			String timeAmt = RegexMethods.getRegexMatches(
					TIME_REGEX, ratePhrase).get(0);
			double time = new Double(timeAmt);
			String monthOrYear = RegexMethods.getRegexMatches(
					MONTH_OR_YEAR_REGEX, ratePhrase).get(0);
			if(monthOrYear.compareTo("YEAR")==0){
				time = time*YEAR_MULTIPLIER;
			}else{
				time = time*MONTH_MULTIPLIER;
			}
			String rateString = RegexMethods.getRegexMatches(
					RATE_REGEX, rateLine).get(0);
			BigDecimal bdTime = new BigDecimal(time);
			BigDecimal bdRate = new BigDecimal(rateString);
			BigDecimal[] expiryArray = {bdTime,bdRate};
			expiries.add(expiryArray);
		}
		
		TreeMap<Integer,BigDecimal> ret = new TreeMap<Integer, BigDecimal>();
		for(BigDecimal[] timeRate : expiries){
			Integer time = new Integer(timeRate[0].toString());
			BigDecimal rate = timeRate[1].divide(HUNDRED);
			ret.put(time,rate);
		}
		return ret;
		
	}
	
	public static void main(String[] args) {
		TreasuryRateSingleQuery trsQuery = 
				new TreasuryRateSingleQuery(new SecDefQueryAllMarkets(), null);
		CollectionsStaticMethods.prtMapItems(trsQuery.rateTable);
	}
}
