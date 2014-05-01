package com.billybyte.commonlibs.testcases;

import java.net.URL;

import com.billybyte.marketdata.futures.MonthYear;



import junit.framework.TestCase;

public class TestGetResource extends TestCase{

	public void test1(){
		MonthYear my = new MonthYear(201401);
		Class<?> c = my.getClass();
		URL url = c.getResource("ExchangeProductList.csv");
		assertNotNull(url);
	}
}
