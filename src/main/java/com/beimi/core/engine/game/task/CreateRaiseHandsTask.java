package com.beimi.core.engine.game.task;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.GameBoard;
import com.beimi.util.GameUtils;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GameRoom;

public class CreateRaiseHandsTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	
	public CreateRaiseHandsTask(long timer , GameRoom gameRoom, String orgi){
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
		/**
		 * 
		 * 顺手 把牌发了，注：此处应根据 GameRoom的类型获取 发牌方式
		 */
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
		Player lastHandsPlayer = null ;
		for(Player player : board.getPlayers()){
			if(player.getPlayuser().equals(board.getBanker())){//抢到地主的人
				byte[] lastHands = board.pollLastHands() ;
				board.setLasthands(lastHands);
				player.setCards(ArrayUtils.addAll(player.getCards(), lastHands)) ;//翻底牌 
				Arrays.sort(player.getCards());									  //重新排序
				player.setCards(GameUtils.reverseCards(player.getCards()));		  //从大到小 倒序
				lastHandsPlayer = player ;
				break ;
			}
		}
		/**
		 * 计算底牌倍率
		 */
		board.setRatio(board.getRatio() * board.calcRatio());
		
		/**
		 * 发送一个通知，翻底牌消息
		 */
		getRoom(gameRoom).sendEvent("lasthands", new GameBoard(lastHandsPlayer.getPlayuser() , board.getLasthands(), board.getRatio())) ;
		
		/**
		 * 更新牌局状态
		 */
		CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, orgi);
		/**
		 * 发送一个 开始打牌的事件
		 */
		game.change(gameRoom , BeiMiGameEvent.PLAYCARDS.toString() , 1);	//通知状态机 , 此处应由状态机处理异步执行
	}
}
