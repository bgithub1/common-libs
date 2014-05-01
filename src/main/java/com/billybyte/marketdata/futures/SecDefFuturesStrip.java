package com.billybyte.marketdata.futures;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefFromSecDefList;

public class SecDefFuturesStrip extends SecDefFromSecDefList{
	static final BigDecimal negOne = new BigDecimal(-1);
	static final long minDaysBetween2Months=20;
	static final long maxDaysBetween2Months=41;
	public SecDefFuturesStrip(SecDef[] secDefLegs,BigDecimal[] sizes,BigDecimal[] multipliers) {
		super(secDefLegs[0].getShortName()+":"+secDefLegs[secDefLegs.length-1], secDefLegs, 
				sizes, multipliers, ":",
				0);
		if(secDefLegs.length<2){
			throw Utils.IllArg(this, "must only at least 2 legs");
		}
		
		// check to make sure that the strip is in serial order
		for(int i = 1;i<secDefLegs.length;i++){
			Calendar c0 = Calendar.getInstance();
			c0.set(secDefLegs[i-1].getExpiryYear(), secDefLegs[i-1].getExpiryMonth(),secDefLegs[i-1].getExpiryDay());
			Calendar c1 = Calendar.getInstance();
			c1.set(secDefLegs[i].getExpiryYear(), secDefLegs[i].getExpiryMonth(),secDefLegs[i].getExpiryDay());
			long diff = Dates.getDifference(c0, c1, TimeUnit.DAYS);
		
			if(diff<minDaysBetween2Months || diff>maxDaysBetween2Months){
				throw Utils.IllArg(this, "secDef "+secDefLegs[i-1].getShortName()+" and "+secDefLegs[i].getShortName()+ 
						" are out of order and not separated by a month  :  " +
						secDefLegs[i-1].getShortName()+"  expiry = "+c0.getTime().toString() +
						secDefLegs[i].getShortName()+"  expiry = "+c1.getTime().toString());
			}
		}
	}

	@Override
	public String getExchangeSymbol() {		
		return getSecDefLegs()[getLooksLikeLeg()].getExchangeSymbol();
	}

	@Override
	public String getSymbol() {
		return getSecDefLegs()[getLooksLikeLeg()].getSymbol();
	}

	@Override
	public String toString() {
		String ret ="";
		for(SecDef sd:getSecDefLegs()){
			ret +=sd.toString()+";";
		}
		
		return ret.substring(0, ret.length()-1);
	}


}
