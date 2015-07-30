package com.billybyte.dse.inputs.diotypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.billybyte.commoncollections.TypedMapKey;
import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

/**
 * 
 * @author bperlman1
 *
 * @param <T>
 */
public abstract class DioType<T> extends TypedMapKey<DseInputQuery<T>> implements Comparable<DioType<?>>{
	public DioType(String name) {
		super(name);
	}

	public List<T> getUnderlyingInputs(InBlk inputs) {
		SecDef[] sds = inputs.getUnderlyingSds();
		Map<String, ComplexQueryResult<T>> inputsPerDiotMap =
				inputs.getUnderlyingDiotMap(this);
		List<T> ret = new ArrayList<T>();
		for(int i=0;i<sds.length;i++){
			String sn = sds[i].getShortName();
			ComplexQueryResult<T> cqr = inputsPerDiotMap.get(sn);
			if(cqr==null || !cqr.isValidResult()){
				ret.add(null);
			}
			ComplexQueryResult<T> cqrT = inputsPerDiotMap.get(sn);
			if(cqrT==null){
				throw Utils.IllState(this.getClass(), "null return from inputsPerDiotMap.get(sn) with sn="+sn);
			}
			if(!cqrT.isValidResult()){
				throw Utils.IllState(this.getClass(), "invalid Cqr return from inputsPerDiotMap.get(sn) with sn="+sn+ " Car message = "+cqrT.getException().getMessage());
			}
			T t = cqrT.getResult();
			ret.add(t);
		}
		
		return ret;
	}

	public T getMainInputs(InBlk inputs) {
		SecDef sd = inputs.getMainSd();
		Map<String, ComplexQueryResult<T>> inputsPerDiotMap =
				inputs.getUnderlyingDiotMap(this);
		if(inputsPerDiotMap==null)return null;
		String sn = sd.getShortName();
		ComplexQueryResult<T> cqr = inputsPerDiotMap.get(sn);
		if(cqr==null || !cqr.isValidResult()){
			return null;
		}
		T t = inputsPerDiotMap.get(sn).getResult();
		return t;
	}

	@Override
	public int compareTo(DioType<?> o) {
		return name().compareTo(o.name());
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		return name().compareTo(((DioType)obj).name())==0;
	}
	
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return name();
	}

	
}
