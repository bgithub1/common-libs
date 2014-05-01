package com.billybyte.dse.outputs;

public class DerivativeReturn {
	private final DerivativeSensitivityTypeInterface sensitivityId;
	private final String withRespectToShortName;
	private final Number value;
	private final Exception exception;
	public Number getValue() {
		return value;
	}
	public DerivativeReturn(DerivativeSensitivityTypeInterface sensitivityId,String withRespectToShortName, Number value) {
		super();
		this.sensitivityId = sensitivityId;
		this.withRespectToShortName = withRespectToShortName;
		this.value = value;
		this.exception=null;
	}

	@SuppressWarnings("unused")
	private DerivativeReturn() {
		super();
		this.sensitivityId = null;
		this.withRespectToShortName = null;
		this.value = null;
		this.exception=null;
	}
	
	public DerivativeReturn(DerivativeSensitivityTypeInterface sensitivityId,
			String withRespectToShortName, Number value,Exception exception) {
		super();
		this.sensitivityId = sensitivityId;
		this.withRespectToShortName = withRespectToShortName;
		this.value = value;
		this.exception=exception;
	}

	public String getWithRespectToShortName() {
		return withRespectToShortName;
	}
	public DerivativeSensitivityTypeInterface getSensitivityId() {
		return sensitivityId;
	}
	public Exception getException() {
		return exception;
	}
	public boolean isValidReturn(){
		if(getException()==null && getWithRespectToShortName()!=null &&
				getSensitivityId()!=null && getValue()!=null){
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		if(exception!=null){
			return sensitivityId + ", " + withRespectToShortName + ", " 
					+ ", " + exception;
		}
		return sensitivityId + ", " + withRespectToShortName + ", " + value
				+ ", " + exception;
	}
	
}
