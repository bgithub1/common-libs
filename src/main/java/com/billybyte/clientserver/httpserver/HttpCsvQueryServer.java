package com.billybyte.clientserver.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
/**
 * Generalized http server which calls a QueryInterface<String,List<String[]>
 *   instance to get its csv data, and then sends it back to the http caller
 * @author bill perlman
 *
 */
public class HttpCsvQueryServer {
	private final QueryInterface<String, List<String[]>> csvQuery;
	private final int httpPort;
	private final HttpServer server;
	private final String httpPath;
	private final int timeoutValue;
	private final TimeUnit timeUnitType;

	/**
	 * 
	 * @param httpPort int
	 * @param httpPath String
	 * @param csvQuery QueryInterface<String, List<String[]>>
	 * @param timeoutValue
	 * @param timeUnitType
	 * @throws IOException
	 */
	public HttpCsvQueryServer(int httpPort, String httpPath,
			QueryInterface<String, List<String[]>> csvQuery,
			int timeoutValue,
			TimeUnit timeUnitType) throws IOException {
		super();
		this.timeoutValue = timeoutValue;
		this.timeUnitType = timeUnitType;
		this.httpPort = httpPort;
		this.httpPath = httpPath;
		this.csvQuery = csvQuery;
		server = HttpServer.create(new InetSocketAddress(httpPort), 0);
        server.createContext(httpPath, new MyHandler());
        server.setExecutor(null); // creates a default executor

	}
	
	public void start(){
		server.start();
	}
	
    private class MyHandler implements HttpHandler {

    	public void handle(HttpExchange t) throws IOException {
    		String q = t.getRequestURI().getQuery();
    		String response = "";
    		List<String[]> csvList = 
    				csvQuery.get(q,timeoutValue,timeUnitType);
    		// turn list of csv into a string
    		for(String[] csvLine: csvList){
    			String line = "";
    			for(String token: csvLine){
    				line += token+",";
    			}
    			line = line.substring(0,line.length()-1);
    			response += line + "\n";
    		}
    		
			t.sendResponseHeaders(200,response.length());
			OutputStream os=t.getResponseBody();
			Utils.prt(response);
			os.write(response.getBytes());
			os.close();
			
        }
    }

	public int getHttpPort() {
		return httpPort;
	}

	public String getHttpPath() {
		return httpPath;
	}

	/**
	 * test HttpCsvQueryServer
	 * @param args
	 */
    public static void main(String[] args) {
    	int httpPort = 8888;
    	String httpPath = "/dummyCrude";
    	QueryInterface<String, List<String[]>> csvQuery = 
    			new TestQuery();
    	int timeoutValue = 1;
    	TimeUnit timeUnitType =  TimeUnit.SECONDS;
		try {
			HttpCsvQueryServer csvs = 
					new HttpCsvQueryServer(
							httpPort, httpPath, 
							csvQuery, timeoutValue, timeUnitType);
			csvs.start();
			Utils.prtObErrMess(HttpCsvQueryServer.class, "server started on port 8888.");
			Utils.prtObErrMess(HttpCsvQueryServer.class, "Enter http://127.0.0.1:8888/dummyCrude?p1=data to test");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    private static final class TestQuery 
    	implements QueryInterface<String, List<String[]>> {
    	
    	private final String[][] dummyCsv = {
        		{"shortName","bid","bidsize","ask","asksize"},
        		{"CL.FUT.NYMEX.USD.201601","65.25","100","65.30","105"},
        		{"CL.FUT.NYMEX.USD.201602","66.25","100","66.30","105"},
        		{"CL.FUT.NYMEX.USD.201603","67.25","100","67.30","105"},
        		{"CL.FUT.NYMEX.USD.201604","68.25","100","68.30","105"},
        		{"CL.FUT.NYMEX.USD.201605","69.25","100","69.30","105"},

    	};
    	private final String[][] badRet = {{"bad key"}};
    	
    	
		@Override
		public List<String[]> get(String key, int timeoutValue,
				TimeUnit timeUnitType) {
			String[] tokens = key.split("=");
			
			if(tokens.length>1 && tokens[1].compareTo("data")==0){
				List<String[]> ret = 
						Arrays.asList(dummyCsv);
				return ret;
			}
			List<String[]> ret = 
					Arrays.asList(badRet);
			return ret;
		}
		
    
    }
    
  
}
