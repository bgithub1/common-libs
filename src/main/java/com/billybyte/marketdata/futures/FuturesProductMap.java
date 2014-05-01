package com.billybyte.marketdata.futures;


import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import com.billybyte.commoncollections.Tuple;

import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.csvprocessing.GenericMapFromCsv;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public class FuturesProductMap extends GenericMapFromCsv<String, FuturesProduct>{
	private static String getExchangeProductListPath(){
		return FuturesProductMap.class.getResource("ExchangeProductList.csv").getPath();
	}
	public FuturesProductMap() {
		super(new ConcurrentHashMap<String, FuturesProduct>(), getExchangeProductListPath(),true);
	}

	@Override
	protected Tuple<String, FuturesProduct> getKeyAndData(int rowNum) {
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
		
		String[] validSymbolStrings = this.getColumnValue("symbol", rowNum).split(";");
		symbols = new HashSet<String>();
		// only use first symbol for now
		symbols.add(validSymbolStrings[0]);
//		for(String validSymbolString:validSymbolStrings){
//			symbols.add(validSymbolString);
//		}

		exchange = SecExchange.fromString(getColumnValue("exchange", rowNum));
		validTypes = new HashSet<SecSymbolType>();
		contractDescription = getColumnValue("contractDescription", rowNum);
		for(String validType:getColumnValue("validTypes_col",rowNum).split(";")){
			validTypes.add(SecSymbolType.fromString(validType));
		}
		
		String monthsAllowedString = getColumnValue("monthsAllowed",rowNum);
		monthsAllowed = new  TreeSet<Integer>();
		for(int j = 0;j<monthsAllowedString.length();j++){
			String s = monthsAllowedString.substring(j,j+1);
			monthsAllowed.add(MarketDataComLib.getMonthNumFromMonthCode(s));
		}
		
		notionalContractSize = RegexMethods.getBigDecimalFromNumberString(getColumnValue("notionalContractSize",rowNum));
		notionalTickSize = RegexMethods.getBigDecimalFromNumberString(getColumnValue("notionalTickSize",rowNum));
		pointValue = RegexMethods.getBigDecimalFromNumberString(getColumnValue("pointValue",rowNum));
		ticValue = RegexMethods.getBigDecimalFromNumberString(getColumnValue("ticValue",rowNum));
		minOrderTick = RegexMethods.getBigDecimalFromNumberString(getColumnValue("minOrderTick",rowNum));
		underlyingSymbol = this.getColumnValue("underlyingSymbol", rowNum);
		FuturesProduct prod = new FuturesProduct(
				symbols,exchange,validTypes,monthsAllowed,
				contractDescription,notionalContractSize,notionalTickSize,
				pointValue,ticValue,startTimeInMills,endTimeInMills,minOrderTick,
				sizeDescription,underlyingSymbol);
		return new Tuple<String, FuturesProduct>(validSymbolStrings[0], prod);
	}

}
