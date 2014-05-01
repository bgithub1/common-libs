package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.InterestRates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.queries.ComplexQueryResult;

public class TreasuryRateSingleQuery implements QueryInterface<String,ComplexQueryResult<BigDecimal>>{

	private final QueryInterface<String,SecDef> sdQuery;
	private final TreeMap<Integer,BigDecimal> rateTable;
	
	public TreasuryRateSingleQuery(QueryInterface<String,SecDef> sdQuery, TreeMap<Integer,BigDecimal> rateTable){
		this.sdQuery = sdQuery;
		this.rateTable = rateTable;
	}

	/**
	 * 
	 * @param sdQuery
	 * @param rateTablePathOrFileNameIfResource - if the file is in the File System : 
	 * 		rateTablePathOrFileNameIfResource is a full path and
	 * 		classOfPackage = null;
	 * @param classOfPackage if the file is NOT in the File System : 
	 * 		rateTablePathOrFileNameIfResource a fileName (no other path info) and
	 * 		classOfPackage = any class that is the same package as the rateTable.xml file;
	 */
	public TreasuryRateSingleQuery(
			QueryInterface<String,SecDef> sdQuery, 
			String rateTablePathOrFileNameIfResource,
			Class<?> classOfPackage){
		this.sdQuery = sdQuery;
		this.rateTable = Utils.getXmlData(TreeMap.class, classOfPackage, rateTablePathOrFileNameIfResource);
	}

	
	@Override
	public ComplexQueryResult<BigDecimal> get(String key, int timeoutValue, TimeUnit timeUnitType) {
		SecDef sd = sdQuery.get(key, timeoutValue, timeUnitType);
		if(sd!=null){
			// TODO making new calendar instance here, should this be passed in??
			Long longDays =  sd.getSymbolType()==SecSymbolType.STK ? 1 : 
					MarketDataComLib.getDaysToExpirationFromSd( Calendar.getInstance(),sd);
			Integer days = Integer.parseInt(longDays.toString());
			double rate = InterestRates.interpolateLinearFromRateTable(rateTable, days);
			return new ComplexQueryResult<BigDecimal>(null, new BigDecimal(rate));
		} else {
			Exception e = Utils.IllArg(this.getClass(), "Could not find SecDef for "+key);
			return new ComplexQueryResult<BigDecimal>(e, null);
		}
	}

}
