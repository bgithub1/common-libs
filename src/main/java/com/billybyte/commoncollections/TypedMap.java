package com.billybyte.commoncollections;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.billybyte.commoninterfaces.QueryInterface;
import com.billybyte.commonstaticmethods.CollectionsStaticMethods;
import com.billybyte.commonstaticmethods.Utils;

 
public class TypedMap /*implements Map<String, Object>*/ {
    private Map<String, Object> delegate;
     
    public TypedMap( Map<String, Object> delegate ) {
        this.delegate = delegate;
    }
 
    public TypedMap() {
        this.delegate = new HashMap<String, Object>();
    }
     
    @SuppressWarnings( "unchecked" )
    public <T> T get( TypedMapKey<T> key ) {
        return (T) delegate.get( key.name() );
    }
         
    @SuppressWarnings( "unchecked" )
    public <T> T remove( TypedMapKey<T> key ) {
        return (T) delegate.remove( key.name() );
    }
     
    public <T> void put( TypedMapKey<T> key, T value ) {
        delegate.put( key.name(), value );
    }

    public <T> boolean containsKey( TypedMapKey<T> key ) {
        return delegate.containsKey( key.name() );
    }


    // --- Only calls to delegates below
     
    public void clear() {
        delegate.clear();
    }
 
    public boolean containsKey( Object key ) {
        return delegate.containsKey( key );
    }
 
    public boolean containsValue( Object value ) {
        return delegate.containsValue( value );
    }
 
    @SuppressWarnings("unchecked")
	public <T> Set<java.util.Map.Entry<TypedMapKey<T>,T>> entrySet() {
    	Set<java.util.Map.Entry<String, Object>> innerEntrySet = 
    			delegate.entrySet();
    	Map<TypedMapKey<T>,T> tMap = new HashMap<TypedMapKey<T>, T>();
    	for(Entry<String, Object> entry:innerEntrySet){
    		tMap.put(new TypedMapKey<T>(entry.getKey()), (T)entry.getValue());
    	}
        return tMap.entrySet();
    }
 
    public boolean equals( Object o ) {
        return delegate.equals( o );
    }
 
    public Object get( Object key ) {
        return delegate.get( key );
    }
 
    public int hashCode() {
        return delegate.hashCode();
    }
 
    public boolean isEmpty() {
        return delegate.isEmpty();
    }
 
    public Set<String> keySet() {
        return delegate.keySet();
    }
 
    public Object put( String key, Object value ) {
        return delegate.put( key, value );
    }
 
    public void putAll( Map<? extends String, ? extends Object> m ) {
        delegate.putAll( m );
    }
 
    public Object remove( Object key ) {
        return delegate.remove( key );
    }
 
    public int size() {
        return delegate.size();
    }
 
    public Collection<Object> values() {
        return delegate.values();
    }
    
    public TypedMap cloneMap(TypedMap tm){
    	TypedMap ret = new TypedMap();
    	ret.putAll(tm.delegate);
    	return ret;
    }
    
