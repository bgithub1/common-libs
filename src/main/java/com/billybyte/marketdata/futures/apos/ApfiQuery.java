package com.billybyte.marketdata.futures.apos;

import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.queries.ComplexQueryResult;

public class ApfiQuery implements QueryInterface<String, ComplexQueryResult<AveragePriceFutureInfo>>{
	private final QueryInterface<String , SecDef> sdQuery;
	private final QueryInterface<String, SettlementDataInterface> setQuery;
	private final NymexAveragePriceSecDefQuery apoUnderlyingsQuery = 
			new NymexAveragePriceSecDefQuery();
	
	public ApfiQuery(QueryInterface<String, SecDef> sdQuery,
			QueryInterface<String, SettlementDataInterface> setQuery) {
		super();
		this.sdQuery = sdQuery;
		this.setQuery = setQuery;
	}
	
	public ApfiQuery(QueryInterface<String , SettlementDataInterface> settleQuery) {
		this(new SecDefQueryAllMarkets(), 
				settleQuery);
	}

	

	@Override
	public ComplexQueryResult<AveragePriceFutureInfo> get(String key,
			int timeoutValue, TimeUnit timeUnitType) {
		SecDef apoSecDef = getMainSecDef(key,timeoutValue,timeUnitType);
		if(apoSecDef==null){
			return errRet(key,"no SecDef");
		}
		SettlementDataInterface settle = getMainSettle(key,timeoutValue,timeUnitType);
		if(settle==null){
			return errRet(key,"no Settle");
		}
		SecDef[] underSds = getUnderlyingSecDefs(key,timeoutValue,timeUnitType);
		if(underSds==null){
			return errRet(key,"can't find all underlying SecDefs");
		}
		SettlementDataInterface[] underSets = getUnderlyingSettles(underSds,timeoutValue,timeUnitType);
		if(underSets==null){
			return errRet(key,"can't find all underlying settles");
		}
		ComplexQueryResult<AveragePriceFutureInfo> apfiResult = 
				AveragePriceFutureInfo.getAveragePriceFutureInfo(
						apoSecDef, settle, 
						underSds, underSets);
		return apfiResult;
	}
	
	private SecDef getMainSecDef(String key,int timeoutValue, TimeUnit timeUnitType){
		return sdQuery.get(key, timeoutValue, timeUnitType);
	}

	private SettlementDataInterface getMainSettle(String key,int timeoutValue, TimeUnit timeUnitType){
		return setQuery.get(key, timeoutValue, timeUnitType);
	}
	
	private  SecDef[] getUnderlyingSecDefs(String key,int timeoutValue, TimeUnit timeUnitType){
		return apoUnderlyingsQuery.get(key, timeoutValue, timeUnitType);
	}
	
	private SettlementDataInterface[] getUnderlyingSettles(SecDef[] sds,int timeoutValue, TimeUnit timeUnitType){
		SettlementDataInterface[] ret = new SettlementDataInterface[sds.length];
		for(int i = 0;i<sds.length;i++){
			ret[i] = setQuery.get(sds[i].getShortName(), timeoutValue, timeUnitType);
		}
		return ret;
	}
	
	private ComplexQueryResult<AveragePriceFutureInfo> errRet(String key,String s){
		Exception e = Utils.IllState(this.getClass(),key+" : "+s);
		return new ComplexQueryResult<AveragePriceFutureInfo>(e, null);
	}
}
