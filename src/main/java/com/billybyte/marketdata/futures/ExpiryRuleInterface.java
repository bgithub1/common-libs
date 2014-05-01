package com.billybyte.marketdata.futures;

import java.util.Calendar;

public interface ExpiryRuleInterface{
	public Calendar getExpiry(String shortName);
}
