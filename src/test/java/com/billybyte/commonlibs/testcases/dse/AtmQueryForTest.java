package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Map;


public class AtmQueryForTest extends InputQuery<BigDecimal>{
	public AtmQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
	}

	@Override
	public BigDecimal newValue(SecInputsInfo t) {
		return t.atm;
	}
}
