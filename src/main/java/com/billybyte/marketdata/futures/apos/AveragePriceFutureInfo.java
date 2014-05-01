package com.billybyte.marketdata.futures.apos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;


import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.MarketDataComLib;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SecEnums.SecLocale;
import com.billybyte.marketdata.futures.FuturesProductQuery;
import com.billybyte.queries.ComplexQueryResult;
/**
 * Info regarding an Averaged Priced Future
 * @author bperlman1
 *
 */
public class AveragePriceFutureInfo {
	private final BigDecimal actualSettle; // settle of ap future
	private final BigDecimal prevAveragedPart; // value of part that has already been settled 
	private final BigDecimal prevAveragedPercent; // 
	private final BigDecimal unSettledPart;
	private final BigDecimal unSettledPerc;
	
	private final Calendar expiryOfActual;
	private final Calendar dateOfAveragingBegin;
	private final Long yyyyMmDdOfActualSettle ;// date that coincide with actualSettle
	private final SecDef[] underlyingSecDefs;
	private final Double[] underlyingUnSettledPercents;
	
	public AveragePriceFutureInfo(
			BigDecimal actualSettle,
			Calendar dateOfSettle,
			Calendar dateOfAveragingBegin,
			Long yyyyMmDdOfActualSettle,
			BigDecimal prevAveragedPart,
			BigDecimal prevAveragedPercent, BigDecimal unSettledPart,
			SecDef[] underlyingSecDefs,Double[] underlyingPercents) {
		super();
		this.actualSettle = actualSettle;
		this.dateOfAveragingBegin = dateOfAveragingBegin;
		this.prevAveragedPart = prevAveragedPart;
		this.prevAveragedPercent = prevAveragedPercent;
		this.unSettledPerc = BigDecimal.ONE.subtract(prevAveragedPercent);
		this.unSettledPart = unSettledPart;
		this.expiryOfActual = dateOfSettle;
		this.underlyingSecDefs = underlyingSecDefs;
		this.underlyingUnSettledPercents = underlyingPercents;
		this.yyyyMmDdOfActualSettle = yyyyMmDdOfActualSettle;
	}

	/**
	 * 
	 * @return actual settlement
	 */
	public BigDecimal getActualSettle() {
		return actualSettle;
	}

	/**
	 * If the evaluation date >= firstBusinessDayOfSwap then you are in the averaging period:
	 * Let T = total number of business days in the swap
	 * Let N = number of business days that have already been settled during averaging period
	 * If p0 = settlement of first business day;
	 * 	  p1 = settlement of second business day;
	 *    pN = settlement of Nth business day;
	 *    
	 * Then
	 *    prevAveragedPart = ((p0+p1...+pN) / T or
	 *    0.0 if you are not yet in the averaging period.
	 *    
	 *    
	 * @return BigDecimal prevAveragedPart
	 */
	public BigDecimal getPrevAveragedPart() {
		return prevAveragedPart;
	}

	/**
	 * If T = Total number of business days in swap month, and
	 *    N = number of business days that already have been settled
	 *    prevAveragedPercent = N / T
	 * @return prevAveragedPercent
	 */
	public BigDecimal getPrevAveragedPercent() {
		return prevAveragedPercent;
	}

	/**
	 * If there are n underlyings to swap:
	 *   let perc0 = percent of business days for first underling that has NOT YET COMPLETELY yet settled;
	 *   let perc1 = percent of business days for second underling that has NOT YET COMPLETELY yet settled;
	 *   let percN = percent of business days for Nth underling that has NOT YET COMPLETELY yet settled;
	 *   
	 *   let price0 = price of first underling that has NOT YET COMPLETELY settled;
	 *   let price1 = price of second underling that has NOT YET COMPLETELY settled;
	 *   let priceN = price of Nth underling that has NOT YET COMPLETELY settled;
	 *   
	 *   unSettledPart = perc0*price0 + perc1*price1 ... + percN*priceN 
	 *   
	 *   where perc0 + perc1 ... + percN = 1
	 *   
	 * @return
	 */
	public BigDecimal getUnSettledPart() {
		return unSettledPart;
	}

	/**
	 * If N = number of business days in swap month, and
	 *    n' = number of business days that already have been settled
	 *    prevAveragedPercent = (N-n') / N
	 * @return prevAveragedPercent
	 */
	public BigDecimal getUnSettledPerc() {
		return unSettledPerc;
	}

	public Calendar getExpiryOfActual() {
		return expiryOfActual;
	}

	public SecDef[] getUnderlyingSecDefs() {
		return underlyingSecDefs;
	}

	/**
	 * Sum(getUnderlyingUnSettledPercents() = unSettledPerc;
	 * @return
	 */
	public Double[] getUnderlyingUnSettledPercents() {
		return underlyingUnSettledPercents;
	}

	
	public Calendar getDateOfAveragingBegin() {
		return dateOfAveragingBegin;
	}
	
	

	public Long getYyyyMmDdOfActualSettle() {
		return yyyyMmDdOfActualSettle;
	}

