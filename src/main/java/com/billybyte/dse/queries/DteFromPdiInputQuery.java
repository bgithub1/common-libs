package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;


public class DteFromPdiInputQuery extends DseInputQuery<BigDecimal>{
	private final QueryInterface<String, SecDef> sdQuery ;
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery;
	private final long evalDate;
	public DteFromPdiInputQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			Long evalDate) {
		this.sdQuery = sdQuery;
		this.pdiQuery = pdiQuery;
		this.evalDate = evalDate==null ? Dates.getYyyyMmDdFromCalendar(Calendar.getInstance()) : evalDate;
	}


	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<PriceDisplayInterface>> pdiMap = 
				pdiQuery.get(keySet, timeoutValue, timeUnitType);
		Map<String,ComplexQueryResult<BigDecimal>> ret = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(String sn:keySet){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			if(sd==null){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(sn+" has no SecDef");
				ret.put(sn, errCqr);
				continue;
			}
			if(pdiMap==null){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(sn+" has no Settlement due to null return from settlement set query");
				ret.put(sn, errCqr);
				continue;
			}
			ComplexQueryResult<PriceDisplayInterface> pdiCqr = 
					pdiMap.get(sn);
			if(!pdiCqr.isValidResult()){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(pdiCqr.getException());
				ret.put(sn, errCqr);
				continue;
			}
			PriceDisplayInterface pdi = pdiCqr.getResult();
			Long today = this.evalDate;
			Calendar cToday = Dates.getCalenderFromYYYYMMDDLong(today);
			BigDecimal dte =  new BigDecimal(MarketDataComLib.getDteFromSd(cToday, sd));
			ret.put(sn,new ComplexQueryResult<BigDecimal>(null,dte));
		}
		return ret;
	}
}
