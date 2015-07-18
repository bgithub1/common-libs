package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;


import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Rounding;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.QueryManager;

import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefSimple;
import com.billybyte.marketdata.ShortNameInfo;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.marketdata.futures.FuturesCodes;
import com.billybyte.marketdata.futures.FuturesProduct;
import com.billybyte.marketdata.futures.FuturesProductQuery;
import com.billybyte.marketdata.futures.MonthYear;
import com.billybyte.mongo.MongoCollectionWrapper;
import com.billybyte.mongo.MongoDatabaseNames;
import com.billybyte.mongo.MongoWrapper;

import com.billybyte.queries.ComplexQueryResult;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

public class MarketDataComLib {
	public static final String[] MONTH_CODES_REGULAR = {"F","G","H","J","K","M","N","Q","U","V","X","Z"};
	public static final Pattern MONTHYEAR_REGEX_PATTERN = Pattern.compile("((F|G|H|J|K|M|N|Q|U|V|X|Z)([0-9]{2,2}))");
	public static final Pattern CME_SHORTNAME_REGEX_PATTERN = Pattern.compile("([A-Z0-9]{1,})(F|G|H|J|K|M|N|Q|U|V|X|Z)([0-9]{2,2})");
	public static final Pattern CME_SHORTNAME_REGEX_PATTERN_1DIGIT_YEAR = Pattern.compile("([A-Z0-9]{1,})(F|G|H|J|K|M|N|Q|U|V|X|Z)([0-9]{1,1})");
	public static final DecimalFormat MONTH_FORMAT = new DecimalFormat("00");
	public static final DecimalFormat YEAR_FORMAT = new DecimalFormat("0000");
	public static final DecimalFormat YEAR_2DIGIT_FORMAT = new DecimalFormat("00");
	public static final String DEFAULT_SHORTNAME_SEPARATOR=".";
	public static final String DEFAULT_CORRELATION_PAIR_SEPARATOR="__";
	public static final String DEFAULT_EXCH_IN_SHORTNAME_SEPARATOR="_";
	

	
	private static final MarketDataComLib instanceForExceptions = new MarketDataComLib();
	private static final int DEFAULT_TIMEOUT_VALUE=1000;
	private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.MILLISECONDS;

	private static final AtomicReference<Map<String, String>> atomicUnderlyingCache = 
			new AtomicReference<Map<String,String>>(new HashMap<String,String>());

	public static String getUnderlyingShortNameFromSpan(String key){
		int cacheSize = atomicUnderlyingCache.get().size();
		// get everything if nothing in cache
		if(cacheSize==0){
			MongoCollectionWrapper mwc = null;
			try {
				mwc =
						new MongoCollectionWrapper(
								"127.0.0.1",27022, 
								UnderlyingInfo.getDbName(UnderlyingInfo.class), 
								UnderlyingInfo.getCollectionName(UnderlyingInfo.class));
			} catch (Exception e) {
				Utils.prtObMess(MarketDataComLib.class, "NON-FATAL: Span Mongo Server not running");
				e.printStackTrace();
				Utils.prtObMess(MarketDataComLib.class, "END NON-FATAL: Span Mongo Server not running");
				// put one dummy item in cache to stop looping thru this code
				atomicUnderlyingCache.get().put("dummyBcNoSpanRunning", "dummyBcNoSpanRunning");
				return null;
			}

			// get everything the first time through
			DBObject search = new BasicDBObject();
			List<UnderlyingInfo> uiList = mwc.getList(UnderlyingInfo.class, search);
			if(uiList.size()<1)return null;
			for(UnderlyingInfo ui : uiList){
				atomicUnderlyingCache.get().put(ui.get_id(), ui.getUnderlyingShorName());
			}
		}
		
		
		return atomicUnderlyingCache.get().get(key);
	}
	

	/**
	 * get month number (1 - 12) for a month code like F or G or Z
	 */
	public static int getMonthNumFromMonthCode(String monthCode){
		for(int i = 0;i<MONTH_CODES_REGULAR.length;i++){
			if(MONTH_CODES_REGULAR[i].compareTo(monthCode)==0){
				return i+1;
			}
		}
		return -1;
	}
	
	public static String getYyyyMmFromYearAndMonth(int year,int month){
		DecimalFormat dcfYear = new DecimalFormat("0000");		
		DecimalFormat dcfMonth = new DecimalFormat("00");
		return dcfYear.format(year)+dcfMonth.format(month);
	}
	

	public static String getFuturesShortNameFromCmeFormat(
			String cmeShortName,
			FuturesProductQuery fpq){
		List<String> futuresPartList = RegexMethods.getRegexMatches(CME_SHORTNAME_REGEX_PATTERN, cmeShortName);
		if(futuresPartList==null || futuresPartList.size()<1){
			return null;
		}
		String futuresPart = futuresPartList.get(0);
		String symbol = futuresPart.substring(0,futuresPart.length()-3);
		FuturesProduct fp = fpq.get(symbol, 1, TimeUnit.SECONDS);
		if(fp==null)return null;
		SecExchange exchange = fp.getExchange();
		SecCurrency currency = fp.getCurrency();
		String monthCode = futuresPart.substring(futuresPart.length()-3, futuresPart.length()-2);
		int year = 2000 + new Integer(futuresPart.substring(futuresPart.length()-2, futuresPart.length()));
		int month=getMonthNumFromMonthCode(monthCode);
		// create shortName for futures or options
		ShortNameInfo sni = new ShortNameInfo(symbol,
				SecSymbolType.FUT ,exchange,currency,year,month,null,	
				null,null);
		return sni.getShortName();							
		
	}
	
