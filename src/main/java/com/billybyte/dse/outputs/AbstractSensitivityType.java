package com.billybyte.dse.outputs;

public abstract class  AbstractSensitivityType implements DerivativeSensitivityTypeInterface{

	@Override
	public boolean equals(Object arg0) {
		return this.toString().compareTo(arg0.toString())==0;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return getString();
	}

	@Override
	public int compareTo(DerivativeSensitivityTypeInterface o) {
		return this.toString().compareTo(o.toString());
	}

	
}
