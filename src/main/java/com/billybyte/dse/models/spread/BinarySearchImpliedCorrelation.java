package com.billybyte.dse.models.spread;

import java.util.Map.Entry;
import java.util.TreeMap;

import com.billybyte.commonstaticmethods.Utils;



public  class BinarySearchImpliedCorrelation {
	private final CsoModel spreadModel;
	private final double  callPut, atmLeg0,  atmLeg1, strike;
	private final double dte,  volLeg0,  volLeg1,  rate;
	private final double divLeg0,  divLeg1;
	private final Object other0,  other1;
	private final double spreadPriceToAchieve;
	private final double precision;
	private final int maxIterations;
	
	
	public BinarySearchImpliedCorrelation(
			double spreadPriceToAchieve,
			double precision, 
			int maxIterations,
			CsoModel spreadModel,
			double callPut, double atmLeg0, double atmLeg1, double strike,
			double dte, double volLeg0, double volLeg1, double rate,
			double divLeg0, double divLeg1, Object other0, Object other1) {
		super();
		
		this.spreadPriceToAchieve = spreadPriceToAchieve;
		this.precision = precision;
		this.maxIterations =maxIterations;
		this.spreadModel = spreadModel;
		this.callPut = callPut;
		this.atmLeg0 = atmLeg0;
		this.atmLeg1 = atmLeg1;
		this.strike = strike;
		this.dte = dte;
		this.volLeg0 = volLeg0;
		this.volLeg1 = volLeg1;
		this.rate = rate;
		this.divLeg0 = divLeg0;
		this.divLeg1 = divLeg1;
		this.other0 = other0;
		this.other1 = other1;
	}
	
	public double findByInteration(){
		TreeMap<Double,Double> resultDiffToCorrMap = new TreeMap<Double, Double>();
		for(double d = -1.0;d<=1;d+=.001){
			double  r = getRoot(d);
			resultDiffToCorrMap.put(r, d);
		}
		Entry<Double, Double> entry = resultDiffToCorrMap.ceilingEntry(0.0);
		if(entry==null)return Double.NaN;
		return entry.getValue();
	}

	public double find(double initSeed){
		// search between -1 and 1 starting at 0;
		
		double seed = initSeed;
		double lastLow = -1;
		double lastHigh = 1;
		for(int i = 0;i<maxIterations;i++){
			double val = getRoot(seed);
			if(isGood(val))return seed;
			// try lower
			double lowerSeed  = seed - (seed-lastLow)/2;
			double lowval = getRoot(lowerSeed);
			if(isGood(lowval))return lowerSeed;
			
			// try higher
			double higherSeed  = seed + (lastHigh-seed)/2;
			double highval = getRoot(higherSeed);
			if(isGood(highval))return higherSeed;
			if(Math.abs(lowval)<Math.abs(highval)){
				lastHigh = seed;
				seed = lowerSeed;
			}else{
				lastLow = seed;
				seed = higherSeed;
			}
		}
		
		return Double.NaN;
	}

	private boolean isGood(double value){
		if(Double.isInfinite(value) || Double.isNaN(value)){
			return false;
		}
		
		if(Math.abs(value)<=this.precision){
			return true;
		}
		return false;
	}
	
	public double getRoot(double correlationCoefficient) {
		return spreadModel.getSpreadPrice(
				callPut, atmLeg0, atmLeg1, strike, 
				dte, volLeg0, volLeg1, rate, rate,
				divLeg0, divLeg1, 
				correlationCoefficient, other0, other1) - spreadPriceToAchieve;
	}


	public double derivation(double corrInput) {
		double corrRisk  = spreadModel.getSpreadCorrRisk(
				callPut, atmLeg0, atmLeg1, strike, dte, volLeg0, volLeg1,
				rate,rate, divLeg0, divLeg1, corrInput, other0, other1);
//		double defaultVersion = super.derivation(corrInput);
		return corrRisk;
	}




	public double floorvalue(double x) {
		double val = x;
		if(val>1)val=1;
		if(val<-1)val = -1;
		return val;
	}

	
	
	
}
