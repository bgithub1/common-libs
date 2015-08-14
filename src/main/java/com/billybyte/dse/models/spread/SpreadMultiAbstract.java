package com.billybyte.dse.models.spread;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.DerivativeAbstractModel;
import com.billybyte.dse.DerivativeSetEngine;
import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.diotypes.AtmDiot;
import com.billybyte.dse.inputs.diotypes.CallPutDiot;
import com.billybyte.dse.inputs.diotypes.CorrDiot;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.inputs.diotypes.DivDiot;
import com.billybyte.dse.inputs.diotypes.DteFromSettleDiot;
import com.billybyte.dse.inputs.diotypes.ImpliedCorr;
import com.billybyte.dse.inputs.diotypes.RateDiot;
import com.billybyte.dse.inputs.diotypes.SettlePriceDiot;
import com.billybyte.dse.inputs.diotypes.StrikeDiot;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.models.spread.newtonraphsonforspreads.NewtonRaphsonImpliedCorrelation;
import com.billybyte.dse.models.vanilla.NormalDistribution;
import com.billybyte.dse.models.vanilla.VanOptAbst;
import com.billybyte.dse.outputs.CorrRiskDerSen;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DeltaNeutralVarDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.DirectionalVarDerSen;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.VegaDerSen;
import com.billybyte.marketdata.SecDef;

/**
 * Abstract class for all DerivativeSetEngine models that are spread based,
 *   like CSO's etc.
 * @author bperlman1
 *
 */
public abstract class SpreadMultiAbstract extends DerivativeAbstractModel{
	final DirectionalVarDerSen directionalVarSense = new  DirectionalVarDerSen();
	final DeltaNeutralVarDerSen deltaNeutralVarSense = new  DeltaNeutralVarDerSen();
	final DeltaDerSen deltaSense = new DeltaDerSen();
	final VegaDerSen vegaSense = new VegaDerSen();
	final GammaDerSen gammaSense = new GammaDerSen();
	final double DEFAULT_CONFIDENCE_FOR_VAR = .99;
	private final double normSinv99 = NormalDistribution.inverseCumulativeDistribution(DEFAULT_CONFIDENCE_FOR_VAR);

	protected static final String CORR_RISK = 
			new CorrRiskDerSen().getString();

	
	public abstract Double getSpreadPrice(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr,Object other0, Object other1);

	public abstract Double[] getSpreadDelta(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);

	public abstract Double[] getSpreadGamma(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);
	
	public abstract Double[] getSpreadVega(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);

	public abstract Double getSpreadTheta(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);

	public abstract Double getSpreadRho(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);

	public abstract Double[] getSpreadDayDel(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);

	public abstract Double getSpreadCorrRisk(double callPut,double atmLeg0, double atmLeg1,double strike,
			double dte, double volLeg0, double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,double corr, Object other0, Object other1);

	
	private static final CallPutDiot cpDiot = new CallPutDiot();
	private static final AtmDiot atmType = new AtmDiot();
	private static final StrikeDiot strkDiot = new StrikeDiot();
//	private static final DteSimpDiot dteType = new DteSimpDiot();
	private static final DteFromSettleDiot dteType = new DteFromSettleDiot();
//	private static final UnderlingVolsFromVsDiot vsDiot = new UnderlingVolsFromVsDiot();
	private static final VolDiot vsDiot = new VolDiot();
	private static final RateDiot rateType = new RateDiot();
	private static final DivDiot divType = new DivDiot();
	private static final ImpliedCorr  corrType = new ImpliedCorr();
	private static final SettlePriceDiot settleDiot = new SettlePriceDiot(); 
	private final DioType<?>[] underTypeList = new DioType<?>[] {
			atmType,vsDiot,rateType,divType};
	private final DioType<?>[] mainTypeList = new DioType[]{
			cpDiot,strkDiot,dteType,corrType,settleDiot};
	
	
	protected class MultiBlk{
		double callPut; double[] atms;
		double strike; double dte; double[] vols; double[] rates;
		double[] divs;Double corr;double settle;

