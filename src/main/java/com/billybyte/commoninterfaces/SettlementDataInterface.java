package com.billybyte.commoninterfaces;

import com.billybyte.marketdata.PriceLevelData;

/**
 * This interface contains all the methods for classes that use
 * price oriented data.  
 * @author Bill Perlman
 *
 */
public interface SettlementDataInterface extends PriceLevelData {
	public String getShortName();
	public String getToStringSeparator();
}
