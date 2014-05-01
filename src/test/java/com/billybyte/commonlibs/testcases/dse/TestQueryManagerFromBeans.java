package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.inputs.UnderlyingInfo;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.DivDiot;
import com.billybyte.dse.inputs.diotypes.DteSimpleDiot;
import com.billybyte.dse.inputs.diotypes.RateDiot;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

import junit.framework.TestCase;

public class TestQueryManagerFromBeans extends TestCase {
	public void test1(){
		Map<String,Object> beansMap =
				Utils.springGetAllBeans("beans_TestQueryManager.xml", this.getClass());
		AtmQueryForTest atmQuery = (AtmQueryForTest)beansMap.get("atmQuery");
		VolQueryForTest volQuery = (VolQueryForTest)beansMap.get("volQuery");
		StrikeQueryForTest strikeQuery = (StrikeQueryForTest)beansMap.get("strikeQuery");
		assertNotNull(atmQuery);
		assertNotNull(volQuery);
		assertNotNull(strikeQuery);
	}
	
	public void test2(){
		//map of InputQueries
		Map<DioType<?>,InputQuery<?>> inputQueryMap = Utils.springGetBean(Map.class, "beans_TestQueryManager.xml", this.getClass(),"queryMap");
		assertNotNull(inputQueryMap);
		assertTrue(inputQueryMap.size()>1);
		//secDefQueryMap
		Map<DioType<?>,InputQuery<?>> sdQueryMap = Utils.springGetBean(Map.class, "beans_TestQueryManager.xml", this.getClass(),"secDefQueryMap");
		assertNotNull(sdQueryMap);
		assertTrue(sdQueryMap.size()>1);
	}
	
	private UnderlyingInfo getAllInfo(
			Map<String,BigDecimal> testDataMap,
			Set<DioType<?>>dioMapKeys,
			QueryManager qm){
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
	
	
	public void test3(){
		Map<String,Object> beansMap =
				Utils.springGetAllBeans("beans_TestQueryManager.xml", this.getClass());
		QueryManager qm = (QueryManager)beansMap.get("queryManager");
		assertNotNull(qm);
		
		Map<String,BigDecimal> testDataMap = 
				(Map<String,BigDecimal>)beansMap.get("testDataMap");
		Map<DioType<?>,InputQuery<?>> inputQueryMap = 
				(Map)beansMap.get("queryMap");
		Set<DioType<?>> dioMapKeys = 
				inputQueryMap.keySet();
		QueryInterface<String, SecDef> sdQuery = 
				(QueryInterface<String, SecDef>)beansMap.get("sdQuery");
		Set<String> names = testDataMap.keySet();
		for(String sn:names){
			SecDef mainSd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			SecDef[] underlyingSds = 
					qm.getUnderlyingSecDefs(sn, 1, TimeUnit.SECONDS).toArray(new SecDef[]{});
			UnderlyingInfo underInputsMap = getAllInfo(testDataMap,dioMapKeys,qm);
			Calendar evaluationDate = Calendar.getInstance();
			InBlk inBlk = new InBlk(
					mainSd, underlyingSds, 
					underInputsMap,
					new ArrayList<DioType<?>>(dioMapKeys), evaluationDate);
			List<BigDecimal> atmList = inBlk.getUnderLyingInputList(new AtmDiot());
			Utils.prt(sn+": atms: ");
			CollectionsStaticMethods.prtListItems(atmList);

			List<BigDecimal> volList = inBlk.getUnderLyingInputList(new VolDiotForTest());
			Utils.prt(sn+": vols: ");
			CollectionsStaticMethods.prtListItems(volList);

			List<BigDecimal> strikeList = inBlk.getUnderLyingInputList(new StrikeDiotForTest());
			Utils.prt(sn+": strikes: ");
			CollectionsStaticMethods.prtListItems(strikeList);
			
			List<BigDecimal> rateList = inBlk.getUnderLyingInputList(new RateDiot());
			Utils.prt(sn+": rates: ");
			CollectionsStaticMethods.prtListItems(rateList);
			
			List<BigDecimal> divList = inBlk.getUnderLyingInputList(new DivDiot());
			Utils.prt(sn+": divs: ");
			CollectionsStaticMethods.prtListItems(divList);
			
			List<BigDecimal> dteList = inBlk.getUnderLyingInputList(new DteSimpleDiot());
			Utils.prt(sn+": dtes: ");
			CollectionsStaticMethods.prtListItems(dteList);
		
		}
	}
}