		boolean allGood;
		List<String> ret = new ArrayList<String>();
		protected MultiBlk(InBlk inblk){
			allGood = true;
			callPut = cpDiot.getMainInputs(inblk).doubleValue();
			if(isNan(callPut)){
				allGood=false;
				ret.add("bad callput");
			}
			strike = strkDiot.getMainInputs(inblk).doubleValue();
			if(isNan(strike)){
				allGood=false;
				ret.add("bad strike");
			}
			dte = dteType.getMainInputs(inblk).doubleValue();
			if(isNan(dte)){
				allGood=false;
				ret.add("bad dte");
			}

			
			atms = VanOptAbst.bdListToDoubleArray(atmType, inblk);
			if(atms==null){
				allGood=false;
				ret.add("bad atm per underlying inputs");
			}
			rates = VanOptAbst.bdListToDoubleArray(rateType,inblk);
			if(rates==null){
				allGood=false;
				ret.add("bad rate per underlying inputs");
			}
			divs = VanOptAbst.bdListToDoubleArray(rateType,inblk);
			if(divs==null){
				allGood=false;
				ret.add("bad div per underlying inputs");
			}
			
			vols = VanOptAbst.bdListToDoubleArray(vsDiot,inblk);
			if(vols==null){
				allGood=false;
				ret.add("bad vol array per underlying inputs");
			}
			BigDecimal bdCorr = corrType.getMainInputs(inblk);
//			if(bdCorr==null ){
//				allGood=false;
//				ret.add("bad correlation input");
//			}else{
//				corr = bdCorr.doubleValue();
//			}
			if(bdCorr!=null ){
				corr = bdCorr.doubleValue();
			}else{
				corr = null;
			}
			SettlementDataInterface sdi = settleDiot.getMainInputs(inblk);
			settle = -1;
			if(sdi!=null){
				BigDecimal bdsettle = sdi.getPrice();
				if(bdsettle!=null){
					settle = bdsettle.doubleValue();
				}
			}
		}
		
		protected String getProblems(){
			if(allGood)return null;
			String problem = "";
			for(String s:ret){
				problem = problem+s+";";
			}
			return problem;
		}
	}
	
	/**
	 * Same constructor as always
	 * @param evaluationDate
	 */
	public SpreadMultiAbstract(Calendar evaluationDate) {
		super(evaluationDate);
	}

	@Override
	public DerivativeReturn getPrice(String derivativeShortName, InBlk inputs) {
		MultiBlk inBlk = new MultiBlk(inputs);
		if(!inBlk.allGood){
			Exception e = Utils.IllState(this.getClass(),inBlk.getProblems());
			DerivativeReturn dr = 
					new DerivativeReturn(new OptPriceDerSen(), derivativeShortName, null, e);
			return dr;
		}
		
		Double price = getSpreadPrice(inBlk.callPut, inBlk.atms[0],inBlk.atms[0] , 
				inBlk.strike, inBlk.dte, 
				inBlk.vols[0], inBlk.vols[0], inBlk.rates[0], inBlk.rates[1],inBlk.divs[0], inBlk.divs[1], 
				inBlk.corr, null, null);
		if(price==null){
			Exception e = Utils.IllState(this.getClass(),"null return from  getSpreadPrice");
			DerivativeReturn dr = 
					new DerivativeReturn(new OptPriceDerSen(), derivativeShortName, null, e);
			return dr;
		}
		return new DerivativeReturn(new OptPriceDerSen(), derivativeShortName, price);
	}

