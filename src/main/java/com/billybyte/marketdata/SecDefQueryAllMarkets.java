package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Rounding;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.marketdata.futures.ExpiryFromListOfExpiryRules;
import com.billybyte.marketdata.futures.ExpiryFromListOfExpiryRulesBySecSymbolType;
import com.billybyte.marketdata.futures.ExpiryRuleInterface;
import com.billybyte.marketdata.futures.SecDefQueryFuturesStrips;
/**
 * THIS CLASS IS VERY WIDELY USED TO CREATE SECURITY DEFINITIONS ALGORYTHMICALLY.  BE
 *    CAREFUL UPDATING IT.
 * Create a list of SecDefQueryFromRules objects for various products.
 * THE PRODUCTS MUST ALSO HAVE BEEN ADDED TO THE CLASS ExpiryFromListOfExpiryRules.java so that
 *   valid expiration dates can be obtained for the product.
 *   
 * this class is a convenience class for accessing SecDefs of Stocks, 
 * Stock Options, Futures, Futures Options, Futures Spreads and Strips
 * It creates new SecDefQuerys of different types, and adds them to a SecDefQueryFromListOfSecDefQueries.
 *   During the get method, the SecDefQueryFromListOfSecDefQueries is queried for the SecDef.
 *   
 *   
 *   
 * @author Bill Perlman
 *
 */
// TODO - must clean up excess code and start using csv file instead of hardcoded product listing
public class SecDefQueryAllMarkets implements QueryInterface<String, SecDef>{
	SecDefQueryFromListOfSecDefQueries listQuery;
	private String productInfoCsvFileName="ExchangeProductList.csv";
	private final Object get_Lock = new Object();
	protected boolean inited = false;
	
//	// support for SecDef queries from mongo
//	private static final String ALT_QUERIES_FROM_SPRING_XML_PATH = 
//			"secDefQueryListFromSpring.xml";
//	private static final String ALT_QUERIES_LIST_BEANNAME = "queryList";
	
	public SecDefQueryAllMarkets(){
		init();
	}
	@Override
	public SecDef get(String key, int timeoutValue, TimeUnit timeUnitType) {
		synchronized (get_Lock) {
			SecDef sdTest = listQuery.get(key, timeoutValue, timeUnitType);
			return sdTest;
		}
	}

	protected void init(){
		synchronized (get_Lock) {
			if(inited)return;
			int symCol = 0;
			// create the stock query allowing symbols from NYSE, AMEX, NASDAQ and OTC bulletin board
			List<String[]> csvData=null;
			Set<String> validSymbols = new HashSet<String>();


			ExpiryRuleInterface expiryLookup = new ExpiryFromListOfExpiryRules();
			List<QueryInterface<String, SecDef>> testQueryList = new ArrayList<QueryInterface<String, SecDef>>();
			listQuery = new SecDefQueryFromListOfSecDefQueries(testQueryList);
			Set<SecSymbolType> validTypes = new HashSet<SecSymbolType>();

			// do STKs
			validTypes.add(SecSymbolType.STK);
			List<Integer> validContractMonths = null;
			int precision = 2;
			BigDecimal minTick = new BigDecimal(.01);
			BigDecimal multiplier = BigDecimal.ONE;
			SecCurrency currency = SecCurrency.USD;
			SecExchange exchange = SecExchange.SMART;
			SecDefQueryFromRules stkQueryEngine = new SecDefQueryFromRules(".",
					validTypes, null, validContractMonths, currency,
					exchange, precision, minTick, multiplier, expiryLookup);
			testQueryList.add(stkQueryEngine);

			// do all stock options
			validTypes = CollectionsStaticMethods.setFromArray(
					new SecSymbolType[]{SecSymbolType.OPT});
			ExpiryRuleInterface expiryLookupForOpt = new ExpiryFromListOfExpiryRulesBySecSymbolType();
			SecDefQueryFromRules optQueryEngine = new SecDefQueryFromRules(".",
					validTypes, null, validContractMonths, currency,
					exchange, precision, minTick, multiplier, expiryLookupForOpt);
			testQueryList.add(optQueryEngine);
			
			
			// do all futures
			// get rules from csv

			csvData = Utils.getCSVData(ExpiryFromListOfExpiryRules.class, productInfoCsvFileName);

			
			String[] header = csvData.get(0);
			HashMap<String, Integer> headerValues = new HashMap<String, Integer>();
			for (int i = 0; i < header.length; i++) {
				headerValues.put(header[i], i);
			}
			for (int i = 1; i < csvData.size(); i++) {
				// get valid types
				validTypes = new HashSet<SecSymbolType>();
				String[] validTypesStrings = csvData.get(i)[headerValues
						.get("validTypes")].split(";");
				for (String validTypeString : validTypesStrings) {
					validTypes.add(SecEnums.SecSymbolType
							.fromString(validTypeString));
				}
				validSymbols = new HashSet<String>();
				String[] validSymbolStrings = csvData.get(i)[headerValues
						.get("symbol")].split(";");
				for (String validSymbolString : validSymbolStrings) {
					validSymbols.add(validSymbolString);
				}
				minTick = new BigDecimal(csvData.get(i)[headerValues.get(
						"minOrderTick").intValue()]);
				precision = Rounding.leastSignificantDigit(minTick);

				multiplier = new BigDecimal(
						csvData.get(i)[headerValues.get("notionalContractSize")]);
				exchange = SecExchange.fromString(csvData.get(i)[headerValues
						.get("exchange")]);

				SecDefQueryFromRules queryFromRules = new SecDefQueryFromRules(
						".", validTypes, validSymbols, validContractMonths,
						currency, exchange, precision, minTick, multiplier,
						expiryLookup);
				testQueryList.add(queryFromRules);
			}
			// create futures spreads and strips queries
			SecDefQueryFuturesSpreads futSpreadsQueryEngine = new SecDefQueryFuturesSpreads(
					this);
			SecDefQueryFuturesStrips futStripsQueryEngine = new SecDefQueryFuturesStrips(
					this);
			testQueryList.add(futSpreadsQueryEngine);
			testQueryList.add(futStripsQueryEngine);
			
			// add other queries from spring xml
			try {
				QueryInterface<String, SecDef> spanSdQuery = new SecDefQuerySpanMongo(null, null, false);
				testQueryList.add(spanSdQuery);
			} catch (Exception e) {
				Utils.prtObErrMess(SecDefQueryAllMarkets.class, "!!!!!!!!! NON-FATAL STACK TRACE DUE TO: Span Mongo not running on default ip and port");
				e.printStackTrace();
				Utils.prtObErrMess(SecDefQueryAllMarkets.class, "!!!!!!!!! END OF NON-FATAL STACK TRACE DUE TO: Span Mongo not running on default ip and port");
			}
			
		}
	}
	
		
	
}
