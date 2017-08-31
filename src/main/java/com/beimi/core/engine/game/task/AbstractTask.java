package com.beimi.core.engine.game.task;

import com.alibaba.fastjson.JSON;
import com.beimi.config.web.model.Game;
import com.beimi.core.BMDataContext;

public class AbstractTask {
	protected Game game ;
	
	public AbstractTask(){
		game = BMDataContext.getContext().getBean(Game.class) ;
	}
	
	public String json(Object data){
		return JSON.toJSONString(data!=null ? data : "") ;
	}
}
