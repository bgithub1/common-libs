package com.billybyte.marketdata.futures.csos;

public class MultiUnderlyingInputBlockNeoDatisWrapper {
	private final String shortName;
	private final Object inputBlockObject;
	public MultiUnderlyingInputBlockNeoDatisWrapper(String shortName,
			Object inputBlockObject) {
		super();
		this.shortName = shortName;
		this.inputBlockObject = inputBlockObject;
	}
	public String getShortName() {
		return shortName;
	}
	public Object getInputBlockObject() {
		return inputBlockObject;
	}
	
}
