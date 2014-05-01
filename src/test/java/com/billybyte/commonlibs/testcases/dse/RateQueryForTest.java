package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Map;


public class RateQueryForTest extends InputQuery<BigDecimal>{
	public RateQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
	}

	@Override
	public BigDecimal newValue(SecInputsInfo t) {
		return t.rate;
	}
}
