package com.billybyte.commoninterfaces;

import java.util.concurrent.TimeUnit;

public interface QueryInterface<K,V> {
	public V get(K key, int timeoutValue, TimeUnit timeUnitType);
}
