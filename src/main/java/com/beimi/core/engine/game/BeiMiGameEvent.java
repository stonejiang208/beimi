package com.beimi.core.engine.game;

/**
 * 所有棋牌类游戏 的基本状态
 * 根据游戏类型不同，状态下的事件有所不同
 * @author chenhao
 *
 */
public enum BeiMiGameEvent {
	ENTER,
	BEGIN ,		//开始
	JOIN,		//成员加入
	ENOUGH,		//凑够一桌子
	RAISEHANDS,	//所有成员举手
	ALLCARDS;	//1、单个玩家打完牌（地主，推到胡）；2、打完桌面的所有牌（血战，血流，德州）
}
