package com.billybyte.csvprocessing.cmeexpiryprocessing;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("CALENDAR_DATA")
public class CmeCalendarData {
	String DATE_CREATED;
	
	@XStreamImplicit(itemFieldName="PRODUCT_TYPES")
	List<CmeProductTypes> productTypes;
	
}
