package com.beimi.config.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.recipes.persist.PersistStateMachineHandler;

import com.beimi.config.web.model.Game;

@Configuration
public class PersistHandlerConfig {
	@Autowired
    private StateMachine<String, String> stateMachine;

    @Bean
    public Game persist() {
        return new Game(persistStateMachineHandler());
    }

    @Bean
    public PersistStateMachineHandler persistStateMachineHandler() {
        return new PersistStateMachineHandler(stateMachine);
    }
}
