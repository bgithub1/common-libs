package com.billybyte.csvprocessing.iceexpiryprocessing;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.csvprocessing.cmeexpiryprocessing.CmeCalendarData;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.neodatis.NeoDatisSingleClassDataBase;

/**
 * class that holds main to parse Ice_Contract_Data.csv file which holds various
 * expiry dates (LTD, FND, etc) for contracts that are traded on ICE.
 * @author Bill Perlman
 *
 */
public class IceExpiryDatabaseBuild {
	// create a series of regex patterns to help extract data from columns
	protected static String exchangesRegex = "(EUROPE)|(OTC)|(US|CANADA)";
	protected static Pattern exchangesRegexPattern;
	protected static String dateTypesRegex = "(FDD)|(FND)|(FTD)|(LDD)|(LTD)";
	protected static Pattern dateTypesRegexPattern;
	protected static String monthNamesRegex = "(Jan)|(Feb)|(Mar)|(Apr)|(May)|(Jun)|(Jul)|(Aug)|(Sep)|(Oct)|(Nov)|(Dec)";
	protected static Pattern monthNamesRegexPattern;
	protected static String yearRegex  = "([0-9]{2,2})";
	protected static Pattern yearRegexPattern;
	protected static String symbolRegex = "\\[([A-Z0-9]{1,})\\]";
	protected static Pattern symbolRegexPattern;
	protected static String contractIdRegex = "("+monthNamesRegex+")"+"-{0,1}"+"([0-9]{2,2})";
	protected static Pattern contractIdRegexPattern;
	protected static String optionIdRegex = "Option";
	protected static Pattern optionIdRegexPattern;
	
	protected static String[] monthCodesArray = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	protected static String fieldWithinFieldSeparator=":";
	protected static String contractWithinFieldSeparator=";";
	private static String[][] exchangeInCsvToExchangePairs = {{"EUROPE","IPE"},{"US","NYBOT"},{"OTC","ICE"},{"CANADA","WCE"}};
	
	private static DecimalFormat monthFormat = new DecimalFormat("00");
	
	private static HashMap<String, String> exchangeInCsvToExchangeMap;
	
	protected static HashMap<String, IceExpiryData> iceExpiryDataMap;
	private static HashMap<String,HashMap<String,String>> exchangeSymbolMap;
	
	
	public static void main(String[] args) {
		// CHECK ARGS
		if(args==null || args.length<2){
			throw Utils.IllArg(CmeCalendarData.class, " need 1 arguments.  arg0 = Ice_Calendar_Data.csv file ftp path. arg1 = NeoDatisSingleClassDatabase<String, CmeContract> path");
		}
		
		final String csvFileName = args[0];
		final String databaseOutputPath = args[1];
		
		// CREATE PATTERN TO DETERMINE WHICH PRODUCTS GET INTO DATABASE
		/**
		 * (NYBOT) all of Nybot
		 * (WCE) all of Canada
		 * (IPE)(G|B|(NCG)|(UCX)|M|N|O|T|P)  on the IPE, WTI, brent, gasoil, heat, rbob and some ng stuff
		 * (ICE)(H|R|HEN|HIS|HHD) on  OTC, just henry stuff
		 */
		Pattern exchangeSymbolRegexValidator=Pattern.compile("^(NYBOT)|(WCE)|(^(IPE)(G|B|(NCG)|(UCX)|M|N|O|T|P)$)|(^(ICE)(H|R|HEN|HIS|HHD)$)$");
		if(args.length>2 && args[2].compareTo("  ")>0){
			// get exchange/product regex pattern
			exchangeSymbolRegexValidator  = Pattern.compile(args[2]);
		}
		
		// INIT ALL MAPS AND REGEX PATTERS
		initMaps();
		initPatterns();

		// GET csv DATA HERE
		List<String[]> csvData  = Utils.getCSVData(csvFileName);
		if(csvData==null){
			throw Utils.IllArg(IceExpiryDatabaseBuild.class, " csvData is null");
		}
		if(csvData.size()<=0){
			throw Utils.IllArg(IceExpiryDatabaseBuild.class, " csvData list has no data");
		}
		
		// OPEN UP DATABASE HERE AND DO ALL PROCESSING - IF IT DOESN'T EXIST, IT WILL BE CREATED
		NeoDatisSingleClassDataBase<String, IceExpiryData> database = 
			populateDatabase(csvData, databaseOutputPath,exchangeSymbolRegexValidator);
		
		// print out product list
		String producutInfoDatabaseName=null;
		if(args.length>3){
			producutInfoDatabaseName = args[3];
		}
		database.close();

		updateProductInfoDatabase(producutInfoDatabaseName);

	}
	

