package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;


public class DteFromSettlementDseInputQuery extends DseInputQuery<BigDecimal>{
	private final QueryInterface<String, SecDef> sdQuery ;
	private final QueryInterface<Set<String>, Map<String, ComplexQueryResult<SettlementDataInterface>>> mongoSettleQuery;
	public DteFromSettlementDseInputQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<SettlementDataInterface>>> mongoSettleQuery) {
		this.sdQuery = sdQuery;
		this.mongoSettleQuery = mongoSettleQuery;
	}


	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<SettlementDataInterface>> settleMap = 
				mongoSettleQuery.get(keySet, timeoutValue, timeUnitType);
		Map<String,ComplexQueryResult<BigDecimal>> ret = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(String sn:keySet){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			if(sd==null){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(sn+" has no SecDef");
				ret.put(sn, errCqr);
				continue;
			}
			if(settleMap==null){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(sn+" has no Settlement due to null return from settlement set query");
				ret.put(sn, errCqr);
				continue;
			}
			ComplexQueryResult<SettlementDataInterface> settleCqr = 
					settleMap.get(sn);
			if(!settleCqr.isValidResult()){
				ComplexQueryResult<BigDecimal> errCqr = 
						MarketDataComLib.errorRet(settleCqr.getException());
				ret.put(sn, errCqr);
				continue;
			}
			SettlementDataInterface settle = settleCqr.getResult();
			Long today = settle.getTime();
			Calendar cToday = Dates.getCalenderFromYYYYMMDDLong(today);
			BigDecimal dte =  new BigDecimal(MarketDataComLib.getDteFromSd(cToday, sd));
			ret.put(sn,new ComplexQueryResult<BigDecimal>(null,dte));
		}
		return ret;
	}
}
