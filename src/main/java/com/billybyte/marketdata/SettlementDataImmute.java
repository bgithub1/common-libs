package com.billybyte.marketdata;

import java.math.BigDecimal;

import com.billybyte.commoninterfaces.SettlementDataInterface;

/**
 * This class holds immutable data related to settlements
 * @author Bill Perlman
 *
 */
public class SettlementDataImmute implements SettlementDataInterface{
	private final String shortName;
	private final BigDecimal price;
	private int size;
	private long time;

	
	
	@SuppressWarnings("unused")
	private SettlementDataImmute() {
		super();
		this.shortName = null;
		this.price = null;
		this.size = 0;
		this.time = 0;
	}

	public SettlementDataImmute(String shortName, BigDecimal price, int size,
			long time) {
		super();
		this.shortName = shortName;
		this.price = price;
		this.size = size;
		this.time = time;
	}

	@Override
	public String getShortName() {
		return shortName;
	}

	@Override
	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public int getSize() {
		return size;
	}

	@Override
	public long getTime() {
		return time;
	}

	@Override
	public String toString() {
		String priceString =null;
		if(price!=null){
			priceString = price.toString();
		}
		return shortName+getToStringSeparator()+
			priceString+getToStringSeparator()+
				size+getToStringSeparator()+
				time;
	}

	@Override
	public String getToStringSeparator() {
		return ",";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((price == null) ? 0 : price.hashCode());
		result = prime * result
				+ ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result + size;
		result = prime * result + (int) (time ^ (time >>> 32));
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
		SettlementDataImmute other = (SettlementDataImmute) obj;
		if (price == null) {
			if (other.price != null)
				return false;
		} else if (!price.equals(other.price))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (size != other.size)
			return false;
		if (time != other.time)
			return false;
		return true;
	}
	
	

}
