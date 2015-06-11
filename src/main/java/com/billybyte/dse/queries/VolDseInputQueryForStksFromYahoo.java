package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.YahooAtmVolCqrQueryForStks;
import com.billybyte.marketdata.YahooHistoricalVolCqrQuery;
import com.billybyte.marketdata.YahooOptionVolCqrQueryForOpts;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.queries.ComplexQueryResult;

public class VolDseInputQueryForStksFromYahoo extends DseInputQuery<BigDecimal>{
	private final QueryInterface<Set<String>,Map<String, ComplexQueryResult<BigDecimal>>> volForStks;
	private final QueryInterface<Set<String>,Map<String, ComplexQueryResult<BigDecimal>>> histVolForStks;
	private final QueryInterface<Set<String>,Map<String, ComplexQueryResult<BigDecimal>>> volForOpts;
	private final QueryInterface<String,SecDef> sdQuery;
	
	public VolDseInputQueryForStksFromYahoo(
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> volForStks,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> histVolForStks,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<BigDecimal>>> volForOpts,
			QueryInterface<String,SecDef> sdQuery) {
		
		super();
		this.volForStks = volForStks;
		this.histVolForStks = histVolForStks;
		this.volForOpts = volForOpts;
		this.sdQuery = sdQuery;
	}

	public VolDseInputQueryForStksFromYahoo(){
		this(new YahooAtmVolCqrQueryForStks(),
		new YahooHistoricalVolCqrQuery(),
		new YahooOptionVolCqrQueryForOpts(),
		new SecDefQueryAllMarkets());
	}
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		// Create return object
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		// divide input keySet into that for stks and that for options
		Set<String> stkKeySet = new HashSet<String>();
		Set<String> optKeySet = new HashSet<String>();
		for(String key : keySet){
			SecDef sd = sdQuery.get(key, timeoutValue, timeUnitType);
			if(sd.getSymbolType()==SecSymbolType.STK){
				stkKeySet.add(key);
				continue;
			}else if(sd.getSymbolType()==SecSymbolType.OPT){
				optKeySet.add(key);
				continue;
			}else{
				Exception e = 
						Utils.IllState(this.getClass(), key+" is not a STK or an OPT");
				ComplexQueryResult<BigDecimal> errCqr = new ComplexQueryResult<BigDecimal>(e, null);
				ret.put(key, errCqr);
				continue;
			}
		}
		
		Map<String, ComplexQueryResult<BigDecimal>> stkRet =
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		if(volForStks!=null){
			stkRet = 
					volForStks.get(stkKeySet, timeoutValue, timeUnitType);
		}
		// see if any of the stocks need to use historical vol (they might be mutual funds, for example
		HashSet<String> histVolStkSet = new HashSet<String>();
		for(String stkSn : stkKeySet){
			ComplexQueryResult<BigDecimal> stkCqr = stkRet.get(stkSn);
			if(stkCqr==null){
				histVolStkSet.add(stkSn);
			}else if(!stkCqr.isValidResult()){
				histVolStkSet.add(stkSn);
				stkRet.remove(stkSn);
			}
		}
		// Put stocks that are still good in return map.  
		ret.putAll(stkRet);

		
		// Now try to get historical vols for those stocks that didn't have an implied vol
		if(histVolStkSet.size()>0 && histVolForStks!=null){
			Map<String, ComplexQueryResult<BigDecimal>> histVolRet = 
					this.histVolForStks.get(histVolStkSet, timeoutValue, timeUnitType);
			for(String stkSn : histVolStkSet){
				ComplexQueryResult<BigDecimal> histVolCqr = 
						histVolRet.get(stkSn);
				if(histVolCqr==null || !histVolCqr.isValidResult()){
					Exception e = Utils.IllState(this.getClass(), stkSn+" Can't get either implied or historical vol");
					ret.put(stkSn, new ComplexQueryResult<BigDecimal>(e, null));
				}else{
					ret.put(stkSn, histVolCqr);
				}
			}
		}
		
		Map<String, ComplexQueryResult<BigDecimal>> optRet = 
				volForOpts.get(optKeySet, timeoutValue, timeUnitType);
		ret.putAll(optRet);
		
		for(String sn : keySet){
			if(!ret.containsKey(sn)){
				ret.put(sn, new ComplexQueryResult<BigDecimal>(Utils.IllState(this.getClass(), 
						sn+" can't find vol"), null));
			}
		}
		return ret;
	}
	
	public static void main(String[] args) {
		VolDseInputQueryForStksFromYahoo volDseInQuery = 
				new VolDseInputQueryForStksFromYahoo();
		String[] array = 
		{
			"IBM.STK.SMART",
			"IBM.OPT.SMART.USD.20170120.C.170.00",
			"AAPL.STK.SMART",
			"AAPL.OPT.SMART.USD.20170120.C.150.00",
			"MSFT.STK.SMART",
			"MSFT.OPT.SMART.USD.20170120.C.45.00",
			"GOOG.STK.SMART",
			"GOOG.OPT.SMART.USD.20170120.C.600.00",
			"RYMEX.STK.SMART"
		};
		Set<String> keySet = CollectionsStaticMethods.setFromArray(array);
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				volDseInQuery.get(keySet,20,TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(ret);
		// do it again with cache
		Utils.prt("");
		Utils.prt("do it again from cache");
		ret = 
				volDseInQuery.get(keySet,20,TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(ret);
		
	}

}
