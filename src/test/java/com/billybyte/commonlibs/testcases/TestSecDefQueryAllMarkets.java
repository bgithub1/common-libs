package com.billybyte.commonlibs.testcases;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.futures.SecDefFuturesStrip;

public class TestSecDefQueryAllMarkets extends TestCase{
	
	public void testStockOptions(){
		String baseName = "IBM.OPT.SMART.USD.MONTHYEAR.C.200.00";
		DecimalFormat dfMonth = new DecimalFormat("00");
		DecimalFormat dfYear = new DecimalFormat("0000");
		
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		TimeUnit tu = TimeUnit.MILLISECONDS;
		int timeoutValue= 2000;
		
		for(int year = 2013;year<2017;year++){
			for(int month = 1;month<=12;month++){
				String my = dfYear.format(year)+dfMonth.format(month);
				String shortName = baseName.replace("MONTHYEAR", my);
				SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
				assertNotNull(sd);
				System.out.println(sd);
			}
		}
		
	}

	
	public void test0(){
		String[] shortNames = {
				"IBM.STK.SMART",
		};
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		TimeUnit tu = TimeUnit.MILLISECONDS;
		int timeoutValue= 2000;
		for(String shortName:shortNames){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			assertNotNull(sd);
			System.out.println(sd);
		}
	

	}
	public void test1(){
		String[] shortNames = {
				"IBM.STK.SMART",
				"CL.FUT.NYMEX.USD.201112",
				"CL.FUT.NYMEX.USD.201201",
				"CL.FUT.NYMEX.USD.201203",
				"CL.FUT.NYMEX.USD.201205",
				"NG.FUT.NYMEX.USD.201112",
				"NG.FUT.NYMEX.USD.201201",
				"NG.FUT.NYMEX.USD.201203",
				"NG.FUT.NYMEX.USD.201205",
				"HO.FUT.NYMEX.USD.201112",
				"HO.FUT.NYMEX.USD.201201",
				"HO.FUT.NYMEX.USD.201203",
				"HO.FUT.NYMEX.USD.201205",
				"RB.FUT.NYMEX.USD.201112",
				"RB.FUT.NYMEX.USD.201201",
				"RB.FUT.NYMEX.USD.201203",
				"RB.FUT.NYMEX.USD.201205",
				"COIL.FUT.IPE.USD.201112",
				"COIL.FUT.IPE.USD.201201",
				"COIL.FUT.IPE.USD.201203",
				"COIL.FUT.IPE.USD.201205",
//				"WTI.FUT.IPE.USD.201112",
//				"WTI.FUT.IPE.USD.201201",
//				"WTI.FUT.IPE.USD.201203",
//				"WTI.FUT.IPE.USD.201205",
				"WTI.FUT.ICE.USD.201112",
				"WTI.FUT.ICE.USD.201201",
				"WTI.FUT.ICE.USD.201203",
				"WTI.FUT.ICE.USD.201205",
				"HOIL.FUT.IPE.USD.201112",
				"HOIL.FUT.IPE.USD.201201",
				"HOIL.FUT.IPE.USD.201203",
				"HOIL.FUT.IPE.USD.201205",
				"RBOB.FUT.IPE.USD.201112",
				"RBOB.FUT.IPE.USD.201201",
				"RBOB.FUT.IPE.USD.201203",
				"RBOB.FUT.IPE.USD.201205",
				"HO.FUT.NYMEX.USD.201201-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201202-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201203-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201204-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201205-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201206-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201207-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201208-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201209-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201210-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201211-HO.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201212-HO.FUT.NYMEX.USD.201301",
		};
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		TimeUnit tu = TimeUnit.MILLISECONDS;
		int timeoutValue= 2000;
		boolean allgood = true;
		for(String shortName:shortNames){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			if(sd==null){
				Utils.prt("sd for shortName: "+shortName+" is null");
				allgood = false;
			}
			
			System.out.println(sd);
		}
		assertTrue(allgood);

		// test futures spreads
		String[] spreads = {
				"CL.FUT.NYMEX.USD.201205-CL.FUT.NYMEX.USD.201206",
				"CL.FUT.NYMEX.USD.201205-CL.FUT.NYMEX.USD.201208",
				"CL.FUT.NYMEX.USD.201205-CL.FUT.NYMEX.USD.201306",
				"CL.FUT.NYMEX.USD.201205-CL.FUT.NYMEX.USD.201301",
				"NG.FUT.NYMEX.USD.201205-NG.FUT.NYMEX.USD.201206",
				"NG.FUT.NYMEX.USD.201205-NG.FUT.NYMEX.USD.201208",
				"NG.FUT.NYMEX.USD.201205-NG.FUT.NYMEX.USD.201306",
				"NG.FUT.NYMEX.USD.201205-NG.FUT.NYMEX.USD.201301",
				"NG.FUT.NYMEX.USD.201205-NG.FUT.NYMEX.USD.201301",
				"HO.FUT.NYMEX.USD.201304-HO.FUT.NYMEX.USD.201310",
				"RB.FUT.NYMEX.USD.201205-RB.FUT.NYMEX.USD.201301",
				"COIL.FUT.IPE.USD.201205-COIL.FUT.IPE.USD.201305",
				"WTI.FUT.ICE.USD.201205-WTI.FUT.ICE.USD.201305",
				"HOIL.FUT.IPE.USD.201205-HOIL.FUT.IPE.USD.201305",
				"RBOB.FUT.IPE.USD.201205-RBOB.FUT.IPE.USD.201305",
		};
		allgood = true;
		for(String shortName:spreads){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			if(sd==null){
				allgood=false;
				Utils.prtObErrMess(this.getClass(), shortName + " : has no secdef in test1");
				continue;
			}
			Utils.prt(sd.toString());
		}
		assertTrue(allgood);
		
		String[] badnames = {
				"HOIL.FUT.IPE.USD.201305-HOIL.FUT.IPE.USD.201205",
				"HOIL.FUT.IPE.USD.201205-HOIL.FUT.IPE.USD.201205",

		};
		for(String shortName:badnames){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			assertNull(sd);
			Utils.prt("bad: "+shortName);
		}

	
		SecDefFuturesStrip stripSd = (SecDefFuturesStrip)allMarketsQuery.get("HOIL.FUT.IPE.USD.201201:HOIL.FUT.IPE.USD.201212",timeoutValue,tu);
		assertEquals(12,stripSd.getSecDefLegs().length);
		assertEquals("HOIL.FUT.IPE.USD.201201",stripSd.getSecDefLegs()[0].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201202",stripSd.getSecDefLegs()[1].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201203",stripSd.getSecDefLegs()[2].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201204",stripSd.getSecDefLegs()[3].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201205",stripSd.getSecDefLegs()[4].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201206",stripSd.getSecDefLegs()[5].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201207",stripSd.getSecDefLegs()[6].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201208",stripSd.getSecDefLegs()[7].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201209",stripSd.getSecDefLegs()[8].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201210",stripSd.getSecDefLegs()[9].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201211",stripSd.getSecDefLegs()[10].getShortName());
		assertEquals("HOIL.FUT.IPE.USD.201212",stripSd.getSecDefLegs()[11].getShortName());
		Utils.prt(stripSd.toString());
		
		// test for options
		String[] futuresOptions = {
				"CL.FOP.NYMEX.USD.201205.C.100.00",
				"CL.FOP.NYMEX.USD.201205.P.100.00",
				"CL.FOP.NYMEX.USD.201201.C.120.00",
				"NG.FOP.NYMEX.USD.201205.C.4.500",
				"NG.FOP.NYMEX.USD.201205.P.4.500",
				"HO.FOP.NYMEX.USD.201205.C.2.1000",
				"HO.FOP.NYMEX.USD.201205.C.2.1000",
				"RB.FOP.NYMEX.USD.201205.C.2.1000",
				"RB.FOP.NYMEX.USD.201205.C.2.1000",
				"COIL.FOP.IPE.USD.201205.C.910.00",
				"COIL.FOP.IPE.USD.201205.P.910.00",
				"WTI.FOP.ICE.USD.201205.C.120.00",
				"WTI.FOP.ICE.USD.201205.P.120.00",
				"HOIL.FOP.IPE.USD.201205.C.2.1000",
				"HOIL.FOP.IPE.USD.201205.P.2.1000",
				"RBOB.FOP.IPE.USD.201205.C.2.1000",
				"RBOB.FOP.IPE.USD.201205.P.2.1000",
		};
		for(String shortName:futuresOptions){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			if(sd==null){
				allgood=false;
				Utils.prtObErrMess(this.getClass(), shortName + " : has no secdef in test1");
				continue;
			}
			Utils.prt(sd.toString());
		}
		assertTrue(allgood);

		

	
	}

