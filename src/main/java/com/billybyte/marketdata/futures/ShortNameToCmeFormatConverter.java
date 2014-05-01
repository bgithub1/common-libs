package com.billybyte.marketdata.futures;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
//import com.billybyte.marketdata.futures.swaps.SwapProduct;
/**
 * convert CME NYMEX formatted shortName to SYM.TYPE.EXH.CURR.YYYYMM.CorP.STRIKE
 * 	like CLF1 = CL.FUT.NYMEX.USD.201101
 * 	or   CLF1 P9500 = CL.FOP.NYMEX.USD.201101.P.95.00
 * @author bperlman1
 *
 */
public class ShortNameToCmeFormatConverter {
	 private final int sizeOfYearField;

	 private final String MONTH_CODE_PATTERN = "(F|G|H|J|K|M|N|Q|U|V|X|Z)";
	 private final String[] MONTH_CODES_REGULAR =FuturesCodes.monthCodes;// {"F","G","H","J","K","M","N","Q","U","V","X","Z"};
	 private final Pattern CME_SHORTNAME_REGEX_PATTERN;// = Pattern.compile("([A-Z0-9]{1,})(F|G|H|J|K|M|N|Q|U|V|X|Z)([0-9]{2,2})");
	 private final DecimalFormat MONTH_FORMAT = new DecimalFormat("00");
	 private final DecimalFormat YEAR_FORMAT = new DecimalFormat("0000");
	 private final String DEFAULT_SHORTNAME_SEPARATOR=".";


	/**
	 * 
	 * @param sizeOfYearField number of digits for year field (normally either 1 or2 for CLV1 or CLV11)
	 */
	  
	  
	public ShortNameToCmeFormatConverter(int sizeOfYearField){
		this.sizeOfYearField = sizeOfYearField;
		CME_SHORTNAME_REGEX_PATTERN = 
				Pattern.compile(
							"("+
							MONTH_CODE_PATTERN+
							"([0-9]{"+
							sizeOfYearField+
							","+
							sizeOfYearField+
							"}))");
		
	}
		
	static private  Map<String, String[]> conversions;
	
	static private final Map<String,String[]> getConversions(){
		if(conversions!=null)return conversions;
		List<String[]> csvData = Utils.getCSVData("SymbolConversions.csv");
		conversions = new  HashMap<String, String[]>();
		for(String[] line:csvData){
			String symKey = line[0];
			conversions.put(symKey, line);
		}
		return conversions;
	}
	
	
	 final  String createShortName(
			String prodMoncodYearFutShortNameFormat, 
			String callPut, 
			String strikeString){

		
		List<String> futuresPartList = RegexMethods.getRegexMatches(CME_SHORTNAME_REGEX_PATTERN, prodMoncodYearFutShortNameFormat);
		if(futuresPartList==null || futuresPartList.size()<1){
			return null;
		}
		// get futures part of shortName
		String futuresPart = futuresPartList.get(0);
		// extract the symbol
		String symbol = futuresPart.substring(0,futuresPart.length()-2);
		String[] conversionArray = getConversions().get(symbol);
		if(conversionArray==null)return null;
		String finalSymbol= conversionArray[1];
		if(finalSymbol==null)return null;

		DecimalFormat df = new DecimalFormat(conversionArray[2]);
		String type = "FUT";
		String cp = null;
		String str = null;
		if(callPut!=null && callPut.toUpperCase().compareTo("F")!=0){
			// it's an option
			type = "FOP";
			cp = callPut;
			double d = new Double(strikeString);
			str = df.format(d);
		}

		String monthCode = futuresPart.substring(futuresPart.length()-3, futuresPart.length()-2);
		int year = 2000 + new Integer(futuresPart.substring(futuresPart.length()-2, futuresPart.length()));
		int month=getMonthNumFromMonthCode(monthCode);
		String yearString = YEAR_FORMAT.format(year);
		String monthString = MONTH_FORMAT.format(month);
		
		String exch = conversionArray[3];
		String curr = "USD";
		String ret= finalSymbol+"."+type+"."+exch+"."+curr+"."+ yearString+monthString;
		ret = ret+cp==null? "" : "." + cp + "." + str;
		return ret;
	}
	
	/**
	 * get month number (1 - 12) for a month code like F or G or Z
	 */
	private  int getMonthNumFromMonthCode(String monthCode){
		for(int i = 0;i<MONTH_CODES_REGULAR.length;i++){
			if(MONTH_CODES_REGULAR[i].compareTo(monthCode)==0){
				return i+1;
			}
		}
		return -1;
	}
	
	

}
