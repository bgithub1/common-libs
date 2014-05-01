package com.billybyte.dse.inputs;

import java.util.Map;


import com.billybyte.commoncollections.TypedMap;
import com.billybyte.commoncollections.TypedMapKey;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.queries.ComplexQueryResult;

public class UnderlyingInfo {

	private final TypedMap map =
			new TypedMap();

	public <I>  void putDiotMap(			
			DioType<I> diot,
			Map<String, ComplexQueryResult<I>> diotResultsMap){
		
		// create a key for the map
		TypedMapKey<Map<String, ComplexQueryResult<I>>> mapKey = 
				new TypedMapKey<Map<String,ComplexQueryResult<I>>>(diot.name());
		map.put(mapKey, diotResultsMap);
	}

	public  void putDiotMapUnspecifiedType(			
			DioType<?> diot,
			Map<String, ComplexQueryResult<?>> diotResultsMap){
		
		// create a key for the map
		TypedMapKey<Map<String, ComplexQueryResult<?>>> mapKey = 
				new TypedMapKey<Map<String,ComplexQueryResult<?>>>(diot.name());
		map.put(mapKey, diotResultsMap);
	}

	
	public <I> Map<String,ComplexQueryResult<I>> getMap(DioType<I> diot){
		TypedMapKey<Map<String, ComplexQueryResult<I>>> mapKey = 
				new TypedMapKey<Map<String,ComplexQueryResult<I>>>(diot.name());
		return map.get(mapKey);
	}
	
	
	public UnderlyingInfo makeCopy(){
		UnderlyingInfo copy = new UnderlyingInfo();
		copy.map.cloneMap(this.map);
		return copy;
	}
}