    // usage to see if this compiles.  The queries return null
    
//    public static void main(String[] args) {
//		// for a complex query
//    	Set<String> names = CollectionsStaticMethods.setFromArray(new String[]{"IBM","GE"});// define some stock names
//    	Map<String, TypedMap> modelInputs = new HashMap<String, TypedMap>(); // create a map of stock names vs TypedMap for the inputs for each stock
//    	TypedMap typedMapForQueries = new TypedMap(); // use this to hold the queries
//    	AtmBdKey atmBdKey = 
//    			new AtmBdKey();// create key to get AtmBd Queries
//    	AtmDbKey atmDbKey = 
//    			new AtmDbKey();// create a key to get AtmDouble Queries
//    	Set<TypedMapKey<?>> queryKeys =  CollectionsStaticMethods.setFromArray(new TypedMapKey<?>[]{atmBdKey,atmDbKey});// create a set of the query keys 
//
//    	List<BigDecimal> bdList = CollectionsStaticMethods.listFromArray(new BigDecimal[]{BigDecimal.ONE,BigDecimal.ZERO,BigDecimal.TEN}); // create some return data
//    	typedMapForQueries.put(atmBdKey,new DseInputQuery<BigDecimal>(bdList)); // create a BigDecimal query and save it in a TypedMap (Het Map)
//
//    	@SuppressWarnings("unused")
//		DseInputQuery<BigDecimal> atmq = typedMapForQueries.get(atmBdKey);// see if you can actually get the query
//    	
//    	List<Double> dbList = CollectionsStaticMethods.listFromArray(new Double[]{1.0,0.0,10.0});// create some Double values
//    	typedMapForQueries.put(atmDbKey,new DseInputQuery<Double>(dbList));// create a query and store it for double values
//    	@SuppressWarnings("unused")
//		DseInputQuery<Double> doubleQuery = typedMapForQueries.get(atmDbKey);// see if you can get the query
//    	
//    	// now see if you can get all the queries by using the key list above
//    	// in this loop you will populate the modelInputs map, which, for each stock, has all of the inputs that the stock's derivative model would
//    	//   need to compute values (there is no model or anything to compute here, but the idea still holds)
//    	for(TypedMapKey inputTypeKey:queryKeys){
//    		DseInputQuery query = typedMapForQueries.get(inputTypeKey);// get the query
//    		Utils.prt(query.toString());
//    		Map<String,List<?>> m = query.get(names,1,TimeUnit.SECONDS);// use the query to get some data
//    		Utils.prt(m.size());
//    		// take the query output, and store it the "inputs" map, whose keys are stock names, and whose values are TypedMaps that hold the stuff we got from the queries
//    		for(String name:names){
//        		if(!modelInputs.containsKey(name)){
//        			TypedMap inputsTm = new TypedMap();// if this name was not in the inputs map, create a new entry
//        			modelInputs.put(name, inputsTm);// put a fresh TypedMap in the entry
//        		}
//        		TypedMap inputsTm = modelInputs.get(name);// get the TypedMap for this stock
//        		inputsTm.put(new ModelInputKey(inputTypeKey.name()), m.get(name));// put the data in map m (from the query) into the TypedMap of for this stock
//    		}
//    	}
//    	
//    	// now use the inputs map
//    	ModelInputKey<BigDecimal> bigDecimalModelInputKey = new ModelInputKey<BigDecimal>(atmBdKey.name());// create a key to get BigDecimal lists from the modelInputs map
//		List<BigDecimal> bdData = modelInputs.get("IBM").get(bigDecimalModelInputKey);// get bigdecimal data for IBM
//		Utils.prt("IBM BD data");
//		CollectionsStaticMethods.prtListItems(bdData);
//		ModelInputKey<Double> doubleModelInputKey = new ModelInputKey<Double>(atmDbKey.name());// create the same key for double data that we got from a query
//		List<Double> dbData = modelInputs.get("IBM").get(doubleModelInputKey);// get Double data for IBM
//		Utils.prt("IBM Double data");
//		CollectionsStaticMethods.prtListItems(dbData);
//
//		// do every thing that you did above, but for GE
//		bdData = modelInputs.get("GE").get(new ModelInputKey<BigDecimal>(atmBdKey.name()));
//		Utils.prt("GE BD data");
//		CollectionsStaticMethods.prtListItems(bdData);
//		dbData = modelInputs.get("GE").get(new ModelInputKey<Double>(atmDbKey.name()));
//		Utils.prt("GE Double data");
//		CollectionsStaticMethods.prtListItems(dbData);
//}
//    
//    private static final class AtmBdKey extends TypedMapKey<DseInputQuery<BigDecimal>>{
//		private static final String key = "ATM_BD_KEY";
//    	public AtmBdKey() {
//			super(key);
//		}
//
//		@Override
//		public String toString() {
//			return key;
//		}
//		
//    }
//    
//    private static final class AtmDbKey extends TypedMapKey<DseInputQuery<Double>>{
//		private static final String key = "ATM_DB_KEY";
//		public AtmDbKey() {
//			super(key);
//		}
//		@Override
//		public String toString() {
//			return key;
//		}
//    }
//    
//    private static final class ModelInputKey<T> extends TypedMapKey<List<T>>{
//
//		public ModelInputKey(String name) {
//			super(name);
//		}
//    	
//    }
//
//    
//    private static final class DseInputQuery<T> implements QueryInterface<Set<String>, Map<String, List<T>>>{
//    	private final List<T> retVals;
//    	DseInputQuery(List<T> retVals){
//    		this.retVals = retVals;
//    	}
//    	
//		@Override
//		public Map<String, List<T>> get(Set<String> keys,
//				int timeoutValue, TimeUnit timeUnitType) {
//			Map<String,List<T>> ret = new HashMap<String, List<T>>();
//			for(String key:keys){
//				ret.put(key,retVals);
//			}
//			return ret;
//		}
//    }
//    


}