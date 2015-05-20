package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.marketdata.futures.FuturesProduct;
import com.billybyte.marketdata.futures.FuturesProductQuery;

public class ShortNameProcessor implements ShortNameProcessorInterface{
	
	private final static FuturesProductQuery fpqStatic = 
			new FuturesProductQuery();

	final String fieldSeparator;
	final Set<String> productSet;
	final List<Pattern> regexPatterns;
	
	@SuppressWarnings("unused")
	private ShortNameProcessor(){
		this.fieldSeparator = null;
		this.productSet = null;
		this.regexPatterns=null;
	}
	
	public ShortNameProcessor(String fieldSeparator,
			Set<String> productSet) {
		super();
		this.fieldSeparator = fieldSeparator;
		this.productSet = productSet;
		// build parts of regex string
		String productPart = "[a-zA-Z0-9]{1,}";
		String stockProductPart = "[a-zA-Z0-9]{1,}(-[ABC]){0,1}(_[A-Z]{1,2}){0,1}";
		String stockTypPart = SecSymbolType.STK.toString();
		String futurePart = SecSymbolType.FUT.toString();
		String optionPart = buildPart(EnumSet.of(SecSymbolType.OPT).toArray());
		String fopPart = buildPart(EnumSet.of(SecSymbolType.FOP).toArray());
		String secExchPart = buildPart(SecExchange.values());
		String secCurrPart = buildPart(SecCurrency.values());
		String yearPart = "([19[7-9][0-9]]|20[0-9][0-9])";
		String monthPart = "(0[1-9]|1[0-2])";
		String dayPart = "(0[1-9]|[1-2][0-9]|3[0-1]){0,1}";
		String secRightPart="(P|C)";
		String strikePart = "(-{0,1}[0-9]*\\.{0,1}[0-9]*)";
		// build all possible regex strings (stock w/o currency, stock with currency, future, option)
		
		String stockNoCurrPattern = stockProductPart+this.fieldSeparator+
				stockTypPart+this.fieldSeparator+
				secExchPart;
		
		String stockCurrPatten = stockNoCurrPattern+this.fieldSeparator+secCurrPart;
		String futurePattern = 
			productPart+this.fieldSeparator+
			futurePart+this.fieldSeparator+
			secExchPart+this.fieldSeparator+
			secCurrPart+this.fieldSeparator+
			yearPart+monthPart;
		

		String optionPattern = 
				stockProductPart+this.fieldSeparator+
				optionPart+this.fieldSeparator+
				secExchPart+this.fieldSeparator+
				secCurrPart+this.fieldSeparator+
				yearPart+monthPart+dayPart+this.fieldSeparator+
				secRightPart+this.fieldSeparator+strikePart;
		String fopPattern = 
				productPart+this.fieldSeparator+
				fopPart+this.fieldSeparator+
				secExchPart+this.fieldSeparator+
				secCurrPart+this.fieldSeparator+
				yearPart+monthPart+this.fieldSeparator+
				secRightPart+this.fieldSeparator+strikePart;
		
		
		this.regexPatterns = new ArrayList<Pattern>();
		this.regexPatterns.add(Pattern.compile("^"+stockNoCurrPattern+"$"));
		this.regexPatterns.add(Pattern.compile("^"+stockCurrPatten+"$"));
		this.regexPatterns.add(Pattern.compile("^"+futurePattern+"$"));
		this.regexPatterns.add(Pattern.compile("^"+optionPattern+"$"));
		this.regexPatterns.add(Pattern.compile("^"+fopPattern+"$"));
	}

	private String buildPart(Object[] values){
		String part = "(";
		for(Object value : values){
			part = part+value.toString()+"|";
		}
		// lop off last "|" character and replace it with ")"
		part = part.substring(0,part.length()-1)+")";
		return part;
	}
	@Override
	public ShortNameInfo getShortNameInfo(String shortName) {
		List<String> tokens=null;
		for(Pattern pattern : regexPatterns){
			Matcher matcher = pattern.matcher(shortName.trim());
			tokens = Utils.getRegexMatches(matcher);
			if(tokens!=null && tokens.size()>0){
				break;
			}
		}
		if(tokens!=null && tokens.size()>0){
			// if you get here, create a new ShortNameInfo object
			// now separate tokens
			List<String> fields = Arrays.asList(tokens.get(0).split("\\"+this.fieldSeparator));
			
			// first process required fields
			int tokensProcessed=0;
			String symbol = fields.get(tokensProcessed);
			// if the product needs to be validated against a productSet - do it here
			//    (ignore if productSet is null)
			if(productSet!=null&& productSet.size()>0){
				if(!productSet.contains(symbol)){
					return null;
				}
			}

			tokensProcessed++;
			SecSymbolType type = SecSymbolType.fromString(fields.get(tokensProcessed));
			tokensProcessed++;
			SecExchange exch = SecExchange.fromString(fields.get(tokensProcessed));
			if(type.equals(SecSymbolType.FUT) || type.equals(SecSymbolType.FOP)){
				if(exch!=null){
					FuturesProduct fp =	fpqStatic.get(symbol, 1, TimeUnit.SECONDS);
					if(fp==null)return null;
					if(!exch.equals(fp.getExchange())){
						return null;
					}
				}
			}
			tokensProcessed++;
			// set defaults
			SecCurrency curr=null;
			int cy = 0;
			int cm = 0;
			Integer cd = null;
			String right = null;
			BigDecimal strike = null;
			
			// do optional fields
			if(fields.size()>tokensProcessed){
				curr = SecCurrency.fromString(fields.get(tokensProcessed));
				tokensProcessed++;
			}

			if(fields.size()>tokensProcessed){
				String monthyear = fields.get(tokensProcessed);
				cy = new Integer(monthyear.substring(0,4));
				cm = new Integer(monthyear.substring(4,6));
				if(monthyear.length()>=8){
					cd = new Integer(monthyear.substring(6,8));
				}
				tokensProcessed++;
			}
			
			String strikeString=null;
			if(fields.size()>tokensProcessed){
				right = fields.get(tokensProcessed);
				tokensProcessed++;
				strikeString = fields.get(tokensProcessed);
				tokensProcessed++;
			}
			if(fields.size()>tokensProcessed){
				strikeString +="."+fields.get(tokensProcessed);
			}
			if(strikeString!=null){
				strike = new BigDecimal(strikeString);
			}
			ShortNameInfo ret = new ShortNameInfo(symbol,type,exch,curr,cy,cm,cd,right,strike);
			return ret;
		}
		
		return null;
	}

}
