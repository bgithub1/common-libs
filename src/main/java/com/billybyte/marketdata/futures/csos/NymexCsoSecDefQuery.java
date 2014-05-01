package com.billybyte.marketdata.futures.csos;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;
import com.billybyte.queries.QueryFromRegexPattern;

public class NymexCsoSecDefQuery implements QueryInterface<String, ComplexQueryResult<SecDef[]>> {
	
//TODO - must take out hard coded values in for partial shortname and product offset	
	/**
	 * 
	 * @param sdQuery
	 * @param patternToUnderlyingQuery - pattern
	 * @param productToMonthSpanMap
	 */
	public NymexCsoSecDefQuery(QueryInterface<String, SecDef> sdQuery) {
		super();
		this.sdQuery = sdQuery;
		productToMonthSpanMap = new HashMap<String, Integer>();
		for(String[] csoProdPair:csoProdNameOFfsetPairs){
			productToMonthSpanMap.put(csoProdPair[0], new Integer(csoProdPair[1]));
		}
//		this.patternToUnderlyingQuery = patternToUnderlyingQuery;
//		this.productToMonthSpanMap = productToMonthSpanMap;
	}

	private final QueryInterface<String, SecDef> sdQuery;
	// THE FOLLOWING 3 String Arrays have to be populated for each CSO family 
	//    (like G-series fo Nat gas CSO, or the 7-series for Crude OTC cso or the 
	//    the W-series for Crude Reg CSO)
	//  First - the csoProdNames array
	private final String[][] csoProdNameOFfsetPairs = new String[][]{
			{"G2","2"},{"G3","3"},{"G3B","3"},{"G4","1"},{"G4X","1"},{"G5","5"},{"G6","6"},{"G6B","6"},{"G7","12"},
			{"7A","1"},{"7B","2"},{"7C","3"},{"7M","6"},{"7Z","12"},{"WA","1"},{"WB","2"},{"WC","3"},{"WM","6"},{"WZ","12"},}
	;
//	private final String[] csoProdNames = {"G2","G3","G3B","G4","G4X","G5","G6","G6B","G7",
//			"7A","7B","7C","7M","7Z","WA","WB","WC","WM","WZ"};
//	private final Integer[] csoOffsets = new Integer[]{2,3,3,1,1,5,6,6,12,1,2,3,6,12,1,2,3,6,12};
	private final Map<String,Integer> productToMonthSpanMap ;//= CollectionsStaticMethods.mapInitFromArray(csoProdNames, csoOffsets);
	
	// Second the csoPatterns array
	private final String[] csoPatterns =
					{"(G2|G3|G3B|G4|G4X|G5|G6|G6B|G7)\\.FOP\\.NYMEX\\.USD",
					"(7A|7B|7C|7M|7Z)\\.FOP\\.NYMEX\\.USD",
					"(WA|WB|WC|WM|WZ)\\.FOP\\.NYMEX\\.USD",
					};
	private final String[] correspondingUnderlyingPartial = {
			"NG.FUT.NYMEX.USD.","CL.FUT.NYMEX.USD.","CL.FUT.NYMEX.USD."};

	private final QueryFromRegexPattern<String, String> patternToUnderlyingQuery = 
			new QueryFromRegexPattern<String, String>(csoPatterns,correspondingUnderlyingPartial);
	
	private final DecimalFormat dfYear = new DecimalFormat("0000");
	private final DecimalFormat dfMonth = new DecimalFormat("00");
	
	@Override
	public ComplexQueryResult<SecDef[]> get(String key, int timeoutValue,
			TimeUnit timeUnitType) {
		SecDef sd = sdQuery.get(key, timeoutValue, timeUnitType);
		if(sd==null){
			Exception e = Utils.IllState(this.getClass(),"Can't find secDef for :"+key);
			return new ComplexQueryResult<SecDef[]>(e, null);
		}
		String partialShortName = patternToUnderlyingQuery.get(key,timeoutValue,timeUnitType);
		if(partialShortName==null){
			Exception e = Utils.IllState(this.getClass(),"Can't find partial shortName for :"+key);
			return new ComplexQueryResult<SecDef[]>(e, null);
		}
		int year = sd.getContractYear();
		String yearString = dfYear.format(year);
		int month = sd.getContractMonth();
		String monthString = dfMonth.format(month);
		String leg0ShortName = partialShortName+yearString+monthString;
		SecDef sdLeg0 = sdQuery.get(leg0ShortName, timeoutValue, timeUnitType);
		if(sdLeg0==null){
			Exception e = Utils.IllState(this.getClass(),"Can't find secDef for :"+leg0ShortName);
			return new ComplexQueryResult<SecDef[]>(e, null);
		}
		month = sdLeg0.getContractMonth()-1;
		year = sdLeg0.getContractYear();
		Calendar c = Calendar.getInstance();
		c.set(year, month,1);
		String prod = sd.getExchangeSymbol();
		int monthOffset = this.productToMonthSpanMap.get(prod);		
		Calendar nextC = Dates.addToCalendar(c, monthOffset, Calendar.MONTH, true);
		month = nextC.get(Calendar.MONTH)+1;
		monthString = dfMonth.format(month);
		year = nextC.get(Calendar.YEAR);
		yearString = dfYear.format(year);
		String leg1ShortName =  partialShortName+yearString+monthString;
		SecDef sdLeg1 = sdQuery.get(leg1ShortName, timeoutValue, timeUnitType);
		if(sdLeg1==null){
			Exception e = Utils.IllState(this.getClass(),"Can't find secDef for :"+leg1ShortName);
			return new ComplexQueryResult<SecDef[]>(e, null);
		}
		ComplexQueryResult<SecDef[]> ret = 
				new ComplexQueryResult<SecDef[]>(null, new SecDef[]{sdLeg0,sdLeg1});
		return ret;
	}
	
	public SecDef getActualSecDef(String key, int timeoutValue,
			TimeUnit timeUnitType) {
		return sdQuery.get(key, timeoutValue, timeUnitType);
	}
}