	@Override
	public DerivativeReturn[] getAllSensitivites(String derivativeShortName,
			InBlk inputs) {
		return null;
	}

	
	@Override
	public DerivativeReturn[] getSensitivity(
			DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName, InBlk inputs) {
		MultiBlk inBlk = new MultiBlk(inputs);
		if(!inBlk.allGood){
			Exception e = Utils.IllState(this.getClass(),inBlk.getProblems());
			DerivativeReturn dr = 
					new DerivativeReturn(sensitivityId, derivativeShortName, null, e);
			return new DerivativeReturn[]{dr};
		}
		Double[] value = null;
		// first do sensitivities with respect to derivativeShortName
		double cp = inBlk.callPut;
		double f1 = inBlk.atms[0];
		double f2 = inBlk.atms[1];
		double strike = inBlk.strike;
		double dte = inBlk.dte;
		double vol1 = inBlk.vols[0];
		double vol2 = inBlk.vols[1];
		double rate0 = inBlk.rates[0];
		double rate1 = inBlk.rates[1];
		double div0 = inBlk.divs[0];
		double div1 = inBlk.divs[1];
		Double corr = inBlk.corr;
		
		
		// first see if you are processing a sensitivity that requires the corr variable to be non-null
		//  The corr variable is need for all sensitivities but implied correlation
		if(corr!=null){
			if(sensitivityId.getString().compareTo(OPTPR)==0){
				Double greek = getSpreadPrice(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null);
				if(greek==null || isNan(greek)) {
					value=null;
				}else{
					return new DerivativeReturn[]{new DerivativeReturn(sensitivityId, derivativeShortName, greek)};
				}
			}
			if(sensitivityId.getString().compareTo(THETA)==0){
				Double greek = getSpreadTheta(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null);
				if(greek==null || isNan(greek)) {
					value=null;
				}else{
					return new DerivativeReturn[]{new DerivativeReturn(sensitivityId, derivativeShortName, greek)};
				}
			}

			if(sensitivityId.getString().compareTo(CORR_RISK)==0){
				Double greek = getSpreadCorrRisk(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null);
				if(greek==null || isNan(greek)) {
					value=null;
				}else{
					return new DerivativeReturn[]{new DerivativeReturn(sensitivityId, derivativeShortName, greek)};
				}
			}
			
			// now do sensitivities that with respect to each underlying
			if(sensitivityId.getString().compareTo(DELTA)==0){
				Double[] greek = getSpreadDelta(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null);
				value = greek;
			}
			if(sensitivityId.getString().compareTo(GAMMA)==0){
				Double[] greek = getSpreadGamma(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null);
				value = greek;
			}
			if(sensitivityId.getString().compareTo(VEGA)==0){
				Double[] greek = getSpreadVega(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null);
				value = greek;
			}

			if(sensitivityId.equals(directionalVarSense)){
				SecDef[] sds = inputs.getUnderlyingSds();
				DerivativeReturn[] vars = getDirectionalVar(sensitivityId, derivativeShortName, inputs);
				if(sds.length!=vars.length){
					Exception e = Utils.IllState(this.getClass(),derivativeShortName+" : "+sensitivityId+" unit vars returned of "+vars.length+" does not equal number of underlyings of "+sds.length);
					DerivativeReturn drErr = new DerivativeReturn(sensitivityId, derivativeShortName, null, e);
					return new DerivativeReturn[]{drErr};
				}
				DerivativeReturn[] drRet = new DerivativeReturn[sds.length];
				for(int i = 0;i<vars.length;i++){
					drRet[i] = new DerivativeReturn(sensitivityId, sds[i].getShortName(), vars[i].getValue(), vars[i].getException());
				}

				return drRet;
			}

			if(sensitivityId.equals(deltaNeutralVarSense)){
				SecDef[] sds = inputs.getUnderlyingSds();
				DerivativeReturn[] vars = getDeltaNeutralVar(sensitivityId, derivativeShortName, inputs);
				if(sds.length!=vars.length){
					Exception e = Utils.IllState(this.getClass(),derivativeShortName+" : "+sensitivityId+" unit vars returned of "+vars.length+" does not equal number of underlyings of "+sds.length);
					DerivativeReturn drErr = new DerivativeReturn(sensitivityId, derivativeShortName, null, e);
					return new DerivativeReturn[]{drErr};
				}
				DerivativeReturn[] drRet = new DerivativeReturn[sds.length];
				for(int i = 0;i<vars.length;i++){
					drRet[i] = new DerivativeReturn(sensitivityId, sds[i].getShortName(), vars[i].getValue(), vars[i].getException());
				}

				return drRet;
			}
			
		}
		
		
		
		// The correlation sensitivity is NOT included in the main sensitivities to be returned,
		//  but can be called separately when needed.
		// !!!!!! It also does not need the corr variable b/c it will find the corr variable !!!!!
		if(sensitivityId.equals(IMPLIEDVOL)){
		//(cp,f1,f2,strike,dte,vol1,vol2,rate0,rate1,div0,div1,corr, null, null)
			if(inBlk.settle>=0){
				double precision = .00001;
				int maxIterations = 1000;
				CsoModel spreadModel = new CsoModel();
				double initialCorrelation = 0;
				Object other1 = null;
				Object other2 = null;
				Double greek  = impliedCorrelation(inBlk.settle, precision, maxIterations, spreadModel, 
						cp, f1,f2,strike,dte,vol1,vol2,rate0,div0,div1, initialCorrelation, other1,other2);
				if(greek==null || isNan(greek)) {
					value=null;
				}else{
					return new DerivativeReturn[]{new DerivativeReturn(sensitivityId, derivativeShortName, greek)};
				}

			}
		}

		if(value==null){
			Exception e = Utils.IllState(this.getClass(),"null return for all sensitivities of multi sensitivity. ");
			DerivativeReturn dr = 
					new DerivativeReturn(sensitivityId, derivativeShortName, null, e);
			return new DerivativeReturn[]{dr};
		}
		DerivativeReturn[] drRet = new DerivativeReturn[value.length];
		SecDef[] sds = inputs.getUnderlyingSds();
		for(int i = 0;i<sds.length;i++){
			if(value[i]==null || isNan(value[i])){
				Exception e = Utils.IllState(this.getClass(),"null return for specific sensitivity of multi sensitivity. ");
				drRet[i] = 
						new DerivativeReturn(sensitivityId, sds[i].getShortName(), null, e);
			}else{
				drRet[i] = 
						new DerivativeReturn(sensitivityId, sds[i].getShortName(), value[i]);
			}
		}
		return drRet;
	}

