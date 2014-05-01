package com.billybyte.dse.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.print.DocFlavor.STRING;

import com.billybyte.clientserver.ServiceBlock;
import com.billybyte.clientserver.webserver.WebServiceComLib;
import com.billybyte.clientserver.webserver.WsEnums.WsPort;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.queries.ComplexQueryResult;
import com.thoughtworks.xstream.XStream;
/**
 * Take a Web service on the WsPort.SETTLE_ALL port, which is a query of 
 * 		String to SettlementDataImmute, and turn it into a query
 * 		of String {@link ComplexQueryResult<@link SettlementDataInterface}
 * @author bperlman1
 *
 */
public class SettleSingleFromWebServ implements QueryInterface<String , ComplexQueryResult<SettlementDataInterface>>{
//	private final static String settlePort = "9500";
//	private final static String DEFAULT_SB_STRING = ","+settlePort+",http://127.0.0.1,settleDirectQuery";
	private final QueryInterface<String, SettlementDataInterface> settleQuery ;
	private final Map<String,SettlementDataInterface> cacheMap;
/**
 * 
 * @param settleSbString full sb string for when the backing settle service is
 *   not NECESSARILY on WsPort.SETTLE_ALL
 */
	public SettleSingleFromWebServ(String settleSbString){
		ServiceBlock sb = new ServiceBlock(settleSbString/*!=null?settleSbString:DefaultValues.DEFAULT_SETTLE_SINGLE_SB_STRING*/);
		settleQuery = WebServiceComLib.getQueryService(sb, new XStream());
		this.cacheMap = new HashMap<String,SettlementDataInterface>();
	}
	/**
	 * 
	 * @param sb for when the backing settle service is
 *   not NECESSARILY on WsPort.SETTLE_ALL
	 */
	public SettleSingleFromWebServ(ServiceBlock sb){
		settleQuery = WebServiceComLib.getQueryService(sb, new XStream());
		this.cacheMap = new HashMap<String,SettlementDataInterface>();
	}
	/**
	 * When backing service is on {@link WsPort.SETTLE_ALL}
	 * @param urlAs
	 * @param urlWs
	 */
	public SettleSingleFromWebServ(String urlAs, String urlWs){
		this(WsPort.SETTLE_ALL.createServiceBlock(urlAs,urlWs));
	}

	@Override
	public ComplexQueryResult<SettlementDataInterface> get(String key,
			int timeoutValue, TimeUnit timeUnitType) {
		if(cacheMap.containsKey(key))return new ComplexQueryResult<SettlementDataInterface>(null, cacheMap.get(key));
		SettlementDataInterface settle = settleQuery.get(key, timeoutValue, timeUnitType);
		if(settle==null){
			return errRet(key+" settle not found");
		}
		cacheMap.put(key, settle);
		return new ComplexQueryResult<SettlementDataInterface>(null, settle);
	}

	private ComplexQueryResult<SettlementDataInterface> errRet(String s){
		Exception e = Utils.IllState(this.getClass(),s);
		return new ComplexQueryResult<SettlementDataInterface>(e, null);
	}

}
