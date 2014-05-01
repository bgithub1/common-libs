package com.billybyte.dse.models.vanilla;

import java.util.Arrays;


/**
 * 
 * @author bperlman1
 *
 */
public class BlackEuropean extends DerivativeModel {
	@Override
	/**
	 * european black sholes
	 */
	public Number[] getSensitivity(Sensitivity sensitivity, Number[] params) {
	
		Double[] ret ;
		switch(sensitivity){
		case DELTA:
			ret = new Double[1];
			double delta = optDelta(
					params[0].doubleValue(), // callput (1=put)
					params[1].doubleValue(),// underling
					params[2].doubleValue(),// strike
					params[3].doubleValue(), // dte
					params[4].doubleValue(), // vol
					params[5].doubleValue(), // rate
					params[6].doubleValue()); // div)
			ret[0] = delta;
			return ret;
		case GAMMA:
			ret = new Double[1];
			double gamma = optGamma(
					params[0].doubleValue(), // callput (1=put)
					params[1].doubleValue(),// underling
					params[2].doubleValue(),// strike
					params[3].doubleValue(), // dte
					params[4].doubleValue(), // vol
					params[5].doubleValue(), // rate
					params[6].doubleValue()); // div)
			ret[0] = gamma;
			return ret;
		case THETA:
			ret = new Double[1];
			double optNow = 
				getSensitivity(Sensitivity.OPTPRICE, params)[0].doubleValue();
			double oneDay = 1.0/365.0;
			Double oneDayLaterDte = params[3].doubleValue()-oneDay;
			Number[] newParams = Arrays.copyOf(params, params.length);
			newParams[3] = oneDayLaterDte>0?oneDayLaterDte:0.00001;
			double optOneDayLater = 
					getSensitivity(Sensitivity.OPTPRICE, newParams)[0].doubleValue();
			double diff = optNow-optOneDayLater;
			ret[0] = new Double(diff);
			return ret;
			
		case RHO:
			ret = new Double[1];
			double optI = 
				getSensitivity(Sensitivity.OPTPRICE, params)[0].doubleValue();
			Number[] newParamsWithNewI = Arrays.copyOf(params, params.length);
			newParamsWithNewI[5] = params[5].doubleValue() + 0.01;
			double optOnePercHigherI = 
					getSensitivity(Sensitivity.OPTPRICE, newParamsWithNewI)[0].doubleValue();
			ret[0] = new Double(optOnePercHigherI-optI);
			return ret;
			
		case VEGA:
			ret = new Double[1];
			double vega = optVega(
					params[0].doubleValue(), // callput (1=put)
					params[1].doubleValue(),// underling
					params[2].doubleValue(),// strike
					params[3].doubleValue(), // dte
					params[4].doubleValue(), // vol
					params[5].doubleValue(), // rate
					params[6].doubleValue()); // div)
			ret[0] = vega;
			return ret;
		case OPTPRICE:
			ret = new Double[1];
			double opt = optPrice(
					params[0].doubleValue(), // callput (1=put)
					params[1].doubleValue(),// underling
					params[2].doubleValue(),// strike
					params[3].doubleValue(), // dte
					params[4].doubleValue(), // vol
					params[5].doubleValue(), // rate
					params[6].doubleValue()); // div)
			ret[0] = opt;
			return ret;
		}
		
		
		return null;
	}
	
	public static double optPrice(double callPut,double atm, double strike,double dte,double vol, double rate, double div){
			if(callPut==1){
				// get put
				return putPrice(atm, strike, dte, vol, rate, div);
			}else{
				return callPrice(atm, strike, dte, vol, rate, div);
			}
	}
	
	
	
	/**
	 * 
	 * @param atm - underlying price
	 * @param strike - strike
	 * @param dte - expiry time as percent of year (e.g. 365 = 100% or 1.0)
	 * @param vol - annualized vol (e.g 0.32)
	 * @param rate - 
	 * @return
	 */
	public static double callPrice(double atm, double strike,double dte,double vol, double rate, double div){
		return atm * Math.exp(-div * dte) * NormalDistribution.cumulativeDistribution(d1(atm,strike,dte,vol,rate,div)) - strike * Math.exp(-rate * dte) * NormalDistribution.cumulativeDistribution(d2(atm,strike,dte,vol,rate,div));
			
	}
	
