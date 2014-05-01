package com.billybyte.dse.inputs.diotypes;

import java.math.BigDecimal;

public class CorrDiot extends DioType<BigDecimal>{
	private static String key = "CORR_MATRIX_DIOT";
	public CorrDiot() {
		super(key);
	}
	
}
