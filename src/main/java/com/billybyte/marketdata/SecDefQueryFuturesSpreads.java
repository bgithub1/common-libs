package com.billybyte.marketdata;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.futures.SecDefFuturesCalendarSpread;

public class SecDefQueryFuturesSpreads implements QueryInterface<String, SecDef>{
	private final QueryInterface<String, SecDef> outRightQuery;
	
	@SuppressWarnings("unused")
	private SecDefQueryFuturesSpreads() {
		super();
		this.outRightQuery=null;
	}

	public SecDefQueryFuturesSpreads(
			QueryInterface<String, SecDef> outRightQuery) {
		super();
		this.outRightQuery = outRightQuery;
	}

	@Override
	public SecDef get(String key, int timeoutValue, TimeUnit timeUnitType) {
		String[] legs = key.split("[-]");
		if(legs.length!=2){
			return null;
		}
		ArrayList<SecDef> secDefList = new ArrayList<SecDef>();
		for(String leg:legs){
			SecDef sd = outRightQuery.get(leg, timeoutValue, timeUnitType);
			if(sd ==null){
				return null;
			}
			secDefList.add(sd);
		}
		try {
			SecDefFuturesCalendarSpread sdfcc = new SecDefFuturesCalendarSpread(secDefList.toArray(new SecDef[0]));
			return sdfcc;
		} catch (Exception e) {
		}
		return null;
	}

}
