package com.beimi.config.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import com.beimi.core.engine.game.BeiMiGameEnum;
import com.beimi.core.engine.game.BeiMiGameEvent;

@Configuration
@EnableStateMachine
public class BeiMiStateMachineConfig extends StateMachineConfigurerAdapter<String, String> {
	
		
	@Override
    public void configure(StateMachineStateConfigurer<String,String> states)
            throws Exception {
        states
            .withStates()
                .initial(BeiMiGameEnum.ENTER.toString())
                    .state(BeiMiGameEnum.BEGIN.toString())
                    .state(BeiMiGameEnum.READY.toString())
                    .state(BeiMiGameEnum.PLAY.toString())
                    .state(BeiMiGameEnum.WAITTING.toString())
                    .state(BeiMiGameEnum.END.toString());
	}

	@Override
    public void configure(StateMachineTransitionConfigurer<String, String> transitions)
            throws Exception {
		/**
		 * 状态切换：BEGIN->WAITTING->READY->PLAY->END
		 */
        transitions
	        .withExternal()	
	        	.source(BeiMiGameEnum.ENTER.toString()).target(BeiMiGameEnum.BEGIN.toString())
	        	.event(BeiMiGameEvent.ENTER.toString()).action(null)
	        	.and()
            .withExternal()	
                .source(BeiMiGameEnum.BEGIN.toString()).target(BeiMiGameEnum.WAITTING.toString())
                .event(BeiMiGameEvent.BEGIN.toString()).action(null)
                .and()
            .withExternal()
                .source(BeiMiGameEnum.WAITTING.toString()).target(BeiMiGameEnum.READY.toString())
                .event(BeiMiGameEvent.ENOUGH.toString()).action(null)
                .and()
            .withExternal()
                .source(BeiMiGameEnum.READY.toString()).target(BeiMiGameEnum.PLAY.toString())
                .event(BeiMiGameEvent.RAISEHANDS.toString()).action(null)
                .and()
            .withExternal()
                .source(BeiMiGameEnum.PLAY.toString()).target(BeiMiGameEnum.END.toString())
                .event(BeiMiGameEvent.ALLCARDS.toString()).action(null)
            ;
    }
}
