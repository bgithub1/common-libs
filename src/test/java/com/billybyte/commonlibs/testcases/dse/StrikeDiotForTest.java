package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;

import com.billybyte.dse.inputs.diotypes.DioType;

public class StrikeDiotForTest extends DioType<BigDecimal>{
	private static String key = "STRIKE_DIOT";
	public StrikeDiotForTest() {
		super(key);
	}
}