package com.billybyte.marketdata;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

//import signpost.StHttpRequest;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
//import com.billybyte.commonstaticmethods.MarketDataComLib;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.PriceDisplayImmute;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefSimple;
import com.billybyte.marketdata.SecEnums.SecCurrency;
import com.billybyte.marketdata.ShortNameInfo;
import com.billybyte.marketdata.ShortNameProcessor;
import com.billybyte.marketdata.SecEnums.SecExchange;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Get OPTION data from Yql and make PDI's
 * ONLY DOES OPT'S
 * implements QueryInterface<Set<String>,Map<String, 
 * 		ComplexQueryResult<PriceDisplayInterface>>>
 * @author bperlman1
 *
 */
public class YqlOptionPdiQuery implements QueryInterface<Set<String>,Map<String, ComplexQueryResult<PriceDisplayInterface>>> {
	private static final String CENTURY_PREFIX = "20";
	private static final int YAHOO_STRIKE_PREC = 3;
	private static final int SYSTEM_STRIKE_PREC = 2;
	private static final int YAHOO_YYMMDD_LENGTH = 6;
//	private static final int YAHOO_STRIKE_LENGTH = 8;
	private static final int SYSTEM_MONTHYEAR_LENGTH = 6;
	private static final int MAX_GETS_BEFORE_WAIT = 25;
	private static final int SLEEP_TIME_BETWEEN_YQL_QUERIES = 25;
//	private String yqlOptionChain = 
//			"http://query.yahooapis.com/v1/public/yql?q=select%20option%20from%20yahoo.finance.options%20where%20symbol%20in%20(%2222STOCK_NAME%22)%20AND%20expiration=%22CONTRACT_YEAR-CONTRACT_MONTH%22&env=store://datatables.org/alltableswithkeys";
	private final String specificOption = 
			"http://query.yahooapis.com/v1/public/yql?q=select%20option%20from%20yahoo.finance.options%20where%20symbol%20in%20(%22STOCK_NAME%22)%20AND%20expiration=%22CONTRACT_YEAR-CONTRACT_MONTH%22%20AND%20option.type=%22PORC%22%20and%20option.strikePrice=%22OPTION_STRIKE%22&env=store://datatables.org/alltableswithkeys";
		
//	private String specificeOption2 = 
//			"http://query.yahooapis.com/v1/public/yql?q=select%20option%20from%20yahoo.finance.options%20where%20symbol%20in%20(%22IBM%22)%20AND%20expiration=%222013-04%22%20AND%20option.type=%22C%22%20and%20option.strikePrice=%22220%22&env=store://datatables.org/alltableswithkeys";
	// replacement fields for above specificOption string
	static final String STOCK_NAME = "STOCK_NAME";
	static final String CONTRACT_YEAR = "CONTRACT_YEAR";
	static final String CONTRACT_MONTH = "CONTRACT_MONTH";
	static final String PORC = "PORC";
	static final String OPTION_STRIKE = "OPTION_STRIKE";
//	private static final String STOCK_LIST = "STOCK_LIST";
	private final DecimalFormat monthFormat = new DecimalFormat("00");
	private final BigDecimal strikeMultiplier = new BigDecimal("1");
	private final int DEFAULT_PRECISION = 2;
	private final BigDecimal DEFAULT_MINTICK = new BigDecimal("0.01");

//	private final String optionRegex = "[a-zA-Z0-9]{1,8}[123][0-9][01][0-9][0123][0-9][CP][0-9]{8,8}";
	private final String optionRegexNoSymbol = "[123][0-9][01][0-9][0123][0-9][CP][0-9]{8,8}";
//	private final Pattern optionRegexPattern = Pattern.compile(optionRegex);
	private final Pattern optionRegexNoSymbolPattern = Pattern.compile(optionRegexNoSymbol);
	private final StHttpRequest request; // = new StHttpRequest(cons); // can this be a field variable?

	public YqlOptionPdiQuery(String consumerKey, String consumerSecret) {
		OAuthConsumer cons = new DefaultOAuthConsumer(consumerKey, consumerSecret);
		this.request = new StHttpRequest(cons);
	}

