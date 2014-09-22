package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.map.MultiKeyMap;

import junit.framework.TestCase;

import Jama.Matrix;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.QueryManager;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CallPutDiot;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.DivDiot;
import com.billybyte.dse.inputs.diotypes.DteSimpleDiot;
import com.billybyte.dse.inputs.diotypes.RateDiot;
import com.billybyte.dse.inputs.diotypes.StrikeDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.models.vanilla.NormalDistribution;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecRight;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class TestCorrelatedRandoms extends TestCase{

	private final DioType<BigDecimal> atmDiot = new AtmDiot();
	private final DioType<BigDecimal>  volDiot = new VolDiot();
	private final DioType<BigDecimal>  corrDiot = new CorrDiot();
	private final DioType<BigDecimal>  rateDiot = new RateDiot();
	private final DioType<BigDecimal>  divDiot = new DivDiot();
	private final DioType<BigDecimal>  dteDiot = new DteSimpleDiot();
	private final DioType<Double>  cpDiot = new CallPutDiot();
	private final DioType<BigDecimal>  strkDiot = new StrikeDiot();

	
	private Set<String> underlyingNames;
	private DseInputQuery<BigDecimal> corrQuery;
	private DseInputQuery<BigDecimal> atmQuery;
	private DseInputQuery<BigDecimal> volQuery;
	
	Map<String, ComplexQueryResult<BigDecimal>> corrMap;
	Map<String, ComplexQueryResult<BigDecimal>> atmMap;
	Map<String,ComplexQueryResult<BigDecimal>> volMap;
	private DerivativeSetEngine dse;
	private int trials = 10000;
	private int days = 1;
	private Map<String, DerivativeModelInterface> modelMap;
	private Map<DioType<?>,QueryInterface<Set<String>,Map<String,double[]>>> mcInputsQueryMap;
	
	@Override
	protected void setUp() throws Exception {
		if(this.dse!=null)return;
		Utils.prtObMess(this.getClass(), "testDseSpringLoad");

		Class<?> classNameOfClassInPkgOfBeanXmlAsResource = this.getClass();
		String beanXmlspath = "beans_TestDseBasic.xml";
		Map<String,Object> objects = 
				Utils.springGetAllBeans(beanXmlspath, classNameOfClassInPkgOfBeanXmlAsResource);
		assertNotNull(objects);
		assertTrue(objects.size()>0);
		this.dse = (DerivativeSetEngine)objects.get("dse");
		this.atmQuery = dse.getQueryManager().getQuery(atmDiot);
		this.volQuery = dse.getQueryManager().getQuery(volDiot);
		this.corrQuery = dse.getQueryManager().getQuery(corrDiot);
		this.underlyingNames = TestCholesky.getTestUnderlyings();
		this.modelMap = new HashMap<String, DerivativeModelInterface>();
		for(String un:underlyingNames){
			modelMap.put(un, dse.getModel(un));
		}
		
		this.corrMap = 
				corrQuery.get(underlyingNames, 20, TimeUnit.SECONDS);
		String mess = MarketDataComLib.CqrInvalidResultListString(corrMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);
	
		this.atmMap = 
				atmQuery.get(underlyingNames, 30, TimeUnit.SECONDS);
		mess = MarketDataComLib.CqrInvalidResultListString(atmMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);
		this.volMap = 
				volQuery.get(underlyingNames, 30, TimeUnit.SECONDS);
		mess = MarketDataComLib.CqrInvalidResultListString(volMap);
		if(mess!=null)
			throw Utils.IllState(MarketDataComLib.class, mess);

		this.mcInputsQueryMap = createMcInputQueryPerDiotMap();
		super.setUp();
	}
	
	/**
	 * create a map of queries per DioType that fetch maps of correlated random data or
	 *   repeated data, that will be used by models
	 *   
	 * @return
	 */
	private Map<DioType<?>,QueryInterface<Set<String>,Map<String,double[]>>> createMcInputQueryPerDiotMap(){
		Map<DioType<?>,QueryInterface<Set<String>,Map<String,double[]>>> ret = 
				new HashMap<DioType<?>, QueryInterface<Set<String>,Map<String,double[]>>>();
		ret.put(atmDiot, new AtmLocalInputQuery(corrQuery, atmQuery, volQuery));
		ret.put(volDiot, new VolLocalInputQuery(corrQuery,volQuery,dse));
		ret.put(corrDiot,new RepeatInputQuery<BigDecimal>(corrDiot));
		ret.put(rateDiot,new RepeatInputQuery<BigDecimal>(rateDiot));
		ret.put(divDiot,new RepeatInputQuery<BigDecimal>(divDiot));
		ret.put(strkDiot,new RepeatInputQuery<BigDecimal>(strkDiot));
		ret.put(dteDiot,new RepeatInputQuery<BigDecimal>(dteDiot));
		ret.put(cpDiot,new RepeatInputQuery<Double>(cpDiot));
		return ret;
	}


	@Override
	protected void tearDown() throws Exception {
		this.dse=null;
		this.atmQuery=null;
		this.volQuery = null;
		this.corrQuery = null;
		this.underlyingNames = null;
		this.corrMap =null;
		this.atmMap = null;
		this.volMap = null;
		super.tearDown();
	}



	public void testGetCorrelatedPrices(){
		
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(underlyingNames, corrMap));
		Matrix atmVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, atmMap),underlyingNames.size());
		
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, volMap),underlyingNames.size());
		volVector = volVector.times(Math.sqrt(days/252.0));
		
		Matrix correlatedNewPricesPerTrial = MathStuff.getCorrelatedValues(trials,volVector,atmVector,corrMatrix);	
		
		assertEquals(trials,correlatedNewPricesPerTrial.getRowDimension());
		assertEquals(underlyingNames.size(),correlatedNewPricesPerTrial.getColumnDimension());
	}
	
	public void testMonteCarloVsAnalytic(){
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(underlyingNames, corrMap));
		Matrix atmVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, atmMap),underlyingNames.size());
		
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, volMap),underlyingNames.size());
		volVector = volVector.times(Math.sqrt(days/252.0));
		Matrix atmTimesVol = volVector.arrayTimes(atmVector);
		Matrix corrRandoms = MathStuff.generateCorrelatedRandoms(trials, null, corrMatrix.getArray());
		double[] portfolioChangesPerTrial = corrRandoms.times(atmTimesVol).getColumnPackedCopy();
		double var = 
				MathStuff.excelPercentile(portfolioChangesPerTrial, .01);
		
		
		double var2 = getAnalyticVar();
		
		Utils.prt("testMonteCarloVsAnalytic monte carlo var: " + var + "  analytic var: "+var2);
		double diff = Math.abs(var+var2);
		assertTrue(diff<var2/10.0);
	}
	
	public void testMcVsAnalytic2(){
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(underlyingNames, corrMap));
		Matrix atmVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, atmMap),underlyingNames.size());
		
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, volMap),underlyingNames.size());
		volVector = volVector.times(Math.sqrt(days/252.0));

		Matrix corrPriceChgs =MathStuff.getCorrelatedValueChanges(trials, volVector, atmVector, corrMatrix);
		Matrix ones = new Matrix(underlyingNames.size(),1,1.0);
		double[] portfolioChangesPerTrial = corrPriceChgs.times(ones).getColumnPackedCopy();

		double var = 
				MathStuff.excelPercentile(portfolioChangesPerTrial, .01);
		
		
		double var2 = getAnalyticVar();
		
		Utils.prt("testMcVsAnalytic2 monte carlo var: " + var + "  analytic var: "+var2);
		double diff = Math.abs(var+var2);
		assertTrue(diff<var2/10.0);
	}
	
	
	public void testMcVsAnalyticWithModels(){
		
//		// create diot map for the each model/security
//		// for this test, pretend the derivative names are the same as the underlying names
		Set<String> derivNames = new HashSet<String>(underlyingNames);
		Matrix currPriceArrayCopied = getCopiedCurrentValues(derivNames,trials);// this a double[trials][derivNames]
		

		Matrix resultsPerTrialPerDerivName = getAllTrialValues(derivNames,dse,mcInputsQueryMap,trials);
		Matrix derivPriceChangePerTrial = resultsPerTrialPerDerivName.minus(currPriceArrayCopied);
		
		Matrix ones = new Matrix(underlyingNames.size(),1,1.0);
		double[] portfolioChangesPerTrial = derivPriceChangePerTrial.times(ones).getColumnPackedCopy();

		double var = 
				MathStuff.excelPercentile(portfolioChangesPerTrial, .01);
		
		double var2 = getAnalyticVar();
		
		Utils.prt("testMcVsAnalyticWithModels monte carlo var: " + var + "  analytic var: "+var2);
		double diff = Math.abs(var+var2);
		assertTrue(diff<var2/10.0);
		
		
	}
	
	/**
	 * This is the main test for monte carlo.
	 * The process of executing a monte carlo run is comprised of several stages:
	 * 1.  Create a dse;
	 * 2.  Get all derivative shortNames;
	 * 3.  Get a Matrix of all initial prices for each security in the portfolio by calling
	 *     getCopiedCurrentValues.  This method calls the dse, and then reorders the price 
	 *     outputs into a Matrix, where the rows are ordered by derivative shortName;
	 * 4.  
	 */
	public void testMcVsAnalyticWithModelsWithOptions(){
		// replace vol query
		VolQueryForTest volQuery = 
				(VolQueryForTest)dse.getQueryManager().getQuery(volDiot);
		VolQueryForOptionsTest newVolQuery = 
				new VolQueryForOptionsTest(volQuery.getTestDataMap());
		dse.getQueryManager().registerDioType(volDiot, newVolQuery);
		
//		// create diot map for the each model/security
//		// for this test, pretend the derivative names are the same as the underlying names
		Set<String> derivNames = generateDerivNamesFromUnderlyingNames(underlyingNames);
		Matrix currPriceArrayCopied = getCopiedCurrentValues(derivNames,trials);// this a double[trials][derivNames]
		
		// get a Matrix of trials X shortNames, that holds the values of derivative prices per trial, per shortName
		Matrix resultsPerTrialPerDerivName = getAllTrialValues(derivNames,dse,mcInputsQueryMap,trials);
		// get a Matrix of trials X shortNames that holds the differences of the trial derivative price minus the current 
		//   deriviatve price.
		Matrix derivPriceChangePerTrial = resultsPerTrialPerDerivName.minus(currPriceArrayCopied);
		// now, add the values for each row together, to create a portfolio value per trial
		Matrix ones = new Matrix(underlyingNames.size(),1,1.0);
		double[] portfolioChangesPerTrial = derivPriceChangePerTrial.times(ones).getColumnPackedCopy();

		// the var is the worst n% of these trial sums
		double var = 
				MathStuff.excelPercentile(portfolioChangesPerTrial, .01);
		
		double var2 = getAnalyticVar()/2;
		
		Utils.prt("testMcVsAnalyticWithModelsWithOptions monte carlo var: " + var + "  analytic var: "+var2);
		double diff = Math.abs(var+var2);
		assertTrue(diff<var2*.2);
		
		
	}

	private Set<String> generateDerivNamesFromUnderlyingNames(Set<String> snSet){
		Set<String> ret = new HashSet<String>();
		QueryInterface<String, SecDef> sdQuery = new SecDefQueryAllMarkets();
		Calendar expiryCal = Calendar.getInstance();
		expiryCal = Dates.addToCalendar(expiryCal, 3, Calendar.MONTH, true);
		Long expiry = new Long(expiryCal.get(Calendar.YEAR)*100 + expiryCal.get(Calendar.MONTH)+1);
		
		for(String sn:snSet){
			SecDef sd = sdQuery.get(sn, 1, TimeUnit.SECONDS);
			String sym = sd.getSymbol();
			SecSymbolType type = SecSymbolType.OPT;
			SecExchange exch = sd.getExchange();
			SecCurrency curr = sd.getCurrency();
			SecRight right = SecRight.C;
			BigDecimal price = atmMap.get(sn).getResult();
			BigDecimal strike = price.divide(BigDecimal.TEN,0,RoundingMode.HALF_EVEN).multiply(BigDecimal.TEN).setScale(2,RoundingMode.HALF_EVEN);
			String optSn = sym + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR +
					type.toString() + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR +
					exch.toString() + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR +
					curr.toString() + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR +
					expiry.toString() + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR +
					right.toString() + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR +
					strike.toString() ;
			SecDef optSd = sdQuery.get(optSn,1, TimeUnit.SECONDS);
			
			ret.add(optSd.getShortName());
			
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
	
	private Matrix getCopiedCurrentValues(Set<String> derivNames, int trials){
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Map<String,DerivativeReturn[]> currPrices = dse.getSensitivity(new OptPriceDerSen(), derivNames);
		// make an array of these prices
		double[] currPriceArray = new double[currPrices.size()];
		for(int i = 0;i<orderedDerivNames.size();i++){
			currPriceArray[i]= currPrices.get(orderedDerivNames.get(i))[0].getValue().doubleValue();
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
	private static Matrix getAllTrialValues(
			Set<String> derivNames,
			DerivativeSetEngine dse,
			Map<DioType<?>,QueryInterface<Set<String>,Map<String,double[]>>> mcInputsQueryMap,
			int trials){
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Map<DioType<?>, Map<String, double[]>> diotToShortNameToTrialValues = getTrialInputs(derivNames,dse,mcInputsQueryMap);
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
			double[] resultsPerTrial = dse.getModel(derivName).getPriceArray(mainTrialsPerDiot, underTrialsPerDiot);
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
			double[] trialValuesPerDiot = diotToShortNameToTrialValues.get(mainDiot).get(derivName);
			ret.put(mainDiot,trialValuesPerDiot);
		}
		return ret;
	}
	
	private static Map<DioType<?>, double[][]> getTrialValuesPerUnderlyingDiot(
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
	 *           1.  the key of the top leve is a DioType and the value is another Map
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
			Map<DioType<?>,QueryInterface<Set<String>,Map<String,double[]>>> mcInputsQueryMap){
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
		for(DioType<?> mainDiot:mainDiots){
			Map<String, double[]> trialValues = 
					mcInputsQueryMap.get(mainDiot).get(mainDiotNames, 1, TimeUnit.SECONDS);	
			diotToShortNameToTrialValues.put(mainDiot, trialValues);
		}
		for(DioType<?> underDiot:underDiots){
			Map<String, double[]> trialValues = 
					mcInputsQueryMap.get(underDiot).get(underDiotNames, 1, TimeUnit.SECONDS);	
			diotToShortNameToTrialValues.put(underDiot, trialValues);
		}
		
		return diotToShortNameToTrialValues;
	}
	
	
	private Set<String> getAllNames(Set<String> derivNames){
		Set<String> ret = new HashSet<String>();	
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			for(SecDef sd:dse.getQueryManager().getUnderlyingSecDefs(derivName, 1, TimeUnit.SECONDS)){
				ret.add(sd.getShortName());
			}
		}
		return ret;
	}
	
	private static Map<String,List<String>> getUnderlyingsPerDerivName(
			Set<String> derivNames,
			QueryManager qm){
		Map<String,List<String>> ret = 
				new HashMap<String, List<String>>();
		
		
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		Set<String> underDiotNames = new HashSet<String>();
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			List<String> underList = new ArrayList<String>();
			for(SecDef sd:qm.getUnderlyingSecDefs(derivName, 1, TimeUnit.SECONDS)){
				String underName = sd.getShortName();
				underList.add(underName);
				underDiotNames.add(sd.getShortName());
			}
			ret.put(derivName,underList);
		}
		return ret;
	}
	
	private Set<DioType<?>> getAllDiots(Set<String> derivNames){
		Set<DioType<?>> ret = getMainDiots(derivNames);
		ret.addAll(getUnderlyingDiots(derivNames));
		return ret;
	}

	private Set<DioType<?>> getMainDiots(Set<String> derivNames){
		Set<DioType<?>> ret = new HashSet<DioType<?>>();
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			ret.addAll(dse.getModel(derivName).getMainInputTypes());
		}
		return ret;
	}
	
	private Set<DioType<?>> getUnderlyingDiots(Set<String> derivNames){
		Set<DioType<?>> ret = new HashSet<DioType<?>>();
		List<String> orderedDerivNames = new ArrayList<String>(new TreeSet<String>(derivNames));
		for(int i = 0;i<orderedDerivNames.size();i++){
			String derivName = orderedDerivNames.get(i);
			ret.addAll(dse.getModel(derivName).getUnderlyingInputTypes());
		}
		return ret;
		
	}

	/**
	 * return a MultiKeyMap where keys are DioType and shortName and
	 *   values are double[] trial values for that DioType and shortName
	 * @param derivNames
	 * @return
	 */

	private MultiKeyMap getTrialInputs2(Set<String> derivNames){
		// create diot map for the each model/security
		MultiKeyMap ret = new MultiKeyMap();
		// get all inputs
		Set<String> allShortNames = getAllNames(derivNames);
		Set<DioType<?>> allDiots = getAllDiots(derivNames);
		
		for(DioType<?> diot:allDiots){
			Map<String, double[]> trialValues = 
					mcInputsQueryMap.get(diot).get(allShortNames, 1, TimeUnit.SECONDS);	
			for(Entry<String,double[]> entry:trialValues.entrySet()){
				ret.put(diot, entry.getKey(),entry.getValue());
			}
		}
		return ret;
	}

	
	
	private double getAnalyticVar(){
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(underlyingNames, corrMap));
		Matrix atmVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, atmMap),underlyingNames.size());
		
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(underlyingNames, volMap),underlyingNames.size());
		volVector = volVector.times(Math.sqrt(days/252.0));
		Matrix atmTimesVol = volVector.arrayTimes(atmVector);

		
		atmTimesVol = atmTimesVol.times(NormalDistribution.inverseCumulativeDistribution(.99));
		Matrix result = atmTimesVol.transpose().times(corrMatrix).times(atmTimesVol);
		double var2 = Math.sqrt(result.get(0, 0));
		return var2;
		
	}
	
	
	private class AtmLocalInputQuery implements QueryInterface<Set<String>,Map<String,double[]>>{
		private final DseInputQuery<BigDecimal> corrQuery;
		private final DseInputQuery<BigDecimal> atmQuery;
		private final DseInputQuery<BigDecimal> volQuery;

		
		public AtmLocalInputQuery(DseInputQuery<BigDecimal> corrQuery,
				DseInputQuery<BigDecimal> atmQuery,
				DseInputQuery<BigDecimal> volQuery) {
			super();
			this.corrQuery = corrQuery;
			this.atmQuery = atmQuery;
			this.volQuery = volQuery;
		}


		@Override
		public Map<String, double[]> get(Set<String> snSet, int timeoutValue,
				TimeUnit timeUnitType) {
			Map<String, ComplexQueryResult<BigDecimal>> corrMap =
					corrQuery.get(underlyingNames, 20, TimeUnit.SECONDS);
			String mess = MarketDataComLib.CqrInvalidResultListString(corrMap);
			if(mess!=null)
				throw Utils.IllState(MarketDataComLib.class, mess);
	
			Map<String, ComplexQueryResult<BigDecimal>> atmMap  = 
					atmQuery.get(underlyingNames, 30, TimeUnit.SECONDS);
			mess = MarketDataComLib.CqrInvalidResultListString(atmMap);
			if(mess!=null)
				throw Utils.IllState(MarketDataComLib.class, mess);
			Map<String,ComplexQueryResult<BigDecimal>>  volMap = 
					volQuery.get(underlyingNames, 30, TimeUnit.SECONDS);
			mess = MarketDataComLib.CqrInvalidResultListString(volMap);
			if(mess!=null)
				throw Utils.IllState(MarketDataComLib.class, mess);
			return getAtmTrialValues(snSet,corrMap,atmMap,volMap,1,trials);
		}
	}
	
	/**
	 * This method gets correlated random Atm values from a
	 *   corrMap, an atmMap, a volMap and the number of trials
	 *   The method getCorrelatedValues will create correlated random prices
	 *     that can be used as Atm value by an options model.
	 * @param snSet
	 * @param corrMap
	 * @param atmMap
	 * @param volMap
	 * @param daysOfVar
	 * @param trials
	 * @return
	 */
