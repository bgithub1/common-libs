package com.billybyte.dse.queries;

import com.billybyte.clientserver.webserver.WsEnums.WsPort;
import com.billybyte.mongo.MongoDatabaseNames;

public class DefaultValues {
	private final static String defaultWsUrl = "http://127.0.0.1";
	private final static int settlePort = WsPort.SETTLE_ALL.getPort();
	private static final int ratePort = WsPort.TREASURY_RATE.getPort();
	private static final int volFromImpliedPort = WsPort.VOL_FROM_RDATA.getPort();
	private static final int volSurfaceSetPort = WsPort.VOLSURF_REQ.getPort();
	private static final int pdiSetPort = WsPort.PDI_SET.getPort();
	private static final int secDefAllPort = WsPort.SECDEF_ALL.getPort();
	private static final int corrFromSnSetCqrRetPort = WsPort.CORRELATIONS_FROM_SHORTNAME_SET_CQR_RET.getPort();
	private static final int impliedCorrFromSnForCsoCqrRetPort = WsPort.CORRELATIONS_IMPLIED_FROM_SHORTNAME_FOR_CSOS.getPort();
	
	public final static String DEFAULT_SECDEF_ALL_SB_STRING = ","+secDefAllPort+","+defaultWsUrl+","+WsPort.SECDEF_ALL.getServiceName();
	public final static String DEFAULT_PDI_SET_SB_STRING = ","+pdiSetPort+","+defaultWsUrl+","+WsPort.PDI_SET.getServiceName();
	public final static String DEFAULT_SETTLE_SINGLE_SB_STRING = ","+settlePort+","+defaultWsUrl+","+WsPort.SETTLE_ALL.getServiceName();
	public static final String DEFAULT_RATE_SINGLE_SB_STRING = ","+ratePort+","+defaultWsUrl+","+WsPort.TREASURY_RATE.getServiceName();
	public static final String DEFAULT_DIV_SINGLE_SB_STRING = ","+ratePort+","+defaultWsUrl+","+WsPort.TREASURY_RATE.getServiceName();
	public static final String DEFAULT_VOL_FROM_IMPLIED_VOL_SB_STRING = ","+volFromImpliedPort+","+defaultWsUrl+","+WsPort.VOL_FROM_RDATA.getServiceName();
	public static final String DEFAULT_VOL_SURFACE_SET_SB_STRING = ","+volSurfaceSetPort+","+defaultWsUrl+","+WsPort.VOLSURF_REQ.getServiceName();
	public static final String DEFAULT_CORR_FROM_SN_SET_CQR_RET_SB_STRING = ","+corrFromSnSetCqrRetPort+","+defaultWsUrl+","+WsPort.CORRELATIONS_FROM_SHORTNAME_SET_CQR_RET.getServiceName();
	public static final String DEFAULT_CORRELATIONS_IMPLIED_FROM_SHORTNAME_FOR_CSOS_SB_STRING = ","+impliedCorrFromSnForCsoCqrRetPort+","+defaultWsUrl+","+WsPort.CORRELATIONS_IMPLIED_FROM_SHORTNAME_FOR_CSOS.getServiceName();

	
	public static final String DEFAULT_MONGO_IP = "localhost";
	public static int DEFAULT_MONGO_PORT = 27017;
	public static String DEFAULT_MONGO_CORRELATION_DB_NAME = MongoDatabaseNames.CORRELATIONS_DB;
	public static String DEFAULT_MONGO_CORRELATION_COLLECTION_NAME = MongoDatabaseNames.CORRELATIONS_CL;
	public static final String DEFAULT_CMCDIFF_PATH = 
			"../PortfolioData/CorrelationData/CommoditiesCorrelations/AllCommodities/perMonthCummCorr.csv";
	public static final String DEFAULT_COMMODPRODCORR_PATH = 
			"../PortfolioData/CorrelationData/CommoditiesCorrelations/AllCommodities/commProdCorrelations.csv";
	public static final String DEFAULT_CSO_IMPLIED_CORRELATIONS_CSV_PATH = 
			"../PortfolioData/CorrelationData/CommoditiesCorrelations/impliedCSOCorrelationsMap.csv";

}
