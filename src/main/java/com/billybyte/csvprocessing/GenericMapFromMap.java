package com.billybyte.csvprocessing;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("genericMapFromMap")
public class  GenericMapFromMap<K,V> implements Map<K,V> {
	@XStreamAsAttribute
	private String name;
	private Map<K,V> map;
	
	
	public Map<K,V> getMap() {
		return map;
	}
	public void setMap(Map<K,V> map) {
		this.map = map;
	}

	@Override
	public void clear() {
		getMap().clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return getMap().containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return getMap().containsValue(value);
	}

	@Override
	public Set<Map.Entry<K,V>> entrySet() {
		return getMap().entrySet();
	}

	@Override
	public V get(Object key) {
		return getMap().get(key);
	}

	@Override
	public boolean isEmpty() {
		return getMap().isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return getMap().keySet();
	}

	@Override
	public V put(K key, V value) {
		return null;
	}

	@Override
	public void putAll(Map<? extends K,? extends V> m) {
		getMap().putAll(m);
	}

	@Override
	public V remove(Object key) {
		return getMap().remove(key);
	}

	@Override
	public int size() {
		return getMap().size();
	}

	@Override
	public Collection<V> values() {
		return getMap().values();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
