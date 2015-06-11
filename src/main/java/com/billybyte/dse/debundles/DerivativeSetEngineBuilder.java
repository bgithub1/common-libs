package com.billybyte.dse.debundles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.RhoDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.queries.QueryFromRegexPattern;

public class DerivativeSetEngineBuilder {
	
	
	/**
	 * Static method for returning a basic DerivativeSetEngine that uses
	 *   public data for stocks and stock option, mostly from Yahoo.
	 *   
	 * DerivativeSetEngines always have 2 main components:
	 * 	1.  QueryManager to manage getting inputs for the options models (like price, vol, interest rates, dividend yields, dte's, etc)
	 *  2.  QueryFromRegexPattern - a class that maps partial security names to option/derivative models.
	 *   
	 * @return DerivativeSetEngine
	 */
	public static DerivativeSetEngine dseForStocksUsingYahoo() {
		// 1. create an eval date
		Calendar evalDate = Calendar.getInstance();
		
		// 2.  create all of the dse input queries that 
		//      the dse needs to price a derivative like an option
		// sd query
		QueryInterface<String, SecDef> sdQuery =
				new SecDefQueryAllMarkets();
		QueryManager queryManager = QueryManagerBuilder.qmForStocksUsingYahoo(evalDate, sdQuery);
		// 3.  Create the DerivativeSetEngine

		QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQuery =
				RegexModelQueryBuilder.regexModelQueryForStksOptsFromYahoo(evalDate);
		
		// 3.3  Create the actual DerivativeSetEngine
		DerivativeSetEngine dse = 
				new DerivativeSetEngine(queryManager, sdQuery, evalDate, regexModelQuery);
		
		return dse;
	}
	
	/**
	 * 
	 * @param args "dseType=[stockEngine, commodEngine, stkCommodEngine]"
	 */
	public static void main(String[] args) {
		Utils.prt("dseType=[stockEngine, commodEngine, stkCommodEngine]");
		Utils.prt("example: dseType=stockEngine");
		Map<String,String> argPairs = 
				Utils.getArgPairsSeparatedByChar(args, "=");
		Utils.prt("command line args: " + Arrays.toString(args));
		String engineType = argPairs.get("dseType");
		DerivativeSetEngine dse = null;
		if(engineType==null){
			dse = dseForStocksUsingYahoo();
		}else if(engineType.compareTo("stockEngine")==0){
			dse = dseForStocksUsingYahoo();
		}
		if(dse==null){
			throw Utils.IllArg(DerivativeSetEngineBuilder.class, "command line did not specify dse type");
		}
		
		// do something with the dse
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
		Set<String> keySet = CollectionsStaticMethods.setFromArray(array);
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
		for(String sn : secToSenseToDrArrMap.keySet()){
			Map<DerivativeSensitivityTypeInterface,DerivativeReturn[]> senseToDrArrMap = 
					secToSenseToDrArrMap.get(sn);
			String[] line = {
					sn,
					new BigDecimal(senseToDrArrMap.get(price)[0].getValue().doubleValue()).setScale(5,RoundingMode.HALF_EVEN).toString(),
					new BigDecimal(senseToDrArrMap.get(delta)[0].getValue().doubleValue()).setScale(5,RoundingMode.HALF_EVEN).toString(),
					new BigDecimal(senseToDrArrMap.get(gamma)[0].getValue().doubleValue()).setScale(5,RoundingMode.HALF_EVEN).toString(),
					new BigDecimal(senseToDrArrMap.get(vega)[0].getValue().doubleValue()).setScale(5,RoundingMode.HALF_EVEN).toString(),
					new BigDecimal(senseToDrArrMap.get(theta)[0].getValue().doubleValue()).setScale(5,RoundingMode.HALF_EVEN).toString(),
					new BigDecimal(senseToDrArrMap.get(rho)[0].getValue().doubleValue()).setScale(5,RoundingMode.HALF_EVEN).toString(),
			};
			snToCsvMap.put(sn,line);
		}

		String header = "shortName,price,delta,gamma,vega,theta,rho";
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
		
	}
}
