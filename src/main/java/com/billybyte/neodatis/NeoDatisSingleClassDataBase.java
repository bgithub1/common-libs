package com.billybyte.neodatis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.neodatis.odb.ODB;
import org.neodatis.odb.ODBFactory;
import org.neodatis.odb.OID;
import org.neodatis.odb.Objects;
import org.neodatis.odb.core.query.IQuery;

import org.neodatis.odb.core.query.criteria.ICriterion;
import org.neodatis.odb.core.query.criteria.Where;
import org.neodatis.odb.impl.core.query.criteria.CriteriaQuery;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commoninterfaces.SingleClassDataBase;

/**
 * 
 * @author bperlman1
 *
 * @param <K> Class that defines a key.  This class can be in two forms:
 * 		1.  The class can be the class of a field in the Class T, which is the class of
 * 			of the Entity object.
 * 		2.  When you want to search on multiple String fields:
 * 			The class can be a Tuple<String, String> where the first String is the name
 * 				of a field in the entity class, and the second String is a value for that class.
 * @param <T>
 */
public class NeoDatisSingleClassDataBase<K,T> implements SingleClassDataBase<K, T>{
	private boolean useClient=false;
	private String dataBaseName;
	private int MAX_CONDITIONS_IN_GET=5;
	transient ODB dataBase;
	private transient  final Object odb_Lock = new Object();
	private Class<T> classOfObjectToGet;
	//private static final NeoDatisPortManagerWebServiceQuery portManager = new NeoDatisPortManagerWebServiceQuery();
	private final static int NEODATIS_PORT = 9100;
	public NeoDatisSingleClassDataBase(String dataBaseName,Class<T> classOfObjectToGet) {
		super();
		if(classOfObjectToGet==null){
			throw new  IllegalArgumentException(NeoDatisSingleClassDataBase.class+" classOfObjectToGet is null");
		}
		if(dataBaseName==null){
			throw new  IllegalArgumentException(NeoDatisSingleClassDataBase.class+" databaseName is null");
		}
		this.dataBaseName = dataBaseName;
		this.classOfObjectToGet=classOfObjectToGet;

		open();
//		int port = portManager.get(this.dataBaseName, 3000, TimeUnit.MILLISECONDS);
//		dataBase = ODBFactory.openClient("localhost",port,this.dataBaseName);
//		dataBase = ODBFactory.open(this.dataBaseName);
		
		
		if(dataBase==null){
			throw new  IllegalArgumentException(NeoDatisSingleClassDataBase.class+" databaseName cannot be created");
		}
	}

	
	public T get(String field,K key){
		synchronized (odb_Lock) {
			IQuery query = new CriteriaQuery(classOfObjectToGet, Where.equal(field, key));
			Objects <T>secdefs = dataBase.getObjects(query);
			if(secdefs!=null&& secdefs.size()==1){
				return secdefs.getFirst();
			}else{
				return null;
			}
		}
	}

	/**
	 *  get objects from database by comparing the value of the field (which must be of type string)
	 *    to the value of the keyString, to see if the field value is "like" the keyString
	 *    EXAMPLE:
	 *    	Class Address{
	 *    		String firstName;
	 *    		String lastName;
	 *    		String streetAddress1;
	 *    		String streetAddress2;
	 *    		String city;
	 *    		String state;
	 *    		String country;
	 *    		String zip;
	 *    }
	 *    
	 *    List<Address> addressesThatAreLikeLastNameJones = getLike("lastName","Jones%");
	 *    
	 * @param field : name of field that you want to get.  THIS FIELD MUST BE OF TYPE String !!!
	 * @param keyString : string that holds a value that will be "like" the value that is contained 
	 * 			in the field for each returned object of type T.
	 * @return List<T>  : list of objects of type T that match criteria
	 */
	public List<T> getLike(String field,String keyString){
		synchronized (odb_Lock) {
			
			CriteriaQuery query = new CriteriaQuery(classOfObjectToGet, Where.like(field, keyString));
			Objects <T>returnObjects = dataBase.getObjects(query);
			if(returnObjects!=null&& returnObjects.size()>=1){
				return new ArrayList<T>(returnObjects);
			}else{
				return null;
			}
		}
	}

	
	public List<T> getLikeWithMultipleConditionsLogicallyAnded(List<Tuple<String, String>> conditionList){
		synchronized (odb_Lock) {
			String[]fieldList=new String[MAX_CONDITIONS_IN_GET];
			String[]valueList=new String[MAX_CONDITIONS_IN_GET];
			// first transfer fields and values from tuple list to arrays
			for(int i =0;i<conditionList.size();i++){
				fieldList[i] = conditionList.get(i).getT1_instance();
				valueList[i] = conditionList.get(i).getT2_instance();
			}
			// now, if any array elements are null or blank, initialize them to the previous value
			//   which will basically mean that they are ignored
			for(int i =0;i<MAX_CONDITIONS_IN_GET;i++){
				if(fieldList[i]==null || fieldList[i].compareTo("  ")<=0){
					fieldList[i]=fieldList[i-1];
					valueList[i]=valueList[i-1];
				}
			}
			// create query with all conditions logically and'ed to gether
			CriteriaQuery query = new CriteriaQuery(classOfObjectToGet, Where.and().add(
					Where.like(fieldList[0],valueList[0])).add(
							Where.like(fieldList[1],valueList[1])).add(
									Where.like(fieldList[2],valueList[2])).add(
											Where.like(fieldList[3],valueList[3])).add(
													Where.like(fieldList[4],valueList[4])));
			Objects <T>returnObjects = dataBase.getObjects(query);
			return new ArrayList<T>(returnObjects);
		}
	}

