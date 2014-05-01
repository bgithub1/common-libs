package com.billybyte.marketdata.futures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecEnums;
import com.billybyte.marketdata.ShortNameInfo;
import com.billybyte.marketdata.ShortNameProcessor;
import com.billybyte.marketdata.SecEnums.DayType;
import com.billybyte.marketdata.SecEnums.SecSymbolType;
/**
 * This class creates Last Trading Day rules for many commodity products.
 * @author bperlman1
 *
 */
public class ExpiryFromListOfExpiryRules implements ExpiryRuleInterface {
	final Map<SecSymbolType, Map<String, ExpiryRuleInterface>> expiryRulesMap= new ConcurrentHashMap<SecSymbolType, Map<String,ExpiryRuleInterface>>();

	Set<SecSymbolType> secSymbolTypesWithExpiries = 
		new HashSet<SecSymbolType>(Arrays.asList(new SecSymbolType[]{
				SecSymbolType.FUT,
				SecSymbolType.FOP,
				SecSymbolType.OPT}));
	
	ShortNameProcessor sproc = new ShortNameProcessor(".",null);
	ExpiryRuleInterface noExpiryRule = new ExpiryFixedDate();
	
	
	public ExpiryFromListOfExpiryRules(){
		// build rules
		// STK
		addRuleByTypeAndSymbol(SecSymbolType.STK,"STK",new ExpiryFixedDate());
		// OPT
		doOpt();
		
		// FUT 
		doCl();
		doNg();
		doHoRb();
		doGoil();
		doWTI();
		doHoRbIpe();
		doBrent();
		doComexGold();
		doComexSilver();
		doComexCopper();
		doNymexMetals();
		doGrains();
		doEMini();
		doNybot();
		doBonds();
		doCmeCurrencies();
		doMeats();
		doCS();
		doIndices();
		doOtherAgs();
	}

		
	@Override
	public Calendar getExpiry(String shortName) {
		ShortNameInfo sni =  sproc.getShortNameInfo(shortName);
		ExpiryRuleInterface rule;
		if(secSymbolTypesWithExpiries.contains(sni.getSymbolType())){
//			sni = sprocForExpiryTypes.getShortNameInfo(shortName);
			Map<String,ExpiryRuleInterface> expiryMapPerType = expiryRulesMap.get(sni.getSymbolType());
			rule = expiryMapPerType.get(sni.getSymbol());
		}else{
			rule = noExpiryRule;
		}
		if(rule==null){
			Utils.prtObErrMess(this.getClass(), " can't find expiry rule for shortName: "+shortName);
			return null;
		}
		Calendar ret = rule.getExpiry(shortName);
		return ret;
	}
	
	public void newRuleMap(SecSymbolType type, Map<String, ExpiryRuleInterface> ruleMap){
		expiryRulesMap.put(type, ruleMap);
	}
	
	public void addRuleByTypeAndSymbol(SecSymbolType type,String symbol,ExpiryRuleInterface rule){
		if(!expiryRulesMap.containsKey(type)){
			newRuleMap(type, new ConcurrentHashMap<String, ExpiryRuleInterface>());
		}
		Map<String, ExpiryRuleInterface> thisMap = expiryRulesMap.get(type);
		thisMap.put(symbol,rule);
	}

