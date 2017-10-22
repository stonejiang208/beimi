package com.beimi.core.engine.game.task;

import java.util.List;

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
		if(gamePlayWay!=null){
			/**
			 * 结算信息 ， 更新 玩家信息
			 */
			Summary summary = board.summary(board, gameRoom, gamePlayWay) ;
			sendEvent("allcards",  summary , gameRoom) ;	//通知所有客户端结束牌局，进入结算
			if(summary.isGameRoomOver()){
				CacheHelper.getGamePlayerCacheBean().delete(gameRoom.getId()) ;
				for(Player player : board.getPlayers()){
					CacheHelper.getGameRoomCacheBean().delete(player.getPlayuser(), gameRoom.getOrgi()) ;
				}
				/**
				 * 重新加入房间资源到 队列
				 */
				CacheHelper.getQueneCache().offer(gameRoom.getPlayway(),gameRoom, gameRoom.getOrgi());
			}
		}

		List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
		for(PlayUserClient player : players){
			if(CacheHelper.getApiUserCacheBean().getCacheObject(player.getId(), player.getOrgi())!=null){
				player.setGamestatus(BMDataContext.GameStatusEnum.NOTREADY.toString());
				CacheHelper.getGamePlayerCacheBean().put(player.getId(),player, gameRoom.getOrgi()) ;
			}
		}
		
		BMDataContext.getGameEngine().finished(gameRoom.getId(), orgi);
	}
}
