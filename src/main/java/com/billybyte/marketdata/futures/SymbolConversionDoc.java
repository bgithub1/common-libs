package com.billybyte.marketdata.futures;

import com.billybyte.mongo.MongoDoc;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class SymbolConversionDoc implements MongoDoc {

	private final String symbol;
	private final String desc;
	private final String exch;
	private final String minTick;
	private final String mult;
	private final String currency;
	private final String prodType;
	private final String underlying;
	private final String bestOptSymbol;
	private final String strikePrec;
	private final String spanSym;
	private final Integer spanSettleDecLoc;
	private final Integer spanStrikeDecLoc;
	private final String spanMult;
	private final String gmiSym;
	private final String gmiExch;
	private final String gmiMult;
	private final String fcSym;
	private final String fcExch;
	private final String fcMult;
	private final String iceFixSym;
	private final String iceFixExch;
	private final String iceFixMult;
	private final String cmeFixSym;
	private final String cmeFixExch;
	private final String cmeFixMult;
	
	public SymbolConversionDoc(String symbol, String desc, String exch, String minTick,
			String mult, String currency, String prodType, String underlying, String bestOptSymbol,
			String strikePrec, String spanSym, int spanSettleDecLoc,
			int spanStrikeDecLoc, String spanMult, String gmiSym, String gmiExch,
			String gmiMult, String fcSym, String fcExch, String fcMult,
			String iceFixSym, String iceFixExch, String iceFixMult,
			String cmeFixSym, String cmeFixExch, String cmeFixMult) {
		super();
		this.symbol = symbol;
		this.desc = desc;
		this.exch = exch;
		this.minTick = minTick;
		this.mult = mult;
		this.currency = currency;
		this.prodType = prodType;
		this.underlying = underlying;
		this.bestOptSymbol = bestOptSymbol;
		this.strikePrec = strikePrec;
		this.spanSym = spanSym;
		this.spanSettleDecLoc = spanSettleDecLoc;
		this.spanStrikeDecLoc = spanStrikeDecLoc;
		this.spanMult = spanMult;
		this.gmiSym = gmiSym;
		this.gmiExch = gmiExch;
		this.gmiMult = gmiMult;
		this.fcSym = fcSym;
		this.fcExch = fcExch;
		this.fcMult = fcMult;
		this.iceFixSym = iceFixSym;
		this.iceFixExch = iceFixExch;
		this.iceFixMult = iceFixMult;
		this.cmeFixSym = cmeFixSym;
		this.cmeFixExch = cmeFixExch;
		this.cmeFixMult = cmeFixMult;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getDesc() {
		return desc;
	}
	public String getExch() {
		return exch;
	}

	public String getMinTick() {
		return minTick;
	}

	public String getMult() {
		return mult;
	}

	public String getCurrency() {
		return currency;
	}

	public String getProdType() {
		return prodType;
	}

	public String getUnderlying() {
		return underlying;
	}

	public String getBestOptSymbol() {
		return bestOptSymbol;
	}
	
	public String getStrikePrec() {
		return strikePrec;
	}

	public String getSpanSym() {
		return spanSym;
	}

	public Integer getSpanSettleDecLoc() {
		return spanSettleDecLoc;
	}

	public Integer getSpanStrikeDecLoc() {
		return spanStrikeDecLoc;
	}

	public String getSpanMult() {
		return spanMult;
	}

	public String getGmiSym() {
		return gmiSym;
	}

	public String getGmiExch() {
		return gmiExch;
	}

	public String getGmiMult() {
		return gmiMult;
	}

	public String getFcSym() {
		return fcSym;
	}

	public String getFcExch() {
		return fcExch;
	}

	public String getFcMult() {
		return fcMult;
	}

	public String getIceFixSym() {
		return iceFixSym;
	}

	public String getIceFixExch() {
		return iceFixExch;
	}

	public String getIceFixMult() {
		return iceFixMult;
	}

	public String getCmeFixSym() {
		return cmeFixSym;
	}

	public String getCmeFixExch() {
		return cmeFixExch;
	}

	public String getCmeFixMult() {
		return cmeFixMult;
	}

	@Override
	public DBObject getDBObject() {
		DBObject doc = new BasicDBObject();
		doc.put("symbol",symbol);
		doc.put("desc",desc);
		doc.put("exch",exch);
		doc.put("minTick",minTick);
		doc.put("mult",mult);
		doc.put("currency",currency);
		doc.put("prodType",prodType);
		doc.put("underlying",underlying);
		doc.put("bestOptSymbol",bestOptSymbol);
		doc.put("strikePrec",strikePrec);
		doc.put("spanSym",spanSym);
		doc.put("spanSettleDecLoc",spanSettleDecLoc);
		doc.put("spanStrikeDecLoc",spanStrikeDecLoc);
		doc.put("spanMult",spanMult);
		doc.put("gmiSym",gmiSym);
		doc.put("gmiExch",gmiExch);
		doc.put("gmiMult",gmiMult);
		doc.put("fcSym",fcSym);
		doc.put("fcExch",fcExch);
		doc.put("fcMult",fcMult);
		doc.put("iceFixSym",iceFixSym);
		doc.put("iceFixExch",iceFixExch);
		doc.put("iceFixMult",iceFixMult);
		doc.put("cmeFixSym",cmeFixSym);
		doc.put("cmeFixExch",cmeFixExch);
		doc.put("cmeFixMult",cmeFixMult);
		return doc;
	}
	
}
