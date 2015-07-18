package com.billybyte.mongo;


import com.billybyte.commonstaticmethods.Utils;
import com.mongodb.DBObject;

public abstract class MongoBaseAbstract<T extends MongoBaseAbstract<T>>{
	protected abstract T fromMongoDbo(DBObject dbo);
	public abstract DBObject toDBObject();
	protected abstract String getDatabaseName();
	protected abstract String getCollName();
	
	private static final <T extends MongoBaseAbstract<T>> T getT(Class<T> clazz){
		try {
			T tempObj = 
					clazz.getConstructor().newInstance();
			return tempObj;
		} catch (Exception e) {
			throw Utils.IllState(e);
		}	
			
	}
	
	public static <T extends MongoBaseAbstract<T>> T fromDbo(Class<T> clazz,DBObject dbo){
		try {
			T tempObj = MongoBaseAbstract.getT(clazz);
			return tempObj.fromMongoDbo(dbo);
		} catch (Exception e) {
			throw Utils.IllState(e);
		}	
	}
	
	public static <T extends MongoBaseAbstract<T>> String getDbName(Class<T> clazz){
		T tempObj = MongoBaseAbstract.getT(clazz);
		return tempObj.getDatabaseName();
	}
	
	public static <T extends MongoBaseAbstract<T>> String getCollectionName(Class<T> clazz){
		T tempObj = MongoBaseAbstract.getT(clazz);
		return tempObj.getCollName();
	}
	
	public static class Example extends MongoBaseAbstract<Example>{
		private final Integer myInt;
		public Example(){
			super();
			this.myInt = 20;
		}
		
		Example(Integer myInt){
			this.myInt = myInt;
		}
		int get(){
			return myInt;
		}


		@Override
		public DBObject toDBObject() {
			
			return  null;
		}

		@Override
		protected Example fromMongoDbo(DBObject dbo) {
			return new Example(10);
		}

		@Override
		protected String getDatabaseName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		protected String getCollName() {
			// TODO Auto-generated method stub
			return null;
		}
		
		
	}
	
	public static void main(String[] args) {
		Example ex = MongoBaseAbstract.fromDbo(Example.class,null);
		
		Utils.prt(ex.get());
	}
}
