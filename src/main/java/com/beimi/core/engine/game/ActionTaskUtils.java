package com.beimi.core.engine.game;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.task.AbstractTask;
import com.beimi.core.engine.game.task.dizhu.CreateAutoTask;
import com.beimi.util.UKTools;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.client.NettyClients;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.DuZhuBoard;
import com.beimi.util.rules.model.GamePlayers;
import com.beimi.util.rules.model.Player;
import com.beimi.util.server.handler.BeiMiClient;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUserClient;

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
	
	public static void sendEvent(String event, Message message,GameRoom gameRoom){
		message.setCommand(event);
		List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi()) ;
		for(PlayUserClient user : players){
			BeiMiClient client = NettyClients.getInstance().getClient(user.getId()) ;
			if(client!=null){
				client.getClient().sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, message);
			}
		}
	}
	
	public static void sendEvent(String event, String userid, Message message){
		message.setCommand(event);
		BeiMiClient client = NettyClients.getInstance().getClient(userid) ;
		if(client!=null){
			client.getClient().sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, message);
		}
	}
	
	/**
	 * 发送消息给 玩家
	 * @param beiMiClient
	 * @param event
	 * @param gameRoom
	 */
	public static void sendPlayers(BeiMiClient beiMiClient , GameRoom gameRoom){
		beiMiClient.getClient().sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, new GamePlayers(gameRoom.getPlayers() , CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), beiMiClient.getOrgi()), BMDataContext.BEIMI_PLAYERS_EVENT));
	}
	
	public static void sendPlayers(GameRoom gameRoom , List<PlayUserClient> players){
		for(PlayUserClient user : players){
			BeiMiClient client = NettyClients.getInstance().getClient(user.getId()) ;
			if(client!=null){
				client.getClient().sendEvent(BMDataContext.BEIMI_MESSAGE_EVENT, new GamePlayers(gameRoom.getPlayers() , CacheHelper.getGamePlayerCacheBean().getCacheObject(gameRoom.getId(), client.getOrgi()), BMDataContext.BEIMI_PLAYERS_EVENT));
			}
		}
	}
	
	
	/**
	 * 发送消息给 玩家
	 * @param beiMiClient
	 * @param event
	 * @param gameRoom
	 */
	public static void sendEvent(PlayUserClient playerUser  , Message message){
		NettyClients.getInstance().sendGameEventMessage(playerUser.getId(), BMDataContext.BEIMI_MESSAGE_EVENT , message);
	}
	
	/**
	 * 发送消息给 玩家
	 * @param beiMiClient
	 * @param event
	 * @param gameRoom
	 */
	public static void sendEvent(String userid  , Message message){
		NettyClients.getInstance().sendGameEventMessage(userid, BMDataContext.BEIMI_MESSAGE_EVENT , message);
	}
	
	public static PlayUserClient getPlayUserClient(String roomid,String player , String orgi){
		PlayUserClient playUserClient = null;
		List<PlayUserClient> players = CacheHelper.getGamePlayerCacheBean().getCacheObject(roomid, orgi) ;
		for(PlayUserClient user : players){
			if(player.equals(user.getId())){
				playUserClient = user ;
			}
		}
		return playUserClient;
	}
	
	public static Object json(Object data){
		return UKTools.json(data) ;
	}
	/**
	 * 临时放这里，重构的时候 放到 游戏类型的 实现类里 
	 * 抢地主的时候，首个抢地主 不翻倍
	 * @param board
	 * @param player
	 * @return
	 */
	public static DuZhuBoard doCatch(DuZhuBoard board, Player player , boolean result){
		player.setAccept(result); //抢地主
		player.setDocatch(true);
		board.setDocatch(true);
		if(result){	//抢了地主
			if(board.isAdded() == false){
				board.setAdded(true);
			}else{
				board.setRatio(board.getRatio()*2);
			}
			board.setBanker(player.getPlayuser());
		}
		return board ;
	}
	
	/**
	 * 临时放这里，重构的时候 放到 游戏类型的 实现类里
	 * @param board
	 * @param player
	 * @return
	 */
	public static void doBomb(Board board , boolean add){
		if(add){	//抢了地主
			board.setRatio(board.getRatio()*2);
		}
	}
	/**
	 * 校验当前出牌是否合规
	 * @param playCardType
	 * @param lastCardType
	 * @return
	 */
	public static boolean allow(CardType playCardType , CardType lastCardType){
		boolean allow = false ;
		if(playCardType.isKing()){	//王炸，无敌
			allow = true ;
		}else if(playCardType.isBomb()){
			if(lastCardType.isBomb()){ //都是炸弹
				if(playCardType.getMaxcard() > lastCardType.getMaxcard()){
					allow = true ;
				}
			}else if(lastCardType.isKing()){
				allow = false ;
			}else{
				allow = true ;
			}
		}else if(lastCardType.isBomb()){	//最后一手牌是炸弹 ， 当前出牌不是炸弹
			allow = false ;
		}else if(playCardType.getCardtype() == lastCardType.getCardtype() && playCardType.getCardtype()>0 && lastCardType.getCardtype() > 0){
			if(playCardType.getMaxcard() > lastCardType.getMaxcard()){
				allow = true ;
			}else if(playCardType.getMaxcardvalue() == 53){
				allow = true ;
			}
		}
		return allow ;
	}
	/**
	 * 分类
	 * @param cards
	 * @return
	 */
	public static Map<Integer , Integer> type(byte[] cards){
		Map<Integer,Integer> types = new HashMap<Integer,Integer>();
		for(int i=0 ; i<cards.length ; i++){
			int card = cards[i]/4 ;
			if(types.get(card) == null){
				types.put(card, 1) ;
			}else{
				types.put(card, types.get(card)+1) ;
			}
		}
		return types ;
	}
	
	/**
	 * 牌型识别
	 * @param cards
	 * @return
	 */
	public static CardType identification(byte[] cards){
		CardType cardTypeBean = new CardType();
		Map<Integer,Integer> types = new HashMap<Integer,Integer>();
		int max = -1 , maxcard = -1 , cardtype = 0 , mincard = -1 , min = 100;
		for(int i=0 ; i<cards.length ; i++){
			int card = cards[i]/4 ;
			if(types.get(card) == null){
				types.put(card, 1) ;
			}else{
				types.put(card, types.get(card)+1) ;
			}
			if(types.get(card) > max){
				max = types.get(card) ;
				maxcard = card ;
			}
			if(cards[i] > cardTypeBean.getMaxcardvalue()){
				cardTypeBean.setMaxcardvalue(cards[i]);
			}
			if(mincard < 0 || mincard > card){
				mincard = card ;
			}
		}
		
		Iterator<Integer> iterator = types.keySet().iterator() ;
		while(iterator.hasNext()){
			Integer key = iterator.next() ;
			if(types.get(key) < min){
				min = types.get(key) ;
			}
		}
		
		cardTypeBean.setCardnum(max);
		cardTypeBean.setMincard(mincard);
		cardTypeBean.setTypesize(types.size());
		cardTypeBean.setMaxcard(maxcard);
		
		
		switch(types.size()){
			case 1 : 
				switch(max){
					case 1 : cardtype = BMDataContext.CardsTypeEnum.ONE.getType() ;break;		//单张
					case 2 : 
						if(mincard == 13){
							cardtype = BMDataContext.CardsTypeEnum.ELEVEN.getType();
						}else{
							cardtype = BMDataContext.CardsTypeEnum.TWO.getType() ;
						}
						break;		//一对
					case 3 : cardtype = BMDataContext.CardsTypeEnum.THREE.getType() ;break;		//三张
					case 4 : cardtype = BMDataContext.CardsTypeEnum.TEN.getType() ;break;		//炸弹
				}
				;break ;
			case 2 :
				switch(max){
					case 3 :
						if(min == 1){//三带一
							cardtype = BMDataContext.CardsTypeEnum.FOUR.getType() ;
						}else if(min == 2){//三带一对
							cardtype = BMDataContext.CardsTypeEnum.FORMTWO.getType() ;
						}
						break;	
					case 4 : cardtype = BMDataContext.CardsTypeEnum.NINE.getType() ;break;	//四带一对
				}
				;break ;
			case 3 : 
				switch(max){
					case 1 : ;break;	//无牌型
					case 2 : if(cards.length == 6 && isAva(types, mincard)){cardtype = BMDataContext.CardsTypeEnum.SIX.getType() ;}break;		//双顺 ， 3连对
					case 3 : if(isAva(types, mincard) && min == max){cardtype = BMDataContext.CardsTypeEnum.SEVEN.getType() ;}break;		//三顺
					case 4 : cardtype = BMDataContext.CardsTypeEnum.SEVEN.getType() ;break;		//四带二
				}
				break;
			case 4 : 
				switch(max){
					case 1 : ;break;		//无牌型
					case 2 : if(cards.length == 8 && isAva(types, mincard)){cardtype = BMDataContext.CardsTypeEnum.SIX.getType() ;}break;		//双顺 ， 4连对
					case 3 : if((cards.length == 8 || cards.length == 10) && isAva(types, mincard)){cardtype = BMDataContext.CardsTypeEnum.SEVEN.getType() ;}break;		//双顺 ， 4连对
				};break ;
			case 5 : 
				switch(max){
					case 1 : if(isAva(types ,mincard) && max == min){cardtype = BMDataContext.CardsTypeEnum.FIVE.getType() ;}break;		//连子
					case 2 : if(cards.length == 10 && isAva(types, mincard)){cardtype = BMDataContext.CardsTypeEnum.SIX.getType() ;}break;		//5连对
					case 3 : if(isAva(types, mincard) && max == min){cardtype = BMDataContext.CardsTypeEnum.SEVEN.getType() ;}break;		//5飞机
				};break ;
			case 6 : 
				switch(max){
					case 1 : if(isAva(types ,mincard) && max == min){cardtype = BMDataContext.CardsTypeEnum.FIVE.getType() ;}break;		//连子
					case 2 : if(isAva(types ,mincard) && max == min){cardtype = BMDataContext.CardsTypeEnum.SIX.getType() ;}break;		//6连对
					case 3 : if(isAva(types ,mincard) && max == min){cardtype = BMDataContext.CardsTypeEnum.SEVEN.getType() ;}break;		//6飞机
				};break ;
			default: 
				switch(max){
					case 1 : if(isAva(types ,mincard)){cardtype = BMDataContext.CardsTypeEnum.FIVE.getType() ;}break;		//连子
					case 2 : if(isAva(types ,mincard) && max == min){cardtype = BMDataContext.CardsTypeEnum.SIX.getType() ;}break;		//连对
				};break ;
		}
		cardTypeBean.setCardtype(cardtype);
		cardTypeBean.setKing(cardtype == BMDataContext.CardsTypeEnum.ELEVEN.getType());
		cardTypeBean.setBomb(cardtype == BMDataContext.CardsTypeEnum.TEN.getType());
		return cardTypeBean ;
	}
	
	private static boolean isAva(Map<Integer,Integer> types , int mincard){
		boolean ava = true ;
		for(int i=mincard ; i<(mincard + types.size())  ; i++){
			if(types.get(i) == null){
				ava = false  ;
			}
		}
		return ava ;
	}
}
