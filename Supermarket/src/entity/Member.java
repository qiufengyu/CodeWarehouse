package entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Member implements Serializable{
	private int memberid;//会员编号
	private String name;//会员姓名
	private double score;//会员积分
	
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getMemberid() {
		return memberid;
	}
	public void setMemberid(int memberid) {
		this.memberid = memberid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	
}