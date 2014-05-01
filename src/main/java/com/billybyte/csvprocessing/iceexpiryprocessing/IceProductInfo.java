package com.billybyte.csvprocessing.iceexpiryprocessing;

public class IceProductInfo {
	private final String symbol;
	private final String description;
	private final String exchange;
	
	public IceProductInfo(String symbol,String exchange, String description) {
		super();
		this.symbol = symbol;
		this.description = description;
		this.exchange = exchange;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result
				+ ((exchange == null) ? 0 : exchange.hashCode());
		result = prime * result + ((symbol == null) ? 0 : symbol.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IceProductInfo other = (IceProductInfo) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (exchange == null) {
			if (other.exchange != null)
				return false;
		} else if (!exchange.equals(other.exchange))
			return false;
		if (symbol == null) {
			if (other.symbol != null)
				return false;
		} else if (!symbol.equals(other.symbol))
			return false;
		return true;
	}
	
	@SuppressWarnings("unused")
	private IceProductInfo() {
		super();
		this.symbol = null;
		this.description = null;
		this.exchange=null;
	}
	public String getSymbol() {
		return symbol;
	}
	public String getDescription() {
		return description;
	}
	public String getExchange() {
		return exchange;
	}
	

	
	@Override
	public String toString() {

		return symbol+","+description;
	}
	
	
}
