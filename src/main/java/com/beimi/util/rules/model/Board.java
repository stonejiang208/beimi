package com.beimi.util.rules.model;

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
public class Board implements java.io.Serializable{
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	
	private byte[] cards;	//4个Bit描述一张牌，麻将：136+2/2 = 69 byte ; 扑克 54/2 = 27 byte 
	private Player[] players;//3~10人(4 byte)
	private String room ;		//房间ID（4 byte）
	
	private byte position ;		//地主牌
	
	private boolean docatch ;	//叫地主 OR 抢地主
	private int ratio ;			//倍数
	
	private String banker ;		//庄家|地主
	private String currplayer ;	//当前出牌人
	private byte currcard ;		//当前出牌
	
	public byte[] getCards() {
		return cards;
	}
	public void setCards(byte[] cards) {
		this.cards = cards;
	}
	public Player[] getPlayers() {
		return players;
	}
	public void setPlayers(Player[] players) {
		this.players = players;
	}
	public String getRoom() {
		return room;
	}
	public void setRoom(String room) {
		this.room = room;
	}
	public String getBanker() {
		return banker;
	}
	public void setBanker(String banker) {
		this.banker = banker;
	}
	public String getCurrplayer() {
		return currplayer;
	}
	public void setCurrplayer(String currplayer) {
		this.currplayer = currplayer;
	}
	public byte getCurrcard() {
		return currcard;
	}
	public void setCurrcard(byte currcard) {
		this.currcard = currcard;
	}
	public byte getPosition() {
		return position;
	}
	public void setPosition(byte position) {
		this.position = position;
	}
	public boolean isDocatch() {
		return docatch;
	}
	public void setDocatch(boolean docatch) {
		this.docatch = docatch;
	}
	public int getRatio() {
		return ratio;
	}
	public void setRatio(int ratio) {
		this.ratio = ratio;
	}
}
