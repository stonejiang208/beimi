package com.beimi.util;

import java.util.List;

import com.beimi.core.BMDataContext;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.model.GameAccountConfig;
import com.beimi.web.service.repository.jpa.GameAccountConfigRepository;

/**
 * 用于获取缓存配置
 * @author iceworld
 *
 */
public class CacheConfigTools {
	public static GameAccountConfig getGameAccountConfig(String orgi){
		GameAccountConfig config = (GameAccountConfig) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.getGameAccountConfig(orgi), orgi) ;
		if(config == null){
			GameAccountConfigRepository accountRes = BMDataContext.getContext().getBean(GameAccountConfigRepository.class) ;
			List<GameAccountConfig> gameAccountList = accountRes.findByOrgi(orgi) ;
			if(gameAccountList!=null && gameAccountList.size() >0){
				config = gameAccountList.get(0) ;
			}else{
				config = new GameAccountConfig() ;
			}
			CacheHelper.getSystemCacheBean().put(BMDataContext.getGameAccountConfig(orgi), config, orgi);
		}
		return config;
	}
}
