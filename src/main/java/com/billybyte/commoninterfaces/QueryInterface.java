package com.billybyte.commoninterfaces;

import java.util.concurrent.TimeUnit;
/**
 * QueryInterface is used to build classes that can are "Map like", 
 *   but that can come from any source (real-time sources) and can time-out
 *   
 * @author bperlman1
 *
 * @param <K>
 * @param <V>
 */
public interface QueryInterface<K,V> {
	public V get(K key, int timeoutValue, TimeUnit timeUnitType);
}