	/**
	 * add an expiry rule for determining last trading days to the list of rules for this
	 *   instance of ExpiryFromListOfExpiryRules
	 * @param symbolName
	 * @param type
	 * @param locale
	 * @param offsetValues
	 * @param offsetTypes
	 */
	public void addRule(String symbolName,SecSymbolType type, 
			String locale,int[] offsetValues, SecEnums.DayType[] offsetTypes){
		if(type==null){
			throw Utils.IllArg(this,symbolName+ " null SecSymbolType");
		}
		if(symbolName==null || symbolName.compareTo(" ")<=0){
			throw Utils.IllArg(this,symbolName +" null or bad symbol");
		}
		if(locale==null || locale.compareTo(" ")<=0){
			throw Utils.IllArg(this,symbolName +" "+ "null or bad locale");
		}
		if(offsetValues==null || offsetValues.length<=0){
			throw Utils.IllArg(this, symbolName +" "+"null or zero length offsetValue array");
		}
		if(offsetTypes==null || offsetTypes.length<=0){
			throw Utils.IllArg(this, symbolName +" "+"null or zero length offsetTypes array");
		}

		if(offsetValues.length!=offsetTypes.length){
			throw Utils.IllArg(this,symbolName +" "+ "offset types array has length: "+offsetTypes.length+ " offset values has length: "+offsetValues.length);
			
		}

		List<DayOffset> timeOffsetList = new ArrayList<DayOffset>();
		for(int i = 0;i<offsetValues.length;i++){
			timeOffsetList.add(new DayOffset(offsetValues[i],offsetTypes[i]));
		}
		ExpiryFromTimeOffsetList efrl = new ExpiryFromTimeOffsetList(locale,timeOffsetList);
		addRuleByTypeAndSymbol(type, symbolName,efrl);
		
		
	}
	
//	private boolean isExpiryType(SecSymbolType symbolType){
//		if(this.secSymbolTypesWithExpiries.contains(symbolType)){
//			return true;
//		}else{
//			return false;
//		}
//	}

	protected void doOpt(){
		addRule("OPT",
				SecSymbolType.OPT,"US",
				new int[]{3,-1},
				new DayType[]{DayType.NTH_FRIDAY,
						DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY});
		
	}
	
