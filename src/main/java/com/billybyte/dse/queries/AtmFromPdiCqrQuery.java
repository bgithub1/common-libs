package com.billybyte.dse.queries;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.Dates;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.PriceLevelData;
import com.billybyte.marketdata.SecDef;
import com.billybyte.queries.ComplexQueryResult;

public class AtmFromPdiCqrQuery extends DseInputQueryFromPdiQuery<BigDecimal> {

	public AtmFromPdiCqrQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			long evalDate) {
		super(sdQuery, pdiQuery, evalDate);		
	}

	public AtmFromPdiCqrQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			Calendar evalDate) {
		this(sdQuery, pdiQuery, Dates.getYyyyMmDdFromCalendar(evalDate));		
	}

	
	@Override
	public ComplexQueryResult<BigDecimal> getValue(PriceDisplayInterface pdi,
			SecDef sd, Long evalDate) {
		String shortName = null;
		if(sd==null){
			if(pdi!=null && pdi.getShortName()!=null){
				shortName=pdi.getShortName();
			}else{
				return errRet("Can't find shortName");
			}
		}else{
			shortName = sd.getShortName();
		}
		
		// get all pricing fields
		BigDecimal bid = null;
		PriceLevelData bidPld = pdi.getBid();
		if(bidPld!=null){
			bid = bidPld.getPrice();
		}
		BigDecimal offer = null;
		PriceLevelData offerPld = pdi.getOffer();
		if(offerPld!=null){
			offer = offerPld.getPrice();
		}
		BigDecimal last = null;
		PriceLevelData lastPld = pdi.getLast();
		if(lastPld!=null){
			last = lastPld.getPrice();
		}
		BigDecimal settle = pdi.getSettlement();

		
		// are either bid or offer null?
		if(bid==null || offer==null){
			// use either last or settlement
			if(last==null){
				// use settle
				if(settle==null){
					return errRet(shortName+ " bid,offer,last and settle are null");
				}else{
					// return settle
					return new ComplexQueryResult<BigDecimal>(null, settle);
				}
			}else{
				// return last
				return new ComplexQueryResult<BigDecimal>(null, last);
			}
		}
		
		// see if bid is less than offer
		if(bid.compareTo(offer)>0){
			// return last or settle
			if(last!=null){
				return new ComplexQueryResult<BigDecimal>(null, last); 
			}
			if(settle!=null){
				return new ComplexQueryResult<BigDecimal>(null, settle);
			}
			// you got a problem
			return errRet(shortName+ " bid higher than offer, and no last or settle is available");
		}
		// see if price is in between bid and offer
		if(bid.compareTo(last)<=0 && last.compareTo(offer)<=0){
			// use last
			return new ComplexQueryResult<BigDecimal>(null, last); 
		}else{
			// use mid point of bid and offer
			BigDecimal mid = bid.add(offer).divide(new BigDecimal("2")).setScale(2, RoundingMode.HALF_EVEN);
			return new ComplexQueryResult<BigDecimal>(null, mid); 
		}
		
	}

	private final ComplexQueryResult<BigDecimal> errRet(String mess){
		Exception e = Utils.IllState(this.getClass(), mess);
		ComplexQueryResult<BigDecimal> err = 
				new ComplexQueryResult<BigDecimal>(e, null);
		return err;
	}
}
