package com.billybyte.marketdata;

import java.math.BigDecimal;

public interface PriceLevelData {
	public BigDecimal getPrice();
	public int getSize();
	public long getTime();
}
