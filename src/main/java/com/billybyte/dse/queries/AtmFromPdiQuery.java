package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.clientserver.webserver.WebServiceComLib;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.PriceLevelData;
import com.billybyte.queries.ComplexQueryResult;
import com.thoughtworks.xstream.XStream;

public class AtmFromPdiQuery extends DseInputQuery<BigDecimal>{

	final BigDecimal point5 = new BigDecimal(".5");
	final QueryInterface<Set<String>, Map<String,PriceDisplayInterface>> innerPdiQuery ;//= 
//			WebServiceComLib.getQueryService(
//					new ServiceBlock(",9223,http://127.0.0.1,pdiSetQuery"), new XStream());

	public AtmFromPdiQuery(QueryInterface<Set<String>, Map<String,PriceDisplayInterface>> innerPdiQuery){
		this.innerPdiQuery = innerPdiQuery;
	}
	public AtmFromPdiQuery(){
		innerPdiQuery = 
				WebServiceComLib.getQueryService(
						new ServiceBlock(DefaultValues.DEFAULT_PDI_SET_SB_STRING), new XStream());
	}
	public AtmFromPdiQuery(String sbString){
		innerPdiQuery = 
				WebServiceComLib.getQueryService(
						new ServiceBlock(sbString), new XStream());
	}
	
	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		// try to get pdi's
		Map<String,PriceDisplayInterface> retPdis = 
				waitForPdis(keySet);
		if(retPdis==null)return null;
		// this will be return map
		Map<String,ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		
		// go through key set and see if all pdi's were returned
		for(String sn :keySet){
			if(!retPdis.containsKey(sn)){
				// it's missing, so make a error return cqr
				ComplexQueryResult<BigDecimal> r = MarketDataComLib.errorRet(sn+" no PDI ");
				ret.put(sn,r) ;
				continue;
			}
			// get price data
			PriceDisplayInterface pdi = retPdis.get(sn);
			PriceLevelData plBid = pdi.getBid();
			PriceLevelData plAsk = pdi.getOffer();
			if(plBid==null || plBid.getPrice()==null ||
					plAsk==null || plAsk.getPrice()==null){
				// price data is shit, make error cqr
				ComplexQueryResult<BigDecimal> r = MarketDataComLib.errorRet(sn+" bad PDI ");
				ret.put(sn,r) ;
				continue;
			}
			// good price data, get the avergae, and make a cqr bigdecimal
			BigDecimal mid = plAsk.getPrice().add(plBid.getPrice()).multiply(point5);
			ret.put(sn, new ComplexQueryResult<BigDecimal>(null, mid));
		}
		// return the whole map
		return ret;
	}
	
	private Map<String,PriceDisplayInterface> waitForPdis(Set<String> snSet){
		boolean allGood = true;
		Map<String,PriceDisplayInterface> retPdis =null;
		boolean first=true;
		// try 10 times
		for(int i = 0;i<10;i++){
			Utils.prt("attempt: "+i+1);
			// don't sleep on the first time
			if(first){
				first=false;
			}else{
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			// get the results
			retPdis = 
					innerPdiQuery.get(snSet,10,TimeUnit.SECONDS);
			
			allGood=true;
			// loop throught them, to see if all is there
			for(String sn:snSet){
				if(retPdis==null || !retPdis.containsKey(sn)){
					allGood=false;
					continue;
				}
			}
			// if they are all good, break, otherwise loop
			if(allGood)break;
		
		}
		if(retPdis==null)return new HashMap<String, PriceDisplayInterface>();
		return retPdis;
	}
	 
	public boolean isValidPdiMap(Map<String, ComplexQueryResult<BigDecimal>> results,Set<String> snSet){
		boolean allGood = true;
		for(String sn:snSet){
			if(!results.containsKey(sn)){
				Utils.prtObErrMess(this.getClass(), sn+" : no pdi rec");
				allGood=false;
				continue;
			}
			if(!results.get(sn).isValidResult()){
				Utils.prtObErrMess(this.getClass(), sn+results.get(sn).getException().getMessage());
				continue;
			}
		}	
		return allGood;

	}
	
}
