package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public  abstract class SecDefFromSecDefList implements SecDef{
	private final String shortName;
	private final SecDef[] secDefLegs;
	private final BigDecimal[] sizes;
	private final BigDecimal[] multipliers;
	private final String shortNameSeparator;
	private final Integer looksLikeLeg;
	
	@SuppressWarnings("unused")
	private SecDefFromSecDefList() {
		super();
		this.shortName = null;
		this.secDefLegs = null;
		this.sizes = null;
		this.multipliers = null;
		this.shortNameSeparator = null;
		this.looksLikeLeg=null;
	}

	public SecDefFromSecDefList(String shortName,SecDef[] secDefLegs, BigDecimal[] sizes,
			BigDecimal[] multipliers, String shortNameSeparator,Integer looksLikeLeg) {
		super();
		if(shortName==null||shortName.compareTo(" ")<=0){
			throw Utils.IllArg(this, " illegal shortName: "+shortName);
		}
		if(secDefLegs==null || secDefLegs.length<2 ){
			String s = secDefLegs == null ? "null" : new Integer(secDefLegs.length).toString();
			throw Utils.IllArg(this, " secDefs are null or have a length < 2 : "+s);
		}
		if(sizes==null || sizes.length<2){
			String s = sizes == null ? "null" : new Integer(sizes.length).toString();
			throw Utils.IllArg(this, "illegal sizes array : "+s);
		}
		if(multipliers==null || multipliers.length<2 ){
			String s = multipliers == null ? "null" : new Integer(multipliers.length).toString();
			throw Utils.IllArg(this, "illegal multipliers array : "+s);
		}
		if(secDefLegs.length!=sizes.length || secDefLegs.length!=multipliers.length){
			throw Utils.IllArg(this, "secDefLegs,  sizes and multipliers arrays are not equal length");
		}

		
		if(shortNameSeparator==null||shortNameSeparator.compareTo(" ")<=0){
			throw Utils.IllArg(this, " illegal shortNameSeparator: "+shortNameSeparator);
		}
		if(looksLikeLeg==null){
			throw Utils.IllArg(this, " looksLikeLeg is null");
		}

		this.shortName = shortName;
		this.secDefLegs = secDefLegs;
		this.sizes = sizes;
		this.multipliers = multipliers;
		this.shortNameSeparator = shortNameSeparator;
		this.looksLikeLeg = looksLikeLeg;
	}

	@Override
	public Integer getContractDay() {
		return secDefLegs[0].getContractDay();
	}

	@Override
	public int getContractMonth() {
		return secDefLegs[0].getContractMonth();
	}

	@Override
	public int getContractYear() {
		return secDefLegs[0].getContractYear();
	}

	@Override
	public SecCurrency getCurrency() {
		return secDefLegs[0].getCurrency();
	}

	@Override
	public SecExchange getExchange() {
		return secDefLegs[0].getExchange();
	}

	@Override
	public int getExchangePrecision() {
		return secDefLegs[this.looksLikeLeg].getExchangePrecision();
	}


	@Override
	public int getExpiryDay() {
		return secDefLegs[0].getExpiryDay();	
	}

	@Override
	public int getExpiryMonth() {
		return secDefLegs[0].getExpiryMonth();	
	}

	@Override
	public int getExpiryYear() {
		return secDefLegs[0].getExpiryYear();	
	}

	@Override
	public BigDecimal getMinTick() {
		return secDefLegs[looksLikeLeg].getMinTick();	
	}

	@Override
	public BigDecimal getMultiplier() {
		return secDefLegs[looksLikeLeg].getMultiplier();	
	}

	@Override
	public SecExchange getPrimaryExch() {
		return secDefLegs[looksLikeLeg].getPrimaryExch();	
	}

	@Override
	public String getRight() {
		return secDefLegs[looksLikeLeg].getRight();	
	}

	@Override
	public String getShortName() {
		return this.shortName;
	}

	@Override
	public BigDecimal getStrike() {
		return secDefLegs[looksLikeLeg].getStrike();	
	}


	@Override
	public SecSymbolType getSymbolType() {
		return secDefLegs[looksLikeLeg].getSymbolType();	
	}

	public SecDef[] getSecDefLegs() {
		SecDef[] ret = new SecDef[secDefLegs.length];
		for(int i = 0;i<secDefLegs.length;i++){
			ret[i] = secDefLegs[i];
		}
		return ret;
	}

	public BigDecimal[] getSizes() {
		BigDecimal[] ret = new BigDecimal[sizes.length];
		for(int i = 0;i<sizes.length;i++){
			ret[i] = sizes[i];
		}
		return ret;
	}

	public BigDecimal[] getMultipliers() {
		BigDecimal[] ret = new BigDecimal[multipliers.length];
		for(int i = 0;i<multipliers.length;i++){
			ret[i] = multipliers[i];
		}
		return ret;
	}

	public String getShortNameSeparator() {
		return shortNameSeparator;
	}

	public Integer getLooksLikeLeg() {
		return looksLikeLeg;
	}



}
