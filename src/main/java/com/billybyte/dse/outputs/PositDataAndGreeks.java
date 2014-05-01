package com.billybyte.dse.outputs;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.billybyte.commoninterfaces.CsvToString;
import com.billybyte.portfolio.PositionData;

public class PositDataAndGreeks implements CsvToString{
	private final String withRespectTo;
	private final Map<DerivativeSensitivityTypeInterface, BigDecimal> unitSensitivityMap;
	private final PositionData posData;
	
	public PositDataAndGreeks(PositionData posData,
			Map<DerivativeSensitivityTypeInterface, BigDecimal> unitSensitivityMap,
			String withRespectTo) {
		super();
		this.posData = posData;
		this.unitSensitivityMap = unitSensitivityMap;
		this.withRespectTo = withRespectTo;
	}
	
	
	public PositionData getPosData() {
		return posData;
	}


	public Map<DerivativeSensitivityTypeInterface, BigDecimal> getSensitivityMap() {
		return unitSensitivityMap;
	}
	
	public BigDecimal getUnitSensitivity(DerivativeSensitivityTypeInterface sense){
		return getSensitivityMap().get(sense);
	}


	public String getWithRespectTo() {
		return withRespectTo;
	}


	@Override
	public String toString() {
		String ret = (withRespectTo==null?"null":withRespectTo)+",";
		
		if(this.unitSensitivityMap!=null){
			for(Entry<DerivativeSensitivityTypeInterface, BigDecimal> entry:unitSensitivityMap.entrySet()){
//				ret=ret+entry.getKey()+":"+entry.getValue().toString()+",";
				ret=ret+entry.getValue().toString()+",";
			}
		}
		
		ret = ret+posData.toString();
		
		return ret;
	}


	@Override
	public String[] getHeaderStrings() {
		List<String> retList = new ArrayList<String>();
		retList.add("withRespectTo");
		for(Entry<DerivativeSensitivityTypeInterface, BigDecimal> entry:unitSensitivityMap.entrySet()){
			retList.add(entry.getKey().toString());
		}
		retList.addAll(Arrays.asList(posData.getHeaderStrings()));
		return retList.toArray(new String[]{});
	}

}
