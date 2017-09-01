package com.beimi.core.engine.game;

import com.alibaba.fastjson.JSON;
import com.beimi.config.web.model.Game;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.task.AbstractTask;
import com.beimi.core.engine.game.task.CreateAutoTask;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GameRoom;
import com.corundumstudio.socketio.BroadcastOperations;
import com.corundumstudio.socketio.SocketIOServer;

public class ActionTaskUtils {
	/**
	 * 
	 * @param times
	 * @param gameRoom
	 * @return
	 */
	public static AbstractTask createAutoTask(int times , GameRoom gameRoom){
		return new CreateAutoTask(times , gameRoom , gameRoom.getOrgi()) ;
	}
	/**
	 * 
	 * @return
	 */
	public static Game game(){
		return BMDataContext.getContext().getBean(Game.class) ;
	}
	public static BroadcastOperations getRoom(GameRoom gameRoom){
		return BMDataContext.getContext().getBean(SocketIOServer.class).getRoomOperations(gameRoom.getId());
	}
	
	public static String json(Object data){
		return JSON.toJSONString(data!=null ? data : "") ;
	}
	/**
	 * 临时放这里，重构的时候 放到 游戏类型的 实现类里
	 * @param board
	 * @param player
	 * @return
	 */
	public static Board doCatch(Board board, Player player , boolean result){
		player.setAccept(result); //抢地主
		player.setDocatch(true);
		board.setDocatch(true);
		if(result){	//抢了地主
			board.setRatio(board.getRatio()*2);
			board.setBanker(player.getPlayuser());
		}
		return board ;
	}
}
