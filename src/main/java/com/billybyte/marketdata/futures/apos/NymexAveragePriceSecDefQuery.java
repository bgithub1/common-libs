package com.billybyte.marketdata.futures.apos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecLocale;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

import com.billybyte.queries.QueryFromRegexPattern;

/**
 * Returns secdef legs of AveragePrice stuff like NYMEX CSX or option AOX
 * @author bperlman1
 *
 */
public class NymexAveragePriceSecDefQuery implements QueryInterface<String, SecDef[]>{
	
	public NymexAveragePriceSecDefQuery() {
		super();
		this.regexQuery =			new QueryFromRegexPattern<String, String>(
				averagePricePartialNames,Arrays.asList(correspondingUnderSymbols));

	}

	
	private final static SecDefQueryAllMarkets sdQuery = new SecDefQueryAllMarkets();
	
	private final static String[]averagePricePartialNames = {
		"CSX.FUT.NYMEX.USD.",
		"AOX.FOP.NYMEX.USD.",
		"MPX.FUT.NYMEX.USD.",
		"ATX.FOP.NYMEX.USD.",
		"RLX.FUT.NYMEX.USD.",
		"RA.FOP.NYMEX.USD.",
	};
	
	private final static String[]correspondingUnderSymbols = {
		"CL.FUT.NYMEX.USD.",
		"CL.FUT.NYMEX.USD.",
		"HO.FUT.NYMEX.USD.",
		"HO.FUT.NYMEX.USD.",
		"RB.FUT.NYMEX.USD.",
		"RB.FUT.NYMEX.USD.",
	};
	
	private final QueryFromRegexPattern<String, String> regexQuery ;
	
	
	
	public static final String[] getAveragePricePartialNames(){
		return averagePricePartialNames;
	}
	
	@Override
	public SecDef[] get(String key, int timeoutValue, TimeUnit timeUnitType) {
		//***************** get the underlying contract that coincides with the first day 
		//   of the year and month of expiry of the Average Price Future or Option whose shortName is in 
		//   key                                                                         *********************
		// get the SecDef of actual Average Priced Future or Option
		SecDef sdActual = sdQuery.get(key, timeoutValue, timeUnitType);
		if(sdActual==null)return null;
		//  get a partial shortName for the underlying of this AP 
		String partialUnderlying = regexQuery.get(key,timeoutValue,timeUnitType);
		if(partialUnderlying==null)return null;
		// get parts of underlying
		String[] parts = partialUnderlying.split("\\.");
		if(parts.length<4)return null;
		String symbol = parts[0];
		SecSymbolType type = SecSymbolType.fromString(parts[1]);
		SecExchange exchange = SecExchange.fromString(parts[2]);
		SecCurrency currency = SecCurrency.fromString(parts[3]);
		List<SecDef> retList = new ArrayList<SecDef>();
		Calendar evalDate = Calendar.getInstance();
		// get a date that is the expiry year, month and the first of the month
		evalDate.set(sdActual.getExpiryYear(), sdActual.getExpiryMonth()-1,1);
		// get the SecDef of the spot contract for this first of the month
		SecDef spotSd = MarketDataComLib.getSpotContractPerBusinessDay(sdQuery, symbol, type, exchange, 
				currency, null, null, evalDate);
		//***************** get the underlying contract that coincides with the first day 
		//   of the year and month of expiry of the Average Price Future or Option whose shortName is in 
		//   END END END key  ************************************

		//**************** loop that adds all of the underlying SecDefs to the return list ******************
		// add this spot contract to return list
		retList.add(spotSd);
		// get local for add business days
		String locale = SecLocale.getLocaleFromSecDef(spotSd).toString();
		
		for(int i=0;i<31;i++){
			// get next business day after the current spot expiry
			Calendar nextBusDay = MarketDataComLib.getCalendarOfExpiryFromSecDef(spotSd);
			// and add a business day to it
			nextBusDay = Dates.addBusinessDays(locale, nextBusDay, 1);
			// see if the month of the next business is day is past the end of the month of the swap
			int monthOfNextBusDay = nextBusDay.get(Calendar.MONTH)+1;
			if(monthOfNextBusDay!=sdActual.getExpiryMonth()){
				// if so, break
				break;
			}
			// otherwise, get the next spot contract that exists for this month, for this underlying
			spotSd = MarketDataComLib.getSpotContractPerBusinessDay(sdQuery, symbol, type, exchange, 
					currency, null, null, nextBusDay);
			// add it to the list
			retList.add(spotSd);

			
		}
		//**************** END loop that adds all of the underlying SecDefs to the return list ******************
		return retList.toArray(new SecDef[]{});
	}
	
	
	
	
	public SecDef getActualSecDef(String key, int timeoutValue, TimeUnit timeUnitType) {
		return sdQuery.get(key, timeoutValue, timeUnitType);
	}
	
}
