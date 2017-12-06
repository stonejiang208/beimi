package com.beimi.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.beimi.web.model.*;
import com.beimi.web.service.repository.jpa.GamePlaywayGroupItemRepository;
import com.beimi.web.service.repository.jpa.GamePlaywayGroupRepository;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import com.beimi.config.web.model.Game;
import com.beimi.core.BMDataContext;
import com.beimi.core.engine.game.BeiMiGame;
import com.beimi.core.engine.game.iface.ChessGame;
import com.beimi.core.engine.game.impl.DizhuGame;
import com.beimi.core.engine.game.impl.MaJiangGame;
import com.beimi.core.engine.game.model.MJCardMessage;
import com.beimi.core.engine.game.model.Playway;
import com.beimi.core.engine.game.model.Type;
import com.beimi.util.cache.CacheHelper;
import com.beimi.util.rules.model.Action;
import com.beimi.util.rules.model.Board;
import com.beimi.util.rules.model.Player;
import com.beimi.web.service.repository.jpa.GamePlaywayRepository;

public class GameUtils {
	
	private static Map<String,ChessGame> games = new HashMap<String,ChessGame>();
	static{
		games.put("dizhu", new DizhuGame()) ;
		games.put("majiang", new MaJiangGame()) ;
	}
	
	public static Game getGame(String playway ,String orgi){
		GamePlayway gamePlayway = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(playway, orgi) ;
		Game game = null ;
		if(gamePlayway!=null){
			SysDic dic = (SysDic) CacheHelper.getSystemCacheBean().getCacheObject(gamePlayway.getGame(), gamePlayway.getOrgi()) ;
			if(dic.getCode().equals("dizhu")){
				game = (Game) BMDataContext.getContext().getBean("dizhuGame") ;
			}else if(dic.getCode().equals("majiang")){
				game = (Game) BMDataContext.getContext().getBean("majiangGame") ;
			}
		}
		return game;
	}
	
