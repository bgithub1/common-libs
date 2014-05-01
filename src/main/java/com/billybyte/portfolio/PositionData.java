package com.billybyte.portfolio;

import java.math.BigDecimal;

import com.billybyte.commoninterfaces.CsvToString;


public class PositionData implements CsvToString {
	/**
	 * This enum describes all valid column headings for a csv file that contains PositionData
	 *   If fields are added to PositionData, then this enum should be updated
	 * @author bperlman1
	 *
	 */
	public enum PositionDataCsvHeaderField{
		shortName,accountId,traderId,owner,qty,execPrice,basisPrice,yyyyMmDdHhMmSsMmm,tradeType,date;
	}

	public enum TradeTypeEnum{
		POSITION,
		TOPDAY
	}
	
	private final String shortName;
	private final TradeTypeEnum tradeType;
	private final BigDecimal execPrice;
	private final BigDecimal refPrice;
	private final Long date;
	private final BigDecimal qty;
	private final String accountId;
	private final String traderId;
	
	
	public String getAccountId() {
		return accountId;
	}

	public String getTraderId() {
		return traderId;
	}

//	public PositionData(
//			String shortName,String accountId,String traderId,
//			TradeTypeEnum tradeType, BigDecimal execPrice,
//			BigDecimal refPrice, Long date, BigDecimal qty) {
	public PositionData(
			String shortName,String accountId,String traderId,
			TradeTypeEnum tradeType, BigDecimal execPrice,
			BigDecimal refPrice, Long date, BigDecimal qty) {

		super();
		this.shortName = shortName;
		this.tradeType = tradeType;
		this.execPrice = execPrice;
		this.refPrice = refPrice;
		this.date = date;
		this.qty = qty;
		this.accountId = accountId;
		this.traderId = traderId;
	}
	
	public PositionData(PositionData pd){
		this(pd.getShortName(), pd.getAccountId(), pd.getTraderId(), pd.getTradeType(),
				pd.getExecPrice(), pd.getRefPrice(), pd.getDate(), pd.getQty());
	}
	
	public String getShortName() {
		return shortName;
	}

	public TradeTypeEnum getTradeType() {
		return tradeType;
	}

	public BigDecimal getExecPrice() {
		return execPrice;
	}

	public BigDecimal getRefPrice() {
		return refPrice;
	}

	public Long getDate() {
		return date;
	}

	public BigDecimal getQty() {
		return qty;
	}

	@Override
	public String toString() {
		return shortName + "," + tradeType + "," + execPrice + ","
				+ refPrice + "," + date + "," + qty + "," + accountId + ","
				+ traderId;
	}

	@Override
	public String[] getHeaderStrings() {
		return "shortName,tradeType,execPrice,refPrice,date,qty,accountId,traderId".split(",");
	}

	public static PositionData multiplyQty(PositionData pd, BigDecimal multiplier) {
		return new PositionData(pd.getShortName(), pd.getAccountId(), pd.getTraderId(), pd.getTradeType(), pd.getExecPrice(),
				pd.getRefPrice(), pd.getDate(), pd.getQty().multiply(multiplier));
	}
	

}
