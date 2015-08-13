package com.billybyte.commonlibs.testcases.dse;

import java.util.Calendar;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.models.vanilla.BachelierEd;
import com.billybyte.dse.models.vanilla.VanOptBachelierEd;

import junit.framework.TestCase;

public class TestBachelierEd extends TestCase{
	
	public void test1(){
		double EDSpot = 99.47;
		double EDStrike = 99.50;
		double sigma = .34;
		double r = .0045;
		double days = 125;
		double T = days/365;
		/**
		 * c	0.065177565
		 * p	0.0951313677
		 */
		double call = BachelierEd.callPrice(EDSpot,EDStrike,sigma,r,T);
		double callDelta = BachelierEd.delta(0,EDSpot,EDStrike,T,sigma,r,0);
		double callVega = BachelierEd.vega(0,EDSpot,EDStrike,T,sigma,r,0);
		Utils.prt("call = price:" + call + " delta:"+callDelta + " vega:"+callVega);
		double put = BachelierEd.putPrice(EDSpot,EDStrike,sigma,r,T);
		double putDelta = BachelierEd.delta(1,EDSpot,EDStrike,T,sigma,r,0);
		double putVega = BachelierEd.vega(1,EDSpot,EDStrike,T,sigma,r,0);
		Utils.prt("put = " + put + " delta:"+putDelta + " vega:"+putVega);
		
		assertEquals(652.0, new Double(Math.round(call*10000)));
		assertEquals(459.0, new Double(Math.round(callDelta*1000)));
		assertEquals(23.0,  new Double(Math.round(callVega*10000)));
		
		assertEquals(951.0,  new Double(Math.round(put*10000)));
		assertEquals(-540.0,  new Double(Math.round(putDelta*1000)));
		assertEquals(23.0,  new Double(Math.round(putVega*10000)));
	}
	
	public void test2(){
		double EDSpot = 99.47;
		double EDStrike = 99.50;
		double sigma = .34;
		double r = .0045;
		double days = 125;
		double T = days/365;

		VanOptBachelierEd vanBach = 
				new VanOptBachelierEd(Calendar.getInstance(), new VolDiot());
		double atm = EDSpot;
		double strike = EDStrike;
		double dte = T;
		double vol = sigma;
		double rate = r;
		double div = 0.0;
		Object[] others =null;
		double call = vanBach.getVanillaPrice(0, atm, strike, dte, vol, rate, div, others);
		double callDelta = vanBach.getVanillaDelta(0, atm, strike, dte, vol, rate, div, others);
		double callVega = vanBach.getVanillaVega(0, atm, strike, dte, vol, rate, div, others);
		Utils.prt("call = price:" + call + " delta:"+callDelta + " vega:"+callVega);

		
		double put = vanBach.getVanillaPrice(1, atm, strike, dte, vol, rate, div, others);
		double putDelta = vanBach.getVanillaDelta(1, atm, strike, dte, vol, rate, div, others);
		double putVega = vanBach.getVanillaVega(1, atm, strike, dte, vol, rate, div, others);
		Utils.prt("put = " + put + " delta:"+putDelta + " vega:"+putVega);
		
		assertEquals(652.0, new Double(Math.round(call*10000)));
		assertEquals(459.0, new Double(Math.round(callDelta*1000)));
		assertEquals(23.0,  new Double(Math.round(callVega*10000)));
		
		assertEquals(951.0,  new Double(Math.round(put*10000)));
		assertEquals(-540.0,  new Double(Math.round(putDelta*1000)));
		assertEquals(23.0,  new Double(Math.round(putVega*10000)));

	}
}
