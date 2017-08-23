package com.beimi.web.model;

/**
 * 
 * @author iceworld
 *
 */
public class ResultData implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2710593844038107782L;
	private boolean status ;
	private String msg ;
	private Object data ;
	private Token token ;
	
	public ResultData(boolean status , String msg , Object data){
		this.status = status ;
		this.msg = msg ;
		this.data = data ;
	}
	
	public ResultData(boolean status , String msg , Object data , Token token ){
		this.status = status ;
		this.msg = msg ;
		this.data = data ;
		this.token = token ;
	}
	
	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}

	public Token getToken() {
		return token;
	}

	public void setToken(Token token) {
		this.token = token;
	}
}
