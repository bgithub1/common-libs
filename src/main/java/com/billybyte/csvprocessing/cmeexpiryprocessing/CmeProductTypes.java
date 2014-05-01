package com.billybyte.csvprocessing.cmeexpiryprocessing;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("PRODUCT_TYPES")
public class CmeProductTypes {
	String PRODUCT_TYPE;
	
	@XStreamImplicit(itemFieldName="PRODUCT")
	List<CmeProduct> products;
}
