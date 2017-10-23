package com.beimi.util.rules.model;

import com.beimi.core.engine.game.Message;

public class GameStatus implements Message{
	private String command ;
	private String gamestatus ;
	
	private String userid ;
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getGamestatus() {
		return gamestatus;
	}
	public void setGamestatus(String gamestatus) {
		this.gamestatus = gamestatus;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}
}
