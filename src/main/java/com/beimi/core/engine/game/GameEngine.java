package com.beimi.core.engine.game;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.state.GameEvent;
import com.beimi.core.engine.game.task.majiang.CreateMJRaiseHandsTask;
import com.beimi.util.GameUtils;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.util.rules.model.Action;
import com.beimi.util.rules.model.ActionEvent;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.DuZhuBoard;
import com.beimi.util.rules.model.Player;
import com.beimi.util.rules.model.RecoveryData;
import com.beimi.util.rules.model.SelectColor;
import com.beimi.util.rules.model.TakeCards;
import com.beimi.util.server.handler.BeiMiClient;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.service.repository.jpa.GameRoomRepository;
import com.corundumstudio.socketio.SocketIOServer;

@Service(value="beimiGameEngine")
public class GameEngine {
	
	@Autowired
	protected SocketIOServer server;
	
	public void gameRequest(String userid ,String playway , String room , String orgi , PlayUserClient userClient , BeiMiClient beiMiClient ){
		GameEvent gameEvent = gameRequest(userClient.getId(), beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), userClient) ;
		if(gameEvent != null){
			/**
			 * 举手了，表示游戏可以开始了
			 */
			if(userClient!=null){
				userClient.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
				CacheHelper.getGamePlayerCacheBean().put(userClient.getId(),userClient, userClient.getOrgi()) ;
			}
			/**
			 * 游戏状态 ， 玩家请求 游戏房间，活动房间状态后，发送事件给 StateMachine，由 StateMachine驱动 游戏状态 ， 此处只负责通知房间内的玩家
			 * 1、有新的玩家加入
			 * 2、给当前新加入的玩家发送房间中所有玩家信息（不包含隐私信息，根据业务需求，修改PlayUserClient的字段，剔除掉隐私信息后发送）
			 */
			ActionTaskUtils.sendEvent("joinroom", userClient , gameEvent.getGameRoom());
			/**
			 * 发送给单一玩家的消息
			 */
			ActionTaskUtils.sendPlayers(beiMiClient, gameEvent.getGameRoom());
			/**
			 * 当前是在游戏中还是 未开始
			 */
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameEvent.getRoomid(), gameEvent.getOrgi());
			if(board !=null){
				Player currentPlayer = null;
				for(Player player : board.getPlayers()){
					if(player.getPlayuser().equals(userClient.getId())){
						currentPlayer = player ; break ;
					}
				}
				if(currentPlayer!=null){
					boolean automic = false ;
					if((board.getLast()!=null && board.getLast().getUserid().equals(currentPlayer.getPlayuser())) || (board.getLast() == null && board.getBanker().equals(currentPlayer.getPlayuser()))){
						automic = true ;
					}
					ActionTaskUtils.sendEvent("recovery", new RecoveryData(currentPlayer , board.getLasthands() , board.getNextplayer() , 25 , automic , board) , gameEvent.getGameRoom());
				}
			}else{
				//通知状态
				GameUtils.getGame(beiMiClient.getPlayway() , gameEvent.getOrgi()).change(gameEvent);	//通知状态机 , 此处应由状态机处理异步执行
			}
		}
	}
	
	/**
	 * 玩家房间选择， 新请求，游戏撮合， 如果当前玩家是断线重连， 或者是 退出后进入的，则第一步检查是否已在房间
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
				}else{	//
					/**
					 * 大厅游戏 ， 撮合游戏 , 发送异步消息，通知RingBuffer进行游戏撮合，撮合算法描述如下：
					 * 1、按照查找
					 * 
					 */
					gameRoom = (GameRoom) CacheHelper.getQueneCache().poll(playway , orgi) ;
					if(gameRoom==null){	//无房间 ， 需要
						gameRoom = this.creatGameRoom(gamePlayway, userid , false) ;
					}else{
					
						/**
						 * 如果当前房间到达了最大玩家数量，则不再加入到 撮合队列
						 */
						List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
						if((playerList.size() + 1) < gamePlayway.getPlayers() && CacheHelper.getExpireCache().get(gameRoom.getId()) != null){
							CacheHelper.getQueneCache().offer(gameRoom.getPlayway() , gameRoom, orgi);	//未达到最大玩家数量，加入到游戏撮合 队列，继续撮合
						}
						playUser.setPlayerindex(System.currentTimeMillis());//从后往前坐，房主进入以后优先坐在 首位
					}
				}
			}
			if(gameRoom!=null){
				gameRoom.setCurrentnum(0);
				/**
				 * 更新缓存
				 */
				CacheHelper.getGameRoomCacheBean().put(gameRoom.getId(), gameRoom, orgi);
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
					playUser.setPlayerindex(System.currentTimeMillis());
					playUser.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
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
			DuZhuBoard board = (DuZhuBoard) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			Player player = board.player(playUser.getId()) ;
			board = ActionTaskUtils.doCatch(board, player , accept) ;
			
			ActionTaskUtils.sendEvent("catchresult",new GameBoard(player.getPlayuser() , player.isAccept(), board.isDocatch() , board.getRatio()),gameRoom) ;
			GameUtils.getGame(gameRoom.getPlayway() , orgi).change(gameRoom , BeiMiGameEvent.AUTO.toString() , 15);	//通知状态机 , 继续执行
			
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
			
			if(board!=null && player.getPlayuser().equals(board.getNextplayer())){
				takeCards = board.takeCardsRequest(gameRoom, board, player, orgi, auto, playCards) ;
			}
		}
		return takeCards ;
	}
	
	/**
	 * 检查是否所有玩家 都已经处于就绪状态，如果所有玩家都点击了 继续开始游戏，则发送一个 ALL事件，继续游戏，
	 * 否则，等待10秒时间，到期后如果玩家还没有就绪，就将该玩家T出去，等待新玩家加入
	 * @param roomid
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public void restartRequest(String roomid , String userid, String orgi , BeiMiClient beiMiClient){
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		boolean notReady = false ;
		List<PlayUserClient> playerList = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
		if(playerList!=null && playerList.size() > 0){
			/**
			 * 有一个 等待 
			 */
			for(int i=0; i<playerList.size() ; ){
				PlayUserClient player = playerList.get(i) ;
				if(player.getPlayertype().equals(BMDataContext.PlayerTypeEnum.NORMAL.toString())){
					//普通玩家，当前玩家修改为READY状态
					PlayUserClient apiPlayUser = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(player.getId(), player.getOrgi()) ;
					if(player.getId().equals(userid)){
						player.setGamestatus(BMDataContext.GameStatusEnum.READY.toString());
						/**
						 * 更新状态
						 */
						CacheHelper.getApiUserCacheBean().put(player.getId(), apiPlayUser, orgi);
					}else{//还有未就绪的玩家
						if(!player.getGamestatus().equals(BMDataContext.GameStatusEnum.READY.toString())){
							notReady = true ;
						}
					}
				}
				i++ ;
			}
		}
		if(notReady == true){
			/**
			 * 需要增加一个状态机的触发事件：等待其他人就绪，超过5秒以后未就绪的，直接踢掉，然后等待机器人加入
			 */
			GameUtils.getGame(gameRoom.getPlayway() , orgi).change(gameRoom , BeiMiGameEvent.ENTER.toString() , 0);
		}else if(playerList == null || playerList.size() == 0){//房间已解散
			PlayUserClient userClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(userid, orgi) ;
			BMDataContext.getGameEngine().gameRequest(userid, beiMiClient.getPlayway(), beiMiClient.getRoom(), beiMiClient.getOrgi(), userClient , beiMiClient) ;
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
	public SelectColor selectColorRequest(String roomid, String userid, String orgi , String color){
		SelectColor selectColor = null ;
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			if(board!=null){
				//超时了 ， 执行自动出牌
//				Player[] players = board.getPlayers() ;
				/**
				 * 检查是否所有玩家都已经选择完毕 ， 如果所有人都选择完毕，即可开始
				 */
				selectColor = new SelectColor(board.getBanker());
				if(!StringUtils.isBlank(color)){
					if(!StringUtils.isBlank(color) && color.matches("[0-2]{1}")){
						selectColor.setColor(Integer.parseInt(color));
					}else{
						selectColor.setColor(0);
					}
					selectColor.setTime(System.currentTimeMillis());
					selectColor.setCommand("selectresult");
					
					selectColor.setUserid(userid);
				}
				boolean allselected = true ;
				for(Player ply : board.getPlayers()){
					if(ply.getPlayuser().equals(userid)){
						if(!StringUtils.isBlank(color) && color.matches("[0-2]{1}")){
							ply.setColor(Integer.parseInt(color));
						}else{
							ply.setColor(0);
						}
						ply.setSelected(true);
					}
					if(!ply.isSelected()){
						allselected = false ;
					}
				}
				CacheHelper.getBoardCacheBean().put(gameRoom.getId() , board, gameRoom.getOrgi());	//更新缓存数据
				ActionTaskUtils.sendEvent("selectresult", selectColor , gameRoom);	
				/**
				 * 检查是否全部都已经 定缺， 如果已全部定缺， 则发送 开打 
				 */
				if(allselected){
					/**
					 * 重置计时器，立即执行
					 */
					CacheHelper.getExpireCache().put(gameRoom.getId(), new CreateMJRaiseHandsTask(1 , gameRoom , gameRoom.getOrgi()) );
					GameUtils.getGame(gameRoom.getPlayway() , orgi).change(gameRoom , BeiMiGameEvent.RAISEHANDS.toString() , 0);	
				}
			}
		}
		return selectColor ;
	}
	
	/**
	 * 麻将 ， 杠碰吃胡过
	 * @param roomid
	 * 
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public ActionEvent actionEventRequest(String roomid, String userid, String orgi , String action){
		ActionEvent actionEvent = null ;
		GameRoom gameRoom = (GameRoom) CacheHelper.getGameRoomCacheBean().getCacheObject(roomid, orgi) ;
		if(gameRoom!=null){
			Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
			if(board!=null){
				Player player = board.player(userid) ;
				byte card = board.getLast().getCard() ;
				actionEvent = new ActionEvent(board.getBanker() , userid , card , action);
				if(!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.GUO.toString())){
					/**
					 * 用户动作，选择 了 过， 下一个玩家直接开始抓牌 
					 * bug，待修复：如果有多个玩家可以碰，则一个碰了，其他玩家就无法操作了
					 */
					board.dealRequest(gameRoom, board, orgi , false , null);
				}else if(!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.PENG.toString())){
					Action playerAction = new Action(userid , action , card);
					
					int color = card / 36 ;
					int value = card % 36 / 4 ;
					List<Byte> otherCardList = new ArrayList<Byte>(); 
					for(int i=0 ; i<player.getCards().length ; i++){
						if(player.getCards()[i]/36 == color && (player.getCards()[i]%36) / 4 == value){
							continue ;
						}
						otherCardList.add(player.getCards()[i]) ;
					}
					byte[] otherCards = new byte[otherCardList.size()] ;
					for(int i=0 ; i<otherCardList.size() ; i++){
						otherCards[i] = otherCardList.get(i) ;
					}
					player.setCards(otherCards);
					player.getActions().add(playerAction) ;
					
					board.setNextplayer(userid);
					
					actionEvent.setTarget(board.getLast().getUserid());
					ActionTaskUtils.sendEvent("selectaction", actionEvent , gameRoom);
					
					CacheHelper.getBoardCacheBean().put(gameRoom.getId() , board, gameRoom.getOrgi());	//更新缓存数据
					
					board.playcards(board, gameRoom, player, orgi);
					
				}else if(!StringUtils.isBlank(action) && action.equals(BMDataContext.PlayerAction.GANG.toString())){
					if(board.getNextplayer().equals(userid)){
						card = GameUtils.getGangCard(player.getCards()) ;
						actionEvent = new ActionEvent(board.getBanker() , userid , card , action);
						actionEvent.setActype(BMDataContext.PlayerGangAction.AN.toString());
					}else{
						actionEvent.setActype(BMDataContext.PlayerGangAction.MING.toString());	//还需要进一步区分一下是否 弯杠
					}
					/**
					 * 检查是否有弯杠
					 */
					Action playerAction = new Action(userid , action , card);
					for(Action ac : player.getActions()){
						if(ac.getCard() == card && ac.getAction().equals(BMDataContext.PlayerAction.PENG.toString())){
							ac.setGang(true);
							ac.setType(BMDataContext.PlayerGangAction.WAN.toString());
							playerAction = ac ;
							break ;
						}
					}
					int color = card / 36 ;
					int value = card % 36 / 4 ;
					List<Byte> otherCardList = new ArrayList<Byte>(); 
					for(int i=0 ; i<player.getCards().length ; i++){
						if(player.getCards()[i]/36 == color && (player.getCards()[i]%36) / 4 == value){
							continue ;
						}
						otherCardList.add(player.getCards()[i]) ;
					}
					byte[] otherCards = new byte[otherCardList.size()] ;
					for(int i=0 ; i<otherCardList.size() ; i++){
						otherCards[i] = otherCardList.get(i) ;
					}
					player.setCards(otherCards);
					player.getActions().add(playerAction) ;
					
					actionEvent.setTarget("all");	//只有明杠 是 其他人打出的 ， target 是单一对象
					
					ActionTaskUtils.sendEvent("selectaction", actionEvent , gameRoom);
					
					/**
					 * 杠了以后， 从 当前 牌的 最后一张开始抓牌
					 */
					board.dealRequest(gameRoom, board, orgi , true , userid);
				}
			}
		}
		return actionEvent ;
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
	 * 结束 当前牌局
	 * @param userid
	 * @param orgi
	 * @return
	 */
	public void finished(String roomid, String orgi){
		if(!StringUtils.isBlank(roomid)){//
			CacheHelper.getExpireCache().remove(roomid);
			CacheHelper.getBoardCacheBean().delete(roomid, orgi) ;
		}
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
		
		CacheHelper.getQueneCache().offer(playway.getId(),gameRoom, playway.getOrgi());	//未达到最大玩家数量，加入到游戏撮合 队列，继续撮合
		
		UKTools.published(gameRoom, null, BMDataContext.getContext().getBean(GameRoomRepository.class) , BMDataContext.UserDataEventType.SAVE.toString());
		
		return gameRoom ;
	}
}
