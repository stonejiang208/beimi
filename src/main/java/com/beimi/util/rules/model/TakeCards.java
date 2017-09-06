package com.beimi.util.rules.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;

import com.beimi.core.engine.game.ActionTaskUtils;
import com.beimi.core.engine.game.CardType;

/**
 * 当前出牌信息
 * 出牌人
 * 牌
 * @author zhangtianyi
 *
 */
public class TakeCards implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8718778983090104033L;
	
	private String banker ;
	private boolean allow ;		//符合出牌规则 ， 
	private boolean donot ;		//出 OR 不出
	private String userid ;
	private byte[] cards ;
	private long time ;
	private int type ;		//出牌类型 ： 1:单张 | 2:对子 | 3:三张 | 4:四张（炸） | 5:单张连 | 6:连对 | 7:飞机 : 8:4带2 | 9:王炸
	private CardType cardType ;//出牌的牌型
	
	private boolean sameside ;	//同一伙
	
	private int cardsnum ;	//当前出牌的人 剩下多少张 牌
	
	private String nextplayer ;	// 下一个出牌玩家
	
	
	public TakeCards(){}
	
	/**
	 * 默认，自动出牌
	 * @param player
	 */
	public TakeCards(Player player){
		this.userid = player.getPlayuser() ;
		this.cards = getAIMostSmall(player, 0) ;
		this.cardType =  ActionTaskUtils.identification(cards);
		this.type = cardType.getCardtype() ;
		
		this.allow = true ;
		
		this.cardsnum = player.getCards().length ;
	}
	/**
	 * 最小出牌 ， 管住 last
	 * @param player
	 * @param last
	 */
	public TakeCards(Player player , TakeCards last){
		this.userid = player.getPlayuser() ;
		if(last != null){
			this.cards = this.search(player, last) ;
		}else{
			this.cards = getAIMostSmall(player, 0) ;
		}
		if(cards!=null){
			this.allow = true ;
			this.cardType =  ActionTaskUtils.identification(cards);
			this.type = cardType.getCardtype() ;
		}
		this.cardsnum = player.getCards().length ;
	}
	
	
	/**
	 * 
	 * 玩家出牌，不做校验，传入之前的校验结果
	 * @param player
	 * @param last
	 * @param cards
	 */
	public TakeCards(Player player , boolean allow , byte[] playCards){
		this.userid = player.getPlayuser() ;
		if(playCards == null){
			this.cards = getAIMostSmall(player, 0) ;
		}else{
			this.cards = playCards ;
			player.setCards(this.removeCards(player.getCards() , playCards));
		}
		this.cardType =  ActionTaskUtils.identification(cards);
		this.type = cardType.getCardtype() ;
		this.cardsnum = player.getCards().length ;
		this.allow = true;
	}
	
	/**
	 * 搜索符合条件的当前最小 牌型
	 * @param player
	 * @param last
	 * @return
	 */
	public byte[] search(Player player , TakeCards lastTakeCards){
		byte[] retValue = null ;
		Map<Integer,Integer> types = ActionTaskUtils.type(player.getCards()) ;
		if(lastTakeCards.getCardType().getTypesize() <= 3){//三带一 、 四带二
			if(lastTakeCards.getCardType().getCardnum() == 4 || lastTakeCards.getCardType().getCardnum() == 3){
				for(int i=lastTakeCards.getCardType().getMincard() + 1; i<14 ; i++){
					if(types.get(i) != null){	//找到能管得住的了 ， 再选 一张到两张配牌
						byte[] supplement = null ;
						Map<Integer,Integer> exist = new HashMap<Integer ,Integer>();
						exist.put(i, i) ;
						if(lastTakeCards.getCardType().getTypesize() == 1){
							supplement = this.getPair(player.getCards(), types , -1, 1, exist) ;
						}else{
							supplement = this.getSingle(player.getCards(), types, -1 , 2, exist) ;
						}
						if(supplement!=null){
							retValue = new byte[types.get(i)] ;
							int length = 0 ;
							for(int inx =0 ; inx < player.getCards().length ; inx++){
								if(player.getCards()[inx] / 4 == i){
									retValue[length++] = player.getCards()[inx] ;
								}
							}
							retValue = ArrayUtils.addAll(retValue, supplement) ;
						}
					}
				}
			}else if(lastTakeCards.getCardType().getCardnum() == 2){	//对子
				retValue = this.getPair(player.getCards(), types, lastTakeCards.getCardType().getMincard() ,1, new HashMap<Integer , Integer>()) ;
			}else{	//单张
				retValue = this.getSingle(player.getCards(), types, lastTakeCards.getCardType().getMincard(), 1, new HashMap<Integer , Integer>()) ;
			}
		}else{//单顺，双顺， 三顺
			
		}
		/**
		 * 有命中的牌型，当前玩家的手牌中移除
		 */
		if(retValue!=null){
			for(byte card : retValue){
				for(int i=0 ; i<player.getCards().length ; ){
					if(player.getCards()[i] == card){
						player.setCards(ArrayUtils.remove(player.getCards(), i)) ;
						continue ;
					}
					i++ ;
				}
			}
		}
		return retValue ;
	}
	/**
	 * 找到num对子
	 * @param num
	 * @return
	 */
	public byte[] getPair(byte[] cards , Map<Integer,Integer> types , int mincard , int num , Map<Integer,Integer> exist){
		return null ;
	}
	public byte[] getSingle(byte[] cards, Map<Integer,Integer> types , int mincard ,int num , Map<Integer,Integer> exist){
		byte[] retCards = null;
		List<Integer> retValue = new ArrayList<Integer>();
		for(int i=0 ; i<14 ; i++){
			if(types.get(i) != null && types.get(i) ==1  && retValue.size() < num && (i<0 || i>mincard) && !exist.containsKey(i)){
				retValue.add(i) ;
				exist.put(i, i) ;
			}
			if(retValue.size() == num){
				break ;
			}
		}
		if(retValue.size() < num){	//补充查找
			for(int i=0 ; i<14 ; i++){
				if(types.get(i) != null && types.get(i) >1 && (i<0 || i>mincard)  && retValue.size() < num){
					retValue.add(i) ;
					exist.put(i, i) ;
					if(retValue.size() == num){
						break ;
					}
				}
			}
		}
		if(retValue.size() == num){
			retCards = new byte[num] ;
			int inx = 0 ;
			for(int temp : retValue){
				for(byte card : cards){
					if(card/4 == temp){
						retCards[inx++] = card ;
					}
					if(inx >= num){
						break ;
					}
				}
			}
		}
		return retCards ;
	}
