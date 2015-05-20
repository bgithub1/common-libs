package com.billybyte.clientserver;


/**
 * This class facilitates passing arguments to WebServiceServer, WebServiceQuery, etc
 * @author bperlman1
 *
 */
public class ServiceBlock {
	private final String urlOfApplicationServer;
	private final Integer portOfService;
	private final String urlOfService;
	private final String nameOfService;

	@SuppressWarnings("unused")
	private ServiceBlock(){
		this.urlOfApplicationServer = null;
		this.portOfService = null;
		this.urlOfService = null;
		this.nameOfService = null;
	}
	
	public ServiceBlock(String[] args){
		this(args[0]==null?null:args[0].trim(), new Integer(args[1].trim()), args[2].trim(),args[3].trim());
	}
	
	public ServiceBlock(String commaSeparatedArgs){
		this(commaSeparatedArgs.split(","));
	}
	
	/**
	 * 
	 * @param urlOfApplicationServer - public
	 * @param portOfService - port
	 * @param ipOfService - private
	 * @param nameOfService - name
	 */
	public ServiceBlock(String urlOfApplicationServer, Integer portOfService,
			String urlOfService, String nameOfService) {
		super();
		this.urlOfApplicationServer = (urlOfApplicationServer==null || urlOfApplicationServer.trim().compareTo("  ")<=0)?null:urlOfApplicationServer;
		this.portOfService = portOfService;
		this.urlOfService = urlOfService;
		this.nameOfService = nameOfService;
	}
	public String getUrlOfApplicationServer() {
		return urlOfApplicationServer;
	}
	public Integer getPortOfService() {
		return portOfService;
	}
	public String getUrlOfService() {
		return urlOfService;
	}
	public String getNameOfService() {
		return nameOfService;
	}

	@Override
	public String toString() {
		return getNameOfService()+","+getUrlOfApplicationServer()+","+getPortOfService()+","+getUrlOfService();
	}
	
	public String getSbString(){
		return ((getUrlOfApplicationServer()==null)?"":getUrlOfApplicationServer())+","+getPortOfService()+","+getUrlOfService()+","+getNameOfService();
	}
	
}