	public YqlOptionPdiQuery(){
		this.request = null;
	}
	
	/*
	 * 
<query xmlns:yahoo="http://www.yahooapis.com/v1/base.rng" yahoo:count="1" yahoo:created="2012-10-22T12:16:03Z" yahoo:lang="en-US">
<results>
<optionsChain>
<option symbol="IBM130119C00200000" type="C">
<strikePrice>200</strikePrice>
<lastPrice>3.70</lastPrice>
<change>0</change>
<changeDir/>
<bid>NaN</bid>
<ask>NaN</ask>
<vol>554</vol>
<openInt>9360</openInt>
</option>
</optionsChain>
</results>
</query>
	 */
	 
	// ...
	 
	Node getNode(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            return node;
	        }
	    }
	 
	    return null;
	}
	 
	String getNodeValue( Node node ) {
	    NodeList childNodes = node.getChildNodes();
	    for (int x = 0; x < childNodes.getLength(); x++ ) {
	        Node data = childNodes.item(x);
	        if ( data.getNodeType() == Node.TEXT_NODE )
	            return data.getNodeValue();
	    }
	    return "";
	}
	 
	String getNodeValue(String tagName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.TEXT_NODE )
	                    return data.getNodeValue();
	            }
	        }
	    }
	    return "";
	}
	 
	String getNodeAttr(String attrName, Node node ) {
	    NamedNodeMap attrs = node.getAttributes();
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	        Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    return "";
	}
	 
	String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            NodeList childNodes = node.getChildNodes();
	            for (int y = 0; y < childNodes.getLength(); y++ ) {
	                Node data = childNodes.item(y);
	                if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
	                    if ( data.getNodeName().equalsIgnoreCase(attrName) )
	                        return data.getNodeValue();
	                }
	            }
	        }
	    }
	 
	    return "";
	}
	
	/**
	 * Main parsing of xml using DocumentBuilderFactory
	 * @param keySet
	 * @return
	 */
	public Map<String, YqlInfo> parseIt(Set<String> keySet) {
	    Map<String, YqlInfo> ret = new HashMap<String, YqlOptionPdiQuery.YqlInfo>();
		try {
			Set<String> productSet = new TreeSet<String>();
			for(String key:keySet){
				productSet.add(key.split("\\.")[0]);
			}
			ShortNameProcessor snp = new ShortNameProcessor(".", productSet);
			// break up into 50's if necessary
			List<String> nameList = new ArrayList<String>(keySet);
//			String stockSymbolListCommaSep = "";
			for(int k = 0;k<keySet.size();k+=MAX_GETS_BEFORE_WAIT){
				for(int j = 0;j<MAX_GETS_BEFORE_WAIT;j++){
					int index = k + j;
					if(index>=nameList.size()) break;
					ShortNameInfo sni = snp.getShortNameInfo(nameList.get(index));
					String stockName = sni.getSymbol();
					int contractYear = sni.getContractYear();
					int contractMonth = sni.getContractMonth();
					String porc = sni.getRight();
					if(porc==null){
						continue;
					}
					
					BigDecimal optionStrike = sni.getStrike();
					if(optionStrike==null){
						continue;
					}
					
					optionStrike = optionStrike.multiply(strikeMultiplier).setScale(0);
					String url = specificOption.replace(STOCK_NAME, stockName);
					url = url.replace(CONTRACT_YEAR, new Integer(contractYear).toString());
					url = url.replace(CONTRACT_MONTH, monthFormat.format(new Integer(contractMonth)));
					url = url.replace(PORC, porc);
					String yqlUrlToFetch = url.replace(OPTION_STRIKE, optionStrike.toString());
							
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(Utils.getInputStreamFromHttpFtp(yqlUrlToFetch));
					doc.getDocumentElement().normalize();
				 
				    // Get the document's root XML node
				    NodeList root = doc.getChildNodes();
				 
				    // Navigate down the hierarchy to get to the CEO node
				    Node query = getNode("query", root);
				    Node results = getNode("results", query.getChildNodes() );
				    Node optionsChain = getNode("optionsChain", results.getChildNodes() );
				    NodeList nodes = optionsChain.getChildNodes();
				    
				    for(int i = 0;i<nodes.getLength();i++){
				    	Node quote = nodes.item(i);
					    String bid = getNodeValue("bid", quote.getChildNodes());
					    String ask = getNodeValue("ask", quote.getChildNodes());
					    String last = getNodeValue("lastPrice", quote.getChildNodes());
					    String change = getNodeValue("change", quote.getChildNodes());
					    BigDecimal bdLast = new BigDecimal(last);
					    BigDecimal bdChange = new BigDecimal(change);
					    
					    String close = bdLast.subtract(bdChange).toString();
					    String symbol = getNodeValue("Symbol", quote.getChildNodes());
				    	YqlInfo info = new YqlInfo(symbol, bid, ask, last, close);
				    	ret.put(sni.getShortName(), info);
				    }
					
				    Thread.sleep(SLEEP_TIME_BETWEEN_YQL_QUERIES);
					
				}
			}
		}
		catch ( Exception e ) {
		    e.printStackTrace();
		}
	    return ret;
	}
	
	Map<String, YqlInfo> fetchOptionData(String yqlUrlToFetch){
	    Map<String, YqlInfo> ret = new HashMap<String, YqlOptionPdiQuery.YqlInfo>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			InputStream is = null;
//			try {
//				is = Utils.getInputStreamFromHttpFtp(yqlUrlToFetch);
//			} catch (Exception e) {
//				Utils.prtObErrMess(this.getClass(), e.getMessage());
//			}

			// use oath or public
			if(this.request==null){
				try {
					is = Utils.getInputStreamFromHttpFtp(yqlUrlToFetch);
				} catch (Exception e) {
					Utils.prtObErrMess(this.getClass(), e.getMessage());
				}
			}else{
				String yqlUrlToFetchOath =  yqlUrlToFetch.replace("public", "yql");
				String response = null;
				try {
					String urlNoQuotes = yqlUrlToFetchOath.replace("\"", "%22");
//					int reqStatus = request.sendGetRequest(yqlUrlToFetchOath);
					int reqStatus = request.sendGetRequest(urlNoQuotes);
					if(reqStatus==200) {
						response = request.getResponseBody();
					}
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				} catch (Exception e){
					// all others
					e.printStackTrace();
				}
				
				if(response==null)return null;
				is = new ByteArrayInputStream(response.getBytes());
			}
			if(is==null)return null;
			Document doc = dBuilder.parse(is);
			doc.getDocumentElement().normalize();
 
			// Get the document's root XML node
			NodeList root = doc.getChildNodes();
 
			// Navigate down the hierarchy to get to the CEO node
			Node query = getNode("query", root);
			Node results = getNode("results", query.getChildNodes() );
			NodeList chains =results.getChildNodes();
			if(chains.getLength()<1){
				Utils.prtObErrMess(this.getClass(), "no chains returned for query : " + yqlUrlToFetch);
				return null;
			}
			for(int j = 0;j<chains.getLength();j++){
				Node optionsChain = chains.item(j);
				NodeList nodes = optionsChain.getChildNodes();
				
				for(int i = 0;i<nodes.getLength();i++){
					Node quote = nodes.item(i);
				    String bid = getNodeValue("bid", quote.getChildNodes());
				    String ask = getNodeValue("ask", quote.getChildNodes());
				    String last = getNodeValue("lastPrice", quote.getChildNodes());
				    String change = getNodeValue("change", quote.getChildNodes());
				   
				    BigDecimal bdLast = createNumber(last);// new BigDecimal(last);
				    BigDecimal bdChange = createNumber(change);//new BigDecimal(change);
				    String close = null;
				    if(bdLast != null && bdChange != null){
				    	close = bdLast.subtract(bdChange).toString(); 	
				    }
				   
				    String yahooSymbol = getNodeAttr("symbol", quote);
				    SecDef sd = optionSecDefFromYahooSymbol(yahooSymbol);
				    if(sd==null){
				    	Utils.prtObErrMess(this.getClass(), yahooSymbol + 
				    			" cannot get SecDef from optionSecDefFromYahooSymbol");
				    }else{
						YqlInfo info = new YqlInfo(yahooSymbol, bid, ask, last, close,sd);
						ret.put(sd.getShortName(), info);
				    }
				}
			}
//			Node optionsChain = getNode("optionsChain", results.getChildNodes() );
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return ret.size()<1 ? null : ret;
	}
	
	BigDecimal createNumber(String numberString){
		if(!RegexMethods.isNumber(numberString))return null;
		try {
			return new BigDecimal(numberString);
		} catch (Exception e) {

		}
		return null;
	}
	
	SecDef optionSecDefFromYahooSymbol(String yahooSymbol){
		// options symbol
		Matcher matcher = optionRegexNoSymbolPattern.matcher(yahooSymbol);
		if(matcher == null)return null;
		List<String> list = RegexMethods.getRegexMatches(optionRegexNoSymbolPattern,yahooSymbol);
		if(list==null || list.size()<1)return null;
		
		List<Integer> indexList = RegexMethods.getRegexMatchesIndices(optionRegexNoSymbolPattern,yahooSymbol);
		int indexAfterSymbol = indexList.get(0);
		// get symbol
		String symbol = yahooSymbol.substring(0,indexAfterSymbol);
		// get monthYear
		String yyyyMmDd = CENTURY_PREFIX + yahooSymbol.substring(
				indexAfterSymbol,indexAfterSymbol+YAHOO_YYMMDD_LENGTH);
		String yyyyMm = yyyyMmDd.substring(0,SYSTEM_MONTHYEAR_LENGTH);
		if(yyyyMm.length()!=SYSTEM_MONTHYEAR_LENGTH || 
				!RegexMethods.isNumber(yyyyMm))return null;
		int contractYear = new Integer(yyyyMm.substring(0,4));
		int contractMonth = new Integer(yyyyMm.substring(4,6)) ;
		int expiryDay = new Integer(
				yyyyMmDd.substring(SYSTEM_MONTHYEAR_LENGTH,yyyyMmDd.length())) - 1;
		
		// get right
		String right =  yahooSymbol.substring(
				indexAfterSymbol+YAHOO_YYMMDD_LENGTH,
				indexAfterSymbol+YAHOO_YYMMDD_LENGTH+1);
		// get strike
		String strikeString = yahooSymbol.substring(
				indexAfterSymbol+YAHOO_YYMMDD_LENGTH+1,
				yahooSymbol.length());
		if(!RegexMethods.isNumber(strikeString))return null;
		// get rid of precision difference
		strikeString = strikeString.substring(0,
				strikeString.length()-(YAHOO_STRIKE_PREC - SYSTEM_STRIKE_PREC));
		// add decimal point
		strikeString = strikeString.substring(0,
				strikeString.length() - SYSTEM_STRIKE_PREC) + 
				"." + 
				strikeString.substring(strikeString.length() - SYSTEM_STRIKE_PREC,
						strikeString.length());
		BigDecimal strike = new BigDecimal(strikeString).setScale(SYSTEM_STRIKE_PREC);
		
		ShortNameInfo sni = 
				new ShortNameInfo(symbol, SecSymbolType.OPT, 
						SecExchange.SMART, SecCurrency.USD, 
						contractYear, contractMonth, 
						null,right, strike);
		
		String shortName = sni.getShortName();
		if(shortName == null) return null;
		
		SecDef sd = 
		new SecDefSimple(
				shortName,
				sni, 
				sni.getExchange().toString(), 
				DEFAULT_PRECISION,
				DEFAULT_MINTICK, 
				sni.getContractYear(), 
				sni.getContractMonth(), 
				expiryDay,
				BigDecimal.ONE, SecExchange.SMART);

		return sd;
	}
	
	static class YqlInfo{
		String symbol;
		String bid;
		String ask;
		String last;
		String close;
		SecDef sd;
		public YqlInfo(String symbol, String bid, String ask, String last,
				String close) {
			super();
			this.symbol = symbol;
			this.bid = bid;
			this.ask = ask;
			this.last = last;
			this.close = close;
		}
		
		public YqlInfo(String symbol, String bid, String ask, String last,
				String close,SecDef sd) {
			super();
			this.symbol = symbol;
			this.bid = bid;
			this.ask = ask;
			this.last = last;
			this.close = close;
			this.sd = sd;
		}

		@Override
		public String toString() {
			return symbol + ", " + bid + ", " + ask + ", " + last + ", "
					+ close + ", " + sd.toString();
		}
		
	}
	

	@Override
	public Map<String, ComplexQueryResult<PriceDisplayInterface>> get(
			Set<String> keySet, int timeoutValue, TimeUnit timeUnitType) {
		long yyyyMmDd = Dates.getYyyyMmDdFromCalendar(Calendar.getInstance());
		Map<String,ComplexQueryResult<PriceDisplayInterface>> ret = 
				new HashMap<String, ComplexQueryResult<PriceDisplayInterface>>();
		Map<String,YqlInfo> infoMap = parseIt(keySet);
		for(String key:keySet){
			if(!key.contains(".OPT."))continue;
			ComplexQueryResult<PriceDisplayInterface> cqr=null;
			if(!infoMap.containsKey(key)){
				cqr = 
						MarketDataComLib.errorRet(key+" No data returned From YQL");
				continue;
			}
			YqlInfo info = infoMap.get(key);
			BigDecimal bid = 
					(info.bid==null || info.bid.compareTo("  ")<=0 || info.bid.contains("Na")) ? null : 
						new BigDecimal(info.bid);
			BigDecimal offer = 
					(info.ask==null || info.ask.compareTo("  ")<=0 || info.ask.contains("Na")) ? null : 
						new BigDecimal(info.ask);
			BigDecimal last = 
					(info.last==null || info.last.compareTo("  ")<=0 || info.last.contains("Na")) ? null : 
						new BigDecimal(info.last);
			BigDecimal close = 
					(info.close==null || info.close.compareTo("  ")<=0 || info.close.contains("Na")) ? null : 
						new BigDecimal(info.close);
			String shortName = key;
			PriceDisplayImmute pdi = 
					new PriceDisplayImmute(shortName, bid, 1, offer, 1, last, 1, close, yyyyMmDd);
			cqr = 
					new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
			
			
			ret.put(shortName, cqr);
		}
		
		return ret;
	}
	
	/**
	 * 
	 * @param shortName
	 * @param info YqlInfo
	 * @return ComplexQueryResult<PriceDisplayInterface>
	 */
	static ComplexQueryResult<PriceDisplayInterface> getPdiCqr(
			String shortName,
			YqlInfo info,
			long yyyyMmDd){

		ComplexQueryResult<PriceDisplayInterface> cqr=null;
		BigDecimal bid = 
				(info.bid==null || info.bid.compareTo("  ")<=0 || info.bid.contains("Na")) ? null : 
					new BigDecimal(info.bid);
		BigDecimal offer = 
				(info.ask==null || info.ask.compareTo("  ")<=0 || info.ask.contains("Na")) ? null : 
					new BigDecimal(info.ask);
		BigDecimal last = 
				(info.last==null || info.last.compareTo("  ")<=0 || info.last.contains("Na")) ? null : 
					new BigDecimal(info.last);
		BigDecimal close = 
				(info.close==null || info.close.compareTo("  ")<=0 || info.close.contains("Na")) ? null : 
					new BigDecimal(info.close);
		PriceDisplayImmute pdi = 
				new PriceDisplayImmute(shortName, bid, 1, offer, 1, last, 1, close, yyyyMmDd);
		cqr = 
				new ComplexQueryResult<PriceDisplayInterface>(null, pdi);
		
		return cqr;
	}

}
