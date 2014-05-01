package com.billybyte.clientserver.webserver;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.simplewebservice.SimpleWebServicePublisher;
import com.thoughtworks.xstream.XStream;

public class WebServiceServer<K,T> extends SimpleWebServicePublisher<K, T>{
	private  String nameOfService=null;
	public WebServiceServer(ServiceBlock serviceSb,boolean isMultiThreadingWebService,
			XStream xstream,
			QueryInterface<K, T> queryEngine,int timeoutValue,TimeUnit timeUnitType){
		this(serviceSb.getUrlOfService(),
				isMultiThreadingWebService,serviceSb.getPortOfService(),xstream,
				queryEngine,timeoutValue,timeUnitType);
		this.nameOfService = serviceSb.getNameOfService();
	}
	
	public WebServiceServer(ServiceBlock serviceSb,
			QueryInterface<K, T> queryEngine,int timeoutValue,TimeUnit timeUnitType){
		this(serviceSb.getUrlOfService(),
				true,serviceSb.getPortOfService(),new XStream(),
				queryEngine,timeoutValue,timeUnitType);
		this.nameOfService = serviceSb.getNameOfService();
	}

	
	
	public WebServiceServer(String serverUrlBehindFirewall,
			boolean isMultiThreadingWebService, String portNum, XStream xstream,
			QueryInterface<K, T> queryEngine,int timeoutValue,TimeUnit timeUnitType/*,T nullReturn*/) {
		super(serverUrlBehindFirewall, isMultiThreadingWebService, portNum, xstream,
				new QueryEngineForWebService<K, T>(queryEngine,timeoutValue,timeUnitType)/*,nullReturn*/);
	}

	public WebServiceServer(String serverUrlBehindFirewall,
			boolean isMultiThreadingWebService, Integer portNum, XStream xstream,
			QueryInterface<K, T> queryEngine,int timeoutValue,TimeUnit timeUnitType/*,T nullReturn*/) {
		super(serverUrlBehindFirewall, isMultiThreadingWebService, portNum.toString(), xstream,
				new QueryEngineForWebService<K, T>(queryEngine,timeoutValue,timeUnitType)/*,nullReturn*/);
	
	}

	public static  boolean isStarted(String publicServerUrl,String portNum){
		String fullWsdlAddress = publicServerUrl+":"+portNum+"/simple?wsdl";
		List<String> wsdl;
		try {
			wsdl = Utils.readHttp(fullWsdlAddress);
		} catch (Exception e) {
			Utils.prtObErrMess(WebServiceServer.class, " must start server at ip: "+publicServerUrl+" port: "+portNum);
			return false;
		}
		if(wsdl!=null){
			return true;
		}
		return false;
	}
	
	
	// Override these methods so that you can expose them to api callers in other
	//   projects, and not have to export entire SimpleService project
	@Override
	public boolean isPublished() {
		return super.isPublished();
	}

	@Override
	public void publishService() {
		if(nameOfService!=null)Utils.prtNoNewLine(nameOfService+" : ");
		super.publishService();
	}

	
}
