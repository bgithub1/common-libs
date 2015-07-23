package com.billybyte.mongo;

public class MongoDatabaseNames {
	public static final String DEFAULT_HOST = "localhost";
	public static final int DEFAULT_PORT = 27017;
	// database names
	public static final String CORRELATIONS_DB = "stkCorrXml";
	public static final String CORRELATIONS_CL = "correls";
	
	// correls (from spanHist)
	public static final String CORREL_DB = "correlDb";
	public static final String CORREL_CL = "correlColl";
	
	// settlements
	public static final String SETTLEMENT_DB = "settleDb";
	public static final String SETTLEMENT_CL = 	"settleColl";
	// rdata
	public static final String RDATA_DB = "rdataDb";
	public static final String RDATA_CL = 	"rdataColl";
	// pdi
	public static final String PDI_DB = "pdiDb";
	public static final String PDI_CL = 	"pdiColl";
	// atm
	public static final String ATM_DB = "atmDb";
	public static final String ATM_CL = "atmColl";
	// rate
	public static final String RATE_DB = "rateDb";
	public static final String RATE_CL = "rateColl";
	// short name
	public static final String SHORTNAME_DB = "shortDb";
	public static final String SHORTNAME_CL = "shortColl";
	// results
	public static final String RESULTS_DB = "resultsDb";
	public static final String RESULTS_CL = "resultsColl";
	// implied vols
	public static final String IMPLIEDVOL_DB = "ImpliedVolDb";
	public static final String IMPLIEDVOL_CL = "ImpliedVolColl";
	// sec defs
	public static final String SECDEF_DB = "secDefDb";
	public static final String SECDEF_CL = "secDefColl";
	// history for Correls
	public static final String HISTORY_FOR_CORRELATIONS_DB = "historyForCorrelationsDb";
	public static final String HISTORY_FOR_CORRELATIONS_CL = "historyForCorrelationsColl";
	// implied correl db
	public static final String IMPLIED_CORREL_DB = "impliedCorrelDb";
	public static final String IMPLIED_CORREL_CL = "impliedCorrelColl";	
	// stk list db
	public static final String STK_LIST_DB = "equityListDb";
	public static final String STK_LIST_CL = "equityListColl";
	// userClearingAccount db
	public static final String USR_CLRING_ACCT_DB = "userClearingAcctDb";
	public static final String USR_CLRING_ACCT_CL = "userClearingAcctColl";
	// clearingHouseLogon db
	public static final String CLRING_LOGON_DB = "clearingHouseLogonDb";
	public static final String CLRING_LOGON_CL = "clearingHouseLogonColl";
	
	public static String INDEXED_SETTLE_HIST_DB = "indexedSettleHistDb";
	public static String INDEXED_SETTLE_HIST_CL = "indexedSettleHistColl";

	public static String SYMBOL_CONV_DB = "symbolConvDb";
	public static String SYMBOL_CONV_CL = "symbolConvColl";

	public static String SPAN_SECDEF_DB = "spanSecDefDb"; // "secDefDb"
	public static String SPAN_SECDEF_CL = "spanSecDefColl"; // "secDefColl"

//	public static String SPAN_UNDER_SECDEF_DB = "spanUnderSecDefDb"; // "spanUnderSecDefDb"
//	public static String SPAN_UNDER_SECDEF_CL = "spanUnderSecDefColl"; // "spanUnderSecDefColl"

	public static String SPAN_UNDER_SNINFO_DB = "spanUnderSnInfoDb"; // "spanUnderSnInfoDb"
	public static String SPAN_UNDER_SNINFO_CL = "spanUnderSnInfoColl"; // "spanUnderSnInfoColl"

	public static final String SPAN_HIST_DB = "spanHistDb"; // ""
	public static final String SPAN_HIST_CL = "spanHistColl"; // ""
}
