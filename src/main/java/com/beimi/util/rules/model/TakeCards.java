package com.beimi.util.rules.model;

/**
 * 当前出牌信息
 * 出牌人
 * 牌
 * @author zhangtianyi
 *
 */
public class TakeCards implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8718778983090104033L;
	private String player ;
	private byte[] cards ;
	private long time ;
	public String getPlayer() {
		return player;
	}
	public void setPlayer(String player) {
		this.player = player;
	}
	public byte[] getCards() {
		return cards;
	}
	public void setCards(byte[] cards) {
		this.cards = cards;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
}