	/**
	 * 
	 * @param cmeShortName - like NGH12
	 * @param exchange - like SecExchange.NYMEX
	 * @param currency - like SecCurrency.USD
	 * @param putCall - either P or C
	 * @param strike - BigDecimal number representing strike
	 * @return - return a valid shortName like NG.FUT.NYMEX.USD.201112, or NG.FOP.NYMEX.USD.201112.C.4.300
	 */
	public static ShortNameInfo getShortNameFromCmeFormat(
			String cmeShortName,
			SecExchange exchange,
			SecCurrency currency,
			String putCall,
			BigDecimal strike){
		List<String> futuresPartList = RegexMethods.getRegexMatches(CME_SHORTNAME_REGEX_PATTERN, cmeShortName);
		if(futuresPartList==null || futuresPartList.size()<1){
			return null;
		}
		String futuresPart = futuresPartList.get(0);
		String monthCode = futuresPart.substring(futuresPart.length()-3, futuresPart.length()-2);
		int year = 2000 + new Integer(futuresPart.substring(futuresPart.length()-2, futuresPart.length()));
		int month=getMonthNumFromMonthCode(monthCode);
		String symbol = futuresPart.substring(0,futuresPart.length()-3);
		// create shortName for futures or options
		ShortNameInfo sni = new ShortNameInfo(
				symbol,putCall==null ? SecSymbolType.FUT : SecSymbolType.FOP,exchange,currency,	year,month,	
				null,putCall,strike);
		return sni;							
		
	}
	
	
	public static String getCmeShortName2DigitYearFromSecDef(SecDef sd){
		if(sd.getSymbolType().compareTo(SecSymbolType.FUT)==0 ||
				sd.getSymbolType().compareTo(SecSymbolType.FOP)==0){
			return sd.getSymbol()+FuturesCodes.monthCodes[sd.getContractMonth()-1]+
					YEAR_2DIGIT_FORMAT.format(sd.getContractYear()-2000);
		}else{
			return null;
		}
	}

		
	/**
	 * 
	 * @param cmeShortName - like NGH2
	 * @param exchange - like SecExchange.NYMEX
	 * @param currency - like SecCurrency.USD
	 * @param putCall - either P or C
	 * @param strike - BigDecimal number representing strike
	 * @return - return a valid shortName like NG.FUT.NYMEX.USD.201112, or NG.FOP.NYMEX.USD.201112.C.4.300
	 */
	public static ShortNameInfo getShortNameFromCmeFormat1DigitYear(
			String cmeShortName,
			SecExchange exchange,
			SecCurrency currency,
			String putCall,
			BigDecimal strike,
			int thisYear){
		// insert decade into cmeShortName, and call getShortNameFromCmeFormat
		int year =   new Integer(cmeShortName.substring(cmeShortName.length()-1, cmeShortName.length()));
		// what decade is it?
		int yearOfDecade = thisYear % ((thisYear/10)*10);
		//=(INT(E14/10)*10 - INT(E14/100)*100)/10
		int decade = ((thisYear/10)*10 - (thisYear/100)*100)/10;
		if(yearOfDecade>year){
			decade = decade+1;
		}
		String shortName2charYear = cmeShortName.substring(0,cmeShortName.length()-1)+(decade % 10)+year;
		return getShortNameFromCmeFormat(shortName2charYear,exchange,currency,putCall,strike);
	}
	
	
	public static ShortNameInfo getShortNameFromMmmYyFormat(
			String symbol,
			String MmmYyFormat,
			SecExchange exchange,
			SecCurrency currency,
			String putCall,
			BigDecimal strike){
		
		// first get MonthYear
		if(MmmYyFormat==null)return null;
		MonthYear my=null;
		try {
			my = new MonthYear(MmmYyFormat);
		} catch (Exception e) {
			return null;
		}
		int month = my.getMonth().getIndex();
		int year = new Integer(my.getYear());
		ShortNameInfo sni = new ShortNameInfo(
				symbol,putCall==null ? SecSymbolType.FUT : SecSymbolType.FOP,exchange,currency,	year,month,	
				null,putCall,strike);
		return sni;							
		
		
		
	}
	
	public static final Tuple<String[],ShortNameInfo> getShortNameInfoFromCmeSymbol(String exchSym,FuturesProductQuery fpq,
			QueryInterface<String, SecDef>sdQuery){
		String[] reasons=null;
		ShortNameInfo sni=null;
		String[] symParts = exchSym.split(" ");
		
		String symbol = symParts[0].substring(0,symParts[0].length()-(3));
		FuturesProduct fp = fpq.get(symbol, 1, TimeUnit.SECONDS);
		if(fp==null){
			reasons = new String[]{ "Can't find FuturesProduct for "+exchSym};
			Tuple<String[],ShortNameInfo> ret = new  Tuple<String[], ShortNameInfo>(reasons, sni);
			return ret;
		}
		SecExchange exchange = fp.getExchange();
		SecCurrency currency = fp.getCurrency();
		
		// get month year
		String myString = symParts[0].substring(symParts[0].length()-(3),symParts[0].length());
		String monthString = myString.substring(0,1);
		int contractMonth = getMonthNumFromMonthCode(monthString);
		int contractYear = new Integer(myString.substring(1,3))+2000;
		SecSymbolType symbolType = null;
		// see if it's a valud options symbol
		if(fp.getValidTypes().contains(SecSymbolType.FOP) && symParts.length==2){
			symbolType = SecSymbolType.FOP;
		}else if(fp.getValidTypes().contains(SecSymbolType.FUT) && symParts.length==1){
			symbolType = SecSymbolType.FUT;
		}
		if(symbolType==null){
			reasons = new String[]{"Can get symbolType for "+exchSym};
			Tuple<String[],ShortNameInfo> ret = new  Tuple<String[], ShortNameInfo>(reasons, sni);
			return ret;
		}
		String right = null;
		BigDecimal strike = null;
		if(symbolType==SecSymbolType.FOP){
			String futSym = fp.getUnderlyingSymbol();
			if(futSym==null){
				reasons = new String[]{"Can get futures Symbol in FuturesProduct for "+exchSym};
				Tuple<String[],ShortNameInfo> ret = new  Tuple<String[], ShortNameInfo>(reasons, sni);
				return ret;
			}
			try {
				ShortNameInfo futSni = 
						new ShortNameInfo(futSym, SecSymbolType.FUT, exchange, 
								currency, contractYear, contractMonth ,null, null, null);
				SecDef futSd = sdQuery.get(futSni.getShortName(),10,TimeUnit.SECONDS);
				if(futSd ==null){
					reasons = new String[]{"Can get futures SecDef for "+exchSym};
					Tuple<String[],ShortNameInfo> ret = new  Tuple<String[], ShortNameInfo>(reasons, sni);
					return ret;
				}
				int futPrec = futSd.getExchangePrecision();
				strike = new BigDecimal(symParts[1].substring(0,symParts[1].length()-1));
				strike = strike.divide(BigDecimal.TEN.pow(futPrec)).setScale(futPrec,RoundingMode.HALF_EVEN);
			} catch (Exception e) {
				reasons = new String[]{"Can get futures shortName for "+exchSym+" exception Cause = "+e.getCause().getMessage()};
				Tuple<String[],ShortNameInfo> ret = new  Tuple<String[], ShortNameInfo>(reasons, sni);
				return ret;
			}
			// parse out right and symbol
			right = symParts[1].substring(symParts[1].length()-1,symParts[1].length());
			//TODO !!!!!!!!!!!DEBUG!!!!!!!! 2012 06 23
		}
		sni = new ShortNameInfo(symbol, symbolType, exchange, currency, contractYear, contractMonth, 
				null,right, strike);
		reasons = new String[]{};
		Tuple<String[],ShortNameInfo> ret = new  Tuple<String[], ShortNameInfo>(reasons, sni);
		return ret;
	}

