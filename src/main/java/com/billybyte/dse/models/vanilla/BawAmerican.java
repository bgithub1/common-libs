package com.billybyte.dse.models.vanilla;

import java.util.Arrays;


public class BawAmerican extends DerivativeModel{
	static final double AMT_TO_MOVE_PER_VOL =  0.01; // 
	static final double AMT_TO_VOL =  0.01; // 
	static final double AMT_TO_INT =  0.01; // 
	static final double minDteTime = 10/(24*60*60);// 10 seconds
	@Override
	public Number[] getSensitivity(Sensitivity sensitivity, Number[] params) {
		Double[] ret ;
		switch(sensitivity){
		case DELTA:
			ret = new Double[1];
			double delta = delta(
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
			double gamma = gamma(
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
			double vega = vega(
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
	 * general Barone-Adesi Whaley American Call model
	 * For futures, let r = D
	 * @param atm
	 * @param strike
	 * @param dte
	 * @param rate
	 * @param vol
	 * @return
	 */
	 public static Double callPrice(double atm,double strike, double dte,double vol,double rate, double div ){
		 double dt;
	     double A1 , a2 , b , a; 
	     double h , L1 , L2; 
	     double d1 , Sc; 
		 double BWAmerican_Call;
		 double D = div;
	     b = 2 * (rate - D) / Math.pow(vol , 2) - 1;
	     a = 2 * rate /Math.pow(vol , 2);
	     h = 1 - Math.exp(-rate * dte);
	     L1 = (-b - Math.sqrt(Math.pow(b , 2) + 4 * a / h)) / 2.0;
	     L2 = (-b + Math.sqrt(Math.pow(b , 2) + 4 * a / h)) / 2.0;
	     dt = vol * Math.sqrt(dte);
	    	           
	     if( D == 0)return  BlackEuropean.callPrice(atm, strike, dte, rate, vol,D);
         Sc = Bisectional_call(atm, strike, dte, rate, D, vol);
         d1 = (log(Sc / strike) + (rate - D + 0.5 * pow(vol , 2)) * dte) / dt;
         a2 = (1 - exp(-D * dte) * DerivativeModel.Norm(d1)) * (Sc / L2);
         if (atm < Sc){
             BWAmerican_Call = BlackEuropean.callPrice(atm, strike, dte,vol,rate,  D) + a2 * pow((atm / Sc) , L2);
         }else{
             BWAmerican_Call = atm - strike;
         }
         if(BWAmerican_Call < atm - strike) {
             BWAmerican_Call = atm - strike;
         }
         
         return BWAmerican_Call;
	 }

	 
	 public static Double putPrice(double atm,double strike, double dte,double vol,double rate,double div ){
		 double dt;
	     double A1 ,  b , a; 
	     double h , L1 ; 
	     double d1 , Sp; 
		 double BWAmerican_Put;
		 double D = div;
	     b = 2 * (rate - D) / pow(vol , 2) - 1;
	     a = 2 * rate /pow(vol , 2);
	     h = 1 - exp(-rate * dte);
	     L1 = (-b - sqrt(pow(b , 2) + 4 * a / h)) / 2.0;
	     dt = vol * sqrt(dte);
	     
	     Sp = Bisectional_put(atm, strike, dte, rate, D, vol);
	     d1 = (log(Sp / strike) + (rate - D + pow(vol , 2) * 0.5) * dte) / dt;
	     
	     
	     A1 = -(1 - exp(-D * dte) * DerivativeModel.Norm(-d1)) * (Sp / L1);
	     
	     if (atm > Sp){
	    	 BWAmerican_Put = BlackEuropean.putPrice(atm, strike, dte, vol, rate,D) + A1 * pow(atm / Sp , L1);
	     }else {
	    	 BWAmerican_Put = strike - atm;
	     }
	     
         if(BWAmerican_Put < strike - atm) {
             BWAmerican_Put = strike - atm;
         }
         
         return BWAmerican_Put;
	 }
	 
	 
	 private static double Bisectional_call(double S ,double X ,double T ,double r ,double D ,double v ){
	     int n;
	     double dt;
	     double Sx, Su, Sl, Suu ;
	     double d1 , d2 , L2 ;
	     double err , C1 , C2 ;
	     double b , a , h ;
	     double N1 , N2 , E_st ;
	     double IterationCountE ;
		 Sx=0;
	      dt = v * sqrt(T);
	      d1 = (log(S / X) + (r - D + 0.5 * pow(v , 2) ) * T) / dt;
	      d2 = d1 - dt;
	      
	      N1 =DerivativeModel.Norm(d1);
	      N2 = DerivativeModel.Norm(d2);
	 
	      E_st = S * exp((r - D) * T) * N1 / N2;
	      
	      Su = E_st; // ' Guess the high bound
	      Sl = S;// ' Guess the low bound
	      Suu = Su;
	      
	      b = 2 * (r - D) / pow(v , 2) - 1;
	      a = 2 * r / pow(v , 2);
	      h = 1 - exp(-r * T);
	      L2 = (-b + sqrt(pow(b , 2) + 4 * a / h)) *0.5;
	    	      
	      while(true){
	          IterationCountE = 0.000000001;
    	      while( (Su - Sl) > IterationCountE){
    	        Sx = (Su + Sl) / 2;
    	        
    	        d1 = (log(Sx / X) + (r - D + pow(v , 2) * 0.5) * T) / dt;
    	        C1 = Sx - X;
    	        C2 = BlackEuropean.callPrice(Sx, X, T,v,r,D) + (1 - exp(-D * T) * DerivativeModel.Norm(d1)) * Sx / L2;
    	        
    	        if ((C2 - C1) > 0 ){
    	           Sl = Sx;
    	        }else{
    	           Su = Sx;
    	        }
    	        
    	      }
    	      
    	      
    	      if (Round(Sx, 4) == Round(Suu, 4)) {
    	        Su = 2 * Suu;
    	        Suu = Su;
    	      }else{
     	         return Sx  ;  	    	  
    	      }
	  
	      }
	 }

	 private static double Bisectional_put(double S ,double X ,double T ,double r ,double D ,double v ){
	     double dt;
	     double Sx, Su, Sl  ;
	     double d1 ,  L1 ;
	     double  P1 , P2 ;
	     double b , a , h ;

	     double IterationCountE ;
		 Sx=0;
	      dt = v * sqrt(T);
	      d1 = (log(S / X) + (r - D + 0.5 * pow(v , 2) ) * T) / dt;
	      Sl = 0;// ' Guess the low bound
	      Su = S;// ' Guess the high bound
 
	      b = 2 * (r - D) /pow( v , 2);
	      a = 2 * r / pow(v , 2);
	      h = 1 - exp(-r * T);
	      L1 = (-(b - 1) - sqrt(pow((b - 1) , 2) + 4 * a / h)) * 0.5;
	 
          IterationCountE = 0.000000001;
	      while( (Su - Sl) > IterationCountE){
	        Sx = (Su + Sl) / 2;
	        
	        d1 = (log(Sx / X) + (r - D + pow(v , 2) * 0.5) * T) / dt;
	        P1 =   X - Sx;
	        P2 = BlackEuropean.putPrice(Sx, X, T, v,r,D) - (1 - exp(-D * T) * DerivativeModel.Norm(-d1)) * Sx / L1;
	        
	        if ((P2 - P1) > 0 ){
 	           Su = Sx;
	        }else{
 	           Sl = Sx;
	        }
	      }
	      
	      return Sx;
	 }


		static double sqrt(double x){
			return Math.sqrt(x);
		}
		static double exp(double x){
			return Math.exp(x);
		}
		static double pow(double value, double exponent){
			return Math.pow(value, exponent);
		}
		static double log(double x){
			return Math.log(x);
		}
		static double fabs(double x){
			return Math.abs(x);
		}
		static double max(double x, double y){
			return Math.max(x, y);
		}
		static double Round(double x,int prec){
			double p = pow(10,prec);
			double y = Math.round(x* p);
			y = y/p;
			return y;
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

}
