package com.billybyte.marketdata.futures;

import java.util.Calendar;

public class ExpiryFixedDate  implements ExpiryRuleInterface {
	private transient final Calendar expiry;

	public ExpiryFixedDate(){
		expiry = null; // for things w/o expiry
		

	}
	
	public ExpiryFixedDate(int expiryYear, int expiryMonth, int expiryDay) {
		super();
		expiry = Calendar.getInstance();
		expiry.set(expiryYear,expiryMonth,expiryDay);
	}


	@Override
	public Calendar getExpiry(String shortName) {
		if(expiry==null)return null;
		Calendar ret = Calendar.getInstance();
		ret.setTimeInMillis(expiry.getTimeInMillis());
		return ret;
	}

}
