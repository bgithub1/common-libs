package com.billybyte.dse.models.spread.newtonraphsonforspreads;

import com.billybyte.dse.models.spread.CsoModel;


public  class NewtonRaphsonImpliedCorrelation extends NewtonRaphson{
	private final CsoModel spreadModel;
	private final double  callPut, atmLeg0,  atmLeg1, strike;
	private final double dte,  volLeg0,  volLeg1,  rate;
	private final double divLeg0,  divLeg1;
	private final Object other0,  other1;
	private final double spreadPriceToAchieve;
	
	
	
	public NewtonRaphsonImpliedCorrelation(
			double spreadPriceToAchieve,
			double precision, 
			int maxInterations,
			CsoModel spreadModel,
			double callPut, double atmLeg0, double atmLeg1, double strike,
			double dte, double volLeg0, double volLeg1, double rate,
			double divLeg0, double divLeg1, Object other0, Object other1) {
		super();
		this.accuracy(precision, maxInterations);

		this.spreadPriceToAchieve = spreadPriceToAchieve;
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

	


	@Override
	public double newtonroot(double correlationCoefficient) {
		return spreadModel.getSpreadPrice(
				callPut, atmLeg0, atmLeg1, strike, 
				dte, volLeg0, volLeg1, rate, rate,
				divLeg0, divLeg1, 
				correlationCoefficient, other0, other1) - spreadPriceToAchieve;
	}


	@Override
	public double derivation(double corrInput) {
		double corrRisk  = spreadModel.getSpreadCorrRisk(
				callPut, atmLeg0, atmLeg1, strike, dte, volLeg0, volLeg1,
				rate,rate, divLeg0, divLeg1, corrInput, other0, other1);

		return corrRisk;
	}




	@Override
	public double floorvalue(double x) {
		double val = super.floorvalue(x);
		if(val>1)val=1;
		if(val<-1)val = -1;
		return val;
	}

	
	
	
}
