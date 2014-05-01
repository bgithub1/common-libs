package com.billybyte.csvprocessing.iceexpiryprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.neodatis.DatabaseAccessEngine;
import com.billybyte.neodatis.NeoDatisSingleClassDataBase;

public class IceProductInfoQuery extends DatabaseAccessEngine<IceProductInfo>{

	public IceProductInfoQuery(
			NeoDatisSingleClassDataBase<List<Tuple<String, String>>, IceProductInfo> database) {
		super(database);
	}
	
	public static void main(String[] args) {
		// check args
		if(args==null){
			prtErr(" no command args");
		}
		if(args.length<1){
			prtErr(" no command args");
		}
		String databaseName = args[0];
		NeoDatisSingleClassDataBase<List<Tuple<String, String>>,IceProductInfo> database = 
			new NeoDatisSingleClassDataBase<List<Tuple<String,String>>, IceProductInfo>(databaseName,IceProductInfo.class);
		IceProductInfoQuery query = new IceProductInfoQuery(database);
		String[][] pairs = {{"exchange","IPE"}};
		prtData(false,query,pairs);

		pairs = new String[][]{{"exchange","IPE"},{"symbol","T"}};
		prtData(false,query,pairs);
	
		pairs = new String[][]{{"symbol","(T|H)"}};
		prtData(false,query,pairs);
	}
	private static void prtData(boolean exact,IceProductInfoQuery iceQuery,String[][] fieldValuePairs){
		List<IceProductInfo> results;
		if(exact){
			results = iceQuery.queryExact(fieldValuePairs);
		}else{
			results = iceQuery.queryLike(fieldValuePairs);
		}
		prt(" beg");
		for(IceProductInfo ied:results){
			prt(ied);
		}
		prt(" end");
	}

}
