package entity;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Goods implements Serializable{
	private String goodid;//��Ʒ���
	private String name;//��Ʒ����
	private double price;//����
	private int count;//����
	//private int stock;//�������

	public String getGoodid() {
		return goodid;
	}
	public void setGoodid(String goodid) {
		this.goodid = goodid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	/*
	 * public String getUnit() {
		return unit;
	}
	*/
	/*
	public void setUnit(String unit) {
		this.unit = unit;
	}
	*/
	/*
	public int getStock() {
		return stock;
	}
	public void setStock(int stock) {
		this.stock = stock;
	}
	*/
	
}
