package com.billybyte.dse.models.spread;

import com.billybyte.dse.models.vanilla.NormalDistribution;



/**
 * Spread Options Model
 * @author bperlman1
 *
 */
public class KirkSpreadApproximation /*  extends DerivativeModel */ {
	static final double AMT_TO_MOVE_PER_VOL =  0.01; // 
	static final double AMT_TO_VOL =  0.01; // 
	static final double AMT_TO_INT =  0.01; // 
	static final double AMT_TO_MOVE_CORRELATION = .001;
	
	
//	@Override
//	public Number[] getSensitivity(Sensitivity sensitivity, Number[] params) {
//		Double[] ret ;
//		switch(sensitivity){
//		case DELTA:
//			ret = new Double[2];
//			double[] dRet = delta(
//					params[0].doubleValue(), // callput
//					params[1].doubleValue(), // F_1
//					params[2].doubleValue(), // F_2
//					params[3].doubleValue(), // X
//					params[4].doubleValue(), // V_1
//					params[5].doubleValue(), // V_2
//					params[6].doubleValue(), // I
//					params[7].doubleValue(), // T
//					params[8].doubleValue()); // Corr
//			ret[0] = dRet[0];
//			ret[1] = dRet[1];
//			return ret;
//		case GAMMA:
//			ret = new Double[2];
//			double[] gRet = gamma(
//					params[0].doubleValue(), // callput
//					params[1].doubleValue(), // F_1
//					params[2].doubleValue(), // F_2
//					params[3].doubleValue(), // X
//					params[4].doubleValue(), // V_1
//					params[5].doubleValue(), // V_2
//					params[6].doubleValue(), // I
//					params[7].doubleValue(), // T
//					params[8].doubleValue()); // Corr
//			ret[0] = gRet[0];
//			ret[1] = gRet[1];
//			return ret;
//		case THETA:
//			ret = new Double[1];
//			ret[0] = new Double(theta(
//					params[0].doubleValue(), // callput
//					params[1].doubleValue(), // F_1
//					params[2].doubleValue(), // F_2
//					params[3].doubleValue(), // X
//					params[4].doubleValue(), // V_1
//					params[5].doubleValue(), // V_2
//					params[6].doubleValue(), // I
//					params[7].doubleValue(), // T
//					params[8].doubleValue())); // Corr
//			return ret;
//		case RHO:
//			ret = new Double[1];
//			ret[0] = new Double(rho(
//					params[0].doubleValue(), // callput
//					params[1].doubleValue(), // F_1
//					params[2].doubleValue(), // F_2
//					params[3].doubleValue(), // X
//					params[4].doubleValue(), // V_1
//					params[5].doubleValue(), // V_2
//					params[6].doubleValue(), // I
//					params[7].doubleValue(), // T
//					params[8].doubleValue())); // Corr
//			return ret;
//		case VEGA:
//			ret = new Double[2];
//			double[] vRet = gamma(
//					params[0].doubleValue(), // callput
//					params[1].doubleValue(), // F_1
//					params[2].doubleValue(), // F_2
//					params[3].doubleValue(), // X
//					params[4].doubleValue(), // V_1
//					params[5].doubleValue(), // V_2
//					params[6].doubleValue(), // I
//					params[7].doubleValue(), // T
//					params[8].doubleValue()); // Corr
//			ret[0] = vRet[0];
//			ret[1] = vRet[1];
//			return ret;
//		case OPTPRICE:
//			ret = new Double[1];
//			ret[0] = new Double(optPrice(
//					params[0].doubleValue(), // callput
//					params[1].doubleValue(), // F_1
//					params[2].doubleValue(), // F_2
//					params[3].doubleValue(), // X
//					params[4].doubleValue(), // V_1
//					params[5].doubleValue(), // V_2
//					params[6].doubleValue(), // I
//					params[7].doubleValue(), // T
//					params[8].doubleValue())); // Corr
//			return ret;
//		}
//		return null;
//	}
	
/**
Fover	F_2P/(F_2P+X)
 Va	SQRT(V_1^2 + (V_2 * Fover)^2 - 2*Corr * V_1 * V_2 *Fover)
dt	Va * SQRT(T)
F 	F_1/ (F_2P+X)
d1	(LN(F) + (0.5*Va^2) * T)/dt
d2	_d1-dt
Nd1	NORMSDIST(_d1)
Nd2	NORMSDIST(_d2)
Nnd1	NORMSDIST(-_d1)
Nnd2	NORMSDIST(-_d2)
call	(F_2P+X)*((EXP(-I*T)*(F*_Nd1-_Nd2)))
put	(F_2P+X)*((EXP(-I*T)*(F*_Nd1-_Nd2)))

 * 	
 */

