package com.beimi.web.handler.api.rest.user;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.beimi.core.BMDataContext;
import com.beimi.util.CacheConfigTools;
import com.beimi.util.GameUtils;
import com.beimi.util.IP;
import com.beimi.util.IPTools;
import com.beimi.util.MessageEnum;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.handler.Handler;
import com.beimi.web.model.GameAccountConfig;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.ResultData;
import com.beimi.web.model.Token;
import com.beimi.web.service.repository.es.PlayUserClientESRepository;
import com.beimi.web.service.repository.es.PlayUserESRepository;
import com.beimi.web.service.repository.es.TokenESRepository;
import com.beimi.web.service.repository.jpa.PlayUserRepository;

@RestController
@RequestMapping("/api/guest")
public class GuestRegisterController extends Handler{

	@Autowired
	private PlayUserESRepository playUserESRes;
	
	@Autowired
	private PlayUserClientESRepository playUserClientRes ;
	
	@Autowired
	private PlayUserRepository playUserRes ;
	
	@Autowired
	private TokenESRepository tokenESRes ;

	@RequestMapping
    public ResponseEntity<ResultData> guest(HttpServletRequest request , @Valid String token) {
		PlayUserClient playUserClient = null ;
		Token userToken = null ;
		if(!StringUtils.isBlank(token)){
			userToken = tokenESRes.findById(token) ;
			if(userToken != null && !StringUtils.isBlank(userToken.getUserid()) && userToken.getExptime()!=null && userToken.getExptime().after(new Date())){
				//返回token， 并返回游客数据给游客
				playUserClient = playUserClientRes.findById(userToken.getUserid()) ;
				if(playUserClient!=null){
					playUserClient.setToken(userToken.getId());
				}
			}else{
				if(userToken!=null){
					tokenESRes.delete(userToken);
					userToken = null ;
				}
			}
		}
		if(playUserClient == null){
			try {
				String ip = UKTools.getIpAddr(request);
				IP ipdata = IPTools.getInstance().findGeography(ip);
				playUserClient = register(new PlayUser() , ipdata , request) ;
			} catch (IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
		if(userToken == null){
			userToken = new Token();
			userToken.setId(UKTools.getUUID());
			userToken.setUserid(playUserClient.getId());
			userToken.setCreatetime(new Date());
			userToken.setOrgi(playUserClient.getOrgi());
			GameAccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI) ;
    		if(config!=null && config.getExpdays() > 0){
    			userToken.setExptime(new Date(System.currentTimeMillis()+60*60*24*config.getExpdays()*1000));//默认有效期 ， 7天
    		}else{
    			userToken.setExptime(new Date(System.currentTimeMillis()+60*60*24*7*1000));//默认有效期 ， 7天
    		}
			userToken.setLastlogintime(new Date());
			userToken.setUpdatetime(new Date(0));
			
			tokenESRes.save(userToken) ;
		}
		playUserClient.setToken(userToken.getId());
		CacheHelper.getApiUserCacheBean().put(userToken.getId(),userToken, userToken.getOrgi());
		CacheHelper.getApiUserCacheBean().put(playUserClient.getId(),playUserClient, userToken.getOrgi());
        return new ResponseEntity<>(new ResultData( playUserClient!=null , playUserClient != null ? MessageEnum.USER_REGISTER_SUCCESS: MessageEnum.USER_REGISTER_FAILD_USERNAME , playUserClient , userToken), HttpStatus.OK);
    }
	/**
	 * 注册用户
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public PlayUserClient register(PlayUser player , IP ipdata , HttpServletRequest request ) throws IllegalAccessException, InvocationTargetException{
		PlayUserClient playUserClient = GameUtils.create(player, ipdata, request) ;
		int users = playUserESRes.countByUsername(player.getUsername()) ;
		if(users == 0){
			UKTools.published(player , playUserESRes , playUserRes);
		}
		return playUserClient ;
	}
	
}