	public static String getSymTypeExch(SecDef sd){
		return sd.getSymbol()+DEFAULT_SHORTNAME_SEPARATOR+
				sd.getSymbolType().toString()+DEFAULT_SHORTNAME_SEPARATOR+
				sd.getExchange().toString();
	}
	
	public static List<SecDef> getAllOutRightShortNamesInclusive(QueryInterface<String, SecDef> outRightQuery,
			SecDef begContract, SecDef endContract, int timeoutValue, TimeUnit timeUnitType) {
		SecDef sdCurrent = begContract;
		SecDef sdLast = endContract;
		// get all legs in between by incrementing the contract month of each leg
		int lastMonthYear = sdLast.getContractYear() * 100
				+ sdLast.getContractMonth();
		int currMonthYear;
		int nextYear;
		int nextMonth;
		
		nextYear = sdCurrent.getContractYear();
		nextMonth = sdCurrent.getContractMonth() ;
		String symbol = sdCurrent.getSymbol();
		SecSymbolType type = sdCurrent.getSymbolType();
		SecExchange exch = sdCurrent.getExchange();
		SecCurrency currency = sdCurrent.getCurrency();
		String right = sdCurrent.getRight();
		BigDecimal strike = sdCurrent.getStrike();
		
		
		ArrayList<SecDef> secDefList = new ArrayList<SecDef>();
		do {
			if(sdCurrent!=null){
				secDefList.add(sdCurrent);
			}
			nextMonth = nextMonth + 1;
			if (nextMonth > 12) {
				nextMonth = 1;
				nextYear += 1;
			}
			currMonthYear = nextYear*100+nextMonth;
			ShortNameInfo sni = new ShortNameInfo(symbol,type,exch,currency,nextYear,nextMonth,
					null,right,strike);
					
			sdCurrent=outRightQuery.get(sni.getShortName(), timeoutValue, timeUnitType);
			
		} while (currMonthYear <= lastMonthYear);
		return secDefList;
	}
	
	
	/**
	 * return the underlying secdef  If necessary use the FuturesOptionsSymbolPairQuery
	 *   to get the symbol that corresponds to the option symbol
	 * @param optionSd - option SecDef (currently supports SecSymbolType.OPT and FOP
	 * @param timeoutValue
	 * @param timeUnitType
	 * @return for OPT - return the STK SecDef with same symbol.  
	 * 			For FOP return the FUT SecDef with same symbol and contractMonth and Year
	 */
	public static SecDef getUnderylingSecDefFromOptionSecDef(
			SecDef optionSd,
			QueryInterface<String, SecDef> secDefQuery,
			FuturesProductQuery fpq, Calendar evalDate,
			int timeoutValue, TimeUnit  timeUnitType){
		String underFromSpan = 
				getUnderlyingShortNameFromSpan(optionSd.getShortName());
		if(underFromSpan!=null){
			return secDefQuery.get(underFromSpan, timeoutValue, timeUnitType);
		}
		SecSymbolType retType =null;
		//TODO - THIS IS A HACK
		if(optionSd.getSymbolType()==SecSymbolType.OPT){
			retType = SecSymbolType.STK;
		}else if(optionSd.getSymbolType()==SecSymbolType.FOP){
			retType = SecSymbolType.FUT;
		}else if(optionSd.getSymbolType()==SecSymbolType.FUT || 
				optionSd.getSymbolType()==SecSymbolType.STK){
			return optionSd;
		}
		String sym=null;
		sym = optionSd.getSymbol();
		if(optionSd.getSymbolType()==SecSymbolType.FOP){
			FuturesProduct fp = fpq.get(optionSd.getSymbol(), timeoutValue, timeUnitType);
			sym = fp.getUnderlyingSymbol();
		}
		SecExchange exc = optionSd.getExchange();
		SecCurrency curr = optionSd.getCurrency();
		int contractYear = optionSd.getContractYear();
		int contractMonth = optionSd.getContractMonth();
		// assemble a shortName, and get it's SecDef from the GlobalQuery
		if(contractYear>0){
			if(contractMonth<=0){
				return null;
			}
		}
		ShortNameInfo sni = 
			new ShortNameInfo(sym,retType,exc,curr,contractYear,contractMonth,null,null,null);
		String shortNameUnderlying = sni.getShortName();
		// try to get sec def
		SecDef ret = secDefQuery.get(shortNameUnderlying, timeoutValue, timeUnitType);
		// now see if the symbol is a special case of some sort
		// TODO - this is a hack!!!!!
		if(optionSd.getSymbol().compareTo("KD")==0){
			// replace the monthyear
			String symbol  = ret.getSymbol();
			SecSymbolType type = ret.getSymbolType();
			SecExchange exchange = ret.getExchange();
			SecCurrency currency = ret.getCurrency();
			String right = ret.getRight();
			BigDecimal strike = ret.getStrike();
			
			SecDef spotSd = getSpotContractPerBusinessDay(secDefQuery, symbol, type, exchange, currency, right, strike, evalDate);
			return spotSd;
		}else{
			return ret;
		}

	}

	
	public static List<SecDef> getAllOutRightShortNamesInclusive(QueryInterface<String, SecDef> outRightQuery,
			String shortNameOfWideSpread, int timeoutValue, TimeUnit timeUnitType) {
		String[] legs = shortNameOfWideSpread.split("[-:]");
		if (legs.length < 2) {
			return null;
		}
		SecDef sdFirst = outRightQuery.get(legs[0], timeoutValue,
				timeUnitType);
		SecDef sdLast = outRightQuery.get(legs[1], timeoutValue,
				timeUnitType);
		return getAllOutRightShortNamesInclusive(outRightQuery,sdFirst,sdLast,timeoutValue,timeUnitType);
	}

	public static List<SecDef> getAllOutRightShortNamesInclusive(
			QueryInterface<String,SecDef> outrightSecDefQueryEngine,
			String shortNameOfWideSpread){
		// 
		return getAllOutRightShortNamesInclusive(outrightSecDefQueryEngine,shortNameOfWideSpread,100,TimeUnit.MILLISECONDS);
	}
	
