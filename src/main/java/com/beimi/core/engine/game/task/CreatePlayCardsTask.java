package com.beimi.core.engine.game.task;

import org.apache.commons.lang3.StringUtils;
import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GameRoom;

/**
 * 出牌计时器，默认25秒，超时执行
 * @author zhangtianyi
 *
 */
public class CreatePlayCardsTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	private String player ;
	
	public CreatePlayCardsTask(long timer ,String userid, GameRoom gameRoom, String orgi){
		super();
		this.timer = timer ;
		this.gameRoom = gameRoom ;
		this.orgi = orgi ;
		this.player = userid ;
	}
	@Override
	public long getCacheExpiryTime() {
		return System.currentTimeMillis()+timer*1000;	//5秒后执行
	}
	
	public void execute(){
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(this.gameRoom.getId(), this.orgi);
		if(board!=null){
			if(!StringUtils.isBlank(player)){
				//超时了 ， 执行自动出牌
				Player player = super.player(board, this.player) ;
				if(board.getCurrent() == null){
					
				}
				/**
				 * 超时处理，如果当前是托管的或玩家超时，直接从最小的牌开始出，如果是 AI，则 需要根据AI级别（低级/中级/高级） 计算出牌 ， 目前先不管，直接从最小的牌开始出
				 */
				super.takecard(player, board.getCurrent()) ;
			}
		}
		CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, gameRoom.getOrgi());
		
		/**
		 * 发送一个 PlayCard 事件 , 下一个人出牌 ， 25秒内，如果未收到 玩家响应， 则直接 出牌，并计数  超时次数，超过超时次数的， 自动进入托管状态
		 */
		game.change(gameRoom , BeiMiGameEvent.PLAYCARDS.toString() , 25);	//通知状态机 , 此处应由状态机处理异步执行 ， 出牌时间由后台参数控制
	}
}