	protected static void initMaps(){
		// set up exchange id maps to parse exhange fields
		exchangeInCsvToExchangeMap  = new HashMap<String, String>();
		for(int i = 0;i<exchangeInCsvToExchangePairs.length;i++){
			exchangeInCsvToExchangeMap.put(exchangeInCsvToExchangePairs[i][0], exchangeInCsvToExchangePairs[i][1]);
		}
		
		// init main maps
		iceExpiryDataMap = new HashMap<String, IceExpiryData>();
		exchangeSymbolMap = new HashMap<String, HashMap<String,String>>();

	}
	protected static void initPatterns(){
		// create regex patterns
		dateTypesRegexPattern = Pattern.compile(dateTypesRegex);
		monthNamesRegexPattern = Pattern.compile(monthNamesRegex);
		symbolRegexPattern = Pattern.compile(symbolRegex);
		exchangesRegexPattern = Pattern.compile(exchangesRegex);
		contractIdRegexPattern = Pattern.compile(contractIdRegex);
		optionIdRegexPattern = Pattern.compile(optionIdRegex);
		yearRegexPattern = Pattern.compile(yearRegex);

	}
	
	/**
	 * This is the main loop, which reads every csv line, parses it, and creates of database of IceExpiryData objects
	 * @param csvData - data from ICE
	 * @param databaseOutputPath - path of database
	 * @return - database, populated
	 */
	protected static NeoDatisSingleClassDataBase<String, IceExpiryData> populateDatabase(List<String[]> csvData, 
			String databaseOutputPath,Pattern exchangeSymbolRegexValidator){
		// open the database (or create it)
		NeoDatisSingleClassDataBase<String, IceExpiryData> dataBase = 
			new NeoDatisSingleClassDataBase<String, IceExpiryData>(databaseOutputPath,IceExpiryData.class);
		
		// start from index = 1 to avoid header
		for(int i = 1;i<csvData.size();i++){
			// get a line
			String[] line = csvData.get(i);
			if(line[0].contains("12/12/2011")){
				Utils.prtObErrMess(IceExpiryDatabaseBuild.class, " debuggin line reached to fine 12/05/2011");
			}
			// validate it and get exchange
			String exchString =  getExchangeStringFromRegex(line);
			if(exchString==null)continue;
			
			// translate that into a SecExchange enum object
			SecExchange  exch = SecExchange.fromString(exchangeInCsvToExchangeMap.get(exchString));

			// get hash map with basic symbol info (does not help database build, but allows you
			//  to print useful info at end of main.
			HashMap<String,String> thisExchangeInfoHashMap = getExchangeSymbolMapHashMap(exch);
			
			// update database with all symbols that are referenced on this csv line
			updateHashMaps(line,  exch,   thisExchangeInfoHashMap, exchangeSymbolRegexValidator);
			
			// print the csv line that you just processed
			Utils.prtObMess(IceExpiryDatabaseBuild.class, Arrays.toString(line));
			
		}
		
		// now update database
		Utils.prtObMess(IceExpiryDatabaseBuild.class, " updating database ...");
		for(String key:iceExpiryDataMap.keySet()){
			IceExpiryData ied = dataBase.get("shortName", key);
			if(ied==null){
				// update it
				dataBase.put(iceExpiryDataMap.get(key));
			}
		}
		// save the database by closing it
		dataBase.close();
		dataBase.open();
		return dataBase;

		
	}

