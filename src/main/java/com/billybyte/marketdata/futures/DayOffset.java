package com.billybyte.marketdata.futures;

import com.billybyte.marketdata.SecEnums.DayType;

public class DayOffset {
	final int qty;
	final DayType dayType;
	
	// to prevent use
	@SuppressWarnings("unused")
	private DayOffset(){
		this.qty = 0;
		dayType=null;
	}
	
	public DayOffset(int qty, DayType dayType){
		this.qty = qty;
		this.dayType = dayType;
	}
}
