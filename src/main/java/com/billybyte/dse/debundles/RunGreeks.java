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

public class RunGreeks {
	/**
	 * Examples of getting greesk from the DerivativeSetEngine
	 * @param args "example=[1 2 3 4 or 5]"
	 */
	public static void main(String[] args) {
		// get command line args
		Map<String,String> argPairs = 
				Utils.getArgPairsSeparatedByChar(args, "=");
		Utils.prt("command line args: " + Arrays.toString(args));
		String example = argPairs.get("example");

		if(example.compareTo("1")==0){
			example1_getPrices();
		}
		if(example.compareTo("2")==0){
			example2_getGreeks();
		}
		if(example.compareTo("3")==0){
			example3_getPortfolioGreeks(argPairs.get("portfolioPath"));
		}
		if(example.compareTo("4")==0){
			example4_DseFromSpring();
		}
		if(example.compareTo("5")==0){
			example5_DseResultsAsCsv(argPairs.get("portfolioPath"));
		}
		if(example.compareTo("6")==0){
			example6_printPortfolio(argPairs.get("portfolioPath"));
		}
	}
	
	
	/**
	 * Just get option prices (or return underlying prices if the security is an underlying)
	 */
	static final void example1_getPrices(){
		DerivativeSensitivityTypeInterface sense = new OptPriceDerSen();
		Set<String> shortNameSet = getShortNameSet();
		DerivativeSetEngine dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		Map<String, DerivativeReturn[]> dseResults = 
				dse.getSensitivity(sense, shortNameSet);
		for(String shortName : dseResults.keySet()){
			DerivativeReturn dr = dseResults.get(shortName)[0];
			printDr(shortName, dr, sense);
				
		}

	}
	
	/**
	 * Get a collection of basic greeks (delta, gamma, vega, theta, rho)
	 */
	static final void example2_getGreeks(){
		List<DerivativeSensitivityTypeInterface> senses = getSenses();
		Set<String> shortNameSet = getShortNameSet();
		DerivativeSetEngine dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		
		for(DerivativeSensitivityTypeInterface sense : senses){
			Map<String, DerivativeReturn[]> dseResults = 
					dse.getSensitivity(sense, shortNameSet);
			for(String shortName : dseResults.keySet()){
				DerivativeReturn dr = dseResults.get(shortName)[0];
				printDr(shortName, dr, sense);
			}
		}

	}
	
	/**
	 * Get a collection of basic greeks from a csv portfolio
	 * @param portfolioPath
	 */
	static final void example3_getPortfolioGreeks(String portfolioPath){
		List<DerivativeSensitivityTypeInterface> senses = getSenses();
		DerivativeSetEngine dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		Map<String, Integer> portfolioMap = getPortfolio(portfolioPath);
		if(portfolioMap == null || portfolioMap.size()<=0){
			Utils.prtObErrMess(RunGreeks.class, "Portfolio Map is null or can't be found");
			return;
		}
		for(DerivativeSensitivityTypeInterface sense : senses){
			Map<String, DerivativeReturn[]> dseResults = 
					dse.getSensitivity(sense, portfolioMap.keySet());
			for(String shortName : portfolioMap.keySet()){
				DerivativeReturn[] drArr =dseResults.get(shortName);
				DerivativeReturn dr = null;
				if(drArr!=null){
					dr = dseResults.get(shortName)[0];
				}
				printDr(shortName, dr, sense);
			}
		}
		
	}
	

	/**
	 * Use Spring dependency injection xml files to create DerivativeSetEngine.
	 *   In most cases, this will be the preferred way to create dse's.
	 */
	static final void example4_DseFromSpring(){
		DerivativeSensitivityTypeInterface sense = new OptPriceDerSen();
		Set<String> shortNameSet = getShortNameSet();
		DerivativeSetEngine dse = DerivativeSetEngineBuilder.dseForStocksUsingYahooAndSpringDefault();
		Map<String, DerivativeReturn[]> dseResults = 
				dse.getSensitivity(sense, shortNameSet);
		for(String shortName : dseResults.keySet()){
			DerivativeReturn dr = dseResults.get(shortName)[0];
			printDr(shortName, dr, sense);
				
		}
	}
	
