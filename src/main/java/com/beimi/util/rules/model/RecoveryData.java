package com.beimi.util.rules.model;

import java.util.ArrayList;
import java.util.List;

import com.beimi.core.engine.game.GameBoard;
import com.beimi.core.engine.game.Message;

public class RecoveryData implements Message{
	private String command ;
	private String userid ;
	private Player player ;
	private byte[] lasthands;
	private TakeCards last ;
	private String banker ;
	private String nextplayer ;//正在出牌的玩家
	private CardsNum[] cardsnum ;
	private int time ;		//计时器剩余时间
	private boolean automic ;	//本轮第一个出牌，不允许出现不出按钮
	private GameBoard data ;
	
	
	public RecoveryData(Player player , byte[] lasthands , String nextplayer , int time , boolean automic , Board board){
		this.player = player ;
		this.userid = player.getPlayuser() ;
		this.lasthands = lasthands ;
		this.nextplayer = nextplayer ;
		this.time = time ;
		this.automic = automic;
		this.data = new GameBoard(board.getBanker(), board.getRatio()) ;
		
		this.last = board.getLast() ;
		this.banker = board.getBanker();
		this.cardsnum = new CardsNum[board.getPlayers().length - 1];
		List<CardsNum> tempList = new ArrayList<CardsNum>();
		for(Player temp : board.getPlayers()){
			if(!temp.getPlayuser().equals(player.getPlayuser())){
				tempList.add(new CardsNum(temp.getPlayuser() , temp.getCards().length)) ;
			}
		}
		cardsnum = tempList.toArray(this.cardsnum) ;
	}
	
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public byte[] getLasthands() {
		return lasthands;
	}
	public void setLasthands(byte[] lasthands) {
		this.lasthands = lasthands;
	}
	public String getNextplayer() {
		return nextplayer;
	}

	public void setNextplayer(String nextplayer) {
		this.nextplayer = nextplayer;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	public boolean isAutomic() {
		return automic;
	}
	public void setAutomic(boolean automic) {
		this.automic = automic;
	}


	public CardsNum[] getCardsnum() {
		return cardsnum;
	}


	public void setCardsnum(CardsNum[] cardsnum) {
		this.cardsnum = cardsnum;
	}


	public String getBanker() {
		return banker;
	}


	public void setBanker(String banker) {
		this.banker = banker;
	}


	public String getUserid() {
		return userid;
	}


	public void setUserid(String userid) {
		this.userid = userid;
	}


	public TakeCards getLast() {
		return last;
	}


	public void setLast(TakeCards last) {
		this.last = last;
	}


	public GameBoard getData() {
		return data;
	}


	public void setData(GameBoard data) {
		this.data = data;
	}
	
}
