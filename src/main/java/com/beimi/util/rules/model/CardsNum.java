package com.beimi.util.rules.model;

public class CardsNum implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4465163177787451309L;
	private String userid;
	private int cardsnum ;
	
	public CardsNum(String userid, int cardsnum){
		this.userid = userid ;
		this.cardsnum = cardsnum ;
	}
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getCardsnum() {
		return cardsnum;
	}
	public void setCardsnum(int cardsnum) {
		this.cardsnum = cardsnum;
	}
}
