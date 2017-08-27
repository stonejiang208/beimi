package com.beimi.core.engine.game.impl;

import java.io.Serializable;

import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;

public class UserBoard implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1224310911110772375L;
	private Player player ;
	private Player[] players ;
	/**
	 * 发给玩家的牌，开启特权后可以将某个其他玩家的牌 显示出来
	 * @param board
	 * @param curruser
	 */
	public UserBoard(Board board , String curruser){
		players = new Player[board.getPlayers().length-1] ;
		int inx = 0 ;
		for(Player temp : board.getPlayers()){
			if(temp.getPlayuser().equals(curruser)){
				player = temp ;
			}else{
				Player clonePlayer = temp.clone() ;
				clonePlayer.setCards(null);	//克隆对象，然后将 其他玩家手里的牌清空
				players[inx++] = clonePlayer;
			}
		}
	}
	
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}

	public Player[] getPlayers() {
		return players;
	}

	public void setPlayers(Player[] players) {
		this.players = players;
	}
}