	/**
	 * 移除GameRoom
	 * @param gameRoom
	 * @param orgi
	 */
	public static void removeGameRoom(String roomid,String playway,String orgi){
		CacheHelper.getQueneCache().delete(roomid);
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
	 * 开始游戏，根据玩法创建游戏 对局
	 * @return
	 */
	public static Board playGame(List<PlayUserClient> playUsers , GameRoom gameRoom , String banker , int cardsnum){
		Board board = null ;
		GamePlayway gamePlayWay = (GamePlayway) CacheHelper.getSystemCacheBean().getCacheObject(gameRoom.getPlayway(), gameRoom.getOrgi()) ;
		if(gamePlayWay!=null){
			ChessGame chessGame = games.get(gamePlayWay.getCode());
			if(chessGame!=null){
				board = chessGame.process(playUsers, gameRoom, gamePlayWay , banker, cardsnum);
			}
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
    		AiConfig aiConfig = CacheConfigTools.getAiConfig(player.getOrgi()) ;
    		
			if(BMDataContext.PlayerTypeEnum.AI.toString().equals(playertype) && aiConfig != null){
				player.setGoldcoins(aiConfig.getInitcoins());
    			player.setCards(aiConfig.getInitcards());
    			player.setDiamonds(aiConfig.getInitdiamonds());
			}else{
	    		AccountConfig config = CacheConfigTools.getGameAccountConfig(BMDataContext.SYSTEM_ORGI) ;
	    		if(config!=null){
	    			player.setGoldcoins(config.getInitcoins());
	    			player.setCards(config.getInitcards());
	    			player.setDiamonds(config.getInitdiamonds());
	    		}
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
	 * 获取房卡游戏的自定义配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<GamePlaywayGroup> playwayGroupsConfig(String orgi){
		List<GamePlaywayGroup> gamePlaywayGroupsList = (List<GamePlaywayGroup>) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.ConfigNames.PLAYWAYGROUP.toString(), orgi) ;
		if(gamePlaywayGroupsList == null){
			gamePlaywayGroupsList = BMDataContext.getContext().getBean(GamePlaywayGroupRepository.class).findByOrgi(orgi, new Sort(Sort.Direction.ASC, "sortindex")) ;
			CacheHelper.getSystemCacheBean().put(BMDataContext.ConfigNames.PLAYWAYGROUP.toString() , gamePlaywayGroupsList , orgi) ;
		}
		return gamePlaywayGroupsList ;
	}

	/**
	 * 获取房卡游戏的自定义配置，后台管理界面上的配置功能
	 * @param orgi
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<GamePlaywayGroupItem> playwayGroupItemConfig(String orgi){
		List<GamePlaywayGroupItem> gamePlaywayGroupsList = (List<GamePlaywayGroupItem>) CacheHelper.getSystemCacheBean().getCacheObject(BMDataContext.ConfigNames.PLAYWAYGROUPITEM.toString(), orgi) ;
		if(gamePlaywayGroupsList == null){
			gamePlaywayGroupsList = BMDataContext.getContext().getBean(GamePlaywayGroupItemRepository.class).findByOrgi(orgi, new Sort(Sort.Direction.ASC, "sortindex")) ;
			CacheHelper.getSystemCacheBean().put(BMDataContext.ConfigNames.PLAYWAYGROUPITEM.toString() , gamePlaywayGroupsList , orgi) ;
		}
		return gamePlaywayGroupsList ;
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

							List<GamePlaywayGroup> gamePlaywayGroups = playwayGroupsConfig(gameModel.getOrgi()) ;
							List<GamePlaywayGroupItem> gamePlaywayGroupItems = playwayGroupItemConfig(gameModel.getOrgi()) ;


							for(GamePlayway gamePlayway : gamePlaywayList){
								Playway playway = new Playway(gamePlayway.getId(), gamePlayway.getName() , gamePlayway.getCode(), gamePlayway.getScore() , gamePlayway.getMincoins(), gamePlayway.getMaxcoins(), gamePlayway.isChangecard() , gamePlayway.isShuffle()) ;
								playway.setLevel(gamePlayway.getTypelevel());

								playway.setGroups(new ArrayList<GamePlaywayGroup>());
								playway.setItems(new ArrayList<GamePlaywayGroupItem>());

								for(GamePlaywayGroup group : gamePlaywayGroups){
									if(group.getPlaywayid().equals(gamePlayway.getId())){
										playway.getGroups().add(group) ;
									}
								}

								for(GamePlaywayGroupItem item : gamePlaywayGroupItems){
									if(item.getPlaywayid().equals(gamePlayway.getId())){
										playway.getItems().add(item) ;
									}
								}

								playway.setSkin(gamePlayway.getTypecolor());
								playway.setMemo(gamePlayway.getMemo());
                                playway.setRoomtitle(gamePlayway.getRoomtitle());
								playway.setFree(gamePlayway.isFree());
								playway.setExtpro(gamePlayway.isExtpro());
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
	
//	public static void main(String[] args){
//		long start = System.nanoTime() ;
//		for(int i=0 ; i<1 ; i++){
//			byte[] cards = new byte[]{7, 12, 13, 14, 77,81, 87, 90,  95, 97, 97, 100, 105} ;
//			byte takecard = 6 ;
//			List<Byte> test = new ArrayList<Byte>();
//			for(byte temp : cards){
//				test.add(temp) ;
//			}
//			test.add(takecard) ;
//			Collections.sort(test);
//			for(byte temp : test){
//				int value = (temp%36) / 4 ;			//牌面值
//				int rote = temp / 36 ;				//花色
//				System.out.print(value+1);
//				if(rote == 0){
//					System.out.print("万,");
//				}else if(rote == 1){
//					System.out.print("筒,");
//				}else if(rote == 2){
//					System.out.print("条,");
//				}
//			}
//			
//			processMJCard("USER1", cards, takecard, false) ;
//		}
//		long end = System.nanoTime() - start ;
//		System.out.println("判断100W次胡牌花费时间："+(end)+"纳秒，约等于："+ end/1000000f+"ms");
//	}
	/**
	 * 麻将的出牌判断，杠碰吃胡
	 * @param cards
	 * @param card
	 * @param deal	是否抓牌
	 * @return
	 */
	public static MJCardMessage processMJCard(Player player,byte[] cards , byte takecard , boolean deal){
		MJCardMessage mjCard = new MJCardMessage();
		mjCard.setCommand("action");
		mjCard.setUserid(player.getPlayuser());
		Map<Integer, Byte> data = new HashMap<Integer, Byte>();
		boolean que = false ;
		if(cards.length > 0){
			for(byte temp : cards){
				int value = (temp%36) / 4 ;			//牌面值
				int rote = temp / 36 ;				//花色
				int key = value + 9 * rote ;		//
				if(rote == player.getColor()){
					que = true ;
				}
				if(data.get(key) == null || rote == player.getColor()){
					data.put(key , (byte)1) ;
				}else{
					data.put(key, (byte)(data.get(key)+1)) ;
				}
				
				if(data.get(key) == 4 && deal == true){	//自己发牌的时候，需要先判断是否有杠牌
					mjCard.setGang(true);
					mjCard.setCard(temp);
				}
			}
			/**
			 * 检查是否有 杠碰
			 */
			int value = (takecard %36)/4 ;
			int key = value + 9*(takecard/36) ;
			Byte card = data.get(key) ;
			if(card!=null){
				if(card ==2 && deal == false){
					//碰
					mjCard.setPeng(true);
					mjCard.setCard(takecard);
				}else if(card == 3){
					//明杠
					mjCard.setGang(true);
					mjCard.setCard(takecard);
				}
			}
			
			/**
			 * 检查是否有弯杠 , 碰过 AND 自己抓了一张碰过的牌
			 */
			int rote = takecard  / 36 ;
			if(deal == true && rote!= player.getColor() ){
				for(Action action : player.getActions()){
					if(action.getCard() == takecard && action.getAction().equals(BMDataContext.PlayerAction.PENG.toString())){
						//
						mjCard.setGang(true); break ;
					}
				}
			}
			/**
			 * 后面胡牌判断使用
			 */
			if(data.get(key) == null){
				data.put(key , (byte)1) ;
			}else{
				data.put(key, (byte)(data.get(key)+1)) ;
			}
		}
		if(que == false){
			/**
			 * 检查是否有 胡 , 胡牌算法，先移除 对子
			 */
			List<Byte> pairs = new ArrayList<Byte>();
			List<Byte> others = new ArrayList<Byte>();
			List<Byte> kezi = new ArrayList<Byte>();
			/**
			 * 处理玩家手牌
			 */
			for(byte temp : cards){
				int key = (((temp%36) / 4) + 9 * (int)(temp / 36)) ;			//字典编码
				if(data.get(key) == 1 ){
					others.add(temp) ;
				}else if(data.get(key) == 2){
					pairs.add(temp) ;
				}else if(data.get(key) == 3){
					kezi.add(temp) ;
				}
			}
			/**
			 * 处理一个单张
			 */
			{
				int key = (((takecard%36) / 4) + 9 * (int)(takecard / 36)) ;			//字典编码
				if(data.get(key) == 1 ){
					others.add(takecard) ;
				}else if(data.get(key) == 2){
					pairs.add(takecard) ;
				}else if(data.get(key) == 3){
					kezi.add(takecard) ;
				}
			}
			/**
			 * 是否有胡
			 */
			processOther(others);
			
			if(others.size() == 0){
				if(pairs.size() == 2 || pairs.size() == 14){//有一对，胡
					mjCard.setHu(true);
				}else{	//然后分别验证 ，只有一种特殊情况，的 3连对，可以组两个顺子，也可以胡 ， 其他情况就呵呵了
					
				}
			}else if(pairs.size() > 2){	//对子的牌大于>张，否则肯定是不能胡的
				//检查对子里 是否有额外多出来的 牌，如果有，则进行移除
				for(int i=0 ; i<pairs.size() ; i++){
					if(i%2==0){
						others.add(pairs.get(i)) ;
					}
				}
				processOther( others);
				
				for(int i=0 ; i<pairs.size() ; i++){
					if(i%2==1){
						others.add(pairs.get(i)) ;
					}
				}
				
				processOther(others);
				
				/**
				 * 检查 others
				 */
				/**
				 * 最后一次，检查所有的值都是 2，就胡了
				 */
				if(others.size() == 2 && getKey(others.get(0)) == getKey(others.get(1))){
					mjCard.setHu(true);
				}else{	//还不能胡？
					
				}
			}
		}
		if(mjCard.isHu()){
			mjCard.setCard(takecard);
			System.out.println("胡牌了");
			for(byte temp : cards){
				System.out.print(temp+",");
			}
			System.out.println(takecard);
		}
		return mjCard;
	}
	
	private static void processOther(List<Byte> others){
		Collections.sort(others);
		for(int i=0 ; i<others.size() && others.size() >(i+2) ; ){
			byte color = (byte) (others.get(i) / 36) ;							//花色
			byte key = getKey(others.get(i));
			byte nextcolor = (byte) (others.get(i) / 36) ;							//花色
			byte nextkey = getKey(others.get(i+1));
			if(color == nextcolor && nextkey == key+1){
				nextcolor = (byte) (others.get(i+2) / 36) ;							//花色
				nextkey = getKey(others.get(i+2));
				if(color == nextcolor && nextkey == key+2){		//数字，移除掉
					others.remove(i+2) ;
					others.remove(i+1) ;
					others.remove(i) ;
				}else{
					i = i+2 ;
				}
			}else{
				i = i+1 ; 	//下一步
			}
		}
	}
	
	public static byte getKey(byte card){
		byte value = (byte) ((card%36) / 4) ;			//牌面值
		int rate = card / 36 ;							//花色
		byte key = (byte) (value + 9 * rate) ;			//字典编码
		return key ;
	}
	
	/**
	 * 麻将的出牌判断，杠碰吃胡
	 * @param cards
	 * @param card
	 * @param deal	是否抓牌
	 * @return
	 */
	public static Byte getGangCard(byte[] cards){
		Byte card = null ;
		Map<Integer, Byte> data = new HashMap<Integer, Byte>();
		for(byte temp : cards){
			int value = (temp%36) / 4 ;			//牌面值
			int rote = temp / 36 ;				//花色
			int key = value + 9 * rote ;		//
			if(data.get(key) == null){
				data.put(key , (byte)1) ;
			}else{
				data.put(key, (byte)(data.get(key)+1)) ;
			}
			if(data.get(key) == 4){	//自己发牌的时候，需要先判断是否有杠牌
				card = temp ;
				break ;
			}
		}
		
		return card;
	}
	/**
	 * 定缺方法，计算最少的牌
	 * @param cards
	 * @return
	 */
	public static int selectColor(byte[] cards){
		Map<Integer, Byte> data = new HashMap<Integer, Byte>();
		for(byte temp : cards){
			int key = temp / 36 ;				//花色
			if(data.get(key) == null){
				data.put(key , (byte)1) ;
			}else{
				data.put(key, (byte)(data.get(key)+1)) ;
			}
		}
		int color = 0 , cardsNum = 0 ;
		if(data.get(0)!=null){
			cardsNum = data.get(0) ;
			if(data.get(1) == null){
				color = 1 ;
			}else{
				if(data.get(1) < cardsNum){
					cardsNum = data.get(1) ;
					color = 1 ;
				}
				if(data.get(2)==null){
					color = 2 ;
				}else{
					if(data.get(2) < cardsNum){
						cardsNum = data.get(2) ;
						color = 2 ;
					}
				}
			}
		}
		return color ;
	}
}
