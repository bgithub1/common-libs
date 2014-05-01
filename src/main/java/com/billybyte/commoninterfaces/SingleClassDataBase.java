package com.billybyte.commoninterfaces;

import java.util.List;

public interface SingleClassDataBase <K,T> {
	public T get(String field,K key);
	public List<T> getAll();
	public void put(T t);
	public void deleteAll();
	public void saveDataBase();
	public void open();
	public void close();
}
