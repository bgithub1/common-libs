package com.billybyte.deprecated;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;

public class CommodToStockEtfConversionQuery implements QueryInterface<String, String>{
	private final Map<String,String> replaceMap = new HashMap<String, String>();
	
	
	public CommodToStockEtfConversionQuery(){
		replaceMap.put("NG.FUT","UNG.STK.SMART");
		replaceMap.put("CL.FUT","USO.STK.SMART");
		replaceMap.put("HO.FUT","USO.STK.SMART");
		replaceMap.put("RB.FUT","USO.STK.SMART");
		replaceMap.put("GC.FUT","GLD.STK.SMART");
		replaceMap.put("SI.FUT","SLV.STK.SMART");
		replaceMap.put("ES.FUT","SPY.STK.SMART");
		replaceMap.put("ZN.FUT","TBF.STK.SMART");
		replaceMap.put("ZB.FUT","TBF.STK.SMART");
	}

	@Override
	public String get(String key, int timeoutValue, TimeUnit timeUnitType) {
		if(key==null) return null;
		String newCorrSn=key;
		String[] keys = key.split("__");
		if(keys.length<2)return null;
		if(keys[0].contains(".STK.") && keys[1].contains(".FUT.")){
			String[] parts = keys[1].split("\\.");
			String replaceKey = parts[0]+"."+parts[1];
			if(!replaceMap.containsKey(replaceKey))return null;
			String replaceSn = replaceMap.get(replaceKey);
			newCorrSn = reAssembleCorrKey(keys[0], replaceSn);
		}else if(keys[1].contains(".STK.") && keys[0].contains(".FUT.")){
			String[] parts = keys[0].split("\\.");
			String replaceKey = parts[0]+"."+parts[1];
			if(!replaceMap.containsKey(replaceKey))return null;
			String replaceSn = replaceMap.get(replaceKey);
			newCorrSn = reAssembleCorrKey(replaceSn,keys[1]);
		}
		return newCorrSn;
	}
	String reAssembleCorrKey(String p0, String p1){
		if(p0.compareTo(p1)<=0){
			return p0+"__"+p1;
		}
		return p1+"__"+p0;
	}

}
