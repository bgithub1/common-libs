package com.billybyte.dse.models.spread.newtonraphsonforspreads;
import static java.lang.Math.abs;
//import java.util.*;
/**
 * general solution for the problem of finding x 
 * 		when  you know 
 * 		1.  the function y=f(x), 
 * 		2.  the first derivative of that function f'(x)
 * 		3.  the value of y, such that you will find the value of x that
 * 				achieves that y.
 * 		If you know f'(x), then @Override the function Derivative.derivation(x).
 * 		If you don't know f'(x), then you use the pseudo version of in Derivative.
 * 
 *  	See BlackSholes.OptionsModel.NrImpliedVol as an example of how to set up
 *  		an f(x), when the main y=f(x) is really y=f(x,y,z,a,b,c...).
 *  
 * @author bperlman1
 *
 */
public abstract class NewtonRaphson extends Derivative
{   
//	protected int counter=0;
	public abstract double newtonroot(double rootvalue);  //the requesting function implements the calculation fx//
	public double precisionvalue=0.0;
        
	public int iterate=0;
	
	public void accuracy(double precision,int iterations)//method gets the desired accuracy//
	{
		super.h=precision;//sets the superclass derivative to the desired precision//
		this.precisionvalue=precision;
		this.iterate=iterations;
	}
	/**
	 * Main entry point to call
	 * @param seedValue - for instance, if you are seeking implied volatility, then
	 *    some volatility number would go into seed value.
	 * @return
	 */
	public double newtraph(double seedValue)
	{ //System.out.println("Accuravcy levels=="+precisionvalue+"ITERATIONS=="+iterate);
         double fx=0.0;
         double Fx=0.0;
         double x=0.0;
         double lowerbound = seedValue;
         double newtRoot = newtonroot(lowerbound);
         fx=floorvalue(newtRoot);
 		 if(Double.isInfinite(fx) || Double.isNaN(fx))return Double.NaN;
         if(Math.abs(fx)<=precisionvalue)return lowerbound;
         double newtDeriv = derivation(lowerbound);
		 Fx=floorvalue(newtDeriv);
	 	 if(Double.isInfinite(Fx) || Double.isNaN(Fx))return Double.NaN;
		 x=floorvalue((lowerbound-(fx/Fx)));
		if(Double.isInfinite(x) || Double.isNaN(x))return Double.NaN;
		int counter = 0;
		
		while((Math.abs(x-lowerbound)>precisionvalue&counter<iterate))
			{
				
				lowerbound=x;
				 fx=newtonroot(lowerbound);
				 Fx=derivation(lowerbound);
				 x=floorvalue((lowerbound-(fx/Fx)));
						counter++;
	}
	//System.out.println("The Solution is:....................."+x);
		if(counter>=iterate){
			return Double.NaN;
		}
		return x;
	}
	/**
	 * DO NOT OVERRIDE THIS FUNCTION!!! USE derivation instead
	 */
	protected double deriveFunction(double inputa)
	{
            double x1=0.0;
		 x1=newtonroot(inputa);
				return x1;
			
	}
    public double floorvalue(double x)
	{
		return abs(x)<Csmallnumber.getSmallnumber()?Csmallnumber.getSmallnumber():x;
	}
}

