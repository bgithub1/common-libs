package com.billybyte.clientserver.webserver;

import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.simplewebservice.SimpleWebServiceClient;
import com.thoughtworks.xstream.XStream;
/**
 * This is a query that holds a SimpleWebServiceClient, and access its query info from the WebService 
 * @author bperlman1
 *
 * @param <K>
 * @param <T>
 */
public class WebServiceQuery<K,T> implements QueryInterface<K, T>{
	private final SimpleWebServiceClient<K, T> webServiceClient;
	
	public WebServiceQuery(XStream xstream,
			String publicIpAddressOfServer, 
			String port,
			String serviceName) {

		super();
		if(!WebServiceServer.isStarted(publicIpAddressOfServer, port)){
			throw Utils.IllState(this.getClass(), " server at ip: "+ 
					publicIpAddressOfServer + " port: "+ port + 
					" serivce: " + serviceName + " has not been started");
		}
		webServiceClient = new SimpleWebServiceClient<K, T>(
				xstream,publicIpAddressOfServer,port);
		webServiceClient.setUsingDirectServerCalls(true);
	}

	public WebServiceQuery(ServiceBlock sb,XStream xs){
		this(xs,sb.getUrlOfService(),sb.getPortOfService().toString(),
				sb.getNameOfService());
	}
	
	
	
	@Override
	public T get(K key, int timeoutValue, TimeUnit timeUnitType) {
		
		T ret = webServiceClient.getDataFromService(key);
		return ret;
	}

}
