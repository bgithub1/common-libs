package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;


public class DteFromPdiInputQuery extends DseInputQueryFromPdiQuery<BigDecimal>{
	/**
	 * 
	 * @param sdQuery
	 * @param pdiQuery
	 * @param evalDate
	 */
	public DteFromPdiInputQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			Long evalDate) {
		super(sdQuery,
				pdiQuery,
				evalDate==null ? Dates.getYyyyMmDdFromCalendar(Calendar.getInstance()) : evalDate);
	}

	public DteFromPdiInputQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			Calendar evalDate) {
		this(sdQuery,pdiQuery,
				Dates.getYyyyMmDdFromCalendar(evalDate));
	}

	@Override
	public ComplexQueryResult<BigDecimal> getValue(PriceDisplayInterface pdi,
			SecDef sd, Long evalDate) {
		Calendar cToday = Dates.getCalenderFromYYYYMMDDLong(evalDate);
		BigDecimal dte =  new BigDecimal(MarketDataComLib.getDteFromSd(cToday, sd));
		return new ComplexQueryResult<BigDecimal>(null, dte);
	}

}
