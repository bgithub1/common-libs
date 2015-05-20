package com.billybyte.commoncollections;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

public class Tuple<T1,T2> {
	private final T1 t1_instance;
	private final T2 t2_instance;
	
	@SuppressWarnings("unused")
	private Tuple(){
		this.t1_instance= null;
		this.t2_instance = null;
	}

	public Tuple(T1 t1_instance, T2 t2_instance) {
		super();
		this.t1_instance = t1_instance;
		this.t2_instance = t2_instance;
	}

	public T1 getT1_instance() {
		return t1_instance;
	}

	public T2 getT2_instance() {
		return t2_instance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((t1_instance == null) ? 0 : t1_instance.hashCode());
		result = prime * result
				+ ((t2_instance == null) ? 0 : t2_instance.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Tuple other = (Tuple) obj;
		if (t1_instance == null) {
			if (other.t1_instance != null)
				return false;
		} else if (!t1_instance.equals(other.t1_instance))
			return false;
		if (t2_instance == null) {
			if (other.t2_instance != null)
				return false;
		} else if (!t2_instance.equals(other.t2_instance))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return t1_instance.toString() + ","
				+ t2_instance.toString();
	}
	
	public static String createLaunchXml(Class<?> clazz,String[] args){
		XStream xs = new XStream();
		List<Tuple<Class<?>, String[]>> outList = new ArrayList<Tuple<Class<?>,String[]>>();
		Tuple<Class<?>, String[]> argTup = 
				new Tuple<Class<?>, String[]>(clazz, args);
		outList.add(argTup);
		return xs.toXML(outList)	;	

	}

	
}
