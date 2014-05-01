package com.billybyte.commonlibs.testcases.dse;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import junit.framework.TestCase;

import Jama.Matrix;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.models.vanilla.NormalDistribution;
import com.billybyte.dse.queries.DseInputQuery;
import com.billybyte.dse.var.McVar;

import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecRight;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.mathstuff.MathStuff;
import com.billybyte.queries.ComplexQueryResult;

public class TestMonteCarlo extends TestCase{

	private final DioType<BigDecimal> atmDiot = new AtmDiot();
	private final DioType<BigDecimal>  volDiot = new VolDiot();
	private final DioType<BigDecimal>  corrDiot = new CorrDiot();

	
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
	
	@Override
	protected void setUp() throws Exception {
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

		super.setUp();
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

		McVar mcv = new McVar();
		double var = mcv.mcVar(derivNames, trials, dse);
		
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
	

}