	protected void doCl(){
		//  CL
		//Trading terminates at the close of business on 
		// the third business day prior to the 25th calendar day of the month 
		//  preceding the delivery month. 
		//  If the 25th calendar day of the month is a non-business day, 
		//   trading shall cease on the third business day prior to 
		//    the business day preceding the 25th calendar day.		
		addRule("CL",
				SecSymbolType.FUT,"US",
				new int[]{-1,24,0,-3,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY}
		);
		// 3 business days before CL
		addRule("CL",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("LO",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("LCO",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("LCE",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		// 1 business days before CL
		addRule("WA",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("WB",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("WC",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("WM",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		addRule("WZ",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);

	}
	

	protected void doCS(){
		//  CSX, AOX, MPX, ATX, RLX, RAX
		//Trading ends the last business day of the calendar month.
		addRule("CSX",
				SecSymbolType.FUT,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("AOX",
				SecSymbolType.FOP,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("AAO",
				SecSymbolType.FOP,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("RLX",
				SecSymbolType.FUT,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("RA",
				SecSymbolType.FOP,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("MPX",
				SecSymbolType.FUT,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("ATX",
				SecSymbolType.FOP,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
	}

	
	protected void doWTI(){
		//  WTI
		//Trading terminates at the close of business on 
		// the fourth business day prior to the 25th calendar day of the month 
		//  preceding the delivery month. 
		//  If the 25th calendar day of the month is a non-business day, 
		//   trading shall cease on the third business day prior to 
		//    the business day preceding the 25th calendar day.		

		addRule("WTI",
				SecSymbolType.FUT,"US",
				new int[]{-1,24,0,-3,-1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		
		// 2 business days before futures
		addRule("WTI",
				SecSymbolType.FOP,"US",
				new int[]{-1,24,0,-3,-1,-1,-2},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_WEEK_OF_THANKSGIVING_APPLY_BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
	}

	
	protected void doNg(){
		//Trading terminates three business days prior to 
		//  the first calendar day of the delivery month
		addRule("NG",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("HH",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("NN",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("NP",
				SecSymbolType.FUT,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("iNG",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G2",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G3",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G3B",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);

		addRule("G4",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G4X",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G5",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G6",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G6B",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G7",
				SecSymbolType.FUT,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		
		
		//1 business day before futures
		addRule("NG",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("ON",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("LN",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("LNE",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G2",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G3",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G3B",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G4",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G4X",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G5",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G6",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G6B",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("G7",
				SecSymbolType.FOP,"US",
				new int[]{-3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("iNG",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("KD",
				SecSymbolType.FOP,"US",
				new int[]{0},
				new DayType[]{DayType.SAME_DAY}
		);
		addRule("NX",
				SecSymbolType.FUT,"US",
				new int[]{-1},
				new DayType[]{DayType.BUSINESS_DAY}
		);

	}
	
	
	protected void doHoRb(){
		//Trading terminates at the close of business on 
		//  the last business day of the month preceding the delivery month.
		addRule("HO",
				SecSymbolType.FUT,"US",
				new int[]{-1},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("RB",
				SecSymbolType.FUT,"US",
				new int[]{-1},
				new DayType[]{DayType.BUSINESS_DAY}
		);

		// 3 business days before futures
		addRule("HO",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("RB",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);

		addRule("OH",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("OB",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("EH", // ethanol
				SecSymbolType.FUT,"US",
				new int[]{3},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("OEH", // TODO probably not right
				SecSymbolType.FOP,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
	}
	
	
	protected void doHoRbIpe(){
		//Trading terminates at the close of business on 
		//  the last business day of the month preceding the delivery month.
		addRule("HOIL",
				SecSymbolType.FUT,"US",
				new int[]{-2},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("RBOB",
				SecSymbolType.FUT,"US",
				new int[]{-2},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("HOIL",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);
		addRule("RBOB",
				SecSymbolType.FOP,"US",
				new int[]{-4},
				new DayType[]{DayType.BUSINESS_DAY}
		);

	}
	
	protected void doGoil(){
		// Trading shall cease at 12:00 hours, 2 business days prior to the 14
		//  calendar day of the delivery month
		addRule("GOIL",
				SecSymbolType.FUT,"UK",
				new int[]{13,-2},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY}
		);
		// 5 buisness days before futures
		addRule("GOIL",
				SecSymbolType.FOP,"UK",
				new int[]{13,-7},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY}
		);
	}
	
	protected void doNybot(){
		// sugar
		/**
		 * Last business day of the month preceding the delivery month (except January, 
		 * which is the second business day before the 24th calendar day of the prior month).
		 */
		addRule("SB",
				SecSymbolType.FUT,"US",
				new int[]{-1,24,-2},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.IF_DECEMBER_APPLY_CALENDAR_DAY,
					DayType.IF_DECEMBER_APPLY_BUSINESS_DAY}
		);
		addRule("SB",
				SecSymbolType.FOP,"US",
				new int[]{-1,14,1,1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.IF_WEEKEND_APPLY_BUSINESS_DAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY}
		);
		addRule("SF", // sugar #16
				SecSymbolType.FUT,"US",
				new int[]{-1,8,1,1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.CALENDAR_DAY,
					DayType.IF_WEEKEND_APPLY_BUSINESS_DAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY}
		);
		
		// cocoa
		/**
		 * 11 business days prior to last business day of delivery month 
		 */
		addRule("CC",
				SecSymbolType.FUT,"US",
				new int[]{1,-12},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);

		addRule("CC",
				SecSymbolType.FOP,"US",
				new int[]{-1,1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_FRIDAY}
		);
		
		// cotton
		/**
		 * 17 business days prior to last business day of delivery month 
		 */
		addRule("CT",
				SecSymbolType.FUT,"US",
				new int[]{1,-17},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("CT",
				SecSymbolType.FOP,"US",
				new int[]{1,-5,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		

		// oj
		/**
		 * 17 business days prior to last business day of delivery month 
		 */
		addRule("OJ",
				SecSymbolType.FUT,"US",
				new int[]{1,-6},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("OJ",
				SecSymbolType.FOP,"US",
				new int[]{-1,3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_FRIDAY}
		);

		// KC
		/**
		 * 17 business days prior to last business day of delivery month 
		 */
		addRule("KC",
				SecSymbolType.FUT,"US",
				new int[]{1,-9},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		addRule("KC",
				SecSymbolType.FOP,"US",
				new int[]{-1,2},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_FRIDAY}
		);
		
/*
 		Last Trading Day: Trading ceases at 10:16 Eastern time two days prior to 
 		settlement (see next entry).
		Final Settlement: The US Dollar Index is physically settled 
		on the third Wednesday of the expiration month 
		against six component currencies (euro, Japanese yen, British pound, Canadian dollar, Swedish krona and Swiss franc) in their respective percentage weights in the Index. Settlement rates may be quoted to three decimal places.
 * 
 */
		addRule("DX",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY}
		);

		// Two Fridays before the third Wednesday of the expiring 
		//   contract month
		addRule("DX",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
				DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY}
		);


	}
	
	protected void doBrent(){
		//Trading ceases at the close of business on 
		// the business day prior to the 15th calendar day prior to the start of the delivery month. 
		//  If the 15th calendar day is a non-business day in London, 
		//  trading shall cease on the business day ( in London) 
		//    immediately preceeding the day preceding the 15th calendar day.
		addRule("COIL",
				SecSymbolType.FUT,"UK",
				new int[]{-14,0,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);
		// options 3 business days before futures
		addRule("COIL",
				SecSymbolType.FOP,"UK",
				new int[]{-14,0,-4},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY}
		);

	}
	
	protected void doComexGold(){
		// futures
		//Trading terminates on the third last business day of the delivery month.
		addRule("GC",
				SecSymbolType.FUT,"US",
				new int[]{1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		
		/**
		 * Expiration occurs four business days prior to the end of the month preceding 
		 * the option contract month. 
		 * If the expiration day falls on a Friday or immediately prior to an Exchange holiday, 
		 * expiration will occur on the previous business day.
		 */
		addRule("GC",
				SecSymbolType.FOP,"US",
				new int[]{-4,-1,-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
		);

		addRule("OG",
				SecSymbolType.FOP,"US",
				new int[]{-4,-1,-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
		);
		
	}
	
	protected void doComexSilver(){
	//Trading terminates on the third last business day of the delivery month.
		addRule("SI",
				SecSymbolType.FUT,"US",
				new int[]{1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		
		/**
		 * Expiration occurs four business days prior to the end of the month preceding 
		 * the option contract month. 
		 * If the expiration day falls on a Friday or immediately prior to an Exchange holiday, 
		 * expiration will occur on the previous business day.
		 */
		addRule("SI",
				SecSymbolType.FOP,"US",
				new int[]{-4,-1,-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
		);
		addRule("SO",
				SecSymbolType.FOP,"US",
				new int[]{-4,-1,-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
					DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
		);
		
	}
	
	protected void doComexCopper(){
		//Trading terminates on the third last business day of the delivery month.
			addRule("HG",
					SecSymbolType.FUT,"US",
					new int[]{1,-3},
					new DayType[]{
						DayType.FULL_MONTH,
						DayType.BUSINESS_DAY}
			);
			
			/**
			 * Expiration occurs four business days prior to the end of the month preceding 
			 * the option contract month. 
			 * If the expiration day falls on a Friday or immediately prior to an Exchange holiday, 
			 * expiration will occur on the previous business day.
			 */
			addRule("HX",
					SecSymbolType.FOP,"US",
					new int[]{-4,-1,-1,-1},
					new DayType[]{
						DayType.BUSINESS_DAY,
						DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
						DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
						DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
			);
			addRule("HXE",
					SecSymbolType.FOP,"US",
					new int[]{-4,-1,-1,-1},
					new DayType[]{
						DayType.BUSINESS_DAY,
						DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
						DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
						DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
			);
			addRule("HG",
					SecSymbolType.FOP,"US",
					new int[]{-4,-1,-1,-1},
					new DayType[]{
						DayType.BUSINESS_DAY,
						DayType.IF_FRIDAY_APPLY_BUSINESS_DAY,
						DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY,
						DayType.IF_WEDNESDAY_BEFORE_THANKSGIVING_APPLY_BUSINESS_DAY}
			);
			
		}

	protected void doNymexMetals(){
		//Trading terminates on the third last business day of the delivery month.
		addRule("PL",
				SecSymbolType.FUT,"US",
				new int[]{1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);
		
		addRule("PA",
				SecSymbolType.FUT,"US",
				new int[]{1,-3},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY}
		);

		/**
		 * Expiration occurs at the close of trading on the third Wednesday of the 
		 * month preceding the option contract month. 
		 * In the event that such business day precedes an Exchange holiday, 
		 * the expiration date shall be the preceding business day.
		 */
		addRule("PAO",
				SecSymbolType.FOP,"US",
				new int[]{-1,3,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_WEDNESDAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY}
		);
		addRule("PA",
				SecSymbolType.FOP,"US",
				new int[]{-1,3,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_WEDNESDAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY}
		);
		addRule("PO",
				SecSymbolType.FOP,"US",
				new int[]{-1,3,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_WEDNESDAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY}
		);
		addRule("PL",
				SecSymbolType.FOP,"US",
				new int[]{-1,3,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_WEDNESDAY,
					DayType.IF_HOLIDAY_APPLY_BUSINESS_DAY}
		);
		
		
	}


	
	
	

	
	protected void doGrains(){
		// for all of the following contracts which are optins, the First Notice Day is the last
		//   business day of the month preceding the contract month
		
		// corn The business day prior to the 15th calendar day of the contract month.
		addRule("ZC",
				SecSymbolType.FUT,"US",
				new int[]{14,-1,1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_BEFORE_COLUMBUS_DAY_APPLY_CALENDAR_DAY}
		);
		// corn opt 
		/**
		 * For standard option contracts: 
		 * The last Friday preceding the first notice day of the corresponding 
		 * corn futures contract month by at least two business days. 
		*/
		/**
		 * 
		 * For serial option contracts: 
		 * The last Friday which precedes by at least two business days 
		 * the last business day of the month preceding the option month.
		 */
		addRule("ZC",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		addRule("OZC",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		
		// soybean The business day prior to the 15th calendar day of the contract month.
		addRule("ZS",
				SecSymbolType.FUT,"US",
				new int[]{14,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY}
		);

		// soy opt 
		/**
		 * For Standard and Serial Option Contracts: 
		 * The last Friday which precedes by at least two business days 
		 * the last business day of the month preceding the option month.
		 */
		addRule("ZS",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);

		addRule("OZS",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);

		// wheat he business day prior to the 15th calendar day of the contract month.
		addRule("ZW",
				SecSymbolType.FUT,"US",
				new int[]{14,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY}
		);
		// wheat opt
		/**
		 * For standard option contracts: 
		 * The last Friday preceding the first notice day of the corresponding 
		 * wheat futures contract month by at least two business days.
		 * For serial option contracts: 
		 * The last Friday which precedes by at least two business days 
		 * the last business day of the month preceding the option month.
		 */
		addRule("ZW",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		
		addRule("OZW",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);

		// soybean oil he business day prior to the 15th calendar day of the contract month.
		addRule("ZL",
				SecSymbolType.FUT,"US",
				new int[]{14,-1,3},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_BEFORE_COLUMBUS_DAY_APPLY_CALENDAR_DAY}
		);

		// soyoil opt 
		/**
		 * For standard option contracts: 
		 * The last Friday preceding the first notice day of the corresponding 
		 * Soybean Oil futures contract month by at least two business days. 
		 * For serial option contracts: 
		 * The last Friday which precedes by at least two business days 
		 * the last business day of the month preceding the option month.
		 */
		addRule("ZL",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		
		addRule("OZL",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		// soymeal
		addRule("ZM",
				SecSymbolType.FUT,"US",
				new int[]{14,-1,3},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.IF_FRIDAY_BEFORE_COLUMBUS_DAY_APPLY_CALENDAR_DAY}
		);


		// soymeal opt
		/**
		 * For standard option contracts: 
		 * The last Friday preceding the first notice day of the corresponding 
		 * Soybean Meal futures contract month by at least two business days.
		 * For serial option contracts: 
		 * The last Friday which precedes by at least two business days 
		 * the last business day of the month preceding the option month.
		 */
		addRule("ZM",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);
		
		addRule("OZM",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY}
		);

	
	
	}
	
	protected void doEMini(){
		// ES e-mini sp
		/*
		 * Trading can occur up to 8:30 a.m. on the 3rd Friday of the contract month
		 */
		addRule("ES",
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ES",
				SecSymbolType.FOP,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		
		addRule("NQ",
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("NQ",
				SecSymbolType.FOP,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("EMD",
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ZD",
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("OZD",
				SecSymbolType.FOP,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("YM",
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("YM",
				SecSymbolType.FOP,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("SMC",
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ND", // TODO NASDAQ 100 different for open outcry and GLOBEX?
				SecSymbolType.FUT,"US",
				new int[]{3,-1},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.NTH_THURSDAY,
				}
		);
		addRule("ND", // TODO NASDAQ 100 different for open outcry and GLOBEX?
				SecSymbolType.FOP,"US",
				new int[]{3,-1},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.NTH_THURSDAY,
				}
		);
	}
	
	protected void doBonds(){
		// ZN, ZB
		addRule("ZN",
				SecSymbolType.FUT,"US",
				new int[]{1,-1,-7},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ZN",
				SecSymbolType.FOP,"US",
				new int[]{-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY
					}
		);
		
		addRule("OZN",
				SecSymbolType.FOP,"US",
				new int[]{-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("ZB",
				SecSymbolType.FUT,"US",
				new int[]{1,-1,-7},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ZB",
				SecSymbolType.FOP,"US",
				new int[]{-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY
					}
		);
		
		addRule("OZB",
				SecSymbolType.FOP,"US",
				new int[]{-1,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY
					}
		);
		addRule("ZF",
				SecSymbolType.FUT,"US",
				new int[]{1,0},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY
					}
		);
		
		addRule("OZF",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1,0},
				new DayType[]{
				DayType.BUSINESS_DAY,
				DayType.NTH_FRIDAY,
				DayType.BUSINESS_DAY
				}
		);
		addRule("ZT",
				SecSymbolType.FUT,"US",
				new int[]{1,0},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY
					}
		);
		addRule("OZT",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1,0},
				new DayType[]{
				DayType.BUSINESS_DAY,
				DayType.NTH_FRIDAY,
				DayType.BUSINESS_DAY
				}
		);
		addRule("UB",
				SecSymbolType.FUT,"US",
				new int[]{1,0,-7},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY,
					DayType.BUSINESS_DAY
					}
		);
		
		addRule("OUB",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY
					}
		);
		addRule("ZQ", // 30 day Fed Funds
				SecSymbolType.FUT,"US",
				new int[]{1,0},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY,
					}
		);
		addRule("ZQ", // 30 day Fed Funds
				SecSymbolType.FOP,"US",
				new int[]{1,0},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY,
					}
		);
		// 
		
/**
 The second London bank business day prior to the third Wednesday of the contract expiry month.
  Trading in the expiring contract closes at 11:00 a.m. London Time on the last trading day.
 * 		
 */
		
		addRule("ED",
				SecSymbolType.FUT,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);
		
		addRule("ED",
				SecSymbolType.FOP,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);

		addRule("SA",
				SecSymbolType.FUT,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);
		
		addRule("OSA",
				SecSymbolType.FOP,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);

		addRule("GE",
				SecSymbolType.FUT,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);
		addRule("GE",
				SecSymbolType.FOP,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);
		addRule("SR",
				SecSymbolType.FUT,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);
		addRule("OSR",
				SecSymbolType.FOP,"UK",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY,
					}
		);		
	
	}
	
	protected void doCmeCurrencies(){
		//AUDUSD
		addRule("6A",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6A",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("AUD",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("AUD",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		//GBPUSD
		addRule("6B",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6B",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("GBP",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("GBP",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		
		// CADUSD
		addRule("6C",
				SecSymbolType.FUT,"US",
				new int[]{3,-1},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6C",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("CAD",
				SecSymbolType.FUT,"US",
				new int[]{3,-1},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("CAD",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);
		

		//EURUSD
		addRule("6E",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6E",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);
		
		addRule("EUR",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("EUR",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		

		//JPYUSD
		addRule("6J",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6J",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);
		
		addRule("JPY",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("JPY",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		//BRLUSD
		addRule("6L",
				SecSymbolType.FUT,"US",
				new int[]{-1},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		addRule("6L",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("BRL",
				SecSymbolType.FUT,"US",
				new int[]{-1},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		addRule("BRL",
				SecSymbolType.FOP,"US",
				new int[]{-1},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		

		
		//MXNUSD
		addRule("6M",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6M",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("MXN",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("MXN",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		
		//NZDUSD
		addRule("6N",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6N",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("NZD",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("NZD",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		//CHFUSD
		addRule("6S",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6S",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);
		
		addRule("CHF",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("CHF",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);

		addRule("6Z",
				SecSymbolType.FUT,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("6Z",
				SecSymbolType.FOP,"US",
				new int[]{3,-2},
				new DayType[]{
					DayType.NTH_WEDNESDAY,
					DayType.NTH_FRIDAY
					}
		);
		
	}

	
	protected void doMeats(){
		// cattle
		addRule("LE",
				SecSymbolType.FUT,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY
					}
		);
		addRule("LC",
				SecSymbolType.FUT,"US",
				new int[]{1,-1},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.BUSINESS_DAY
					}
		);
		addRule("LE",
				SecSymbolType.FOP,"US",
				new int[]{1},
				new DayType[]{
					DayType.NTH_FRIDAY
					}
		);
		addRule("LC",
				SecSymbolType.FOP,"US",
				new int[]{1},
				new DayType[]{
					DayType.NTH_FRIDAY
					}
		);
		
		// hogs
		addRule("HE",
				SecSymbolType.FUT,"US",
				new int[]{9},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		addRule("LH",
				SecSymbolType.FUT,"US",
				new int[]{9},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		addRule("HE",
				SecSymbolType.FOP,"US",
				new int[]{9},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		addRule("LH",
				SecSymbolType.FOP,"US",
				new int[]{9},
				new DayType[]{
					DayType.BUSINESS_DAY
					}
		);
		addRule("GF",
				SecSymbolType.FUT,"US",
				new int[]{1,-1,-7},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_THURSDAY,
					DayType.IF_HOLIDAY_APPLY_CALENDAR_DAY
					}
		);
		addRule("GF",
				SecSymbolType.FOP,"US",
				new int[]{1,-1,-7},
				new DayType[]{
					DayType.FULL_MONTH,
					DayType.NTH_THURSDAY,
					DayType.IF_HOLIDAY_APPLY_CALENDAR_DAY
					}
		);
	}
	
	protected void doIndices(){
		addRule("SP",
				SecSymbolType.FUT,"US",
				new int[]{3,-1,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.NTH_THURSDAY,
					DayType.BUSINESS_DAY,
					}
		);
		addRule("SP", // TODO S&P Options, expiry is handled differently on serial options
				SecSymbolType.FOP,"US",
				new int[]{3,-1,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.NTH_THURSDAY,
					DayType.BUSINESS_DAY,
					}
		);
		// ICE CCI Index
		addRule("CI",
				SecSymbolType.FUT,"US",
				new int[]{2},
				new DayType[]{
					DayType.NTH_FRIDAY,
					}
		);
		addRule("RF", // Russell 1000 mini futures
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("RF",
				SecSymbolType.FOP,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY,
					}
		);
		addRule("TF", // Russell 2000 mini futures
				SecSymbolType.FUT,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("TF",
				SecSymbolType.FOP,"US",
				new int[]{3,0},
				new DayType[]{
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY,
					}
		);
	}

	protected void doOtherAgs(){
		// Class III Milk
		// TODO this is not right, expiration occurs on business day immediately preceding USDA announcement (not algorithmic)
		addRule("DC",
				SecSymbolType.FUT,"US",
				new int[]{2},
				new DayType[]{
					DayType.NTH_FRIDAY,
				}
		);
		addRule("DC",
				SecSymbolType.FOP,"US",
				new int[]{2},
				new DayType[]{
					DayType.NTH_FRIDAY,
				}
		);
		addRule("LBS",
				SecSymbolType.FUT,"US",
				new int[]{16,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY
				}
		);
		addRule("LBS",
				SecSymbolType.FOP,"US",
				new int[]{-1},
				new DayType[]{
					DayType.BUSINESS_DAY,
				}
		);
		addRule("ZO",
				SecSymbolType.FUT,"US",
				new int[]{15,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY
				}
		);
		addRule("OZO",
				SecSymbolType.FOP,"US",
				new int[]{-3,-1,0},
				new DayType[]{
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ZR",
				SecSymbolType.FUT,"US",
				new int[]{15,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY
					}
		);
		addRule("ZR",
				SecSymbolType.FOP,"US",
				new int[]{15,-3,-1},
				new DayType[]{
					DayType.CALENDAR_DAY,
					DayType.BUSINESS_DAY,
					DayType.NTH_FRIDAY
					}
		);	}

	
}
