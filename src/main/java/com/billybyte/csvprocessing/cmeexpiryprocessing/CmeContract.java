package com.billybyte.csvprocessing.cmeexpiryprocessing;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("CONTRACT")
public class CmeContract {
	// fields from other classes that are higher  up in xml hierarchy .
	// from CmeCalendarData.java
	String DATE_CREATED;
	// from CmeProductTypes.java
	String PRODUCT_TYPE;

	// CmeProduct.java fields
	String Exchange;
	String Commodity_Name;
	String Commodity_Code;
	String PRODUCT_TYPE_CODE;
	String Weather_City;

	// fields from CmeContract.java
	String Contract_Name;
	String Contract_Product_Code;
	String Exp_Contract_Code;
	String Contract_Code;
	String FTD;
	String LTD;
	String SD;
	String DD;
	String IID;
	String FID;
	String FND;
	String FDD;
	String LPD;
	String LID;
	String LND;
	String LDD;
	String Option_Type;
	String Payment_Date;

	// for xstream
	public CmeContract(){}
	
	public CmeContract(CmeCalendarData calData,CmeProductTypes cpts,CmeProduct prod,CmeContract c){
		CmeContract newc = this;

		// fields from CmeCalendarData level
		newc.DATE_CREATED = calData.DATE_CREATED;
		
		// fields from CmeProductTypes level
		newc.PRODUCT_TYPE = cpts.PRODUCT_TYPE;
		
		// fields from CmeProduct level
		newc.Commodity_Code = prod.Commodity_Code;
		newc.Commodity_Name =prod.Commodity_Name;
		newc.Exchange = prod.Exchange;
		newc.PRODUCT_TYPE_CODE = prod.PRODUCT_TYPE_CODE;
		newc.Weather_City = prod.Weather_City;

		// fields from CmeContract level
		newc.Contract_Code = c.Contract_Code;
		newc.Contract_Name = c.Contract_Name;
		newc.Contract_Product_Code = c.Contract_Product_Code;
		newc.DD = c.DD;
		newc.Exp_Contract_Code = c.Exp_Contract_Code;
		newc.FDD = c.FDD;
		newc.FID = c.FID;
		newc.FND = c.FND;
		newc.FTD = c.FTD;
		newc.IID = c.IID;
		newc.LDD = c.LDD;
		newc.LID = c.LID;
		newc.LND = c.LND;
		newc.LPD = c.LPD;
		newc.LTD = c.LTD;
		newc.Option_Type = c.Option_Type;
		newc.Payment_Date = c.Payment_Date;
		newc.SD = c.SD;

	}

	public CmeContract(CmeContract c){
		CmeContract newc = this;

		// fields from CmeCalendarData level
		newc.DATE_CREATED = c.DATE_CREATED;
		
		// fields from CmeProductTypes level
		newc.PRODUCT_TYPE = c.PRODUCT_TYPE;
		
		// fields from CmeProduct level
		newc.Commodity_Code = c.Commodity_Code;
		newc.Commodity_Name =c.Commodity_Name;
		newc.Exchange = c.Exchange;
		newc.PRODUCT_TYPE_CODE = c.PRODUCT_TYPE_CODE;
		newc.Weather_City = c.Weather_City;

		// fields from CmeContract level
		newc.Contract_Code = c.Contract_Code;
		newc.Contract_Name = c.Contract_Name;
		newc.Contract_Product_Code = c.Contract_Product_Code;
		newc.DD = c.DD;
		newc.Exp_Contract_Code = c.Exp_Contract_Code;
		newc.FDD = c.FDD;
		newc.FID = c.FID;
		newc.FND = c.FND;
		newc.FTD = c.FTD;
		newc.IID = c.IID;
		newc.LDD = c.LDD;
		newc.LID = c.LID;
		newc.LND = c.LND;
		newc.LPD = c.LPD;
		newc.LTD = c.LTD;
		newc.Option_Type = c.Option_Type;
		newc.Payment_Date = c.Payment_Date;
		newc.SD = c.SD;

	}

	
	@Override
	public String toString() {
		String ret = Exp_Contract_Code+","+PRODUCT_TYPE_CODE+","+LTD+","+FND+","+Commodity_Name;
		return ret;
	}

	// public getters for all strings
	public String getDATE_CREATED() {
		return DATE_CREATED;
	}

	public String getPRODUCT_TYPE() {
		return PRODUCT_TYPE;
	}

	public String getExchange() {
		return Exchange;
	}

	public String getCommodity_Name() {
		return Commodity_Name;
	}

	public String getCommodity_Code() {
		return Commodity_Code;
	}

	public String getPRODUCT_TYPE_CODE() {
		return PRODUCT_TYPE_CODE;
	}

	public String getWeather_City() {
		return Weather_City;
	}

	public String getContract_Name() {
		return Contract_Name;
	}

	public String getContract_Product_Code() {
		return Contract_Product_Code;
	}

	public String getExp_Contract_Code() {
		return Exp_Contract_Code;
	}

	public String getContract_Code() {
		return Contract_Code;
	}

	public String getFTD() {
		return FTD;
	}

	public String getLTD() {
		return LTD;
	}

	public String getSD() {
		return SD;
	}

	public String getDD() {
		return DD;
	}

	public String getIID() {
		return IID;
	}

	public String getFID() {
		return FID;
	}

	public String getFND() {
		return FND;
	}

	public String getFDD() {
		return FDD;
	}

	public String getLPD() {
		return LPD;
	}

	public String getLID() {
		return LID;
	}

	public String getLND() {
		return LND;
	}

	public String getLDD() {
		return LDD;
	}

	public String getOption_Type() {
		return Option_Type;
	}

	public String getPayment_Date() {
		return Payment_Date;
	};
	
	
	
}