	@Override
	public List<DioType<?>> getUnderlyingInputTypes() {
		return Arrays.asList(underTypeList);
	}

	@Override
	public List<DioType<?>> getMainInputTypes() {
		return Arrays.asList(mainTypeList);
	}

	
	private static final double rnd(double n,int p){
		double x = Math.round(n * Math.pow(10,p));
		return x / Math.pow(10,p);
	}
	
	public static double impliedCorrelation(
			double spreadPriceToAchieve,
			double precision,
			int maxIterations,
			CsoModel spreadModel,
			double callPut,
			double atmLeg0, 
			double atmLeg1,
			double strike,
			double dte, 
			double volLeg0, 
			double volLeg1, 
			double rate, 
			double divLeg0, 
			double divLeg1,
			double initialCorrelation, 
			Object other0, 
			Object other1){
		
		BinarySearchImpliedCorrelation bsImpCorr = 
				new BinarySearchImpliedCorrelation(
						spreadPriceToAchieve, .00005, 10000, 
						spreadModel, callPut, atmLeg0, atmLeg1, strike,
						dte, volLeg0, volLeg1, rate, divLeg0, divLeg1, other0, other1);
		double bsCorr = bsImpCorr.findByInteration();
		if(!Double.isInfinite(bsCorr) || !Double.isNaN(bsCorr)){
			return rnd(bsCorr,10);
		}
		
		
		double priceCorrOne = spreadModel.getSpreadPrice(
				callPut, atmLeg0, atmLeg1, strike, dte, 
				volLeg0, volLeg1, rate, rate,divLeg0, divLeg1, 1.0, other0, other1);
		if(spreadPriceToAchieve<= priceCorrOne)return 1.0;
		double priceCorrNegOne = spreadModel.getSpreadPrice(callPut, atmLeg0, atmLeg1, strike, dte, 
				volLeg0, volLeg1, rate,rate, divLeg0, divLeg1, -1.0, other0, other1);
		if(spreadPriceToAchieve>= priceCorrNegOne)return -1.0;
		
		NewtonRaphsonImpliedCorrelation nr = 
				new NewtonRaphsonImpliedCorrelation(
						spreadPriceToAchieve,
						precision,maxIterations,spreadModel, callPut, atmLeg0, 
						atmLeg1, strike, dte, volLeg0, volLeg1, rate, divLeg0, 
						divLeg1, other0, other1);
		Double result = Double.NaN;
		double seed = initialCorrelation;
		if(seed<-1)seed = -.99;
		if(seed>1)seed = .99;
		for(int i = 0;i<20;i++){
			result = nr.newtraph(seed);
			if(result.compareTo(Double.NaN)!=0 && result<=0.999999999 && result>=-0.999999999)break;
			seed = seed +.1;
			if(seed>1)break;
		}
		return result;
	}

