package com.billybyte.commoncollections;


import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;


public class MapFromMap<K,V> implements Map<K,V> {
	/**
	 * 
	 * Map from a csv file
	 * @param csvNameOrPath
	 * @param classInPkgOfReource
	 * @param colNameOfKey
	 * @param classOfKey
	 * @param colNameOfData
	 * @param classOfData
	 */
	@SuppressWarnings("unchecked")
	public MapFromMap(
			String csvNameOrPath,
			Class<?> classInPkgOfReource,
			String colNameOfKey,
			Class<?> classOfKey,
			String colNameOfData,
			Class<?> classOfData){
		super();
		@SuppressWarnings("rawtypes")
		Map map = CollectionsStaticMethods.mapFromCsv(
				classInPkgOfReource, csvNameOrPath,
				colNameOfKey, classOfKey, 
				colNameOfData, classOfData);
		setMap(map);
	}
	
	/**
	 * make a map of objects of classOfData, where the key is the
	 *    colNameOfKey
	 *    
	 * @param csvNameOrPath
	 * @param classInPkgOfReource
	 * @param colNameOfKey
	 * @param classOfKey
	 * @param classOfData
	 */
	public MapFromMap(
			String csvNameOrPath,
			Class<?> classInPkgOfReource,
			String colNameOfKey,
			Class<K> classOfKey,
			Class<V> classOfData){
		
		setMap(CollectionsStaticMethods.mapFromCsv(csvNameOrPath, classInPkgOfReource, colNameOfKey, classOfKey, classOfData));
 	}
 
	public MapFromMap(){
		super();
	}
	
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

}
