package com.billybyte.clientserver.webserver;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;

public class RunTestWebServiceServer {
	static class TestClass{
		String stringData;
		List<Integer> intList;
	}
	
	public static void main(String[] args) {
		// create a query
		
		QueryInterface<String,TestClass> testQuery = 
				new QueryInterface<String, RunTestWebServiceServer.TestClass>() {
					
					@Override
					public TestClass get(String key, int timeoutValue, TimeUnit timeUnitType) {
						TestClass tc = new TestClass();
						tc.stringData = key;
						Integer num = new Integer(key)+10;
						tc.intList = new ArrayList<Integer>();
						tc.intList.add(num);
						return tc;
					}
		};
		// 
		ServiceBlock sb = new Args(args).sb;
		WebServiceServer<String, TestClass> wss  = WebServiceComLib.startServer(sb, testQuery);
		wss.publishService();
		
	}
	
	private static class Args {
		private final String urlOfApplicationServer;
		private final Integer portOfService;
		private final String urlOfService;
		private final String nameOfService;
		private final ServiceBlock sb;
		private Args(String[] args) {
			super();
			Map<String, String> argpairs = 
					Utils.getArgPairsSeparatedByChar(args, "=");
			this.urlOfApplicationServer = 
					argpairs.get("urlOfApplicationServer")==null ? null : argpairs.get("urlOfApplicationServer");
			this.portOfService = 
					argpairs.get("portOfService")==null ? 7000: new Integer(argpairs.get("portOfService"));
			this.urlOfService = 
					argpairs.get("urlOfService")==null ? "http://127.0.0.1" : argpairs.get("urlOfService");
			this.nameOfService = 
					argpairs.get("nameOfService")==null ? "TestService" : argpairs.get("nameOfService");
			this.sb = new ServiceBlock(urlOfApplicationServer, portOfService, urlOfService, nameOfService);
			Utils.prtObMess(RunTestWebServiceClient.class, "portOfService:" + urlOfApplicationServer);
			Utils.prtObMess(RunTestWebServiceClient.class, "urlOfApplicationServer:" + portOfService);
			Utils.prtObMess(RunTestWebServiceClient.class, "urlOfService:" + urlOfService);
			Utils.prtObMess(RunTestWebServiceClient.class, "nameOfService:" + nameOfService);
			
		}
		
	}
	
}
