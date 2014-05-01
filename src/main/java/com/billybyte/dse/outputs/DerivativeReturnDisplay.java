package com.billybyte.dse.outputs;

public class DerivativeReturnDisplay extends DerivativeReturn{
	private final String derivativeShortName;
	
	public DerivativeReturnDisplay(
			String derivativeShortName,
			DerivativeReturn dr){
		super(
				dr.getSensitivityId(),
				dr.getWithRespectToShortName(),
				dr.getValue(),
				dr.getException());
		this.derivativeShortName = derivativeShortName;
	}
	
	public DerivativeReturnDisplay(
			String derivativeShortName,
			DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName, Number value) {
		super(sensitivityId, withRespectToShortName, value);
		this.derivativeShortName = derivativeShortName;
	}

	public DerivativeReturnDisplay(
			String derivativeShortName,
			DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName, Number value, Exception exception) {
		super(sensitivityId, withRespectToShortName, value, exception);
		this.derivativeShortName = derivativeShortName;
	}
	
	

	public String getDerivativeShortName() {
		return derivativeShortName;
	}

	@Override
	public String toString() {
		return super.toString() + "," +  derivativeShortName ;
	}
	
	

}
