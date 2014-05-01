package com.billybyte.dse.queries;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.marketdata.SecDef;

public class MarJunSepDecSerialOptUnderQuery extends BaseUnderlyingSecDefQuery{
	QueryInterface<String, SecDef> query ;
	String normRegex = "\\.USD\\.201.((0[369])|(12))";
	Pattern normP = Pattern.compile(normRegex);
	
	String q1Regex = "\\.USD\\.201.0[12]";
	Pattern q1p = Pattern.compile(q1Regex);
	String q1Replace = "\\.USD\\.201.03";
	String q2Regex = "\\.USD\\.201.0[45]";
	Pattern q2p = Pattern.compile(q2Regex);
	String q2Replace = "\\.USD\\.201.06";
	String q3Regex = "\\.USD\\.201.0[78]";
	Pattern q3p = Pattern.compile(q3Regex);
	String q3Replace = "\\.USD\\.201.09";
	String q4Regex = "\\.USD\\.201.1[01]";
	Pattern q4p = Pattern.compile(q4Regex);
	String q4Replace = "\\.USD\\.201.12";

	public MarJunSepDecSerialOptUnderQuery(QueryInterface<String, SecDef> sdQuery) {
		super(sdQuery);
		this.query = sdQuery;
	}

	@Override
	public List<SecDef> get(String key, int timeoutValue,
			TimeUnit timeUnitType) {
		List<String> results = RegexMethods.getRegexMatches(normP, key);
		if(results !=null && results.size()>0){
			return super.get(key, timeoutValue, timeUnitType);
		}
		
		results = RegexMethods.getRegexMatches(q1p, key);
		if(results !=null && results.size()>0){
			List<SecDef> sdsFromBase = super.get(key, timeoutValue, timeUnitType);
			if(sdsFromBase == null || sdsFromBase.size()<1)return null;
			String old = results.get(0);
			String replace = old.substring(0,old.length()-1) + "3";
			String newKey = sdsFromBase.get(0).getShortName().replaceFirst(results.get(0), replace);
			SecDef sd = query.get(newKey, timeoutValue, timeUnitType);
			return CollectionsStaticMethods.listFromArray(new SecDef[]{sd});
		}
		
		results = RegexMethods.getRegexMatches(q2p, key);
		if(results !=null && results.size()>0){
			List<SecDef> sdsFromBase = super.get(key, timeoutValue, timeUnitType);
			if(sdsFromBase == null || sdsFromBase.size()<1)return null;
			String old = results.get(0);
			String replace = old.substring(0,old.length()-1) + "6";
			String newKey = sdsFromBase.get(0).getShortName().replaceFirst(results.get(0), replace);
			SecDef sd = query.get(newKey, timeoutValue, timeUnitType);
			return CollectionsStaticMethods.listFromArray(new SecDef[]{sd});
		}
		
		results = RegexMethods.getRegexMatches(q3p, key);
		if(results !=null && results.size()>0){
			List<SecDef> sdsFromBase = super.get(key, timeoutValue, timeUnitType);
			if(sdsFromBase == null || sdsFromBase.size()<1)return null;
			String old = results.get(0);
			String replace = old.substring(0,old.length()-1) + "9";
			String newKey = sdsFromBase.get(0).getShortName().replaceFirst(results.get(0), replace);
			SecDef sd = query.get(newKey, timeoutValue, timeUnitType);
			return CollectionsStaticMethods.listFromArray(new SecDef[]{sd});
		}

		results = RegexMethods.getRegexMatches(q4p, key);
		if(results !=null && results.size()>0){
			List<SecDef> sdsFromBase = super.get(key, timeoutValue, timeUnitType);
			if(sdsFromBase == null || sdsFromBase.size()<1)return null;
			String old = results.get(0);
			String replace = old.substring(0,old.length()-2) + "12";
			String newKey = sdsFromBase.get(0).getShortName().replaceFirst(results.get(0), replace);
			SecDef sd = query.get(newKey, timeoutValue, timeUnitType);
			return CollectionsStaticMethods.listFromArray(new SecDef[]{sd});
		}
		
		return null;
	}
	
}
