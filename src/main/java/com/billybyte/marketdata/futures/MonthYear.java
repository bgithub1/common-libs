package com.billybyte.marketdata.futures;

import java.util.ArrayList;
import java.util.Calendar;

import com.billybyte.marketdata.Year;

/**
 * @author msl1
 * For generation of pairs, strips, etc., see @link NonRecursiveSpreadGenerator
 */
public class MonthYear {

	private Month month;
	private Year year;
	
	
	// FIXME better to use orderController.getTime(), if I could figure out how to expose it here
	private static int currentYear = Calendar.getInstance().get(Calendar.YEAR);
	
	public MonthYear(Year year, Month month) {
		
		try { 
		
			this.year = year;
			this.month = month;
		} catch( NumberFormatException ex) {
				ex.printStackTrace();
		}
	}
	public MonthYear(int year, Month month)
	{
		this.year = Year.getYear(year);
		
		this.month = month;
	}
	
	public MonthYear(int monthYear) {
		this.year = Year.getYear(monthYear/100);
		
		this.month = Month.getMonth(monthYear%100);
	}
	
	public MonthYear(String MmmYyFormat){
		this(new Integer(MmmYyFormat.trim().substring(3,5)).intValue()+2000,
				Month.valueOf(MmmYyFormat.trim().substring(0,3))
				);
	}
	
	public String nymexNotation()
	{
		return (month.nymNotation() + "" + year.getShortNotation());
	}

	@Override
	public String toString() {
		return this.nymexNotation();
	}

	
	public static void setCurrentYear(int year) {
		currentYear = year;
	}
	
	public static int getCurrentYear() {
		return currentYear;
	}
	
	public static  MonthYear getMonthYear(String nymexNotation)
	{
		
		String monthYear = nymexNotation.substring(nymexNotation.length()-2, nymexNotation.length());
		Month month = Month.getMonth(monthYear.substring(0, 1));
		int myYear = Integer.parseInt(monthYear.substring(1));
		// Assume that last year's contracts are still in the system
		if(myYear < currentYear%10  )
			myYear = currentYear - currentYear%10 + 10 + myYear;
		else
			myYear = currentYear - currentYear%10 + myYear;
					
		MonthYear retMonthYear = new MonthYear(myYear, month);
		return retMonthYear;
	}
	
	public static void resetCurrentYear( ){
		currentYear = Calendar.getInstance().get(Calendar.YEAR);
	}


	public Month getMonth() {
		return month;
	}
	public String getYear() {
		return String.valueOf(year.getFullYear());
	}
	public Year getYearObject () {
		return year;
	}
	public int getYearInt() {
		return year.getFullYear();
	}
	public int getMonthYearInt()
	{
		try {
			if (month == null) {
				System.out.println("MonthYear.java Month is null... returning -1 ");
				return -1;
			}
			if (year == null) {
				System.out.println("MonthYear.java Year is null... returning -1  ");
				return -1;
			}
			String m =  (month.getIndex() >= 10)? ""+month.getIndex(): "0"+month.getIndex();
			String y = String.valueOf(year.getFullYear());
			return Integer.parseInt(y+m);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public int getMonthYearStartInt()
	{
		return year.getFullYear()*100+ month.getFirstIndex();
	}
	public int getMonthYearEndInt()
	{
		return year.getFullYear()*100+ month.getLastIndex();
	}

	public MonthYear next() {
		if( month.getLastIndex() < 12)
			return new MonthYear(year.getFullYear(),month.next());
		else 
			return new MonthYear(year.getFullYear()+1,month.next());
			

	}
	
	public MonthYear previous() {
		if( month.getFirstIndex() >1)
			return new MonthYear(year.getFullYear(),month.previous());
		else 
			return new MonthYear(year.getFullYear()-1,month.previous());
	
	}
	
	
	public MonthYear plus(int periods) {
		MonthYear m = this;
		for( int i = 0; i < periods; i++)
			m = m.next();
		return m;
	}
	
	public MonthYear minus(int periods) {
		MonthYear m = this;
		for( int i = 0; i < periods; i++)
			m = m.previous();
		return m;
	}
	
	public int daysInMonth() {
		// Feb, Q1, Cal all have leapdays != nonLeapDays
		if((year.getFullYear())%4 == 0)
			return month.leapDays();
		else 
			return month.nonLeapDays();
	}
	public int minus(MonthYear otherExpiry) {
		int yearDiff = Integer.valueOf(this.year.getFullYear()) - Integer.valueOf(otherExpiry.year.getFullYear());
		return yearDiff*12 + month.getLastIndex() - otherExpiry.month.getLastIndex();
	}
	
	public static MonthYear[] monthsBetweenInclusive(MonthYear m1, MonthYear m2) {
		if( m2.minus(m1) < 0 ) return null;
		int range = m2.minus(m1)+1;
		int start = m1.getMonthYearStartInt();
		MonthYear[] months = new MonthYear[range];
		months[0] = new MonthYear(start);
		for( int i = 1;i<range;i++) {
			
			months[i] = months[i-1].next();
		}
		return months;
	}
	
	public static MonthYear[] monthsBetweenInclusiveSansMonths(MonthYear m1, MonthYear m2, String monthsToInclude) {
		if( m2.minus(m1) < 0 ) return null;
		int range = m2.minus(m1)+1;
		int start = m1.getMonthYearStartInt();
		MonthYear[] months = new MonthYear[range];
		months[0] = new MonthYear(start);
		for( int i = 1;i<range;i++) {
			months[i] = months[i-1].next();
		}
		ArrayList<MonthYear> goodMonthYr = new ArrayList<MonthYear>();
		for(MonthYear monthYr : months)
		{
			
			String monthName = monthYr.toString();
			String monthCode = monthName.substring(0,1);
			if(monthsToInclude.contains(monthCode))
				goodMonthYr.add(monthYr);
			
		}
		return goodMonthYr.toArray(new MonthYear[goodMonthYr.size()]);
	}
	

	public boolean equals(MonthYear m) {
		return this.year.getFullYear() == m.year.getFullYear() && this.month == m.month;
	}
	
	@Override
	public boolean equals(Object o) {
		return equals((MonthYear) o);
	}

}
