package com.billybyte.commonlibs.testcases;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.csvprocessing.cmeexpiryprocessing.CmeContract;
import com.billybyte.csvprocessing.cmeexpiryprocessing.CmeContractDatabaseQuery;
import com.billybyte.csvprocessing.iceexpiryprocessing.IceExpiryData;
import com.billybyte.csvprocessing.iceexpiryprocessing.IceExpiryDataQuery;
import com.billybyte.csvprocessing.iceexpiryprocessing.IceProductInfoQuery;
import com.billybyte.marketdata.ShortNameProcessor;
import com.billybyte.marketdata.futures.ExpiryFromListOfExpiryRules;
import com.billybyte.marketdata.futures.ExpiryRuleInterface;
import com.billybyte.marketdata.futures.FuturesCodes;
//import com.billybyte.marketdata.futures.swaps.SwapProduct;
import com.billybyte.neodatis.NeoDatisSingleClassDataBase;

public class TestExpiryFromListOfExpiryRules extends TestCase{
	/**
		NG(0,0,false,true,3,false,0), //Trading terminates three business days prior to the first calendar day of the delivery month
	iNG(0,0,false,true,2,false,0),// Trading shall cease at the close of business two business days prior to the first calendar day of the delivery month, quarter or season.
	CL(-1,25,true,true,3,false,0),//Trading terminates at the close of business on the third business day prior to the 25th calendar day of the month preceding the delivery month. If the 25th calendar day of the month is a non-business day, trading shall cease on the third business day prior to the business day preceding the 25th calendar day.
	iCL(-1,25,true,true,4,false,0),//The close of business on the fourth US business day prior to the 25th calendar day of the month preceding the contract month. If the 25th day is a non-business day, trading shall cease on the fourth business day prior to the business day preceding the 25th calendar day.
	iHH(0,0,false,true,3,false,0),
	HO(0,0,false,true,1,false,0),//Trading terminates at the close of business on the last business day of the month preceding the delivery month.
	iHO(0,0,false,true,2,false,0),//Trading shall cease at the end of the designated settlement period on the penultimate US business day of the month preceding the contract month
	RB(0,0,false,true,1,false,0),//Trading terminates at the close of business on the last business day of the month preceding the delivery month.
	iRB(0,0,false,true,2,false,0),//Trading shall cease at the end of the designated settlement period on the penultimate US business day of the month preceding the delivery month. 
	BZ(0,-15,true,false,1,true,1),//Trading ceases at the close of business on the business day prior to the 15th calendar day prior to the start of the delivery month. If the 15th calendar day is a non-business day in London, trading shall cease on the business day ( in London) immediately preceeding the day preceding the 15th calendar day.
	iBZ(0,-15,true,false,1,true,1),
	iGO(0,14,true,true,2,false,0),//Trading shall cease at 12:00 hours, 2 business days prior to the 14th calendar day of the delivery month
	ZW(0,15,false,true,1,false,0),//The business day prior to the 15th calendar day of the contract month.
	ZS(0,15,false,true,1,false,0),//The business day prior to the 15th calendar day of the contract month.
	ZL(0,15,false,true,1,false,0), //The business day prior to the 15th calendar day of the contract month.
	ZM(0,15,false,true,1,false,0), //The business day prior to the 15th calendar day of the contract month.
	ZC(0,15,false,true,1,false,0), //The business day prior to the 15th calendar day of the contract month.
	iCT(1,-1,true,true,16,false,0), //Seventeen business days from end of spot month. 
	iKC(1,-1,true,true,8,false,0), //Eight business days prior to the last business day of the delivery month 
	iCC(1,-1,true,true,11,false,0), //Eight business days prior to the last business day of the delivery month 
	iSB(0,0,false,true,1,false,0), //Last business day of the month preceding the delivery month (except January, which is the second business day before the 24th calendar day of the prior month).
	GC(1,0,false,true,3,false,0), //Trading terminates Third to last business day of the contract  month
	SI(1,0,false,true,3,false,0), //Trading terminates Third to last business day of the contract  month
	HG(1,0,false,true,3,false,0), //Trading terminates Third to last business day of the contract  month
	PL(1,0,false,true,3,false,0), //Trading terminates Third to last business day of the contract  month
	HE(0,10,true,true,0,false,0), //Trading terminates on the 10th business day of the contract  month
	LE(1,0,false,true,1,false,0),//Trading terminates at the close of business on the last business day of the contract  month
	iOJ(1,-1,true,true,14,false,0); //14th business day prior to the last business day of the month

* 		
*/
	CmeContractDatabaseQuery query;
	IceExpiryDataQuery iceExpiryDataQuery ;

	
	static final ShortNameProcessor sproc = 
		new ShortNameProcessor(".",
				new HashSet<String>(Arrays.asList(new String[]{"CL","NG","HO","GOIL","GC","SI","ED"})));

