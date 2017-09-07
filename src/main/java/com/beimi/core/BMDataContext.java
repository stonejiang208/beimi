package com.beimi.core;

import org.springframework.context.ApplicationContext;

import com.beimi.core.engine.game.GameEngine;

public class BMDataContext {
	public static final String USER_SESSION_NAME = "user";
	public static final String GUEST_USER = "guest";
	public static final String IM_USER_SESSION_NAME = "im_user";
	public static final String GUEST_USER_ID_CODE = "BEIMIGUESTUSEKEY" ;
	public static final String SERVICE_QUENE_NULL_STR = "service_quene_null" ;
	public static final String DEFAULT_TYPE = "default"	;		//默认分类代码
	public static final String BEIMI_SYSTEM_DIC = "com.dic.system.template";
	public static final String BEIMI_SYSTEM_GAME_TYPE_DIC = "com.dic.game.type";
	
	public static final String BEIMI_SYSTEM_GAME_ACCOUNT_CONFIG = "game_account_config";
	public static final String BEIMI_GAME_PLAYWAY = "game_playway";
	
	public static final String BEIMI_SYSTEM_AUTH_DIC = "com.dic.auth.resource";
	
	
	public static String SYSTEM_ORGI = "beimi" ;
	
	private static int WebIMPort = 9081 ;
	
	private static boolean imServerRunning = false ;			//IM服务状态
	
	private static ApplicationContext applicationContext ;
	
	
	private static GameEngine gameEngine ;
	
	public static int getWebIMPort() {
		return WebIMPort;
	}

	public static void setWebIMPort(int webIMPort) {
		WebIMPort = webIMPort;
	}
	
	public static void setApplicationContext(ApplicationContext context){
		applicationContext = context ;
	}
	
	public static void setGameEngine(GameEngine engine){
		gameEngine = engine ;
	}
	/**
	 * 根据ORGI找到对应 游戏配置
	 * @param orgi
	 * @return
	 */
	public static String getGameAccountConfig(String orgi){
		return BEIMI_SYSTEM_GAME_ACCOUNT_CONFIG+"_"+orgi ;
	}
	
	public static ApplicationContext getContext(){
		return applicationContext ;
	}
	
	public static GameEngine getGameEngine(){
		return gameEngine; 
	}
	/**
	 * 系统级的加密密码 ， 从CA获取
	 * @return
	 */
	public static String getSystemSecrityPassword(){
		return "BEIMI" ;
	}
	
	public enum NameSpaceEnum{
		
		SYSTEM("/bm/system") ,
		GAME("/bm/game");
		
		private String namespace ;
		
		public String getNamespace() {
			return namespace;
		}

		public void setNamespace(String namespace) {
			this.namespace = namespace;
		}

		NameSpaceEnum(String namespace){
			this.namespace = namespace ;
		}
		
		public String toString(){
			return super.toString().toLowerCase() ;
		}
	}
	
	public enum ModelType{
		ROOM;
		public String toString(){
			return super.toString().toLowerCase() ;
		}
	}
	
	public enum UserDataEventType{
		SAVE,UPDATE,DELETE;
		public String toString(){
			return super.toString().toLowerCase() ;
		}
	}
	
	
	public enum GameTypeEnum{
		MAJIANG,
		DIZHU,
		DEZHOU;
		public String toString(){
			return super.toString().toLowerCase() ;
		}
	}
	
	public enum PlayerTypeEnum{
		AI,			//AI
		NORMAL,		//普通玩家
		OFFLINE;	//离线 托管玩家
		public String toString(){
			return super.toString().toLowerCase() ;
		}
	}
	
	public enum CardsTypeEnum{
		ONE(1),		//单张      3~K,A,2
		TWO(2),		//一对	 3~K,A,2
		THREE(3),	//三张	 3~K,A,2
		FOUR(4),	//三带一	 AAA+K
		FIVE(5),	//单顺	连子		10JQKA
		SIX(6),		//双顺	连对		JJQQKK
		SEVEN(7),	//三顺	飞机		JJJQQQ
		EIGHT(8),	//飞机	带翅膀	JJJ+QQQ+K+A
		NINE(9),	//四带二			JJJJ+Q+K
		TEN(10),	//炸弹			JJJJ
		ELEVEN(11);	//王炸			0+0
		
		private int type ;
		
		CardsTypeEnum(int type){
			this.type = type ;
		} 
		

		public int getType() {
			return type;
		}


		public void setType(int type) {
			this.type = type;
		}
	}
	
	public enum MessageTypeEnum{
		JOINROOM,
		MESSAGE, 
		END,
		TRANS, STATUS , AGENTSTATUS , SERVICE, WRITING;
		
		public String toString(){
			return super.toString().toLowerCase() ;
		}
	}

	public static void setIMServerStatus(boolean running){
		imServerRunning = running ;
	}
	public static boolean getIMServerStatus(){
		return imServerRunning;
	}
	
}
