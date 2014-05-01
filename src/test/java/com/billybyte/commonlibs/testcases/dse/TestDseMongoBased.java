package com.billybyte.commonlibs.testcases.dse;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.billybyte.commoncollections.MapFromMap;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.models.vanilla.VanOptBawAmerican;
import com.billybyte.dse.models.vanilla.VanOptBlackEuropean;
import com.billybyte.dse.models.vanilla.VanOptUnderlying;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.queries.QueryFromRegexPattern;

import junit.framework.TestCase;


public class TestDseMongoBased extends TestCase {
	
	public void testBuildDse(){
		Utils.prtObMess(this.getClass(), "testBuildMongoDse");
		QueryInterface<String, SecDef> sdQuery = 
				new SecDefQueryAllMarkets();
		QueryManager qm =
				Utils.springGetBean(
						QueryManager.class,
						"beans_TestMongoBasedQueryManager.xml",
						 this.getClass(),
						"queryManager");
		
		assertNotNull(qm);
		
		Calendar evaluationDate = Calendar.getInstance();
		evaluationDate.set(2013,00,01);
		
		
		DerivativeModelInterface[] modelArray = {
			new VanOptBlackEuropean(evaluationDate, 
					new VolDiotForTest(),null),
			 new VanOptBawAmerican(evaluationDate, 
					 new VolDiotForTest(), null),
			 new VanOptUnderlying(evaluationDate, 
					 new VolDiotForTest(), null),
			 new VanOptUnderlying(evaluationDate, 
					 new VolDiotForTest(), null)
		};
		String[] names = {
				".OPT.SMART","IBM.OPT.SMART",".FUT.",".STK."
		};
		Map<String, DerivativeModelInterface> modelMap = 
				CollectionsStaticMethods.mapInitFromArray(names, modelArray);
		QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQuery = 
				new QueryFromRegexPattern<String, DerivativeModelInterface>(modelMap);
		DerivativeSetEngine dse = 
				new DerivativeSetEngine(qm, sdQuery, 
						evaluationDate, regexModelQuery);
		assertNotNull(dse);
		dseTest(dse);
	}
	
	private final void dseTest(DerivativeSetEngine dse){
		DerivativeSensitivityTypeInterface[] senses = {
				new OptPriceDerSen(),
				new DeltaDerSen(),
				new VegaDerSen(),
				new GammaDerSen(),
				new ThetaDerSen()
		};
		String csvNameOrPath = "testQueryDataForTestDseMongoBased.csv";
		Class<?> classInPkgOfResource = this.getClass();
		String colNameOfKey = "shortName";
		Class<String> classOfKey = String.class;
		Class<SecInputsInfo> classOfData= SecInputsInfo.class;
		Map<String, SecInputsInfo> secInfoMap = 
				new MapFromMap<String, SecInputsInfo>(
						csvNameOrPath,classInPkgOfResource,
						colNameOfKey,classOfKey,classOfData);
		
		String[] optNames = secInfoMap.keySet().toArray(new String[]{});
		Set<String> derivativeShortNameSet = 
				CollectionsStaticMethods.setFromArray(optNames);
		// make a tree map of the results
		TreeMap<DerivativeSensitivityTypeInterface, TreeMap<String, DerivativeReturn[]>> sortedResults = 
				new TreeMap<DerivativeSensitivityTypeInterface, TreeMap<String,DerivativeReturn[]>>();
		for(DerivativeSensitivityTypeInterface sense : senses){
			TreeMap<String, DerivativeReturn[]> innerMap=new TreeMap<String, DerivativeReturn[]>();
			sortedResults.put(sense, innerMap);
			Map<String,DerivativeReturn[]> results =
					dse.getSensitivity(sense, derivativeShortNameSet);
			for (Entry<String, DerivativeReturn[]> entry : results.entrySet()) {
				String key = entry.getKey();
				DerivativeReturn[] value = entry.getValue();
				innerMap.put(key,value);
			}
			assertEquals(9,results.size());
		}
		
		// make a csv printout
		List<String[]> csv = new ArrayList<String[]>();
		String[] head = {"sense","shortName","value"};
		csv.add(head);
		
		for (Entry<DerivativeSensitivityTypeInterface, TreeMap<String, DerivativeReturn[]>> topLevelEntry : sortedResults.entrySet()) {
			DerivativeSensitivityTypeInterface sense = topLevelEntry.getKey();
			TreeMap<String, DerivativeReturn[]> results = topLevelEntry.getValue();
			for (Entry<String, DerivativeReturn[]> entry : results.entrySet()) {
				String key = entry.getKey();
				DerivativeReturn[] value = entry.getValue();
				for(DerivativeReturn dr:value){
					String[] line=  {sense.toString(),key,
							dr.isValidReturn()?dr.getValue().toString():dr.getException().getMessage()};
					csv.add(line);
				}
			}
		}
		for(int i=0;i<csv.size();i++){
			String[] line=csv.get(i);
			String consoleLine = "";
			for(String s:line){
				consoleLine +=s+",";
			}
			consoleLine = consoleLine.substring(0, consoleLine.length()-1);
			Utils.prt(consoleLine);
		}
	}
	
	public void testDseSpringLoad(){
		Utils.prtObMess(this.getClass(), "testDseSpringLoad");

		Class<?> classNameOfClassInPkgOfBeanXmlAsResource = this.getClass();
		String beanXmlspath = "beans_TestDseFromMongoBasedQm.xml";
		Map<String,Object> objects = 
				Utils.springGetAllBeans(beanXmlspath, classNameOfClassInPkgOfBeanXmlAsResource);
		assertNotNull(objects);
		assertTrue(objects.size()>0);
	}
	
	public void testModelsFromSpring(){
		Utils.prtObMess(this.getClass(), "testModelsFromSpring");

		Class<?> classNameOfClassInPkgOfBeanXmlAsResource = this.getClass();
		String beanXmlspath = "beans_TestDseFromMongoBasedQm.xml";
		String beanName = "dse";
		Class<DerivativeSetEngine> classOfReturn = DerivativeSetEngine.class;
		DerivativeSetEngine dse = Utils.springGetBean(
				classOfReturn, beanXmlspath, 
				classNameOfClassInPkgOfBeanXmlAsResource, beanName);
		assertNotNull(dse);
		dseTest(dse);
		
	}
}
