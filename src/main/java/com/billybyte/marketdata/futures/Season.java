package com.billybyte.marketdata.futures;

import java.util.Calendar;

import java.util.Arrays;

import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.Dates;

public class Season {
	private final NavigableSet<Integer> monthYears = new TreeSet<Integer>();
	private final NavigableSet<Calendar> keyMonthYearCalendars = new TreeSet<Calendar>();

	public Season(int year, int[] monthPattern,Integer[] keyMonthYears){
		this(year,monthPattern,new TreeSet<Integer>(Arrays.asList(keyMonthYears)));
		
	}
	
	public Season(int year, int[] monthPattern,NavigableSet<Integer> keyMonthYears){
		int year100 = year*100;
		for(int mon:monthPattern){
			int yyyyMm = year100+mon;
			monthYears.add(yyyyMm);
			if(keyMonthYears.contains(yyyyMm)){
				keyMonthYearCalendars.add(calendarfromYyyyMm(yyyyMm));
			}
		}
	}
	
	public static Calendar calendarfromYyyyMm(int yyyyMm){
		Calendar c = Calendar.getInstance();
		c.set(yyyyMm/100, (yyyyMm % 100)-1,1);
		return c;
	}
	
	public boolean inSeason(int yyyyMm){
		return monthYears.contains(yyyyMm);
	}
	
	public Integer getKeyMonthProxy(int yyyyMm){
		if(!inSeason(yyyyMm))return null;
		Calendar c = calendarfromYyyyMm(yyyyMm);
		Calendar ceiling = keyMonthYearCalendars.ceiling(c);
		Calendar floor = keyMonthYearCalendars.floor(c);
		if(ceiling==null && floor==null)return null;
		long daysDiffFromCeiling = ceiling!=null ? Dates.getDifference(c,ceiling,TimeUnit.DAYS) : Long.MAX_VALUE;
		long daysDiffFromFloor = floor!=null ? Dates.getDifference(floor,c,TimeUnit.DAYS) : Long.MAX_VALUE;
		Integer ret;
		if(daysDiffFromFloor-daysDiffFromCeiling>10){
			ret = ceiling.get(Calendar.YEAR)*100+ceiling.get(Calendar.MONTH)+1;
		}else{
			ret = floor.get(Calendar.YEAR)*100+floor.get(Calendar.MONTH)+1;
		}
		return ret;
	}
	
	public NavigableSet<Integer> getSeasonComponents(){
		return new TreeSet<Integer>(this.monthYears);
	}
}