	public static double putPrice(double atm, double strike,double dte,double vol, double rate,double div){
//		return  getStrike() * Math.exp(-getRate() * getDte()) * sCumNorm(-getD2()) - getATM() * sCumNorm(-getD1());
//                         6.192047682664017E-9    0.7324073490807343                              3.1242797238206776E-10
		return  strike * Math.exp(-rate * dte) * NormalDistribution.cumulativeDistribution(-d2(atm,strike,dte,vol,rate,div)) - atm * Math.exp(-div * dte) * NormalDistribution.cumulativeDistribution(-d1(atm,strike,dte,vol,rate,div));
	}
	
	
	public double optDelta(double callput, double atm, double strike,
			double dte, double vol, double rate, double div) {
		// checks to see if contract is in expiry day and if so adjusts the dte from 0 to 0.001
		if(dte==0)dte=0.001;
		double d1 = d1(atm,strike,dte,vol,rate,div);
		if(callput!=1.0){
			return NormalDistribution.cumulativeDistribution(d1);
		}else{
			return NormalDistribution.cumulativeDistribution(d1)-1;
		}
	}

	public double optGamma(double callput, double atm, double strike,
			double dte, double vol, double rate, double div) {
		// checks to see if contract is in expiry day and if so adjusts the dte from 0 to 0.001
		if(dte==0)dte=0.001;
		double d1 = d1(atm,strike,dte,vol,rate,div);
		double ret = sProNorm(d1)/(atm*vol*Math.sqrt(dte));
		return ret;
		
	}

	public double optVega(double callput, double atm, double strike,
			double dte, double vol, double rate, double div) {
		if(dte==0)dte=0.001;
		return atm * sProNorm(d1(atm,strike,dte,vol,rate,div)) * Math.sqrt(dte) *.01;
	}
	
	private static final double ONEDAYDOUBLE = 1.0/365.0;
	private static final double NODAYS = 0.00001;
	public double optTheta(double callput, double atm, double strike,
			double dte, double vol, double rate, double div){
		double ret;
		if(dte<=0)return 0.0;
		if(dte<ONEDAYDOUBLE){
			ret = optPrice(callput, atm, strike, NODAYS, vol, rate, div) -
					optPrice(callput, atm, strike, dte, vol, rate, div);
		}else{
			ret = optPrice(callput, atm, strike, dte-ONEDAYDOUBLE, vol, rate, div) -
					optPrice(callput, atm, strike, dte, vol, rate, div);
		}
		return ret;
	}
		
	public double optRho(double callput, double atm, double strike,
			double dte, double vol, double rate, double div){
		double ret;
		if(rate<=0)return 0.0;
		ret = optPrice(callput, atm, strike, dte, vol, rate+0.01, div) -
				optPrice(callput, atm, strike, dte, vol, rate, div);
		return ret;
	}

	
	public static double BSOptionISDEstimate(
			int iopt,
			double S,
			double X,
			double r,
			double q,
			double tyr,
			double optprice){
		double BSOptionISDEstimate;
		// Estimates Corrado & Miller ISD from BS option price
		double Sq, pvX, p, calc0, calc1, calc2;
		 Sq = S * Math.exp(-q * tyr);
		 pvX = X * Math.exp(-r * tyr);
		 p = Math.PI;
		 calc0 = optprice - iopt * 0.5 * (Sq - pvX);
		 calc1 = Math.pow(calc0,2) - Math.pow((Sq - pvX), 2) / p;
		 if(calc1 < 0 ){
			 BSOptionISDEstimate=-1;
		 }else{
		        calc2 = calc0 + Math.sqrt(calc1);
		        BSOptionISDEstimate = Math.sqrt(2 * p / tyr) * calc2 / (Sq + pvX);
		 }
		 return BSOptionISDEstimate;
	}
	
	
	
}
