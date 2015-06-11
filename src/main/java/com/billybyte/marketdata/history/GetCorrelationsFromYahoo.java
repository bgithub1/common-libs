package com.billybyte.marketdata.history;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;


import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.HistData;
import com.billybyte.commonstaticmethods.HistDataSources;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.queries.ComplexQueryResult;

/**
 * 
 * @author bperlman1
 *
 */
public class GetCorrelationsFromYahoo {
	/**
	 * get a map of common history, along with the set of history dates in common to 
	 *   all shortNames in snSet
	 * @param snSet
	 * @param begDate - java.util.Calendar
	 * @param endDate - java.util.Calendar
	 * @param useAdjClose - boolean
	 * @return Tuple<Set<Long>,Map<String, TreeMap<Long, HistData>>>  
	 *    The return Tuple has both a set of dates, and the actual data for each security
	 */
	public static Tuple<Set<Long>,Map<String, TreeMap<Long, HistData>>> getCommonHistory(
			Set<String> snSet,
			Calendar begDate,
			Calendar endDate,
			boolean useAdjClose){
		
		long todayLong = endDate.getTimeInMillis(); // end date for hist
		long beforeLong = begDate.getTimeInMillis(); // beg
		Map<String, TreeMap<Long, HistData>> shortNameToHistDataMap = 
				new HashMap<String, TreeMap<Long,HistData>>(); // shortname to treemap of hist
		Set<Long> commonDaysInHist=null;
		for(String sn:snSet){ // build map of treemaps
			TreeMap<Long, HistData> tmHist = new TreeMap<Long, HistData>();
			
			List<HistData> l = 
					HistDataSources.getYahooDailyHistData(sn, beforeLong, todayLong);
			for(HistData hd:l){
				tmHist.put(hd.getDateInMills(), hd);
			}
			if(commonDaysInHist==null){
				commonDaysInHist = new HashSet<Long>(tmHist.keySet());
			}
			commonDaysInHist.retainAll(tmHist.keySet()); // build common days
			shortNameToHistDataMap.put(sn, tmHist);
		}

		Map<String, TreeMap<Long, HistData>> shortNameToHistDataMapInCommon = 
				new HashMap<String, TreeMap<Long,HistData>>(); // only days in common

		// create new treemaps
		for(Entry<String, TreeMap<Long, HistData>> entry : shortNameToHistDataMap.entrySet()){
			TreeMap<Long, HistData> data = entry.getValue();
			TreeMap<Long, HistData> newDataInCommon = new TreeMap<Long, HistData>();
			for(Long date:commonDaysInHist){
				newDataInCommon.put(date,data.get(date));
			}
			shortNameToHistDataMapInCommon.put(entry.getKey(),newDataInCommon);
		}
		
		return new Tuple<Set<Long>, Map<String,TreeMap<Long,HistData>>>(commonDaysInHist, shortNameToHistDataMapInCommon);
		
	}
	
	/**
	 * Get matrix of returns data from price data
	 * @param commonDaysInHist - Set<Long> set of days where every
	 *    security in snSet has history data for these days
	 * @param shortNameToHistDataMapInCommon - Map<String,TreeMap<Long,HistData>> 
	 *      Map of shortNames and TreeMaps of actual HistData.
	 *      
	 * @return
	 */
	public static double[][] getReturnsMatrix(
			Set<Long> commonDaysInHist,
			Map<String,TreeMap<Long, HistData>> shortNameToHistDataMapInCommon){

		List<String> orderedNames = 
				new ArrayList<String>(
						new TreeSet<String>(
								shortNameToHistDataMapInCommon.keySet()));
		double[][] returns = new double[commonDaysInHist.size()-1][orderedNames.size()];
		for(int nameSubScript =0;nameSubScript<orderedNames.size();nameSubScript++){
			String name0 = orderedNames.get(nameSubScript);
			List<HistData> lHist0 = 
					new ArrayList<HistData>(shortNameToHistDataMapInCommon.get(name0).values());
			if(lHist0.size()!=commonDaysInHist.size() ){
				throw Utils.IllArg(GetCorrelationsFromYahoo.class, "not enough data for " + name0);
			}

			for(int daySubscript = 0;daySubscript<commonDaysInHist.size()-1;daySubscript++){  // make a block matrix of returns
				double perc = lHist0.get(daySubscript+1).getAdjClose() / lHist0.get(daySubscript).getAdjClose() - 1;
				returns[daySubscript][nameSubScript] = perc;
			}
			
		}
		return returns;
	}
	
	public static double[][] getReturnsMatrix(
			Set<String> snSet,
			Calendar begDate,
			Calendar endDate,
			boolean useAdjClose){
		
		Tuple<Set<Long>,Map<String, TreeMap<Long, HistData>>> tuple = 
				getCommonHistory(snSet, begDate, endDate, true);
		
		Set<Long> commonDaysInHist = tuple.getT1_instance();
		
		Map<String, TreeMap<Long, HistData>> shortNameToHistDataMapInCommon =
				tuple.getT2_instance();
		
		double[][] returnsTranspose = getReturnsMatrix(commonDaysInHist, shortNameToHistDataMapInCommon);
		return returnsTranspose;
	}
	
