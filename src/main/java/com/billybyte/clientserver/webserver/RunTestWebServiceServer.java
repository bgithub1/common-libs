package com.billybyte.clientserver.webserver;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.ui.messagerboxes.MessageBox;
import com.thoughtworks.xstream.XStream;

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
		// use TestQuery
		String urlOfApplicationServer = null;
		int portOfService = 7000;
		String urlOfService = "http://127.0.0.1";
		String nameOfService = "TestService";
		ServiceBlock sb = new ServiceBlock(urlOfApplicationServer, portOfService, urlOfService, nameOfService);
		WebServiceServer<String, TestClass> wss  = WebServiceComLib.startServer(sb, testQuery);
		wss.publishService();
		
	}
}
