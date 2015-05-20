package com.billybyte.marketdata.futures.apos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
/**
 * Class to use to calculate Nymex ap's and apo's like CS or AO
 * @author bperlman1
 *
 */

import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.marketdata.SecDef;

/**
 * Input block to support models that have a signature of:
 * int callPut, double[] percEachParmIsOfTotal,
			Number[] atms,  double strike, Number[] vols,   
			Number[] rates, Number[] divs,  double dteAsPercOfYear,double averagingPeriodAsPercOfYear,
			double averagePrice
 * @author bperlman1
 *
 */
public class CalendarSwapAveragePriceInputBlock {
	private final String derivativeShortName;
	private final String underlyingShortNames[];
	private final Double callPut;
	private final BigDecimal strike;
	private final BigDecimal realtimeAveragePrice;
	private final double timeToExpiryAsPercOfYear;
	private final double timeToaveragingPeriodStartAsPercOfYear;
	private final BigDecimal[] underlyingAtms;
	private final BigDecimal[] underlyingVols;
	private final Double[] underlyingWeights;
	private final BigDecimal[] rates;
	private final BigDecimal[] divs;
	
	
	
	public CalendarSwapAveragePriceInputBlock(
			Calendar evalDate,
			SecDef apSd, 
			AveragePriceFutureInfo apfi,
			BigDecimal[] unSettledAtmPrices,
			BigDecimal[] unSettledVols,
			BigDecimal[] unSettledRates,
			BigDecimal[] unSettledDivs){
		this(
				apSd.getShortName(),
				createUnderlyingShortNames(apfi),
				createCallPut(apSd), 
				unSettledAtmPrices, 
				createRealtimeAveragePrice(apfi, unSettledAtmPrices), 
				apSd.getStrike(),
				createTimeToExpiry(evalDate, apfi.getExpiryOfActual()),
				createTimeToAveragingBegin(evalDate,apfi.getDateOfAveragingBegin()),
				unSettledVols,
				unSettledRates,
				unSettledDivs, 
				apfi.getUnderlyingUnSettledPercents());
	}
	
	private CalendarSwapAveragePriceInputBlock(
			String derivativeShortName,
			String[] underlyingShortNames,
			Double callPut,
			BigDecimal[] unSettledAtms, 
			BigDecimal realtimeAveragePrice,
			BigDecimal strike, 
			double timeToExpiryAsPercOfYear,
			double timeToaveragingPeriodStartAsPercOfYear, 
			BigDecimal[] unSettledVols,
			BigDecimal[] rates, 
			BigDecimal[] divs, 
			Double[] unSettledWeights) {
		super();
		this.callPut = callPut;
		this.derivativeShortName = derivativeShortName;
		this.underlyingShortNames = underlyingShortNames;
		this.underlyingAtms = unSettledAtms;
		this.realtimeAveragePrice = realtimeAveragePrice;
		this.strike = strike;
		this.timeToExpiryAsPercOfYear = timeToExpiryAsPercOfYear;
		this.timeToaveragingPeriodStartAsPercOfYear = timeToaveragingPeriodStartAsPercOfYear;
		this.underlyingVols = unSettledVols;
		this.rates = rates;
		this.divs = divs;
		this.underlyingWeights = unSettledWeights;
	}

	
	private static final String[] createUnderlyingShortNames(AveragePriceFutureInfo apfi){
		String[] ret = new String[apfi.getUnderlyingSecDefs().length];
		for(int i = 0;i<ret.length;i++){
			SecDef sd = apfi.getUnderlyingSecDefs()[i];
			ret[i] = sd!=null?sd.getShortName():null;
		}
		return ret;
	}

	private static final double createTimeToAveragingBegin(Calendar evalDate, Calendar dateOfAverageingBegin){
		long days = Dates.getDifference(evalDate, dateOfAverageingBegin, TimeUnit.DAYS);
		if(days<0)return 0.0;
		return 1.0*days/365;
	}
	private static final double createTimeToExpiry(Calendar evalDate, Calendar actualExpiry){
		long days = Dates.getDifference(evalDate, actualExpiry, TimeUnit.DAYS)+1;
		return 1.0*days/365;
	}
	
	private static final Double createCallPut(SecDef sd){
		
		String right = sd.getRight();
		if(right==null)return null;
		if(right.toUpperCase().compareTo("P")==0)return 1.0;
		return 0.0;
	}


	/**
	 * 
	 * @param apfo
	 * @param underlyingPrices BigDecimal[] array whose size MUST be
	 *         the same size as AveragePriceFutureInfo.underlyingSecDefs.length
	 * @return
	 */
	public static final BigDecimal createRealtimeAveragePrice(
			AveragePriceFutureInfo apfo,BigDecimal[] underlyingPrices){
		double unSettledPrice = 0.0;
		Double[] unsettledWeights = apfo.getUnderlyingUnSettledPercents();
		for(int i = 0;i<underlyingPrices.length;i++){
			if(underlyingPrices[i]!=null){
				unSettledPrice += 
						underlyingPrices[i].doubleValue() * unsettledWeights[i];
			}
		}
		BigDecimal unSettledRealtimePriceAsPercOfAllBusDaysInSwap = 
				new BigDecimal(unSettledPrice).multiply(apfo.getUnSettledPerc());
		int prec = apfo.getUnderlyingSecDefs()[0].getExchangePrecision();
		BigDecimal realTimeAverage = 
				apfo.getPrevAveragedPart().add(
					unSettledRealtimePriceAsPercOfAllBusDaysInSwap).setScale(
							prec,RoundingMode.HALF_EVEN);
		return realTimeAverage;
	}


	public Double getCallPut() {
		return callPut;
	}

	public BigDecimal getStrike() {
		return strike;
	}

	public BigDecimal getRealtimeAveragePrice() {
		return realtimeAveragePrice;
	}

	public double getTimeToExpiryAsPercOfYear() {
		return timeToExpiryAsPercOfYear;
	}

	public double getTimeToaveragingPeriodStartAsPercOfYear() {
		return timeToaveragingPeriodStartAsPercOfYear;
	}


	public BigDecimal[] getRates() {
		return rates;
	}

	public String[] getUnderlyingShortNames() {
		return underlyingShortNames;
	}

	public BigDecimal[] getUnderlyingAtms() {
		return underlyingAtms;
	}

	public BigDecimal[] getUnderlyingVols() {
		return underlyingVols;
	}

	public Double[] getUnderlyingWeights() {
		return underlyingWeights;
	}

	public BigDecimal[] getDivs() {
		return divs;
	}

	
	public String getDerivativeShortName() {
		return derivativeShortName;
	}

	public String[] getUnSettledShortNames() {
		return underlyingShortNames;
	}

	@Override
	public String toString() {
		return derivativeShortName + ", "
				+ Arrays.toString(underlyingShortNames) + ", " + callPut + ", "
				+ strike + ", " +
				+ timeToExpiryAsPercOfYear + ", "
				+ timeToaveragingPeriodStartAsPercOfYear + ", "
				+ Arrays.toString(underlyingAtms) + ", "
				+ Arrays.toString(underlyingVols) + ", "
				+ Arrays.toString(underlyingWeights) + ", "
				+ Arrays.toString(rates) + ", " + Arrays.toString(divs);
	}

	
	
}