	public Calendar getDateOfActualSettle(){
		return Dates.getCalendarFromYYYYMMDD(getYyyyMmDdOfActualSettle());
	}
	
	public Double getTimeToSwapExpiryAsPOY(){
		Calendar evalDate = getDateOfActualSettle();
		long days = Dates.getDifference(evalDate, getExpiryOfActual(), TimeUnit.DAYS);
		if(days<0)return 0.0;
		return 1.0*days/365;
	}
	
	public Double createTimeToExpiry(){
		Calendar evalDate = getDateOfActualSettle();
		Calendar actualExpiry= getExpiryOfActual();
		long days = Dates.getDifference(evalDate, actualExpiry, TimeUnit.DAYS)+1;
		return 1.0*days/365;
	}
	
	public Double getLenOfTimeOfAverPeriodAsPOY(){
		Calendar begAvPeriod = getDateOfAveragingBegin();
		Calendar expiry = getExpiryOfActual();
		long days = Dates.getDifference(begAvPeriod, expiry, TimeUnit.DAYS)+1;
		return 1.0*days/365;
	}

	
	public Double getTimeToAveragingBeginAsPOY(){
		Calendar evalDate = getDateOfActualSettle();
		long days = Dates.getDifference(evalDate, getDateOfAveragingBegin(), TimeUnit.DAYS)+1;
		return 1.0*days/365;
	}

	@Override
	public String toString() {
		return actualSettle + ", " + prevAveragedPart + ", "
				+ prevAveragedPercent + ", " + unSettledPart + ", "
				+ unSettledPerc + ", " + Dates.getYyyyMmDdFromCalendar(expiryOfActual) + ", "
				+ Arrays.toString(underlyingSecDefs) + ", "
				+ Arrays.toString(underlyingUnSettledPercents);
	}
	
	
	/**
	 * 
	 * @param actualSd - the SecDef of the Future, not the option!!
	 * @param apoUnderlyingSdQuery - instance of NymexAveragePriceSecDefQuery
	 * @param settleQuery - QueryInterface<String, SettlementDataInterface>
	 * @param evalDate
	 * @param apShortName
	 * @return
	 */
	public static ComplexQueryResult<AveragePriceFutureInfo> getAveragePriceFutureInfo(
			SecDef actualSd,
			SettlementDataInterface actualSettle,
			SecDef[] underlyingSecDefs,
			SettlementDataInterface[] underlyingSettles//,
			) {
		Calendar evalDate = Dates.getCalendarFromYYYYMMDD(actualSettle.getTime());
		//****** get settlement *****
		String apShortName = actualSd.getShortName();	
		BigDecimal actSetPr = actualSettle.getPrice();
		//****** END get settlement *****5

		// ***************** get total business days of swap  ***************************
		String locale = SecLocale.getLocaleFromSecDef(actualSd).toString();
		Calendar dateOfAveragingBegin = Calendar.getInstance();
		dateOfAveragingBegin.set(actualSd.getContractYear(), actualSd.getContractMonth()-1,1);
		if(!Dates.isBusinessDay(locale, dateOfAveragingBegin)){
			dateOfAveragingBegin = Dates.addBusinessDays(locale, dateOfAveragingBegin, 1);
		}
		Calendar expiryOfAcutal = MarketDataComLib.getCalendarOfExpiryFromSecDef(actualSd);
		int totalBusDaysOfSwap =Dates.getAllBusinessDays(locale.toString(),dateOfAveragingBegin, expiryOfAcutal).size();
		// ***************** END get total business days of swap  ***************************
		
		//******************* get currRefPoint and total business days until expiry
		//                      which will be used in loop ************************
		//   first get the current reference point, which is either the beginning of the month
		//    or the evalDate, if it is later
		Calendar currRefPoint = Dates.getDifference(evalDate, dateOfAveragingBegin, TimeUnit.DAYS)<0  ?  
				(Calendar)evalDate.clone()  :(Calendar)dateOfAveragingBegin.clone();
		int totalBusDaysTillExpiry = Dates.getAllBusinessDays(locale,currRefPoint, expiryOfAcutal).size();
		//******************* END get currRefPoint and total business days until expiry
		//                      which will be used in loop ************************

		// ************************ get weighting info ***********************
		Tuple<Integer[],Double[]> infoTuple = getWeightInfo(underlyingSecDefs, currRefPoint, expiryOfAcutal, totalBusDaysOfSwap, totalBusDaysTillExpiry, locale);
		
		Double[] weightsOfUnSettledContracts = infoTuple.getT2_instance();
		if(weightsOfUnSettledContracts==null){
			return errRet("Can't compute leg weights for: "+apShortName);
		}
		// ************************ END get weighting info ***********************

		// ************** calculate priceOfRemainingDays: 
		//            the average of the part of AP that remains to be averaged *****************
		double priceOfRemainingDays = 0.0;
		for(int i =0;i<underlyingSecDefs.length;i++){
			Double weight = weightsOfUnSettledContracts[i];
			if(weight<=0){
				continue;
			}
			if(underlyingSettles[i]==null){
				return errRet("no settlement for : "+underlyingSecDefs[i].getShortName());
			}
			double currMid = underlyingSettles[i].getPrice().doubleValue();
			priceOfRemainingDays = priceOfRemainingDays + weight * currMid;
		}
		// ************** END calculate priceOfRemainingDays: 
		//            the average of the part of AP that remains to be averaged *****************
		
		//*****************  get totaling info regarding each underlying component
		//          such as weightOfRemainingDays, weightedPriceOfRemainingDays, and
		//                  priceOfExpiredDays	*********************
		Integer[] daysInfo = infoTuple.getT1_instance();
		int totalDaysOfSwap = daysInfo[0];
		int totalDaysUnSettledInSwap = daysInfo[1];
		double weightOfRemainingDays = 1.0*totalDaysUnSettledInSwap/totalDaysOfSwap;
		double weightedPriceOfRemainingDays = priceOfRemainingDays*weightOfRemainingDays;
		double priceOfExpiredDays = actSetPr.doubleValue() - weightedPriceOfRemainingDays;
		//***************** END get totaling info regarding each underlying component
		//          such as weightOfRemainingDays, weightedPriceOfRemainingDays, and
		//                  priceOfExpiredDays	*********************
		
		// ******************* create final AveragePriceFutureInfo instance ***************
		int prec = actualSd.getExchangePrecision();
		BigDecimal prevSettledPart = new BigDecimal(priceOfExpiredDays).setScale(prec,RoundingMode.HALF_EVEN);
		BigDecimal prevSettledPercent = new BigDecimal(1-weightOfRemainingDays).setScale(20,RoundingMode.HALF_EVEN);
		BigDecimal unSettledPart = 
				new BigDecimal(weightedPriceOfRemainingDays).setScale(prec,RoundingMode.HALF_EVEN);
		Long yyyyMmDdOfActualSettlement = actualSettle.getTime();
		AveragePriceFutureInfo apfi = new AveragePriceFutureInfo(actSetPr, expiryOfAcutal,
				dateOfAveragingBegin,yyyyMmDdOfActualSettlement,
				prevSettledPart, prevSettledPercent, unSettledPart, 
				underlyingSecDefs, weightsOfUnSettledContracts);
		return new ComplexQueryResult<AveragePriceFutureInfo>(null, apfi);
		// ******************* END create final AveragePriceFutureInfo instance ***************
	}
	
	
	private static ComplexQueryResult<AveragePriceFutureInfo> errRet(String s){
		Exception e = Utils.IllState(AveragePriceFutureInfo.class, s);
		ComplexQueryResult<AveragePriceFutureInfo> ret = 
				new ComplexQueryResult<AveragePriceFutureInfo>(e, null);
		return ret;
	}
	
