package com.billybyte.dse.var;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import Jama.Matrix;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CallPutDiot;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.DivDiot;
import com.billybyte.dse.inputs.diotypes.DteFromSettleDiot;
import com.billybyte.dse.inputs.diotypes.DteSimpleDiot;
import com.billybyte.dse.inputs.diotypes.ImpliedCorr;
import com.billybyte.dse.inputs.diotypes.RateDiot;
import com.billybyte.dse.inputs.diotypes.SettlePriceDiot;
import com.billybyte.dse.inputs.diotypes.StrikeDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.var.stressinputqueries.AtmStressInputQuery;
import com.billybyte.dse.var.stressinputqueries.RepeatInputQuery;
import com.billybyte.dse.var.stressinputqueries.VolStressInputQuery;
import com.billybyte.marketdata.SecDef;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class McVar { 
	private final DioType<BigDecimal> atmDiot = new AtmDiot();
	private final DioType<BigDecimal>  volDiot = new VolDiot();
	private final DioType<BigDecimal>  corrDiot = new CorrDiot();
	private final DioType<BigDecimal>  rateDiot = new RateDiot();
	private final DioType<BigDecimal>  divDiot = new DivDiot();
	private final DioType<BigDecimal>  dteDiot = new DteSimpleDiot();
	private final DioType<Double>  cpDiot = new CallPutDiot();
	private final DioType<BigDecimal>  strkDiot = new StrikeDiot();
	private final DioType<BigDecimal> impliedCorr = new ImpliedCorr();
	private final DioType<BigDecimal> dteFromSettleCorr = new DteFromSettleDiot();
	
	private final Map<DioType<?>,QueryInterface<StressInputQueryBlock,Map<String,double[]>>> userSuppliedMcInputQueryMap
			= new HashMap<DioType<?>, QueryInterface<StressInputQueryBlock,Map<String,double[]>>>();

	// settlediot is ingored
	static private final DioType<SettlementDataInterface> settleDiot = new SettlePriceDiot();
	
	public McVar(){
		
	}
	
	public McVar(
			Map<DioType<?>, QueryInterface<StressInputQueryBlock, Map<String, double[]>>> userSuppliedMcInputQueryMap) {
		super();
		this.userSuppliedMcInputQueryMap.putAll(userSuppliedMcInputQueryMap);
				
	}

	public static class StressInputQueryBlock {
		private final Set<String> derivSns;
		private final int numTrials;
		public StressInputQueryBlock(Set<String> derivSns, int numTrials) {
			super();
			this.derivSns = derivSns;
			this.numTrials = numTrials;
		}
		public Set<String> getDerivSns() {
			return derivSns;
		}
		public int getNumTrials() {
			return numTrials;
		}
		
	}
	
	
	
	public double mcVar(
			Set<String> derivNames, 
			int trials,
			DerivativeSetEngine dse){

		Map<DioType<?>,QueryInterface<StressInputQueryBlock,Map<String,double[]>>> mcInputsQueryMap = 
				createMcInputQueryPerDiotMap(dse);
		Matrix currPriceArrayCopied = getCopiedCurrentValues(derivNames,trials,dse);// this a double[trials][derivNames]
		
		// get a Matrix of trials X shortNames, that holds the values of derivative prices per trial, per shortName
		Matrix resultsPerTrialPerDerivName = getAllTrialValues(derivNames,dse,mcInputsQueryMap,trials);
		// get a Matrix of trials X shortNames that holds the differences of the trial derivative price minus the current 
		//   deriviatve price.
		Matrix derivPriceChangePerTrial = resultsPerTrialPerDerivName.minus(currPriceArrayCopied);
		// now, add the values for each row together, to create a portfolio value per trial
		Matrix ones = new Matrix(derivNames.size(),1,1.0);
		double[] portfolioChangesPerTrial = derivPriceChangePerTrial.times(ones).getColumnPackedCopy();

		// the var is the worst n% of these trial sums
		double var = 
				MathStuff.excelPercentile(portfolioChangesPerTrial, .01);
		
		return var;
	}

	
	public double mcVar(
			Map<String,BigDecimal> position, 
			int trials,
			DerivativeSetEngine dse){

		Set<String> derivNames = new TreeSet<String>(position.keySet());
		Map<DioType<?>,QueryInterface<StressInputQueryBlock,Map<String,double[]>>> mcInputsQueryMap = 
				createMcInputQueryPerDiotMap(dse);
		Matrix currPriceArrayCopied = getCopiedCurrentValues(derivNames,trials,dse);// this a double[trials][derivNames]
		
		// get a Matrix of trials X shortNames, that holds the values of derivative prices per trial, per shortName
		Matrix resultsPerTrialPerDerivName = getAllTrialValues(derivNames,dse,mcInputsQueryMap,trials);
		// get a Matrix of trials X shortNames that holds the differences of the trial derivative price minus the current 
		//   deriviatve price.
		Matrix derivPriceChangePerTrial = resultsPerTrialPerDerivName.minus(currPriceArrayCopied);
		// now, multiply values time position sizes
		double[] posSizes = new double[derivNames.size()];		
		List<String> orderedList = new ArrayList<String>(derivNames);
		double[] notionalValues = getNotionals(orderedList, dse);
		for(int i = 0;i<orderedList.size();i++){
			posSizes[i] = position.get(orderedList.get(i)).doubleValue();
			// need to multiply position sizes by notionals
			posSizes[i] = posSizes[i] * notionalValues[i];
		}
		Matrix posSizeMatrix = new Matrix(posSizes,posSizes.length);
		double[] portfolioChangesPerTrial = derivPriceChangePerTrial.times(posSizeMatrix).getColumnPackedCopy();

		// the var is the worst n% of these trial sums
		double var = 
				MathStuff.excelPercentile(portfolioChangesPerTrial, .01);
		
		return var;
	}
	
	/**
	 * Get list of notional values for each derivative
	 * @param snList
	 * @return
	 */
	private double[] getNotionals(List<String> snList,
			DerivativeSetEngine dse){
		double[] ret = new double[snList.size()];
		QueryInterface<String, SecDef> sdQuery = dse.getSdQuery();
		for(int i = 0;i<snList.size();i++){
			String name = snList.get(i);
			SecDef sd = sdQuery.get(name, 200, TimeUnit.MILLISECONDS);
			if(sd==null){
				throw Utils.IllState(this.getClass(), name+" : no sec def found");
			}
			ret[i] = sd.getMultiplier().doubleValue();
		}
		return ret;
	}
	
	private static List<String> getUnderlyingNames(String derivName,InBlk inblk){
		SecDef[] sds  = inblk.getUnderlyingSds();
		List<String> ret = new ArrayList<String>();
		for(int i = 0;i<sds.length;i++){
			ret.add(sds[i].getShortName());
		}
		return ret;
	}
	
	private Matrix getCopiedCurrentValues(Set<String> derivNames, int trials,
			DerivativeSetEngine dse){
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Map<String,DerivativeReturn[]> currPrices = dse.getSensitivity(new OptPriceDerSen(), derivNames);
		// make an array of these prices
		double[] currPriceArray = new double[currPrices.size()];
		for(int i = 0;i<orderedDerivNames.size();i++){
			String orderedName = orderedDerivNames.get(i);
			DerivativeReturn[] drArray = currPrices.get(orderedName);
			DerivativeReturn dr = drArray[0];
			if(!dr.isValidReturn()){
				throw Utils.IllState( dr.getException());
			}
			currPriceArray[i]= dr.getValue().doubleValue();//currPrices.get(orderedDerivNames.get(i))[0].getValue().doubleValue();
		}
		Matrix ret = // this a double[trials][derivNames]
				MathStuff.generateDuplicateRowMatrix(
						trials, currPriceArray);
		return ret;
	}
	
	/**
	 * This method returns a matrix of derivative prices per each trial,
	 *   per each deriv shortName that you pass it.
	 *   For example, if you have 10 derivative shortNames in your portfolio, 
	 *     and you want to do a monte carlo run with 10,000 trials, then the 
	 *     Matrix that gets returned with be a 10,000 X 10 Matrix
	 *   
	 * @param derivNames
	 * @param dse
	 * @param mcInputsQueryMap
	 * @param trials
	 * @return - Matrix with 
	 * 				1. numRows = trials; 
	 * 				2. numCols = derivNames.size
	 *   The value in each cell of the matrix is an option price (price of underlying if 
	 *   	derivative is not an option), or that trial.
	 */
//	private static Matrix getAllTrialValues(
	private Matrix getAllTrialValues(
			Set<String> derivNames,
			DerivativeSetEngine dse,
			Map<DioType<?>,QueryInterface<StressInputQueryBlock,Map<String,double[]>>> mcInputsQueryMap,
			int trials){
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Map<DioType<?>, Map<String, double[]>> diotToShortNameToTrialValues = 
				getTrialInputs(derivNames,dse,mcInputsQueryMap,trials);
//		
//		// pretend that you don't have underlying names, and recreate  them
		Map<String,ComplexQueryResult<InBlk>> currInblks = dse.getInputs(derivNames);

		// now build maps for main and underlying diots, per deriviative shortName
		double[][] resultsPerTrialPerDerivNameTransposed = new double[derivNames.size()][trials];
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			
			// get main diots trial values
			Map<DioType<?>, double[]> mainTrialsPerDiot = getTrialValuesPerMainDiot(
					derivName,diotToShortNameToTrialValues,dse);
			// get underlying diots per diot, which are 2 dimensional since
			//   diots like AtmDiot can have mutiple underlyings
			Map<DioType<?>, double[][]> underTrialsPerDiot = getTrialValuesPerUnderlyingDiot(
					derivName,currInblks.get(derivName).getResult(),
					dse,
					diotToShortNameToTrialValues,
					trials);
			
			// call batch get price
			double[] resultsPerTrial = null;
			try {
				resultsPerTrial = dse.getModel(derivName).getPriceArray(mainTrialsPerDiot, underTrialsPerDiot);
			} catch (Exception e) {
				Utils.prtObErrMess(this.getClass(), derivName + " : error during getPriceArray");
				resultsPerTrial = dse.getModel(derivName).getPriceArray(mainTrialsPerDiot, underTrialsPerDiot);
				//throw Utils.IllState(e);
			}
			resultsPerTrialPerDerivNameTransposed[i] = resultsPerTrial;
		}
		
		Matrix ret = new Matrix(resultsPerTrialPerDerivNameTransposed).transpose();
		return ret;
	}
	
	private static Map<DioType<?>, double[]> getTrialValuesPerMainDiot(
			String derivName,
			Map<DioType<?>, Map<String, double[]>> diotToShortNameToTrialValues,
			DerivativeSetEngine dse){
		List<DioType<?>> mainDiotsPerDerivSn = dse.getModel(derivName).getMainInputTypes();
		Map<DioType<?>, double[]> ret = new HashMap<DioType<?>, double[]>();
		for(DioType<?> mainDiot:mainDiotsPerDerivSn){
			if(mainDiot.compareTo(settleDiot)==0){
				continue;
			}
			double[] trialValuesPerDiot = diotToShortNameToTrialValues.get(mainDiot).get(derivName);
			ret.put(mainDiot,trialValuesPerDiot);
		}
		return ret;
	}
	
