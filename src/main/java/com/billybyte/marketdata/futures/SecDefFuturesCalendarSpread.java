package com.billybyte.marketdata.futures;

import java.math.BigDecimal;
import java.util.Calendar;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefFromSecDefList;

public class SecDefFuturesCalendarSpread extends SecDefFromSecDefList {
	static final BigDecimal negOne = new BigDecimal(-1);
	public SecDefFuturesCalendarSpread(SecDef[] secDefLegs) {
		super(secDefLegs[0].getShortName()+"-"+secDefLegs[1].getShortName(), secDefLegs, new BigDecimal[]{BigDecimal.ONE,negOne}, new BigDecimal[]{BigDecimal.ONE,negOne}, "-",
				0);
		if(secDefLegs.length!=2){
			throw Utils.IllArg(this, "must only have 2 legs");
		}
		Calendar c0 = Calendar.getInstance();
		c0.set(secDefLegs[0].getExpiryYear(), secDefLegs[0].getExpiryMonth(),secDefLegs[0].getExpiryDay());
		Calendar c1 = Calendar.getInstance();
		c1.set(secDefLegs[1].getExpiryYear(), secDefLegs[1].getExpiryMonth(),secDefLegs[1].getExpiryDay());
		if(c1.compareTo(c0)<=0){
			throw Utils.IllArg(this, "secDef0 expires after secDef1" + 
					"  secDef0 expiry = "+c0.getTime().toString() +
					"  secDef1 expiry = "+c1.getTime().toString());
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
		return getSecDefLegs()[0].toString()+"-"+getSecDefLegs()[1].toString();
	}

}
