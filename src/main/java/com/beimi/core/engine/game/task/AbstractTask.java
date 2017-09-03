package com.beimi.core.engine.game.task;

import java.util.List;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.config.web.model.Game;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.util.rules.model.TakeCards;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;

public abstract class AbstractTask implements ValueWithExpiryTime {
	protected Game game ;

	public AbstractTask(){
		game = ActionTaskUtils.game();
	}
	
	public void sendEvent(String event , Object data , GameRoom gameRoom){
		ActionTaskUtils.sendEvent(event, data, gameRoom);
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
	

	public Player nextPlayer(Board board, int index) {
		if(index == 0){
			index = board.getPlayers().length - 1 ;
		}else{
			index = index - 1 ;
		}
		return board.getPlayers()[index];
	}
	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 * @param player
	 * @param current
	 * @return
	 */
	public TakeCards takecard(Board board , Player player , TakeCards last) {
		TakeCards current = null;
		if(last == null){
			current = new TakeCards(player);
		}else{
			current = new TakeCards(player);
		}
		return current;
	}
	
	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 * @param player
	 * @param current
	 * @return
	 */
	public TakeCards takecard(Board board ,Player player) {
		return takecard(board  , player , null);
	}
	
	public PlayUserClient getPlayUserClient(String roomid,String player , String orgi){
		PlayUserClient playUserClient = null;
		List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(roomid, orgi) ;
		for(PlayUserClient user : players){
			if(player.equals(user.getId())){
				playUserClient = user ;
			}
		}
		return playUserClient;
	}
	
	public Object json(Object data){
		return UKTools.json(data) ;
	}

}