	/**
	 * 
	 * @param productName
	 * @param businessDay
	 * @return SecurityDefinition
		// get the year and month of the businessDate to create a MonthYear object for
		//  the MonthYear that immediately proceeds the month of this businessDate.  The spot commodity contract for
		//   any commodity always has a month year that is the same as or greater than the month and year of the business date.
		//  So, if the business date is 12/15/2009, then the spot contract for any commodity is not going to be X9.  It will
		 * be Z9 or greater. 
		 * We will start searching at the current monthyear. 
	 * 
	 */
	@Deprecated
	public static SecDef getSpotContractPerBusinessDay_old(QueryInterface<String,SecDef> secDefQueryEngine,
			String symbol,SecSymbolType type,SecExchange exchange,SecCurrency currency,
			String right,BigDecimal strike, Calendar businessDay){
		int year = businessDay.get(Calendar.YEAR);
		int firstSearhContractYear = year;
		int lastSearhContractYear = year+1;
		int month = businessDay.get(Calendar.MONTH)+1;
		
		ShortNameInfo sniFirst = new ShortNameInfo(symbol,type,exchange,currency,firstSearhContractYear,month,
				null,right,strike);
		SecDef sdFirst = 
			secDefQueryEngine.get(sniFirst.getShortName(),DEFAULT_TIMEOUT_VALUE,DEFAULT_TIMEUNIT);
		ShortNameInfo sniLast = new ShortNameInfo(symbol,type,exchange,currency,lastSearhContractYear,month,
				null,right,strike);
		SecDef sdLast = 
			secDefQueryEngine.get(sniLast.getShortName(),DEFAULT_TIMEOUT_VALUE,DEFAULT_TIMEUNIT);

		
		List<SecDef> sds =null;
		try {
			sds = getAllOutRightShortNamesInclusive(secDefQueryEngine,sdFirst,sdLast,DEFAULT_TIMEOUT_VALUE,DEFAULT_TIMEUNIT);
		} catch (Exception e) {
			throw Utils.IllState(instanceForExceptions, e.getMessage());
		}
		if(sds==null){
			throw Utils.IllState(instanceForExceptions, " getSpotContractPerBusinessDay error on symbol:"+symbol+" exchange: "+exchange+" businessDay: "+businessDay.getTime().toString());
		}
		SecDef current = null;
		for(SecDef sd : sds){
			Calendar c = Calendar.getInstance();
			c.set(sd.getExpiryYear(),sd.getExpiryMonth()-1,sd.getExpiryDay());
			if(c.compareTo(businessDay)>=0){
				current = sd;
				break;
			}
		}
		return current;
		
	}
	
    
    
    
	/**
	* 
	* @param productName
	* @param businessDay
	* @return SecurityDefinition
	// get the year and month of the businessDate to create a MonthYear object for
	//  the MonthYear that immediately proceeds the month of this businessDate.  The spot commodity contract for
	//   any commodity always has a month year that is the same as or greater than the month and year of the business date.
	//  So, if the business date is 12/15/2009, then the spot contract for any commodity is not going to be X9.  It will
	* be Z9 or greater. 
	* We will start searching at the current monthyear. 
	* 
	*/
	public static SecDef getSpotContractPerBusinessDay(QueryInterface<String,SecDef> secDefQueryEngine,
	String symbol,SecSymbolType type,SecExchange exchange,SecCurrency currency,
	String right,BigDecimal strike, Calendar businessDay){
		int year = businessDay.get(Calendar.YEAR);
		int firstSearhContractYear = year;
		int month = businessDay.get(Calendar.MONTH)+1;
		Calendar lastCalendar = Dates.addBusinessDays("US", businessDay, 100);
		int lastSearchContractYear = lastCalendar.get(Calendar.YEAR);
		int lastMonth = lastCalendar.get(Calendar.MONTH)+1;
		
		ShortNameInfo sniFirst = new ShortNameInfo(symbol,type,exchange,currency,firstSearhContractYear,month,
				null,right,strike);
		SecDef sdFirst = 
		secDefQueryEngine.get(sniFirst.getShortName(),DEFAULT_TIMEOUT_VALUE,DEFAULT_TIMEUNIT);
		ShortNameInfo sniLast = new ShortNameInfo(symbol,type,exchange,currency,lastSearchContractYear,lastMonth,
				null,right,strike);
		SecDef sdLast = 
		secDefQueryEngine.get(sniLast.getShortName(),DEFAULT_TIMEOUT_VALUE,DEFAULT_TIMEUNIT);
		
		
		List<SecDef> sds =null;
		try {
			sds = getAllOutRightShortNamesInclusive(secDefQueryEngine,sdFirst,sdLast,DEFAULT_TIMEOUT_VALUE,DEFAULT_TIMEUNIT);
		} catch (Exception e) {
			throw Utils.IllState(instanceForExceptions, e.getMessage());
		}
		if(sds==null){
			throw Utils.IllState(instanceForExceptions, " getSpotContractPerBusinessDay error on symbol:"+symbol+" exchange: "+exchange+" businessDay: "+businessDay.getTime().toString());
		}
		SecDef current = null;
		for(SecDef sd : sds){
			Calendar c = Calendar.getInstance();
			c.set(sd.getExpiryYear(),sd.getExpiryMonth()-1,sd.getExpiryDay());
			if(c.compareTo(businessDay)>=0){
				current = sd;
				break;
			}
		}
		return current;
	
	}	
		
	public static List<SecDef> getFuturesListFromDateAndSymbol(QueryInterface<String,SecDef> secDefQueryEngine,
			String symbol, Calendar firstBusinessDay,Calendar lastBusinessDay){
		FuturesProductQuery fpq = new FuturesProductQuery();
		FuturesProduct fp = fpq.get(symbol, 1, TimeUnit.SECONDS);
		return getFuturesListFromDate(secDefQueryEngine,symbol,fp.getExchange(),
				fp.getCurrency(),firstBusinessDay,lastBusinessDay);
	}
	
	public static List<SecDef> getFuturesListFromDate(QueryInterface<String,SecDef> secDefQueryEngine,
			String symbol,SecExchange exchange,SecCurrency currency, Calendar firstBusinessDay,Calendar lastBusinessDay){
		if(lastBusinessDay.compareTo(firstBusinessDay)<0){
			throw Utils.IllArg(instanceForExceptions, "firstBusinessDay: "+firstBusinessDay.getTime().toString()+" is greater than lastBusinessDay: "+lastBusinessDay.getTime().toString());
		}
		
		Calendar currentCal = (Calendar)firstBusinessDay.clone();
		List<SecDef> ret = new ArrayList<SecDef>();
		do{
			SecDef currSd = getSpotContractPerBusinessDay(secDefQueryEngine,
					symbol,SecSymbolType.FUT,exchange,currency,null,null,currentCal);
			if(currSd==null){
				throw Utils.IllState(instanceForExceptions, "no spot futures contract for Calendar: "+currentCal.getTime().toString());
			}
			ret.add(currSd);
			currentCal.add(Calendar.MONTH, 1);
		}while(currentCal.compareTo(lastBusinessDay)<=0);
		return ret;
	}
	
