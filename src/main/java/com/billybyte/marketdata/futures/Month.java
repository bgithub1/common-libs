package com.billybyte.marketdata.futures;

import java.util.HashMap;


public enum Month {
	
	Jan ( 'F', 31, 31),
	Feb ( 'G', 28, 29),
	Mar ( 'H', 31, 31),
	Apr ( 'J', 30, 30),
	May ( 'K', 31, 31),
	Jun ( 'M', 30, 30),
	Jul ( 'N', 31, 31),
	Aug ( 'Q', 31, 31),
	Sep ( 'U', 30, 30),
	Oct ( 'V', 31, 31),
	Nov ( 'X', 30, 30),
	Dec ( 'Z', 31, 31),
	Q1 ("F:H", 90, 91),
	Q2 ("J:M", 91, 91),
	Q3 ("N:U", 92, 92),
	Q4 ( "V:Z", 92, 92),
	Cal ("F:Z", 365, 366);
	

	public static final String[] monthCodesArray = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
	public static final String[] fullMonthNameArray = 
		{"January","February","March","April","May","June","July","August","September","October","November","December"};

	
	private final String NYMEX_NOTATION; //aka V for October
	private final int DAYS_IN_MONTH_NON_LEAP; 
	private final int DAYS_IN_MONTH_LEAP;
	private static final HashMap<String, Month> monthSymbolToMonth = new HashMap<String, Month>();
	private static final HashMap<String, String> monthToMonthSymbol = new HashMap<String, String>();
	static{
		for(Month month : Month.values())
		{
			Month.monthSymbolToMonth.put(month.nymNotation(), month);
			monthToMonthSymbol.put(month.name(), month.NYMEX_NOTATION);
		}
		
	}
	Month( char nymNotation, int nonLeapDays,  int leapDays)
	{
		
		this.NYMEX_NOTATION = ""+ nymNotation;
		this.DAYS_IN_MONTH_NON_LEAP = nonLeapDays;
		this.DAYS_IN_MONTH_LEAP = leapDays;
		
	}
	Month( String nymNotation, int nonLeapDays,  int leapDays)
	{
		
		this.NYMEX_NOTATION =  nymNotation;
		this.DAYS_IN_MONTH_NON_LEAP = nonLeapDays;
		this.DAYS_IN_MONTH_LEAP = leapDays;
	}
	
	public int nonLeapDays() { return DAYS_IN_MONTH_NON_LEAP; }
	public int leapDays() { return DAYS_IN_MONTH_LEAP; }
	public String iceNotation() { return this.name(); }
	public String nymNotation() { return NYMEX_NOTATION; }
	public static int returnDaysForYear(int year, Month month)
	{
		if(month != Feb)
			return month.DAYS_IN_MONTH_NON_LEAP;
		else if(year%4 == 0)
			return month.DAYS_IN_MONTH_LEAP;
		else return month.DAYS_IN_MONTH_NON_LEAP;
			
	}
	
	
	public static Month getMonth(int month)
	{	
		if(month > 0 && month<13)
			return Month.values()[month-1];
		else 
			return null;
	}
	
	public int getIndex() {
		return getLastIndex();
	}
	
	public int getFirstIndex() {
		if( ordinal() < 12 ) return ordinal() + 1;
		switch( this ) {
		case Q1:
			return 1;
		case Q2:
			return 4;
		case Q3:
			return 7;
		case Q4:
			return 10;
		case Cal:
			return 1;
		}
		return 0;
		
	}
	public int getLastIndex() {
		if( ordinal() < 12 ) return ordinal() + 1;
		switch( this ) {
		case Q1:
			return 3;
		case Q2:
			return 6;
		case Q3:
			return 9;
		case Q4:
			return 12;
		case Cal:
			return 12;
		}
		return 0;
		
	}
	
	public boolean isStrip() {
		return getLastIndex() - getFirstIndex() > 0;
	}
	
	public Month next() {
		switch( this ) {
		case Q1: return Q2;
		case Q2: return Q3;
		case Q3: return Q4;
		case Q4: return Q1;
		case Cal: return Cal;
		case Dec: return Jan;
		default: return Month.getMonth(ordinal()+2);
		}
	}
	public Month previous() {
		switch( this ) {
		case Q1: return Q4;
		case Q2: return Q1;
		case Q3: return Q2;
		case Q4: return Q3;
		case Cal: return Cal;
		case Jan: return Dec;
		default: return Month.getMonth(ordinal());
		}
	}
	public static Month getMonth(String month)
	{
		return Month.monthSymbolToMonth.get(month);
	}
	public static String getMonthSymbolByMonthName(String month) {
		return monthToMonthSymbol.get(month);
	}
	public static String getMonthCharFromIndex(int index) {
		switch(index) {
		case 1 :
			return "F";
		case 2 :
			return "G";
		case 3 : 
			return "H";
		case 4 :
			return "J";
		case 5 :
			return "K";
		case 6 :
			return "M";
		case 7 :
			return "N";
		case 8 :
			return "Q";
		case 9 :
			return "U";
		case 10 :
			return "V";
		case 11 :
			return "X";
		case 12 :
			return "Z";
			
			
		}
		return null;
	}
	
	public static int getIntMonth(char month)
	{

		int realMonth = 0;
		switch(month)
		{
		case('F'):
			realMonth = 1;
		break;
		case('G'):
			realMonth = 2;
		break;
		case('H'):
			realMonth = 3;
		break;
		case('J'):
			realMonth = 4;
		break;
		case('K'):
			realMonth = 5;
		break;
		case('M'):
			realMonth = 6;
		break;
		case('N'):
			realMonth = 7;
		break;
		case('Q'):
			realMonth = 8;
		break;
		case('U'):
			realMonth = 9;
		break;
		case('V'):
			realMonth = 10;
		break;
		case('X'):
			realMonth = 11;
		break;
		case('Z'):
			realMonth = 12;
		break;
		}
		return realMonth;
		
		
	}
	public static String getStringIntMonth(char month)
	{

		String realMonth = null;
		switch(month)
		{
		case('F'):
			realMonth = "01";
		break;
		case('G'):
			realMonth = "02";
		break;
		case('H'):
			realMonth = "03";
		break;
		case('J'):
			realMonth = "04";
		break;
		case('K'):
			realMonth = "05";
		break;
		case('M'):
			realMonth = "06";
		break;
		case('N'):
			realMonth = "07";
		break;
		case('Q'):
			realMonth = "08";
		break;
		case('U'):
			realMonth = "09";
		break;
		case('V'):
			realMonth = "10";
		break;
		case('X'):
			realMonth = "11";
		break;
		case('Z'):
			realMonth = "12";
		break;
		}
		return realMonth;
		
		
	}
	
	public static String getMonthName(char month)
	{
		String realMonth = null;
		switch(month)
		{
		case('F'):
			realMonth = "Jan";
		break;
		case('G'):
			realMonth = "Feb";
		break;
		case('H'):
			realMonth = "Mar";
		break;
		case('J'):
			realMonth = "Apr";
		break;
		case('K'):
			realMonth = "May";
		break;
		case('M'):
			realMonth = "Jun";
		break;
		case('N'):
			realMonth = "Jul";
		break;
		case('Q'):
			realMonth = "Aug";
		break;
		
		case('U'):
			realMonth = "Sep";
		break;
		case('V'):
			realMonth = "Oct";
		break;
		case('X'):
			realMonth = "Nov";
		break;
		case('Z'):
			realMonth = "Dec";
		break;
		}
		return realMonth;
		
	}
	
}
