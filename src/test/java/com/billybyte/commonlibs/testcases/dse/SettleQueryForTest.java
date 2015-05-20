package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Map;

import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.marketdata.SettlementDataImmute;

public class SettleQueryForTest  extends InputQuery<SettlementDataInterface> {
	public SettleQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
		// TODO Auto-generated constructor stub
	}

	@Override
	public SettlementDataInterface newValue(SecInputsInfo testValue) {
		String shortName = testValue.shortName;
		BigDecimal price = testValue.atm;
		
		return new SettlementDataImmute(shortName, price, 1,1);
	}

}
