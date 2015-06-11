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
import java.util.concurrent.TimeUnit;

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
	
	public static DerivativeSetEngine dseForStocksUsingYahooAndSpringDefault(){
		return Utils.springGetBean(
				DerivativeSetEngine.class, 
				"beans_DseYahooBased_EvalToday.xml", 
				DerivativeSetEngineBuilder.class,
				"dse");
		
	}
	
	public static DerivativeSetEngine dseForStocksUsingYahooAndSpring(Class<?> classInResoucePacakge,String pathOfSpringXml){
		if(classInResoucePacakge==null){
			return Utils.springGetBean(
					DerivativeSetEngine.class, 
					pathOfSpringXml, 
					"dse");
		}
		return Utils.springGetBean(
				DerivativeSetEngine.class, 
				pathOfSpringXml, 
				classInResoucePacakge,
				"dse");
	}
	

	

}
