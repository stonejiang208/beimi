package com.beimi.config.web.model;

/*
 * Copyright 2015 the original author or authors. 
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler.PersistStateChangeListener;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.transition.Transition;

import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.web.model.GameRoom;

public class Game { 

	private final PersistStateMachineHandler handler; 

	private final PersistStateChangeListener listener = new LocalPersistStateChangeListener(); 

	public Game(PersistStateMachineHandler handler) { 
		this.handler = handler; 
		this.handler.addPersistStateChangeListener(listener); 
	} 
	
	public void change(GameRoom room, BeiMiGameEvent event) { 
		  handler.handleEventWithState(MessageBuilder.withPayload(event.toString()).setHeader("room", room.getRoomid()).build(), room.getStatus()); 
	} 

	private class LocalPersistStateChangeListener implements PersistStateChangeListener { 
		@Override 
		public void onPersist(State<String, String> state, Message<String> message, 
				Transition<String, String> transition, StateMachine<String, String> stateMachine) { 
			if (message != null && message.getHeaders().containsKey("order")) { 
				
			} 
		} 
	} 
}
