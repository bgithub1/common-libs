package com.billybyte.clientserver.webserver;

import com.billybyte.clientserver.ServiceBlock;

/**
 * 
 * @author bperlman1
 *
 */
public class WsEnums {
	WsEnums ws;
	public enum WsPort{
		INT_SERVER("intServer",9066),
		SETTLE_ALL("settleAllQuery",9500),
		SETTLE_SET_MONGO_CQR_RET_FUT_FOP_STK("settleSetFromMongoCqrRetQuery",9510),
		SETTLE_REGEX_SET_MONGO_CQR_RET_FUT_FOP_STK("settleRegexSetFromMongoCqrRetQuery",9520),
		SETTLE_REGEX_W_LIST_RET("regexToSettlementQuery",9130),
		SETTLE_PARTIAL_SN_MAP_RET("settleDataMapFromPartialShortNameQuery",9100),
		SETTLE_DEPTHBOOK("settlementDepthBookQuery",9000),
		SETTLE_SET("settlementSetQuery",9200),
		SETTLE_DB_ONLY("settlementDatabaseOnlyQuery",9131),
		SECDEF_ALL("secDefAllQuery",9028),
		SECDEF_SINGLE("secDefQueryEngine",9002),
		SECDEF_SINGLE_W_LIST_RET("secDefListQuery",9027),
		SECDEF_PDI("secDefPdiQuery",9023),
		SECDEF_PDI_9123("secDefPdiQueryOn123",9123),
		SECDEF_IB("ibSecDefQuery",9004),
		DEPTHBOOK_IB("ibDepthBookQuery",9003),
		DEPTHBOOK_RT("realTimeDepthBookQuery",9001),
		VAR_SINGLE("varCommoditiesQuery",9005),
		NG_PRICE_DISPLAY("ngPriceDisplay",9007),
		PL_UPDATE_PORTFOLIO("plUpdatePortfolioQuery",9008),
		PL("plQuery",9009),
		SHORTNAME_VALIDATOR("shortNameValidator",9020),
		SHORTNAME_SET_VALIDATOR("shortNameSetValidator",9120),
		RDATA_ALL("rDataWithDatabaseQuery",9024),
		RDATA_ONLY_IN_SETTLES("rDataFromDataInSettleDatabaseQuery",9124),
		RDATA_RT("rDataRtQuery",9025),
		RDATA_PER_PR("rDataPerPrQuery",9026),
		VOL_FROM_RDATA("volFromRDataQuery",9325),
		VOL_FROM_MONGO("volFromMongoImpliedVolDbQuery",9325),
		VOLSURF_REQ("volSurfRequestQuery",9041),
		VOLSURF_UPLOAD("volSurfUploadQuery",9042),
		UNDERLYING_SECDEF("underlyingSdQuery",9111),
		TREASURY_RATE("treasuryRateQuery",9081),
		FEE_SERVICE("feeService",9083),
		COMMISSION_SERVICE("feeService",9084),
		PDI_SET("pdiSetQuery",9223),
		ATM_MIDPOINT_SET("atmMidPointSetQuery",9323),
		CORRELATIONS_FROM_SHORTNAME_SET_CQR_RET("corrFromSnSetCqrRetQuery",9425),
		CORRELATIONS_FROM_SHORTNAME_PAIR_SET_CQR_RET("corrFromSnPairSetCqrRetQuery",9525),
		CORRELATIONS_IMPLIED_FROM_SHORTNAME_FOR_CSOS("impliedCorrForCsoQuery",9625),
		;
		
		private final String serviceName;
		private final int port;
		WsPort(String serviceName,int port){
			this.port = port;
			this.serviceName =serviceName;
		}
		
		public int getPort(){
			return this.port;
		}
		public String getServiceName(){
			return this.serviceName;
		}
		
		public ServiceBlock createServiceBlock(String url_Of_Service){
			return new ServiceBlock(url_Of_Service+","+getPort()+","+
					url_Of_Service+","+getServiceName());
		}
		public ServiceBlock createServiceBlock(String url_As, String url_Service){
			return new ServiceBlock(
					(url_As==null?" ":url_As)+","+getPort()+","+
					url_Service+","+getServiceName());
		}
	}
}
