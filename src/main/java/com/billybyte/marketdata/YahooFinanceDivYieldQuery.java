package com.billybyte.marketdata;

import java.math.BigDecimal;
import java.util.Map;

import com.billybyte.commonstaticmethods.RegexMethods;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.queries.ComplexQueryResult;

public class YahooFinanceDivYieldQuery extends YahooFinanceCsvAbstractGenericQuery<ComplexQueryResult<BigDecimal>> {
	private static String[] YfFields = {"s","y0"};
	
	public YahooFinanceDivYieldQuery(Map<String, String> exchMap,
			Map<String, String> prodAssocMap) {
		super(exchMap, prodAssocMap, YfFields);
	}

	@Override
	public ComplexQueryResult<BigDecimal> getValue(String shortName,
			String[] line) {
		ComplexQueryResult<BigDecimal> ret = null;
		BigDecimal div = (line[1]==null || !RegexMethods.isNumber(line[1])) ? null : 
			new BigDecimal(line[1]);

		if(div==null){
			Exception e = Utils.IllState(this.getClass(),"No dividend yield returned for "+shortName);
			ret = new ComplexQueryResult<BigDecimal>(e, null);
		}else{
			ret = new ComplexQueryResult<BigDecimal>(null, div);
		}
		return ret;
	}

	@Override
	public ComplexQueryResult<BigDecimal> getNotFound(String shortName) {
		Exception e = Utils.IllState(this.getClass(),"No dividend yield returned for "+shortName);
		return new ComplexQueryResult<BigDecimal>(e, null);
	}

}
