package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;


public class DteQueryForTest extends InputQuery<BigDecimal>{
	QueryInterface<String, SecDef> sdQuery = 
			new SecDefQueryAllMarkets();
	private final BigDecimal daysInYear = new BigDecimal("365");
	public DteQueryForTest(Map<String, SecInputsInfo> testDataMap) {
		super(testDataMap);
	}

	@Override
	public BigDecimal newValue(SecInputsInfo t) {
		if(t.dte==null){
			String sn = t.shortName;
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			return new BigDecimal(MarketDataComLib.getDteFromSd(Calendar.getInstance(), sd));
		}
		return t.dte.divide(daysInYear,4,RoundingMode.HALF_EVEN);
	}
}
