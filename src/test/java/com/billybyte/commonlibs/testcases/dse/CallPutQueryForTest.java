package com.billybyte.commonlibs.testcases.dse;


import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;


public class CallPutQueryForTest extends InputQuery<Double>{
	private static final QueryInterface<String, SecDef> sdQuery = 
			new SecDefQueryAllMarkets();
	
	public CallPutQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
	}

	@Override
	public Double newValue(SecInputsInfo t) {
		String sn = t.shortName;
		SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
		double cpd = 0;
		String cp = sd.getRight();
		if(cp!=null && !MarketDataComLib.isCall(sd)){
			cpd=1.0;
		}
		return cpd;
	}
}
