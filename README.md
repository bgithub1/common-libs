common-libs
===========

main api for trading/risk/marketdata etc

Example mains:
1. Turn any of your code into an http server see main in: 
   com.billybyte.clientserver.httpserver.HttpCsvQueryServer

2. Turn any of your code into a WebService, and build java clients that
   can consume your web service:
   com.billybyte.clientserver.webserver.RunTestWebServiceServer
   com.billybyte.clientserver.webserver.RunTestWebServiceClient

3. Examples of using lots of different math libraries (org.apache.commons.math3, Jama, org.paukov.combinatorics, etc):
   com.billybyte.mathstuff.MathStuff
   
4. Java Mongo wrappers to facilate accessing mongo dbs using java:
   com.billybyte.mongo.MongoWrapper
   com.billybyte.mongo.MongoXml for easily turning any pojo into a mongo doc that can be stored in a mongodb

5. Using Neodatis in memory sql db:
   com.billybyte.neodatis
   
6. Example of using Spring to launch instantiate java classes form Spring Beans xml files
   com.billybyte.spring.BeansLaunch
   
7. MessageBox routine for modal and non-modal Message Boxes with and without Swing
   com.billybyte.ui

DerivativeSetEngine stuff:
DeriviativeSetEngine allows you to compute prices and greeks for exchange traded commodity options
   using the following syntax: 
    
	static final void example1_getPrices(){
		// com.billybyte.dse.outputs.OptPriceDerSen is a class that defines the "sensitivity" price
		//   Other basic sensitivities are (all in com.billybyte.dse.outputs):
		// 		DeltaDerSen (delta)
		// 		GammaDerSen (gamma)
		// 		ThetaDerSen (theta)
		// 		VegaDerSen (vega)
		// 		RhoDerSen (rho)
		//  There are other more complicated sensitivities, and you can
		//    roll your own by extending com.billybyte.dse.outputs.AbstractSensitivityType
		DerivativeSensitivityTypeInterface sense = new OptPriceDerSen();
		// The static method getShortNameSet() gets a java.util.Set<String> of
		//   strings like: 
		//    NG.FUT.NYMEX.USD.201601 (for NGF16), 
		//    ON.FOP.NYMEX.USD.201601.C.3.250 (for the Jan16 3.250 call on NGF16, 
		//			where "ON" is one of the NYMEX option symbols for NG), 
		//    IBM.STK.SMART (IBM),
		//    IBM.OPT.SMART.USD.C.20170120.C.170.00 (IBM 170.00 call expiring on 01/20/2017 - 
		//			see: http://finance.yahoo.com/q?s=IBM150619C00170000)
		// The shortName convention is 
		//	<symbol>.<type>.<exchange>.<currency>.<yearmonth>.<C or P>.<strike price>
		//  
		Set<String> shortNameSet = getShortNameSet();
		// Get one of the pre-packaged DerivativeSetEngine instances from 
		//   com.billybyte.dse.debundles.DerivativeSetEngineBuilder
		DerivativeSetEngine dse = DerivativeSetEngineBuilder.dseForStocksUsingYahoo();
		// Get the option prices for all of these shortNames
		Map<String, DerivativeReturn[]> dseResults = 
				dse.getSensitivity(sense, shortNameSet);
		// getSensitivity returns Arrays of type com.billybyte.dse.DerivativeReturn
		for(String shortName : dseResults.keySet()){
			DerivativeReturn dr = dseResults.get(shortName)[0];
			printDr(shortName, dr, sense);
				
		}

	}
    
             
