package com.billybyte.dse.debundles;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CallPutDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.DivDiot;
import com.billybyte.dse.inputs.diotypes.DteSimpleDiot;
import com.billybyte.dse.inputs.diotypes.RateDiot;
import com.billybyte.dse.inputs.diotypes.SettlePriceDiot;
import com.billybyte.dse.inputs.diotypes.StrikeDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.queries.AtmFromPdiCqrQuery;
import com.billybyte.dse.queries.BaseUnderlyingSecDefQuery;
import com.billybyte.dse.queries.CallPutInputQuery;
import com.billybyte.dse.queries.DivDseInputQuery;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.dse.queries.DteFromPdiInputQuery;
import com.billybyte.dse.queries.SettleDseInputQueryFromPdiQuery;
import com.billybyte.dse.queries.StrikeDseInputQuery;
import com.billybyte.dse.queries.TreasuryRateQueryFromTreasuryRateSingle;
import com.billybyte.dse.queries.VolDseInputQueryForStksFromYahoo;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.YahooCombinedStkOptPdiSetCqrRetQuery;
import com.billybyte.marketdata.YahooFinanceDivYieldQuery;
import com.billybyte.marketdata.YahooHistoricalVolCqrQuery;
import com.billybyte.marketdata.YahooOptionVolCqrQueryForOpts;

/**
 * QueryManagerBuilder builds instances of QueryManager.
 *   QueryManager is the class that gathers inputs for the options models
 *     of DerivativeSetEngine.  
 *   QueryManager maps the name of a security to various input queries which
 *     fetch atm prices, vols, rates, etc that you use to calculate a
 *     derivative price or a derivative sensitivity.
 *       
 * @author bperlman1
 *
 */
public class QueryManagerBuilder {
	public static final QueryManager qmForStocksUsingYahoo(
			Calendar evalDate,
			QueryInterface<String, SecDef> sdQuery){
		long evalDateLong = Dates.getYyyyMmDdFromCalendar(evalDate);
		
		// 1.  create all of the dse input queries that 
		//      the dse needs to price a derivative like an option
		YahooCombinedStkOptPdiSetCqrRetQuery innerPdiQuery = 
				new YahooCombinedStkOptPdiSetCqrRetQuery();
		AtmFromPdiCqrQuery atmQuery = 
				new AtmFromPdiCqrQuery(sdQuery,innerPdiQuery,evalDateLong);		
		
		// vol - don't get implied atm vol for stocks - it's too slow
		VolDseInputQueryForStksFromYahoo volQuery = 
				new VolDseInputQueryForStksFromYahoo(
						null,
						new YahooHistoricalVolCqrQuery(),
						new YahooOptionVolCqrQueryForOpts(),
						new SecDefQueryAllMarkets());
		
		// dte
		DteFromPdiInputQuery dteQuery = 
				new DteFromPdiInputQuery(sdQuery,innerPdiQuery, evalDate);
		
		// strike
		StrikeDseInputQuery strikeQuery = 
				new StrikeDseInputQuery(sdQuery);
		
		// call put
		CallPutInputQuery cpQuery = 
				new CallPutInputQuery(sdQuery);
		
		// rate
		TreasuryRateQueryFromTreasuryRateSingle rateQuery = 
				new TreasuryRateQueryFromTreasuryRateSingle();
		// div
		DivDseInputQuery divQuery = 
				new DivDseInputQuery(
						rateQuery,
						new YahooFinanceDivYieldQuery());
		
		// settle		
		SettleDseInputQueryFromPdiQuery settleQuery = 
				new SettleDseInputQueryFromPdiQuery(
						sdQuery, 
						innerPdiQuery, 
						evalDateLong);
		
		
		//  2.  Create the QueryManager
		// 	2.1. create map of dioTypes to dseInput queries as per xml pattern below
		VolDiot volDiot = new VolDiot();  // we use it in several places
		HashMap<DioType<?>, DseInputQuery<?>> dseInputQueryMap = 
				new HashMap<DioType<?>, DseInputQuery<?>>();
		dseInputQueryMap.put(new AtmDiot(), atmQuery);
		dseInputQueryMap.put(volDiot, volQuery);
		dseInputQueryMap.put(new DteSimpleDiot(),dteQuery);
		dseInputQueryMap.put(new CallPutDiot(), volQuery);
		dseInputQueryMap.put(new StrikeDiot(),strikeQuery);
		dseInputQueryMap.put(new CallPutDiot(), cpQuery);
		dseInputQueryMap.put(new RateDiot(), rateQuery);
		dseInputQueryMap.put(new DivDiot(), divQuery);
		dseInputQueryMap.put(new SettlePriceDiot(), settleQuery);
	
		//   2.2  create a secDefQueryMap that maps SecSymbolType's to queries
		//     that produce an underlying for that SecSymbolType
		
		HashMap<String, QueryInterface<String, List<SecDef>>> symTypeToSecDefListMap =
				new HashMap<String, QueryInterface<String,List<SecDef>>>();
		QueryInterface<String,List<SecDef>> baseUnderlyingQuery = 
				new BaseUnderlyingSecDefQuery(sdQuery);
		symTypeToSecDefListMap.put("((OPT)|(STK))", baseUnderlyingQuery);
	
		//  2.3  Create the QueryManager
		QueryManager queryManager = 
				new QueryManager(dseInputQueryMap, symTypeToSecDefListMap);
		return queryManager;
	}
}
