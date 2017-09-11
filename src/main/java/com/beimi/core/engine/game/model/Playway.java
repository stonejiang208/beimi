package com.beimi.core.engine.game.model;

public class Playway implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Playway(String id, String name , String code , int score , int mincoins , int maxcoins , boolean changecard, boolean shuffle){
		this.id = id ; 
		this.name = name ;
		this.score = score ;
		this.code = code ;
		this.mincoins = mincoins ;
		this.maxcoins = maxcoins ;
		this.changecard = changecard ;
		this.shuffle = shuffle ;
	}
	
	private String id;
	private String name ;
	private String code ;
	
	private int score;		//底分
	private int mincoins ;	//最小金币数量
	private int maxcoins ;	//最大金币数量
	
	private boolean changecard ;	//换牌
	
	private int onlineusers ;	//在线用户数
	
	private boolean shuffle ;	//是否洗牌
	private String level ;		//级别
	private String skin ;		//图标颜色
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getMincoins() {
		return mincoins;
	}
	public void setMincoins(int mincoins) {
		this.mincoins = mincoins;
	}
	public int getMaxcoins() {
		return maxcoins;
	}
	public void setMaxcoins(int maxcoins) {
		this.maxcoins = maxcoins;
	}
	public boolean isChangecard() {
		return changecard;
	}
	public void setChangecard(boolean changecard) {
		this.changecard = changecard;
	}
	public int getOnlineusers() {
		return onlineusers;
	}
	public void setOnlineusers(int onlineusers) {
		this.onlineusers = onlineusers;
	}
	public boolean isShuffle() {
		return shuffle;
	}
	public void setShuffle(boolean shuffle) {
		this.shuffle = shuffle;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getSkin() {
		return skin;
	}
	public void setSkin(String skin) {
		this.skin = skin;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}	
