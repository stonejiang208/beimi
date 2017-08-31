package com.beimi.core.engine.game.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.alibaba.fastjson.JSON;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.corundumstudio.socketio.SocketIOServer;

public class CreateAITask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	
	public CreateAITask(long timer , GameRoom gameRoom, String orgi){
		super();
		this.timer = timer ;
		this.gameRoom = gameRoom ;
		this.orgi = orgi ;
	}
	@Override
	public long getCacheExpiryTime() {
		return System.currentTimeMillis()+timer*1000;	//5秒后执行
	}
	
	public void execute(){
		//执行生成AI
		GameUtils.removeGameRoom(gameRoom.getId(), orgi);
		Collection<Object> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
		List<Object> playUserClientList = new ArrayList<Object>();
		playUserClientList.addAll(playerList) ;
		int aicount = gameRoom.getPlayers() - playerList.size() ;
		if(aicount>0){
			for(int i=0 ; i<aicount ; i++){
				PlayUserClient playerUser = GameUtils.create(new PlayUser() , BMDataContext.PlayerTypeEnum.AI.toString()) ;
				playerUser.setPlayerindex(playerList.size()+i);
				CacheHelper.getGamePlayerCacheBean().put(gameRoom.getId(), playerUser, orgi); //将用户加入到 room ， MultiCache
				BMDataContext.getContext().getBean(SocketIOServer.class).getRoomOperations(gameRoom.getId()).sendEvent("joinroom",JSON.toJSONString(playerUser));
				playUserClientList.add(playerUser) ;
			}
			/**
			 * 发送一个 Enough 事件
			 */
			game.change(gameRoom , BeiMiGameEvent.ENOUGH.toString());	//通知状态机 , 此处应由状态机处理异步执行
		}
	}
}
