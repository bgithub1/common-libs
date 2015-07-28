package com.billybyte.dse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;


import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.inputs.UnderlyingInfo;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.models.vanilla.VanOptAbst;
import com.billybyte.dse.models.vanilla.VanOptBlackEuropean;
import com.billybyte.dse.models.vanilla.VanOptUnderlying;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;


//import com.billybyte.derivativesetengine.inputs.InBlk;
//import com.billybyte.derivativesetengine.inputs.PrimitiveMap;
//import com.billybyte.derivativesetengine.inputs.QueryManager;
//import com.billybyte.derivativesetengine.inputs.UnderlyingInfo;
//import com.billybyte.derivativesetengine.inputs.diotypes.AtmDiot;
//import com.billybyte.derivativesetengine.inputs.diotypes.DioType;
//import com.billybyte.derivativesetengine.models.vanilla.BasicBlackEuropeanVan;
//import com.billybyte.derivativesetengine.models.vanilla.BasicBlackFuturesVan;
//import com.billybyte.derivativesetengine.models.vanilla.UnderLyingModel;
//import com.billybyte.derivativesetengine.models.vanilla.VanOptAbst;
//import com.billybyte.derivativesetengine.outputs.DerivativeReturn;
//import com.billybyte.derivativesetengine.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

import com.billybyte.queries.QueryFromRegexPattern;


public class DerivativeSetEngine {
	// default values
	public static final double DEFAULT_CONFIDENCE_FOR_VAR = 0.99;
	public static final double DEFAULT_VOL_STRESS_FOR_VAR = 0.2;

	// err messages
	private final static String NOSD = "Can't find SecDef";
	private final static String NOUNDERSD = "Can't find Underlying SecDef";
	private final static String NOMODELFOUND = "Can't find Model";
	private final static String NOTYPESTOPROCESS = "Model is not returning any DerivativeInputTypes to process";
	// end errr mess
	
	// timer values for Queries
	private transient final int tovSdGet = 200;
	private transient final int tovUnderSdGet = 2000;
	private transient  final int tovDataGet = 2000;
	private transient final TimeUnit tut = TimeUnit.MILLISECONDS;
	// END timer values for Queries
	
	
	private final QueryManager queryManager;
	private final QueryInterface<String, SecDef> sdQuery;
	private final Calendar evaluationDate;
	
	// models
	private final QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQuery;

	
	
	public DerivativeSetEngine(
			QueryManager queryManager,
			QueryInterface<String, SecDef> sdQuery,
			Calendar evaluationDate,
			QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQuery) {
		super();
		this.queryManager = queryManager;
		this.sdQuery = sdQuery;
		this.evaluationDate = evaluationDate;
		this.regexModelQuery = regexModelQuery;	
	}

	public DerivativeSetEngine(
			QueryManager queryManager,
			QueryInterface<String, SecDef> sdQuery,
			Calendar evaluationDate) {
		this(queryManager,sdQuery,evaluationDate,defaultQueryFromRegexPattern(sdQuery));
		
	}

	
	
	/**
	 * Returns a map of all the DioTypes associated with each derivative short name
	 * @param shortNameSet
	 * @return
	 */
	public Map<String,Set<DioType<?>>> getDiotsByShortName(Set<String> shortNameSet){
		Map<String,Set<DioType<?>>> ret = new HashMap<String,Set<DioType<?>>>();
		for(String shortName:shortNameSet){
			DerivativeModelInterface model = getModel(shortName);
			List<DioType<?>> mainDiotList = model.getMainInputTypes();
			List<DioType<?>> undDiotList = model.getUnderlyingInputTypes();
			Set<DioType<?>> diotSet = new HashSet<DioType<?>>();
			diotSet.addAll(mainDiotList);
			diotSet.addAll(undDiotList);
			ret.put(shortName, diotSet);
		}
		return ret;
	}
	
