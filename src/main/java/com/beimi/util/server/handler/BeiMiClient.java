package com.beimi.util.server.handler;

import com.corundumstudio.socketio.SocketIOClient;

public class BeiMiClient{
	private String token ;
	private String playway ;
	private String orgi ;
	private String room ;
	
	private String userid ;
	
	private String session ;
	
	
	private SocketIOClient client;
	
	public BeiMiClient(){
		
	}
	
	public String getSession() {
		return session;
	}


	public void setSession(String session) {
		this.session = session;
	}


	public String getPlayway() {
		return playway;
	}

	public void setPlayway(String playway) {
		this.playway = playway;
	}

	public String getOrgi() {
		return orgi;
	}

	public void setOrgi(String orgi) {
		this.orgi = orgi;
	}

	public SocketIOClient getClient() {
		return client;
	}

	public void setClient(SocketIOClient client) {
		this.client = client;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
}
