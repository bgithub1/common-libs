package com.billybyte.marketdata;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;

public class SecDefQueryFromListOfSecDefQueries implements QueryInterface<String, SecDef>{
	private  final List<QueryInterface<String, SecDef>> queryList;
	
	
	@SuppressWarnings("unused")
	private SecDefQueryFromListOfSecDefQueries() {
		super();
		queryList = null;
	}


	public SecDefQueryFromListOfSecDefQueries(
			List<QueryInterface<String, SecDef>> queryList) {
		super();
		this.queryList = queryList;
	}


	@Override
	public SecDef get(String key, int timeoutValue, TimeUnit timeUnitType) {
		for(QueryInterface<String, SecDef> query:queryList){
			SecDef sd = query.get(key, timeoutValue, timeUnitType);
			if(sd!=null){
				return sd;
			}
		}
		return null;
	}

}
