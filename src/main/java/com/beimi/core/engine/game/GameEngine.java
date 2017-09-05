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
import com.beimi.util.rules.model.TakeCards;
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
				gameEvent.setGameRoom(gameRoom);
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
	 * 出牌，并校验出牌是否合规
	 * @param roomid
	 * 
	 * @param auto 是否自动出牌，超时/托管/AI会调用 = true
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public TakeCards takeCardsRequest(String roomid, String playUserClient, String orgi , boolean auto , byte[] playCards){
		TakeCards takeCards = null ;
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			Player player = board.player(playUserClient) ;
//			board = ActionTaskUtils.doCatch(board, player , accept) ;
			
			if(board!=null){
				//超时了 ， 执行自动出牌
				if(auto == true || playCards != null){
					if(board.getLast() == null || board.getLast().getUserid().equals(player.getPlayuser())){	//当前无出牌信息，刚开始出牌，或者出牌无玩家 压
						/**
						 * 超时处理，如果当前是托管的或玩家超时，直接从最小的牌开始出，如果是 AI，则 需要根据AI级别（低级/中级/高级） 计算出牌 ， 目前先不管，直接从最小的牌开始出
						 */
						takeCards = board.takecard(player , true , playCards) ;
					}else{
						if(playCards == null){
							takeCards = board.takecard(player , board.getLast()) ;
						}else{
							CardType playCardType = ActionTaskUtils.identification(playCards) ;
							CardType lastCardType = ActionTaskUtils.identification(board.getLast().getCards()) ;
							if(ActionTaskUtils.allow(playCardType, lastCardType)){//合规，允许出牌
								takeCards = board.takecard(player , true , playCards) ;
							}else{
								//不合规的牌 ， 需要通知客户端 出牌不符合规则 ， 此处放在服务端判断，防外挂
							}
						}
					}
				}else{
					takeCards = new TakeCards();
					takeCards.setUserid(player.getPlayuser());
				}
				if(takeCards!=null){		//通知出牌
					takeCards.setCardsnum(player.getCards().length);
					takeCards.setAllow(true);
					
					if(takeCards.getCards()!=null){
						board.setLast(takeCards);
						takeCards.setDonot(false);	//出牌
					}else{		
						takeCards.setDonot(true);	//不出牌
					}
					Player next = board.nextPlayer(board.index(player.getPlayuser())) ;
					if(next!=null){
						takeCards.setNextplayer(next.getPlayuser());
						board.setNextplayer(next.getPlayuser());
						
					}
					CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());
					/**
					 * 判断下当前玩家是不是和最后一手牌 是一伙的，如果是一伙的，手机端提示 就是 不要， 如果不是一伙的，就提示要不起
					 */
					if(player.getPlayuser().equals(board.getBanker())){ //当前玩家是地主
						takeCards.setSameside(false);
					}else{
						if(board.getLast().getUserid().equals(board.getBanker())){ //最后一把是地主出的，然而我却不是地主
							takeCards.setSameside(false);	
						}else{
							takeCards.setSameside(true);
						}
					}
					/**
					 * 移除定时器，然后重新设置
					 */
					CacheHelper.getExpireCache().remove(gameRoom.getRoomid());
					
					/**
					 * 牌出完了就算赢了
					 */
					if(board.isWin()){//出完了
						ActionTaskUtils.game().change(gameRoom , BeiMiGameEvent.ALLCARDS.toString() , 0);	//赢了，通知结算
					}else{
						PlayUserClient nextPlayUserClient = ActionTaskUtils.getPlayUserClient(gameRoom.getId(), takeCards.getNextplayer(), orgi) ;
						if(BMDataContext.PlayerTypeEnum.NORMAL.toString().equals(nextPlayUserClient.getPlayertype())){
							ActionTaskUtils.game().change(gameRoom , BeiMiGameEvent.PLAYCARDS.toString() , 25);	//应该从 游戏后台配置参数中获取
						}else{
							ActionTaskUtils.game().change(gameRoom , BeiMiGameEvent.PLAYCARDS.toString() , 3);	//应该从游戏后台配置参数中获取
						}
						
						ActionTaskUtils.sendEvent("takecards", ActionTaskUtils.json(takeCards) , gameRoom);	//type字段用于客户端的音效
					}
				}else{
					takeCards = new TakeCards();
					takeCards.setAllow(false);
					ActionTaskUtils.sendEvent("takecards", ActionTaskUtils.json(takeCards) , gameRoom);	//type字段用于客户端的音效
				}
				
			}
		}
		return takeCards ;
	}
	
	/**
	 * 出牌，不出牌
	 * @param roomid
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public void noCardsRequest(String roomid, PlayUserClient playUser, String orgi){
		
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
