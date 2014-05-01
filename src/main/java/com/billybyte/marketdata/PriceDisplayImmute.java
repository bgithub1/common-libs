package com.billybyte.marketdata;

import java.math.BigDecimal;

import com.billybyte.marketdata.PriceLevelWithType.TickField;

/**
 * Used to display top book info
 * @author bperlman1
 *
 */
public class PriceDisplayImmute implements PriceDisplayInterface {
	private final String shortName;
	private final PriceLevelData bid;
	private final PriceLevelData offer;
	private final PriceLevelData last;
	private final BigDecimal settlement;

	public PriceDisplayImmute(String shortName, PriceLevelData bid,
			PriceLevelData offer, PriceLevelData last, BigDecimal settlement) {
		super();
		this.shortName = shortName;
		this.bid = bid;
		this.offer = offer;
		this.last = last;
		this.settlement = settlement;
	}

	public PriceDisplayImmute(PriceDisplayInterface pd) {
		super();
		this.shortName = pd.getShortName();
		this.bid = new PriceLevelWithType(pd.getBid().getPrice(), pd.getBid().getSize(), pd.getBid().getTime(), null);
		this.offer = new PriceLevelWithType(pd.getOffer().getPrice(), pd.getOffer().getSize(), pd.getOffer().getTime(), null);
		this.last = new PriceLevelWithType(pd.getLast().getPrice(), pd.getLast().getSize(), pd.getLast().getTime(), null);
		this.settlement = pd.getSettlement();
	}

	public PriceDisplayImmute(String shortName, BigDecimal bid, int bidSize,BigDecimal offer, int offerSize,
			BigDecimal last, int lastSize, BigDecimal settlement, long time){
		super();
		this.shortName = shortName;
		PriceLevelData bidLevel  = new PriceLevelWithType(bid, bidSize,time, TickField.BID);
		PriceLevelData askLevel  = new PriceLevelWithType(offer, offerSize,time, TickField.ASK);
		PriceLevelData lastLevel  = new PriceLevelWithType(last, lastSize,time, TickField.LAST);
		this.bid = bidLevel;
		this.offer = askLevel;
		this.last  = lastLevel;
		this.settlement = settlement;
	}
	
	@Override
	public String getShortName() {
		return shortName;
	}
	@Override
	public PriceLevelData getBid() {
		return bid;
	}
	@Override
	public PriceLevelData getOffer() {
		return offer;
	}
	@Override
	public PriceLevelData getLast() {
		return last;
	}
	@Override
	public BigDecimal getSettlement() {
		return settlement;
	}
	@Override
	public String toString() {
		return shortName+","+
				(settlement==null?"null":settlement.toString())+","+
				(bid==null?"null":bid.toString())+","+
				(offer==null?"null":offer.toString())+","+
				(last==null?"null":last.toString());
	}
	
	
}
