package com.billybyte.commoncollections;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

 
public class TypedMap  {
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
    


}