	/**
	 * 
	 * @param callPut - either 1 for put or anything else for call
	 * @param F_1 Futures Price 1
	 * @param F_2 Futures Price 2
	 * @param X Strike
	 * @param V_1 Volatility of F1
	 * @param V_2 Volatility of F2
	 * @param I interest rate
	 * @param T time as % of year (one day = 1/365)
	 * @param Corr - Pearson's coefficient, between 0 and 1
	 * @return price of call or put
 Fover = F_2 / (F_2 + X)
 ' Approximated volatility
 Va = Sqr(V_1 ^ 2 + (V_2 * Fover) ^ 2 - 2 * Corr * V_1 * V_2 * Fover)
 
 dt = Va * Sqr(T)
 F = F_1 / (F_2 + X)
 
 d1 = (Log(F) + (0.5 * Va ^ 2) * T) / dt
 d2 = d1 - dt
 Nd1 = SNorm(d1)
 Nd2 = SNorm(d2)
 NNd1 = SNorm(-d1)
 NNd2 = SNorm(-d2)
 
 If OptionType = 1 Then
 SpreadFuturesApprox = (F_2 + X) * ((Exp(-r * T) * (F * Nd1 - Nd2)))
 Else
 SpreadFuturesApprox = (F_2 + X) * ((Exp(-r * T) * (NNd2 - F * NNd1)))
 End If
 

	 * 
	 */
	public static final double optPrice(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double Corr){
		double Fover =	F_2/(F_2+X);
		double Va=	Math.sqrt(Math.pow(V_1,2) + Math.pow(V_2 * Fover,2) - 2*Corr * V_1 * V_2 *Fover);
		double dt =	Va * Math.sqrt(T);
		double F =	F_1/ (F_2+X);
		
		double _d1=(Math.log(F) + (0.5*Math.pow(Va,2)) * T)/dt;
		double _d2	=_d1-dt;
		if(callPut!=1){
			if(Double.isNaN(_d1) || Double.isNaN(_d2)){
				double diff = F_1 - F_2;
				if(diff>X){
					return diff-X;
				}
				return 0;
			}
			double _Nd1 = NormalDistribution.cumulativeDistribution(_d1);//	OptionsModel.Norm(_d1);
			double _Nd2	= NormalDistribution.cumulativeDistribution(_d2);// OptionsModel.Norm(_d2);
			double call =	(F_2+X)*((Math.exp(-I*T)*(F*_Nd1-_Nd2)));
			return call;
		}
		if(Double.isNaN(_d1) || Double.isNaN(_d2)){
			double diff = F_1 - F_2;
			if(diff<X){
				return X-diff;
			}
			return 0;
		}
		double _Nnd1 = NormalDistribution.cumulativeDistribution(-_d1);//	OptionsModel.Norm(-_d1);
		double _Nnd2 =	NormalDistribution.cumulativeDistribution(-_d2);
		double put = (F_2+X)*((Math.exp(-I*T)*(_Nnd2-F*_Nnd1)));
		return put;
	}

	public static final double[] delta(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double Corr){
		// get delta with respect to F_1
		double[] ret = new double[2];  // return delta with respect to F_1 and F_2
		double amtToMoveF1 = F_1 * AMT_TO_MOVE_PER_VOL*V_1;
		double pInit = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, Corr);
		double F_1_Up = F_1 + amtToMoveF1;
		double p1Up = optPrice(callPut, F_1_Up, F_2, X, V_1, V_2, I, T, Corr);
		double F_1_Down = F_1 * (1-AMT_TO_MOVE_PER_VOL*V_1);
		double p1Down = optPrice(callPut, F_1_Down, F_2, X, V_1, V_2, I, T, Corr);
		double delta_F1 = (((p1Up-pInit)/amtToMoveF1) + ((pInit-p1Down)/amtToMoveF1))/2;
		ret[0] = delta_F1;

