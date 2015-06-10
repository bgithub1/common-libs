package com.billybyte.dse.debundles;

import com.billybyte.dse.queries.PdiDseQueryFromCqrPdiQuery;
import com.billybyte.marketdata.YahooCombinedStkOptPdiSetCqrRetQuery;

public class DerivativeSetEngineForStocks {
	public static void main(String[] args) {
		// atm
		YahooCombinedStkOptPdiSetCqrRetQuery innerPdiQuery = 
				new YahooCombinedStkOptPdiSetCqrRetQuery();
		PdiDseQueryFromCqrPdiQuery atmPdiQuery = 
				new PdiDseQueryFromCqrPdiQuery(innerPdiQuery);
		
		// vol
		// dte
		// rate
		// div
		// 
	}
}