	public static List<String[]> getAmexNames(){
		String csvFileName = Utils.createPackagePath("../CommonLibraries/src/", MarketDataComLib.class)+"AmexSymbols.csv";
		return Utils.getCSVData(csvFileName);
	}

	public static List<String[]> getNasdaqNames(){
		String csvFileName = Utils.createPackagePath("../CommonLibraries/src/", MarketDataComLib.class)+"NasdaqSymbols.csv";
		return Utils.getCSVData(csvFileName);
	}

	public static List<String[]> getOtcbbNames(){
		String csvFileName = Utils.createPackagePath("../CommonLibraries/src/", MarketDataComLib.class)+"OtcbbSymbols.csv";
		return Utils.getCSVData(csvFileName);
	}
	
	public static List<String[]> getAllEquityNames(){
		List<String[]> ret = getAmexNames();
		ret.addAll(getNasdaqNames());
		ret.addAll(getOtcbbNames());
		return ret;
	}
	

	public static Boolean isCall(SecDef optionSecDef){
		if(optionSecDef==null)return null;
		String right = optionSecDef.getRight();
		if(right==null){
			return null;
		}
		if(right.trim().compareTo(" ")<=0)return null;
		right = right.toUpperCase().substring(0);
		if(right.compareTo("C")==0){
			return true;
		}
		return false;
		
	}
	
