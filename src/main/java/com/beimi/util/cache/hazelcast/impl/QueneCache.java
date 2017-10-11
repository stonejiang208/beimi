package com.beimi.util.cache.hazelcast.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.core.HazelcastInstance;

/**
 * 主要用于游戏的撮合部分，游戏的玩法配置是系统级别个参数配置，
 * 代理和分销账号下的 只包含游戏玩家的业务数据，不包括系统级别的配置，无租户相关问题
 * @author iceworld
 *
 */
@Service("quene_cache")
public class QueneCache{
	
	@Autowired
	public HazelcastInstance hazelcastInstance;	
	
	public HazelcastInstance getInstance(){
		return hazelcastInstance ;
	}
	public QueneCache getCacheInstance(String cacheName){
		return this ;
	}
	
	public void offer(String playway,Object value, String orgi){
		getInstance().getQueue(playway).offer(value) ;
	}

	public Object poll(String playway,String orgi) {
		//not support 
		return getInstance().getQueue(playway).poll() ;
	}
}
