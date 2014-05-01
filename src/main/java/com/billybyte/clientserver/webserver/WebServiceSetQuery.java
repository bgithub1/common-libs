package com.billybyte.clientserver.webserver;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QuerySetInterface;
import com.thoughtworks.xstream.XStream;

public class WebServiceSetQuery<K,V> implements QuerySetInterface<K,V> {

	private final WebServiceQuery<Set<K>,Tuple<Set<K>,Map<K,V>>> innerQuery;
	
	public WebServiceSetQuery(ServiceBlock sb, XStream xs){
		this.innerQuery = 
				new WebServiceQuery<Set<K>, Tuple<Set<K>,Map<K,V>>>(
						xs, sb.getUrlOfApplicationServer(), sb.getPortOfService().toString(),
						sb.getNameOfService());
	}
	
	@Override
	public Tuple<Set<K>, Map<K, V>> get(Set<K> keySet, int timeoutValue,
			TimeUnit timeUnitType) {
		return innerQuery.get(keySet, timeoutValue, timeUnitType);
	}
	
}
