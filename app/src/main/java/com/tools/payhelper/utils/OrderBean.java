package com.tools.payhelper.utils;

import java.io.Serializable;

/**
 * 

* @ClassName: OrderBean

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:27

*
 */
public class OrderBean implements Serializable{
	private static final long serialVersionUID = 8988815091574805671L;
	private String money;
	private String mark;
	private String type;
	private String no;
	private String dt;
	private String result;
	private int time;
	
	
	public OrderBean() {
		super();
	}
	public OrderBean(String money, String mark, String type, String no, String dt, String result, int time) {
		super();
		this.money = money;
		this.mark = mark;
		this.type = type;
		this.no = no;
		this.dt = dt;
		this.result = result;
		this.time = time;
	}
	public String getMoney() {
		return money;
	}
	public void setMoney(String money) {
		this.money = money;
	}
	public String getMark() {
		return mark;
	}
	public void setMark(String mark) {
		this.mark = mark;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getDt() {
		return dt;
	}
	public void setDt(String dt) {
		this.dt = dt;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public int getTime() {
		return time;
	}
	public void setTime(int time) {
		this.time = time;
	}
	
	
}
