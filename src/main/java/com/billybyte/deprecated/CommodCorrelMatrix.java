package com.billybyte.deprecated;

import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecDefQueryAllMarkets;
import com.billybyte.marketdata.futures.FuturesProduct;
import com.billybyte.marketdata.futures.FuturesProductQuery;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;


/**
 * 
 * 
NG	0.184	0.181	0.178	0.108	0.102	0.098	0.095	0.093	0.090	0.085	0.015	0.005	0.003	0.003	0.070	0.006	0.004	0.003	0.002	0.003	0.005	0.070	0.010	0.005
CL	0.012	0.011	0.010	0.009	0.008	0.007	0.006	0.005	0.004	0.003	0.002	0.001	0.001	0.001	0.001	0.001	0.001	0.001	0.001	0.001	0.001	0.001	0.001	0.001
HO	0.179	0.177	0.174	0.104	0.094	0.091	0.089	0.087	0.085	0.080	0.020	0.010	0.002	0.003	0.070	0.010	0.003	0.002	0.002	0.002	0.005	0.060	0.010	0.010
PJM	0.190	0.188	0.186	0.116	0.112	0.110	0.107	0.105	0.075	0.072	0.012	0.002	0.002	0.002	0.070	0.004	0.002	0.003	0.002	0.030	0.003	0.060	0.010	0.002
PJM00.190	0.188	0.186	0.116	0.112	0.110	0.107	0.105	0.075	0.072	0.012	0.002	0.002	0.002	0.070	0.004	0.002	0.003	0.002	0.030	0.003	0.060	0.010	0.002
CIN	0.190	0.188	0.186	0.116	0.112	0.110	0.107	0.105	0.075	0.072	0.012	0.002	0.002	0.002	0.070	0.004	0.002	0.003	0.002	0.030	0.003	0.060	0.010	0.002
CINO0.190	0.188	0.186	0.116	0.112	0.110	0.107	0.105	0.075	0.072	0.012	0.002	0.002	0.002	0.070	0.004	0.002	0.003	0.002	0.030	0.003	0.060	0.010	0.002
NI	0.190	0.188	0.186	0.116	0.112	0.110	0.107	0.105	0.075	0.072	0.012	0.002	0.002	0.002	0.070	0.004	0.002	0.003	0.002	0.030	0.003	0.060	0.010	0.002
NIO	0.195	0.193	0.190	0.120	0.114	0.110	0.107	0.105	0.075	0.072	0.012	0.002	0.002	0.003	0.070	0.006	0.004	0.003	0.002	0.030	0.003	0.060	0.010	0.002
ZC	0.179	0.177	0.174	0.104	0.094	0.091	0.089	0.087	0.085	0.080	0.020	0.010	0.002	0.003	0.070	0.010	0.003	0.002	0.002	0.002	0.005	0.060	0.010	0.010
ZW	0.179	0.177	0.174	0.104	0.094	0.091	0.089	0.087	0.085	0.080	0.020	0.010	0.002	0.003	0.070	0.010	0.003	0.002	0.002	0.002	0.005	0.060	0.010	0.010
ZS	0.179	0.177	0.174	0.104	0.094	0.091	0.089	0.087	0.085	0.080	0.020	0.010	0.002	0.003	0.070	0.010	0.003	0.002	0.002	0.002	0.005	0.060	0.010	0.010
KCW	0.179	0.177	0.174	0.104	0.094	0.091	0.089	0.087	0.085	0.080	0.020	0.010	0.002	0.003	0.070	0.010	0.003	0.002	0.002	0.002	0.005	0.060	0.010	0.010
													0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000
													0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000
													0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000	0.000

                    ' HERE IS THE MAIN CALCULATION
                    '  case 1 : year_j and year_k are the same: cummCorr_j = 1- cumm_corr_j - (1 - prodCorr)
                    '     example: CLM1 vs CLN1
                    '  CL  CL  CLCL   1.0
'       1       2      3        4      5       6       7       8       9      10      11      12
'CL  0.012   0.011   0.010   0.009   0.008   0.007   0.006   0.005   0.004   0.003   0.002   0.001
                    ' correlate CLM1 to CLN1
                    ' year_j = 2011, year_k = 2011
                    ' month_j = 6, month_k = 7
                    ' x = .007
                    ' y = .006
                    ' cummCorr_j = 1 - (x-y) + highestCorrelation_j*(year_k year_j) + - (1-1) = 1 - (.007 - .006) - 0  = 1 - .001  = .999
                    '  the term highestCorrelation_j*(year_k year_j) will add a full year's correlation to the cummCorr_j value, if there months span a year
                    '  thus the correlation coefficient between CLM1 and CLN1 will be .999  (very highly correlated)

                    
                    ' calculate correlation via col product when the products are DIFFERENT
                    '  case 1 : year_j and year_k are the same: cummCorr_j = 1- cumm_corr_j - (1 - prodCorr)
                    '     example: CL vs HO
                    '  CL  HO  CLHO    0.9
'       1       2      3        4      5       6       7       8       9      10      11      12
'CL  0.012   0.011   0.010   0.009   0.008   0.007   0.006   0.005   0.004   0.003   0.002   0.001
'HO  0.179   0.177   0.174   0.104   0.094   0.091   0.089   0.087   0.085   0.080   0.020   0.010
                    ' correlate CLH1 to HOK1
                    ' year_j = 2011, year_k = 2011
                    '  you must do the calculation for x and y for row j and x and y for row k
                    ' for row j month_j = 3, month_k = 5
                    ' x = .010
                    ' y = .008
                    ' cummCorr_j = 1 - (x-y) + 0 + - (1-.9) = 1 - (.01 - .008) - .1  = 1 - -.002 - .1 = .898
                    ' x = .174
                    ' y = .094
                    ' cummCorr_k = 1 - (x-y) + 0 + - (1-.9) = 1 - (.174-.094) - .1  = 1 - -.080 - .1 = .820
                    

 * 
 * 
 * 
 * @author Bill Perlman
 *
 */
