package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.marketdata.history.GetCorrelationsFromYahoo;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class YahooHistoricalVolCqrQuery implements QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> {
	private static final int DEFAULT_DAYS_FOR_STD = 120;
	private final Calendar lastestDayOfHistory ;
	private final Calendar earliestDayOfHistory;
	private final QueryInterface<String, SecDef> sdQuery;
	private final int daysPerYear;

	private final ConcurrentHashMap<String, ComplexQueryResult<BigDecimal>> cache = 
			new ConcurrentHashMap<String, ComplexQueryResult<BigDecimal>>();
	
	
	public YahooHistoricalVolCqrQuery(
			Calendar earliestDayOfHistory,
			Calendar lastestDayOfHistory, 
			QueryInterface<String, SecDef> sdQuery,
			int daysPerYear) {
		super();
		this.lastestDayOfHistory = lastestDayOfHistory;
		this.earliestDayOfHistory = 
				earliestDayOfHistory==null ? Dates.addToCalendar(lastestDayOfHistory, -1*DEFAULT_DAYS_FOR_STD, Calendar.DAY_OF_YEAR, true) : earliestDayOfHistory;
		this.sdQuery = sdQuery;
		this.daysPerYear = daysPerYear;
	}

	public YahooHistoricalVolCqrQuery(){
		this(null,Calendar.getInstance(),new SecDefQueryAllMarkets(),252);
	}


	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> stockKeySet,
			int timeoutValue, TimeUnit timeUnitType) {
		
		// Create return map
		Map<String, ComplexQueryResult<BigDecimal>>  ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		
		// Make sure that all keys are valid, and that they are stocks
		Map<String,String> stkNameToShortName = 
				new HashMap<String, String>();
		
		for(String stkKey : stockKeySet){
			SecDef sd = sdQuery.get(stkKey, timeoutValue, timeUnitType);
			if(sd==null){
				ret.put(stkKey,errRet(stkKey+" no SecDef"));
				continue;
			}
			if(sd.getSymbolType()!=SecSymbolType.STK){
				ret.put(stkKey,errRet(stkKey+" symbol is not an stock"));
				continue;
			}
			// See if it's cached
			if(cache.containsKey(stkKey)){
				ret.put(stkKey, cache.get(stkKey));
				continue;
			}

			String symbol = sd.getSymbol();
			stkNameToShortName.put(symbol, stkKey);
		}
		
		// make sure there is something to process
		if(stkNameToShortName.size()<1){
			return ret;
		}
		// Get returns in order to calculate standard deviations
		Tuple<List<String>, double[][]> returnTuple = 
				GetCorrelationsFromYahoo.getReturnsMatrixTuple(
						stkNameToShortName.keySet(),earliestDayOfHistory,lastestDayOfHistory,true);
		// do the standard deviation calc
		List<String> orderedNames = returnTuple.getT1_instance();
		double[][] returnsMatrix = returnTuple.getT2_instance();
		returnsMatrix = transpose(returnsMatrix);
		
		for(int i = 0;i<orderedNames.size();i++){
			double[] returns = returnsMatrix[i];
			double std = MathStuff.stdDev(returns);
			std = std * Math.sqrt(daysPerYear);
			ComplexQueryResult<BigDecimal> cqr = 
					new ComplexQueryResult<BigDecimal>(null, new BigDecimal(std));
			String stkName = orderedNames.get(i);
			String shortName = stkNameToShortName.get(stkName);
			ret.put(shortName,cqr);
		}
		
		cache.putAll(ret);
		return ret;
	}
	
	private final ComplexQueryResult<BigDecimal> errRet(String mess){
		Exception e =  Utils.IllState(this.getClass(), mess);
		return new ComplexQueryResult<BigDecimal>(e, null);
	}
	
	
	private final double[][] transpose(double[][] matrix){
		
		double ret[][] = new double[matrix[0].length][matrix.length];
		int m = matrix.length;
		for(int i = 0; i < m; i++) {
			int n = matrix[i].length;
			  for(int j = 0; j < n; j++) {
			    ret[j][i] = matrix[i][j];
			  }
		}
		return ret;
	}

	public static void main(String[] args) {
		int daysOfHist=120;
		Calendar lastestDayOfHistory = Calendar.getInstance();
		Calendar earliestDayOfHistory = 
				Dates.addToCalendar(
						lastestDayOfHistory, -1*daysOfHist, Calendar.DAY_OF_YEAR, true);
		YahooHistoricalVolCqrQuery histVolQuery = 
				new YahooHistoricalVolCqrQuery(
						earliestDayOfHistory,
						Calendar.getInstance(),
						new SecDefQueryAllMarkets(),
						252);
		
		String[] array = 
		{
			"IBM.STK.SMART",
			"IBM.OPT.SMART.USD.20170120.C.170.00",
			"AAPL.STK.SMART",
			"AAPL.OPT.SMART.USD.20170120.C.150.00",
			"MSFT.STK.SMART",
			"MSFT.OPT.SMART.USD.20170120.C.45.00",
			"GOOG.STK.SMART",
			"GOOG.OPT.SMART.USD.20170120.C.600.00",
		};
		Set<String> keySet = CollectionsStaticMethods.setFromArray(array);
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				histVolQuery.get(keySet,20,TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(ret);
		// do it again with cache
		Utils.prt("");
		Utils.prt("do it again from cache");
		ret = 
				histVolQuery.get(keySet,20,TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(ret);
	}
	
	

}
