package com.billybyte.marketdata;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;


/**
 * Sample code to use Yahoo! Search BOSS
 * 
 * Please include the following libraries 
 * 1. Apache Log4j
 * 2. oAuth Signpost
 * 
 * @author xyz
 */
public class SignPostExample {

	private static final Logger log = Logger.getLogger(SignPostExample.class);
	private static final String yahooServer = "http://query.yahooapis.com/v1/public/yql?q=select%20option%20from%20yahoo.finance.options%20where%20symbol%20in%20(%22A%22,%22AA%22,%22AAPL%22,%22ABC%22,%22ABT%22,%22ADBE%22,%22ADI%22,%22ADM%22,%22ADP%22,%22ADSK%22,%22AEE%22,%22AEP%22,%22AES%22,%22AET%22,%22AFL%22,%22AGN%22,%22AIG%22,%22AIV%22,%22AIZ%22,%22AKAM%22,%22AKS%22,%22ALL%22,%22ALTR%22,%22AMAT%22,%22AMD%22)%20AND%20expiration=%222014-04%22&env=store://datatables.org/alltableswithkeys";
	private static final String yahooServerNonPublic =
		"http://query.yahooapis.com/v1/yql/yql?q=select%20option%20from%20yahoo.finance.options%20where%20symbol%20in%20(%22A%22,%22AA%22,%22AAPL%22,%22ABC%22,%22ABT%22,%22ADBE%22,%22ADI%22,%22ADM%22,%22ADP%22,%22ADSK%22,%22AEE%22,%22AEP%22,%22AES%22,%22AET%22,%22AFL%22,%22AGN%22,%22AIG%22,%22AIV%22,%22AIZ%22,%22AKAM%22,%22AKS%22,%22ALL%22,%22ALTR%22,%22AMAT%22,%22AMD%22)%20AND%20expiration=%222014-05%22&env=store://datatables.org/alltableswithkeys";
	//protected static String yahooServer = "http://yboss.yahooapis.com/ysearch/";
	//dj0yJmk9TXZJUm1sTUZGTm9VJmQ9WVdrOWVFZzVWalF3TkcwbWNHbzlNVFUyTVRJMk5qazJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD0wYw--
	//79f1b802b1197298e1692d698f054ab821f00223
	
	// Please provide your consumer key here
	private static String consumer_key = "dj0yJmk9TXZJUm1sTUZGTm9VJmQ9WVdrOWVFZzVWalF3TkcwbWNHbzlNVFUyTVRJMk5qazJNZy0tJnM9Y29uc3VtZXJzZWNyZXQmeD0wYw--";
	
	// Please provide your consumer secret here
	private static String consumer_secret = "79f1b802b1197298e1692d698f054ab821f00223";
	
	/** The HTTP request object used for the connection */
	private static StHttpRequest httpRequest = new StHttpRequest();
	
	/** Encode Format */
	private static final String ENCODE_FORMAT = "UTF-8";
	
	/** Call Type */
	private static final String callType = "web";
	
	private static final int HTTP_STATUS_OK = 200;
	
	/**
	 * 
	 * @return
	 */
	public int returnHttpData() 
	throws UnsupportedEncodingException, 
	Exception{
	
		
		if(this.isConsumerKeyExists() && this.isConsumerSecretExists()) {
			
			// Start with call Type
			String params = callType;
			
			// Add query
			params = params.concat("?q=");
			
			// Encode Query string before concatenating
			params = params.concat(URLEncoder.encode(this.getSearchString(), "UTF-8"));
			
			// Create final URL
			//String url = yahooServer ;
			String url = yahooServerNonPublic ;
			
			// Create oAuth Consumer 
			OAuthConsumer consumer = new DefaultOAuthConsumer(consumer_key, consumer_secret);
			
			// Set the HTTP request correctly
			httpRequest.setOAuthConsumer(consumer);
			
			try {
				log.info("sending get request to" + URLDecoder.decode(url, ENCODE_FORMAT));
				int responseCode = httpRequest.sendGetRequest(url); 
				
				// Send the request
				if(responseCode == HTTP_STATUS_OK) {
					log.info("Response ");
				} else {
					log.error("Error in response due to status code = " + responseCode);
				}
				log.info(httpRequest.getResponseBody());
				List<String> ret = parse(httpRequest.getResponseBody());
				for(String s:ret){
					log.info(s);
				}
			} catch(UnsupportedEncodingException e) {
				log.error("Encoding/Decording error");
			} catch (IOException e) {
				log.error("Error with HTTP IO", e);
			} catch (Exception e) {
				log.error(httpRequest.getResponseBody(), e);
				return 0;
			}
			
			
		} else {
			log.error("Key/Secret does not exist");
		}
		return 1;
	}
	
