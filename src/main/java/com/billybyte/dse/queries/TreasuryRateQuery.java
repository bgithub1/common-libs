package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.clientserver.webserver.WebServiceComLib;
import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.queries.ComplexQueryResult;
import com.thoughtworks.xstream.XStream;

public class TreasuryRateQuery extends DseInputQuery<BigDecimal> {
	private final QueryInterface<Set<String>, Tuple<Set<String>, Map<String, BigDecimal>>> rateService;

	public TreasuryRateQuery(String rateSbString){
		this(new ServiceBlock(rateSbString));
	}
	
	public TreasuryRateQuery(ServiceBlock rateSb){
		this.rateService = WebServiceComLib.getQueryService(
				rateSb, new XStream());
	}
	
	

	
	@Override
	public Map<String, ComplexQueryResult<BigDecimal>> get(Set<String> key,
			int timeoutValue, TimeUnit timeUnitType) {
		Tuple<Set<String>, Map<String, BigDecimal>> retTup= rateService.get(key, timeoutValue, timeUnitType);
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(String badName:retTup.getT1_instance()){
			ComplexQueryResult<BigDecimal> cqr = 
					MarketDataComLib.errorRet(badName+" no rate found");
			ret.put(badName, cqr);
		}
		for(Entry<String, BigDecimal> goodEntry:retTup.getT2_instance().entrySet()){
			ComplexQueryResult<BigDecimal> cqr = 
					new ComplexQueryResult<BigDecimal>(null, goodEntry.getValue());
			ret.put(goodEntry.getKey(), cqr);
		}
		return ret;
	}
	
}
