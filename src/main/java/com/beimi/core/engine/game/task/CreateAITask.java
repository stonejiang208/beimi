package com.beimi.core.engine.game.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.alibaba.fastjson.JSON;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.impl.UserBoard;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.util.rules.model.Board;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.corundumstudio.socketio.SocketIOServer;

public class CreateAITask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private SocketIOServer server ;
	private String orgi ;
	
	public CreateAITask(long timer , GameRoom gameRoom , SocketIOServer server , String orgi){
		this.timer = timer ;
		this.gameRoom = gameRoom ;
		this.server = server ;
		this.orgi = orgi ;
	}
	@Override
	public long getCacheExpiryTime() {
		return System.currentTimeMillis()+timer*1000;	//5秒后执行
	}
	
	public void execute(){
		//执行生成AI
		GameUtils.removeGameRoom(this.gameRoom, orgi);
		Collection<Object> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
		List<Object> playUserClientList = new ArrayList<Object>();
		playUserClientList.addAll(playerList) ;
		int aicount = gameRoom.getPlayers() - playerList.size() ;
		if(aicount>0){
			for(int i=0 ; i<aicount ; i++){
				PlayUserClient playerUser = GameUtils.create(new PlayUser() , BMDataContext.PlayerTypeEnum.AI.toString()) ;
				playerUser.setPlayerindex(playerList.size()+i);
				CacheHelper.getGamePlayerCacheBean().put(gameRoom.getId(), playerUser, orgi); //将用户加入到 room ， MultiCache
				server.getRoomOperations(gameRoom.getId()).sendEvent("joinroom",JSON.toJSONString(playerUser));
				playUserClientList.add(playerUser) ;
			}
		}
//		client.sendEvent("players", JSON.toJSONString(CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi)));
		Board board = GameUtils.playDizhuGame(playUserClientList, gameRoom, null, gameRoom.getCardsnum()) ;
		CacheHelper.getGameCacheBean().put(gameRoom.getId(), board, orgi);
		for(Object temp : playerList){
			PlayUserClient playerUser = (PlayUserClient) temp ;
			NettyClients.getInstance().sendGameEventMessage(playerUser.getId(), "play", JSON.toJSONString(new UserBoard(board , playerUser.getId())) );
		}
	}
}
