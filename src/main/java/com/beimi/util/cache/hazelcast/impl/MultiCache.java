package com.beimi.util.cache.hazelcast.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

@Service("multi_cache")
public class MultiCache{
	
	@Autowired
	public HazelcastInstance hazelcastInstance;	
	
	private String cacheName ; 
	
	public HazelcastInstance getInstance(){
		return hazelcastInstance ;
	}
	public MultiCache getCacheInstance(String cacheName){
		this.cacheName = cacheName ;
		return this ;
	}
	
	public void put(String key, Object value, String orgi) {
		
		getInstance().getMultiMap(getName()).put(key, value) ;
	}

	public void clear(String orgi) {
		getInstance().getMultiMap(getName()).clear();
	}

	public Object delete(String key, String value) {
		return getInstance().getMultiMap(getName()).remove(key , value) ;
	}
	
	public Object delete(String key) {
		return getInstance().getMultiMap(getName()).remove(key) ;
	}


	public void update(String key, String orgi, Object value) {
		getInstance().getMultiMap(getName()).put(key, value);
	}

	public Collection<Object> getCacheObject(String key, String orgi) {
		return getInstance().getMultiMap(getName()).get(key);
	}

	public String getName() {
		return cacheName ;
	}
}
