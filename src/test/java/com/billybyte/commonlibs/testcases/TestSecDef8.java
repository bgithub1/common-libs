package com.billybyte.commonlibs.testcases;

import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;

import junit.framework.TestCase;

/**
 * Test yyyyMmDdHh in the monthyear field
 * @author bperlman1
 *
 */
public class TestSecDef8 extends TestCase{
	public void test1(){
		String[] sns = {"IBM.OPT.SMART.USD.20130727.C.190.00",
				"IBM.OPT.SMART.USD.201307.C.190.00",
				"HLF.OPT.SMART.USD.201303.P.36.00",
				"HLF.OPT.SMART.USD.201304.P.36.00",
				"HLF.OPT.SMART.USD.201305.P.36.00",
				"HLF.OPT.SMART.USD.201306.P.36.00",
				"HLF.OPT.SMART.USD.201307.P.36.00",
				"HLF.OPT.SMART.USD.201308.P.36.00",
				"HLF.OPT.SMART.USD.201401.P.36.00",
				"DX.FOP.NYBOT.USD.201304.C.85.000"};
		SecDefQueryAllMarkets sdQuery = new SecDefQueryAllMarkets();
		for(String sn:sns){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			assertNotNull(sd);
			Utils.prt(sd.toString());
		}
		
	}
}