	protected static String  getExchangeStringFromRegex(String[] line){
		if(line.length<2)return null;// ignore balmo contracts for now
		List<String> exchStringListFromRegex =  RegexMethods.getRegexMatches(exchangesRegexPattern, line[1]);
		if(exchStringListFromRegex==null || exchStringListFromRegex.size()<=0){
			return null;
		}
		// get the exchange string (like EUROPE, US, CANADA, OTC)
		String exchString = exchStringListFromRegex.get(0);

		return exchString;
	}
	protected static void updateHashMaps(
			String[] line,
			SecExchange exch,
			HashMap<String,String> thisExchangeInfoHashMap,
			Pattern exchangeSymbolRegexValidator){

		// get tokens from column 2 to be used later on
		String[] column2tokens = parseColumn(2, line);
		// get type of date, LTD, FND, FTD, etc
		String dateType = RegexMethods.getRegexMatches(dateTypesRegexPattern, line[1]).get(0);
		// get all of the symbols on this csv line
		List<String> symbols = RegexMethods.getRegexMatchesAllowRepeats(symbolRegexPattern, line[2]);
		// get all of the monthyears on this csv line
		List<String> contractIds = RegexMethods.getRegexMatchesAllowRepeats(contractIdRegexPattern, line[2]);
		// make sure there are enough monthyears for all of the symbols
		if(symbols.size()>contractIds.size()){
			String errmess = "symbols: "+Arrays.toString(symbols.toArray())+" "+"contractIDS: "+Arrays.toString(contractIds.toArray());
			Utils.prtObErrMess(IceExpiryDatabaseBuild.class," symbols and contractIds are not same size :" +errmess);
			return;
		}
		// get the actual date value string to stick into one of the date fields of the IceExpiryData object
		String dateStringValueToBeUpdated = line[0];
		
		// for each symbol, get the symbol and the monthyear to create a shortName
		for(int j=0;j<symbols.size();j++){
			String symbol = symbols.get(j).replace("[", "").replace("]", "");
			
			// combine exchange and symbol, and see if it meets regex criteria to be processed
			if(exchangeSymbolRegexValidator!=null){
				String exchSym = exch.toString()+symbol;
				if(RegexMethods.getRegexMatches(exchangeSymbolRegexValidator, exchSym).size()<=0){
					// ignore
					continue;
				}
			}
			
			
			String contractId = contractIds.get(j);
			int monthInt = getMonthNum(RegexMethods.getRegexMatches(monthNamesRegexPattern, contractId).get(0));
			String monthString = monthFormat.format(monthInt);
			
			String yearString = new Integer(2000+new Integer(RegexMethods.getRegexMatches(yearRegexPattern,contractId).get(0))).toString();
			// get symbol type, i.e FUT or FOP
			SecSymbolType symbolType=SecSymbolType.FUT;
			List<String> optionIdResultList = RegexMethods.getRegexMatches(optionIdRegexPattern, column2tokens[3+j*2]);
			if(optionIdResultList!=null && optionIdResultList.size()>0){
				symbolType=SecSymbolType.FOP;
			}
			String shortName = symbol+"."+symbolType.toString()+"."+exch.toString()+".USD."+yearString+monthString;
			Utils.prtObMess(IceExpiryDatabaseBuild.class, shortName);
			
			// UPDATE THE INFORMATIONAL MAP
			thisExchangeInfoHashMap.put(symbol,column2tokens[3+j*2]);
			
			// update the map that will generate the database after this loop is finished
			// first create an object
			String ltd, ftd,fnd,fdd,ldd;
			ltd = ftd=fnd=fdd=ldd=null;
			// see if we already hava a record
			if(shortName.compareTo("G.FUT.IPE.USD.201112")==0){
				Utils.prtObMess(IceExpiryDatabaseBuild.class, " got G.FUT.IPE.USD.201112");
			}
			IceExpiryData ied;
			if(iceExpiryDataMap.containsKey(shortName)){
				ied = iceExpiryDataMap.get(shortName);
				ltd=ied.getLTD();
				ftd = ied.getFTD();
				fnd = ied.getFND();
				fdd = ied.getFDD();
				ldd = ied.getLDD();
				
			}
			if(dateType.compareTo("FDD")==0){
				fdd = dateStringValueToBeUpdated;
			}else if(dateType.compareTo("FND")==0){
				fnd = dateStringValueToBeUpdated;
			}else if(dateType.compareTo("FTD")==0){
				ftd = dateStringValueToBeUpdated;
			}else if(dateType.compareTo("LDD")==0){
				ldd = dateStringValueToBeUpdated;
			}else if(dateType.compareTo("LTD")==0){
				ltd = dateStringValueToBeUpdated;
			}

			ied = new IceExpiryData(shortName,ltd,ftd,fnd,fdd,ldd);
			// update map
			iceExpiryDataMap.put(shortName, ied);
			
		}

	}
	