public class CommodCorrelMatrix {
/**
 */
	private final QueryInterface<String, SecDef> sdQuery = new SecDefQueryAllMarkets();
	@SuppressWarnings("serial")
	private class CMCDiffMap extends TreeMap<String,CummMonthCorrelationDiffs>{
	}
	QueryInterface<String, FuturesProduct> fpQuery = new FuturesProductQuery();

	@SuppressWarnings("serial")
	private class ProdCorrMap extends TreeMap<String,Double>{
	}

	@XStreamAsAttribute
	private String name;
	private boolean wired;
	private String cMCDiffMapCsvName;
	private String prodCorrMapCsvName;
	private long yyyyMmDdBegBusinessDayOfContractsToGet;
	private long yyyyMmDdEndBusinessDayOfContractsToGet;
	
	private transient CMCDiffMap cMCDiffMap;
	private transient ProdCorrMap prodCorrMap;
	private transient Map<String, Double> correlationPairs;
	
	private final String DIFF_MAP_CSV_FILENAME = "perMonthCummCorr.csv";
	private final String PROD_CORRMAP_CSV_FILENAME = "commProdCorrelations.csv";
	// new 07/25/2012
//	private transient final Object duplicateProducts_Lock = new Object();
//	private transient Map<String,String> duplicateProducts;
	// end new 07/25/2012

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

	public boolean isWired() {
		return wired;
	}

	public void setWired(boolean wired) {
		if (this.wired)
			return;
		wire();
		this.wired = wired;
	}

	
	public CommodCorrelMatrix(){
		super();
	}
	
