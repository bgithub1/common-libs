package com.billybyte.commoninterfaces;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;


public interface QuerySetInterface<K,V> {
	public Tuple<Set<K>, Map<K,V>> get(Set<K> keySet,int timeoutValue, TimeUnit timeUnitType);
}
