package com.billybyte.dse.models.spread;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;


public class CsoModel extends SpreadMultiAbstract{

	public CsoModel() {
		super(Calendar.getInstance());
	}
	public CsoModel(Calendar calendar) {
		super(calendar);
	}

	@Override
	public Double getSpreadPrice(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		return KirkSpreadApproximation.optPrice(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, rate0, dte, corr);
	}

	@Override
	public Double[] getSpreadDelta(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		double[] values = KirkSpreadApproximation.delta(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, rate0, dte, corr);
		return new Double[]{values[0],values[1]};
	}

	@Override
	public Double[] getSpreadGamma(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		double[] values = KirkSpreadApproximation.gamma(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, rate0, dte, corr);
		return new Double[]{values[0],values[1]};
	}

	@Override
	public Double[] getSpreadVega(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		double[] values = KirkSpreadApproximation.vega(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, rate0, dte, corr);
		return new Double[]{values[0],values[1]};
	}

	@Override
	public Double getSpreadTheta(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		return KirkSpreadApproximation.theta(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, rate0, dte, corr);
	}

	@Override
	public Double getSpreadRho(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		return KirkSpreadApproximation.rho(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, rate0, dte, corr);
	}

	@Override
	public Double[] getSpreadDayDel(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr,Object other0, Object other1) {
		Double[] delNow = getSpreadDelta(callPut, atmLeg0, atmLeg1, strike, dte, volLeg0, volLeg1, 
					rate0,rate1, divLeg0, divLeg1, corr, other0, other1);
		double dteOneDayLater = dte-ONEDAYLATER;
		if(dteOneDayLater<=0){
			return delNow;
		}
		Double[] delOneDayLater = getSpreadDelta(callPut, atmLeg0, atmLeg1, strike, dteOneDayLater, volLeg0, volLeg1, 
					rate0,rate1, divLeg0, divLeg1, corr, other0, other1);
		Double[] ret = new Double[]{
				delNow[0]-delOneDayLater[0],
				delNow[1]-delOneDayLater[1]};
		return ret;
	}

	@Override
	public Double getSpreadCorrRisk(double callPut, double atmLeg0,
			double atmLeg1, double strike, double dte, double volLeg0,
			double volLeg1, double rate0, double rate1, 
			double divLeg0, double divLeg1,
			double corr, Object other0, Object other1) {
		return KirkSpreadApproximation.corrRisk(callPut, atmLeg0, atmLeg1, strike, volLeg0, volLeg1, 
				rate0, dte, corr);
	}

	@Override
	public Set<DerivativeSensitivityTypeInterface> getSensitivitesSupported() {
		Set<DerivativeSensitivityTypeInterface> senseSet = new HashSet<DerivativeSensitivityTypeInterface>();
		senseSet.add(new OptPriceDerSen());
		senseSet.add(new DeltaDerSen());
		senseSet.add(new GammaDerSen());
		senseSet.add(new ThetaDerSen());
		senseSet.add(new VegaDerSen());
		return senseSet;
	}

	
}
