package com.beimi.util.rules.model;

import com.beimi.core.engine.game.Message;
import com.beimi.web.model.PlayUserClient;

public class JoinRoom implements Message{
	private String command ;
	private PlayUserClient player ;
	private int index ;
	private int maxplayers ;
	
	public JoinRoom(PlayUserClient player , int index , int maxplayer){
		this.player = player;
		this.index = index;
		this.maxplayers = maxplayer ;
		
	}
	
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public PlayUserClient getPlayer() {
		return player;
	}
	public void setPlayer(PlayUserClient player) {
		this.player = player;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getMaxplayers() {
		return maxplayers;
	}
	public void setMaxplayers(int maxplayers) {
		this.maxplayers = maxplayers;
	}
}	
