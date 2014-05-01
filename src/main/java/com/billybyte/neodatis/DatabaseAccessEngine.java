package com.billybyte.neodatis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.neodatis.odb.core.query.criteria.ICriterion;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonstaticmethods.Utils;
import com.billybyte.csvprocessing.iceexpiryprocessing.IceExpiryDataQuery;
/**
 * 
 * @author Bill Perlman
 *
 * @param <T>
 */
public class DatabaseAccessEngine<T>  {
	private final NeoDatisSingleClassDataBase<List<Tuple<String, String>>, T> database;
	private final Object database_Lock = new Object();
	
	public enum DatabaseAccessEngineType{
		QUERY_EXACT,
		QUERY_LIKE
	}
	
	public DatabaseAccessEngine(
			NeoDatisSingleClassDataBase<List<Tuple<String, String>>, T> database) {
		super();
		this.database = database;
	}

	public DatabaseAccessEngine(String databaseName,Class<T> classOfT){
		super();
		this.database = new NeoDatisSingleClassDataBase<List<Tuple<String,String>>, T>(databaseName,classOfT);
	}
	

	protected DatabaseAccessEngine() {
		super();
		this.database = null;
	}

	protected static void prtErr(String s){
		Utils.prtObErrMess(IceExpiryDataQuery.class, s);
	}
	protected static void prt(Object s){
		Utils.prtObMess(IceExpiryDataQuery.class, s.toString());
	}


	
	private List<Tuple<String,String>> buildKey(String[][] fieldValuePairs){
		if(fieldValuePairs==null)return null;
		if(fieldValuePairs.length<1)return null;
		
		List<Tuple<String,String>> list = new ArrayList<Tuple<String,String>>();
		for(String[] pair:fieldValuePairs){
			if(pair==null){
				Utils.prtObErrMess(this.getClass(), " null fieldValuePair in buildKey");
				return null;
			}
			if(pair.length<2){
				Utils.prtObErrMess(this.getClass(), " invalid fieldValuePair in buildKey"+Arrays.toString(pair));
				return null;
			}
			list.add(new Tuple<String, String>(pair[0],pair[1]));
		}
		return list;
	}
	public List<T>  queryLike(String[][] fieldValuePairs){
		List<Tuple<String,String>> list = buildKey(fieldValuePairs);
		if(list==null)return null;
		List<T> expiryData;
		synchronized (database_Lock) {
			expiryData = database
					.getLikeWithMultipleConditionsLogicallyAnded(list);
		}
		return expiryData;
	}

	public List<T>  queryExact(String[][] fieldValuePairs){
		List<Tuple<String,String>> list = buildKey(fieldValuePairs);
		if(list==null)return null;
		List<T> expiryData;
		synchronized (database_Lock) {
			expiryData = database
					.getWithMultipleConditionsLogicallyAnded(list);
		}
		return expiryData;
	}
	
	public List<T>  queryWithCriterion(ICriterion criterion){
		List<T> expiryData;
		synchronized (database_Lock) {
			expiryData = database.getWithCriterion(criterion);
		}
		return expiryData;
	}


	public void putData(T dataToPut){
		synchronized (database_Lock) {
			database.put(dataToPut);
		}
	}
	
	public void closeDatabase(){
		database.close();
	}
	
	public void openDatabase(){
		database.open();
	}
	
	public void saveDatabase(){
		database.saveDataBase();
	}
	
	public void deleteAll(){
		database.deleteAll();
	}
	
	public void deleteObject(T t){
		database.deleteObject(t);
	}
}