	static final String[] monthCodes =FuturesCodes.monthCodes;// {"F","G","H","J","K","M","N","Q","U","V","X","Z",};
	static int getMonthNum(String monthCode){
		int ret = 0;
		for(int i = 0;i<monthCodes.length;i++){
			if(monthCode.compareTo(monthCodes[i])==0){
				ret = i+1;
				return ret;
			}
		}
		return -1;
	}
	
	static DecimalFormat dfMonth = new DecimalFormat("00");

	static String cmeFutureShortNameToShortName(String productInShortName,
			String cmeShortName,String exch,String currency){
		String product = productInShortName;//cmeShortName.substring(0, cmeShortName.length()-3);
		String yearNumString = "20"+cmeShortName.substring(cmeShortName.length()-2,cmeShortName.length() );
		String monthCode = cmeShortName.substring(cmeShortName.length()-3,cmeShortName.length()-2) ;
		String monthNumString = dfMonth.format(getMonthNum(monthCode));
		return product+".FUT."+exch+"."+currency+"."+yearNumString+monthNumString;
	}

	static String cmeOptionShortNameToShortName(String productInShortName,
			String cmeShortName,String exch,
			String currency,String cp,String strike){
		String product = productInShortName;//cmeShortName.substring(0, cmeShortName.length()-3);
		String yearNumString = "20"+cmeShortName.substring(cmeShortName.length()-2,cmeShortName.length() );
		String monthCode = cmeShortName.substring(cmeShortName.length()-3,cmeShortName.length()-2) ;
		String monthNumString = dfMonth.format(getMonthNum(monthCode));
		return product+".FOP."+exch+"."+currency+"."+yearNumString+monthNumString+"."+cp+"."+strike;
	}

	
	static String databasePath = "../PortfolioData/CmeData/cmeContracts.database";
	
	static String iceExpiryQueryDatabasePath = "../PortfolioData/CmeData/IceExpiryDatabase.database";

	
	
	private void runTests(String locale,String contractFirstPart,
			int[] yearArray,int[] monthArray,int[] dayArray,int contractYear){
		ExpiryFromListOfExpiryRules efrl = new ExpiryFromListOfExpiryRules();
		Calendar cal;
		DecimalFormat dfYear = new DecimalFormat("0000");
		DecimalFormat dfMonth = new DecimalFormat("00");
		for(int i=0;i<12;i++){
			int expMonth = monthArray[i];
			int expDay = dayArray[i];
			int expYear = yearArray[i];
			
			String shortName = contractFirstPart+dfYear.format(contractYear)+dfMonth.format(i+1);
			cal = efrl.getExpiry(shortName);
			assertNotNull(cal);
			int actualYear = cal.get(Calendar.YEAR);
			int actualMonth = cal.get(Calendar.MONTH)+1;
			int actualDay = cal.get(Calendar.DAY_OF_MONTH);
			assertEquals(expYear,actualYear);
			assertEquals(expMonth,actualMonth);
			assertEquals(expDay,actualDay);
		}
		
	}
	
	private String[][]  buildIceConditions(String shortName,String[][] additionalDatabaseGetConditions){
		String[][] ret = {{"shortName",shortName}};
		return ret;
	}

