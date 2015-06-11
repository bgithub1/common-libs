package com.billybyte.dse.models.vanilla;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeAbstractModel;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CallPutDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.DivDiot;
import com.billybyte.dse.inputs.diotypes.DteSimpleDiot;
import com.billybyte.dse.inputs.diotypes.RateDiot;
import com.billybyte.dse.inputs.diotypes.SettlePriceDiot;
import com.billybyte.dse.inputs.diotypes.StrikeDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.OptPriceDerSen;


public abstract class VanOptAbst extends DerivativeAbstractModel{
	private final String BADVOL = "bad vol;";
	private final double NEWTON_RAPHSON_PRECISION = 0.00001;
	private final int NEWTON_RAPHSON_MAX_ITERATIONS = 500;
	private final double NEWTON_RAPHSON_SEED_VOL = .1;
		
	/**
	 * default
	 * 
	 * @param evaluationDate
	 */
	public VanOptAbst(Calendar evaluationDate) {
		this(evaluationDate,new VolDiot(),null);
	}
	/**
	 * custom vsDiot
	 * 
	 * @param evaluationDate
	 * @param vsDiot
	 */
	public VanOptAbst(Calendar evaluationDate, DioType<BigDecimal> vsDiot) {
		this(evaluationDate,vsDiot,null);
//		this.vsDiot = vsDiot;
//		mainTypeList = new DioType[]{
//				cpDiot,strkDiot,dteType,vsDiot};
	}

	/**
	 * specify vsDiot and others array
	 * 
	 * @param evaluationDate
	 * @param vsDiot
	 * @param others
	 */
	public VanOptAbst(Calendar evaluationDate, DioType<BigDecimal> vsDiot,
			DioType<?>[] others) {
		super(evaluationDate);
		this.vsDiot = vsDiot;
		mainTypeList = new DioType[]{
				cpDiot,strkDiot,dteType,vsDiot,settlePriceDiot};
//				cpDiot,strkDiot,dteType,vsDiot};
		this.otherList = others;
	}

