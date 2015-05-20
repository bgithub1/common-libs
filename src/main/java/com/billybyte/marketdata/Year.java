package com.billybyte.marketdata;

import java.util.HashMap;

public enum Year {

	TwoThousandSeven (7, 2007),
	TwoThousandEight (8, 2008),
	TwoThousandNine (9, 2009),
	TwoThousandTen (10, 2010),
	TwoThousandEleven(11, 2011),
	TwoThousandTwelve(12, 2012),
	TwoThousandThirteen(13,2013),
	TwoThousandFourteen(14,2014),
	TwoThousandFifteen(15,2015),
	TwoThousandSixteen(16,2016),
	TwoThousandSeventeen(17,2017),
	TwoThousandEighteen(18,2018),
	TwoThousandNineteen(19,2019),
	TwoThousandTwenty(20,2020),
	TwoThousandTwentyOne(21,2021),
	TwoThousandTwentyTwo(22,2022),
	TwoThousandTwentyThree(23,2023),
	TwoThousandTwentyFour(24,2024),
	TwoThousandTwentyFive(25,2025);
	
	
	
	private final int shortNameNotation;
	private final int fullYear;
	
	private static final HashMap<Integer, Year> yearToYear = new HashMap<Integer, Year>();
	static {
		for (Year year : Year.values()) {
			if (Year.yearToYear.containsKey(year.fullYear)) {
					Year.yearToYear.remove(year.fullYear);
					System.out.println("ERROR Year enum contains duplicate years");				
					System.err.println("ERROR Year enum contains duplicate years");
					System.err.flush();
					break;
			} else {
				Year.yearToYear.put(year.fullYear, year);
			} 
			
		}
	}
	Year(int shortNameNotation, int year) {
		this.shortNameNotation = shortNameNotation;
		this.fullYear = year;
	}
	
	public static Year getYear(int year) {
		Year yr = yearToYear.get(year);
		if (yr == null )
			throw new NullPointerException("Year enum is null for year "+year);
		return yearToYear.get(year);
	}
	
	public static Year getYear(String year) {
		
		return getYear(Integer.parseInt(year));
	}
	
	public int getShortNotation() {
		return this.shortNameNotation%10;
	}
	
	public int getFullYear() {
		return this.fullYear;
	}
	
}
