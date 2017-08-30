package com.beimi.core.statemachine.action;

import com.beimi.core.statemachine.message.Message;

public interface Action<T,S> {
	void execute(Message<T> message); 
}
