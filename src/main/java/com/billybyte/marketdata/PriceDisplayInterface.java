package com.billybyte.marketdata;

import java.math.BigDecimal;

public interface PriceDisplayInterface {
	public String getShortName() ;
	public PriceLevelData getBid() ;
	public PriceLevelData getOffer();
	public PriceLevelData getLast() ;
	public BigDecimal getSettlement() ;
}
