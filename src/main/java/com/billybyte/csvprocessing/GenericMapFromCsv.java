package com.billybyte.csvprocessing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.billybyte.commoncollections.Tuple;
import com.billybyte.commonstaticmethods.Utils;

/**
 * use an abstract method to create Key and Value instances to populate a map
 * 		from a csv file
 * @author bperlman1
 *
 * @param <K> - the class of the key of the map
 * @param <V> - the class of the values of the map
 */
public abstract class GenericMapFromCsv<K,V> extends GenericMapFromMap<K, V>{
	private final List<String[]> csvData;
	private final ConcurrentHashMap<String, Integer> headerNameToColNumMap = new 
			ConcurrentHashMap<String, Integer>();
	private boolean ignoreBadLines= false;
	/**
	 * 
	 * @param csvDataLine - a csvData Line
	 * @return {@code Tuple<K,V>} which holds the key and the instance of V to go
	 * 			into the map
	 */
	protected abstract Tuple<K,V> getKeyAndData(int lineNum);
	
	
	
	@SuppressWarnings("unused")
	private GenericMapFromCsv() {
		super();
		csvData = null;
	}

	protected int getLineCount(){
		return csvData.size();
	}

	protected String[] getLine(int lineNum){
		if(lineNum>csvData.size()-1)return null;
		return csvData.get(lineNum);
	}
	
	/**
	 * 
	 * @param csvFilePath Path to csv file
	 * @param processInConstructor - if true, read in all csv data in constructor.
	 * 									if false, you must call the process() method.
	 */
	public GenericMapFromCsv(String csvFilePath,boolean processInConstructor) {
		this(new HashMap<K, V>(),csvFilePath,processInConstructor);
	}
	
	/**
	 * 
	 * @param ignoreBadLines - keep on going if you hit a bad line
	 * @param csvFilePath
	 * @param processInConstructor
	 */
	public GenericMapFromCsv(boolean ignoreBadLines,
			String csvFilePath,boolean processInConstructor) {
		this(new HashMap<K, V>(),csvFilePath,false);
		this.ignoreBadLines = ignoreBadLines;
		if(processInConstructor)process();
	}
	
	/**
	 * 
	 * @param classInSamePackageAsCsv A class that is located in the same package as
	 *     the csvFile that you are using.  It will be read as a resource.
	 * @param csvFileNameInPackage  - just the file name, without any path info
	 * @param processInConstructor - if true, read in all csv data in constructor.
	 * 									if false, you must call the process() method.
	 */
	public GenericMapFromCsv(Class<?> classInSamePackageAsCsv, String csvFileNameInPackage,boolean processInConstructor) {
		this(new HashMap<K, V>(),Utils.getCSVData(classInSamePackageAsCsv, csvFileNameInPackage),processInConstructor);
	}
	
	
	/**
	 * 
	 * @param mapToInstantiate - map (e.g. {@code HashMap<K,V>} , {@code ConcurrentHashMap<K,V>}
	 * 		that you want to use has your map.  If null, a HashMap<K,V> will be used
	 * @param csvFilePath - path of csvFile with data and keys.
	 */
	public GenericMapFromCsv(Map<K, V> mapToInstantiate,String csvFilePath,boolean processInConstructor) {
		super();
		if(mapToInstantiate!=null){
			setMap(mapToInstantiate);
		}
		if(csvFilePath==null || csvFilePath.compareTo("  ")<=0){
			throw Utils.IllArg(this.getClass(), " bad csvFilePath: " + csvFilePath);
		}
		this.csvData = Utils.getCSVData(csvFilePath);
		if(this.csvData.size()<1){
			throw Utils.IllState(this.getClass()," no data in csvFile: "+csvFilePath);
		}
		if(processInConstructor){
			process();
		}
	}

	/**
	 * 
	 * @param mapToInstantiate - map that will be populated
	 * @param csvData  List<String[]> of csv data
	 * @param processInConstructor - if true, read in all csv data in constructor.
	 * 									if false, you must call the process() method.
	 */
	public GenericMapFromCsv(Map<K, V> mapToInstantiate,List<String[]> csvData,boolean processInConstructor) {
		super();
		if(mapToInstantiate!=null){
			setMap(mapToInstantiate);
		}
		this.csvData = csvData;
		if(this.csvData.size()<1){
			throw Utils.IllState(this.getClass()," no data in csvFile: ");
		}
		if(processInConstructor){
			process();
		}
	}

	/**
	 * 
	 * @param csvData  List<String[]> of csv data
	 * @param processInConstructor - if true, read in all csv data in constructor.
	 * 									if false, you must call the process() method.
	 */
	public GenericMapFromCsv(List<String[]> csvData,boolean processInConstructor) {
		this(new HashMap<K,V>(),csvData,processInConstructor);
	}
	
	public void process(){
		processHeader();
		processCsvLines();
	}
	
	/**
	 * populate the headerNameToColNumMap map that maps column Names to column numbers
	 */
	protected void processHeader(){
		String[] header = csvData.get(0);
		for(int i = 0;i<header.length;i++){
			String colName = header[i];
			this.headerNameToColNumMap.put(colName, i);
		}
	}
	
	/**
	 * 
	 * @param colName name of a column
	 * @param rowNum - row of line data
	 * @return - String value of data at that row and column
	 */
	protected String getColumnValue(String colName,int rowNum){
		int col = this.headerNameToColNumMap.get(colName);
		return this.csvData.get(rowNum)[col];
	}
	
	protected void processCsvLines(){
		for(int i = 1;i<csvData.size();i++){
			Tuple<K, V> lineData = getKeyAndData(i);
			if(lineData==null && ignoreBadLines)continue;
			K key = lineData.getT1_instance();
			
			V value = lineData.getT2_instance();
			getMap().put(key, value);
		}
	}
}
