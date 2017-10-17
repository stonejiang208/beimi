package com.beimi.core.engine.game.model;

public class SummaryPlayer implements java.io.Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String userid ;
	private int ratio ;
	private int score ;
	private boolean win ;
	private byte[] cards ;
	
	public SummaryPlayer(){}
	public SummaryPlayer(String userid , int ratio , int score, boolean win){
		this.userid = userid ;
		this.ratio = ratio ; 
		this.score = score ;
		this.win = win ;
	}
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getRatio() {
		return ratio;
	}
	public void setRatio(int ratio) {
		this.ratio = ratio;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public boolean isWin() {
		return win;
	}
	public void setWin(boolean win) {
		this.win = win;
	}
	public byte[] getCards() {
		return cards;
	}
	public void setCards(byte[] cards) {
		this.cards = cards;
	}
}