//	
//	public byte[] searchBegin(Player player , int card){
//		
//	}
	
	/**
	 * 找到机器人或托管的最小的牌
	 * @param cards
	 * @param start
	 * @return
	 */
	public byte[] getAIMostSmall(Player player , int start){
		Integer card = null;
		int index = 0 ;
		byte[] takeCards = null;
		for(int i = player.getCards().length - 1 - start; i>=0; i--){
			byte temp = player.getCards()[i] ;
			if(card == null){
				card = temp/4 ;
				index = i ;
			}else{
				if(card == temp/4){
					continue ;
				}else if((i - index) == 4 && (i+1) < player.getCards().length){	//炸弹，AI先不出，往下重新继续
					index = i ;
					card = null ;
					continue ;
				}else{
					takeCards = ArrayUtils.subarray(player.getCards(), i+1, player.getCards().length - start) ;
					player.setCards(this.removeCards(player.getCards(), i+1, player.getCards().length - start));
					break ;
				}
			}
		}
		return takeCards;
	}
	
	/**
	 * 找到托管玩家或超时玩家的最小的牌 ，不管啥牌，从最小的开始出
	 * @param cards
	 * @param start
	 * @return
	 */
	public byte[] getMostSmall(Player player, int start ){
		byte[] takeCards = null;
		if(player.getCards().length>0){
			takeCards = ArrayUtils.subarray(player.getCards(),player.getCards().length - 1,player.getCards().length) ;
			player.setCards(this.removeCards(player.getCards(), player.getCards().length - 1,player.getCards().length));
		}
		return takeCards ;
	}
	
	/**
	 * 移除出牌
	 * @param cards
	 * @param start
	 * @param end
	 * @return
	 */
	public byte[] removeCards(byte[] cards , int start , int end){
		byte[] retCards = new byte[cards.length - (end - start)] ;
		int inx = 0 ;
		for(int i=0; i<cards.length ; i++){
			if(i<start || i >= end){
				retCards[inx++] = cards[i] ;
			}
		}
		return retCards ;
	}
	
	/**
	 * 移除出牌
	 * @param cards
	 * @param start
	 * @param end
	 * @return
	 */
	public byte[] removeCards(byte[] cards , byte[] playcards){
		byte[] retCards = new byte[cards.length - playcards.length] ;
		int cardsindex = 0 ;
		for(int i=0; i<cards.length ; i++){
			boolean found = false ;
			for(int inx = 0 ;inx<playcards.length ; inx++){
				if(cards[i] == playcards[inx]){
					found = true ; break ;
				}
			}
			if(found == false){
				retCards[cardsindex++] = cards[i] ;
			}
		}
		return retCards ;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public byte[] getCards() {
		return cards;
	}
	public void setCards(byte[] cards) {
		this.cards = cards;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCardsnum() {
		return cardsnum;
	}
	public void setCardsnum(int cardsnum) {
		this.cardsnum = cardsnum;
	}
	public String getNextplayer() {
		return nextplayer;
	}
	public void setNextplayer(String nextplayer) {
		this.nextplayer = nextplayer;
	}

	public CardType getCardType() {
		return cardType;
	}

	public void setCardType(CardType cardType) {
		this.cardType = cardType;
	}

	public boolean isAllow() {
		return allow;
	}

	public void setAllow(boolean allow) {
		this.allow = allow;
	}

	public boolean isDonot() {
		return donot;
	}

	public void setDonot(boolean donot) {
		this.donot = donot;
	}

	public boolean isSameside() {
		return sameside;
	}

	public void setSameside(boolean sameside) {
		this.sameside = sameside;
	}

	public String getBanker() {
		return banker;
	}

	public void setBanker(String banker) {
		this.banker = banker;
	}
}
