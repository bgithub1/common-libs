package com.billybyte.dse.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Settlement query of sets from the query of QueryInterface<String,SettlementDataInterface> 
 *   on port WsPort.SETTLE_ALL. 
 * @author bperlman1
 *
 */
public class SettleSetQueryFromWebService implements QueryInterface<Set<String>,Map<String,ComplexQueryResult<SettlementDataInterface>>>{
	private final SettleSingleFromWebServ settleSingle;
	/**
	 * 
	 * @param urlAs - pass a single space " " if you want the query to not have an application
	 * 		server talked to the WsPort.SETTLE_ALL port.
	 * @param urlWs - valid url, like http://127.0.0.1
	 */
	public SettleSetQueryFromWebService(String urlAs,String urlWs){
		this.settleSingle = new SettleSingleFromWebServ(urlAs, urlWs);
	}
	
	@Override
	public Map<String, ComplexQueryResult<SettlementDataInterface>> get(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		Map<String,ComplexQueryResult<SettlementDataInterface>> ret = 
				new HashMap<String, ComplexQueryResult<SettlementDataInterface>>();
		for(String key:keySet){
			ret.put(key, this.settleSingle.get(key, timeoutValue, timeUnitType));
		}
		return ret;
	}
	
}
