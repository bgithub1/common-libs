package com.billybyte.marketdata.futures;

import java.util.Calendar;
import java.util.Map;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.ShortNameInfo;

public class ExpiryFromListOfExpiryRulesBySecSymbolType extends ExpiryFromListOfExpiryRules{

	public ExpiryFromListOfExpiryRulesBySecSymbolType(){
		doOpt();

	}
	
	@Override
	public Calendar getExpiry(String shortName) {
		ShortNameInfo sni =  sproc.getShortNameInfo(shortName);
		ExpiryRuleInterface rule;
		if(secSymbolTypesWithExpiries.contains(sni.getSymbolType())){
			Map<String,ExpiryRuleInterface> expiryMapPerType = expiryRulesMap.get(sni.getSymbolType());
			rule = expiryMapPerType.get(sni.getSymbolType().toString());
		}else{
			rule = noExpiryRule;
		}
		if(rule==null){
			Utils.prtObErrMess(this.getClass(), " can't find expiry rule for shortName: "+shortName);
			return null;
		}
		Calendar ret = rule.getExpiry(shortName);
		return ret;
	}

}
