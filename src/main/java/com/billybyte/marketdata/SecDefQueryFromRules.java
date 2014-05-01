package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

import com.billybyte.marketdata.futures.ExpiryRuleInterface;
import com.billybyte.marketdata.futures.FuturesProductQuery;

public class SecDefQueryFromRules implements QueryInterface<String, SecDef>{
	private final ConcurrentHashMap<String, SecDef> cache= new ConcurrentHashMap<String, SecDef>();
	private final ShortNameProcessorInterface shortNameProcessor;
	private final Set<SecSymbolType> validTypes;
	private final Set<String> validSymbols;
	private final List<Integer> validContractMonths;
	private final int defaultPrecision;
	private final BigDecimal defaultMinTick;
	private final BigDecimal defaultMultiplier;
	private final SecExchange defaultExchange;
	private final SecCurrency defaultCurrency;
	
//	private final ExpiryFromListOfExpiryRules expiryLookup = new ExpiryFromListOfExpiryRules();
	private final ExpiryRuleInterface expiryLookup;// = new ExpiryFromListOfExpiryRules();
	
	private transient final Object getLock = new Object();
	
	@SuppressWarnings("unused")
	private SecDefQueryFromRules(){
		this.shortNameProcessor = null;
		this.validTypes=null;
		this.validSymbols = null;
		this.validContractMonths=null;
		this.defaultPrecision = 0;
		this.defaultMinTick = null;
		this.defaultMultiplier = null;
		this.defaultCurrency = null;
		this.defaultExchange=null;
		this.expiryLookup=null;
	}
	
	public SecDefQueryFromRules(String fieldSeparator,
			Set<SecSymbolType> validTypes,
			Set<String> validSymbols, 
			List<Integer> validContractMonths,
			SecCurrency defaultCurrency,
			SecExchange defaultExchange, int defaultPrecision,
			BigDecimal defaultMinTick, BigDecimal defaultSize,ExpiryRuleInterface expiryLookup) {
		super();
		this.shortNameProcessor = new ShortNameProcessor(fieldSeparator,validSymbols);
		this.validTypes = validTypes;
		this.validSymbols = validSymbols;
		this.validContractMonths = validContractMonths;
		this.defaultPrecision = defaultPrecision;
		this.defaultMinTick = defaultMinTick;
		this.defaultMultiplier = defaultSize;
		this.defaultCurrency = defaultCurrency;
		this.defaultExchange=defaultExchange;
		this.expiryLookup = expiryLookup;
	}

//203 557 9007

	@Override
	public SecDef get(String shortName, int timeoutValue, TimeUnit timeUnitType) {
		synchronized (getLock) {
			if (cache.contains(shortName))
				return cache.get(shortName);
			ShortNameInfo sni = this.shortNameProcessor.getShortNameInfo(shortName);
			// is shortName valid??
			if(sni==null)return null;
			// accept all STK.SMART
			if((sni.getSymbolType()==SecSymbolType.STK || sni.getSymbolType()==SecSymbolType.OPT) && sni.getExchange()==SecExchange.SMART){
				if(!validateStkSmart(sni.getSymbol())){
					return null;
				}
			}else{
				if(!validateSymbol(sni.getSymbol()))return null;
			}
			if (!validTypes.contains(sni.getSymbolType())){
				return null;
			}
		
			// is contractMonth valid??
			Integer contractMonth = new Integer(sni.getContractMonth());
			if(validContractMonths!=null && !validContractMonths.contains(contractMonth)){
				return null;
			}
			Calendar expiry = this.expiryLookup.getExpiry(shortName);
			int expiryYear = 0;
			int expiryMonth = 0;
			int expiryDay = 0;
			if(expiry!=null){
				expiryYear = expiry.get(Calendar.YEAR);
				expiryMonth = expiry.get(Calendar.MONTH)+1;
				expiryDay = expiry.get(Calendar.DAY_OF_MONTH);
			}
			
			// make sure exchange and currency are the same as the default
			
			// buile the SecDef
			SecDefSimple ret = new SecDefSimple(shortName, sni, sni.getSymbol(), 
					defaultPrecision, defaultMinTick, expiryYear, expiryMonth, expiryDay,
					defaultMultiplier, sni.getExchange());
			// cache it
			cache.put(shortName, ret);
			// return it
			return ret;
		}
	}
	
	protected boolean validateSymbol(String key){
		if(validSymbols==null)return true;
		return validSymbols.contains(key);
	}
	
	protected boolean validateStkSmart(String key){
		return true;
	}

}