	public CommodCorrelMatrix(String diffMapCsvName,
		String prodCorrMapCsvName, String sDataXmlName) {
		super();
		if(diffMapCsvName==null || diffMapCsvName.compareTo(" ")<=0){
			throw Utils.IllArg(this.getClass(), " null or invalid diffMapCsvName");
		}
		if(prodCorrMapCsvName==null|| prodCorrMapCsvName.compareTo(" ")<=0){
			throw Utils.IllArg(this.getClass(), " null or invalid prodCorrMapCsvName");
		}
		if(sDataXmlName==null || sDataXmlName.compareTo(" ")<=0){
			throw Utils.IllArg(this.getClass(), " null or invalid sDataXmlName");
		}
		cMCDiffMapCsvName = diffMapCsvName;
		this.prodCorrMapCsvName = prodCorrMapCsvName;
		wire();
	}

	
	
	public CommodCorrelMatrix(String diffMapCsvName,
			String prodCorrMapCsvName,List<SecDef> secDefList){
		super();
		if(diffMapCsvName==null || diffMapCsvName.compareTo(" ")<=0){
			throw Utils.IllArg(this.getClass(), " null or invalid diffMapCsvName");
		}
		if(prodCorrMapCsvName==null|| prodCorrMapCsvName.compareTo(" ")<=0){
			throw Utils.IllArg(this.getClass(), " null or invalid prodCorrMapCsvName");
		}
//		if(secDefList==null || secDefList.size()<=0){
//			throw Utils.IllArg(this.getClass(), " null or secDefList prodCorrMapCsvName");
//		}
		if(secDefList==null ){
			throw Utils.IllArg(this.getClass(), " null or secDefList prodCorrMapCsvName");
		}
		cMCDiffMapCsvName = diffMapCsvName;
		this.prodCorrMapCsvName = prodCorrMapCsvName;
		wire();
	}
	
	public CommodCorrelMatrix(List<SecDef> secDefList){
		super();
		if(secDefList==null ){
			throw Utils.IllArg(this.getClass(), " null or secDefList prodCorrMapCsvName");
		}
		cMCDiffMapCsvName = null;
		this.prodCorrMapCsvName = null;
		wire();
	}
	
	private void wire(){
		
		
		this.cMCDiffMap = getCMCData(cMCDiffMapCsvName);
		this.prodCorrMap = getProdCorrData(prodCorrMapCsvName);
	}
	
	
	public static CommodCorrelMatrix CommoditesCorrelationMatrixFromSDataTemplate(String diffMapCsvName,
			String prodCorrMapCsvName, String sDataXmlTemplageName){
		CommodCorrelMatrix ret = new CommodCorrelMatrix();
		ret.SetCMCDiffMapCsvName(diffMapCsvName);
		
		return null;
	}
	
	public void SetCMCDiffMapCsvName(String cMCDiffMapCsvName){
		this.cMCDiffMapCsvName = cMCDiffMapCsvName;
	}

	public Map<String, Double> getCorrelationPairs(){
		return this.correlationPairs;
	}

	

	private class CummMonthCorrelationDiffs{
		String prodName;
		Double[] correlaltionDiffs = new Double[12];
	}

	
	
	
	public CMCDiffMap getCMCData(String csvFileName){
//		ArrayList<String[]> data =  Utils.getCSVData(csvFileName);
		ArrayList<String[]> data = null;
		if(csvFileName==null || csvFileName.compareTo("   ")<0){
			data = Utils.getCSVData(CommodCorrelMatrix.class,DIFF_MAP_CSV_FILENAME);
		}else{
			data = Utils.getCSVData(csvFileName);
		}
		CMCDiffMap cummArray = new CMCDiffMap();
		for(String[] stringArray:data){
			try {
				CummMonthCorrelationDiffs cummDiffs = new CummMonthCorrelationDiffs();
				cummDiffs.prodName = stringArray[0];
				if(cummDiffs.prodName.compareTo(" ")>0){
					for(int i=1;i<=12;i++){
						cummDiffs.correlaltionDiffs[i-1] = new Double(stringArray[i]);
					}	
					cummArray.put(cummDiffs.prodName,cummDiffs);
				}
			} catch (NumberFormatException e) {
			}
		}
		return cummArray;
	}
	
