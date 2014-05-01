package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.inputs.UnderlyingInfo;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.queries.BaseUnderlyingSecDefQuery;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.dse.queries.ImpliedCsoCorrelationSetQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.futures.apos.CalSwapUnderlyingSecDefQuery;
import com.billybyte.marketdata.futures.csos.NymexCsoSecDefListQuery;
import com.billybyte.queries.ComplexQueryResult;

import junit.framework.TestCase;

public class TestQueryManager extends TestCase{
	Map<String, SecInputsInfo> testDataMap;
	AtmQueryForTest atmQuery;
	VolQueryForTest volQuery ;
	StrikeQueryForTest strikeQuery ;
	QueryInterface<String,SecDef> sdQuery = new SecDefQueryAllMarkets();

	QueryManager qm ;
	
	DioType<?>[] dioMapKeys = { // create keys for dseInputQueryMap
			new AtmDiot(),new VolDiotForTest(),new StrikeDiotForTest()
	};
	DseInputQuery<?>[] inputQueryArray ;

	
	@Override
	protected void setUp() throws Exception {
		if(qm==null){
			// get test data for all test Input Queries
			this.testDataMap = CollectionsStaticMethods.mapFromCsv(
					"testQueryDataForTestQueryManager.csv",
					this.getClass(), 
					"shortName", String.class,  SecInputsInfo.class);
			atmQuery = new AtmQueryForTest(testDataMap);
			volQuery = new VolQueryForTest(testDataMap);
			strikeQuery = new  StrikeQueryForTest(testDataMap);
			this.inputQueryArray = new InputQuery[]
				{// create values for the dseInputQueryMap
					atmQuery,volQuery,strikeQuery
				};

			Map<DioType<?>,DseInputQuery<?>> dseInputQueryMap = 
				CollectionsStaticMethods.mapInitFromArray(dioMapKeys, inputQueryArray);
			QueryInterface<String,List<SecDef>> baseUnderlyingQuery = 
					new BaseUnderlyingSecDefQuery(sdQuery);
			// add a query that gets underlyings for cso's
			
			// make the specfic underlying queries map have no enteries for this testing
			Map<String,QueryInterface<String,List<SecDef>>> regexSearchableUnderlyingQueries = 
					new HashMap<String, QueryInterface<String,List<SecDef>>>();
			regexSearchableUnderlyingQueries.put("((FOP)|(FUT)|(OPT)|(STK))", baseUnderlyingQuery);
			regexSearchableUnderlyingQueries.put("((CSX)|(AAO)|(AOX))\\.((FOP)|(FUT))",new CalSwapUnderlyingSecDefQuery());
			regexSearchableUnderlyingQueries.put(ImpliedCsoCorrelationSetQuery.REGEX_GET_STRING,new NymexCsoSecDefListQuery(sdQuery) );
			qm = new  QueryManager(
					dseInputQueryMap, 
					regexSearchableUnderlyingQueries);
		}
		super.setUp();
	}


	public void testUsingQueriesFromQueryManger(){
		// create one
		
		// get a query
		Set<String> names = testDataMap.keySet();
		Utils.prtObMess(this.getClass(), "atm query");
		DseInputQuery<BigDecimal> atmq = 
				qm.getQuery(new AtmDiot());
		Map<String, ComplexQueryResult<BigDecimal>> bdResult = 
				atmq.get(names, 1, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(bdResult);

		Utils.prtObMess(this.getClass(), "vol query");
		DseInputQuery<BigDecimal> volq = 
				qm.getQuery(new VolDiotForTest());
		Map<String, ComplexQueryResult<BigDecimal>> dbResult = 
				volq.get(names, 1, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(dbResult);
	
		Utils.prtObMess(this.getClass(), "integer query");
		DseInputQuery<BigDecimal> strikeq = 
				qm.getQuery(new StrikeDiotForTest());
		Map<String, ComplexQueryResult<BigDecimal>> strikeResult = 
				strikeq.get(names, 1, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(strikeResult);
	
	}
	
	
	private UnderlyingInfo getAllInfo(){
		UnderlyingInfo ret = new UnderlyingInfo();
		Set<String> names = testDataMap.keySet();
		Set<DioType<?>> diotSet = CollectionsStaticMethods.setFromArray(dioMapKeys);
		for(DioType<?> diot:diotSet){
			Map<String, ComplexQueryResult<?>> diotResultsMap = 
					qm.getDioInputsWithUnSpecifiedType(names, diot,1, TimeUnit.SECONDS);
			ret.putDiotMapUnspecifiedType(diot, diotResultsMap);
		}
		return ret;
	}
	
	public void testBuildInBlk(){
		// use qm to get secdefs and inputs
		Set<String> names = testDataMap.keySet();
		for(String sn:names){
			SecDef mainSd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			SecDef[] underlyingSds = 
					qm.getUnderlyingSecDefs(sn, 1, TimeUnit.SECONDS).toArray(new SecDef[]{});
			UnderlyingInfo underInputsMap = getAllInfo();
			Calendar evaluationDate = Calendar.getInstance();
			InBlk inBlk = new InBlk(
					mainSd, underlyingSds, 
					underInputsMap,Arrays.asList( dioMapKeys), evaluationDate);
			List<BigDecimal> atmList = inBlk.getUnderLyingInputList(new AtmDiot());
			Utils.prt(sn+": atms: ");
			CollectionsStaticMethods.prtListItems(atmList);

			List<BigDecimal> volList = inBlk.getUnderLyingInputList(new VolDiotForTest());
			Utils.prt(sn+": vols: ");
			CollectionsStaticMethods.prtListItems(volList);

			List<BigDecimal> strikeList = inBlk.getUnderLyingInputList(new StrikeDiotForTest());
			Utils.prt(sn+": strikes: ");
			CollectionsStaticMethods.prtListItems(strikeList);
		}
	}
	
	
}