	public static Boolean isCall(String shortName) {
		String callString = DEFAULT_SHORTNAME_SEPARATOR+"C"+DEFAULT_SHORTNAME_SEPARATOR;
		if(shortName.contains(callString)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static Boolean isOutOfMoney(SecDef optionSecDef,BigDecimal atm){
		Boolean iscall = isCall(optionSecDef);
		if(iscall==null)return null;
		BigDecimal strike = optionSecDef.getStrike();
		if(iscall){
			if(strike.compareTo(atm)>=0){
				return true;
			}else{
				return false;
			}
		}else{
			if(strike.compareTo(atm)<=0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	
	public static SecDef swapOptionRight(SecDef optionSecDef){
		Boolean iscall = isCall(optionSecDef);
		if(iscall==null)return null;
		SecDef ret;
		if(iscall){
			// make a put
			ShortNameInfo sni = new ShortNameInfo(optionSecDef.getSymbol(),
					optionSecDef.getSymbolType(),optionSecDef.getExchange(),
					optionSecDef.getCurrency(),
					optionSecDef.getContractYear(),optionSecDef.getContractMonth(),
					optionSecDef.getContractDay(),"P",
					optionSecDef.getStrike());
			
			ret = new SecDefSimple(
							sni.getShortName(),
							sni,optionSecDef.getExchangeSymbol(),optionSecDef.getExchangePrecision(),
							optionSecDef.getMinTick(),optionSecDef.getExpiryYear(),optionSecDef.getExpiryMonth(),
							optionSecDef.getExpiryDay(),optionSecDef.getMultiplier(),optionSecDef.getPrimaryExch()
						);
		}else{
			// make a call
			ShortNameInfo sni = new ShortNameInfo(optionSecDef.getSymbol(),
					optionSecDef.getSymbolType(),optionSecDef.getExchange(),
					optionSecDef.getCurrency(),
					optionSecDef.getContractYear(),optionSecDef.getContractMonth(),
					optionSecDef.getExpiryDay(),"C",
					optionSecDef.getStrike());
			
			ret = new SecDefSimple(
							sni.getShortName(),
							sni,optionSecDef.getExchangeSymbol(),optionSecDef.getExchangePrecision(),
							optionSecDef.getMinTick(),optionSecDef.getExpiryYear(),optionSecDef.getExpiryMonth(),
							optionSecDef.getExpiryDay(),optionSecDef.getMultiplier(),optionSecDef.getPrimaryExch()
						);
		}
		
		return ret;
	}
	
	/**
	 * Create a map of futures products from the csv file with those products
	 * The key is the symbol.
	 */
	public static Map<String,FuturesProduct> getFuturesProductMapFromCsvFile(){
		Map<String, FuturesProduct> ret = new ConcurrentHashMap<String, FuturesProduct>();
		List<FuturesProduct> list = getFuturesProductListFromCsvFile();
			for(FuturesProduct prod:list){
				for(String symbol:prod.getSymbols()){
					ret.put(symbol,prod);
				}
			}
		return ret;
	}
	


	private static final String EXCHANGE_PRODUCT_LIST_PATH = "ExchangeProductList.csv";
	public static List<FuturesProduct> getFuturesProductListFromCsvFile(){
		List<FuturesProduct> ret = new ArrayList<FuturesProduct>();
		List<String[]> csvData = Utils.getCSVData(FuturesProduct.class, EXCHANGE_PRODUCT_LIST_PATH);
		String[] csvHeader = csvData.get(0);

 		int symbol_col = Utils.getCsvColumnIndex("symbol", csvHeader);
 		int contractDescription_col = Utils.getCsvColumnIndex("contractDescription", csvHeader);
 		int exchange_col = Utils.getCsvColumnIndex("exchange", csvHeader);
 		int monthsAllowed_col = Utils.getCsvColumnIndex("monthsAllowed", csvHeader);
 		int ticValue_col = Utils.getCsvColumnIndex("ticValue", csvHeader);
 		int pointValue_col = Utils.getCsvColumnIndex("pointValue", csvHeader);
 		int notionalContractSize_col = Utils.getCsvColumnIndex("notionalContractSize", csvHeader);
 		int minOrderTick_col = Utils.getCsvColumnIndex("minOrderTick", csvHeader);
 		int notionalTickSize_col = Utils.getCsvColumnIndex("notionalTickSize", csvHeader);
 		int validTypes_col = Utils.getCsvColumnIndex("validTypes", csvHeader);
 		int underlyingSymbol_col = Utils.getCsvColumnIndex("underlyingSymbol", csvHeader);
		int spanSymbol_col  = Utils.getCsvColumnIndex("spanSymbol", csvHeader);
 		
		for(int i=1;i<csvData.size();i++){
			String[] line = csvData.get(i);
			  Set<String> symbols	;
			  String contractDescription;
			  SecExchange exchange	;
			  long startTimeInMills=0;
			  long endTimeInMills=0;
			  String sizeDescription = null	;
			  Set<Integer> monthsAllowed	;
			  BigDecimal ticValue	;
			  BigDecimal pointValue	;
			  BigDecimal notionalContractSize	;
			  BigDecimal minOrderTick	;
			  BigDecimal notionalTickSize;	
			  Set<SecSymbolType> validTypes;
			  String underlyingSymbol;
			  
			String[] validSymbolStrings = line[symbol_col].split(";");
			symbols = new HashSet<String>();
			for(String validSymbolString:validSymbolStrings){
				symbols.add(validSymbolString);
			}

			exchange = SecExchange.fromString(line[exchange_col]);
			validTypes = new HashSet<SecSymbolType>();
			contractDescription = line[contractDescription_col];
			for(String validType:line[validTypes_col].split(";")){
				validTypes.add(SecSymbolType.fromString(validType));
			}
			
			String monthsAllowedString = line[monthsAllowed_col];
			monthsAllowed = new  TreeSet<Integer>();
			for(int j = 0;j<monthsAllowedString.length();j++){
				String s = monthsAllowedString.substring(j,j+1);
				monthsAllowed.add(getMonthNumFromMonthCode(s));
			}
			
			notionalContractSize = new BigDecimal(line[notionalContractSize_col]);
			notionalTickSize = new BigDecimal(line[notionalTickSize_col]);
			pointValue = new BigDecimal(line[pointValue_col]);
			ticValue = notionalTickSize;
			minOrderTick = new BigDecimal(line[minOrderTick_col]);
			underlyingSymbol =  line[underlyingSymbol_col];
			
			String spanSymbol = FuturesProduct.NO_SPAN_SYMBOL;
			if(line.length>spanSymbol_col){
				spanSymbol = line[spanSymbol_col];
			}
			
			FuturesProduct prod = new FuturesProduct(
					symbols,exchange,validTypes,monthsAllowed,
					contractDescription,notionalContractSize,notionalTickSize,
					pointValue,ticValue,startTimeInMills,endTimeInMills,minOrderTick,
					sizeDescription,underlyingSymbol,SecCurrency.USD,spanSymbol);
			ret.add(prod);
			
		}
		return ret;
	}
	
	
	
	public static BigDecimal getAtmStrike(SecDef secDef,BigDecimal price, double strikeValToRoundto){
		int prec = secDef.getExchangePrecision();
		BigDecimal strike = Rounding.round_by_xs_decimal(price.doubleValue(), strikeValToRoundto, prec);
		return strike;
	}
	


	
	
	public static String getOptionsShortnameWoStrikeRight(SecDef optionSd){
		String sep = DEFAULT_SHORTNAME_SEPARATOR;
		return optionSd.getSymbol()+sep+optionSd.getSymbolType()+sep+optionSd.getExchange().toString()+sep+optionSd.getCurrency().toString()+sep+
			YEAR_FORMAT.format(optionSd.getContractYear())+MONTH_FORMAT.format(optionSd.getContractMonth());
	}
	
	
	/**
	 * 
	 * @param right - "C" or "P", depending if you want calls or puts
	 * @param sdList - list of options SecDefs for a particular underlying
	 * @return }@code TreeMap<Long, TreeMap<BigDecimal, SecDef>>} which represents all of the SecDefs for these options
	 * 			sorted by expiry, and then strike 
	 */
	public static TreeMap<Long, TreeMap<BigDecimal, SecDef>> sortSecDefsByExpiryStrike(String right,List<SecDef> sdList){
		TreeMap<Long, TreeMap<BigDecimal, SecDef>> ret = new TreeMap<Long, TreeMap<BigDecimal,SecDef>>();
		for(int i = 0;i<sdList.size();i++){
			SecDef sd = sdList.get(i);
			if(sd.getRight().compareTo(right)==0){
				updateStrikeSdTreeMap(sd,ret);
			}else{
				continue;
			}
		}
		return ret;
	}
	

	
	/**
	 * 
	 * @param sd : SecDef to add
	 * @param rightMap - a TreeMap<Long, TreeMap<BigDecimal, SecDef>> of
	 * 			options that have the same right (C or P)
	 */
	private static void updateStrikeSdTreeMap(SecDef sd,
			TreeMap<Long, TreeMap<BigDecimal, SecDef>> rightMap){
		long yyyyMmDd = sd.getExpiryYear()*10000+sd.getExpiryMonth()*100+sd.getExpiryDay();
		if(!rightMap.containsKey(yyyyMmDd)){
			TreeMap<BigDecimal, SecDef> newMap = new TreeMap<BigDecimal, SecDef>();
			rightMap.put(yyyyMmDd, newMap);
		}
		TreeMap<BigDecimal, SecDef> innerMap = rightMap.get(yyyyMmDd);
		innerMap.put(sd.getStrike(), sd);
	}

	/**
	 * Returns a ComplexQueryResult consisting of the supplied exception and a null result
	 * @param e
	 * @return
	 */
	public static <K> ComplexQueryResult<K> errorRet(Exception e){
		return new ComplexQueryResult<K>(e, null);
	}
	
	/**
	 * Returns a ComplexQueryResult consisting of the supplied exception and a null result
	 * @param e
	 * @return
	 */
	public static <K> ComplexQueryResult<K> errorRet(Exception e, K dummy){
		return new ComplexQueryResult<K>(e, null);
	}

	
	/**
	 * Returns a ComplexQueryResult consisting of the supplied String to be used as an exception message and a null result
	 * @param e
	 * @return
	 */
	public static <K> ComplexQueryResult<K> errorRet(String exceptionMessage){
		Exception e = Utils.IllState(ComplexQueryResult.class, exceptionMessage);
		return new ComplexQueryResult<K>(e, null);
	}
	
	public static BigDecimal getCallPutFromSecDef(SecDef sd){
		BigDecimal ret = BigDecimal.ZERO;
		if(sd.getRight()!=null&&sd.getRight().compareTo("P")==0){
			ret = BigDecimal.ONE;
		}
		return ret;
	}


	
	
	public static Calendar getCalendarOfExpiryFromSecDef(SecDef sd){
		int day = sd.getExpiryDay();
		int month = sd.getExpiryMonth();
		int year = sd.getExpiryYear();
		Calendar ret = Calendar.getInstance();
		ret.set(year, month-1,day);
		return ret;
	}
	
	
	public static int getBusinessDaysFromFirstToExpiry(SecDef sd){
		Calendar actualExpiry = getCalendarOfExpiryFromSecDef(sd);
		Calendar firstOfMonthActual = Calendar.getInstance();
		firstOfMonthActual.set(sd.getContractYear(), sd.getContractMonth()-1,1);
		int totalBusinessDaysInMonthTillExpiry = Dates.getAllBusinessDays(firstOfMonthActual, actualExpiry).size();
		return totalBusinessDaysInMonthTillExpiry;
	}

	/**
	 * Get a new SecDef at the provided strike based on the supplied base SecDef
	 * @param baseSd
	 * @param strike
	 * @param sdQuery
	 * @return
	 */
	public static SecDef getSecDefAtNewStrike(SecDef baseSd, BigDecimal strike, QueryInterface<String,SecDef> sdQuery){
		int prec = baseSd.getExchangePrecision();
		strike.setScale(prec, RoundingMode.HALF_EVEN);
		String newShort = baseSd.getExchangeSymbol()+"."+baseSd.getSymbolType()+"."+baseSd.getExchange()+"."+
		baseSd.getCurrency()+"."+(baseSd.getContractYear()*100+baseSd.getContractMonth())+"."+baseSd.getRight()+"."+strike;
		return sdQuery.get(newShort, 1, TimeUnit.SECONDS);
	}

	public static Double getDte(Calendar today, Calendar expiry){
		long days = Dates.getDifference(today, expiry, TimeUnit.DAYS)+1l;
		return new Double(days/365.0);
	}

	public static Double getDteFromSd(Calendar today, SecDef sd){
		if(sd == null)return null;
		Calendar c = getCalendarOfExpiryFromSecDef(sd);
		
		long days = Dates.getDifference(today, c, TimeUnit.DAYS)+1l;
		return new Double(days/365.0);
	}
	
	public static Long getDaysToExpirationFromSd(Calendar today, SecDef sd){
		if(sd == null)return null;
		Calendar c = getCalendarOfExpiryFromSecDef(sd);
		
		long days = Dates.getDifference(today, c, TimeUnit.DAYS)+1l;
		return days;
	}

	public static double[][] getCorrMatrixFromCqrMap(SecDef[] sds,
			Map<String, ComplexQueryResult<BigDecimal>> in) {
		TreeSet<String> ts = new TreeSet<String>();
		TreeSet<String> pairs = new TreeSet<String>();
		List<String> shortList = new ArrayList<String>();
		double[][] ret = new double[sds.length][sds.length];		
		for(int i = 0;i<sds.length;i++){
			ts.add(sds[i].getShortName());
			// fill diagonals
			ret[i][i] = 1.0;
			shortList.add(sds[i].getShortName());
		}
		for(Entry<String, ComplexQueryResult<BigDecimal>> entry: in.entrySet()){
			ComplexQueryResult<BigDecimal> cqr = entry.getValue();
			if(!cqr.isValidResult())return null;
			String[] parts = entry.getKey().split("__");
			if(parts.length!=2)return null;
			if(shortList.contains(parts[0])&&shortList.contains(parts[1])){
				
			}
		}
		return ret;
	}

	public TreeSet<String> getUnderlyingShortNamesFromCqrCorrMap(
			Map<String, ComplexQueryResult<BigDecimal>>cqrMap){
		TreeSet<String> ret = new TreeSet<String>();
		for(Entry<String, ComplexQueryResult<BigDecimal>> entry:cqrMap.entrySet()){
			String[] split = entry.getKey().split(DEFAULT_CORRELATION_PAIR_SEPARATOR);
			ret.add(split[0]);
			ret.add(split[1]);
		}
		return ret;
	}
	
	public static double[][] getCorrMatrixFromBigDecMap(SecDef[] sds,
			Map<String, BigDecimal> in) {
		TreeSet<String> ts = new TreeSet<String>();
		
		for(int i = 0;i<sds.length;i++){
			ts.add(sds[i].getShortName());
		}
		double[][] ret = new double[sds.length][sds.length];
		for(Entry<String, BigDecimal> entry: in.entrySet()){
			BigDecimal value = entry.getValue();
			String[] parts = entry.getKey().split("__");
			if(parts.length!=2)return null;
			int index0 = ts.headSet(parts[0]).size();
			int index1 = ts.headSet(parts[1]).size();
			ret[index0][index1] = value.doubleValue();
		}
		return ret;
	}

	
	public static boolean isValidComplexQueryResultMap(Map<String, ComplexQueryResult<?>> results,Set<String> snSet){
		boolean allGood = true;
		for(String sn:snSet){
			if(!results.containsKey(sn)){
				allGood=false;
				continue;
			}
			if(!results.get(sn).isValidResult()){
				continue;
			}
		}	
		return allGood;
	}
	
	public static List<String> ComplexQueryResultMapInvalidResults(Map<String, ComplexQueryResult<?>> results){
		List<String> errors = new ArrayList<String>();
		for(Entry<String, ComplexQueryResult<?>> result:results.entrySet()){
			ComplexQueryResult<?> cqr = result.getValue();
			if(!cqr.isValidResult()){
				errors.add(cqr.getException().getMessage());
			}
		}
		if(errors.size()<1)return null;
		return errors;
	}

	@SuppressWarnings("rawtypes")
	public static String CqrInvalidResultListString(Map<String,ComplexQueryResult<BigDecimal>> cqrMap){
		List<String> errors = new ArrayList<String>();
		for(Entry<String, ComplexQueryResult<BigDecimal>> result:cqrMap.entrySet()){
			ComplexQueryResult<?> cqr = result.getValue();
			if(!cqr.isValidResult()){
				errors.add(cqr.getException().getMessage());
			}
		}
		String mess = null;
		
		if(errors.size()>0){
			mess = "";
			for(String err:errors){
				mess += err+";";
			}
		}
		return mess;
	}
	
	public static List<SecDef> getOptionSecDefs(
			QueryInterface<String, SecDef> sdQuery,
			int timeoutValue, TimeUnit timeUnitType,
			String symbol,SecSymbolType symbolType,
			SecExchange exchange,SecCurrency currency,
			int contractYear,int contractMonth, Integer contractDay,
			BigDecimal midPointStrike, BigDecimal strikeDistance, 
			int numCalls,int numPuts){
	
		List<SecDef> ret = new ArrayList<SecDef>();
		String right = "P";
		for(int i = numPuts-1;i>=0;i--){
			BigDecimal strike = 
					midPointStrike.multiply(strikeDistance).multiply(new BigDecimal(i));
			ShortNameInfo sni = 
					new ShortNameInfo(
							symbol, symbolType, exchange, 
							currency, contractYear, contractMonth, 
							null,right, strike);
			String sn = sni.getShortName();
			if(sn==null){
				ret.add(null);
				continue;
			}
			SecDef sd = sdQuery.get(sn, timeoutValue, timeUnitType);
			if(sd==null){
				ret.add(null);
				continue;
			}
			ret.add(sd);
		}
		
		right = "C";
		for(int i = 1;i<numCalls;i++){
			BigDecimal strike = 
					midPointStrike.multiply(strikeDistance).multiply(new BigDecimal(i));
			ShortNameInfo sni = 
					new ShortNameInfo(
							symbol, symbolType, exchange, 
							currency, contractYear, contractMonth, contractDay,
							right, strike);
			String sn = sni.getShortName();
			if(sn==null){
				ret.add(null);
				continue;
			}
			SecDef sd = sdQuery.get(sn, timeoutValue, timeUnitType);
			if(sd==null){
				ret.add(null);
				continue;
			}
			ret.add(sd);
		}
		return ret;
	}
	
	public static <I,O> Map<String, ComplexQueryResult<O>> processCqrResult(
			Set<String> keySet,
			Map<String,ComplexQueryResult<I>> map,
			TransformCqrQuery<I,O> transformQuery,
			int timeoutValue, TimeUnit timeUnitType){
		
		Map<String, ComplexQueryResult<O>> ret = new HashMap<String, ComplexQueryResult<O>>();
		
		for(String key:keySet){
			if(!map.containsKey(key)){
				ComplexQueryResult<O> cqrErr = 
						errorRet(key+" No Input Cqr Found");
				ret.put(key,cqrErr);
				continue;
			}
			ComplexQueryResult<I> cqrIn = map.get(key);
			if(!cqrIn.isValidResult()){
				ComplexQueryResult<O> cqrErr = 
						errorRet(key+" "+cqrIn.getException().getMessage());
				ret.put(key,cqrErr);
				continue;
			}
			I in = cqrIn.getResult();
			Tuple<String, I> inTuple = new  Tuple<String, I>(key, in);
			ComplexQueryResult<O> cqrOut = transformQuery.get(inTuple, timeoutValue, timeUnitType);
			if(cqrOut==null){
				ComplexQueryResult<O> cqrErr = 
						errorRet(key+" Transform Failed");
				ret.put(key,cqrErr);
				continue;
			}
			ret.put(key, cqrOut);
		}
		return ret;
		
	}
	
	public abstract static class TransformCqrQuery<I,O> implements 
		QueryInterface<Tuple<String, I>, ComplexQueryResult<O>>{
		
	}
	
	public static <K,V> Tuple<Boolean,ComplexQueryResult<V>> checkCqr(K key,
			Map<K,ComplexQueryResult<V>> cqrMap){
		if(!cqrMap.containsKey(key)){
			ComplexQueryResult<V> cqr = errorRet(key+" No value returned from Query");
			Tuple<Boolean,ComplexQueryResult<V>> ret = 
					new Tuple<Boolean, ComplexQueryResult<V>>(false, 
							cqr);
			return  ret;
		}
		ComplexQueryResult<V> cqrInMap = cqrMap.get(key);
		boolean goodOrBad = true;
		if(!cqrInMap.isValidResult()){
			goodOrBad=false;
		}
		Tuple<Boolean,ComplexQueryResult<V>> ret = 
				new Tuple<Boolean, ComplexQueryResult<V>>(goodOrBad, 
						cqrInMap);
		return  ret;
	}
	
	public static Long getYYYYMMFromContract(String MYY){
		String contract = MYY.substring(MYY.length()-3,MYY.length());
		Integer month = getMonthNumFromMonthCode(contract.substring(0,1));
		Integer year = Integer.parseInt(contract.substring(1, 3));
		return Long.parseLong((((year+2000)*100)+month)+"");
	}

	public static Set<String> getPairNameSet(
			Set<String> snSet,
			String pairSeparator){
		String ps = pairSeparator;
		if(ps==null){
			ps = DEFAULT_CORRELATION_PAIR_SEPARATOR;
		}
		Set<String> ret = new TreeSet<String>();
		List<String> orderedList = new ArrayList<String>(new TreeSet<String>(snSet));
		for(int i = 0;i<snSet.size();i++){
			for(int j = i;j<snSet.size();j++){
				String pair = orderedList.get(i)+ps+orderedList.get(j);
				ret.add(pair);
			}
		}
		return ret;
	}
	
	public static <V> Map<String, V> getSubPairMapFromStringSet(
		Set<String> snSet,
		Map<String,V> map,
		String pairSeparator){
		String ps = pairSeparator;
		if(ps==null){
			ps = DEFAULT_CORRELATION_PAIR_SEPARATOR;
		}
		TreeMap<String, V> ret = new TreeMap<String, V>();
		List<String> orderedList = new ArrayList<String>(new TreeSet<String>(snSet));
		for(int i = 0;i<orderedList.size();i++ ){
			for(int j = 0;j<orderedList.size();j++){
				String pair = orderedList.get(i)+ps+orderedList.get(j);
				if(!map.containsKey(pair))continue;
				ret.put(pair, map.get(pair));
			}
		}
		
		return ret;
	}

	
	public static Map<String,List<String>> getUnderlyingsPerDerivName(
			Set<String> derivNames,
			DerivativeSetEngine dse,
			int timeunitValue,
			TimeUnit timeUnit){
		Map<String,List<String>> ret = 
				new HashMap<String, List<String>>();
		
		QueryManager qm = dse.getQueryManager();
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Set<String> underDiotNames = new HashSet<String>();
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			List<String> underList = new ArrayList<String>();
			for(SecDef sd:qm.getUnderlyingSecDefs(derivName, timeunitValue, timeUnit)){
				String underName = sd.getShortName();
				underList.add(underName);
				underDiotNames.add(sd.getShortName());
			}
			ret.put(derivName,underList);
		}
		return ret;
	}

	public static Set<String> getUnderlyingNamesSet(
			Set<String> derivNames,
			DerivativeSetEngine dse,
			int timeunitValue,
			TimeUnit timeUnit){
		Map<String, List<String>> underlyingNamesMap = 
				getUnderlyingsPerDerivName(derivNames, dse, timeunitValue, timeUnit);
		Set<String> underlyingNames = new HashSet<String>();
		for(Entry<String, List<String>> entry:underlyingNamesMap.entrySet()){
			underlyingNames.addAll(entry.getValue());
		}
		return underlyingNames;
	}
}
