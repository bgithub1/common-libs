package com.billybyte.marketdata.futures;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;

public class GmiShortNameConverter {
	private final FuturesProductQuery fpq = new FuturesProductQuery();
	private static Map<String,String> gmiExchAndProdToPartialSnMap ;
	private static final String OPTION_PREFIX_TO_GMI_KEY = "O";
	@SuppressWarnings("unchecked")
	public GmiShortNameConverter(){
		if(gmiExchAndProdToPartialSnMap==null){
			gmiExchAndProdToPartialSnMap = Utils.getXmlData(Map.class, GmiShortNameConverter.class,  "gmiProductConversionMap.xml");
		}
	}
	public String getShortName(
			String gmi2DigitExchCode,
			String gmiProductCode,
			String yyyyMm,
			String gmiRight,
			BigDecimal gmiStrike){
		
		String key = gmi2DigitExchCode+gmiProductCode;
		String right = null;
		if(gmiRight!=null){
			if(gmiStrike==null)return null;
			key = OPTION_PREFIX_TO_GMI_KEY+key;
			right = gmiRight.toUpperCase();
		}
		if(!gmiExchAndProdToPartialSnMap.containsKey(key)){
			return null;
		}
		String partialSn = gmiExchAndProdToPartialSnMap.get(key);
		// need to add
		partialSn = partialSn + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR + yyyyMm;
		// is this an option
		if(gmiRight==null){
			return partialSn;
		}
		
		String sysProd = partialSn.split("\\" +MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR)[0];
		FuturesProduct fp = fpq.get(sysProd, 1, TimeUnit.SECONDS);
		if(fpq==null)return null;
		int prec = fp.getExchangePrecision();
		
		BigDecimal strike = gmiStrike.setScale(prec,RoundingMode.HALF_EVEN); 
		partialSn = partialSn + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR + 
				right + MarketDataComLib.DEFAULT_SHORTNAME_SEPARATOR + strike.toString();
		return partialSn;
	}
	
}
