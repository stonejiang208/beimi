package com.beimi.util.rules.model;

public class Action implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8077510029073026136L;
	private byte card ;
	private String action ;
	private String type ;			//动作类型， 杠 ： 明杠|暗杠|弯杠  ，  胡：胡法
	private String userid ;
	
	
	public Action(){}
	public Action(String userid , String action , String type , byte card){
		this.userid = userid ;
		this.action = action ;
		this.type = type ; 
		this.card = card ;
	}
	
	public Action(String userid , String action ,byte card){
		this.userid = userid ;
		this.action = action ;
		this.card = card ;
	}
	
	public byte getCard() {
		return card;
	}
	public void setCard(byte card) {
		this.card = card;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
}
