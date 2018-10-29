package com.tools.payhelper.utils;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * 

* @ClassName: QrCodeBean

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:45

*
 */
public class QrCodeBean implements Serializable{
	private static final long serialVersionUID = 8988815091574805671L;
	private static final String serialVersionSTR = "1BQljr6O6WxeHGZ77JU2qQ1eiMQvBdFFl3xTYWEIfW4=";
	
	private String money;
	private String mark;
	private String type;
	private String payurl;
	private String dt;
	
	
	public QrCodeBean(String money, String mark, String type, String payurl, String dt) {
		super();
		this.money = money;
		this.mark = mark;
		this.type = type;
		this.payurl = payurl;
		this.dt = dt;
	}

	public QrCodeBean() {
		super();
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

	public String getPayurl() {
		return payurl;
	}

	public void setPayurl(String payurl) {
		this.payurl = payurl;
	}

	public String getDt() {
		return dt;
	}

	public void setDt(String dt) {
		this.dt = dt;
	}

}