	public void testFuturesOptionsWithOtherOptionsSymbols(){
		String[] shortNames = {
				"ON.FOP.NYMEX.USD.201205.C.5.200",
				"ON.FOP.NYMEX.USD.201205.C.4.100",
				"ON.FOP.NYMEX.USD.201205.P.4.100",
				"ON.FOP.NYMEX.USD.201205.P.3.250",
		};
		
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		TimeUnit tu = TimeUnit.MILLISECONDS;
		int timeoutValue= 2000;
		for(String shortName:shortNames){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			assertNotNull(sd);
			System.out.println(sd);
		}
	}
	
	public void testGEs(){
		String[] shortNames = {
				"GE.FUT.GLOBEX.USD.201212",
		};
		
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		TimeUnit tu = TimeUnit.MILLISECONDS;
		int timeoutValue= 2000;
		for(String shortName:shortNames){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			assertNotNull(sd);
			System.out.println(sd);
		}
		
	}
	
	public void testHyphen(){
		String[] shortNames = {
				"IBM.OPT.SMART.USD.201307.C.200.00",
				"BF-B.OPT.SMART.USD.201307.C.50.00",
				"BF-B.STK.SMART",
				"WPP_L.STK.SMART",
				"WPP_L.OPT.SMART.USD.201307.C.50.00",
				"AIM_TO.STK.SMART",
				"AIM_TO.OPT.SMART.USD.201307.C.50.00"
		};
		
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		TimeUnit tu = TimeUnit.MILLISECONDS;
		int timeoutValue= 2000;
		for(String shortName:shortNames){
			SecDef sd = allMarketsQuery.get(shortName,timeoutValue,tu);
			assertNotNull(sd);
			System.out.println(sd);
		}
		
		
	}
	
	public void testHyphenAndUnderscore(){
		SecDefQueryAllMarkets allMarketsQuery = new SecDefQueryAllMarkets();
		String key = "VOLVO-B_ST.STK.SMART";
		SecDef sd = allMarketsQuery.get(key, 1, TimeUnit.SECONDS);
		assertNotNull(sd);
	}
	
}
