package com.beimi.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGame;
import com.beimi.core.engine.game.model.Playway;
import com.beimi.core.engine.game.model.Type;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.model.AccountConfig;
import com.beimi.web.model.BeiMiDic;
import com.beimi.web.model.GameConfig;
import com.beimi.web.model.GamePlayway;
import com.beimi.web.model.GameRoom;
import com.beimi.web.model.PlayUser;
import com.beimi.web.model.PlayUserClient;
import com.beimi.web.model.SysDic;
import com.beimi.web.service.repository.jpa.GameConfigRepository;
import com.beimi.web.service.repository.jpa.GamePlaywayRepository;

public class GameUtils {
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
	public static Board playDizhuGame(List<PlayUserClient> playUsers , GameRoom gameRoom , String banker , int cardsnum){
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
		
		board.setRatio(15); 	//默认倍率 15
		int random = playUsers.size() * gameRoom.getCardsnum() ;
		
		board.setPosition((byte)new Random().nextInt(random));	//按照人数计算在随机界牌 的位置，避免出现在底牌里
		
		Player[] players = new Player[playUsers.size()];
		
		int inx = 0 ;
		for(PlayUserClient playUser : playUsers){
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
			tempPlayer.setCards(reverseCards(tempPlayer.getCards()));
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
	
	public static byte[] reverseCards(byte[] cards) {  
		byte[] target_cards = new byte[cards.length];  
		for (int i = 0; i < cards.length; i++) {  
			// 反转后数组的第一个元素等于源数组的最后一个元素：  
			target_cards[i] = cards[cards.length - i - 1];  
		}  
		return target_cards;  
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
    		
    		AccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI) ;
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
	/**
	 * 获取游戏全局配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	public static GameConfig gameConfig(String orgi){
		GameConfig gameConfig = (GameConfig) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.ConfigNames.GAMECONFIG.toString()+"."+orgi, orgi) ;
		if(gameConfig == null){
			List<GameConfig> gameConfigList = BMDataContext.getContext().getBean(GameConfigRepository.class).findByOrgi(orgi) ;
			if(gameConfigList.size() > 0){
				gameConfig = gameConfigList.get(0) ;
			}else{
				gameConfig = new GameConfig() ;
			}
		}
		return gameConfig ;
	}
	
	/**
	 * 获取游戏全局配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<GamePlayway> playwayConfig(String gametype,String orgi){
		List<GamePlayway> gamePlayList = (List<GamePlayway>) CacheHelper.getSystemCacheBean().getCacheObject(gametype+"."+BMDataContext.ConfigNames.PLAYWAYCONFIG.toString(), orgi) ;
		if(gamePlayList == null){
			gamePlayList = BMDataContext.getContext().getBean(GamePlaywayRepository.class).findByOrgiAndTypeid(orgi, gametype , new Sort(Sort.Direction.ASC, "sortindex")) ;
			CacheHelper.getSystemCacheBean().put(gametype+"."+BMDataContext.ConfigNames.PLAYWAYCONFIG.toString() , gamePlayList , orgi) ;
		}
		return gamePlayList ;
	}
	/**
	 * 
	 * @param gametype
	 * @param orgi
	 */
	public static void cleanPlaywayCache(String gametype,String orgi){
		CacheHelper.getSystemCacheBean().delete(gametype+"."+BMDataContext.ConfigNames.PLAYWAYCONFIG.toString(), orgi) ;
	}
	/**
	 * 封装Game信息，基于缓存操作
	 * @param gametype
	 * @return
	 */
	public static List<BeiMiGame> games(String gametype){
		List<BeiMiGame> beiMiGameList = new ArrayList<BeiMiGame>();
		if(!StringUtils.isBlank(gametype)){
			/**
			 * 找到游戏配置的 模式 和玩法，如果多选，则默认进入的是 大厅模式，如果是单选，则进入的是选场模式
			 */
			String[] games = gametype.split(",") ;
			for(String game : games){
				BeiMiGame beiMiGame = new BeiMiGame();
				for(SysDic sysDic : BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC)){
					if(sysDic.getId().equals(game)){
						beiMiGame.setName(sysDic.getName());
						beiMiGame.setId(sysDic.getId());
						beiMiGame.setCode(sysDic.getCode());
						
						List<SysDic> gameModelList = BeiMiDic.getInstance().getDic(BMDataContext.BEIMI_SYSTEM_GAME_TYPE_DIC, game) ;
						for(SysDic gameModel : gameModelList){
							Type type = new Type(gameModel.getId(), gameModel.getName() , gameModel.getCode()) ;
							beiMiGame.getTypes().add(type) ;
							List<GamePlayway> gamePlaywayList = playwayConfig(gameModel.getId(), gameModel.getOrgi()) ;
							for(GamePlayway gamePlayway : gamePlaywayList){
								Playway playway = new Playway(gamePlayway.getId(), gamePlayway.getName() , gamePlayway.getCode(), gamePlayway.getScore() , gamePlayway.getMincoins(), gamePlayway.getMaxcoins(), gamePlayway.isChangecard() , gamePlayway.isShuffle()) ;
								playway.setLevel(gamePlayway.getTypelevel());
								playway.setSkin(gamePlayway.getTypecolor());
								type.getPlayways().add(playway) ;
							}
						}
						beiMiGameList.add(beiMiGame) ;
					}
				}
			}
		}
		return beiMiGameList ;
	}
}
