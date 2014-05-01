package com.billybyte.csvprocessing.cmeexpiryprocessing;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("PRODUCT")
public class CmeProduct {
	String Exchange;
	String Commodity_Name;
	String Commodity_Code;
	String PRODUCT_TYPE_CODE;
	String Weather_City;
	int Current_Listed;
	
	@XStreamImplicit(itemFieldName="CONTRACT")
	List<CmeContract> contracts;
	
	
}
