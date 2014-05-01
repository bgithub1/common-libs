package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;

import com.billybyte.dse.inputs.diotypes.DioType;

public class  VolDiotForTest extends DioType<BigDecimal>{
	private static String key = "VOL_DIOT";
	public VolDiotForTest() {
		super(key);
	}
}