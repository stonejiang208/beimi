package com.beimi.core.engine.game;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.state.GameEvent;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.service.repository.jpa.GameRoomRepository;
import com.corundumstudio.socketio.SocketIOServer;

@Service(value="beimiGameEngine")
public class GameEngine {
	
	@Autowired
	protected SocketIOServer server;
	/**
	 * 玩家房间选择，新的请求， 如果当前玩家是断线重连， 或者是 退出后进入的，则第一步检查是否已在房间
	 * 如果已在房间，直接返回
	 * @param userid
	 * @param room
	 * @param orgi
	 * @return
	 */
	public GameEvent gameRequest(String userid ,String playway , String room , String orgi , PlayUserClient playUser){
		GameEvent gameEvent = null ;
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userid, orgi) ;
		GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(playway, orgi) ;
		if(gamePlayway!=null){
			gameEvent = new GameEvent(gamePlayway.getPlayers() , gamePlayway.getCardsnum() , orgi) ;
			GameRoom gameRoom = null ;
			if(!StringUtils.isBlank(roomid) && CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi)!=null){//
				gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;		//直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
			}else{
				if(!StringUtils.isBlank(room)){	//房卡游戏 , 创建ROOM
					gameRoom = this.creatGameRoom(gamePlayway, userid , true) ;
					CacheHelper.getGameRoomCacheBean().put(gameRoom.getId(), gameRoom, orgi);
				}else{	//大厅游戏 ， 撮合游戏
					gameRoom = (GameRoom) CacheHelper.getQueneCache().poll(orgi) ;
					if(gameRoom==null){	//无房间 ， 需要
						gameRoom = this.creatGameRoom(gamePlayway, userid , false) ;
						CacheHelper.getGameRoomCacheBean().put(gameRoom.getId(), gameRoom, orgi);
					}else{
					
						/**
						 * 如果当前房间到达了最大玩家数量，则不再加入到 撮合队列
						 */
						List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
						if((playerList.size() + 1) < gamePlayway.getPlayers() && CacheHelper.getExpireCache().get(gameRoom.getId()) != null){
							CacheHelper.getQueneCache().offer(gameRoom, orgi);	//未达到最大玩家数量，加入到游戏撮合 队列，继续撮合
						}
						playUser.setPlayerindex(gameRoom.getPlayers() - playerList.size() - 1);//从后往前坐，房主进入以后优先坐在 首位
					}
				}
			}
			if(gameRoom!=null){
				
				/**
				 * 如果当前房间到达了最大玩家数量，则不再加入到 撮合队列
				 */
				List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
				if(playerList.size() == 0){
					gameEvent.setEvent(BeiMiGameEvent.ENTER.toString());
				}else{	
					gameEvent.setEvent(BeiMiGameEvent.JOIN.toString());
				}
				
				gameEvent.setRoomid(gameRoom.getId());
				NettyClients.getInstance().joinRoom(userid, gameRoom.getId());
				List<PlayUserClient> userList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi) ;
				boolean inroom = false ;
				for(Object user : userList){
					PlayUserClient tempPlayUser = (PlayUserClient) user ;
					if(tempPlayUser.getId().equals(userid)){
						inroom = true ; break ;
					}
				}
				if(inroom == false){
					playUser.setPlayerindex(gameRoom.getPlayers() - playerList.size());
					playUser.setPlayertype(BMDataContext.PlayerTypeEnum.NORMAL.toString());
					CacheHelper.getGamePlayerCacheBean().put(gameRoom.getId(), playUser, orgi); //将用户加入到 room ， MultiCache
				}
				/**
				 *	不管状态如何，玩家一定会加入到这个房间 
				 */
				CacheHelper.getRoomMappingCacheBean().put(userid, gameRoom.getId(), orgi);
			}
		}
		return gameEvent;
	}
	
	/**
	 * 抢地主，斗地主
	 * @param roomid
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public void actionRequest(String roomid, PlayUserClient playUser, String orgi , boolean accept){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			Player player = board.player(playUser.getId()) ;
			board = ActionTaskUtils.doCatch(board, player , accept) ;
			
			ActionTaskUtils.sendEvent("catchresult",UKTools.json(new GameBoard(player.getPlayuser() , player.isAccept(), board.isDocatch() , board.getRatio())) ,gameRoom) ;
			ActionTaskUtils.game().change(gameRoom , BeiMiGameEvent.AUTO.toString() , 15);	//通知状态机 , 继续执行
			
			CacheHelper.getBoardCacheBean().put(gameRoom.getId() , board , gameRoom.getOrgi()) ;
			
			CacheHelper.getExpireCache().put(gameRoom.getRoomid(), ActionTaskUtils.createAutoTask(1, gameRoom));
		}
	}
	
	/**
	 * 加入房间，房卡游戏
	 * @param roomid
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public GameRoom joinRoom(String roomid, PlayUserClient playUser, String orgi){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			CacheHelper.getGamePlayerCacheBean().put(gameRoom.getId(), playUser, orgi); //将用户加入到 room ， MultiCache
		}
		return gameRoom ;
	}
	
	/**
	 * 退出房间
	 * 1、房卡模式，userid是房主，则解散房间
	 * 2、大厅模式，如果游戏未开始并且房间仅有一人，则解散房间
	 * @param roomid
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public GameRoom leaveRoom(PlayUserClient playUser , String orgi){
		GameRoom gameRoom = whichRoom(playUser.getId(), orgi) ;
		if(gameRoom!=null){
			List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), orgi) ;
			if(gameRoom.isCardroom()){
				CacheHelper.getGameRoomCacheBean().delete(gameRoom.getId(), gameRoom.getOrgi()) ;
				CacheHelper.getGamePlayerCacheBean().delete(gameRoom.getId()) ;
				UKTools.published(gameRoom , null , BMDataContext.getContext().getBean(GameRoomRepository.class) , BMDataContext.UserDataEventType.DELETE.toString());
			}else{
				if(players.size() <= 1){
					//解散房间 , 保留 ROOM资源 ， 避免 从队列中取出ROOM
					CacheHelper.getGamePlayerCacheBean().delete(gameRoom.getId()) ;
				}else{
					CacheHelper.getGamePlayerCacheBean().delete(gameRoom.getId(), playUser) ;
				}
			}
		}
		return gameRoom;
	}
	/**
	 * 当前用户所在的房间
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public GameRoom whichRoom(String userid, String orgi){
		GameRoom gameRoom = null ;
		String roomid = (String) CacheHelper.getRoomMappingCacheBean().getCacheObject(userid, orgi) ;
		if(!StringUtils.isBlank(roomid)){//
			gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;		//直接加入到 系统缓存 （只有一个地方对GameRoom进行二次写入，避免分布式锁）
		}
		return gameRoom;
	}
	/**
	 * 创建新房间 ，需要传入房间的玩法 ， 玩法定义在 系统运营后台，玩法创建后，放入系统缓存 ， 客户端进入房间的时候，传入 玩法ID参数
	 * @param playway
	 * @param userid
	 * @return
	 */
	private  GameRoom creatGameRoom(GamePlayway playway , String userid , boolean cardroom){
		GameRoom gameRoom = new GameRoom() ;
		gameRoom.setCreatetime(new Date());
		gameRoom.setRoomid(UKTools.getUUID());
		gameRoom.setUpdatetime(new Date());
		
		if(playway!=null){
			gameRoom.setPlayway(playway.getId());
			gameRoom.setRoomtype(playway.getRoomtype());
			gameRoom.setPlayers(playway.getPlayers());
		}
		

		gameRoom.setPlayers(playway.getPlayers());
		gameRoom.setCardsnum(playway.getCardsnum());
		
		gameRoom.setCurpalyers(1);
		gameRoom.setCardroom(cardroom);
		
		gameRoom.setStatus(BeiMiGameEnum.CRERATED.toString());
		
		gameRoom.setCardsnum(playway.getCardsnum());
		
		gameRoom.setCurrentnum(0);
		
		gameRoom.setMaster(userid);
		gameRoom.setNumofgames(playway.getNumofgames());   //无限制
		gameRoom.setOrgi(playway.getOrgi());
		
		CacheHelper.getQueneCache().offer(gameRoom, playway.getOrgi());	//未达到最大玩家数量，加入到游戏撮合 队列，继续撮合
		
		UKTools.published(gameRoom, null, BMDataContext.getContext().getBean(GameRoomRepository.class) , BMDataContext.UserDataEventType.SAVE.toString());
		
		return gameRoom ;
	}
}
