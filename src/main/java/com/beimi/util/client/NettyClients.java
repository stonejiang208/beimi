package com.beimi.util.client;

import java.util.List;

import com.corundumstudio.socketio.SocketIOClient;


public class NettyClients {
	
	private static NettyClients clients = new NettyClients();
	
	private NettyGameClient gameClients = new NettyGameClient();
	private NettySystemClient systemClients = new NettySystemClient();
	
	public static NettyClients getInstance(){
		return clients ;
	}

	public void setImClients(NettyGameClient imClients) {
		this.gameClients = imClients;
	}
	public void putIMEventClient(String id , SocketIOClient userClient){
		gameClients.putClient(id, userClient);
	}
	public void removeIMEventClient(String id , String sessionid){
		gameClients.removeClient(id, sessionid);
	}
	public void sendIMEventMessage(String id , String event , Object data){
		List<SocketIOClient> userClients = gameClients.getClients(id) ;
		for(SocketIOClient userClient : userClients){
			userClient.sendEvent(event, data);
		}
	}
	
	public void setGameClients(NettySystemClient gameClients) {
		this.systemClients = gameClients;
	}
	public void putGameEventClient(String id , SocketIOClient gameClient){
		systemClients.putClient(id, gameClient);
	}
	public void removeGameEventClient(String id , String sessionid){
		systemClients.removeClient(id, sessionid);
	}
	public void sendGameEventMessage(String id , String event , Object data){
		List<SocketIOClient> gameClients = systemClients.getClients(id) ;
		for(SocketIOClient gameClient : gameClients){
			gameClient.sendEvent(event, data);
		}
	}
	
	public void joinRoom(String id , String roomid){
		List<SocketIOClient> gameClients = systemClients.getClients(id) ;
		for(SocketIOClient gameClient : gameClients){
			gameClient.joinRoom(roomid);
		}
	}
}
