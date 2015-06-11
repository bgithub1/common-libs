package com.billybyte.dse.debundles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.RhoDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;
import com.billybyte.marketdata.SecDef;

public class RunGreeks {
	/**
	 * 
	 * @param args "dseType=[stockEngine, commodEngine, stkCommodEngine]"
	 */
	public static void main(String[] args) {
		// print out user info to console
		Utils.prt("dseType=[stockEngine, stkFromSpring,commodEngine, stkCommodEngine] portfolioPath=my2ColumnQtyAndShortName.csv resourceClass=com.billybyte.dse.debundles.RunGreeks" );
		Utils.prt("example1: dseType=stockEngine");
		Utils.prt("example1: dseType=stockEngine portfolioPath=myPort.csv");

		// get command line args
		Map<String,String> argPairs = 
				Utils.getArgPairsSeparatedByChar(args, "=");
		Utils.prt("command line args: " + Arrays.toString(args));

		// find type of dse engine we should create
		String engineType = argPairs.get("dseType");
		DerivativeSetEngine dse = null;

		// choose a dse
		if(engineType==null){
			// default yahoo based
			dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		}else if(engineType.compareTo("stockEngine")==0){
			// yahoo based
			dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		}else if(engineType.compareTo("stkFromSpring")==0){
			// use default spring injection file in this package
			String springBeansPath = argPairs.get("springBeansPath");
			if(springBeansPath==null){
				dse = DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpringDefault();
			}else{
				// stock/yahoo based, but using spring injection file
				Class<?> classToLocateResource = null;
				String resourceString = argPairs.get("resourceClass");
				// the spring injection file might be in a package in 
				//   the class system, or an external file
				if(resourceString!=null){
					try {
						classToLocateResource = Class.forName(resourceString);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					dse = DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpring(classToLocateResource,springBeansPath);
				}else{
					
				}
				
			}
		}
		if(dse==null){
			throw Utils.IllArg(DerivativeSetEngineBuilder.class, "command line did not specify dse type");
		}
		
		Map<String, Integer> portfolioMap = 
				new TreeMap<String, Integer>();
		// DEFAULT SECURITIES to do something with the dse
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
			"RYMEX.STK.SMART"
		};
		// create default portfolio in case user doesn't provide one
		for(String s:array)
		{
			portfolioMap.put(s, 1);
		}
		
		
		// maybe the user passed in a portfolio
		String portfolioPath = argPairs.get("portfolioPath");
		
		if(portfolioPath!=null){
			List<String[]> myPort = Utils.getCSVData(portfolioPath);
			portfolioMap = 
					new TreeMap<String, Integer>();
			for(int i = 1;i<myPort.size();i++){
				String[] line = myPort.get(i);
				if(line.length<2){
					Utils.prtObErrMess(DerivativeSetEngineBuilder.class, "csv line "+i+ " has an invalid format.  Contents="+Arrays.toString(line));
					continue;
				}
				String sn = line[1];
				// check to see if it's a valid shortName
				SecDef sd = dse.getSdQuery().get(sn, 1, TimeUnit.SECONDS);
				if(sd==null){
					Utils.prtObErrMess(DerivativeSetEngineBuilder.class, sn+" invalid security name");
				}
				Integer size = new Integer(line[0]);
				portfolioMap.put(sn,size);
			}
		}

		Set<String> keySet = portfolioMap.keySet();

		// get stuff from dse (risk and prices)
		List<DerivativeSensitivityTypeInterface> senseList = 
				new ArrayList<DerivativeSensitivityTypeInterface>();
		DerivativeSensitivityTypeInterface price = new OptPriceDerSen();
		DerivativeSensitivityTypeInterface delta = new DeltaDerSen();
		DerivativeSensitivityTypeInterface gamma = new GammaDerSen();
		DerivativeSensitivityTypeInterface vega = new VegaDerSen();
		DerivativeSensitivityTypeInterface theta = new ThetaDerSen();
		DerivativeSensitivityTypeInterface rho = new RhoDerSen();
		
		senseList.add(price);
		senseList.add(delta);
		senseList.add(gamma);
		senseList.add(vega);
		senseList.add(theta);
		senseList.add(rho);
		
		Map<String,Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]>> secToSenseToDrArrMap = 
				new HashMap<String, Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]>>();
		// create inner map for each shortName
		for(String sn : keySet){
			secToSenseToDrArrMap.put(sn, new HashMap<DerivativeSensitivityTypeInterface, DerivativeReturn[]>());
		}
		
		
		for(DerivativeSensitivityTypeInterface sense : senseList){
			Map<String, DerivativeReturn[]> snToDrArrMap =  dse.getSensitivity(sense, keySet);
			for(String sn : snToDrArrMap.keySet()){
				DerivativeReturn[] drArr = snToDrArrMap.get(sn);
				secToSenseToDrArrMap.get(sn).put(sense,drArr);
			}
		}

		// Create csv output
		TreeMap<String,String[]> snToCsvMap = 
				new TreeMap<String, String[]>();
		List<String> exceptionList = new ArrayList<String>();
		
		for(String sn : secToSenseToDrArrMap.keySet()){
			Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]> senseToDrArrMap = 
					secToSenseToDrArrMap.get(sn);
			BigDecimal size = new BigDecimal(portfolioMap.get(sn));
			String[] line = new String[senseList.size()+2];
			line[0] = sn;
			line[1] = size.toString();
			for(int i = 0;i<senseList.size();i++){
				line[i+2]="null";
				DerivativeSensitivityTypeInterface sense = senseList.get(i);
				DerivativeReturn dr = senseToDrArrMap.get(sense)[0];
				if(dr==null){
					exceptionList.add(sense.getString()+ " No return from DSE");
					continue;
				}
				if(!dr.isValidReturn()){
					exceptionList.add(sn + " " + sense.getString()+ " " + dr.getException().getMessage());
					continue;
				}
				line[i+2] = new BigDecimal(dr.getValue().doubleValue()).multiply(size).setScale(5,RoundingMode.HALF_EVEN).toString();
			}
			
			snToCsvMap.put(sn,line);
		}
		

		System.out.flush();
		String header = "shortName,size,price,delta,gamma,vega,theta,rho";
		Utils.prt(header);
		for(String key : snToCsvMap.keySet()){
			String[] lineTokens = snToCsvMap.get(key);
			String line="";
			for(String lineToken:lineTokens){
				line+=lineToken+",";
			}
			line = line.substring(0,line.length()-1);
			Utils.prt(line);
		}

		if(exceptionList.size()>0){
			Utils.prt("");
			Utils.prt("exceptions below:");
			CollectionsStaticMethods.prtListItems(exceptionList);
			
		}

	}


}
