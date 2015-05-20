package com.billybyte.commoninterfaces;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;

/**
 * Like QueryInterface, but for keys that are sets.
 *   The user is returned a Tuple of the original keyset, and a map of data.
 * @author bperlman1
 *
 * @param <K>
 * @param <V>
 */
public interface QuerySetInterface<K,V> {
	public Tuple<Set<K>, Map<K,V>> get(Set<K> keySet,int timeoutValue, TimeUnit timeUnitType);
}
