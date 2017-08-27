package com.beimi.util.cache.hazelcast.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

@Service("quene_cache")
public class QueneCache{
	
	@Autowired
	public HazelcastInstance hazelcastInstance;	
	
	private String cacheName ; 
	
	public HazelcastInstance getInstance(){
		return hazelcastInstance ;
	}
	public QueneCache getCacheInstance(String cacheName){
		this.cacheName = cacheName ;
		return this ;
	}
	
	public void offer(Object value, String orgi){
		getInstance().getQueue(getName(orgi)).offer(value) ;
	}

	public Object poll(String orgi) {
		//not support 
		return getInstance().getQueue(getName(orgi)).poll() ;
	}

	public String getName(String orgi) {
		return cacheName+"."+orgi ;
	}
}