	/**
	 * 
	 * @param componentSds
	 * @param begDate - either the first of the swap month or the eval date, which ever is further in time
	 * @param actualExpiry - expiry of swap
	 * @param totalBusDaysOfSwap
	 * @param totalBusDaysTillExpiry
	 * @param locale
	 * @return
	 */
	private static Tuple<Integer[], Double[]> getWeightInfo(SecDef[] componentSds, Calendar begDate, Calendar actualExpiry, 
			int totalBusDaysOfSwap,int totalBusDaysTillExpiry, String locale){
		Double[] ret = new Double[componentSds.length];
		Calendar currRefPoint = (Calendar)begDate.clone();
		// loop to create percentages of years
		for(int i = 0;i<componentSds.length;i++){
			SecDef sd = componentSds[i];
			Calendar expiryOfCurrentSecDef = MarketDataComLib.getCalendarOfExpiryFromSecDef(sd);
			if(Dates.getDifference(currRefPoint,expiryOfCurrentSecDef, TimeUnit.DAYS)<0){
				// this has expired
				ret[i] = 0.0;
			}else if(Dates.getDifference(expiryOfCurrentSecDef,actualExpiry, TimeUnit.DAYS)<=0){
				// expiry of this component is beyond expiry of actual
				int totalBusDaysTillExpiryOfThisComponent = 
						Dates.getAllBusinessDays(locale,currRefPoint, actualExpiry).size();
				double perc = 1.0*totalBusDaysTillExpiryOfThisComponent/totalBusDaysTillExpiry;
				ret[i] = new Double(perc);
			}else{
				// expiry of component is before expiry of actual
				int totalBusDaysTillExpiryOfThisComponent = 
						Dates.getAllBusinessDays(locale,currRefPoint, expiryOfCurrentSecDef).size();
				double perc = 1.0*totalBusDaysTillExpiryOfThisComponent/totalBusDaysTillExpiry;
				ret[i] = new Double(perc);
				currRefPoint = Dates.addBusinessDays(locale, expiryOfCurrentSecDef, 1);
			}
			// now you want to add one business day from what date is greater:
			 //   currRefPoint or expiryOfCurrentSecDef
		}
		Integer[] daysArray = new Integer[]{totalBusDaysOfSwap,totalBusDaysTillExpiry};
		Tuple<Integer[], Double[]> retTuple = new Tuple<Integer[], Double[]>(daysArray, ret);
		return retTuple;

	}
	
	


}