//	private static Map<DioType<?>, double[][]> getTrialValuesPerUnderlyingDiot(
	private Map<DioType<?>, double[][]> getTrialValuesPerUnderlyingDiot(
			String derivName,
			InBlk currInblk,
			DerivativeSetEngine dse,
			Map<DioType<?>, Map<String, double[]>> diotToShortNameToTrialValues,
			int trials){
		

		List<DioType<?>> underDiotsPerDerivSn = dse.getModel(derivName).getUnderlyingInputTypes();
		Map<DioType<?>, double[][]> ret = new HashMap<DioType<?>, double[][]>();
		List<String> underNames = getUnderlyingNames(derivName,currInblk);  
				
		double[][] underTrialsTransposed = new double[underNames.size()][trials];
		for(DioType<?> underDiot:underDiotsPerDerivSn){
			for(int j = 0;j<underNames.size();j++){
				String underName = underNames.get(j);
				underTrialsTransposed[j] = diotToShortNameToTrialValues.get(underDiot).get(underName);
			}
			
			ret.put(underDiot,new Matrix(underTrialsTransposed).transpose().getArray());
		}
		return ret;
	}

	/**
	 * 
	 * @param derivNames - shortNames of derivatives in Portfolio
	 * @param dse 
	 * @param mcInputsQueryMap - map of queries that will generate Matrices of
	 *        inputs for each Diot type that the dse needs to generate an option value.
	 * @return - Map<DioType<?>, Map<String, double[]>> - a 2 level map where :
	 *           1.  the key of the top level is a DioType and the value is another Map
	 *           2.  the key of the inner map is a shortName of either the portfolio
	 *               derivative, or any of the underlyings that are associated with that
	 *               derivative.  The values of the inner map are a list of values for
	 *               that DioType, where the list size = trials; 
	 *               WARNING, the value of trials is passed into the queries that generate
	 *                 the monte carlo inputs per Diotype.  This needs to be changed 
	 *                 so that the number of trials are passed into this routine.
	 *                 
	 */
	private static Map<DioType<?>, Map<String, double[]>> getTrialInputs(
			Set<String> derivNames,
			DerivativeSetEngine dse,
			Map<DioType<?>,QueryInterface<StressInputQueryBlock,Map<String,double[]>>> mcInputsQueryMap,
			int numTrials){
		// create diot map for the each model/security

		// get all inputs
		Set<String> mainDiotNames = new HashSet<String>();
		Set<String> underDiotNames = new HashSet<String>();
		Set<DioType<?>> mainDiots = new HashSet<DioType<?>>();
		Set<DioType<?>> underDiots = new HashSet<DioType<?>>();
		Map<String,List<String>> underNamesPerDerivName = 
				new HashMap<String, List<String>>();
		
		
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		// populate the following lists and maps:
		// 1. mainDiotNames - a list of every derivative shortName that will be used for this monte carlo run
		// 2. mainDiots - a list of every main Diot that will be used for this monte carlo run
		// 3. underDiots - a list of every underlying Diot that will be used for this monte carlo run
		// 4. underNamesPerDerivName - a map where the key is derivative shortName, and the value is a list of underlying
		//                     names per that derivative shortName
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			List<String> underList = new ArrayList<String>();
			mainDiotNames.add(derivName);
			mainDiots.addAll(dse.getModel(derivName).getMainInputTypes());
			underDiots.addAll(dse.getModel(derivName).getUnderlyingInputTypes());
			// populate the map underNamesPerDerivName 
			for(SecDef sd:dse.getQueryManager().getUnderlyingSecDefs(derivName, 1, TimeUnit.SECONDS)){
				String underName = sd.getShortName();
				underList.add(underName);
//				underDiotNames.add(sd.getShortName());
				underDiotNames.add(underName);
			}
			underNamesPerDerivName.put(derivName,underList);
		}
		
		// Populate a 2 level map called diotToShortNameToTrialValues.  This is the map that 
		//   you will return.
		//  You will be retrieving values from the map mcInputsQueryMap, which was passed to
		//    this method by the caller.
		//  The key of the top level is a DioType.  The value is a sub-map.
		//  The key of the sub-map is a shortName, and the value is an array of doubles.
		//   Some of the Diot's have multiple underlying shortName inputs, like the VolDiot for 
		//     a Kirk spread model.
		Map<DioType<?>, Map<String, double[]>> diotToShortNameToTrialValues = 
				new HashMap<DioType<?>, Map<String,double[]>>();
		StressInputQueryBlock mainInpBlk = new StressInputQueryBlock(mainDiotNames, numTrials);
		for(DioType<?> mainDiot:mainDiots){
			// ignore settlement diot
			if(mainDiot.compareTo(settleDiot)==0){
				continue;
			}
			Map<String, double[]> trialValues = 
					mcInputsQueryMap.get(mainDiot).get(mainInpBlk, 1, TimeUnit.SECONDS);	
			diotToShortNameToTrialValues.put(mainDiot, trialValues);
		}
		StressInputQueryBlock underInpBlk = new StressInputQueryBlock(underDiotNames, numTrials);
		for(DioType<?> underDiot:underDiots){
			Map<String, double[]> trialValues = 
					mcInputsQueryMap.get(underDiot).get(underInpBlk, 1, TimeUnit.SECONDS);
			Map<String, double[]> innerMap = diotToShortNameToTrialValues.get(underDiot);
			if(innerMap==null){
				diotToShortNameToTrialValues.put(underDiot, trialValues);	
			}else{
				innerMap.putAll(trialValues);
			}
			
		}
		
		return diotToShortNameToTrialValues;
	}
	
	/**
	 * create a map of queries per DioType that fetch maps of correlated random data or
	 *   repeated data, that will be used by models
	 * @param dse - DerivativeSetEngine
	 * @return
	 */
	private Map<DioType<?>,
				QueryInterface<
					StressInputQueryBlock,Map<String,double[]>>> 
							createMcInputQueryPerDiotMap(
									DerivativeSetEngine dse){
		Map<DioType<?>,QueryInterface<StressInputQueryBlock,Map<String,double[]>>> ret = 
				new HashMap<DioType<?>, QueryInterface<StressInputQueryBlock,Map<String,double[]>>>();
		ret.put(atmDiot, new AtmStressInputQuery(dse));
		ret.put(volDiot, new VolStressInputQuery(dse));
		ret.put(corrDiot,new RepeatInputQuery<BigDecimal>(corrDiot,dse));
		ret.put(rateDiot,new RepeatInputQuery<BigDecimal>(rateDiot,dse));
		ret.put(divDiot,new RepeatInputQuery<BigDecimal>(divDiot,dse));
		ret.put(strkDiot,new RepeatInputQuery<BigDecimal>(strkDiot,dse));
		ret.put(dteDiot,new RepeatInputQuery<BigDecimal>(dteDiot,dse));
		ret.put(cpDiot,new RepeatInputQuery<Double>(cpDiot,dse));
		ret.put(impliedCorr,new RepeatInputQuery<BigDecimal>(impliedCorr,dse));
		ret.put(dteFromSettleCorr,new RepeatInputQuery<BigDecimal>(dteFromSettleCorr,dse));
		// overwrite any of the above with user supplied queries
		ret.putAll(this.userSuppliedMcInputQueryMap);
		return ret;
	}



}
