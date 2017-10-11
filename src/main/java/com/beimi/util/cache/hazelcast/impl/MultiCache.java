package com.beimi.util.cache.hazelcast.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beimi.web.model.PlayUserClient;
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
	
	public void put(String key, PlayUserClient value, String orgi) {
		getInstance().getMultiMap(getName()).put(key, value) ;
	}

	public void clear(String orgi) {
		getInstance().getMultiMap(getName()).clear();
	}

	public Object delete(String key, PlayUserClient value) {
		return getInstance().getMultiMap(getName()).remove(key , value) ;
	}
	
	public Object delete(String key) {
		return getInstance().getMultiMap(getName()).remove(key) ;
	}

	public void update(String key, String orgi, PlayUserClient value) {
		getInstance().getMultiMap(getName()).put(key, value);
	}

	public List<PlayUserClient> getCacheObject(String key, String orgi) {
		List<PlayUserClient> values = new ArrayList<PlayUserClient>();
		Collection<Object> dataList = getInstance().getMultiMap(getName()).get(key) ; 
		for(Object data : dataList){
			values.add((PlayUserClient) data) ;
		}
		Collections.sort(values, new Comparator<PlayUserClient>(){

			@Override
			public int compare(PlayUserClient o1, PlayUserClient o2) {
				return o1.getPlayerindex() > o2.getPlayerindex() ? 1 : -1;
			}
			
		});
		return values;
	}

	public String getName() {
		return cacheName ;
	}
}
