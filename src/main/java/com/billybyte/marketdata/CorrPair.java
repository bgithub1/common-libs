package com.billybyte.marketdata;

public class CorrPair{
	private final String sn0;
	private final String sn1;
	private final double corr;
	
	public CorrPair(String sn0, String sn1, double corr) {
		super();
		this.sn0 = sn0;
		this.sn1 = sn1;
		this.corr = corr;
	}
	
	
	public String getSn0() {
		return sn0;
	}

	public String getSn1() {
		return sn1;
	}


	public double getCorr() {
		return corr;
	}
	
	public String getKey(){
		if(sn0==null || sn1==null)return null;
		if(sn0.compareTo(sn1)<=0){
			return sn0+"__"+sn1;
		}else{
			return sn1+"__"+sn0;
		}
	}


	@Override
	public String toString() {
		return sn0 + ", " + sn1 + ", " + corr;
	}
	
}