	private String getSearchString() {
		return "Yahoo";
	}
	
	private boolean isConsumerKeyExists() {
		if(consumer_key.isEmpty()) {
			log.error("Consumer Key is missing. Please provide the key");
			return false;
		}
		return true;
	}
	
	private boolean isConsumerSecretExists() {
		if(consumer_secret.isEmpty()) {
			log.error("Consumer Secret is missing. Please provide the key");
			return false;
		}
		return true;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	
	
	BasicConfigurator.configure();
	
	try {
	
		SignPostExample signPostTest = new SignPostExample();
		
		signPostTest.returnHttpData();
		
		} catch (Exception e) {
		log.info("Error", e);
		}
	}
	
	static List<String> parse(String response){
		List<String>  ret = new ArrayList<String>();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder=null;
		Document doc = null;
		try {
			dBuilder = dbFactory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(response.getBytes());
			if(is==null)return null;
			doc = dBuilder.parse(is);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();

		// Get the document's root XML node
		NodeList root = doc.getChildNodes();

		// Navigate down the hierarchy to get to the CEO node
		Node query = getNode("query", root);
		Node results = getNode("results", query.getChildNodes() );
		NodeList chains =results.getChildNodes();
		for(int j = 0;j<chains.getLength();j++){
			Node optionsChain = chains.item(j);
			NodeList nodes = optionsChain.getChildNodes();
			
			for(int i = 0;i<nodes.getLength();i++){
				Node quote = nodes.item(i);
			    String yahooSymbol = getNodeAttr("symbol", quote);

			    String bid = getNodeValue("bid", quote.getChildNodes());
			    String ask = getNodeValue("ask", quote.getChildNodes());
			    String last = getNodeValue("lastPrice", quote.getChildNodes());
			    String change = getNodeValue("change", quote.getChildNodes());

				ret.add(yahooSymbol+","+bid+","+ask+","+last+","+change);
			}
		}
		return ret;
	}
	
	static Node getNode(String tagName, NodeList nodes) {
	    for ( int x = 0; x < nodes.getLength(); x++ ) {
	        Node node = nodes.item(x);
	        if (node.getNodeName().equalsIgnoreCase(tagName)) {
	            return node;
	        }
	    }
	 
	    return null;
	}
	 
	static String getNodeValue( Node node ) {
	    NodeList childNodes = node.getChildNodes();
	    for (int x = 0; x < childNodes.getLength(); x++ ) {
	        Node data = childNodes.item(x);
	        if ( data.getNodeType() == Node.TEXT_NODE )
	            return data.getNodeValue();
	    }
	    return "";
	}
	 
	static String getNodeValue(String tagName, NodeList nodes ) {
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
	 
	static String getNodeAttr(String attrName, Node node ) {
	    NamedNodeMap attrs = node.getAttributes();
	    for (int y = 0; y < attrs.getLength(); y++ ) {
	        Node attr = attrs.item(y);
	        if (attr.getNodeName().equalsIgnoreCase(attrName)) {
	            return attr.getNodeValue();
	        }
	    }
	    return "";
	}
	 
	static String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
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


}