package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.marketdata.SecDef;

public class StrikeQueryForTest extends InputQuery<BigDecimal>{
	public StrikeQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
		// TODO Auto-generated constructor stub
	}

	@Override
	public BigDecimal newValue(SecInputsInfo t) {
		String sn = t.shortName;
		SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
		
		return sd.getStrike()==null?BigDecimal.ZERO : sd.getStrike(); 
	}
}