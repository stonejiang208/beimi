package com.beimi.util.server.handler;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.task.CreateAITask;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.Token;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;

public class GameEventHandler     
{  
	protected SocketIOServer server;
	
    @Autowired  
    public GameEventHandler(SocketIOServer server)   
    {  
        this.server = server ;
    }  
    
    @OnConnect  
    public void onConnect(SocketIOClient client)  
    {  
    	try {
			String token = client.getHandshakeData().getSingleUrlParam("token") ;
			String room = client.getHandshakeData().getSingleUrlParam("room") ;	//房卡标识，使用房卡创建 ， 获取 6位数字 房间号
			String orgi = client.getHandshakeData().getSingleUrlParam("orgi") ;	//租户ID
			String playway = client.getHandshakeData().getSingleUrlParam("playway") ;	//租户ID
			/**
			 * Token不为空，并且，验证Token有效，验证完毕即开始进行游戏撮合，房卡类型的
			 * 1、大厅房间处理
			 *    a、从房间队列里获取最近一条房间信息
			 *    b、将token对应玩家加入到房间
			 *    c、如果房间凑齐了玩家，则将房间从等待撮合队列中移除，放置到游戏中的房间信息，如果未凑齐玩家，继续扔到队列
			 *    d、通知房间的所有人，有新玩家加入
			 *    e、超时处理，增加AI进入房价
			 *    f、事件驱动
			 *    g、定时器处理
			 * 2、房卡房间处理
			 * 	  a、创建房间
			 * 	  b、加入到等待中队列
			 */
			Token userToken ;
			if(!StringUtils.isBlank(token) && (userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, orgi))!=null){
				//鉴权完毕
				PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userToken.getUserid(), userToken.getOrgi()) ;
				NettyClients.getInstance().putGameEventClient(userClient.getId(), client);
				GameRoom gameRoom = BMDataContext.getGameEngine().gameRequest(userToken.getUserid(), playway, room, orgi , userClient) ;
				if(gameRoom != null){
					client.joinRoom(gameRoom.getId());
					server.getRoomOperations(gameRoom.getId()).sendEvent("joinroom",JSON.toJSONString(userClient));
					client.sendEvent("players", JSON.toJSONString(CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi)));
					
					if(CacheHelper.getExpireCache().get(gameRoom.getId())==null){
						Collection<Object> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
						if(gameRoom.getPlayers() == playerList.size()){
							//结束撮合，可以开始玩游戏了
							CacheHelper.getExpireCache().put(gameRoom.getId(), new CreateAITask(0 , gameRoom , server , gameRoom.getOrgi()));
						}else{
							//等5秒，然后进AI ， 每次有玩家加入，先判断当前状态，是否是玩家已凑齐或已开始游戏，否则，计时重新开始
							CacheHelper.getExpireCache().put(gameRoom.getId(), new CreateAITask(5 , gameRoom , server , gameRoom.getOrgi()));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }  
      
    //添加@OnDisconnect事件，客户端断开连接时调用，刷新客户端信息  
    @OnDisconnect  
    public void onDisconnect(SocketIOClient client)  
    {  
    	String token = client.getHandshakeData().getSingleUrlParam("token") ;
		if(!StringUtils.isBlank(token)){
			Token userToken = (Token) CacheHelper.getApiUserCacheBean().getCacheObject(token, BMDataContext.SYSTEM_ORGI) ;
			if(userToken!=null){
				BMDataContext.getGameEngine().leaveRoom(userToken.getUserid(), userToken.getOrgi());
			}
			NettyClients.getInstance().removeGameEventClient(token , client.getSessionId().toString());
		}
    }  
}  