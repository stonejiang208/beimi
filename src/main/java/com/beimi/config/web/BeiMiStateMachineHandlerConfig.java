package com.beimi.config.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.beimi.config.web.model.Game;
import com.beimi.core.statemachine.BeiMiStateMachine;
import com.beimi.core.statemachine.impl.BeiMiMachineHandler;

@Configuration
public class BeiMiStateMachineHandlerConfig {
	
	@Autowired
	private BeiMiStateMachine<String,String> configure ;
	
    @Bean
    public Game persist() {
        return new Game(persistStateMachineHandler());
    }

    public BeiMiMachineHandler persistStateMachineHandler() {
        return new BeiMiMachineHandler(this.configure);
    }
}
