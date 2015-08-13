package com.billybyte.dse.models.vanilla;

import com.billybyte.commonstaticmethods.Utils;

public class BachelierEd {
	static final double AMT_TO_MOVE_PER_VOL =  0.01; // 
	static final double AMT_TO_VOL =  0.01; // 
	static final double AMT_TO_INT =  0.01; // 
	static final double minDteTime = 10/(24*60*60);// 10 seconds
	
	public static double optPrice(
			double callPut, 
			double EDFuture,  
			double EDStrike, 
			double timeToExpiry,
			double sigma, 
			double riskFreeRate, 
			double div){
		if(callPut==1){
			return putPrice(EDFuture,EDStrike,sigma,riskFreeRate,timeToExpiry);
		}
		return callPrice(EDFuture,EDStrike,sigma,riskFreeRate,timeToExpiry);
	}
	
	
	// call and put are reverese for ED's
	public static Double callPrice(
			double EDFuture,
			double EDStrike,
			double sigma,
			double riskFreeRate,
			double timeToExpiry){
		
		//EXP(-r_*T_)*((K_-F_)*N_d1+sigma*SQRT(T_)*probDensity(d1))
		// d1 = (F_-K_)/(sigma*SQRT(T_))
		double K = 100-EDStrike;
		double F = 100-EDFuture;
		double d1 = (F-K)/(sigma*Math.sqrt(timeToExpiry));
		double N_d1 = NormalDistribution.cumulativeDistribution(-d1);
		double probDensity = NormalDistribution.probabilityDensity(d1);
		double call = Math.exp(-riskFreeRate*timeToExpiry)*(  (K-F)*N_d1  +  sigma*Math.sqrt(timeToExpiry)*probDensity  );
		
		return call;
	}

	public static Double putPrice(
			double EDFuture,
			double EDStrike,
			double sigma,
			double riskFreeRate,
			double timeToExpiry){
		
		//EXP(-r_*T_)*((F_-C_)*Nd1+sigma*SQRT(T_)*_nd1)
		// d1 = (F_-K_)/(sigma*SQRT(T_))
		double K = 100-EDStrike;
		double F = 100-EDFuture;
		double d1 = (F-K)/(sigma*Math.sqrt(timeToExpiry));
		double Nd1 = NormalDistribution.cumulativeDistribution(d1);
		double probDensity = NormalDistribution.probabilityDensity(d1);
		double put = Math.exp(-riskFreeRate*timeToExpiry)*(  (F-K)*Nd1  +  sigma*Math.sqrt(timeToExpiry)*probDensity  );
		
		return put;
	}
	
	
	
	public static final double delta(
			double callPut,double atm, 
			double strike, double dte,double vol,double rate,
			 double div){
		double amtToMove = atm * AMT_TO_MOVE_PER_VOL*vol;
		double pInit = optPrice(callPut, atm,  strike, dte,vol, rate, div);
		double atmUp = atm + amtToMove;
		double pUp = optPrice(callPut, atmUp, strike, dte,vol, rate, div);
		double atmDown = atm * (1-AMT_TO_MOVE_PER_VOL*vol);
		double pDown = optPrice(callPut, atmDown,  strike, dte,vol, rate, div);
		double delta = (((pUp-pInit)/amtToMove) + ((pInit-pDown)/amtToMove))/2;

		return delta;
	}

	public static final double gamma(
			double callPut,double atm, 
			double strike, double dte,double vol,double rate,
			 double div){
		double amtToMove = atm * AMT_TO_MOVE_PER_VOL*vol;
		double deltaInit = delta(callPut, atm,  strike, dte,vol, rate, div);
		double atmUp = atm + amtToMove;
		double deltaUp = delta(callPut, atmUp, strike, dte,vol, rate, div);
		double atmDown = atm * (1-AMT_TO_MOVE_PER_VOL*vol);
		double deltaDown = delta(callPut, atmDown,  strike, dte,vol, rate, div);
		double gamma = (((deltaUp-deltaInit) + (deltaInit-deltaDown))/2)/amtToMove;

		return gamma;
	}
	
	public static final double vega(
			double callPut,double atm, 
			double strike, double dte,double vol,double rate,
			 double div){
		double priceInit = optPrice(callPut, atm,  strike, dte,vol, rate, div);
		double volUp = vol + AMT_TO_VOL;
		double priceUpVol = optPrice(callPut, atm, strike, dte,volUp, rate, div);
		double volDown = vol - AMT_TO_VOL;
		double priceDownVol = optPrice(callPut, atm,  strike, dte,volDown, rate, div);
		double vega = ((priceUpVol-priceInit) + (priceInit-priceDownVol))/2;

		return vega;
	}
	
	public static final double theta(double callPut,double atm, 
			double strike, double dte,double vol,double rate,
			 double div){
		double optNow = optPrice(callPut, atm, strike, dte, vol, rate, div);
		double oneDay = 1.0/365.0;
		double newDte = dte-oneDay;
		if(newDte<minDteTime)newDte=minDteTime;
		double optOneDayLater = optPrice(callPut, atm, strike, newDte, vol, rate, div);
		double diff = optOneDayLater-optNow;
		return diff;

	}

	public static final double rho(double callPut,double atm, 
			double strike, double dte,double vol,double rate,
			 double div){
		double optNow = optPrice(callPut, atm, strike, dte, vol, rate, div);
		double optRateOnePercHigher = optPrice(callPut, atm, strike, dte, vol, rate+0.01, div);
		double diff = optRateOnePercHigher-optNow;
		return diff;

	}
	
	
	
	public static void main(String[] args) {
		double EDSpot = 99.47;
		double EDStrike = 99.50;
		double sigma = .34;
		double r = .0045;
		double days = 125;
		double T = days/365;
		/**
		 * c	0.065177565
		 * p	0.0951313677
		 */
		double call = callPrice(EDSpot,EDStrike,sigma,r,T);
		Utils.prt("call = price:" + call + " delta:"+delta(0,EDSpot,EDStrike,T,sigma,r,0) + " vega:"+vega(0,EDSpot,EDStrike,T,sigma,r,0));
		double put = putPrice(EDSpot,EDStrike,sigma,r,T);
		Utils.prt("put = " + put + " delta:"+delta(1,EDSpot,EDStrike,T,sigma,r,0) + " vega:"+vega(1,EDSpot,EDStrike,T,sigma,r,0));
	}

	

}
