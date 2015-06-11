package com.billybyte.dse.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Abstract class for using pdiCqr query to get some generic dse input.
 * @author bperlman1
 *
 * @param <T>
 */
public abstract  class DseInputQueryFromPdiQuery<T> extends DseInputQuery<T>{
	public abstract ComplexQueryResult<T> getValue(PriceDisplayInterface pdi, SecDef sd, Long evalDate);
	protected final QueryInterface<String, SecDef> sdQuery ;
	protected final QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery;
	protected final long evalDate;

	
	/**
	 * 
	 * @param sdQuery
	 * @param pdiQuery
	 * @param evalDate
	 */
	public DseInputQueryFromPdiQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			long evalDate) {
		super();
		this.sdQuery = sdQuery;
		this.pdiQuery = pdiQuery;
		this.evalDate = evalDate;
	}



	@Override
	public Map<String, ComplexQueryResult<T>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<PriceDisplayInterface>> pdiMap = 
				pdiQuery.get(keySet, timeoutValue, timeUnitType);
		Map<String,ComplexQueryResult<T>> ret = new HashMap<String, ComplexQueryResult<T>>();
		for(String sn:keySet){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			if(sd==null){
				ComplexQueryResult<T> errCqr = 
						MarketDataComLib.errorRet(sn+" has no SecDef");
				ret.put(sn, errCqr);
				continue;
			}
			if(pdiMap==null){
				ComplexQueryResult<T> errCqr = 
						MarketDataComLib.errorRet(sn+" has no Settlement due to null return from settlement set query");
				ret.put(sn, errCqr);
				continue;
			}
			ComplexQueryResult<PriceDisplayInterface> pdiCqr = 
					pdiMap.get(sn);
			if(!pdiCqr.isValidResult()){
				ComplexQueryResult<T> errCqr = 
						MarketDataComLib.errorRet(pdiCqr.getException());
				ret.put(sn, errCqr);
				continue;
			}
			PriceDisplayInterface pdi = pdiCqr.getResult();
			ComplexQueryResult<T> result = 
					getValue(pdi, sd, evalDate);
			ret.put(sn,result);
		}
		return ret;
	}

}
