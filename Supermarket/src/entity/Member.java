package entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Member implements Serializable{
	private int memberid;//��Ա���
	private String name;//��Ա����
	private double score;//��Ա����
	
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