package com.beimi.core.engine.game.task;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.model.Summary;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;

public class CreateAllCardsTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	
	public CreateAllCardsTask(long timer , GameRoom gameRoom, String orgi){
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
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
		board.setFinished(true);
		GamePlayway gamePlayWay = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi()) ;
		boolean gameOver = false ;
		if(gamePlayWay!=null){
			/**
			 * 结算信息 ， 更新 玩家信息
			 */
			Summary summary = board.summary(board, gameRoom, gamePlayWay) ;
			sendEvent("allcards",  summary , gameRoom) ;	//通知所有客户端结束牌局，进入结算
			if(summary.isGameRoomOver()){
				gameOver = true ;
			}
		}
		for(Player player : board.getPlayers()){
			PlayUserClient playUserClient = (PlayUserClient) CacheHelper.getApiUserCacheBean().getCacheObject(player.getPlayuser(), this.orgi) ;
			if(playUserClient!=null && playUserClient.getPlayertype().equals(BMDataContext.PlayerTypeEnum.NORMAL.toString())){
				playUserClient.setGamestatus(BMDataContext.GameStatusEnum.NOTREADY.toString());
				
				playUserClient.setRoomready(false);
				CacheHelper.getGamePlayerCacheBean().put(playUserClient.getId(),playUserClient, gameRoom.getOrgi()) ;
			}else if(playUserClient == null || !playUserClient.getPlayertype().equals(BMDataContext.PlayerTypeEnum.AI.toString())){
				//清理已经离线的玩家
				gameOver = true ;			
			}
		}
		
		if(gameOver){
			CacheHelper.getGamePlayerCacheBean().clean(gameRoom.getId() ,orgi) ;
			for(Player player : board.getPlayers()){
				CacheHelper.getGameRoomCacheBean().delete(player.getPlayuser(), gameRoom.getOrgi()) ;
				CacheHelper.getRoomMappingCacheBean().delete(player.getPlayuser(), this.orgi) ;
				
			}
			if(gameRoom.isCardroom() == false){ //房卡模式，清理掉房卡资源
				/**
				 * 重新加入房间资源到 队列
				 */
				CacheHelper.getQueneCache().put(gameRoom, gameRoom.getOrgi());
			}
		}
		
		BMDataContext.getGameEngine().finished(gameRoom.getId(), orgi);
	}
}
