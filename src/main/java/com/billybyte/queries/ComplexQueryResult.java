package com.billybyte.queries;

import java.util.List;
/**
 * Class that wraps the return of a QueryInterface get, so that you 
 * 		can have information about why the get failed.
 * @author bperlman1
 *
 * @param <E>
 */
public class ComplexQueryResult<E> {
	private final Exception exception;
	private final E result;

	public ComplexQueryResult(Exception exception, E result) {
		super();
		this.exception = exception;
		this.result = result;
	}


	
	public Exception getException() {
		return exception;
	}
	public E getResult() {
		return result;
	}
	
	public boolean isValidResult(){
		if(getException()==null && getResult()!=null){
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		return (exception==null?"null":exception) + ", " + (result==null?"null":result.toString());
	}
	
}
