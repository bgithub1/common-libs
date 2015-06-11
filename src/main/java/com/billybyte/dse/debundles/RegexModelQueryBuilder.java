package com.billybyte.dse.debundles;

import java.util.Calendar;
import java.util.HashMap;

import com.billybyte.dse.DerivativeModelInterface;
import com.billybyte.dse.inputs.diotypes.VolDiot;
import com.billybyte.dse.models.vanilla.VanOptBawAmerican;
import com.billybyte.dse.models.vanilla.VanOptUnderlying;
import com.billybyte.queries.QueryFromRegexPattern;

/**
 * static method builders to create instances of 
 *    QueryFromRegexPattern<String, DerivativeModelInterface> .
 *    These queries map security shortName patterns to derivative models.
 *    
 * @author bperlman1
 *
 */
public class RegexModelQueryBuilder {
	/**
	 * Basic QueryFromRegexPattern for use with yahoo input queries.
	 *   This Query only returns a Baw model for stock options, 
	 *     and an "underlying" pseudo model for stocks themselves.
	 *    This does not map FUT or FOP types.
	 *    
	 * @param evalDate
	 * @return QueryFromRegexPattern<String, DerivativeModelInterface>
	 */
	public static final QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQueryForStksOptsFromYahoo(Calendar evalDate){
		//   1  Create option models for both options and underlyings
		VanOptBawAmerican vanBawModel = 
				new  VanOptBawAmerican(evalDate, new VolDiot());
		VanOptUnderlying vanUnderlyingModel = 
				new VanOptUnderlying(evalDate, new VolDiot());
		
		// 2  Create model map for dse
		HashMap<String,DerivativeModelInterface> modelMap = 
				new HashMap<String, DerivativeModelInterface>();
		modelMap.put("(\\.OPT\\.)",vanBawModel);
		modelMap.put("(\\.STK\\.)",vanUnderlyingModel);
		QueryFromRegexPattern<String, DerivativeModelInterface> regexModelQuery =
				new QueryFromRegexPattern<String, DerivativeModelInterface>(modelMap);
		return regexModelQuery;
	}
}
