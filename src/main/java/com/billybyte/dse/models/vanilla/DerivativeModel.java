package com.billybyte.dse.models.vanilla;

import java.util.Arrays;


public abstract class DerivativeModel {
	public enum Sensitivity{
		OPTPRICE,DELTA,GAMMA,VEGA,RHO,THETA,CORRELATION,DAY_DELTA;
	}
	
	public static final Sensitivity[] VANILLA_OPTIONS_SENSTIVITIES = 
		new Sensitivity[]{
			Sensitivity.OPTPRICE,
			Sensitivity.DELTA,
			Sensitivity.GAMMA,
			Sensitivity.VEGA,
			Sensitivity.RHO,
			Sensitivity.THETA,
			Sensitivity.DAY_DELTA
		};

	public static final Sensitivity[] SPREAD_OPTIONS_SENSTIVITIES = 
			new Sensitivity[]{
				Sensitivity.OPTPRICE,
				Sensitivity.DELTA,
				Sensitivity.GAMMA,
				Sensitivity.VEGA,
				Sensitivity.RHO,
				Sensitivity.THETA,
				Sensitivity.DAY_DELTA,
				Sensitivity.CORRELATION
			};


	
	/**
	 * 
	 * @param sensitivity
	 * @param params
	 * @return options sensitivity, like OPTPRICE (1=Put,anything else = Call), 
	 * 			DELTA, GAMMA, VEGA, RHO, THETA, CORRELATION
	 */
	public abstract Number[] getSensitivity(Sensitivity sensitivity, Number[] params);



	/**
	 * 
	 *  Equivalent to GoalSeek for BS Option ISD
	*   Corrado & Miller estimate as starting value
	* Alternatively, use Manaster & Koehler seed value
	*   Uses BSOptionISDEstimate fn
	*   Uses BSOptionValue fn
	*   Uses BSOptionVega fn
	 * @param iopt
	 * @param S
	 * @param X
	 * @param r
	 * @param q
	 * @param tyr
	 * @param optprice
	 * @return
	 */
	public static double impliedVol(
			int iopt,
			Number[] params,
			int indexOfVolParam,
			int indexOfVolReturn,
			double optprice, DerivativeModel model,double volSeed){
	    double atol, sigmanow, fval, fdashval;
	    fval=0.000;
	    atol = 0.0001;
	    sigmanow = volSeed;
	    boolean done=false;
	    int count=0;
	    Number[] localParams = Arrays.copyOf(params,params.length);
	    localParams[indexOfVolParam] = sigmanow;
	    while(!done){
	    	
	        fval = model.getSensitivity(Sensitivity.OPTPRICE, localParams)[0].doubleValue() - optprice;
	        fdashval = model.getSensitivity(Sensitivity.VEGA, localParams)[indexOfVolReturn].doubleValue();
	        sigmanow = sigmanow - (fval / fdashval);
	        if(Math.abs(fval) <= atol){
	        	done=true;
	        }
	        count++;
	        if(count>15){
	        	done=true;
	        }
	    }
	    return sigmanow;
		
	}

	
	
	
	//
	// Lower tail quantile for standard normal distribution function.
	//
	// This function returns an approximation of the inverse cumulative
	// standard normal distribution function.  I.e., given P, it returns
	// an approximation to the X satisfying P = Pr{Z <= X} where Z is a
	// random variable from the standard normal distribution.
	//
	// The algorithm uses a minimax approximation by rational functions
	// and the result has a relative error whose absolute value is less
	// than 1.15e-9.
	//
	// Author:      Peter John Acklam
	// (Javascript version by Alankar Misra @ Digital Sutras (alankar@digitalsutras.com))
	// Time-stamp:  2003-05-05 05:15:14
	// E-mail:      pjacklam@online.no
	// WWW URL:     http://home.online.no/~pjacklam

	// An algorithm with a relative error less than 1.15*10-9 in the entire region.

	public static double NORMSINV(double p)
	{
	    // Coefficients in rational approximations
	    double[] a = new double[]{-3.969683028665376e+01,  2.209460984245205e+02,
	                      -2.759285104469687e+02,  1.383577518672690e+02,
	                      -3.066479806614716e+01,  2.506628277459239e+00};

	    double[] b = new double[]{-5.447609879822406e+01,  1.615858368580409e+02,
	                      -1.556989798598866e+02,  6.680131188771972e+01,
	                      -1.328068155288572e+01 };

	    double[] c = new double[]{-7.784894002430293e-03, -3.223964580411365e-01,
	                      -2.400758277161838e+00, -2.549732539343734e+00,
	                      4.374664141464968e+00,  2.938163982698783e+00};

	    double[] d = new double[] {7.784695709041462e-03, 3.224671290700398e-01,
	                       2.445134137142996e+00,  3.754408661907416e+00};

	    // Define break-points.
	    double plow  = 0.02425;
	    double phigh = 1 - plow;

	    // Rational approximation for lower region:
	    if ( p < plow ) {
	    	double q  = Math.sqrt(-2*Math.log(p));
	             return (((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
	                                             ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
	    }

	    // Rational approximation for upper region:
	    if ( phigh < p ) {
	    	double q  = Math.sqrt(-2*Math.log(1-p));
	             return -(((((c[0]*q+c[1])*q+c[2])*q+c[3])*q+c[4])*q+c[5]) /
	                                                    ((((d[0]*q+d[1])*q+d[2])*q+d[3])*q+1);
	    }

	    // Rational approximation for central region:
	    double q = p - 0.5;
	    double r = q*q;
	    return (((((a[0]*r+a[1])*r+a[2])*r+a[3])*r+a[4])*r+a[5])*q /
	                             (((((b[0]*r+b[1])*r+b[2])*r+b[3])*r+b[4])*r+1);
	}


	/**
		standard normal probability density function.
	 * @param X
	 * @return  (1 / Sqr(2 * Application.Pi())) * Exp(-(X ^ 2 / 2))
	 */
	public static double sProNorm(double X){
		return (1 / Math.sqrt(2 * Math.PI)) * Math.exp(-(Math.pow(X , 2) / 2));
	}

	
	static double Exp(double value){
		return Math.exp(value);
	}
	
	static  double Sqr(double value){
		return Math.sqrt(value);
	}
	
	static double Norm(double value){
		return NormalDistribution.cumulativeDistribution(value);
	}

	
	
	

	public static double d1(double atm, double strike, double dte, double vol,double rate, double div){
		return (Math.log((atm/strike)) + ((rate-div + 0.5 * Math.pow(vol,2)) * dte)) / (vol * Math.sqrt(dte));
	}
	
	public static double d2(double atm, double strike, double dte, double vol,double rate, double div){
		return (d1(atm,strike,dte,vol,rate,div) - vol*Math.sqrt(dte));

	}
	
	static double Log(double value){
		return Math.log(value);
	}


}
