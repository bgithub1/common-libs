package com.billybyte.commoncollections;

public class TypedMapKey<T> {
    private String name;
     
    public TypedMapKey(String name) {
        this.name = name;
    }
     
    public String name() {
        return name;
    }
}