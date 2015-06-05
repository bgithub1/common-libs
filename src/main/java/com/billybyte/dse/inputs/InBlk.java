package com.billybyte.dse.inputs;


import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.DerivativeSetEngine;
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
	private final UnderlyingInfo underlyingInfo;
	
	
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
	
	public static List<String[]> getCsvListFromInBlkMap(
			DerivativeSetEngine dse,
			Map<String, ComplexQueryResult<InBlk>> cqrResults,
			int precision){
		// create the return object
		List<String[]> ret = new ArrayList<String[]>();
		
		// create a map that maps shortName to the values per that shortName
		Map<String,Map<String, String>> snVsTypeVsValue = new HashMap<String, Map<String,String>>();
		// create a TreeSet of all possible DioTypes used as inputs.  You'll use to create csv columns later on
		Set<String> allPossibleTypes = new TreeSet<String>();
		
		// iterate through shortNames
		for(String sn : cqrResults.keySet()){
			if(!cqrResults.containsKey(sn)){
				String[] line = {sn,"no cqr returned from dse"};
				ret.add(line);
				continue;
			}
			
			// get the InBlk for this shortName
			ComplexQueryResult<InBlk> cqr = 
					cqrResults.get(sn);
			if(!cqr.isValidResult()){
				String[] line = {sn,cqr.getException().getMessage()};
				ret.add(line);
				continue;
			}
			InBlk inblk = cqr.getResult();

			// get the model per the shortName
			DerivativeModelInterface optionsModel = dse.getModel(sn);
			
			// create a map of DioTypes vs values per that type.  This map will be put into the snVsTypeVsValue map for each shortName
			Map<String, String> typeVsValue = new HashMap<String, String>();
			
			// get main DioTypes and their values
			List<DioType<?>> mainTypes = optionsModel.getMainInputTypes();
			for(DioType<?> diot: mainTypes){
				String diotName = diot.name();

				Object o=diot.getMainInputs(inblk);
				// some DioTypes have multiple field outputs
				if(o==null){
					o="Null return from " + diot.name() + ".getMainInputs";
				}
				String[] multiPart = o.toString().split(",");
				if(multiPart.length>1){
					for(int j = 0;j<multiPart.length;j++){
						String typeKey = diotName + "_" + j;
						String value = multiPart[j];
						typeVsValue.put(typeKey,value);
						allPossibleTypes.add(typeKey);
					}
				}else{
					typeVsValue.put(diotName, multiPart[0]);
					allPossibleTypes.add(diotName);
				}

			}
			
			// get underlying DioTypes and their values
			List<DioType<?>> underTypes = optionsModel.getUnderlyingInputTypes();
			for(DioType<?> diot: underTypes){
				// you can have multiple underlying shortNames for each optionable security (e.g spread options)
				List<?> l = diot.getUnderlyingInputs(inblk);
				String diotName = diot.name();
				for(int i = 0;i<l.size();i++){
					String typeKey = diotName + "_" + i;
					Object o = l.get(i);
					if(o==null){
						o="Null return from " + diot.name() + ".getUnderlyingInputs";
					}
					String value = o.toString();
					typeVsValue.put(typeKey,value);
					allPossibleTypes.add(typeKey);
				}
			}
			
			SecDef[] underlyingSecDefs = inblk.getUnderlyingSds();
			for(int i = 0;i < underlyingSecDefs.length; i++){
				String typeKey = "Underlying_" + i;
				String value = underlyingSecDefs[i].getShortName();
				// create columns for each underlying shortName
				typeVsValue.put(typeKey, value);
				allPossibleTypes.add(typeKey);
			}
			
			// save all inputs for this shortName
			snVsTypeVsValue.put(sn,typeVsValue);
		}

		List<String> colNames = new ArrayList<String>();
		colNames.add("shortName");
		for(String diotName : allPossibleTypes ){
			colNames.add(diotName);
		}
		ret.add(colNames.toArray(new String[]{}));
		
		// now add lines for each shortname
		for(String sn : cqrResults.keySet()){
			Map<String,String> valuesForThisShortName = 
					snVsTypeVsValue.get(sn);
			String[] line = new String[allPossibleTypes.size()+1]; // plus 1 for the shortname
			line[0] = sn;
			for(int j = 1;j<colNames.size();j++){
				String colName = colNames.get(j);
				String value = valuesForThisShortName.get(colName);
				// if value is a number, then round it
				if(RegexMethods.isNumber(value)){
					value = new BigDecimal(value,
							new MathContext(precision, RoundingMode.HALF_EVEN)).toString();
				}
				line[j] = value;
			}
			ret.add(line);
		}
		return ret;
	}
}
