package entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Trade implements Serializable{
	private double cost1;//½ð¶î
	private String id;
	private String date;

	public double getCost() {
		return cost1;
	}

	public void setCost(double cost) {
		this.cost1 = cost;
	}
	
	public String getid() {
		return id;
	}

	public void setid(String id) {
		this.id = id;
	}
	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date=date;
	}
		
}
