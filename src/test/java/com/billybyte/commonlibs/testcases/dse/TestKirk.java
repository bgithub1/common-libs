package com.billybyte.commonlibs.testcases.dse;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.models.spread.KirkSpreadApproximation;

import junit.framework.TestCase;

public class TestKirk extends TestCase{

	public void testModel(){
		// test corrRisk function
		double callPutForCall = 0;
		double callPutForPut = 1.0;
		double F_1 = 4.241 ; // future 1
		double F_2 = 4.541 ; // future 2
		double X = -.35; // spread
		double V_1 = .294; // vol F_1
		double V_2 = .281; // vol F_2
		double I = .0015; // interest
		double T = 169.0/365; // time in years
		double corrInput = .976 ; // initial correlation
		double impliedCorr = 
				KirkSpreadApproximation.corrRisk(callPutForCall, F_1, F_2, X, 
				V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Implied Correlation: "+impliedCorr);
		
		double priceCall = 
				KirkSpreadApproximation.optPrice(
						callPutForCall, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Call Price: "+priceCall);
	
		double pricePut = 
				KirkSpreadApproximation.optPrice(
						callPutForPut, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Put Price: "+pricePut);
		
		double[] deltaCall = 
				KirkSpreadApproximation.delta(
						callPutForCall, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Call Deltas: F1: "+
						deltaCall[0] + "F2: " +  deltaCall[1]);
		
		double[] deltaPut = 
				KirkSpreadApproximation.delta(
						callPutForPut, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Put Deltas: F1: "+
				deltaPut[0] + "F2: " +  deltaPut[1]);

		double[] gammaCall = 
				KirkSpreadApproximation.gamma(
						callPutForCall, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Call Gammas: F1: "+
				gammaCall[0] + "F2: " +  gammaCall[1]);

		double[] gammaPut = 
				KirkSpreadApproximation.gamma(
						callPutForPut, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Put Gammas: F1: "+ 
				gammaPut[0] + "F2: " +  gammaPut[1]);

		double vegaCall = 
				KirkSpreadApproximation.optPrice(
						callPutForCall, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Call vega: " + vegaCall);


		double[] vegaPut = 
				KirkSpreadApproximation.vega(
						callPutForPut, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Put vega: " + vegaPut);

		double thetaCall = 
				KirkSpreadApproximation.theta(
						callPutForCall, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Call theta: " + thetaCall);


		double thetaPut =
				KirkSpreadApproximation.theta(
						callPutForPut, F_1, F_2, X, 
						V_1, V_2, I, T, corrInput);
		Utils.prtObMess(this.getClass(), "Put theta: " + thetaPut);


		assertEquals(1035,Math.round(priceCall*10000));
		assertEquals(535,Math.round(pricePut*10000));
		assertEquals(6117,Math.round(deltaCall[0]*10000));
		assertEquals(-5960,Math.round(deltaCall[1]*10000));
		assertEquals(-3876,Math.round(deltaPut[0]*10000));
		assertEquals(4033,Math.round(deltaPut[1]*10000));
		assertEquals(249,Math.round(gammaCall[0]*10000));
		assertEquals(260,Math.round(gammaCall[1]*10000));
		assertEquals(249,Math.round(gammaPut[0]*10000));
		assertEquals(260,Math.round(gammaPut[1]*10000));

		assertEquals(1035,Math.round(vegaCall*10000));
		assertEquals(-2,Math.round(thetaPut*10000));
	
	}
	
	
	
	
}
