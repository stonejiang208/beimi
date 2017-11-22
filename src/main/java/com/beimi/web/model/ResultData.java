package com.beimi.web.model;

import java.util.List;

import com.beimi.core.engine.game.BeiMiGame;

/**
 * 
 * @author iceworld
 *
 */
public class ResultData implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2710593844038107782L;
	private boolean status ;
	private String msg ;
	private Object data ;
	
	private String gametype ;			// game model : 大厅 | 房卡
	
	private Token token ;
	private List<BeiMiGame> games ;		//游戏配置里选择的游戏类型
	
	public ResultData(boolean status , String msg , Object data){
		this.status = status ;
		this.msg = msg ;
		this.data = data ;
	}
	
	public ResultData(boolean status , String msg , Object data , Token token ){
		this.status = status ;
		this.msg = msg ;
		this.data = data ;
		this.token = token ;
	}
	
	public ResultData(boolean status , String msg , Object data , Token token ,List<BeiMiGame> games){
		this.status = status ;
		this.msg = msg ;
		this.data = data ;
		this.token = token ;
		this.games = games ;
	}
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}

	public String getGametype() {
		return gametype;
	}

	public void setGametype(String gametype) {
		this.gametype = gametype;
	}

	public List<BeiMiGame> getGames() {
		return games;
	}

	public void setGames(List<BeiMiGame> games) {
		this.games = games;
	}
}
