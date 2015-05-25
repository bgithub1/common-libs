package com.billybyte.clientserver.webserver;


import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.clientserver.webserver.RunTestWebServiceServer.TestClass;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.ui.messagerboxes.MessageBox;
import com.thoughtworks.xstream.XStream;

public class RunTestWebServiceClient {
	/**
	 * Only run this main AFTER you have run the main in RunTestWebServiceServer.java
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		ServiceBlock sb = new Args(args).sb;

		QueryInterface<String,TestClass> clientQuery = WebServiceComLib.getQueryService(sb, new XStream());
		// loop to get some stuff
		for(int i = 0;i<3;i++){
			String response = 
					MessageBox.MessageBoxNoChoices("enter a your age", "59");
			TestClass tc = clientQuery.get(response, 1, TimeUnit.SECONDS);
			Utils.prt("In 10 years, you'll be "+tc.intList.get(0));
		}
		Utils.prt("Exiting Program.  Don't forget to kill the process "+RunTestWebServiceServer.class.getCanonicalName());
		System.exit(0);
		
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
