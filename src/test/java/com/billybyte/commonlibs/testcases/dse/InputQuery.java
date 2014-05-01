package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.queries.ComplexQueryResult;
/**
 * Test query that gets a value from a map and returns that value to caller
 *   for each security in the keylist of the get
 * @author bperlman1
 *
 * @param <T>
 */
public abstract class InputQuery <T> extends DseInputQuery<T>{
	protected static final QueryInterface<String, SecDef> sdQuery = 
			new SecDefQueryAllMarkets();
	public abstract T newValue(SecInputsInfo testValue);
	protected final Map<String, SecInputsInfo> testDataMap;
	public InputQuery(
			Map<String, SecInputsInfo> testDataMap){
		this.testDataMap = testDataMap;
	}
	@Override
	public Map<String, ComplexQueryResult<T>> get(
			Set<String> keySet,
			int timeoutValue, TimeUnit timeUnitType) {
		
		Map<String,ComplexQueryResult<T>> ret = new HashMap<String, ComplexQueryResult<T>>();
		for(String key:keySet){
			SecInputsInfo testValue = testDataMap.get(key);
			if(testValue==null){
				testValue = new SecInputsInfo();
				testValue.shortName = key;
			}
			T nv = newValue(testValue);
			ret.put(key,new ComplexQueryResult<T>(null,nv));
		}
		return ret;
	}
	
	public Map<String, SecInputsInfo> getTestDataMap(){
		return this.testDataMap;
	}
}

