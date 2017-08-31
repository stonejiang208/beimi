package com.beimi.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;

import com.beimi.core.BMDataContext;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.GameAccountConfig;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.service.repository.jpa.GamePlaywayRepository;

public class GameUtils {
	@SuppressWarnings("unchecked")
	public static List<GamePlayway> cacheGamePlayway(String orgi){
		List<GamePlayway> gamePlaywayList = (List<GamePlayway>) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.BEIMI_GAME_PLAYWAY+"."+orgi, orgi) ; 
		if(gamePlaywayList == null){
			GamePlaywayRepository gamePlayRes = BMDataContext.getContext().getBean(GamePlaywayRepository.class) ;
			gamePlaywayList = gamePlayRes.findByOrgi(orgi) ;
			CacheHelper.getSystemCacheBean().put(BMDataContext.BEIMI_GAME_PLAYWAY+"."+orgi , gamePlaywayList, orgi) ;
		}
		return gamePlaywayList;
	}
	/**
	 * 移除GameRoom
	 * @param gameRoom
	 * @param orgi
	 */
	public static void removeGameRoom(String roomid,String orgi){
		GameRoom tempGameRoom ;
		while((tempGameRoom = (GameRoom) CacheHelper.getQueneCache().poll(orgi)) != null){
			if(tempGameRoom.getId().equals(roomid)){
				break ;		//拿走，不排队了，开始增加AI
			}else{
				CacheHelper.getQueneCache().offer(tempGameRoom , orgi) ;	//还回去
			}
		}
	}
	
	/**
	 * 创建一个AI玩家
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static PlayUserClient create(PlayUser player,String playertype) {
		return create(player, null , null , playertype) ;
	}
	/**
	 * 开始斗地主游戏
	 * @return
	 */
	public static Board playDizhuGame(Collection<Object> playUsers , GameRoom gameRoom , String banker , int cardsnum){
		Board board = new Board() ;
		board.setCards(null);
		List<Byte> temp = new ArrayList<Byte>() ;
		for(int i= 0 ; i<54 ; i++){
			temp.add((byte)i) ;
		}
		Collections.shuffle(temp);
		byte[] cards = new byte[54] ;
		for(int i=0 ; i<temp.size() ; i++){
			cards[i] = temp.get(i) ;
		}
		board.setCards(cards);
		board.setPosition((byte)new Random().nextInt(55));
		
		Player[] players = new Player[playUsers.size()];
		
		Iterator<Object> playerIter = playUsers.iterator() ;
		int inx = 0 ;
		while(playerIter.hasNext()){
			PlayUserClient playUser = (PlayUserClient) playerIter.next() ;
			Player player = new Player(playUser.getId()) ;
			player.setCards(new byte[cardsnum]);
			players[inx++] = player ;
		}
		for(int i = 0 ; i<gameRoom.getCardsnum()*gameRoom.getPlayers(); i++){
			int pos = i%players.length ; 
			players[pos].getCards()[i/players.length] = cards[i] ;
			if(i == board.getPosition()){
				players[pos].setRandomcard(true);		//起到地主牌的人
			}
		}
		for(Player tempPlayer : players){
			Arrays.sort(tempPlayer.getCards());
			StringBuffer strb = new StringBuffer() ;
			for(byte v : tempPlayer.getCards()){
				strb.append(",").append(v) ;
			}
		}
		board.setRoom(gameRoom.getId());
		Player tempbanker = players[0];
		if(!StringUtils.isBlank(banker)){
			for(int i= 0 ; i<players.length ; i++){
				Player player = players[i] ;
				if(player.equals(banker)){
					if(i < (players.length - 1)){
						tempbanker = players[i+1] ;
					}
				}
			}
			
		}
		board.setPlayers(players);
		if(tempbanker!=null){
			board.setBanker(tempbanker.getPlayuser());
		}
		return board;
	}
	
	/**
	 * 创建一个普通玩家
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static PlayUserClient create(PlayUser player , IP ipdata , HttpServletRequest request ) throws IllegalAccessException, InvocationTargetException{
		return create(player, ipdata, request, BMDataContext.PlayerTypeEnum.NORMAL.toString()) ;
	}
	
	/**
	 * 注册用户
	 * @param player
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalAccessException 
	 */
	public static PlayUserClient create(PlayUser player , IP ipdata , HttpServletRequest request , String playertype){
		PlayUserClient playUserClient = null ;
		if(player!= null){
    		if(StringUtils.isBlank(player.getUsername())){
    			player.setUsername("Guest_"+Base62.encode(UKTools.getUUID().toLowerCase()));
    		}
    		if(!StringUtils.isBlank(player.getPassword())){
    			player.setPassword(UKTools.md5(player.getPassword()));
    		}else{
    			player.setPassword(UKTools.md5(RandomKey.genRandomNum(6)));//随机生成一个6位数的密码 ，备用
    		}
    		player.setPlayertype(playertype);	//玩家类型
    		player.setCreatetime(new Date());
    		player.setUpdatetime(new Date());
    		player.setLastlogintime(new Date());
    		
    		BrowserClient client = UKTools.parseClient(request) ;
    		player.setOstype(client.getOs());
    		player.setBrowser(client.getBrowser());
    		if(request!=null){
	    		String usetAgent = request.getHeader("User-Agent") ;
	    		if(!StringUtils.isBlank(usetAgent)){
	    			if(usetAgent.length() > 255){
	    				player.setUseragent(usetAgent.substring(0,250));
	    			}else{
	    				player.setUseragent(usetAgent);
	    			}
	    		}
    		}
    		if(ipdata!=null){
	    		player.setRegion(ipdata.getRegion());
				player.setCountry(ipdata.getCountry());
				player.setProvince(ipdata.getProvince());
				player.setCity(ipdata.getCity());
				player.setIsp(ipdata.getIsp());
    		}
			
    		
    		player.setOrgi(BMDataContext.SYSTEM_ORGI);
    		
    		GameAccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI) ;
    		if(config!=null){
    			player.setGoldcoins(config.getInitcoins());
    			player.setCards(config.getInitcards());
    			player.setDiamonds(config.getInitdiamonds());
    		}
    		
    		if(!StringUtils.isBlank(player.getId())){
    			playUserClient  = new PlayUserClient() ;
    			try {
					BeanUtils.copyProperties(playUserClient , player);
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
    		}
    	}
		return playUserClient ;
	}
}
