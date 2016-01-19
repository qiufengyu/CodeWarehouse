package entity;

import java.io.Serializable;


@SuppressWarnings("serial")
public class User implements Serializable{
	private String userid;//用户编号
	//private String name;//用户名
	private String password;//密码
	private int authority;//权限
	
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public int getAuthority() {
		return authority;
	}
	public void setAuthority(int authority) {
		this.authority = authority;
	}
	/*
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	*/
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
