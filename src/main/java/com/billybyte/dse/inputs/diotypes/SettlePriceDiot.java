package com.billybyte.dse.inputs.diotypes;


import com.billybyte.commoninterfaces.SettlementDataInterface;

public class SettlePriceDiot extends DioType<SettlementDataInterface>{
	private static String key = "SETTLE_PRICE_ARRAY_DIT";
	public SettlePriceDiot() {
		super(key);
	}
	
}
