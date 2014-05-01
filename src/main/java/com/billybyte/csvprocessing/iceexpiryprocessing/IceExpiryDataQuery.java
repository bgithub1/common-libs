package com.billybyte.csvprocessing.iceexpiryprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.neodatis.DatabaseAccessEngine;
import com.billybyte.neodatis.NeoDatisSingleClassDataBase;

public class IceExpiryDataQuery extends DatabaseAccessEngine<IceExpiryData>{//implements QueryInterface<List<Tuple<String, String>>, List<IceExpiryData>>{

	public IceExpiryDataQuery(
			NeoDatisSingleClassDataBase<List<Tuple<String, String>>, IceExpiryData> database) {
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
		NeoDatisSingleClassDataBase<List<Tuple<String, String>>,IceExpiryData> database = 
			new NeoDatisSingleClassDataBase<List<Tuple<String,String>>, IceExpiryData>(databaseName,IceExpiryData.class);
		IceExpiryDataQuery iceQuery = new IceExpiryDataQuery(database);
		
		// do queries
		query(iceQuery,"H.FUT.ICE%");
		query(iceQuery,"SB.FUT.NYBOT%");
		query(iceQuery,"G.FUT.IPE");
		query(iceQuery,"T.FUT.IPE%");
	}
	

	public static void query(IceExpiryDataQuery iceQuery,String shortNameRegex){
		List<Tuple<String,String>> list = new ArrayList<Tuple<String,String>>();
		list.add(new Tuple<String, String>("shortName",shortNameRegex));
		List<IceExpiryData> results = iceQuery.queryLike(new String[][]{{"shortName",shortNameRegex}});
		prt(" beg");
		for(IceExpiryData ied:results){
			prt(ied);
		}
		prt(" end");
	}

}
