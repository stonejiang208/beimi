package com.beimi.core.engine.game.task;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.config.web.model.Game;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.TakeCards;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GameRoom;
import com.corundumstudio.socketio.BroadcastOperations;

public abstract class AbstractTask implements ValueWithExpiryTime {
	protected Game game ;

	public AbstractTask(){
		game = ActionTaskUtils.game();
	}
	
	public BroadcastOperations getRoom(GameRoom gameRoom){
		return ActionTaskUtils.getRoom(gameRoom);
	}
	/**
	 * 找到玩家
	 * @param board
	 * @param userid
	 * @return
	 */
	public Player player(Board board , String userid){
		Player target = null ;
		for(Player temp : board.getPlayers()){
			if(temp.getPlayuser().equals(userid)){
				target = temp ; break ;
			}
		}
		return target ;
	}
	
	/**
	 * 找到玩家的 位置
	 * @param board
	 * @param userid
	 * @return
	 */
	public int index(Board board , String userid){
		int index = 0;
		for(int i=0 ; i<board.getPlayers().length ; i++){
			Player temp = board.getPlayers()[i] ;
			if(temp.getPlayuser().equals(userid)){
				index = i ; break ;
			}
		}
		return index ;
	}
	
	
	/**
	 * 找到下一个玩家
	 * @param board
	 * @param index
	 * @return
	 */
	protected Player next(Board board , int index){
		Player catchPlayer = null;
		if(index == 0 && board.getPlayers()[index].isRandomcard()){	//fixed
			index = board.getPlayers().length - 1 ;
		}
		for(int i = index ; i>=0 ; i--){
			Player player = board.getPlayers()[i] ;
			if(player.isDocatch() == false){
				catchPlayer = player ;
				break ;
			}else if(player.isRandomcard()){	//重新遍历一遍，发现找到了地主牌的人，终止查找
				break ;
			}else if(i == 0){
				i = board.getPlayers().length;
			}
		}
		return catchPlayer;
	}
	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 * @param player
	 * @param current
	 * @return
	 */
	public TakeCards takecard(Player player , TakeCards current) {
		
		return null;
	}
}