	/**
	 * Get a collection of basic greeks from a csv portfolio.  Print the results
	 *   as csv.
	 * @param portfolioPath
	 */
	static final void example5_DseResultsAsCsv(String portfolioPath){
		List<DerivativeSensitivityTypeInterface> senses = getSenses();
		DerivativeSetEngine dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		Map<String, Integer> portfolioMap = getPortfolio(portfolioPath);
		if(portfolioMap == null || portfolioMap.size()<=0){
			Utils.prtObErrMess(RunGreeks.class, "Portfolio Map is null or can't be found");
			return;
		}
		
		
		Map<String,Map<DerivativeSensitivityTypeInterface,DerivativeReturn>> secToSenseToDrArrMap = 
				new HashMap<String, Map<DerivativeSensitivityTypeInterface,DerivativeReturn>>();

		// create inner map for each shortName which holds the sensitivities
		for(String sn : portfolioMap.keySet()){
			secToSenseToDrArrMap.put(sn, new HashMap<DerivativeSensitivityTypeInterface, DerivativeReturn>());
		}
		
		for(DerivativeSensitivityTypeInterface sense : senses){
			Map<String, DerivativeReturn[]> dseResults = 
					dse.getSensitivity(sense, portfolioMap.keySet());
			for(String shortName : dseResults.keySet()){
				DerivativeReturn dr = dseResults.get(shortName)[0];
				 Map<DerivativeSensitivityTypeInterface,DerivativeReturn> senseMap = 
						 secToSenseToDrArrMap.get(shortName);
				senseMap.put(sense, dr);
			}
		}
		
		

		// Create csv output
		TreeMap<String,String[]> snToCsvMap = 
				new TreeMap<String, String[]>();
		List<String> exceptionList = new ArrayList<String>();
		
		for(String sn : secToSenseToDrArrMap.keySet()){
			Map<DerivativeSensitivityTypeInterface,DerivativeReturn> senseToDrArrMap = 
					secToSenseToDrArrMap.get(sn);
			BigDecimal size = new BigDecimal(portfolioMap.get(sn));
			String[] line = new String[senses.size()+2];
			line[0] = sn;
			line[1] = size.toString();
			for(int i = 0;i<senses.size();i++){
				line[i+2]="null";
				DerivativeSensitivityTypeInterface sense = senses.get(i);
				DerivativeReturn dr = senseToDrArrMap.get(sense);
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

	static final void example6_printPortfolio(String portfolioPath){
		Map<String, Integer> portfolioMap = getPortfolio(portfolioPath);
		if(portfolioMap == null || portfolioMap.size()<=0){
			Utils.prtObErrMess(RunGreeks.class, "Portfolio Map is null or can't be found");
			return;
		}
		CollectionsStaticMethods.prtMapItems(portfolioMap);
	}
	
	
	
	// helper methods below
	/**
	 * Get basic shortName set
	 * @return Set<String>
	 */
	static final Set<String> getShortNameSet(){
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
		
		Set<String> shortNameSet = CollectionsStaticMethods.setFromArray(array);
		return shortNameSet;
	}

	static final List<DerivativeSensitivityTypeInterface> getSenses(){
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
		
		return senseList;
	}
	
	/**
	 * Get a csv portfolio, which has the first column as the size, and the
	 *   second column as the shortName
	 * @param portfolioPath
	 * @return Map<String, Integer>
	 */
	static final Map<String, Integer>  getPortfolio(String portfolioPath){
		TreeMap<String, Integer> portfolioMap = new TreeMap<String, Integer>();
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
				Integer size = new Integer(line[0]);
				portfolioMap.put(sn,size);
			}
		}
		return portfolioMap;

	}
	
	/**
	 * Print DerivativeReturn objects if they are valid.
	 * @param shortName
	 * @param dr
	 * @param sense
	 */
	static final void printDr(String shortName,DerivativeReturn dr, DerivativeSensitivityTypeInterface sense){
		if(dr==null || !dr.isValidReturn()){
			Utils.prtObErrMess(RunGreeks.class, shortName+" : "+dr.getException().getMessage());
		}else{
			Utils.prt(shortName+" : "+ sense.getString()+ " : " + dr.getValue().toString());				
		}

	}

}
