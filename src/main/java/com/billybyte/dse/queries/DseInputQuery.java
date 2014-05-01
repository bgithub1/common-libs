package com.billybyte.dse.queries;


import java.util.Map;
import java.util.Set;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.queries.ComplexQueryResult;

/**
 * All inputs to the Dse will take the form of QueryInterface<Set<String>, Map<String, ComplexQueryResult<T>>>
 * 
 * @author bperlman1
 *
 * @param <T>
 */
public abstract class DseInputQuery<T> implements QueryInterface<Set<String>, Map<String,ComplexQueryResult<T>>>{

}
