package com.billybyte.dse;



import java.util.List;
import java.util.Map;

import java.util.Set;

import com.billybyte.dse.inputs.InBlk;
import com.billybyte.dse.inputs.diotypes.DioType;
import com.billybyte.dse.outputs.DerivativeReturn;
import com.billybyte.dse.outputs.DerivativeSensitivityTypeInterface;


public interface DerivativeModelInterface {
	public DerivativeReturn getPrice(String derivativeShortName,InBlk inputs);
	
	public Set<DerivativeSensitivityTypeInterface> getSensitivitesSupported();
	
	public DerivativeReturn[] getAllSensitivites(String derivativeShortName,InBlk inputs);
	
	public DerivativeReturn[] getSensitivity(DerivativeSensitivityTypeInterface sensitivityId,
			String derivativeShortName,InBlk inputs);
	
	
	public List<DioType<?>> getUnderlyingInputTypes();
	
	public List<DioType<?>> getMainInputTypes();
	
//	public DioType<?>[] getPriceArgTypes();
//	
//	public double[][]  getPriceArgValues(InBlk inBlk);
	
	public double[] getPriceArray(Map<DioType<?>,double[]> mainInputs,
			Map<DioType<?>,double[][]> underlyingInputs);
//	public Calendar getEvaluationDate();
}