	public ProdCorrMap getProdCorrData(String csvFileName){
//		ArrayList<String[]> data =  Utils.getCSVData(csvFileName);
		ArrayList<String[]> data = null;
		if(csvFileName==null || csvFileName.compareTo("   ")<0){
			data = Utils.getCSVData(CommodCorrelMatrix.class,PROD_CORRMAP_CSV_FILENAME);
		}else{
			data = Utils.getCSVData(csvFileName);
		}
		ProdCorrMap prodCorrData = new ProdCorrMap();
		for(String[] stringArray:data){
			if(stringArray[0].compareTo(" ")>0){
				try {
					prodCorrData.put(stringArray[0], new Double(stringArray[1]));
				} catch (NumberFormatException e) {
					String mess = "";
					for(String item:stringArray){
						mess = mess+(mess.compareTo("")>0?",":"")+item;
					}
					Utils.prtObErrMess(CommodCorrelMatrix.class, e.getMessage()+" on line: "+mess);
				}
			}
		}
		return prodCorrData;
	}

	/**
	 * 
	 * @param secDataMon1 - 
	 * @param secDataMon2
	 * @param diffMap - map of correlation differences
	 * @param prodCorrMap - map of correlations between products
	 * @return - correlation
	 */
	public double getCorrelationCoefficient(SecDef secDataMon1,SecDef secDataMon2, CMCDiffMap diffMap,ProdCorrMap prodCorrMap){
		
		int month1 = secDataMon1.getContractMonth();
		int year1 = secDataMon1.getContractYear();
		String prod1 = secDataMon1.getSymbol();	
		int month2 = secDataMon2.getContractMonth();
		int year2 = secDataMon2.getContractYear();
		String prod2 = secDataMon2.getSymbol();
		return getCorrelationCoefficientFromMonthAndYearInts(prod1,month1,year1,prod2,month2,year2,diffMap,prodCorrMap);

	}

	// 
	public double getCorrelationCoefficientFromShortNames(String pairString){
		String[] shortNames = pairString.split("__");
		if(shortNames.length<2)return Double.NaN;
		return getCorrelationCoefficientFromShortNames(shortNames[0],shortNames[1]);
	}
	
	public double getCorrelationCoefficientFromShortNames(String shortName1,String shortName2){
		SecDef secDataMon1 = sdQuery.get(shortName1, 1, TimeUnit.SECONDS);
		SecDef secDataMon2 = sdQuery.get(shortName2, 1, TimeUnit.SECONDS);
		int month1 = secDataMon1.getContractMonth();
		int year1 = secDataMon1.getContractYear();
		String prod1 = secDataMon1.getSymbol();	
		int month2 = secDataMon2.getContractMonth();
		int year2 = secDataMon2.getContractYear();
		String prod2 = secDataMon2.getSymbol();
		return getCorrelationCoefficientFromMonthAndYearInts(prod1,month1,year1,prod2,month2,year2,cMCDiffMap,prodCorrMap);
	}
	
