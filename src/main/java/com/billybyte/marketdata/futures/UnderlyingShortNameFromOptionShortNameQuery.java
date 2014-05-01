package com.billybyte.marketdata.futures;


import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public class UnderlyingShortNameFromOptionShortNameQuery implements QueryInterface<String, String> {
	private final QueryInterface<String, SecDef> sdQuery;
	private final FuturesProductQuery fplQuery = new FuturesProductQuery();
	private final Calendar evalDate;
	
	public UnderlyingShortNameFromOptionShortNameQuery(
			QueryInterface<String, SecDef> sdQuery,Calendar evalDate) {
		super();
		if(sdQuery==null){
			this.sdQuery = new SecDefQueryAllMarkets();
		}else{
			this.sdQuery = sdQuery;
		}
		this.evalDate = evalDate;
	}

	public UnderlyingShortNameFromOptionShortNameQuery(
			QueryInterface<String, SecDef> sdQuery){
		this(sdQuery, Calendar.getInstance());
	}

	@Override
	public String get(String key, int timeoutValue, TimeUnit timeUnitType) {
		SecDef optionSd = sdQuery.get(key, timeoutValue, timeUnitType);
		if(optionSd.getSymbolType()==SecSymbolType.FUT  || optionSd.getSymbolType()==SecSymbolType.STK ){
			return key;
		}
		SecDef ret = MarketDataComLib.getUnderylingSecDefFromOptionSecDef(
				optionSd, sdQuery, fplQuery, evalDate, timeoutValue, timeUnitType);
		if(ret!=null)return ret.getShortName();
		return null;
	}
	
	
}
