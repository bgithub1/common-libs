package com.billybyte.clientserver.webserver;

import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commoninterfaces.QueryInterface;
import com.thoughtworks.xstream.XStream;

public class AppServiceClientQuery<K,V>  
	implements QueryInterface<K, V>{
	private final AppServiceClient<K, V> appClient;
//	public AppServiceClientQuery(String publicIpAddressOfWebService,
//			XStream xs, String port, String ip) {
//		appClient  = new AppServiceClient<K, V>(publicIpAddressOfWebService, xs, port, ip);
//	}

	public AppServiceClientQuery(ServiceBlock sb,XStream xs){
		appClient  = new AppServiceClient<K, V>(sb,xs);
	}
	
	@Override
	public V get(K key, int timeoutValue, TimeUnit timeUnitType) {
		return appClient.getDataFromWebService(key);
	}

}
