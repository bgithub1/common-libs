package com.billybyte.clientserver.webserver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QuerySetInterface;
import com.thoughtworks.xstream.XStream;

public class AppServiceClientSetQuery<K,V> implements QuerySetInterface<K,V> {

	private final AppServiceClientQuery<Set<K>, Tuple<Set<K>,Map<K,V>>> clientQuery;
	
	public AppServiceClientSetQuery(ServiceBlock sb, XStream xs){
		this.clientQuery = new AppServiceClientQuery<Set<K>, Tuple<Set<K>,Map<K,V>>>(sb, xs);
	}
	
	
	@Override
	public Tuple<Set<K>, Map<K, V>> get(Set<K> keySet, int timeoutValue,
			TimeUnit timeUnitType) {
		return clientQuery.get(keySet, timeoutValue, timeUnitType);
	}

}
