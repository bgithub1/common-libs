package com.billybyte.neodatis;

import java.util.ArrayList;
import java.util.List;


public abstract class ObjectListToDatabase<S,T> {
	public abstract T buildInstance(S inputInstance);
	public abstract boolean isValidLine(int inputListLineNum);
	
	private Object process_Lock = new Object();
	
	private List<S> inputData;
	private List<T> instancesBuilt= new ArrayList<T>();
	private DatabaseAccessEngine<T> databaseQueryEngine;
	
	
	public ObjectListToDatabase(List<S> inputData,
			DatabaseAccessEngine<T> databaseQueryEngine) {
		super();
		this.inputData = inputData;
		this.databaseQueryEngine = databaseQueryEngine;
	}


	public int processData(){
		synchronized (process_Lock) {
			int count = 0;
			for (int i = 1; i < inputData.size(); i++) {
				if (!isValidLine(i))
					continue;
				T tInstance = buildInstance(inputData.get(i));

				if (tInstance != null) {
					databaseQueryEngine.putData(tInstance);
					instancesBuilt.add(tInstance);
					count = count + 1;
				}
			}
			databaseQueryEngine.saveDatabase();
			return count;
		}
	}

	public List<T> getInstancesBuilt(){
		synchronized (process_Lock) {
			return instancesBuilt;
		}
	}

	public List<S> getInputData() {
		return inputData;
	}


	public DatabaseAccessEngine<T> getDatabaseQueryEngine() {
		return databaseQueryEngine;
	}
	
	
		

}