	/**
	 * Get Inputs for models of DerivativeSetEngine
	 * @param derivativeShortNameSet
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Map<String,ComplexQueryResult<InBlk>> getInputs(Set<String> derivativeShortNameSet){
		Map<String,ComplexQueryResult<InBlk>> ret = 
				new HashMap<String, ComplexQueryResult<InBlk>>();
		
		Map<String,SecDef> mainSdMap = new HashMap<String, SecDef>();
		Map<String, List<SecDef>> underSdMap = new HashMap<String, List<SecDef>>();
		Map<String,DerivativeModelInterface> modelMap = new HashMap<String, DerivativeModelInterface>();
		Map<String,Tuple<DioType<?>,Set<String>>> underlyingsPerTypeMap = 
				new HashMap<String, Tuple<DioType<?>,Set<String>>>();
		Map<String,Tuple<DioType<?>,Set<String>>> derivSnsPerTypeMap = 
				new HashMap<String, Tuple<DioType<?>,Set<String>>>();
		
		
		for(String derivativeShortName:derivativeShortNameSet){
			// *********** populate mainSd map ***************
			SecDef mainSd = getSdQuery().get(derivativeShortName, tovSdGet, tut);
			if(mainSd==null){
				ret.put(derivativeShortName, errCqr(
						InBlk.class,derivativeShortName,NOSD));
				continue;
			}
			mainSdMap.put(derivativeShortName,mainSd);
			
			// *********** populate Map of List<Secdef> per deriv shortName\ ***************	
			List<SecDef> underSds = 
					getQueryManager().getUnderlyingSecDefs(
							derivativeShortName, tovUnderSdGet, tut);
			if(underSds==null || underSds.size()<1){
				ret.put(derivativeShortName, errCqr(
						InBlk.class,derivativeShortName,NOUNDERSD));
				continue;
			}
			boolean allSdsGood = true;
			for(SecDef sd : underSds){
				if(sd == null){
					allSdsGood = false;
				}
			}
			if(!allSdsGood){
				ret.put(derivativeShortName, errCqr(
						InBlk.class,derivativeShortName,NOUNDERSD));
				continue;
			}
			underSdMap.put(derivativeShortName,underSds);
			
			//  ************* now get all types to process ***************
			DerivativeModelInterface model  = getModel(derivativeShortName);
			if(model==null){
				ret.put(derivativeShortName, errCqr(
						InBlk.class,derivativeShortName,NOMODELFOUND));
				continue;
			}
			modelMap.put(derivativeShortName,model);
			
			List<DioType<?>> underlyingTypesToProcess = model.getUnderlyingInputTypes();
			if(underlyingTypesToProcess==null || underlyingTypesToProcess.size()<1){
				ret.put(derivativeShortName, errCqr(
						InBlk.class,derivativeShortName,NOTYPESTOPROCESS));
				continue;
			}
			
			//  add the underlying level inputs, like callput and strike, and, sometimes vol
			// the types are good, process them
			for(DioType<?> diot:underlyingTypesToProcess){
				if(!underlyingsPerTypeMap.containsKey(diot.name())){
					Set<String> set = new HashSet<String>();
					Tuple<DioType<?>, Set<String>> tup = 
							new Tuple<DioType<?>, Set<String>>(diot, set);
					// the set of underlying shortNames has NOT been populated YET.
					underlyingsPerTypeMap.put(diot.name(), tup);
				}
				
				Tuple<DioType<?>, Set<String>> tup  = underlyingsPerTypeMap.get(diot.name());
				Set<String> set = tup.getT2_instance();
				for(SecDef sd:underSds){
					set.add(sd.getShortName());
				}
			}

			// add the main level inputs, like implied correlation, etc
			//  by building the derivSnsPerTypeMap.  This map holds derivativeShortName per
			//     DioType
			List<DioType<?>> mainTypesToProcess = model.getMainInputTypes();
			if(mainTypesToProcess!=null && mainTypesToProcess.size()>0){
				for(DioType<?> diot:mainTypesToProcess){
					if(!derivSnsPerTypeMap.containsKey(diot.name())){
						Set<String> set = new HashSet<String>();
						Tuple<DioType<?>, Set<String>> tup = 
								new Tuple<DioType<?>, Set<String>>(diot, set);
						derivSnsPerTypeMap.put(diot.name(), tup);
					}
					
					Tuple<DioType<?>, Set<String>> tup  = derivSnsPerTypeMap.get(diot.name());
					Set<String> set = tup.getT2_instance();
					set.add(mainSd.getShortName());
				}

			}

		}
		
		//  ******** !!!!!!!!!!! HERE'S WHERE ALL OF THE GETS OCCURED !!!!!!!! **********
		
		//  ******** !!!!!!!!!!! HERE'S WHERE ALL OF THE GETS OCCURED !!!!!!!! **********
		// ************ now create the big UnderlyingInfo map, which holds ***********
		// ************   all underlying inputs by DioType ***********
		UnderlyingInfo uim = new UnderlyingInfo();
		for(Entry<String,Tuple<DioType<?>, Set<String>>> entry: underlyingsPerTypeMap.entrySet()){
			// type safety is protected here because the DioType object controls the
			//   the class type of the queries that are loaded into QueryManager
			//  See QueryManager for more info
			Tuple<DioType<?>, Set<String>> tup = entry.getValue();
			DioType<?> diot = tup.getT1_instance();
			Set<String> set = tup.getT2_instance();
			try {
				@SuppressWarnings("rawtypes")
				Map  diotMap = 
						(Map)getQueryManager().getDioInputs(diot, set, tovDataGet,tut);
				uim.putDiotMap(diot, diotMap);
			} catch (Exception e) {
				e.printStackTrace();
				throw Utils.IllState(this.getClass(),"getInputs: can't process Diot=" + 
						diot.toString() + " exception: " + Utils.stackTraceAsString(e));
			}
		}
		
		//**************** get all main level inputs by DioType **************
		for(Entry<String,Tuple<DioType<?>, Set<String>>> entry: derivSnsPerTypeMap.entrySet()){
			// type safety is protected here because the DioType object controls the
			//   the class type of the queries that are loaded into QueryManager
			//  See QueryManager for more info
			Tuple<DioType<?>, Set<String>> tup = entry.getValue();
			DioType<?> diot = tup.getT1_instance();
			Set<String> set = tup.getT2_instance();
			@SuppressWarnings("rawtypes")
			Map  diotMap = 
					(Map)getQueryManager().getDioInputs(diot, set, tovDataGet,tut);
			if(diotMap!=null){
				Map diotMapFromUnderlyings = uim.getMap(diot);
				if(diotMapFromUnderlyings!=null){
					diotMap.putAll(diotMapFromUnderlyings);
				}
				uim.putDiotMap(diot, diotMap);
			}
		}
		
		
		Map<String,ComplexQueryResult<Map<String, Object>>> dioInMapsToDerivShortNameMap = 
				new HashMap<String, ComplexQueryResult<Map<String, Object>>>();
		// ***************** now create inBlks ****************
		for(String derivativeShortName:derivativeShortNameSet){
			if(!mainSdMap.containsKey(derivativeShortName))continue;
			if(!underSdMap.containsKey(derivativeShortName))continue;
			if(!modelMap.containsKey(derivativeShortName))continue;
			SecDef mainSd = mainSdMap.get(derivativeShortName);
			List<SecDef> sds = underSdMap.get(derivativeShortName);
			SecDef[] underlyingSds = sds.toArray(new SecDef[]{});
			DerivativeModelInterface model = modelMap.get(derivativeShortName);
			List<DioType<?>> dioTypeList = model.getUnderlyingInputTypes();
			// ---------- create the InBlk here, and put it to the ret map of InBlks -------
			InBlk ib = new InBlk(mainSd, underlyingSds, uim,dioTypeList,getEvaluationDate());
			ret.put(derivativeShortName, goodCqr(InBlk.class, derivativeShortName, ib));

//			// ALL OF THE CODE BELOW IS EXPERIMENTAL
//			// ---------- create the diotToInputMap here EXPERIMENTAL FOR LATER USE
//			Map<String,Object> diotToInputMap = new HashMap<String, Object>();  
//			for(DioType<?> diot:dioTypeList){
//				diotToInputMap.put(diot.name(),diot.getFromInBlk(ib));
//			}
//			// populate the map that is NOT used yet, but might be in the future
//			dioInMapsToDerivShortNameMap.put(derivativeShortName, 
//					new ComplexQueryResult<Map<String,Object>>(null,diotToInputMap));
		}
		
		return ret;
		
	}

	/**
	 * Get sensitivity specified with the supplied map of adjusted input blocks
	 * @param adjInBlkMap
	 * @return
	 */
	public Map<String,List<DerivativeReturn>> getSensitivityFromCustomInBlk(
			DerivativeSensitivityTypeInterface sense, 
			Map<String,ComplexQueryResult<InBlk>> adjInBlkMap){
		Map<String,List<DerivativeReturn>> ret = new HashMap<String,List<DerivativeReturn>>();
		Set<String> shortNameSet = adjInBlkMap.keySet();
		for(String derivativeShortName:shortNameSet){			
			ret.put(derivativeShortName, new ArrayList<DerivativeReturn>());
			DerivativeModelInterface model = getModel(derivativeShortName);
			ComplexQueryResult<InBlk> cqr = adjInBlkMap.get(derivativeShortName);
			if(!cqr.isValidResult()){
				String message = cqr.getException().getMessage();
				ret.get(derivativeShortName).addAll(Arrays.asList(errRet(sense, derivativeShortName, message)));
			}else{
				InBlk blk = cqr.getResult();
				DerivativeReturn[] drArr = model.getSensitivity(sense, derivativeShortName, blk);
				ret.get(derivativeShortName).addAll(Arrays.asList(drArr));						
			}
			
		}		
		return ret;
	}
	

	
	List<Map<String,DerivativeReturn[]>> getSensitivityLoop(DerivativeSensitivityTypeInterface sense,
			Set<String> derivativeShortNameSet,
			int numLoops, BigDecimal atmChgPerc){
		AtmDiot atm = new AtmDiot();
		List<Map<String,DerivativeReturn[]>> ret = new ArrayList<Map<String,DerivativeReturn[]>>();
		// get inputs for all derivative shortNames
		TreeMap<String,ComplexQueryResult<InBlk>> inputs = new TreeMap<String, ComplexQueryResult<InBlk>>( getInputs(derivativeShortNameSet));
		UnderlyingInfo ui = inputs.get(inputs.firstKey()).getResult().getUnderInputsMap();
		Map<String,ComplexQueryResult<BigDecimal>> atmIn =  ui.getMap(atm);
		Map<String,ComplexQueryResult<BigDecimal>> atmInNew = new HashMap<String, ComplexQueryResult<BigDecimal>>();
		
		for(int i = 0;i<numLoops;i++){
			for(String derivSn:derivativeShortNameSet){
				for(Entry<String,ComplexQueryResult<BigDecimal>>entry:atmIn.entrySet()){
					BigDecimal netAtmVal = entry.getValue().getResult().multiply(BigDecimal.ONE.add(atmChgPerc));
					atmInNew.put(derivSn, new ComplexQueryResult<BigDecimal>(null, netAtmVal));
				}
			}
			Map<String,ComplexQueryResult<BigDecimal>> m = atmInNew;
			ui.putDiotMap(atm, m);
			
			Map<String,DerivativeReturn[]> thisRet = getSensitivity(sense, derivativeShortNameSet, inputs);
			ret.add(thisRet);
		}
		return ret;
	}
	
