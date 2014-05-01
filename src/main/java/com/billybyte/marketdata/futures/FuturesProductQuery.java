package com.billybyte.marketdata.futures;


import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;

public class FuturesProductQuery implements QueryInterface<String, FuturesProduct>{
	private static final String shortNameSep = MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR;
	private static  Map<String, FuturesProduct> futuresProductMap ;
	
	
	public FuturesProductQuery(){
		if(futuresProductMap==null){
			futuresProductMap= MarketDataComLib.getFuturesProductMapFromCsvFile();
		}
	}
	@Override
	public FuturesProduct get(String key, int timeoutValue,
			TimeUnit timeUnitType) {
		if(futuresProductMap.containsKey(key)){
			return futuresProductMap.get(key);
		}
		return null;
	}

	public static Map<String, FuturesProduct> getFuturesProductMap(){
		return futuresProductMap;
	}
	
	/**
	 * 
	 * @param sd
	 * @param timeoutValue
	 * @param timeUnitType
	 * @param regexBasedSettleQuery
	 * @return String
	 */
	public final String findBestOptionSymbolForFuturesAtmVol(
			SecDef sd,
			int timeoutValue,
			TimeUnit timeUnitType,
			QueryInterface<String, Map<String, SettlementDataInterface>> regexBasedSettleQuery
			){
		if(sd == null )return null;
		String sym = sd.getSymbol();
		if(this.get(sym, timeoutValue, timeUnitType) == null) return null;
		// see if there is an options product that has this futures sym as it's underlying sym
		TreeMap<Integer,String> strikesInSettleDbToSymbol = new TreeMap<Integer, String>();
		for(FuturesProduct fp : getFuturesProductMap().values()){
			if(!fp.getValidTypes().contains(SecSymbolType.FOP)) continue;
			String underSym = fp.getUnderlyingSymbol();
			if(underSym.compareTo(sym)!=0)continue;
			SecExchange exch = fp.getExchange();
			if(exch == null)continue;
			SecCurrency curr = fp.getCurrency();
			if(curr == null) continue;
			String thisSym = fp.getPrimarySymbol();
			String partial = thisSym + shortNameSep + 
					SecSymbolType.FOP.toString() + shortNameSep +
					exch.toString() + shortNameSep +
					curr.toString();
			Map<String, SettlementDataInterface> setMap = 
					regexBasedSettleQuery.get(partial,timeoutValue,timeUnitType);
			if(setMap == null || setMap.size()<1)continue;
			Integer size = setMap.size();
			strikesInSettleDbToSymbol.put(size,thisSym);
		}
		if(strikesInSettleDbToSymbol.size()<1)return null;
		return strikesInSettleDbToSymbol.lastEntry().getValue();
	}

}
