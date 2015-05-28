package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.dse.queries.StrikeDseInputQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

import junit.framework.TestCase;

public class TestMongoBasedQueriesFromBeans  extends TestCase{
	private final String beansFileName = "beans_TestMongoBasedQueryManager.xml";
	public void test1(){
		Map<String,Object> beansMap =
				Utils.springGetAllBeans(beansFileName, this.getClass());
		@SuppressWarnings("unchecked")
		DseInputQuery<BigDecimal> atmQuery = (DseInputQuery<BigDecimal>)beansMap.get("atmQuery");
		@SuppressWarnings("unchecked")
		DseInputQuery<BigDecimal> volQuery = (DseInputQuery<BigDecimal>)beansMap.get("volQuery");
		StrikeDseInputQuery strikeQuery = (StrikeDseInputQuery)beansMap.get("strikeQuery");
		assertNotNull(atmQuery);
		assertNotNull(volQuery);
		assertNotNull(strikeQuery);
	}
	
	@SuppressWarnings("unchecked")
	public void test2(){
		//map of InputQueries
		Map<DioType<?>,InputQuery<?>> inputQueryMap = Utils.springGetBean(Map.class, beansFileName, this.getClass(),"queryMap");
		assertNotNull(inputQueryMap);
		assertTrue(inputQueryMap.size()>1);
		//secDefQueryMap
		Map<DioType<?>,InputQuery<?>> sdQueryMap = Utils.springGetBean(Map.class, beansFileName, this.getClass(),"secDefQueryMap");
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
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void test3(){
//		Map<String,Object> beansMap =
//				Utils.springGetAllBeans("beans_TestQueryManager.xml", this.getClass());
		Map<String,Object> beansMap =
				Utils.springGetAllBeans("beans_TestMongoBasedQueryManager.xml", this.getClass());
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
			if(atmList==null){
				Utils.prt("no atms for :" + sn);
			}else{
				CollectionsStaticMethods.prtListItems(atmList);
			}

			BigDecimal vol = inBlk.getMainInputList(new VolDiotForTest());
			if(vol==null){
				Utils.prt("no vol for :" + sn);
			}else{
				Utils.prt(sn+": vol: "+vol.toString());
			}

			BigDecimal strike = inBlk.getMainInputList(new StrikeDiotForTest());
			Utils.prt(sn+": strikes=: " + strike);
			
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
