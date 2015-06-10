package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.queries.ComplexQueryResult;

public class YahooFinanceDivYieldQuery extends YahooFinanceCsvAbstractGenericQuery<ComplexQueryResult<BigDecimal>> {
	private static final BigDecimal REALLY_SMALL_RETURN = new BigDecimal("0.00001");
	private static String[] YfFields = {"s","y"};
	private static final BigDecimal hundred = new BigDecimal("100");
	public YahooFinanceDivYieldQuery(Map<String, String> exchMap,
			Map<String, String> prodAssocMap) {
		super(exchMap, prodAssocMap, YfFields);
	}

	public YahooFinanceDivYieldQuery() {
		super(YfFields);
	}

	
	@Override
	public ComplexQueryResult<BigDecimal> getValue(String shortName,
			String[] line) {
		ComplexQueryResult<BigDecimal> ret = null;
		BigDecimal div = (line[1]==null || !RegexMethods.isNumber(line[1])) ? null : 
			new BigDecimal(line[1]);

		if(div==null){
			div = REALLY_SMALL_RETURN;
		}
		ret = new ComplexQueryResult<BigDecimal>(null, div.divide(hundred));

		return ret;
	}

	@Override
	public ComplexQueryResult<BigDecimal> getNotFound(String shortName) {
		Exception e = Utils.IllState(this.getClass(),"No dividend yield returned for "+shortName);
		return new ComplexQueryResult<BigDecimal>(e, null);
	}
	
	public static void main(String[] args) {
		YahooFinanceDivYieldQuery divQuery = 
				new YahooFinanceDivYieldQuery();
		String[] stkArray = 
			{
				"IBM.STK.SMART",
				"AAPL.STK.SMART",
				"MSFT.STK.SMART",
				"GOOG.STK.SMART",
			};
		Set<String> underlyingSnSet = 
				CollectionsStaticMethods.setFromArray(stkArray);
		Map<String, ComplexQueryResult<BigDecimal>> divMap =
				divQuery.get(underlyingSnSet, 10, TimeUnit.SECONDS);
		CollectionsStaticMethods.prtMapItems(divMap);

	} 
	

}