	public List<T> getWithMultipleConditionsLogicallyAnded(List<Tuple<String, String>> conditionList){
		synchronized (odb_Lock) {
			String[]fieldList=new String[MAX_CONDITIONS_IN_GET];
			String[]valueList=new String[MAX_CONDITIONS_IN_GET];
			// first transfer fields and values from tuple list to arrays
			for(int i =0;i<conditionList.size();i++){
				fieldList[i] = conditionList.get(i).getT1_instance();
				valueList[i] = conditionList.get(i).getT2_instance();
			}
			// now, if any array elements are null or blank, initialize them to the previous value
			//   which will basically mean that they are ignored
			for(int i =0;i<MAX_CONDITIONS_IN_GET;i++){
				if(fieldList[i]==null || fieldList[i].compareTo("  ")<=0){
					fieldList[i]=fieldList[i-1];
					valueList[i]=valueList[i-1];
				}
			}
			// create query with all conditions logically and'ed to gether
			CriteriaQuery query = new CriteriaQuery(classOfObjectToGet, Where.and().add(
					Where.equal(fieldList[0],valueList[0])).add(
							Where.equal(fieldList[1],valueList[1])).add(
									Where.equal(fieldList[2],valueList[2])).add(
											Where.equal(fieldList[3],valueList[3])).add(
													Where.equal(fieldList[4],valueList[4])));
			Objects <T>returnObjects = dataBase.getObjects(query);
			return new ArrayList<T>(returnObjects);
		}
	}
	
	public List<T> getWithCriterion(ICriterion criterion){
		CriteriaQuery query = new CriteriaQuery(classOfObjectToGet,criterion);
		Objects <T>returnObjects = dataBase.getObjects(query);
		return new ArrayList<T>(returnObjects);
	}
	
	public String getDataBaseName(){
		return this.dataBaseName;
	}

	public List<T> getAll(){
		synchronized (odb_Lock) {
			List<T> ret = new ArrayList<T>();
			Objects<T> ts = dataBase.getObjects(this.classOfObjectToGet);
			for (T t : ts) {
				ret.add(t);
			}
			return ret;
		}

	}
	
	public void put(T t){
		synchronized (odb_Lock) {
			dataBase.store(t);
		}
	}

	public void deleteAll(){
		synchronized (odb_Lock) {
			List<T> ts = getAll();
			for (T t : ts) {
				try {
					dataBase.delete(t);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public void deleteObject(T t){
		synchronized (odb_Lock) {
			dataBase.delete(t);
		}
	}
	
	public void saveDataBase(){
		synchronized (odb_Lock) {
//			dataBase.commit();
			close();
			open();
		}
		
	}
	public void open(){
		synchronized (odb_Lock) {
			// this is not down via server.  Ignore this
//			if(dataBase!=null)return;  // only allow once;
			if(!useClient){
				dataBase = ODBFactory.open(this.dataBaseName);
			}else{
				int port = NEODATIS_PORT;//portManager.get(this.dataBaseName, 10000, TimeUnit.MILLISECONDS);
				try {
					dataBase = ODBFactory.openClient("localhost",port,this.dataBaseName);
				} catch (Exception e) {
					e.printStackTrace();
					int k = 1;
				}
			}
		}

	}
	
	public void close(){
		synchronized (odb_Lock) {
			dataBase.close();
		}
	}
	
	
	


	public boolean isInDatabase(T t){
		synchronized (odb_Lock) {
			try {
				OID oid = dataBase.getObjectId(t);
				return true;
			} catch (Exception e) {
				// ignore b/c object is not there.  
			}
			return false;
		}
	}
	
}
