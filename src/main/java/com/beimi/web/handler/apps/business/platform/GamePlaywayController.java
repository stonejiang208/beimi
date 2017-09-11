package com.beimi.web.handler.apps.business.platform;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.beimi.core.BMDataContext;
import com.beimi.util.GameUtils;
import com.beimi.util.Menu;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.handler.Handler;
import com.beimi.web.model.BeiMiDic;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.service.repository.jpa.GameModelRepository;
import com.beimi.web.service.repository.jpa.GamePlaywayRepository;

@Controller
@RequestMapping("/apps/platform")
public class GamePlaywayController extends Handler{
	
	@Autowired
	private GameModelRepository gameModelRes ;
	
	@Autowired
	private GamePlaywayRepository playwayRes ;
	
	@RequestMapping({"/playway"})
	@Menu(type="platform", subtype="playway")
	public ModelAndView playway(ModelMap map , HttpServletRequest request , @Valid String id){
		map.addAttribute("game", BeiMiDic.getInstance().getDicItem(id)) ;
		map.addAttribute("gameModelList", BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC, id)) ;
		
		map.addAttribute("playwayList", playwayRes.findByOrgi(super.getOrgi(request) , new Sort(Sort.Direction.ASC, "sortindex"))) ;
		
		return request(super.createAppsTempletResponse("/apps/business/platform/game/playway/index"));
	}
	
	@RequestMapping({"/playway/add"})
	@Menu(type="platform", subtype="playway")
	public ModelAndView add(ModelMap map , HttpServletRequest request , @Valid String id){
		
		map.addAttribute("game", BeiMiDic.getInstance().getDicItem(id)) ;
		map.addAttribute("gameModelList", BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC, id)) ;
		map.addAttribute("sceneList", BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_SCENE_DIC)) ;
		
		return request(super.createRequestPageTempletResponse("/apps/business/platform/game/playway/add"));
	}
	
	@RequestMapping("/playway/save")
    @Menu(type = "admin" , subtype = "user")
    public ModelAndView save(HttpServletRequest request ,@Valid GamePlayway playway) {
		playway.setOrgi(super.getOrgi(request));
		playway.setCreater(super.getUser(request).getId());
		playway.setCreatetime(new Date());
		playway.setUpdatetime(new Date());
		playwayRes.save(playway) ;
		
		/**
		 * 清除缓存
		 */
		if(!StringUtils.isBlank(playway.getTypeid())){
			GameUtils.cleanPlaywayCache(playway.getTypeid(), super.getOrgi(request));
		}
		
		CacheHelper.getSystemCacheBean().put(playway.getId(), playway, playway.getOrgi());
    	return request(super.createRequestPageTempletResponse("redirect:/apps/platform/playway.html?id="+playway.getGame()));
    }
	
	
	@RequestMapping({"/playway/edit"})
	@Menu(type="platform", subtype="playway")
	public ModelAndView edit(ModelMap map , HttpServletRequest request , @Valid String id , @Valid String game){
		
		map.addAttribute("playway", playwayRes.findByIdAndOrgi(id, super.getOrgi(request))) ;
		
		map.addAttribute("game", BeiMiDic.getInstance().getDicItem(game)) ;
		map.addAttribute("gameModelList", BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC, game)) ;
		
		map.addAttribute("sceneList", BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_SCENE_DIC)) ;
		
		return request(super.createRequestPageTempletResponse("/apps/business/platform/game/playway/edit"));
	}
	
	@RequestMapping("/playway/update")
    @Menu(type = "admin" , subtype = "user")
    public ModelAndView update(HttpServletRequest request ,@Valid GamePlayway playway) {
		GamePlayway tempPlayway = playwayRes.findByIdAndOrgi(playway.getId(), super.getOrgi(request)) ;
		if(tempPlayway!=null){
			playway.setOrgi(super.getOrgi(request));
			playway.setCreater(tempPlayway.getCreater());
			playway.setCreatetime(tempPlayway.getCreatetime());
			playway.setUpdatetime(new Date());
			playwayRes.save(playway) ;
			/**
			 * 清除修改之前的缓存
			 */
			GameUtils.cleanPlaywayCache(tempPlayway.getTypeid(), super.getOrgi(request));
			
			/**
			 * 清除修改之后的缓存
			 */
			GameUtils.cleanPlaywayCache(playway.getTypeid(), super.getOrgi(request));
			
			
			CacheHelper.getSystemCacheBean().put(playway.getId(), playway, playway.getOrgi());
		}
    	return request(super.createRequestPageTempletResponse("redirect:/apps/platform/playway.html?id="+playway.getGame()));
    }
	

	@RequestMapping({"/playway/delete"})
	@Menu(type="platform", subtype="playway")
	public ModelAndView delete(ModelMap map , HttpServletRequest request , @Valid String id , @Valid String game){
		if(!StringUtils.isBlank(id)){
			GamePlayway tempPlayway = playwayRes.findByIdAndOrgi(id, super.getOrgi(request)) ;
			if(tempPlayway!=null){
				playwayRes.delete(tempPlayway);
			}
			/**
			 * 清除缓存
			 */
			GameUtils.cleanPlaywayCache(tempPlayway.getTypeid(), super.getOrgi(request));
		}
		return request(super.createRequestPageTempletResponse("redirect:/apps/platform/playway.html?id="+game));
	}
	
}