	/**
	 * Get all sensitivities associated with the supplied map of adjusted input blocks
	 * @param adjInBlkMap
	 * @return
	 */
	public Map<String,List<DerivativeReturn>> getAllSensitivitiesFromCustomInBlk(Map<String,ComplexQueryResult<InBlk>> adjInBlkMap){
		Map<String,List<DerivativeReturn>> ret = new HashMap<String,List<DerivativeReturn>>();
		Set<String> shortNameSet = adjInBlkMap.keySet();
		for(String derivativeShortName:shortNameSet){			
			ret.put(derivativeShortName, new ArrayList<DerivativeReturn>());
			DerivativeModelInterface model = getModel(derivativeShortName);
			ComplexQueryResult<InBlk> cqr = adjInBlkMap.get(derivativeShortName);
			if(model.getSensitivitesSupported() == null){
				Utils.prtObErrMess(this.getClass(), "Null supported sensitivities set for short name "+derivativeShortName);
				continue;
			}
			Set<DerivativeSensitivityTypeInterface> senseSet = model.getSensitivitesSupported();
			if(!cqr.isValidResult()){
				for(DerivativeSensitivityTypeInterface sense:senseSet){
					String message = cqr.getException().getMessage();
					ret.get(derivativeShortName).addAll(Arrays.asList(errRet(sense, derivativeShortName, message)));
				}
			}else{
				for(DerivativeSensitivityTypeInterface sense:senseSet){
					InBlk blk = cqr.getResult();
					// TODO is getAllSensitivitesSupported()???
					DerivativeReturn[] drArr = model.getSensitivity(sense, derivativeShortName, blk);
					ret.get(derivativeShortName).addAll(Arrays.asList(drArr));						
				}
			}
			
		}		
		return ret;
	}
	
