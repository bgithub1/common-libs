package com.billybyte.marketdata.futures.csos;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public  class MultiUnderlyingInputBlock{
	final String derivativeName;
	final List<String> underlyings;
	final double callPut;
	final List<BigDecimal> atms;
	final BigDecimal strike;
	final List<Double> dte;
	final List<BigDecimal> vols;
	final List<BigDecimal> rates;
	final List<BigDecimal> divs;
	final double[][] corrMatrix;
	final List<Object[]> others;
			
	public MultiUnderlyingInputBlock(String derivativeName,
			List<String> underlyings,
			Double callPut,
			List<BigDecimal> atms, BigDecimal strike, List<Double> dte,
			List<BigDecimal> vols, List<BigDecimal> rates,
			List<BigDecimal> divs, final double[][] corrMatrix,
			List<Object[]> others) {
		super();
		this.underlyings = underlyings;
		this.derivativeName = derivativeName;
		this.callPut = callPut;
		this.atms = atms;
		this.strike = strike;
		this.dte = dte;
		this.vols = vols;
		this.rates = rates;
		this.divs = divs;
		this.corrMatrix = corrMatrix;
		this.others = others;
	}

	public String getDerivativeName() {
		return derivativeName;
	}

	public List<String> getUnderlyings() {
		return underlyings;
	}

	public double getCallPut() {
		return callPut;
	}

	public List<BigDecimal> getAtms() {
		return atms;
	}

	public BigDecimal getStrike() {
		return strike;
	}

	public List<Double> getDte() {
		return dte;
	}

	public List<BigDecimal> getVols() {
		return vols;
	}

	public List<BigDecimal> getRates() {
		return rates;
	}

	public List<BigDecimal> getDivs() {
		return divs;
	}

	public double[][] getCorrMatrix() {
		return corrMatrix;
	}

	public List<Object[]> getOthers() {
		return others;
	}

	@Override
	public String toString() {
		
		String ret =  derivativeName + ", " ;
		ret = ret + 
				(underlyings==null?"null":Arrays.toString(underlyings.toArray(new String[]{}))) + ", " ;
		ret = ret + 
				callPut + ", " ;
		ret = ret + 
				(atms==null?"null":Arrays.toString(atms.toArray(new BigDecimal[]{}))) + ", " ;
		ret = ret + 
				strike + ", "; 
		ret = ret + 
				(dte==null?"null":Arrays.toString(dte.toArray(new Double[]{}))) + ", "+ 
				(vols==null?"null":Arrays.toString(vols.toArray(new BigDecimal[]{}))) + ", ";
		ret = ret + 
				(rates==null?"null":Arrays.toString(rates.toArray(new BigDecimal[]{}))) + ", " ;
		ret = ret + 
				(divs==null?"null":Arrays.toString(divs.toArray(new BigDecimal[]{}))) + ", " ;
		for(int i = 0;i<corrMatrix.length;i++){
			for(int j = 0;j<corrMatrix[i].length;j++){
				ret = ret + corrMatrix[i][j]  + ", " ;
			}
		}
		for(Object o:others){
			ret = ret + o.toString()+",";
		}
			
		return ret.substring(0,ret.length()-1);
	}
	
}
