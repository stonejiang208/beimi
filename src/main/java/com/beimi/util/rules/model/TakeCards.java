package com.beimi.util.rules.model;

import org.apache.commons.lang.ArrayUtils;

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
	private String userid ;
	private byte[] cards ;
	private long time ;
	private byte type ;		//出牌类型 ： 1:单张 | 2:对子 | 3:三张 | 4:四张（炸） | 5:单张连 | 6:连对 | 7:飞机 : 8:4带2 | 9:王炸
	
	private int cardsnum ;	//当前出牌的人 剩下多少张 牌
	
	private String nextplayer ;	// 下一个出牌玩家
	
	
	public TakeCards(){}
	public TakeCards(Player player){
		this.userid = player.getPlayuser() ;
		this.cards = getAIMostSmall(player, 0) ;
		switch(cards.length){
			case 1 : this.type = 1 ; break;	//单张,	暂时不做处理，将来扩充 连串
			case 2 : this.type = 2 ;break;	//对子,	暂时不做处理，将来扩充 连对
			case 3 : this.type = 3 ;this.cards = ArrayUtils.addAll(this.cards, this.getMostSmall(player, 0));break;	//三张,	可以带一张，补充处理规则，将来扩充飞机
			default: this.type = 4 ;break;	//炸弹, 不做处理 （AI的最后一手牌），其他牌型暂不做处理
		}
	}
	
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
	public byte getType() {
		return type;
	}
	public void setType(byte type) {
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
}
