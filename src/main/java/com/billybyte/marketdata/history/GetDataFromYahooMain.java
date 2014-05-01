package com.billybyte.marketdata.history;

import java.util.Calendar;
import java.util.List;
import java.util.Set;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.HistData;
import com.billybyte.commonstaticmethods.HistDataSources;
import com.billybyte.commonstaticmethods.Utils;

public class GetDataFromYahooMain {
	public static void main(String[] args) {
		Set<String> snSet = 
				CollectionsStaticMethods.setFromArray(new String[]{
						"IBM","GE","AAPL"
				});
		Calendar today = Calendar.getInstance();
		Calendar before = Dates.addToCalendar(today, -100, Calendar.DAY_OF_YEAR, true);
		long todayLong = today.getTimeInMillis();
		long beforeLong = before.getTimeInMillis();
				
		for(String sn:snSet){
			List<HistData> l = 
					HistDataSources.getYahooDailyHistData(sn, beforeLong, todayLong);
			Utils.prt("shortName: "+sn);
			CollectionsStaticMethods.prtListItems(l);
		}
	}
}
