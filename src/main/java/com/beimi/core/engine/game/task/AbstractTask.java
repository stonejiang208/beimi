package com.beimi.core.engine.game.task;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.config.web.model.Game;
import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.util.UKTools;
import com.beimi.web.model.GameRoom;

public abstract class AbstractTask implements ValueWithExpiryTime {
	protected Game game ;

	public AbstractTask(){
		game = ActionTaskUtils.game();
	}
	
	public void sendEvent(String event , Object data , GameRoom gameRoom){
		ActionTaskUtils.sendEvent(event, data, gameRoom);
	}
	
	public Object json(Object data){
		return UKTools.json(data) ;
	}

}
