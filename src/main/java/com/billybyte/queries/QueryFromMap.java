package com.billybyte.queries;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;

public class QueryFromMap<K,V> implements QueryInterface<K, V> {
	private final Map<K, V> map;

	@Override
	public V get(K key, int timeoutValue, TimeUnit timeUnitType) {
		return map.get(key);
	}

	public QueryFromMap(Map<K, V> map) {
		super();
		this.map = map;
	}
	
	
}
