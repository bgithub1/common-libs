package com.billybyte.clientserver.webserver;


import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.clientserver.webserver.RunTestWebServiceServer.TestClass;
import com.billybyte.commoninterfaces.QueryInterface;
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
		String urlOfApplicationServer = null;
		int portOfService = 7000;
		String urlOfService = "http://127.0.0.1";
		String nameOfService = "TestService";
		ServiceBlock sb = new ServiceBlock(urlOfApplicationServer, portOfService, urlOfService, nameOfService);

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
}
