package com.billybyte.marketdata;

import java.math.BigDecimal;

import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public interface SecDef {
	public String getShortName();
    public String getSymbol();
    public SecSymbolType getSymbolType();
    public int getContractYear();
    public int getContractMonth();
    public Integer getContractDay();
    public int getExpiryYear();
    public int getExpiryMonth();
    public int getExpiryDay();
    public BigDecimal getStrike();
    public String getRight();
    public BigDecimal getMultiplier();
    public int getExchangePrecision();
    public BigDecimal getMinTick();
    public SecExchange getExchange();
    
    public SecCurrency getCurrency();
    public String getExchangeSymbol();
    public SecExchange getPrimaryExch();
   

}