//	private static Map<String,double[]> getAtmTrialValues(
	private Map<String,double[]> getAtmTrialValues(
			Set<String> snSet,
			Map<String, ComplexQueryResult<BigDecimal>> corrMap,
			Map<String, ComplexQueryResult<BigDecimal>> atmMap,
			Map<String, ComplexQueryResult<BigDecimal>> volMap,
			int daysOfVar,
			int trials){
		
	
		// make a Matrix out of the corrMap
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(snSet, corrMap));
		// make vectors from the atmMap and volMap
		Matrix atmVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(snSet, atmMap),snSet.size());
		
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(snSet, volMap),snSet.size());
		volVector = volVector.times(Math.sqrt(daysOfVar/252.0));

		// create correlated random Atm values for all shortNames, and all trials
		//  the array of corrPrices will have a number or rows  = trials, and
		//   a number of colums = to snSet.size().
		double[][] corrPrices =MathStuff.getCorrelatedValues(trials, volVector, atmVector, corrMatrix).transpose().getArray();
		Map<String,double[]> ret = new HashMap<String, double[]>();
		List<String> orderedList = 
				new ArrayList<String>(new TreeSet<String>(snSet));
		
		// for each shortName, put the row vector of correlated randoms Atm's in
		//   the map that you return to the caller.
		for(int i = 0;i<orderedList.size();i++){
			ret.put(orderedList.get(i),corrPrices[i]);
		}
		return ret;
	
	}

	private class VolLocalInputQuery implements QueryInterface<Set<String>,Map<String,double[]>>{
		private final DseInputQuery<BigDecimal> corrQuery;
		private final DseInputQuery<BigDecimal> volQuery;
		private final DerivativeSetEngine dse;
		

		public VolLocalInputQuery(DseInputQuery<BigDecimal> corrQuery,
				DseInputQuery<BigDecimal> volQuery,
				DerivativeSetEngine dse) {
			super();
			this.corrQuery = corrQuery;
			this.volQuery = volQuery;
			this.dse = dse;
		}

		@Override
		public Map<String, double[]> get(Set<String> snSet, int timeoutValue,
				TimeUnit timeUnitType) {
			Map<String, ComplexQueryResult<BigDecimal>> corrMap =
					corrQuery.get(underlyingNames, 20, TimeUnit.SECONDS);
//					dse.getQueryManager().corrFromDerivNameSet(snSet, timeoutValue, timeUnitType);
			String mess = MarketDataComLib.CqrInvalidResultListString(corrMap);
			if(mess!=null)
				throw Utils.IllState(MarketDataComLib.class, mess);
			Map<String, ComplexQueryResult<BigDecimal>> volMap = 
					volQuery.get(snSet, 1, TimeUnit.SECONDS);
			return getVolTrialValues(snSet,corrMap,volMap,dse,days,trials);
		}
	}
	

	/**
	 * This method will use the volatilies of the actual derivatives
	 *   in conjunction with the correlations of their underlyings to
	 *   create correlated vol changes
	 * @param snSet
	 * @param corrMap
	 * @param volMap
	 * @param daysOfVar
	 * @param trials
	 * @return
	 */
	private static Map<String,double[]> getVolTrialValues(
			Set<String> snSet,
			Map<String, ComplexQueryResult<BigDecimal>> corrMap,
			Map<String, ComplexQueryResult<BigDecimal>> volMap,
			DerivativeSetEngine dse,
			int daysOfVar,
			int trials){
		
		List<String> orderedList = new ArrayList<String>(new TreeSet<String>(snSet));
		Map<String, List<String>> derivSnToUnderlyingSnList = getUnderlyingsPerDerivName(snSet,dse.getQueryManager());
		
		// get underlying names in the same order as the derivative name
		List<String> underList = new ArrayList<String>();
		for(String derivName:orderedList){
			underList.add(derivSnToUnderlyingSnList.get(derivName).get(0));
		}
		
		Matrix corrMatrix = new Matrix(MathStuff.buildCorrMatrixFromCqrBigDecMap(underList, corrMap));
		Matrix volVector = 
				new Matrix(MathStuff.getVectorFromCqrBigDecMap(snSet, volMap),snSet.size());
		
		Matrix psuedoVolVector = new Matrix(snSet.size(),1,0.10);

		double[][] corrVols =MathStuff.getCorrelatedValues(trials, psuedoVolVector, volVector, corrMatrix).transpose().getArray();
		Map<String,double[]> ret = new HashMap<String, double[]>();
		
		for(int i = 0;i<orderedList.size();i++){
			ret.put(orderedList.get(i),corrVols[i]);
		}
		return ret;
		
	}
	
	private class RepeatInputQuery<T extends Number> implements QueryInterface<Set<String>,Map<String,double[]>>{
		
		private RepeatInputQuery(DioType<T> dioType) {
			super();
			this.dioType = dioType;
		}
		
		private final DioType<T> dioType;
		@Override
		public Map<String, double[]> get(Set<String> snSet, int timeoutValue,
				TimeUnit timeUnitType) {
			Map<String, ComplexQueryResult<T>> r = 
					dse.getQueryManager().getDioInputs(dioType, snSet, timeoutValue,timeUnitType);
			List<String> orderedList = 
					new ArrayList<String>(new TreeSet<String>(snSet));
			int len = orderedList.size();
			double[] vecDouble = new double[len];
			List<Exception> excepts = new ArrayList<Exception>();
			for(int i = 0;i<len;i++){
				String sn = orderedList.get(i);
				ComplexQueryResult<T> cqr = r.get(sn);
				if(!cqr.isValidResult()){
					excepts.add(cqr.getException());
					continue;
				}
				vecDouble[i] = cqr.getResult().doubleValue();
			}
			if(excepts.size()>0){
				throw Utils.illStateWithExceptList(this.getClass(),  excepts);
			}
			
//			double[][] retDouble = MathStuff.generateDuplicateRowMatrix(trials,new Matrix(vecDouble,len)).transpose().getArray();
			double[][] retDouble = MathStuff.generateDuplicateRowMatrix(trials,vecDouble).transpose().getArray();
			Map<String,double[]> ret = new HashMap<String, double[]>();
			
			for(int i = 0;i<len;i++){
				ret.put(orderedList.get(i),retDouble[i]);
			}
			return ret;
		}
	}

	private class VolQueryForOptionsTest extends InputQuery<BigDecimal>{

		public VolQueryForOptionsTest(Map<String, SecInputsInfo> testDataMap) {
			super(testDataMap);
		}

		@Override
		public BigDecimal newValue(SecInputsInfo testValue) {
			if(testValue.vol==null){
				String underSn =
						dse.getQueryManager().getUnderlyingSecDefs(
								testValue.shortName, 1, TimeUnit.SECONDS).get(0).getShortName();
				return volMap.get(underSn).getResult();
			}
			return testValue.vol;
		}
		
	}
	
	private <T1,T2> List<Tuple<T1, T2>> getAllCombos(Set<T1> t1Set, Set<T2> t2Set){
		List<Tuple<T1, T2>> ret = new ArrayList<Tuple<T1,T2>>();
		for(T1 t1:t1Set){
			for(T2 t2:t2Set){
				Tuple<T1,T2> item = new Tuple<T1, T2>(t1, t2);
				ret.add(item);
			}
		}
		return ret;
	}


}
