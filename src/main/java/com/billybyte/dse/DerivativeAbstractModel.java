package com.billybyte.dse;

import java.util.Calendar;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.dse.outputs.DayDeltaDerSen;
import com.billybyte.dse.outputs.DeltaDerSen;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;
import com.billybyte.dse.outputs.GammaDerSen;
import com.billybyte.dse.outputs.ImpliedVolDerSen;
import com.billybyte.dse.outputs.OptPriceDerSen;
import com.billybyte.dse.outputs.RhoDerSen;
import com.billybyte.dse.outputs.ThetaDerSen;
import com.billybyte.dse.outputs.VegaDerSen;

public abstract class DerivativeAbstractModel implements DerivativeModelInterface{
	protected static final String SENSITIVITY_NOT_SUPPORTED = 
			"Sensitivity Not Supported";
	
	protected static final String OPTPR = 
			new OptPriceDerSen().getString();
	protected static final String DELTA = 
			new DeltaDerSen().getString();
	protected static final String GAMMA = 
			new GammaDerSen().getString();
	protected static final String VEGA = 
			new VegaDerSen().getString();
	protected static final String THETA = 
			new ThetaDerSen().getString();
	protected static final String DAYDELTA = 
			new DayDeltaDerSen().getString();
	protected static final String RHO = 
			new RhoDerSen().getString();
	protected static final String IMPLIEDVOL = 
			new ImpliedVolDerSen().getString();
	
	
	public static final boolean isNan(double value){
//		Double d = value;
		return Double.isNaN(value);//d.compareTo(Double.NaN)==0;
	}
	
	public static final boolean isNan(Double value){
		return Double.isNaN(value);//d.compareTo(Double.NaN)==0;
//		return value.compareTo(Double.NaN)==0;
	}
	
	public static final double ONEDAYLATER = 1.0/365.0;
	private final Calendar evaluationDate;
	
	public DerivativeAbstractModel(Calendar evaluationDate) {
		super();
		this.evaluationDate = evaluationDate;
	}

	
	protected DerivativeReturn errRet(DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName,String cause){
		Exception e  = Utils.IllState(this.getClass(),withRespectToShortName+" : "+cause);
		return new DerivativeReturn(sensitivityId, withRespectToShortName, null,e);
	}
	
	protected DerivativeReturn goodRet(
			DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName,double value){
		return new DerivativeReturn(sensitivityId, withRespectToShortName, value);
	}

	
	

}