	public static Map<String, ComplexQueryResult<BigDecimal>> createCorrMap(
			List<String> orderedNames,
			double[][] corrOrCovMatrix){
		Map<String, ComplexQueryResult<BigDecimal>> ret = 
				new HashMap<String, ComplexQueryResult<BigDecimal>>();
		for(int i = 0;i<orderedNames.size();i++){
			for(int j=i;j<orderedNames.size();j++){
				String corrName = 
						orderedNames.get(i)+
						MarketDataComLib.DEFAULT_CORRELATION_PAIR_SEPARATOR+
						orderedNames.get(j);
				double corr = corrOrCovMatrix[i][j];
				BigDecimal corrBd = new BigDecimal(corr);
				ComplexQueryResult<BigDecimal> cqr = 
						new ComplexQueryResult<BigDecimal>(null, corrBd);
				ret.put(corrName, cqr);
			}
		}
		return ret;

	}

	
	public static Map<String, ComplexQueryResult<BigDecimal>> getCorrelationMap(
		Set<String> snSet,
		Calendar begDate,
		Calendar endDate,
		boolean useAdjClose){
		
		double[][] returnsTranspose = getReturnsMatrix(snSet, begDate, endDate, useAdjClose);
		List<String> orderedNames = new ArrayList<String>( new TreeSet<String>(snSet));
		PearsonsCorrelation pc = new PearsonsCorrelation(returnsTranspose);
		double[][] pearsonsMatrix = pc.getCorrelationMatrix().getData();
		return createCorrMap(orderedNames, pearsonsMatrix);
	}
	
	public static Map<String, ComplexQueryResult<BigDecimal>> getCovarianceMap(
		Set<String> snSet,
		Calendar begDate,
		Calendar endDate,
		boolean useAdjClose){
		
		double[][] returnsTranspose = getReturnsMatrix(snSet, begDate, endDate, useAdjClose);
		List<String> orderedNames = new ArrayList<String>( new TreeSet<String>(snSet));
		RealMatrix covMatrixFromCov = new Covariance(returnsTranspose).getCovarianceMatrix();

		double[][] covMatrix = covMatrixFromCov.getData();
		return createCorrMap(orderedNames, covMatrix);
	}
	
	
	public static Tuple<List<String>, double[][]> getReturnsMatrixTuple(
			Set<String> snSet,
			Calendar begDate,
			Calendar endDate,
			boolean useAdjClose){

		List<String> orderedNames = 
				new ArrayList<String>(
						new TreeSet<String>(
								snSet));
		
		
		double[][] returnsTranspose = getReturnsMatrix(snSet, begDate,endDate,useAdjClose);
		return new Tuple<List<String>, double[][]>(orderedNames, returnsTranspose);
	}

	
	public static void main(String[] args) {
		Set<String> snSet = 
				CollectionsStaticMethods.setFromArray(new String[]{
						"IBM","GE","AAPL"
				});
		Calendar endDate = Calendar.getInstance();
		Calendar begDate = Dates.addToCalendar(endDate, -200, Calendar.DAY_OF_YEAR, true);
		
		
		
		Tuple<Set<Long>,Map<String, TreeMap<Long, HistData>>> tuple = 
				getCommonHistory(snSet, begDate, endDate, true);
		
		Set<Long> commonDaysInHist = tuple.getT1_instance();
		
		Map<String, TreeMap<Long, HistData>> shortNameToHistDataMapInCommon =
				tuple.getT2_instance();
		
		List<String> orderedNames = 
				new ArrayList<String>(
						new TreeSet<String>(
								snSet));
		
		
		double[][] returnsTranspose = getReturnsMatrix(commonDaysInHist, shortNameToHistDataMapInCommon);
		String header = "";
		for(String on :orderedNames){
			header += on+",";
		}
		header = header.substring(0, header.length()-1);
		Utils.prt(header);
		prtIt(returnsTranspose);
		
		PearsonsCorrelation pc = new PearsonsCorrelation(returnsTranspose);
		double[][] pearsonsMatrix = pc.getCorrelationMatrix().getData();
		Utils.prt(header);
		prtIt(pearsonsMatrix);
		// covariance
		RealMatrix covMatrixFromCov = new Covariance(returnsTranspose).getCovarianceMatrix();
		Utils.prt(header);
		prtIt(covMatrixFromCov.getData());
		
	}
	
	

	private static void prtIt(double[][] values){
		for(int i=0;i<values.length;i++){
			String row = "";
			for(int j = 0;j<values[i].length;j++){
				row += values[i][j] + ",";
			}
			Utils.prt(row.substring(0,row.length()-1));
		}
		
	}
}