		double amtToMoveF2 = F_2 * AMT_TO_MOVE_PER_VOL*V_2;
		pInit = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, Corr);
		double F_2_Up = F_2 + amtToMoveF2;
		double p2Up = optPrice(callPut, F_1, F_2_Up, X, V_1, V_2, I, T, Corr);
		double F_2_Down = F_2 * (1-AMT_TO_MOVE_PER_VOL*V_2);
		double p2Down = optPrice(callPut, F_1, F_2_Down, X, V_1, V_2, I, T, Corr);
		double delta_F2 = (((p2Up-pInit)/amtToMoveF2) + ((pInit-p2Down)/amtToMoveF2))/2;
		ret[1] = delta_F2;
		return ret;
	}

	public static final double[] gamma(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double Corr){
		double[] ret = new double[2];  // return gamma with respect to F_1 and F_2
		// get initial deltas with respect to F_1 and F_2
		double[] deltaInit = delta(callPut, F_1, F_2, X, V_1, V_2, I, T, Corr);
		// get the change in deltas if you change F_1 up and down
		double amtToMoveF1 = F_1 * AMT_TO_MOVE_PER_VOL*V_1;
		double F_1_Up = F_1 + amtToMoveF1;
		double[] deltaF1Up = delta(callPut, F_1_Up, F_2, X, V_1, V_2, I, T, Corr);
		double F_1_Down = F_1 * (1-AMT_TO_MOVE_PER_VOL*V_1);
		double[] deltaF1Down = delta(callPut, F_1_Down, F_2, X, V_1, V_2, I, T, Corr);
		// use the delta change with respect to F_1 as the gamma with respect to F_1
		ret[0] = ((deltaF1Up[0]-deltaInit[0]) + (deltaInit[0]-deltaF1Down[0])) / 2;

		// get the change in deltas if you change F_2 up and down
		double amtToMoveF2 = F_2 * AMT_TO_MOVE_PER_VOL*V_2;
		double F_2_Up = F_2 + amtToMoveF2;
		double[] deltaF2Up = delta(callPut, F_1, F_2_Up, X, V_1, V_2, I, T, Corr);

		double F_2_Down = F_2 * (1-AMT_TO_MOVE_PER_VOL*V_2);
		double[] deltaF2Down = delta(callPut, F_1, F_2_Down, X, V_1, V_2, I, T, Corr);
		ret[1] = ((deltaF2Up[1]-deltaInit[1]) + (deltaInit[1]-deltaF2Down[1])) / 2;
		return ret;
	}

	public static final double[] vega(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double Corr){
		// get delta with respect to F_1
		double[] ret = new double[2];  // return delta with respect to F_1 and F_2
		double pInit = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, Corr);
		double V_1_Up = V_1 + AMT_TO_VOL;
		double p1Up = optPrice(callPut, F_1, F_2, X, V_1_Up, V_2, I, T, Corr);
		double V_1_Down = V_1 - AMT_TO_VOL;
		double p1Down = optPrice(callPut, F_1, F_2, X, V_1_Down, V_2, I, T, Corr);
		double vega_V1 = ((p1Up-pInit) + (pInit-p1Down))/2;
		ret[0] = vega_V1;

		double V_2_Up = V_2 + AMT_TO_VOL;
		double p2Up = optPrice(callPut, F_1, F_2, X, V_1, V_2_Up, I, T, Corr);
		double V_2_Down = V_2 - AMT_TO_VOL;
		double p2Down = optPrice(callPut, F_1, F_2, X, V_1, V_2_Down, I, T, Corr);
		double vega_V2 = ((p2Up-pInit) + (pInit-p2Down))/2;
		ret[1] = vega_V2;
		return ret;
	}



	public static final double rho(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double Corr){
		// get delta with respect to F_1
		double pInit = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, Corr);
		double IUp = I + AMT_TO_INT;
		double pUp = optPrice(callPut, F_1, F_2, X, V_1, V_2, IUp, T, Corr);
		double IDown = I - AMT_TO_INT;
		double pDown = optPrice(callPut, F_1, F_2, X, V_1, V_2, IDown, T, Corr);
		double rho = ((pUp-pInit) + (pInit-pDown))/2;

		return rho;
	}

	public static final double theta(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double Corr){
		// get delta with respect to F_1
		double pInit = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, Corr);
		if(T<=0){
			return pInit;
		}
		double TDown =  T - 1.0/365;
		double pDown = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, TDown, Corr);
		return pDown-pInit;
	}
	
	public static final double corrRisk(
			double callPut,double F_1, double F_2, 
			double X, double V_1, double V_2,
			double I, double T,double corrInput){
		// get delta with respect to F_1
		// if correlation is 1.0 just do down move
		double corrCoef =corrInput;
		if(corrCoef>=1.0){
			corrCoef = 1.0 - AMT_TO_MOVE_CORRELATION;
		}else{
			if(corrCoef<=-1.0){
				corrCoef = -1.0+AMT_TO_MOVE_CORRELATION;
			}
		}
		double pInit = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, corrCoef);
		double corrUp =  corrCoef+AMT_TO_MOVE_CORRELATION;
		double pCorrUp = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, corrUp);
		double corrDown =  corrCoef-AMT_TO_MOVE_CORRELATION;
		double pCorrDown = optPrice(callPut, F_1, F_2, X, V_1, V_2, I, T, corrDown);
		return ((pCorrUp-pInit)+(pInit-pCorrDown))/(2*AMT_TO_MOVE_CORRELATION);
	}
	
}
