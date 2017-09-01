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
}
