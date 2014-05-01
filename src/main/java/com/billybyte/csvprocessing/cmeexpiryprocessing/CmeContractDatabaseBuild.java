package com.billybyte.csvprocessing.cmeexpiryprocessing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;

import com.billybyte.neodatis.NeoDatisSingleClassDataBase;
import com.thoughtworks.xstream.XStream;

public class CmeContractDatabaseBuild {

	/**
	 * Create a database of trading dates for every CME product that you specify in the regex string
	 *    (third argument from command line) in the main method of this class.
	 * 
	 * EXPLANATION BELOW:
	 * On the CME web site,  at the address:
	 *     ftp://ftp.cmegroup.com/pub/span/util/calendar_data.xml
	 *     
	 *     you will find the xml file that has every important date (e.g. LTD = last trading date)
	 *       for every contract listed on the cme - including OTC contracts.
	 *       this class is both the top level class in the xml graph of calendar_data.xml,
	 *       and has a main which creates and updates a 
	 *       {@code NewDatisSingleClassDatabase<String,CmeContract>} database that holds 
	 *       CmeContract class instances, which hold all of the date oriented data for each contract
	 *         the database is over 40MB, so it is not held in a java project, but anywhere on the users
	 *         file system.
	 * @param args 
	 * 		args[0] path or ftp address of cme calendar_data.xml file with product expiries
	 * 		args[1] path of {@code NewDatisSingleClassDatabase<String,CmeContract>} that holds contract data
	 * 		args[2] (optional) regexSeachCriteriaPattern (example below) that determines which
	 *               products will be placed into the NeoDatisSingleClassDatabase.
	 *               If this is missing, every product will be put in Database
	 *               EXAMPLE for all Nymex energies, Comex and Nymex metals, 
	 *               "^(CL|LO|NG|ON|HO|OH|RB|OB|WS|GC|OG|SI|SO|HO|HX|PL|PO|PA|PAO)([FGHJKMNQUVX])([0-9]){2,2}$"
	 *               This regex string is applied to the Exp_Contract_Code field of the calendar_data.xml
	 *               file to capture which contracts will eventually be inserted into the 
	 *               database.  
	 *               
	 *               
	 *               The 3 Current command line args are:
	 *               "../PortfolioData/CmeData/calendar_data.xml"  
	 *               "/Documents and Settings/Bill Perlman/My Documents/Downloads/cmeContracts.database " 
	 *               ^(CL|LO|NG|ON|HO|OH|RB|OB|WS|GC|OG|SI|SO|HG|HXE|PL|PO|PA|PAO|C|S|W|06|07|LC|LN|ES|EC|ED|SF|CD|AD|BP|JY|MP)([FGHJKMNQUVXZ])([0-9]){2,2}$"
	 */				

	static Pattern regexSeachCriteriaPattern=null;

	public static void main(String[] args) {
		
//		if(args==null || args.length<2){
//			throw UtilsComLib.IllArg(CmeCalendarData.class, " need 2 arguments.  arg0 = calendar_data.xml file ftp path. arg1 = NeoDatisSingleClassDatabase<String, CmeContract> path");
//		}
//		final String xmlDataPath = args[0];
		if(args==null || args.length<1){
			throw Utils.IllArg(CmeCalendarData.class, " need Regex string that defines contract names");
		}
 		final String xmlDataPath =  "../PortfolioData/CmeData/calendar_data.xml";
		
//		final String databaseOutputPath = args[1];
 		String dateTimeSuffix = Calendar.getInstance().getTime().toString().replace(" ", "_").replace(":", "_");
 		final String databaseOutputPath =  "../PortfolioData/CmeData/cmeContracts.database";
		final String databaseSavePath = databaseOutputPath+"."+dateTimeSuffix;
		if(args.length>2){
			regexSeachCriteriaPattern = Pattern.compile(args[2]);
		}

		// make a backup of old database, just in case
		try {
			Utils.copyFile(databaseOutputPath, databaseSavePath);
			Utils.deleteFile(databaseOutputPath);
		} catch (Exception e) {
			Utils.prtObErrMess(CmeContractDatabaseBuild.class, e.getMessage());
			return;
		}

		CmeCalendarData calData = getFromXstream(xmlDataPath);
		
		NeoDatisSingleClassDataBase<String, CmeContract> dataBase = populateDatabase(calData, databaseOutputPath);
		List<CmeContract> contracts = dataBase.getAll();
		for(CmeContract c: contracts){
			Utils.prtObMess(CmeCalendarData.class, c.toString());
		}
		dataBase.close();
		System.exit(1);
	}

	protected static CmeCalendarData getFromXstream(String xmlDataPath){
		XStream xs = new XStream();
		
		InputStream is;
		try {
			is = new FileInputStream(new File(xmlDataPath));
		} catch (FileNotFoundException e) {
			throw Utils.IllState(CmeCalendarData.class, e.getMessage());
		}
		//read the file into the CmeCalendarData class
		CmeCalendarData calData = (CmeCalendarData)xs.fromXML(is);
		return calData;

	}

	protected static int recordCount(CmeCalendarData calData){
		int ret=0;
		for(CmeProductTypes cpts: calData.productTypes){
			if(cpts.products==null)continue;
			for(CmeProduct prod:cpts.products){
				if(prod.contracts==null)continue;
				ret = ret+prod.contracts.size();
			}
		}
		
		return ret;
	}
	
	protected static NeoDatisSingleClassDataBase<String, CmeContract> populateDatabase(CmeCalendarData calData, String databaseOutputPath){
		int recCount = recordCount(calData);
		int currCount = 0;
		NeoDatisSingleClassDataBase<String, CmeContract> dataBase = 
			new NeoDatisSingleClassDataBase<String, CmeContract>(databaseOutputPath,CmeContract.class);
		for(CmeProductTypes cpts: calData.productTypes){
			if(cpts.products==null)continue;
			for(CmeProduct prod:cpts.products){
				if(prod.contracts==null)continue;
				for(CmeContract c: prod.contracts){
					if(c==null)continue;
					// see if there is a search criteria, and if so allow only those keys
					if(regexSeachCriteriaPattern!=null && 
							RegexMethods.getRegexMatches(regexSeachCriteriaPattern, c.Exp_Contract_Code).size()<=0){
						continue;
					}
					// see if it's already in database
					CmeContract dupe = dataBase.get("Exp_Contract_Code", c.Exp_Contract_Code);
					if(dupe!=null){
						// see if the dupe is a Fut/Opt and the new Contract is an Opt/Fut
						//   If this is a case, add an options suffix to the product
						//     and continue processing.
						String prodTypeCodeOfDupe = dupe.getPRODUCT_TYPE_CODE();
						String prodTypeCodeOfThisContract = prod.PRODUCT_TYPE_CODE;
						if(prodTypeCodeOfDupe.compareTo(prodTypeCodeOfThisContract)==0){
							// if you get here, then just loop to next contract b/c the
							//  contract is already in the database
							currCount++;
							Utils.prtObMess(CmeCalendarData.class,"record: "+currCount+ " of "+recCount+  " ALREADY IN DATABASE: "+dupe.toString());
							continue;
						}
					}
					
					// processs new Contract
					
					// new contract to clone and put in database
					CmeContract newc = new CmeContract(calData,cpts,prod,c);
					
					dataBase.put(newc);
					currCount++;
					Utils.prtObMess(CmeCalendarData.class,"record: "+currCount+ " of "+recCount+   " ADDED TO DATABASE: "+newc.toString());
				}
			}
		}
		dataBase.close();
		dataBase.open();
		return dataBase;
		
	}

}