	public abstract double getVanillaPrice(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaDelta(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaGamma(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaVega(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaTheta(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaRho(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaDayDelta(double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);
	public abstract double getVanillaCustomSensitivity(DerivativeSensitivityTypeInterface customSensitivityType,
			double callput,double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others);

	private static final CallPutDiot cpDiot = new CallPutDiot();
	private static final AtmDiot atmType = new AtmDiot();
	private static final StrikeDiot strkDiot = new StrikeDiot();
	private static final DteSimpleDiot dteType = new DteSimpleDiot();
	private static final SettlePriceDiot settlePriceDiot = new SettlePriceDiot();
//	private final VolDiot vsDiot;
	private final DioType<BigDecimal> vsDiot;
	private static final RateDiot rateType = new RateDiot();
	private static final DivDiot divType = new DivDiot();
	private final DioType<?>[] underTypeList = new DioType[]{
			atmType,rateType,divType};
//	private final DioType<?,?>[] mainTypeList = new DioType[]{
//			cpDiot,strkDiot,dteType,vsDiot};
	private final DioType<?>[] mainTypeList;
	private DioType<?>[] otherList;
	
	
	public static double[] bdListToDoubleArray(DioType<BigDecimal> diot,InBlk inputs){
		List<BigDecimal> l = diot.getUnderlyingInputs(inputs);
		double[] ret = new double[l.size()];
		for(int i=0;i<l.size();i++){
			ret[i] = l.get(i).doubleValue();
		}
		return ret;
	}
	
	
	private  class VanInBlk{
		double callput;double atm; double strike;
		double dte; double vol; double rate; double div; Object[] others ;
		boolean allGood;
		Double priceForImplCalc=null;


		
		private VanInBlk(InBlk inputs){
			allGood = true;
			callput =  cpDiot.getMainInputs(inputs);
			strike = strkDiot.getMainInputs(inputs).doubleValue();
			dte = dteType.getMainInputs(inputs).doubleValue();
			double[] atms = bdListToDoubleArray(atmType, inputs);
			if(atms==null || atms.length<1){
				atm = Double.NaN;
				allGood=false;
			}else{
				atm = atms[0];
			}
			vol =  vsDiot.getMainInputs(inputs).doubleValue();
			if(isNan(vol)){
				allGood=false;
			}
			
			double[] rates = bdListToDoubleArray(rateType,inputs);
			if(rates==null || rates.length<1){
				allGood=false;
				rate = Double.NaN;
			}else{
				rate = rates[0];
			}
			double[] divs = bdListToDoubleArray(divType,inputs);
			if(divs==null || divs.length<1){
				allGood =false;
				div=Double.NaN;
			}else{
				div = divs[0];
			}
			if(otherList!=null){
				this.others = new Object[otherList.length];
				for(int k = 0;k<otherList.length;k++){
					this.others[k] = otherList[k].getMainInputs(inputs);
				}
			}
			SettlementDataInterface settle = settlePriceDiot.getMainInputs(inputs); 
			if(settle!=null && settle.getPrice()!=null){
				priceForImplCalc =  settle.getPrice().doubleValue();
			}
			if(priceForImplCalc==null){
				priceForImplCalc = null;
			}
				
		}
		
		String problems(){
			if(allGood)return null;
			String problems = "";
			if(callput==Double.NaN)problems = problems+"bad callput;";
			if(atm==Double.NaN)problems = problems+"bad atm;";
			if(strike==Double.NaN)problems = problems+"bad strike;";
			if(dte==Double.NaN)problems = problems+"bad dte;";
			if(vol==Double.NaN)problems = problems+"bad vol;";
			if(rate==Double.NaN)problems = problems+"bad rate;";
			if(div==Double.NaN)problems = problems+"bad div;";
			if(problems.compareTo("")>0)return problems;
			return null;
		}
	}


	@Override
	public DerivativeReturn getPrice(String derivativeShortName, InBlk inputs) {
		VanInBlk vib = new VanInBlk(inputs);
		String problems = vib.problems();
		if(vib.problems()!=null){
			return errRet(new OptPriceDerSen(),inputs.getMainSd().getShortName(),problems);
		}
		double price =  getVanillaPrice(
				vib.callput, vib.atm, vib.strike, vib.dte, 
				vib.vol, vib.rate, vib.div, vib.others);
		return goodRet(new OptPriceDerSen(),inputs.getMainSd().getShortName(),price);
	}

	@Override
	public DerivativeReturn[] getAllSensitivites(String derivativeShortName,
			InBlk inputs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DerivativeReturn[] getSensitivity(
			DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName, InBlk inputs) {
		if(sensitivityId.getString().compareTo(OPTPR)==0){
			return new DerivativeReturn[]{getPrice(derivativeShortName, inputs)};
		}
		double value=Double.NaN;
		VanInBlk vib = new VanInBlk(inputs);
		String problems = vib.problems();
		if(vib.problems()!=null){
			return new DerivativeReturn[]{
					errRet(new DeltaDerSen(),derivativeShortName,problems)};
		}
		if(sensitivityId.getString().compareTo(DELTA)==0){
			value =  getVanillaDelta(
					vib.callput, vib.atm, vib.strike, vib.dte, 
					vib.vol, vib.rate, vib.div, vib.others);
		}
		if(sensitivityId.getString().compareTo(GAMMA)==0){
			value =  getVanillaGamma(
					vib.callput, vib.atm, vib.strike, vib.dte, 
					vib.vol, vib.rate, vib.div, vib.others);
		}
		if(sensitivityId.getString().compareTo(VEGA)==0){
			value =  getVanillaVega(
					vib.callput, vib.atm, vib.strike, vib.dte, 
					vib.vol, vib.rate, vib.div, vib.others);
		}
		if(sensitivityId.getString().compareTo(THETA)==0){
			value =  getVanillaTheta(
					vib.callput, vib.atm, vib.strike, vib.dte, 
					vib.vol, vib.rate, vib.div, vib.others);
		}
		if(sensitivityId.getString().compareTo(RHO)==0){
			value =  getVanillaRho(
					vib.callput, vib.atm, vib.strike, vib.dte, 
					vib.vol, vib.rate, vib.div, vib.others);
		}

// ************ implied vol *****************		
		// implied vol?
		if(sensitivityId.getString().compareTo(IMPLIEDVOL)==0){
			if(vib.allGood || vib.problems().compareTo(BADVOL)==0){
				// TRY IMPLIED VOL
				if(vib.priceForImplCalc!=null){
					try{
						value = AnalyticFormulas.blackScholesOptionImpliedVolatility(
								vib.atm,
								vib.dte,
								vib.strike,
//								vib.rate,
								1,
								vib.priceForImplCalc);
						if(!Double.isNaN(value) && !Double.isInfinite(value)){
							return new DerivativeReturn[]{
									goodRet(sensitivityId,
											derivativeShortName,value)};
						}
					} catch (Exception e) {
						return new DerivativeReturn[]{
								new DerivativeReturn(
										sensitivityId, 
										derivativeShortName, 
										null,e)};
					}
					if(Double.isNaN(value)){
						Exception e = Utils.IllState(this.getClass(), " Cannot compute Implied Vol with inputs : " + vib.toString());
						return new DerivativeReturn[]{
								new DerivativeReturn(
										sensitivityId, 
										derivativeShortName, 
										null,e)};
					}
					
				}
			}
		}
		// ************ END implied vol *****************		


		
		
		
		if(isNan(value)){
			value = getVanillaCustomSensitivity(sensitivityId, 
					vib.callput, vib.atm, vib.strike, vib.dte, 
					vib.vol, vib.rate, vib.div, vib.others);
			if(isNan(value)){
				return new DerivativeReturn[]{
						errRet(sensitivityId,derivativeShortName,"can't compute value")};
			}
		}
		return new DerivativeReturn[]{
				goodRet(sensitivityId,inputs.getUnderlyingSds()[0].getShortName(),value)};
	}

	@Override
	public List<DioType<?>> getUnderlyingInputTypes() {
		return Arrays.asList(underTypeList);
	}

	
	protected DerivativeReturn errRet(DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName,String cause){
		Exception e  = Utils.IllState(this.getClass(),withRespectToShortName+" : "+cause);
		return new DerivativeReturn(sensitivityId, withRespectToShortName, null,e);
	}
	
	protected DerivativeReturn goodRet(
			DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName,double value){
		return new DerivativeReturn(sensitivityId, withRespectToShortName, value);
	}
	@Override
	public List<DioType<?>> getMainInputTypes() {
		return Arrays.asList(mainTypeList);
	}
	
	
	@Override
	public double[] getPriceArray(Map<DioType<?>, double[]> mainInputs,
			Map<DioType<?>, double[][]> underlyingInputs) {
		int len = mainInputs.get(cpDiot).length;
		double[] cpArr = mainInputs.get(cpDiot);
		double[][] atmArr = underlyingInputs.get(atmType);
		double[] strkArr = mainInputs.get(strkDiot);
		double[] dteArr = mainInputs.get(dteType);
		double[] volArr = mainInputs.get(vsDiot);
		double[][] rateArr = underlyingInputs.get(rateType);
		double[][] divArr = underlyingInputs.get(divType);
		List<double[]> otherArrList = null;
		if(otherList!=null && otherList.length>0){
			otherArrList = new ArrayList<double[]>();
			for(int j=0;j<otherList.length;j++){
				double[] o = mainInputs.get(otherList[j]);
				otherArrList.add(o);
			}
		}

		double[] ret = new double[len];
		Arrays.fill(ret, Double.NaN);
		
		for(int i = 0;i<len;i++){
			double callput=cpArr[i] ; 
			double atm= atmArr[i][0] ;
			double strike= strkArr[i] ;
			double dte= dteArr[i] ; 
			double vol= volArr[i] ;
			double rate = rateArr[i][0] ; 
			double div = divArr[i][0] ;
			Object[] others =null;
			if(otherArrList!=null){
				others = new Object[otherArrList.size()];
				for(int k = 0;k<otherArrList.size();k++){
					others[k] = otherArrList.get(k)[i];
				}
			}
			
			double val = 
					getVanillaPrice(
							callput, atm, strike, 
							dte, vol, rate, div, others);
			ret[i] = val;
		}
		return ret;
	}
	
	
	
	

}