	public DerivativeReturn[] getDirectionalVar(
			DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName, InBlk inputs) {

		// do Var
		DerivativeReturn[] deltas = 
				this.getSensitivity(deltaSense, derivativeShortName, inputs);
		
		DerivativeReturn[] bads = 
				badReturns(sensitivityId, derivativeShortName, deltas);
		if(bads!=null){
			return bads;
		}
		
		MultiBlk inBlk = new MultiBlk(inputs);
		double[] vars = new double[inBlk.atms.length];
		for(int i = 0; i<deltas.length; i++){
			// return a VaR for each delta
			double delta = deltas[i].getValue().doubleValue();
			double var = (delta * inBlk.atms[i] * normSinv99 * inBlk.vols[i]/ Math.sqrt(252.0));
			vars[i]= var;
		}
		
		DerivativeReturn[] ret = new DerivativeReturn[vars.length];
		for(int i = 0;i<vars.length;i++){
			double var = vars[i];
			ret[i] = goodRet(sensitivityId,
					derivativeShortName,var);
		}
		return ret;
	}

	
	public DerivativeReturn[] getDeltaNeutralVar(
			DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName, InBlk inputs) {
		double value=Double.NaN;
		
		// do Var
		DerivativeReturn[] vegas = 
				this.getSensitivity(vegaSense, derivativeShortName, inputs);
		DerivativeReturn[] gammas = 
				this.getSensitivity(gammaSense, derivativeShortName, inputs);
		
		DerivativeReturn[] bads = 
				badReturns(sensitivityId, derivativeShortName, vegas);
		if(bads!=null){
			return bads;
		}
		bads = 
				badReturns(sensitivityId, derivativeShortName, gammas);
		if(bads!=null){
			return bads;
		}

		
		MultiBlk inBlk = new MultiBlk(inputs);
		String problems = inBlk.getProblems();
		if(!inBlk.allGood){
			Exception e = Utils.IllState(this.getClass(), 
					" Cannot compute Implied Vol " +  
						problems +  " with inputs : " + 
						inBlk.toString());
			return new DerivativeReturn[]{
					new DerivativeReturn(
							sensitivityId, 
							derivativeShortName, 
							null,e)};
			
		}

		
		DerivativeReturn[] ret = new DerivativeReturn[inBlk.atms.length];
		for(int i = 0;i<vegas.length;i++){
			double vega = vegas[i].getValue().doubleValue();
			double gamma = gammas[i].getValue().doubleValue();
			double gammaEffect = Double.NaN;
			if(!Double.isNaN(gamma)){
				gammaEffect = .25 * gamma * inBlk.vols[i] * inBlk.atms[i] / Math.sqrt(252.0);
			}
			double vegaEffect = Double.NaN;
			if(!Double.isNaN(vega)){
				double vegaMultiplier = inBlk.vols[i] * DerivativeSetEngine.DEFAULT_VOL_STRESS_FOR_VAR * 100 ;
				vegaEffect = vega * vegaMultiplier * .5;
			}
			
			if(Double.isNaN(gammaEffect) || Double.isNaN(vegaEffect)){
				Exception e = Utils.IllState(this.getClass(), " Cannot compute Implied Vol " +  problems +  " with inputs : " + inBlk.toString());
				ret[i] =
						new DerivativeReturn(
								sensitivityId, 
								derivativeShortName, 
								null,e);
				
			}

			value =  vegaEffect  + gammaEffect;
			ret[i] =
					goodRet(sensitivityId,
							derivativeShortName,value);

		}
		
		return ret;
		
	}

	
	public DerivativeReturn[] getItmValue(
			DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName, InBlk inputs) {
		// get atm
		double[] atms = VanOptAbst.bdListToDoubleArray(atmType, inputs);// atmType.getFromInBlk(inputs);
		SecDef[] sds = inputs.getUnderlyingSds();
		if(sds==null || sds.length<2){
			return new DerivativeReturn[]{ errRet(sensitivityId, "","Can't find all Underlying SecDefs for : "+derivativeShortName)};
		}
		if(atms==null || atms.length<2){
			return new DerivativeReturn[]{ errRet(sensitivityId, inputs.getMainSd().getShortName(),"Can't find all atms for : "+derivativeShortName)};
		}
		return new DerivativeReturn[]{ goodRet(sensitivityId, inputs.getMainSd().getShortName(),atms[0])};

	}

	
	DerivativeReturn[] badReturns(
			DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName,			
			DerivativeReturn[] drs){
		if(drs==null || drs.length<1){
			Exception e = Utils.IllState(this.getClass(), 
					" Cannot compute Implied Vol " +  
							" no return from delta calculation");
			return new DerivativeReturn[]{
					new DerivativeReturn(
							sensitivityId, 
							derivativeShortName, 
							null,e)};
		}

		
		boolean allGood = true;
		DerivativeReturn[] badRets = new DerivativeReturn[drs.length]; 
		for(int i = 0;i<drs.length;i++){
			DerivativeReturn dr = drs[i];
			if(!dr.isValidReturn()){
				allGood=false;
				badRets[i] = 
						new DerivativeReturn(
								sensitivityId, 
								derivativeShortName, 
								null,dr.getException());
			}
		}
		if(!allGood){
			return badRets; 
		}
		
		return null;

	}

	@Override
	public double[] getPriceArray(Map<DioType<?>, double[]> mainInputs,
			Map<DioType<?>, double[][]> underlyingInputs) {
		return null;
	}
	


}