	private List<Tuple<String, String>>  buildBasicCmeConditions(String productName_in_contracts_data_xml_file,
			String cp){
		List<Tuple<String, String>> conditions = new  ArrayList<Tuple<String,String>>();
		// first query on Exp_Contract_Code, which is like CLN11 (for crude), or CN11 (for corn)
		String field1="Exp_Contract_Code";
		// search for all values like CLN11 or CN11 or GCZ11
		String searchValue1 = "^(("+productName_in_contracts_data_xml_file+")(F|G|H|J|K|M|N|Q|U|V|X|Z)(11|12|13|14))$";
		// load these values into a Tuple
		Tuple<String, String> c1 = new Tuple<String, String>(field1,searchValue1);
		conditions.add(c1);

		// now search on PRODUCT_TYPE_CODE like FUT or OOF (for Options On Futures)
		String field2="PRODUCT_TYPE_CODE";
		String searchValue2 ;
		if(cp==null){
			searchValue2 = "FUT";
			Tuple<String, String> commodityCodeCondition = new Tuple<String, String>("Commodity_Code",productName_in_contracts_data_xml_file);
			conditions.add(commodityCodeCondition);
		}else{
			searchValue2 = "OOF";
		}
		Tuple<String, String> c2 = new Tuple<String, String>(field2,searchValue2);
		conditions.add(c2);
		return conditions;
	}

	private void runTestsUsingIceExpiryData(			
			String symbol,
			String iceExpiryDataShortNameSearchString,
			String systemExch, String iceExch,
			String cp,String strikeString,
			String[][] additionalDatabaseGetConditions){
		
		// instantiate a rule list - which is what we are going to UnitTest here
		ExpiryFromListOfExpiryRules efrl = new ExpiryFromListOfExpiryRules();
		// build query conditions list
		String[][] conditions = 
			buildIceConditions(iceExpiryDataShortNameSearchString,additionalDatabaseGetConditions);		

		
		List<IceExpiryData> iceExData = iceExpiryDataQuery.queryLike(conditions);
		assertNotNull(iceExData);
		assertTrue(iceExData.size()>0);
		
		Utils.prtObMess(this.getClass()," ice data for shortName: "+symbol+ " = "+iceExData.size());
		for(IceExpiryData ied:iceExData){
			// replace IceExpiryData product/symbol name with name that expiry rules know
			String iedSymbol = ied.getShortName().split("\\.")[0];
			String typeReplace = ".FUT.";
			if(!ied.getShortName().contains("FUT")){
				typeReplace = ".FOP.";
			}
			String shortName = ied.getShortName().replace(iedSymbol+typeReplace, symbol+typeReplace);
			shortName = shortName.replace(iceExch, systemExch);
			// do rest of test
			if(ied.getLTD()!=null){
				compareActualWithExpected(ied.getLTD(), shortName, efrl);
				Utils.prtObMess(this.getClass(), "success: "+shortName+","+ied.getLTD());
			}else{
				Utils.prtObMess(this.getClass(), "no LTD for: "+shortName+","+ied.getLTD());
			}
		}
		
		
	}

	
	@Override
	protected void setUp() throws Exception {
		try {
			this.query= new CmeContractDatabaseQuery(databasePath);
			NeoDatisSingleClassDataBase<List<Tuple<String, String>>, IceExpiryData> iceExpiryDatabase = 
					new NeoDatisSingleClassDataBase<List<Tuple<String,String>>, IceExpiryData>(iceExpiryQueryDatabasePath,IceExpiryData.class);
			this.iceExpiryDataQuery = new IceExpiryDataQuery(iceExpiryDatabase);
		} catch (Exception e) {
			Utils.prtObErrMess(this.getClass(), "no expiry files from exchange - don't run these tests");
			Utils.prtObErrMess(this.getClass(), "exception message: "+e.getMessage());
			fail("no expiry files from exchange - don't run these tests.  Exception message: "+e.getMessage());
		}
		
		super.setUp();
	}
	
	

	@Override
	protected void tearDown() throws Exception {
		query.closeDb();
		this.iceExpiryDataQuery.closeDatabase();
		super.tearDown();
	}