	public double getCorrelationCoefficientFromMonthAndYearInts(String prod1,int month1,int year1, String prod2,
			int month2, int year2, CMCDiffMap diffMap,ProdCorrMap prodCorrMap){

		CummMonthCorrelationDiffs diffData1 = diffMap.get(prod1);
		if(diffData1==null){
			Utils.prtObErrMess(this.getClass(), " getCorrelationCoefficient error: no entry int he CMCDiffMap for product: "+prod1);
			throw Utils.IllState(this.getClass(), " getCorrelationCoefficient error: no entry int he CMCDiffMap for product: "+prod1);
		}

		CummMonthCorrelationDiffs diffData2 = diffMap.get(prod2);
		if(diffData2==null){
			Utils.prtObErrMess(this.getClass(), " getCorrelationCoefficient error: no entry int he CMCDiffMap for product: "+prod2);
			throw Utils.IllState(this.getClass(), " getCorrelationCoefficient error: no entry int he CMCDiffMap for product: "+prod2);
		}

		double prodCore;

		if(prod1.compareTo(prod2)==0){
			prodCore=1.0;
		}else{
			if(prodCorrMap.containsKey(prod1+prod2)){
				prodCore = prodCorrMap.get(prod1+prod2);
			}else if(prodCorrMap.containsKey(prod2+prod1)){
				prodCore = prodCorrMap.get(prod2+prod1);
			}else{
				return 0;
			}
		}
		
	
		// first calculate the correlation coefficient using the path via month1
		double month1CummCorr = diffData1.correlaltionDiffs[month1-1];
		double month2CummCorr = diffData1.correlaltionDiffs[month2-1];
		double highestCummCorr = diffData1.correlaltionDiffs[0];
		double cummCorrDiff = month1CummCorr - month2CummCorr;
		double coefficientViaProd1;
		// this if statement insures that the calculation of coefficientViaProd1
		//   will make sure that the lower is substracted from the higher year
		if(year1*100.0+month1 <= year2 * 100.0 + month2){
			coefficientViaProd1= 1 - (cummCorrDiff + Math.abs(year1-year2) * highestCummCorr) - (1-prodCore) ;
		}else{
			coefficientViaProd1= 1 - (-cummCorrDiff + Math.abs(year1-year2) * highestCummCorr)  - (1-prodCore);
		}
		
		month1CummCorr = diffData2.correlaltionDiffs[month1-1];
		month2CummCorr = diffData2.correlaltionDiffs[month2-1];
		highestCummCorr = diffData2.correlaltionDiffs[0];
		cummCorrDiff = month1CummCorr - month2CummCorr;
		double coefficientViaProd2;
		if(year1*100.0+month1 <= year2 * 100.0 + month2){
			coefficientViaProd2 = 1 - (cummCorrDiff + Math.abs(year1-year2) * highestCummCorr) - (1 - prodCore) ;
		}else{
			coefficientViaProd2 =  1 - (-cummCorrDiff + Math.abs(year1-year2) * highestCummCorr) - (1 - prodCore) ;
		}

		// fix to make sure that, in the case when the intra-product correlation is positive,
		//   the resulting correlation is NOT less than 0 - and visa versa.
		double ret = Math.max(coefficientViaProd1, coefficientViaProd2);
		if(prodCore>=0.0){
			if(ret<0.0){
				ret=0.0;
			}
		}else{
			if(ret>0.0){
				ret=0.0;
			}
		}
		return ret;
		// end fix - 05/12/2012

		// line below is commented out b/c it is replaced by fix above
//		return Math.max(coefficientViaProd1, coefficientViaProd2);
	}

	
	
	public Map<String, Double> getCorrelationCoefficientMatrixPairs(Map<String,SecDef> underlyings, CMCDiffMap diffMap,ProdCorrMap prodCorrMap){
		TreeMap<String, Double> ret = new TreeMap<String, Double>();
		SecDef[] underlyingArray = underlyings.values().toArray(new SecDef[0]);
	
		for(int i=0;i< underlyingArray.length;i++){
			for(int j = 0;j<underlyingArray.length;j++){
				SecDef s1 = underlyingArray[i];
				SecDef s2 = underlyingArray[j];
				Double value=null;
				try {
					value = getCorrelationCoefficient(s1, s2, diffMap, prodCorrMap);
				} catch (Exception e) {
					Utils.prtObErrMess(this.getClass(), " getCorrelationCoefficientMatrixPairs: error getting coefficient pair for : "+ s1.getShortName() + " and "+ s2.getShortName());
					Utils.prtObErrMess(this.getClass()," cause = "+e.getMessage()) ;
					continue;
				}
				ret.put(s1.getShortName()+"__"+s2.getShortName(),value);
			}
		}
		
		
		return ret;
		
	}
}
