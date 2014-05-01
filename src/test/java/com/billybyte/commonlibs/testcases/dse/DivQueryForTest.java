package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Map;


public class DivQueryForTest extends InputQuery<BigDecimal>{
	public DivQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
	}

	@Override
	public BigDecimal newValue(SecInputsInfo t) {
		return t.div;
	}
}
