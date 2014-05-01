package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;

import junit.framework.TestCase;

import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CallPutDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.VegaDerSen;

public class TestComparators extends TestCase {
	public void test1(){
		// sense interface
		DerivativeSensitivityTypeInterface ds1 = new DeltaDerSen();
		DerivativeSensitivityTypeInterface ds2 = new VegaDerSen();
		int diff = ds1.compareTo(ds2);
		assertEquals("DELTA".compareTo("VEGA"),diff);
		DioType<BigDecimal> atm = new AtmDiot();
		DioType<Double> cp = new CallPutDiot();
		assertEquals("ATM_DIOT".compareTo("CALLPUT_DIOT"),atm.compareTo(cp));
	}
}