	private void runTestUsingCmeContractData(
			String productName_in_contracts_data_xml_file,
			String productInShortName,
			String exchangeSymbolInShortName, 
			String currency,String exchange_symbol_in_contracts_data_xml_file,
			String cp,String strikeString,
			List<Tuple<String, String>> additionalDatabaseGetConditions){
		
		// instantiate a rule list - which is what we are going to UnitTest here
		ExpiryFromListOfExpiryRules efrl = new ExpiryFromListOfExpiryRules();
		// build query conditions list
		List<Tuple<String, String>> conditions = 
			buildBasicCmeConditions(productName_in_contracts_data_xml_file,cp);//= new  ArrayList<Tuple<String,String>>();
		if(additionalDatabaseGetConditions!=null){
			for(Tuple<String, String> additionalCondition: additionalDatabaseGetConditions){
				conditions.add(additionalCondition);
			}
		}
		List<CmeContract> contracts = query.get(conditions, 1000, TimeUnit.MILLISECONDS);
		assertNotNull(contracts);
		assertTrue(contracts.size()>0);

		// test each contract
		Calendar expiryFromRule;
		for(CmeContract c:contracts){
			if(c.getExchange().compareTo(exchange_symbol_in_contracts_data_xml_file)!=0)continue;
			String shortName;
			if(cp==null){
				shortName = cmeFutureShortNameToShortName(productInShortName,c.getExp_Contract_Code(), exchangeSymbolInShortName, currency);
			}else{
				shortName = cmeOptionShortNameToShortName(productInShortName,c.getExp_Contract_Code(), exchangeSymbolInShortName, currency,cp,strikeString);
			}

			// do rest of test
			compareActualWithExpected(c.getLTD(), shortName, efrl);

		}
		

	}

	private void compareActualWithExpected(String LTD,String shortName, ExpiryRuleInterface efrl){
		String[] monthDayYear = LTD.split("/");
		int expectedYear = new Integer(monthDayYear[2]);
		if(expectedYear<2000){
			expectedYear = expectedYear+2000;
		}
		int expectedMonth = new Integer(monthDayYear[0])-1;
		int expectedDay = new Integer(monthDayYear[1]);
		Calendar expiryFromRule=null;
		try {
			expiryFromRule = efrl.getExpiry(shortName);
		} catch (Exception e) {
			// retry it for debugging
			try {
				efrl.getExpiry(shortName);
			} catch (Exception e1) {
			}
			e.printStackTrace();
		}
		if(expiryFromRule==null){
			Utils.prtObErrMess(this.getClass(), " can't find rule for : "+shortName);
		}
		assertNotNull(expiryFromRule);
		int actualYear = expiryFromRule.get(Calendar.YEAR);
		int actualMonth = expiryFromRule.get(Calendar.MONTH);
		int actualDay = expiryFromRule.get(Calendar.DAY_OF_MONTH);
		if(expectedYear!=actualYear){
			Utils.prtObErrMess(this.getClass(), " actualYear = "+actualYear+" expectedYear : "+expectedYear+" "+shortName);
		}
		if(expectedMonth!=actualMonth){
			Utils.prtObErrMess(this.getClass(), " actualMonth = "+actualMonth+" expectedMonth : "+expectedMonth+" "+shortName);
		}
		if(expectedDay!=actualDay){
			Utils.prtObErrMess(this.getClass(), " actualDay = "+actualDay+" expectedDay : "+expectedDay+" "+shortName);
		}
		assertEquals(expectedYear,actualYear);
		assertEquals(expectedMonth,actualMonth);
		if(expectedDay!=actualDay){
			Utils.prtObErrMess(this.getClass(), shortName + " expected day != actual day");
		}
		assertEquals(expectedDay,actualDay);

	}
	
	public void testNG2015(){
		//Trading terminates three business days prior to the first calendar day of the delivery month
		// NG arrays for 2015 months and days
		runTestUsingCmeContractData("NG","NG","NYMEX","USD","XNYM",null,null,null);
		String contractFirstPart = "NG.FUT.NYMEX.USD.";
		// create offsets
		int[] yearArray   ={2014,2015,2015,2015,2015,2015,2015,2015,2015,2015,2015,2015};
		int[] monthArray = {12,1,2,3,4,5,6,7,8,9,10,11};
		int[] dayArray = {29,28,25,27,28,27,26,29,27,28,28,25};
		int contractYear = 2015;
		runTests("US",contractFirstPart,yearArray,monthArray,dayArray,contractYear);
	}
	
