package com.billybyte.marketdata;

import java.math.BigDecimal;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public class ShortNameInfo {
	final static SecCurrency DEFAULT_CURRENCY=SecCurrency.USD;
	final static String DEFAULT_SHORTNAME_SEPARATOR=".";
	public ShortNameInfo(String symbol, SecSymbolType symbolType,
			SecExchange exchange, SecCurrency currency, int contractYear,
			int contractMonth, Integer contractDay,String right, BigDecimal strike) {
		super();
		if(symbol==null){
			Utils.IllArg(this, "null symbol");
		}
		if(symbolType==null){
			Utils.IllArg(this, "null symbolType");
		}
		if(exchange==null){
			Utils.IllArg(this, "null exchange");
		}
		if(symbolType==SecSymbolType.FUT || symbolType==SecSymbolType.FOP || symbolType==SecSymbolType.OPT){
			if(contractYear==0 || contractMonth==0){
				Utils.IllArg(this, "illegal contractYear of contractMonth : year = " + contractYear+" month = "+contractMonth);
			}
		}
		if(symbolType==SecSymbolType.FOP || symbolType==SecSymbolType.OPT){
			if(right==null || right.compareTo(" ")<=0){
				Utils.IllArg(this, "illegal or null right for option");
			}
			if(strike==null ){
				Utils.IllArg(this, "null strike for optio");
			}
		}
		
		this.symbol = symbol;
		this.symbolType = symbolType;
		this.exchange = exchange;
		if(currency==null){
			this.currency = DEFAULT_CURRENCY;
		}else{
			this.currency = currency;
		}
		this.contractYear = contractYear;
		this.contractMonth = contractMonth;
		this.contractDay = contractDay;
		this.right = right;
		this.strike = strike;
	}

	Integer contractDay;
	public Integer getContractDay(){
		return this.contractDay;
	}
	
	int contractMonth;
	public int getContractMonth() {
		return contractMonth;
	}

	int contractYear;
	public int getContractYear() {
		return contractYear;
	}

	SecCurrency currency;
	public SecCurrency getCurrency() {
		return currency;
	}

	SecExchange exchange;
	public SecExchange getExchange() {
		return exchange;
	}
	String right;
	public String getRight() {
		return right;
	}
	
	BigDecimal strike;
	public BigDecimal getStrike() {
		return strike;
	}

	String symbol;
	public String getSymbol() {
		return symbol;
	}

	SecSymbolType symbolType;
	public SecSymbolType getSymbolType() {
		return symbolType;
	}
	
	public String getShortName(){
		String ret = 		this.symbol+DEFAULT_SHORTNAME_SEPARATOR+
		this.symbolType.toString()+DEFAULT_SHORTNAME_SEPARATOR+
		this.exchange.toString();
		if(this.symbolType==SecSymbolType.STK && getCurrency()==SecCurrency.USD){
			return ret;
		}
		// add in currency
		ret = ret+DEFAULT_SHORTNAME_SEPARATOR+
				this.currency.toString();
		// add in monthyear
		Integer monthYear = this.contractYear*100+this.contractMonth;
		ret = ret+DEFAULT_SHORTNAME_SEPARATOR+monthYear;
		// add in right and strike if option
		
		if(right==null || this.right.compareTo(" ")<=0){
			if(this.symbolType==SecSymbolType.FOP || this.symbolType==SecSymbolType.OPT){
				throw Utils.IllState(this, " null or blank right when symbol type = "+getSymbolType().toString());
			}
			// else this is a FUT, so return it
			return ret;
		}
		// you have a non null right with an option.  Now check the strike
		if(strike==null){
			throw Utils.IllState(this, " strike cannot be null, while right is set");
		}
		ret  = ret+DEFAULT_SHORTNAME_SEPARATOR+this.right+DEFAULT_SHORTNAME_SEPARATOR+this.strike.toString();
		return ret;
	}


}
