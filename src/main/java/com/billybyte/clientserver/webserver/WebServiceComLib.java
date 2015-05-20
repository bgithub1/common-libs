package com.billybyte.clientserver.webserver;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.clientserver.webserver.WebServiceQuery;
import com.billybyte.clientserver.webserver.WebServiceServer;
import com.billybyte.clientserver.webserver.WsEnums;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.QuerySetInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.thoughtworks.xstream.XStream;

public class WebServiceComLib {
	public static <K,V> WebServiceServer<K, V> startServer(ServiceBlock sb,QueryInterface<K, V> query){
		String firewallIp = sb.getUrlOfService();
		String port = sb.getPortOfService().toString();
		if(WebServiceServer.isStarted(firewallIp, port))return null;
		WebServiceServer<K, V> server = 
			new WebServiceServer<K, V>(sb,query,3000,TimeUnit.MILLISECONDS);
		return server;
	}

	public static <K,V> WebServiceServer<K, V> startServerFromWsPort(
			String url_Ws, 
			WsEnums.WsPort portEnum,
			QueryInterface<K, V> query){
		
		String urlWebService = url_Ws;
		Integer port = new Integer(portEnum.getPort());
		if(WebServiceServer.isStarted(urlWebService, port.toString()))return null;
		WebServiceServer<K, V> server = 
			new WebServiceServer<K, V>(urlWebService,true,port,new XStream(),query,3000,TimeUnit.MILLISECONDS);
		return server;
	}

	
	public static <K,V> QueryInterface<K,V> getQueryService(ServiceBlock sb, XStream xs){
		QueryInterface<K,V>  ret=null;
		if(sb.getUrlOfApplicationServer()==null){
			 ret = new WebServiceQuery<K, V>(sb, xs);
		}else{
			 ret = 
					new AppServiceClientQuery<K, V>(sb, xs);
		}
		return ret;
	}
	
	public static <K,V> QueryInterface<K,V> getQueryService(String serviceBlockServiceString,
			String nameOfServiceToGet){
		XStream xs = new XStream();
		ServiceBlock sbOfServiceBlockService = new ServiceBlock(serviceBlockServiceString);
		QueryInterface<String, ServiceBlock> sbQuery = 
				getQueryService(sbOfServiceBlockService, xs);
		ServiceBlock retServiceSb = sbQuery.get(nameOfServiceToGet, 1, TimeUnit.SECONDS);
		QueryInterface<K,V> ret = getQueryService(retServiceSb, xs);
		return ret;
	}
	
	public static <K,V> QuerySetInterface<K,V> getQuerySetService(ServiceBlock sb, XStream xs){
		QuerySetInterface<K,V>  ret=null;
		if(sb.getUrlOfApplicationServer()==null){
			ret = new WebServiceSetQuery<K, V>(sb, xs);
		}else{
			 ret = new AppServiceClientSetQuery<K, V>(sb, xs);
		}
		return ret;
	}

	public static <K,V> QueryInterface<K,V> getQueryServiceFromWsPort(
			WsEnums.WsPort portEnum,
			String url_As,String url_Ws, XStream xs){
		QueryInterface<K,V>  ret=null;
		ServiceBlock sb = portEnum.createServiceBlock(url_As == null ? "" : url_As, url_Ws);
		if(sb.getUrlOfApplicationServer()==null){
			ret = new WebServiceQuery<K, V>(sb, xs);
		}else{
			 ret = new AppServiceClientQuery<K, V>(sb, xs);
		}
		return ret;
	}

	
	public static void serviceBlockServiceStart(final Map<String,ServiceBlock> sbMap,
			ServiceBlock sbServiceSb){
		final QueryInterface<String, ServiceBlock> queryFromMap = 
				new QueryInterface<String,ServiceBlock>() {
			
					@Override
					public ServiceBlock get(String key, int timeoutValue,
							TimeUnit timeUnitType) {
						return sbMap.get(key);
					}
				}
		;
		
		 
		startServer(sbServiceSb, queryFromMap)	;
	}
				
	public static void serviceBlockServiceStart(String sbMapPath,
			ServiceBlock sbServiceSb){
		@SuppressWarnings("unchecked")
		Map<String,ServiceBlock> sbMap = 
				(Map<String,ServiceBlock>)Utils.getFromXml(Map.class, null, sbMapPath);
		serviceBlockServiceStart(sbMap, sbServiceSb);
	}
	
	
}