	public void testCL2015(){
		//Trading terminates at the close of business on 
		// the third business day prior to the 25th calendar day of the month 
		//  preceding the delivery month. 
		//  If the 25th calendar day of the month is a non-business day, 
		//   trading shall cease on the third business day prior to 
		//    the business day preceding the 25th calendar day.		
		String contractFirstPart = "CL.FUT.NYMEX.USD.";
		int[] yearArray   ={2014,2015,2015,2015,2015,2015,2015,2015,2015,2015,2015,2015};
		int[] monthArray = {12,1,2,3,4,5,6,7,8,9,10,11};
		int[] dayArray = {19,20,20,20,21,19,22,21,20,22,20,20};
		int contractYear = 2015;
		runTests("US",contractFirstPart,yearArray,monthArray,dayArray,contractYear);
	}
	
	public void testCLFuturesFromCmeCalendarData(){
		compareActualWithExpected("11/16/2012", "CL.FUT.NYMEX.USD.201212",  new ExpiryFromListOfExpiryRules());
		runTestUsingCmeContractData("CL","CL","NYMEX","USD","XNYM",null,null,null);
	}

	public void testCLOptions1FromCmeCalendarData(){
		runTestUsingCmeContractData("LO","LO","NYMEX","USD","XNYM","P","60.00",null);
	}

	public void testCLOptions2FromCmeCalendarData(){
		runTestUsingCmeContractData("LO","CL","NYMEX","USD","XNYM","P","60.00",null);
	}

	public void testHO2012(){
		//Trading terminates at the close of business on 
		//  the last business day of the month preceding the delivery month.
		runTestUsingCmeContractData("HO","HO","NYMEX","USD","XNYM",null,null,null);
		runTestUsingCmeContractData("OH","OH","NYMEX","USD","XNYM","P","2.000",null);
		runTestUsingCmeContractData("OH","HO","NYMEX","USD","XNYM","P","2.000",null);

		String contractFirstPart = "HO.FUT.NYMEX.USD.";
		int[] yearArray   ={2011,2012,2012,2012,2012,2012,2012,2012,2012,2012,2012,2012};
		int[] monthArray = {12,1,2,3,4,5,6,7,8,9,10,11};
		int[] dayArray = {30,31,29,30,30,31,29,31,31,28,31,30};
		int contractYear = 2012;
		runTests("US",contractFirstPart,yearArray,monthArray,dayArray,contractYear);
		
	}

	public void testRB(){
		runTestUsingCmeContractData("RB","RB","NYMEX","USD","XNYM",null,null,null);
		runTestUsingCmeContractData("OB","OB","NYMEX","USD","XNYM","P","2.000",null);
		runTestUsingCmeContractData("OB","RB","NYMEX","USD","XNYM","P","2.000",null);

	}
	public void testGoil2012(){
		// Trading shall cease at 12:00 hours, 2 business days prior to the 14
		//  calendar day of the delivery month
		String contractFirstPart = "GOIL.FUT.IPE.USD.";
		int[] yearArray   ={2012,2012,2012,2012,2012,2012,2012,2012,2012,2012,2012,2012};
		int[] monthArray = {1,2,3,4,5,6,7,8,9,10,11,12};
		int[] dayArray = {12,10,12,12,10,12,12,10,12,11,12,12};
		int contractYear = 2012;
		runTests("UK",contractFirstPart,yearArray,monthArray,dayArray,contractYear);
	}
	
	public void testGOILFromIceExpiryData(){
		runTestsUsingIceExpiryData("GOIL","G.FUT.IPE","IPE","IPE",null,null,null);
	}

	public void testIceWTIFromIceExpiryData(){
//		compareActualWithExpected("11/15/2012", "WTI.FUT.IPE.USD.201212",  new ExpiryFromListOfExpiryRules());
		compareActualWithExpected("11/15/2012", "WTI.FUT.ICE.USD.201212",  new ExpiryFromListOfExpiryRules());

//		runTestsUsingIceExpiryData("WTI","T.FUT.IPE",null,null,null);
		runTestsUsingIceExpiryData("WTI","T.FUT.IPE","ICE","IPE",null,null,null);
	}


