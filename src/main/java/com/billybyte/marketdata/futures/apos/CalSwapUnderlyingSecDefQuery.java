package com.billybyte.marketdata.futures.apos;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.futures.FuturesProductQuery;

public class CalSwapUnderlyingSecDefQuery implements QueryInterface<String, List<SecDef>>{
	public static final String REGEX_GET_STRING = "((CSX)|(AAO)|(AOX))\\.((FOP)|(FUT))";
	final QueryInterface<String, SecDef> sdQuery;
	final NymexAveragePriceSecDefQuery apSecDefQuery;
	final FuturesProductQuery fpq;

	public CalSwapUnderlyingSecDefQuery(){
		sdQuery= new SecDefQueryAllMarkets();
		apSecDefQuery = new NymexAveragePriceSecDefQuery();
		fpq =  new FuturesProductQuery();
	}
	
	@Override
	public List<SecDef> get(String derivativeShortName, int timeoutValue,
			TimeUnit timeUnitType) {
		SecDef actualSd = sdQuery.get(derivativeShortName, timeoutValue, timeUnitType);
		if(actualSd==null){
			return null;
		}

		SecDef futureSd = MarketDataComLib.getUnderylingSecDefFromOptionSecDef(actualSd, sdQuery, 
				fpq, null, timeoutValue, timeUnitType);
		// ************* END  get the secDef for the actual deriv, and it's main underlying *****

		//************** get underlying settle *******************
		SecDef[] underlyingSecDefs = 
				apSecDefQuery.get(futureSd.getShortName(), timeoutValue, timeUnitType);
		//************** END get underlying settle *******************
		
		return CollectionsStaticMethods.listFromArray(underlyingSecDefs);
	}

}

