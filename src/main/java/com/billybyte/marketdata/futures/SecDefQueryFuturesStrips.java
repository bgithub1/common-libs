package com.billybyte.marketdata.futures;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;

public class SecDefQueryFuturesStrips implements QueryInterface<String, SecDef>{
	private final QueryInterface<String, SecDef> outRightQuery;
	private final Object getLock = new Object();
	
	@SuppressWarnings("unused")
	private SecDefQueryFuturesStrips() {
		super();
		this.outRightQuery=null;
	}

	public SecDefQueryFuturesStrips(
			QueryInterface<String, SecDef> outRightQuery) {
		super();
		this.outRightQuery = outRightQuery;
	}

	@Override
	public SecDef get(String key, int timeoutValue, TimeUnit timeUnitType) {
		synchronized (getLock) {
			String[] legs = key.split("[:]");
			if (legs.length < 2) {
				//			Utils.prtErr(this.getClass().getName()+" wrong number of legs in shortName: "+key);
				return null;
			}
			List<SecDef> secDefList  =  
				MarketDataComLib.getAllOutRightShortNamesInclusive(outRightQuery, key, timeoutValue, timeUnitType);

			try {
				BigDecimal[] ones = arrayOfBigDecimalValues(secDefList.size(), BigDecimal.ONE);
				SecDefFuturesStrip sdfcc = new SecDefFuturesStrip(
						secDefList.toArray(new SecDef[0]),
						ones,ones);
				return sdfcc;
			} catch (Exception e) {
				//			Utils.prtErr(e.getMessage());
			}
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static final BigDecimal[] arrayOfBigDecimalValues(int arraySize, BigDecimal fillValue){
		BigDecimal[] ret = new BigDecimal[arraySize];
		Arrays.fill(ret, fillValue);
		return ret;
	}

	
}
