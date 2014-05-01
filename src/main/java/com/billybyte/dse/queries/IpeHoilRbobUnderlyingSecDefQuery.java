package com.billybyte.dse.queries;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.futures.FuturesProductQuery;

public class IpeHoilRbobUnderlyingSecDefQuery implements QueryInterface<String, List<SecDef>>{
	public static final String REGEX_GET_STRING = "((RBOB)|(HOIL))\\.((FOP)|(FUT))";
	QueryInterface<String, List<SecDef>> underlyingSdQuery ;
	
	public IpeHoilRbobUnderlyingSecDefQuery(final QueryInterface<String, SecDef>sdQuery){
		final  FuturesProductQuery fpq = new FuturesProductQuery();
		this.underlyingSdQuery  = 
				new QueryInterface<String, List<SecDef>>() {
					@Override
					public List<SecDef> get(String key, int timeoutValue, TimeUnit timeUnitType) {
						SecDef sd = sdQuery.get(key, timeoutValue, timeUnitType);
						if(sd==null)return null;
						SecDef underSd = MarketDataComLib.getUnderylingSecDefFromOptionSecDef(
								sd, sdQuery, fpq, Calendar.getInstance(), timeoutValue, timeUnitType);
						return Arrays.asList(new SecDef[]{underSd});
					}
				}
		;
	}
	
	@Override
	public List<SecDef> get(String key, int timeoutValue, TimeUnit timeUnitType) {
		return underlyingSdQuery.get(key, timeoutValue, timeUnitType);
	}

}
