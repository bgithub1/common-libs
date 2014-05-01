package com.billybyte.dse.models.vanilla;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.outputs.DayDeltaDerSen;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.RhoDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;

public class VanOptBawAmerican extends VanOptAbst {
//	private final BawAmerican model = 
//			new BawAmerican();
	
	public VanOptBawAmerican(Calendar evaluationDate,
			DioType<BigDecimal> vsDiot, DioType<?>[] others) {
		super(evaluationDate, vsDiot, others);
	}

	public VanOptBawAmerican(Calendar evaluationDate,
			DioType<BigDecimal> vsDiot) {
		super(evaluationDate, vsDiot);
	}

	@Override
	public Set<DerivativeSensitivityTypeInterface> getSensitivitesSupported() {
		Set<DerivativeSensitivityTypeInterface> senseSet = 
				new HashSet<DerivativeSensitivityTypeInterface>();
		senseSet.add(new OptPriceDerSen());
		senseSet.add(new DeltaDerSen());
		senseSet.add(new GammaDerSen());
		senseSet.add(new ThetaDerSen());
		senseSet.add(new VegaDerSen());
		senseSet.add(new DayDeltaDerSen());
		senseSet.add(new RhoDerSen());
		return senseSet;
	}

	@Override
	public double getVanillaPrice(double callput, double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others) {
		double ret = 
				BawAmerican.optPrice(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	@Override
	public double getVanillaDelta(double callput, double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others) {
		double ret = 
				BawAmerican.delta(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	@Override
	public double getVanillaGamma(double callput, double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others) {
		double ret = 
				BawAmerican.gamma(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	@Override
	public double getVanillaVega(double callput, double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others) {
		double ret = 
				BawAmerican.vega(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	@Override
	public double getVanillaTheta(double callput, double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others) {
		double ret = 
				BawAmerican.theta(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	@Override
	public double getVanillaRho(double callput, double atm, double strike,
			double dte, double vol, double rate, double div, Object[] others) {
		double ret = 
				BawAmerican.rho(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	@Override
	public double getVanillaDayDelta(double callput, double atm,
			double strike, double dte, double vol, double rate, double div,
			Object[] others) {
		double dteOneDayForward = dte - 1/365;
		if(dteOneDayForward<0.0001){
			dteOneDayForward = 0.0001;
		}
		double ret = 
				BawAmerican.delta(callput, atm, strike, dte, vol, rate, div);
		ret = ret - 
				BawAmerican.delta(callput, atm, strike, dteOneDayForward, vol, rate, div);
		return ret;

	}

	@Override
	public double getVanillaCustomSensitivity(
			DerivativeSensitivityTypeInterface customSensitivityType,
			double callput, double atm, double strike, double dte, double vol,
			double rate, double div, Object[] others) {
		throw Utils.IllState(this.getClass(),"not supporting custom sensitivity: "+customSensitivityType.toString());
//		return 0;
	}

}
