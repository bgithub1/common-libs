package com.billybyte.clientserver.webserver;



import com.billybyte.WsAccessClient;
import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.commonstaticmethods.Utils;
import com.thoughtworks.xstream.XStream;

public class AppServiceClient<K,V> {
	private final 	WsAccessClient wsClient ;
	private final XStream xs;
	private final String port ;
	private final String ip;
	
	
	
	public AppServiceClient(
			String publicIpAddressOfWebService,
			XStream xs, String port, String ip) {
		super();
		this.xs = xs;
		this.port = port;
		this.ip = ip;
		wsClient = new WsAccessClient(publicIpAddressOfWebService, "8080");

	}
	public AppServiceClient(ServiceBlock serviceBlock,XStream xs){
		this.xs = xs;
		this.port = serviceBlock.getPortOfService().toString();
		this.ip = serviceBlock.getUrlOfService();
		wsClient = new WsAccessClient(serviceBlock.getUrlOfApplicationServer(), "8080");
		
	}

	
	private Object send_lock = new Object();
	@SuppressWarnings("unchecked")
	public V getDataFromWebService(K objectToSend){
		synchronized (send_lock) {
			String xmlVersionOfK = xs.toXML(objectToSend);
			// construct as string array of the ip+,+port and the xml'ed version of K
			String[] xmlStrings = { ip + "," + port, xmlVersionOfK };
			// send it to the public web service (to be passed along to an
			//   internal SimpleService implementation)
			String[] stringResults = wsClient.callService(xmlStrings);
//			File f = new File("dump.txt");
//			try {
//				FileWriter fw = new FileWriter(f);
//				fw.write(stringResults[0]);
//				fw.close();
//				
//			} catch (IOException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
			
			//  The returned string is a 1 element String array of xml.
			//    The string array itself is in xml, so you have to call
			//       fromXml twice.
			V r=null;
			try {
				String xmlStage1=null;
				try {
					xmlStage1 = (String)xs.fromXML(stringResults[0]);
				} catch (Exception e) {
					Utils.prtObErrMess(this.getClass(), " error on stage1 of xml conversion: "+stringResults[0]);
					return null;
				}
//				r = (V) xs.fromXML((String) xs.fromXML(stringResults[0]));					
				try {
					r = (V) xs.fromXML(xmlStage1);
				} catch (Exception e) {
					Utils.prtObErrMess(this.getClass(), " error on stage2 of xml conversion: "+xmlStage1);
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return r;
		}
	}

}
