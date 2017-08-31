package com.beimi.core.engine.game;

public class GameBoard implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -907633644768054042L;
	
	public GameBoard(String userid, boolean docatch , int ratio){
		this.userid = userid ;
		this.docatch = docatch ;
		this.ratio = ratio ;
	}
	
	private String userid ;
	private boolean docatch ;
	private int ratio ;
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
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
