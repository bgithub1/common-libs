package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.queries.ComplexQueryResult;
import com.billybyte.queries.SettleQueryFromMongo;
/**
 * Get SettlementDataImmute from webservice or mongo, and turn it into BigDecimal 
 * @author bperlman1
 *
 */
public class SettleToBigDecSetQuery extends DseInputQuery<BigDecimal>{
	private final QueryInterface<Set<String>,Map<String,ComplexQueryResult<SettlementDataInterface>>> setQuery;

	/**
	 * Get SettlementDataImmute from webservice, and turn it into BigDecimal 
	 * @param urlAs 
	 * @param urlWs
	 */
	public SettleToBigDecSetQuery(String urlAs, String urlWs){
		this.setQuery = new SettleSetQueryFromWebService(urlAs, urlWs);
	}
	/**
	 * 
	 * Get SettlementDataImmute from mongoDb, and turn it into BigDecimal
	 * @param ipOfMongo - like 127.0.0.1
	 * @param portOfMongo - like 27017
	 * @param initialRegexGetPreFetchKey - like "\\.((NG)|(LNE))\\.((FUT)|(FOP)).((2012)|(2013)|2014))")
	 */
	public SettleToBigDecSetQuery(String ipOfMongo,Integer portOfMongo,
			String initialRegexGetPreFetchKey){
		this.setQuery = new SettleQueryFromMongo(ipOfMongo, 
				portOfMongo, "\\.((NG)|(LNE))\\.((FUT)|(FOP)).((2012)|(2013)|2014))");

	}
	/**
	 * 
	 * @param setQuery - QueryInterface<Set<String>,
				Map<String,ComplexQueryResult<SettlementDataInterface>>>
	 */
	public SettleToBigDecSetQuery(
			QueryInterface<Set<String>,
				Map<String,ComplexQueryResult<SettlementDataInterface>>> setQuery){
		this.setQuery = setQuery;
	}
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<SettlementDataInterface>> cqrMap = 
				this.setQuery.get(keySet, timeoutValue, timeUnitType);
		Map<String,ComplexQueryResult<BigDecimal>> cqrMapBd = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(Entry<String, ComplexQueryResult<SettlementDataInterface>> entry:cqrMap.entrySet()){
			ComplexQueryResult<SettlementDataInterface> cqr = entry.getValue();
			if(cqr==null || !cqr.isValidResult()){
				ComplexQueryResult<BigDecimal> cqrToRet = 
						MarketDataComLib.errorRet(cqr.getException());
				cqrMapBd.put(entry.getKey(),cqrToRet);
				continue;
			}
			cqrMapBd.put(entry.getKey(),
					new ComplexQueryResult<BigDecimal>(
							null,cqr.getResult().getPrice()));
		}
		return cqrMapBd;
	}
	
	

}
