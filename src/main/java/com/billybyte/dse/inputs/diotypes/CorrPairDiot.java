package com.billybyte.dse.inputs.diotypes;

import java.math.BigDecimal;

/**
 * DioType that passes Correlation Map to user
 * @author bperlman1
 *
 */
public class CorrPairDiot extends DioType<BigDecimal>{
	private static String key = "CORR_PAIR_DIOT";
	public CorrPairDiot() {
		super(key);
	}
}
