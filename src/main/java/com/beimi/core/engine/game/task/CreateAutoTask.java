package com.beimi.core.engine.game.task;

import org.cache2k.expiry.ValueWithExpiryTime;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGameEvent;
import com.beimi.core.engine.game.BeiMiGameTask;
import com.beimi.core.engine.game.GameBoard;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GameRoom;
import com.corundumstudio.socketio.SocketIOServer;

/**
 * 抢地主
 * @author iceworld
 *
 */
public class CreateAutoTask extends AbstractTask implements ValueWithExpiryTime  , BeiMiGameTask{

	private long timer  ;
	private GameRoom gameRoom = null ;
	private String orgi ;
	
	public CreateAutoTask(long timer , GameRoom gameRoom, String orgi){
		super();
		this.timer = timer ;
		this.gameRoom = gameRoom ;
		this.orgi = orgi ;
	}
	@Override
	public long getCacheExpiryTime() {
		return System.currentTimeMillis()+timer*1000;	//5秒后执行
	}
	
	public void execute(){
		Board board = (Board) CacheHelper.getBoardCacheBean().getCacheObject(gameRoom.getId(), gameRoom.getOrgi());
		Player randomCardPlayer = null , catchPlayer = null;
		int index = 0 ;
		if(board!=null){
			/**
			 * 抢地主，首个抢地主的人 在发牌的时候已经生成
			 */
			for(Player player : board.getPlayers()){
				index++;
				if(player.isRandomcard()){
					randomCardPlayer = player ;
					break ;
				}else if(randomCardPlayer == null){
					randomCardPlayer = player ;
				}
			}
			if(randomCardPlayer.isDocatch()){
				catchPlayer = next(board , index);
			}else{
				catchPlayer = randomCardPlayer ; 
			}
			if(catchPlayer == null && randomCardPlayer.isAccept() && !board.getBanker().equals(randomCardPlayer.getPlayuser())){
				//
				catchPlayer = randomCardPlayer ;	//起到地主牌的人第二次抢地主 ， 抢完就结束了
			}
		}
		/**
		 * 地主抢完了即可进入玩牌的流程了，否则，一直发送 AUTO事件，进行抢地主
		 */
		if(catchPlayer!=null){
			catchPlayer.setDocatch(true);//抢过了
//			board.setBanker(catchPlayer.getPlayuser());	//玩家 点击 抢地主按钮后 赋值
			CacheHelper.getBoardCacheBean().put(gameRoom.getId(), board, orgi);
			BMDataContext.getContext().getBean(SocketIOServer.class).getRoomOperations(gameRoom.getId()).sendEvent("catch", super.json( new GameBoard(catchPlayer.getPlayuser() , board.isDocatch() , board.getRatio()))) ;
			game.change(gameRoom , BeiMiGameEvent.AUTO.toString() , 15);	//通知状态机 , 此处应由状态机处理异步执行
		}else{
			//开始打牌，地主的人是最后一个抢了地主的人
			game.change(gameRoom , BeiMiGameEvent.RAISEHANDS.toString());	//通知状态机 , 全部都抢过地主了 ， 把底牌发给 最后一个抢到地主的人
		}
	}
	/**
	 * 找到下一个抢地主的人
	 * @param board
	 * @param index
	 * @return
	 */
	private Player next(Board board , int index){
		Player catchPlayer = null;
		if(index > board.getPlayers().length){
			index = 0 ;
		}
		for(int i= index ; i<board.getPlayers().length ; i++){
			Player player = board.getPlayers()[i] ;
			if(player.isDocatch() == false){
				catchPlayer = player ;
				break ;
			}else if(player.isRandomcard()){
				break ;
			}else if((i+1) >= board.getPlayers().length){
				i=0 ;
			}
		}
		return catchPlayer;
	}
}
