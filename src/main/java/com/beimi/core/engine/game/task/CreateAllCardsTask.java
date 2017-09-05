package com.beimi.core.engine.game.task;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.web.model.GameRoom;

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
		/**
		 * 结算信息 ， 更新 玩家信息
		 */
		sendEvent("allcards", board , gameRoom) ;
	}
}