	public Map<String,DerivativeReturn[]> getSensitivity(DerivativeSensitivityTypeInterface sense,
			Set<String> derivativeShortNameSet,
			Map<String,ComplexQueryResult<InBlk>> inputs){
		// create a return map
		Map<String,DerivativeReturn[]> ret = new HashMap<String, DerivativeReturn[]>();
		
		// per shortname sense loop
		for(String derivativeShortName:derivativeShortNameSet){
			DerivativeModelInterface model = getModel(derivativeShortName);
			ComplexQueryResult<InBlk> cqr = inputs.get(derivativeShortName);
			if(!cqr.isValidResult()){
				ret.put(derivativeShortName, errRet(
						sense,derivativeShortName,cqr.getException().getMessage()));
			}else{
				DerivativeReturn[] dr=null;
				try {
					dr = model.getSensitivity(
							sense, derivativeShortName, cqr.getResult());
				} catch (Exception e) {
					dr = new DerivativeReturn[]{new DerivativeReturn(sense, derivativeShortName, null,e)};
				}
				ret.put(derivativeShortName, dr);
			}
		}
		return ret;
	}

	
	/**
	 * Get all sensitivities associated with the supplied set of short names
	 * @param derivativeShortNameSet
	 * @return
	 */
	public Map<String,List<DerivativeReturn>> getAllSensitivities(Set<String> derivativeShortNameSet){
		Map<String,List<DerivativeReturn>> ret = new HashMap<String,List<DerivativeReturn>>();
		Map<String,ComplexQueryResult<InBlk>> inputs = getInputs(derivativeShortNameSet);
		for(String derivativeShortName:derivativeShortNameSet){			
			ret.put(derivativeShortName, new ArrayList<DerivativeReturn>());
			DerivativeModelInterface model = getModel(derivativeShortName);
			ComplexQueryResult<InBlk> cqr = inputs.get(derivativeShortName);
			if(model.getSensitivitesSupported() == null){
				Utils.prtObErrMess(this.getClass(), "Null supported sensitivities set for short name "+derivativeShortName);
				continue;
			}
			Set<DerivativeSensitivityTypeInterface> senseSet = model.getSensitivitesSupported();
			if(!cqr.isValidResult()){
				for(DerivativeSensitivityTypeInterface sense:senseSet){
					String message = cqr.getException().getMessage();
					ret.get(derivativeShortName).addAll(Arrays.asList(errRet(sense, derivativeShortName, message)));
				}
			}else{
				for(DerivativeSensitivityTypeInterface sense:senseSet){
					InBlk blk = cqr.getResult();
					// TODO is getAllSensitivitesSupported()???
					DerivativeReturn[] drArr = model.getSensitivity(sense, derivativeShortName, blk);
					ret.get(derivativeShortName).addAll(Arrays.asList(drArr));						
				}
			}
			
		}		
		return ret;
	}
	
