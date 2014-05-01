package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Map;

import com.billybyte.marketdata.MarketDataComLib;

public class VolQueryForTest extends InputQuery<BigDecimal>{
	
	public VolQueryForTest(
			Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
	}

	@Override
	public BigDecimal newValue(SecInputsInfo t) {
		if(t.vol==null){
			/// get underlying vol
			String[] parts = t.shortName.split("\\.");
			if(parts[1].compareTo("OPT")==0){
				String sn = 
						parts[0]+MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR+
						"STK.SMART";
				return testDataMap.get(sn).vol;
			}
			return null;
		}
		return t.vol;
	}
}