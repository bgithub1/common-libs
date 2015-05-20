package com.billybyte.clientserver.webserver;

import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.simplewebservice.SimpleWebServiceProcessRequestInterface;
import com.thoughtworks.xstream.XStream;

public class QueryEngineForWebService<K,T> 
	implements SimpleWebServiceProcessRequestInterface<K, T>{
	private  final QueryInterface<K, T> queryEngine;
	private final int timeoutValue;
	private final TimeUnit timeUnitType;
	
	public QueryEngineForWebService(
			QueryInterface<K, T> queryEngine,
			int timeoutValue,TimeUnit timeUnitType) {
		super();
		this.queryEngine = queryEngine;
		this.timeoutValue = timeoutValue;
		this.timeUnitType = timeUnitType;
	}

	@Override
	public T processRequest(K requestKey, XStream xstream) {
		T ret = queryEngine.get(requestKey, timeoutValue, timeUnitType);
		if(ret==null){
			Utils.prtObErrMess(this.getClass(), " null return for key: "+requestKey.toString());
		}else{
		}
		return ret;
	}
}
