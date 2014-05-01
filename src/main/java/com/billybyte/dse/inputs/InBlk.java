package com.billybyte.dse.inputs;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;


import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;
/**
 * DO NOT TRY TO SEND THIS CLASS ACROSS A WEB SERVICE
 * @author bperlman1
 *
 */
public class InBlk {
	private final SecDef mainSd;
	private final SecDef[] underlyingSds;
	private final List<DioType<?>> dioTypeList;
	private final Calendar evaluationDate;
	private final transient UnderlyingInfo underlyingInfo;
	
	
	public InBlk(SecDef mainSd, SecDef[] underlyingSds,
			UnderlyingInfo underInputsMap,
			List<DioType<?>> dioTypeList,
			Calendar evaluationDate) {
		super();
		this.mainSd = mainSd;
		this.underlyingSds = underlyingSds;
		this.underlyingInfo = underInputsMap;
		this.dioTypeList = dioTypeList;
		this.evaluationDate = evaluationDate;
	}
	
	public SecDef getMainSd() {
		return mainSd;
	}
	public SecDef[] getUnderlyingSds() {
		return underlyingSds;
	}
	public UnderlyingInfo getUnderInputsMap() {
		return underlyingInfo;
	}
	
	/**
	 * return a List<I> of values that correspond to the secdefs in the 
	 *   underlyingSds array;
	 * @param diot
	 * @return
	 */
	public <I> List<I> getUnderLyingInputList(DioType<I> diot){
		Map<String,ComplexQueryResult<I>> inputsPerDioType = 
				underlyingInfo.getMap(diot);
		List<I> ret = new ArrayList<I>();
		for(SecDef underSd:underlyingSds){
			String sn  = underSd.getShortName();
			ComplexQueryResult<I> val = inputsPerDioType.get(sn);
			if(val==null || !val.isValidResult()){
				ret.add(null);
				continue;
			}
			ret.add(val.getResult());
		}
		return ret;
	}
	
	public <I> I getMainInputList(DioType<I> diot){
		Map<String,ComplexQueryResult<I>> inputsPerDioType = 
				underlyingInfo.getMap(diot);
		String sn = mainSd.getShortName();
		return inputsPerDioType.get(sn).getResult();
	}
	
	public <I> Map<String,ComplexQueryResult<I>> getUnderlyingDiotMap(DioType<I> diot){
		Map<String,ComplexQueryResult<I>> diotValuesPerShortNameMap = underlyingInfo.getMap(diot);
		return diotValuesPerShortNameMap;
	}
	
	public List<DioType<?>> getDioTypeList() {
		return dioTypeList;
	}
	
	@Override
	public String toString() {
		String ret = mainSd.toString()+";"+getUnderlyingSds().toString()+";";
		for(DioType<?> diot:getDioTypeList()){
			Map<String,?> map = underlyingInfo.getMap(diot);
			if(map==null){
				ret = ret+"no values for "+diot.name()+";";
			}else{
				for(SecDef sd:getUnderlyingSds()){
					Object o = map.get(sd.getShortName());
					ret = ret+(o==null?"null "+ diot.name()+" for "+sd.getShortName():o.toString())+";";				
				}
			}
		}
		return ret;
	}

	public Calendar getEvaluationDate() {
		return evaluationDate;
	}
}
