package com.billybyte.commoninterfaces;

import java.math.BigDecimal;
import java.util.Calendar;

import com.billybyte.marketdata.SecDef;

/**
 * Interface for  accessing volatility surface information, especially in 
 *  DerivativeSetEngine.
 *  
 * @author bperlman1
 *
 */
public interface VolSurfaceInterface {
	public BigDecimal getVol(
			SecDef secDef,BigDecimal atmPrice, BigDecimal atmVol,Double rate,Object[] params, Calendar evaluationTime);
	public BigDecimal getAtmVol(
			SecDef secDef,BigDecimal atmPrice, BigDecimal atmVol, Double rate,Object[] params, Calendar evaluationTime);
}