	public void testIceHOFromIceExpiryData(){
//		runTestsUsingIceExpiryData("HOIL","T.FUT.IPE","IPE","IPE",null,null,null);
//		runTestsUsingIceExpiryData("WTI","T.FUT.ICE",null,null,null);
	}


	public void testIceRBOBOFromIceExpiryData(){
		runTestsUsingIceExpiryData("RBOB","N.FUT.IPE","IPE","IPE",null,null,null);
//		runTestsUsingIceExpiryData("RBOB","N.FUT.ICE",null,null,null);
	}

	public void testIceSBFromIceExpiryData(){
		runTestsUsingIceExpiryData("SB","SB.FUT.NYBOT","NYBOT","NYBOT",null,null,null);
//		runTestsUsingIceExpiryData("WTI","T.FUT.ICE",null,null,null);
	}

	public void testIceSBOptFromIceExpiryData(){
		ExpiryFromListOfExpiryRules ex = new ExpiryFromListOfExpiryRules();
		Calendar c = ex.getExpiry("SB.FOP.NYBOT.USD.201401.C.0.2500");
		assertEquals(2013,c.get(Calendar.YEAR));
		assertEquals(11,c.get(Calendar.MONTH));
		assertEquals(16,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201402.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(0,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201403.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(1,c.get(Calendar.MONTH));
		assertEquals(18,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201404.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(2,c.get(Calendar.MONTH));
		assertEquals(17,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201405.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(3,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201406.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(4,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201407.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(5,c.get(Calendar.MONTH));
		assertEquals(16,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201408.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(6,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201409.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(7,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201410.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(8,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201411.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(9,c.get(Calendar.MONTH));
		assertEquals(15,c.get(Calendar.DAY_OF_MONTH));
		
		c = ex.getExpiry("SB.FOP.NYBOT.USD.201412.C.0.2500");
		assertEquals(2014,c.get(Calendar.YEAR));
		assertEquals(10,c.get(Calendar.MONTH));
		assertEquals(17,c.get(Calendar.DAY_OF_MONTH));
		
	}

	
	public void testComexGold(){
		// Trading shall cease at 12:00 hours, 2 business days prior to the 14
		//  calendar day of the delivery month
		// USE DATA BASE TO CHECK LOGIC
		runTestUsingCmeContractData("GC","GC","COMEX","USD","XCMX",null,null,null);
	}

	public void testComexGoldOpt1(){
		runTestUsingCmeContractData("OG","OG","COMEX","USD","XCMX","C","1500.00",null);
	}
	public void testComexGoldOpt2(){
		runTestUsingCmeContractData("OG","GC","COMEX","USD","XCMX","C","1500.00",null);
	}
	public void testComexSilver(){
		// Trading shall cease at 12:00 hours, 2 business days prior to the 14
		//  calendar day of the delivery month
		// USE DATA BASE TO CHECK LOGIC
		runTestUsingCmeContractData("SI","SI","COMEX","USD","XCMX",null,null,null);
		runTestUsingCmeContractData("SO","SO","COMEX","USD","XCMX","C","10.00",null);
		runTestUsingCmeContractData("SO","SI","COMEX","USD","XCMX","C","10.00",null);
	}
	
	public void testComexCopper(){
		// Trading shall cease at 12:00 hours, 2 business days prior to the 14
		//  calendar day of the delivery month
		// USE DATA BASE TO CHECK LOGIC
		runTestUsingCmeContractData("HG","HG","COMEX","USD","XCMX",null,null,null);
		runTestUsingCmeContractData("HXE","HX","COMEX","USD","XCMX","C","10.00",null);
		runTestUsingCmeContractData("HXE","HG","COMEX","USD","XCMX","C","10.00",null);
	}


	public void testNymexMetals(){
		runTestUsingCmeContractData("PL","PL","NYMEX","USD","XNYM",null,null,null);
		runTestUsingCmeContractData("PO","PO","NYMEX","USD","XNYM","P","1000.00",null);
		runTestUsingCmeContractData("PO","PL","NYMEX","USD","XNYM","P","1000.00",null);
	}

	public void testCmeCorn(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Corn"));
		runTestUsingCmeContractData("C","ZC","ECBOT","USD","XCBT",null,null,additionalConditions);
		// test regular corn options
		additionalConditions.add(new Tuple<String, String>("Option_Type","AME"));
		// only get normal futures months - not serial months
		additionalConditions.add(new Tuple<String, String>("Contract_Code","C(H|K|N|U)(1|2|3|4|5|6)"));
		runTestUsingCmeContractData("C","ZC","ECBOT","USD","XCBT","P","400.00",additionalConditions);
	}

	public void testCmeWheat(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Wheat"));
		runTestUsingCmeContractData("W","ZW","ECBOT","USD","XCBT",null,null,additionalConditions);
		// test regular corn options
		additionalConditions.add(new Tuple<String, String>("Option_Type","AME"));
		// only get normal futures months - not serial months
		additionalConditions.add(new Tuple<String, String>("Contract_Code","W(H|K|N|U|Z)(1|2|3|4|5|6)"));
		runTestUsingCmeContractData("W","ZW","ECBOT","USD","XCBT","P","400.00",additionalConditions);
	}

	public void testCmeSoybeans(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Soybean"));
		runTestUsingCmeContractData("S","ZS","ECBOT","USD","XCBT",null,null,additionalConditions);
		// test regular corn options
		additionalConditions.add(new Tuple<String, String>("Option_Type","AME"));
		// only get normal futures months - not serial months
		additionalConditions.add(new Tuple<String, String>("Contract_Code","S(F|H|K|N|Q|U|X)(1|2|3|4|5|6)"));
		runTestUsingCmeContractData("S","ZS","ECBOT","USD","XCBT","P","1000.00",additionalConditions);
	}

	public void testCmeSoyMeal(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Soybean Meal"));
		runTestUsingCmeContractData("06","ZM","ECBOT","USD","XCBT",null,null,additionalConditions);
	}

	public void testCmeSoyMealOptions(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Soybean Meal"));
		// test regular corn options
		additionalConditions.add(new Tuple<String, String>("Option_Type","AME"));
		// only get normal futures months - not serial months
		additionalConditions.add(new Tuple<String, String>("Contract_Code","06(F|H|K|N|Q|U|X)(1|2|3|4|5|6)"));
		runTestUsingCmeContractData("06","ZM","ECBOT","USD","XCBT","P","1000.00",additionalConditions);
		
	}

	public void testCmeSoyBeanOil(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Soybean Oil"));
		runTestUsingCmeContractData("07","ZL","ECBOT","USD","XCBT",null,null,additionalConditions);
	}

	public void testCmeSoyBeanOilOptions(){
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Soybean Oil"));
		// test regular corn options
		additionalConditions.add(new Tuple<String, String>("Option_Type","AME"));
		// only get normal futures months - not serial months
		additionalConditions.add(new Tuple<String, String>("Contract_Code","07(F|H|K|N|Q|U|X)(1|2|3|4|5|6)"));
		runTestUsingCmeContractData("07","ZL","ECBOT","USD","XCBT","P","1000.00",additionalConditions);
		
	}
	
	public void testEminis(){
		runTestUsingCmeContractData("ES","ES","GLOBEX","USD","XCME",null,null,null);
		runTestUsingCmeContractData("ES","ES","GLOBEX","USD","XCME","P","1100.00",null);

	}
	
	public void testEurodollar(){
//		runTestUsingCmeContractData("ED","ED","GLOBEX","USD","XCME",null,null,null);
		runTestUsingCmeContractData("ED","ED","CME","USD","XCME",null,null,null);
		List<Tuple<String, String>> additionalConditions = new ArrayList<Tuple<String,String>>();
		additionalConditions.add(new Tuple<String, String>("Commodity_Name","Eurodollar"));
		// test regular corn options
		additionalConditions.add(new Tuple<String, String>("Option_Type","AME"));
		// only get normal futures months - not serial months
		additionalConditions.add(new Tuple<String, String>("Contract_Code","ED(H|M|U|Z)(1|2|3|4|5|6)"));
//		runTestUsingCmeContractData("ED","ED","GLOBEX","USD","XCME","P","100.00000000",additionalConditions);
		runTestUsingCmeContractData("ED","ED","CME","USD","XCME","P","100.00000000",additionalConditions);

	}
	
	
	
}
