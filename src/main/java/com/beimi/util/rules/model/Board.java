package com.beimi.util.rules.model;

import org.apache.commons.lang.ArrayUtils;

/**
 * 牌局，用于描述当前牌局的内容 ， 
 * 1、随机排序生成的 当前 待起牌（麻将、德州有/斗地主无）
 * 2、玩家 手牌
 * 3、玩家信息
 * 4、当前牌
 * 5、当前玩家
 * 6、房间/牌桌信息
 * 7、其他附加信息
 * 数据结构内存占用 78 byte ， 一副牌序列化到 数据库 占用的存储空间约为 78 byt， 数据库字段长度约为 20
 *
 * @author iceworld
 *
 */
public class Board extends AbstractBoard implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6143646772231515350L;

	/**
	 * 翻底牌 ， 斗地主
	 */
	@Override
	public byte[] pollLastHands() {
		return ArrayUtils.subarray(this.getCards() , this.getCards().length - 3 , this.getCards() .length);
	}

	/**
	 * 暂时不做处理，根据业务规则修改，例如：底牌有大王翻两倍，底牌有小王 翻一倍，底牌是顺子 翻两倍 ====
	 */
	@Override
	public int calcRatio() {
		return 1;
	}

	@Override
	public TakeCards takeCards(Player player , String playerType, TakeCards current) {
		return new TakeCards(player);
	}
	
	
	/**
	 * 找到玩家
	 * @param board
	 * @param userid
	 * @return
	 */
	public Player player(String userid){
		Player target = null ;
		for(Player temp : this.getPlayers()){
			if(temp.getPlayuser().equals(userid)){
				target = temp ; break ;
			}
		}
		return target ;
	}
	
	/**
	 * 找到玩家的 位置
	 * @param board
	 * @param userid
	 * @return
	 */
	public int index(String userid){
		int index = 0;
		for(int i=0 ; i<this.getPlayers().length ; i++){
			Player temp = this.getPlayers()[i] ;
			if(temp.getPlayuser().equals(userid)){
				index = i ; break ;
			}
		}
		return index ;
	}
	
	
	/**
	 * 找到下一个玩家
	 * @param board
	 * @param index
	 * @return
	 */
	public Player next(int index){
		Player catchPlayer = null;
		if(index == 0 && this.getPlayers()[index].isRandomcard()){	//fixed
			index = this.getPlayers().length - 1 ;
		}
		for(int i = index ; i>=0 ; i--){
			Player player = this.getPlayers()[i] ;
			if(player.isDocatch() == false){
				catchPlayer = player ;
				break ;
			}else if(player.isRandomcard()){	//重新遍历一遍，发现找到了地主牌的人，终止查找
				break ;
			}else if(i == 0){
				i = this.getPlayers().length;
			}
		}
		return catchPlayer;
	}
	

	public Player nextPlayer(int index) {
		if(index == 0){
			index = this.getPlayers().length - 1 ;
		}else{
			index = index - 1 ;
		}
		return this.getPlayers()[index];
	}
	/**
	 * 
	 * @param player
	 * @param current
	 * @return
	 */
	public TakeCards takecard( Player player , boolean allow , byte[] playCards) {
		return new TakeCards(player , allow , playCards);
	}
	
	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 * @param player
	 * @param current
	 * @return
	 */
	public TakeCards takecard(Player player) {
		return new TakeCards(player);
	}
	
	/**
	 * 当前玩家随机出牌，能管住当前出牌的 最小牌
	 * @param player
	 * @param current
	 * @return
	 */
	public TakeCards takecard(Player player , TakeCards last) {
		return new TakeCards(player, last);
	}

	@Override
	public boolean isWin() {
		boolean win = false ;
		if(this.getLast()!=null && this.getLast().getCardsnum() == 0){//出完了
			win = true ;
		}
		return win;
	}
	
}
