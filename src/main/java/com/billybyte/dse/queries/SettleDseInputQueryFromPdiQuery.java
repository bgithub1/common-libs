package com.billybyte.dse.queries;

import java.util.Map;
import java.util.Set;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commoninterfaces.SettlementDataInterface;
import com.billybyte.marketdata.PriceDisplayInterface;
import com.billybyte.marketdata.SecDef;
import com.billybyte.marketdata.SettlementDataImmute;
import com.billybyte.queries.ComplexQueryResult;

/**
 * Get settlement from settlement inside of pdi's (usually the prevClose field of
 *    something like a yahoo price data pdi.
 *    
 * @author bperlman1
 *
 */
public class SettleDseInputQueryFromPdiQuery extends DseInputQueryFromPdiQuery<SettlementDataInterface>{

	/**
	 * 
	 * @param sdQuery
	 * @param pdiQuery
	 * @param evalDate
	 */
	public SettleDseInputQueryFromPdiQuery(
			QueryInterface<String, SecDef> sdQuery,
			QueryInterface<Set<String>, Map<String, ComplexQueryResult<PriceDisplayInterface>>> pdiQuery,
			long evalDate) {
		super(sdQuery, pdiQuery, evalDate);
	}

	@Override
	public ComplexQueryResult<SettlementDataInterface> getValue(
			PriceDisplayInterface pdi, SecDef sd, Long evalDate) {
		SettlementDataInterface settle = 
				new SettlementDataImmute(pdi.getShortName(), pdi.getSettlement(), 1, evalDate);
		return new ComplexQueryResult<SettlementDataInterface>(null,settle);
	}
	

}
