package com.billybyte.marketdata;

import java.util.HashMap;
import java.util.Locale;



public class SecEnums {
	public enum SecCurrency{
		USD("USD"),
		AUD("AUD"),
		EUR("EUR"),
		JPY("JPY"),
		CAD("CAD"),
		GBP("GBP");
		
		String currency;
		SecCurrency(String name){
			this.currency = name;
		}
		@Override
		public String toString() {
			return currency;
		}
		
		public static SecCurrency fromString(String currString){
			return valueOf(currString);
		}
		
	}
	

	private static final HashMap<SecCurrency,SecLocale> currencyToLocale = 
			new HashMap<SecEnums.SecCurrency, SecEnums.SecLocale>();
	
	public enum SecLocale{
		US("US",SecCurrency.USD),
		UK("UK",SecCurrency.GBP),
		JPY("JPY",SecCurrency.JPY),
		EUR("EUR",SecCurrency.EUR),
		AUD("UK",SecCurrency.AUD),
		NYMEX("NYMEX",SecCurrency.USD),
		;
		
		String locale ;
		SecCurrency currency;
		SecLocale(String locale, SecCurrency currency){
			this.locale = locale;
			this.currency = currency;
			currencyToLocale.put(currency, this);
		}
		
		public  final SecCurrency getCurrency(){
			return this.currency;
		}
		
		public static final SecLocale getLocaleFromSecDef(SecDef sd){
			SecExchange exch = sd.getExchange();
				SecLocale locale = SecLocale.valueOf(exch.toString());
				if(locale!=null)return locale;
			return currencyToLocale.get(sd.getCurrency());
		}
	}
	
	public enum SecSymbolType{
		STK("STK"),
		FUT("FUT"),
		CASH("CASH"),
		OPT("OPT"),
		IND("IND"),
		FOP("FOP"),
		TAS("TAS"),
		BAG("BAG"),
		OOC("OOC"),
		CMB("CMB"),
		FWD("FWD"),
		OOF("OOF");

		String ibSymbolTypeString;
		SecSymbolType(String ibSymbolTypeString){
			this.ibSymbolTypeString = ibSymbolTypeString;
			
		}
		

		@Override
		public String toString() {
			return ibSymbolTypeString.toString();
		}
		
		public static SecSymbolType fromString(String symbolTypeString){
			return valueOf(symbolTypeString);
		}
		
	}

	
	public enum SecExchange{
		IDEALPRO(SecSymbolType.CASH,"IDEALPRO"),
		SMART(SecSymbolType.STK,"SMART"),
		ARCA(SecSymbolType.STK,"ARCA"),
		AMEX(SecSymbolType.STK,"AMEX"),
		CBOE(SecSymbolType.STK,"CBOE"),
		ISE(SecSymbolType.STK,"ISE"),
		NYSE(SecSymbolType.STK,"ISE"),
		LIFFE(SecSymbolType.FUT,"LIFFE"),
		WCE(SecSymbolType.FUT,"WCE"),
		GLOBEX(SecSymbolType.FUT,"GLOBEX"),
		NYMEX(SecSymbolType.FUT,"NYMEX"),
		NYM(SecSymbolType.FUT,"NYMEX"),
		COMEX(SecSymbolType.FUT,"COMEX"),
		CMX(SecSymbolType.FUT,"COMEX"),
		MGEX(SecSymbolType.FUT,"MGEX"),
		KCBT(SecSymbolType.FUT,"KCBT"),
		CFE(SecSymbolType.FUT,"CFE"),
		CME(SecSymbolType.FUT,"CME"),
		CBOT(SecSymbolType.FUT,"CBOT"),
		CBT(SecSymbolType.FUT,"CBOT"),
		ECBOT(SecSymbolType.FUT,"ECBOT"),
		NYBOT(SecSymbolType.FUT,"NYBOT"),
		NYB(SecSymbolType.FUT,"NYBOT"),
		IPE(SecSymbolType.FUT,"IPE"),
		ICE(SecSymbolType.FUT,"ICE");
		
		String iBexchange;
		SecSymbolType symbolType;
		SecExchange(SecSymbolType symbolType,String ibExch){
			this.iBexchange = ibExch;
			this.symbolType = symbolType;
		}
		
		@Override
		public String toString() {
			// TODO Auto-generated method stub
			return iBexchange;
		}

		public String getIBexchange() {
			return iBexchange;
		}

		public SecSymbolType getSymbolType() {
			return symbolType;
		}
		
		public static SecExchange fromString(String exchString){
			return valueOf(exchString);
		}
		
	}
	
	public enum EquityExchange{
		SMART,
		ARCA,
		AMEX,
		CBOE,
		ISE,
		NYSE
		
	}

	public enum SecRight{
		C("C"),
		P("P");
		
		String putCall;
		SecRight(String putCall){
			this.putCall = putCall;
		}
		@Override
		public String toString() {
			return putCall;
		}
		
		public static SecRight fromString(String putCall){
			return valueOf(putCall);
		}
		
	}

	
	
	public enum DayType{
		BUSINESS_DAY,
		CALENDAR_DAY,
		NTH_MONDAY,
		NTH_TUESDAY,
		NTH_WEDNESDAY,
		NTH_THURSDAY,
		NTH_FRIDAY,
		IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY,
		IF_FRIDAY_APPLY_BUSINESS_DAY,
		IF_HOLIDAY_APPLY_BUSINESS_DAY,
		IF_WEEKEND_APPLY_BUSINESS_DAY,
		IF_FRIDAY_APPLY_CALENDAR_DAY,
		IF_HOLIDAY_APPLY_CALENDAR_DAY,
		IF_FRIDAY_BEFORE_COLUMBUS_DAY_APPLY_CALENDAR_DAY,
		IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
		IF_FRIDAY_AFTER_THANKSGIVING_APPLY_BUSINESS_DAY,
		IF_NOVEMBER_APPLY_BUSINESS_DAY,
		IF_DECEMBER_APPLY_CALENDAR_DAY,
		IF_DECEMBER_APPLY_BUSINESS_DAY,
		SAME_DAY,
		FULL_MONTH;

	}
	
	public enum InvalidShortNameCause{
		INVALID_YEAR,
		INVALID_MONTH,
		INVALID_MONTHYEAR,
		INVALID_PRODUCT_SYMBOL,
		INVALID_TYPE,
		INVALID_EXCHANGE,
		INVALID_EXCHANGE_EXCHANGE_DOES_NOT_MATCH_FUTURES_SYMBOL,
		INVALID_EXCHANGE_EXCHANGE_IS_NOT_VALID_EQUITY_EXCHANGE,
		INVALID_CURRENCY,
		INVALID_PUTCALL,
		INVALID_STRIKE,
		INVALID_SHORTNAME_NOT_CURRENTLY_SUPPORTED,
		INVALID_SHORTNAME_NUM_FIELDS,
		INVALID_SHORTNAME_HAS_EXPIRED,
		VALID,
		INVALID_SHORTNAME_IS_NULL,
		INVALID_SHORTNAME_QUERY_TIME_IS_NULL,
		INVALID_SHORTNAME_CAUSE_UNKNOWN,
		;
	}

}
