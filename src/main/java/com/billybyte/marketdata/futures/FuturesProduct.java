package com.billybyte.marketdata.futures;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.billybyte.commonstaticmethods.Rounding;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public class FuturesProduct {
	public static  final  String NO_SPAN_SYMBOL = "_NA"; 
	private final Set<String> symbols	;
	private final String contractDescription;
	private final SecExchange exchange	;
	private final long startTimeInMills;
	private final long endTimeInMills;
	private final String sizeDescription	;
	private final TreeSet<Integer> monthsAllowed	;
	private final BigDecimal ticValue	;
	private final BigDecimal pointValue	;
	private final BigDecimal notionalContractSize	;
	private final BigDecimal minOrderTick	;
	private final BigDecimal notionalTickSize;	
	private final Set<SecSymbolType> validTypes;
	private final String underlyingSymbol;
	private final SecCurrency currency;
	private final String spanSymbol;
	
	
	public FuturesProduct(Set<String> symbols, SecExchange exchange,
			Set<SecSymbolType> validTypes, Set<Integer> monthsAllowed,
			String contractDescription, BigDecimal notionalContractSize,
			BigDecimal notionalTickSize, BigDecimal pointValue,
			BigDecimal ticValue, long startTimeInMills, long endTimeInMills,
			BigDecimal minOrderTick, String sizeDescription,String underlyingSymbol) {
		this(symbols, exchange, validTypes, monthsAllowed,
				contractDescription, notionalContractSize, notionalTickSize, pointValue, 
				ticValue, startTimeInMills, endTimeInMills, minOrderTick, 
				sizeDescription,	underlyingSymbol, SecCurrency.USD);
	}
	
	public FuturesProduct(Set<String> symbols, SecExchange exchange,
			Set<SecSymbolType> validTypes, Set<Integer> monthsAllowed,
			String contractDescription, BigDecimal notionalContractSize,
			BigDecimal notionalTickSize, BigDecimal pointValue,
			BigDecimal ticValue, long startTimeInMills, long endTimeInMills,
			BigDecimal minOrderTick, String sizeDescription,String underlyingSymbol,
			SecCurrency currency,String spanSymbol) {
		super();
		this.symbols = symbols;
		this.exchange = exchange;
		this.validTypes = new HashSet<SecSymbolType>(validTypes);
		this.monthsAllowed = new TreeSet<Integer>(monthsAllowed);
		this.contractDescription = contractDescription;
		this.notionalContractSize = notionalContractSize;
		this.notionalTickSize = notionalTickSize;
		this.pointValue = pointValue;
		this.ticValue = ticValue;
		this.startTimeInMills = startTimeInMills;
		this.endTimeInMills = endTimeInMills;
		this.minOrderTick = minOrderTick;
		this.sizeDescription = sizeDescription;
		this.underlyingSymbol = underlyingSymbol;
		this.currency = currency;
		this.spanSymbol = spanSymbol;
	}

	public FuturesProduct(Set<String> symbols, SecExchange exchange,
			Set<SecSymbolType> validTypes, Set<Integer> monthsAllowed,
			String contractDescription, BigDecimal notionalContractSize,
			BigDecimal notionalTickSize, BigDecimal pointValue,
			BigDecimal ticValue, long startTimeInMills, long endTimeInMills,
			BigDecimal minOrderTick, String sizeDescription,String underlyingSymbol,
			SecCurrency currency) {
		this(symbols, exchange, validTypes, monthsAllowed, contractDescription, 
				notionalContractSize, notionalTickSize, pointValue, 
				ticValue, startTimeInMills, endTimeInMills, minOrderTick, 
				sizeDescription, underlyingSymbol, currency, NO_SPAN_SYMBOL);
	}

	
	public SecCurrency getCurrency() {
		return currency;
	}


	@SuppressWarnings("unused")
	private FuturesProduct() {
		super();
		this.symbols = null;
		this.exchange = null;
		this.validTypes = null;
		this.monthsAllowed = null;
		this.contractDescription = null;
		this.notionalContractSize = null;
		this.notionalTickSize = null;
		this.pointValue = null;
		this.ticValue = null;
		this.startTimeInMills = -1;
		this.endTimeInMills = -1;
		this.minOrderTick = null;
		this.sizeDescription = null;
		this.underlyingSymbol = null;
		this.currency=null;
		this.spanSymbol = null;
	}


	public Set<String> getSymbols() {
		return symbols;
	}
	public String getContractDescription() {
		return contractDescription;
	}
	public SecExchange getExchange() {
		return exchange;
	}
	public long getStartTimeInMills() {
		return startTimeInMills;
	}
	public long getEndTimeInMills() {
		return endTimeInMills;
	}
	public String getSizeDescription() {
		return sizeDescription;
	}
	public TreeSet<Integer> getMonthsAllowed() {
		return monthsAllowed;
	}
	public BigDecimal getTicValue() {
		return ticValue;
	}
	public BigDecimal getPointValue() {
		return pointValue;
	}
	public BigDecimal getNotionalContractSize() {
		return notionalContractSize;
	}
	public BigDecimal getMinOrderTick() {
		return minOrderTick;
	}
	public BigDecimal getNotionalTickSize() {
		return notionalTickSize;
	}
	public Set<SecSymbolType> getValidTypes() {
		return validTypes;
	}
	
	public String getUnderlyingSymbol() {
		return underlyingSymbol;
	}

	public String getSpanSymbol() {
		return spanSymbol;
	}

	
	public boolean isValidFuturesMonth(int month){
		return getMonthsAllowed().contains(month);
	}
	public MonthYear getNextValidFuturesMonth(MonthYear monthYearToStart){
		if(monthYearToStart==null)return null;
		int monthToStartAt = monthYearToStart.getMonth().getIndex();
		int yearToStartAt = monthYearToStart.getYearInt();
		Integer nextMonth = getMonthsAllowed().ceiling(monthToStartAt);
		if(nextMonth==null){
			return new MonthYear(yearToStartAt+1,Month.getMonth(getMonthsAllowed().first()));
		}
		return new MonthYear(yearToStartAt,Month.getMonth(nextMonth));
	}

	public String getPrimarySymbol(){
		List<String> symList = new ArrayList<String>(getSymbols());
		if(symList==null || symList.size()<1) return null;
		String thisSym = symList.get(0);
		return thisSym;

	}
	
	public SecSymbolType getPrimaryType(){
		List<SecSymbolType> typeList = new ArrayList<SecSymbolType>(getValidTypes());
		if(typeList==null || typeList.size()<1) return null;
		SecSymbolType thisType = typeList.get(0);
		return thisType;
	}

	public int getExchangePrecision(){
		return Rounding.leastSignificantDigit(getMinOrderTick());

	}

	@Override
	public String toString() {
		return symbols.toString().replace(",", ";") + ", " + contractDescription + ", " + exchange + ", "
				+ startTimeInMills + ", " + endTimeInMills + ", "
				+ sizeDescription + ", " + monthsAllowed.toString().replace(",", ";") + ", " + ticValue
				+ ", " + pointValue + ", " + notionalContractSize + ", "
				+ minOrderTick + ", " + notionalTickSize + ", " + validTypes.toString().replace(",", ";")
				+ ", " + underlyingSymbol + ", " + currency;
	}

	public String toStringDescription(){
		return "symbols, contractDescription,exchange,startTimeInMills,endTimeInMills,sizeDescription,monthsAllowed,ticValue,pointValue,notionalContractSize,minOrderTick,notionalTickSize,validTypes,underlyingSymbol,currency";
	}
}