	public Map<String,DerivativeReturn[]> getSensitivity(DerivativeSensitivityTypeInterface sense,
			Set<String> derivativeShortNameSet){
		// create a return map
//		Map<String,DerivativeReturn[]> ret = new HashMap<String, DerivativeReturn[]>();
		// get inputs for all derivative shortNames
		Map<String,ComplexQueryResult<InBlk>> inputs =getInputs(derivativeShortNameSet);
		return getSensitivity(sense, derivativeShortNameSet, inputs);
	}
	
	public DerivativeModelInterface getModel(String derivativeShortName){
		return this.regexModelQuery.get(derivativeShortName, tovDataGet, tut);
	}
	
	public QueryManager getQueryManager(){
		return this.queryManager;
	}
	
	public QueryInterface<String, SecDef> getSdQuery(){
		return sdQuery;
	}
	protected DerivativeReturn[]  errRet(
			DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName, String message){
		Exception e = Utils.IllState(this.getClass(),message);
		return new DerivativeReturn[]{new DerivativeReturn(sensitivityId, withRespectToShortName, null, e)};
	}
	
	protected <E> ComplexQueryResult<E> errCqr(Class<E> cls,
			String shortName,String cause){
		Exception e = Utils.IllState(this.getClass(),shortName+" : "+cause);
		return new ComplexQueryResult<E>(e, null);
	}

	protected <E> ComplexQueryResult<E> goodCqr(Class<E> cls,
			String shortName,E value){
		return new ComplexQueryResult<E>(null, value);
	}
	

	
	public void addModel(boolean isExclusive,String regexString, DerivativeModelInterface model){
		if(isExclusive){
			this.regexModelQuery.addPvPairExclusive(regexString, model);
		}else{
			this.regexModelQuery.addPvPairInclusive(regexString, model);
		}
	}
	
	public void replaceAllModels(Map<String, DerivativeModelInterface> modelMap){
		this.regexModelQuery.replacePatterns(modelMap);
	}

	public Calendar getEvaluationDate() {
		return evaluationDate;
	}
	
	private static QueryFromRegexPattern<String, DerivativeModelInterface> defaultQueryFromRegexPattern(QueryInterface<String, SecDef> sdQuery){
		Calendar c = Calendar.getInstance();
		VanOptAbst underModel  = new VanOptUnderlying(c,new VolDiot());
		VanOptAbst vanModel  = new VanOptBlackEuropean(c,new VolDiot());
		VanOptAbst stockModel  = vanModel;
		DerivativeModelInterface[] modelArray = 
				new DerivativeModelInterface[]{underModel,vanModel,stockModel};
		QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQuery = 
				new QueryFromRegexPattern<String, DerivativeModelInterface>(
						new String[]{"\\.((FUT)|(STK))\\.","\\.FOP\\.","\\.OPT\\."},
						modelArray);
		return regexModelQuery;
	}
}
