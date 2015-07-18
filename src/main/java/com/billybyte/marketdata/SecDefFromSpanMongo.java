package com.billybyte.marketdata;

import java.math.BigDecimal;

import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.mongodb.DBObject;

public class SecDefFromSpanMongo implements SecDef{
	public static final String  SD_shortName =  "shortName";
    public static final String  SD_symbol =  "symbol";
    public static final String  SD_symbolType =  "symbolType";
    public static final String  SD_contractYear =  "contractYear";
    public static final String  SD_contractMonth =  "contractMonth";
    public static final String  SD_contractDay =  "contractDay";
    public static final String  SD_expiryYear =  "expiryYear";
    public static final String  SD_expiryMonth =  "expiryMonth";
    public static final String  SD_expiryDay =  "expiryDay";
    public static final String  SD_strike =  "strike";
    public static final String  SD_right =  "right";
    public static final String  SD_multiplier =  "multiplier";
    public static final String  SD_exchangePrecision =  "exchangePrecision";
    public static final String  SD_minTick =  "minTick";
    public static final String  SD_exchange =  "exchange";
    public static final String  SD_currency =  "currency";
    public static final String  SD_exchangeSymbol =  "exchangeSymbol";
    public static final String  SD_primaryExch =  "primaryExch";
	
	
	
	private final SecDef innerSecDef;
	
	public SecDefFromSpanMongo(DBObject spanDbObject){
		String shortName = spanDbObject.get(SD_shortName).toString();
		String symbol = spanDbObject.get(SD_symbol).toString();
		SecSymbolType symbolType = SecSymbolType.fromString(spanDbObject.get(SD_symbolType).toString());
		SecExchange exchange = SecExchange.fromString(spanDbObject.get(SD_exchange).toString());
		SecCurrency currency = SecCurrency.fromString(spanDbObject.get(SD_currency).toString());
		int contractYear = new Integer(spanDbObject.get(SD_contractYear).toString());
		int contractMonth = new Integer(spanDbObject.get(SD_contractMonth).toString());
		int contractDay = new Integer(spanDbObject.get(SD_contractDay).toString());
		String right =  null;
		BigDecimal strike = null;
		Object strikeObj = spanDbObject.get(SD_strike);
		if(strikeObj!=null && strikeObj.toString().compareTo("  ")>0){
			strike = new BigDecimal(strikeObj.toString());
			right = spanDbObject.get(SD_right).toString();
		}
		
		ShortNameInfo shortNameInfo = 
				new  ShortNameInfo(
						symbol, symbolType, exchange, currency, 
						contractYear, contractMonth, contractDay, right, strike);
		int exchangePrecision = new Integer(spanDbObject.get(SD_exchangePrecision).toString());
		BigDecimal minTick = new BigDecimal(spanDbObject.get(SD_minTick).toString());
		int expiryYear = new Integer(spanDbObject.get(SD_expiryYear).toString());
		int expiryMonth = new Integer(spanDbObject.get(SD_expiryMonth).toString());
		int expiryDay = new Integer(spanDbObject.get(SD_expiryDay).toString());
		BigDecimal multiplier = new BigDecimal(spanDbObject.get(SD_multiplier).toString());
		SecExchange primaryExch = SecExchange.fromString(spanDbObject.get(SD_primaryExch).toString());
		
		this.innerSecDef = new SecDefSimple(
				shortName, shortNameInfo, exchange.toString(), 
				exchangePrecision, minTick, expiryYear, expiryMonth, 
				expiryDay, multiplier, primaryExch);
	}

	@Override
	public String getShortName() {
		return this.innerSecDef.getShortName();
	}

	@Override
	public String getSymbol() {
		return this.innerSecDef.getSymbol();
	}

	@Override
	public SecSymbolType getSymbolType() {
		return this.innerSecDef.getSymbolType();
	}

	@Override
	public int getContractYear() {
		return this.innerSecDef.getContractYear();
	}

	@Override
	public int getContractMonth() {
		return this.innerSecDef.getContractMonth();
	}

	@Override
	public Integer getContractDay() {
		return this.innerSecDef.getContractDay();
	}

	@Override
	public int getExpiryYear() {
		return this.innerSecDef.getExpiryYear();
	}

	@Override
	public int getExpiryMonth() {
		return this.innerSecDef.getExpiryMonth();
	}

	@Override
	public int getExpiryDay() {
		return this.innerSecDef.getExpiryDay();
	}

	@Override
	public BigDecimal getStrike() {
		return this.innerSecDef.getStrike();
	}

	@Override
	public String getRight() {
		return this.innerSecDef.getRight();
	}

	@Override
	public BigDecimal getMultiplier() {
		return this.innerSecDef.getMultiplier();
	}

	@Override
	public int getExchangePrecision() {
		return this.innerSecDef.getExchangePrecision();
	}

	@Override
	public BigDecimal getMinTick() {
		return this.innerSecDef.getMinTick();
	}

	@Override
	public SecExchange getExchange() {
		return this.innerSecDef.getExchange();
	}

	@Override
	public SecCurrency getCurrency() {
		return this.innerSecDef.getCurrency();
	}

	@Override
	public String getExchangeSymbol() {
		return this.innerSecDef.getExchangeSymbol();
	}

	@Override
	public SecExchange getPrimaryExch() {
		return this.innerSecDef.getPrimaryExch();
	}

	@Override
	public String toString() {
		return getShortName() + ", " + getSymbol() + ", " + getSymbolType()
				+ ", " + getContractYear() + ", " + getContractMonth() + ", "
				+ getContractDay() + ", " + getExpiryYear() + ", "
				+ getExpiryMonth() + ", " + getExpiryDay() + ", " + getStrike()
				+ ", " + getRight() + ", " + getMultiplier() + ", "
				+ getExchangePrecision() + ", " + getMinTick() + ", "
				+ getExchange() + ", " + getCurrency() + ", "
				+ getExchangeSymbol() + ", " + getPrimaryExch();
	}
	

}
