package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;


public class SecDefSimple implements SecDef{
	ShortNameInfo shortNameInfo;
	String shortName;
	int exchangePrecision;
	String exchangeSymbol;
	int expiryDay;
	int expiryMonth;
	int expiryYear;
	BigDecimal minTick;
	BigDecimal multiplier;
	SecExchange primaryExch;
	
	
	public SecDefSimple(String shortName,ShortNameInfo shortNameInfo,String exchangeSymbol,
			int exchangePrecision, BigDecimal minTick, int expiryYear,
			int expiryMonth, int expiryDay, BigDecimal multiplier,
			SecExchange primaryExch) {
		super();
		this.shortName = shortName;
		this.shortNameInfo = shortNameInfo;
		this.exchangeSymbol = exchangeSymbol;
		this.exchangePrecision = exchangePrecision;
		this.minTick = minTick.setScale(exchangePrecision,RoundingMode.HALF_EVEN);
		this.expiryYear = expiryYear;
		this.expiryMonth = expiryMonth;
		this.expiryDay = expiryDay;
		this.multiplier = multiplier;
		this.primaryExch = primaryExch;
	}
	
	public SecDefSimple(SecDef clone) {
		super();
		this.shortNameInfo = new ShortNameInfo(clone.getSymbol(),clone.getSymbolType(),clone.getExchange(),
				clone.getCurrency(),clone.getContractYear(),clone.getContractMonth(),
				clone.getContractDay(),clone.getRight(),clone.getStrike());
		this.shortName = shortNameInfo.getShortName();
		this.exchangeSymbol = clone.getExchangeSymbol();
		this.exchangePrecision = clone.getExchangePrecision();
		this.minTick = clone.getMinTick();
		this.expiryYear = clone.getExpiryYear();
		this.expiryMonth = clone.getExpiryMonth();
		this.expiryDay = clone.getExpiryDay();
		this.multiplier = clone.getMultiplier();
		this.primaryExch = clone.getPrimaryExch();
	}

	
	@Override
	public String getShortName() {
		// 
		return shortName;
	}
	
	@Override
	public Integer getContractDay() {
		return shortNameInfo.getContractDay();
	}

	@Override
	public int getContractMonth() {
		return shortNameInfo.getContractMonth();
	}

	@Override
	public int getContractYear() {
		return shortNameInfo.getContractYear();
	}

	
	
	@Override
	public SecCurrency getCurrency() {
		return shortNameInfo.getCurrency();
	}

	@Override
	public SecExchange getExchange() {
		return shortNameInfo.getExchange();
	}


	@Override
	public String getRight() {
		return shortNameInfo.getRight();
	}


	@Override
	public BigDecimal getStrike() {
		return shortNameInfo.getStrike();
	}

	@Override
	public String getSymbol() {
		return shortNameInfo.getSymbol();
	}

	@Override
	public SecSymbolType getSymbolType() {
		return shortNameInfo.getSymbolType();
	}

	@Override
	public int getExchangePrecision() {
		// 
		return exchangePrecision;
	}

	@Override
	public String getExchangeSymbol() {
		// 
		return exchangeSymbol;
	}

	@Override
	public int getExpiryDay() {
		// 
		return expiryDay;
	}

	@Override
	public int getExpiryMonth() {
		// 
		return expiryMonth;
	}

	@Override
	public int getExpiryYear() {
		// 
		return expiryYear;
	}

	@Override
	public BigDecimal getMinTick() {
		// 
		return minTick;
	}

	@Override
	public BigDecimal getMultiplier() {
		// 
		return multiplier;
	}

	@Override
	public SecExchange getPrimaryExch() {
		// 
		return primaryExch;
	}
	
	
	
	public static String createToString(SecDef sd){
		return sd.getShortName()+","+
			sd.getSymbol()+","+
			sd.getSymbolType()+","+
			sd.getCurrency().toString()+","+
			sd.getContractYear()+","+
			sd.getContractMonth()+","+
			sd.getRight()+","+
			sd.getStrike()+","+
			sd.getExchangeSymbol()+","+
			sd.getExchangePrecision()+","+
			sd.getMinTick()+","+
			sd.getExpiryYear()+","+
			sd.getExpiryMonth()+","+
			sd.getExpiryDay()+","+
			sd.getMultiplier()+","+
			sd.getPrimaryExch()
			;
	}

	
	
	@Override
	public String toString() {
		return createToString(this);
	}

}
