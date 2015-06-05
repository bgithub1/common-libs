package com.billybyte.marketdata;

import java.awt.HeadlessException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.queries.ComplexQueryResult;
import com.billybyte.ui.messagerboxes.MessageBox.MessageBoxNonModalWithTextBox;

/**
 * show message box to ask for comma sep names of stock prices to get
 * @author bperlman1
 *
 */
public class RunYahooQuerys {
	public static void main(String[] args) {
//		YahooFinanceCsvPdiQuery yqlStkQuery = new YahooFinanceCsvPdiQuery();
//		YqlOptionPdiQuery yqlOptQuery = new YqlOptionPdiQuery();
		YahooCombinedStkOptPdiSetCqrRetQuery  yahooAllQuery = new YahooCombinedStkOptPdiSetCqrRetQuery();
		new LocalMb(yahooAllQuery);

		
	}

	private static class LocalMb extends MessageBoxNonModalWithTextBox{
		private final YahooCombinedStkOptPdiSetCqrRetQuery  yahooAllQuery;
		public LocalMb(
				YahooCombinedStkOptPdiSetCqrRetQuery  yahooAllQuery)
				throws HeadlessException {
			
			super("ENTER STOCK SYMBOLS, LIKE MSFT.STK.SMART, IBM.OPT.SMART.USD.20160115.C.200.00,AAPL.STK.SMART,GE.STK.SMART", 
						"YQL QUERY", 
						"MSFT.STK.SMART," + 
						"IBM.OPT.SMART.USD.20160115.C.200.00," + 
						"AAPL.STK.SMART,GE.STK.SMART"
			);
			this.yahooAllQuery = yahooAllQuery;
		}

		@Override
		protected void processCommaSepValuesAndDisplay(
				String[] messageBoxResponseCommaSepValues) {
			Set<String> keySet = 
					CollectionsStaticMethods.setFromArray(messageBoxResponseCommaSepValues);
			Map<String,ComplexQueryResult<PriceDisplayInterface>> cqrMap =
					yahooAllQuery.get(keySet, 10, TimeUnit.SECONDS);
			for(Entry<String, ComplexQueryResult<PriceDisplayInterface>> entry:cqrMap.entrySet()){
				Utils.prt(entry.getValue().toString());
			}
			
		}

		@Override
		protected void processCsvDataAndDisplay(List<String[]> csvData) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected MessageBoxNonModalWithTextBox newInstance() {
			
			return new LocalMb(yahooAllQuery);
		}
		
	}
}