	protected static HashMap<String,String> getExchangeSymbolMapHashMap(SecExchange exch){
		// see if the exchangeSymbol map needs to have a new hashmap for this exchange
		if(!exchangeSymbolMap.containsKey(exch.toString())){
			exchangeSymbolMap.put(exch.toString(), new HashMap<String,String>());
		}
		// get the hashmap for this exchange
		//  this hash map accumulates product names and symbols by exchange, for informational help -
		//   NOT FOR UPDATING THE DATABASE
		HashMap<String,String> thisExchangeMap = exchangeSymbolMap.get(exch.toString());
		return thisExchangeMap;
		
	}
	
	protected static String[] parseColumn(int colIndex,String[] line){
		if(line.length<colIndex)return null;
		String field = line[colIndex];
		// if it's blank return futures
		if(field==null || field.compareTo(" ")<=0){
			return null;
		}
		// get pairs of contract id's and contract names
		String[] tokens = field.split(fieldWithinFieldSeparator);
		return tokens;
	}
	
	protected static int getMonthNum(String monthString){
		for(int i=0;i<monthCodesArray.length;i++){
			if(monthCodesArray[i].compareTo(monthString)==0){
				return i+1;
			}
		}
		return -1;
	}
	
	protected static String createExchangeName(String[] line){
		String fieldWithExchangeId = line[1];
		String[] tokens = fieldWithExchangeId.split(fieldWithinFieldSeparator);
		String exchangeId = tokens[0].trim();
		return exchangeInCsvToExchangeMap.get(exchangeId);
		
	}
	
	protected static void updateProductInfoDatabase(String productInfoDataBase){
		NeoDatisSingleClassDataBase<String, IceProductInfo> productInfo=null;
		if(productInfoDataBase!=null){ 
			productInfo = new NeoDatisSingleClassDataBase<String, IceProductInfo>(productInfoDataBase,IceProductInfo.class);
		}
		// print out product list
		for(String exch:exchangeSymbolMap.keySet()){
			Utils.prtObMess(IceExpiryDatabaseBuild.class, exch.toString());
			String out = "";
			for(String symbol:exchangeSymbolMap.get(exch).keySet()){
				String description = exchangeSymbolMap.get(exch).get(symbol);
				Utils.prtObMess(IceExpiryDatabaseBuild.class,exch+" : "+symbol+" : "+description);
				if(productInfo!=null){
					productInfo.put(new IceProductInfo(symbol,exch,description));
				}
			}
		}
		if(productInfo!=null)productInfo.close();

	}
}
