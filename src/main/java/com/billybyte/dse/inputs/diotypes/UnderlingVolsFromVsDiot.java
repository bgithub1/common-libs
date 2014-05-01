package com.billybyte.dse.inputs.diotypes;

import java.math.BigDecimal;

/**
 * Diot that gets vols for each underlying from a query that returns Map<String, ComplexQueryResult<BigDecimal>
 * @author bperlman1
 *
 */
public class UnderlingVolsFromVsDiot extends DioType<BigDecimal>{
	private static String key = "UNDERLYING_VOLS_FROM_VOLSURF_DIOT";

	public UnderlingVolsFromVsDiot() {
		super(key);
	}
}
