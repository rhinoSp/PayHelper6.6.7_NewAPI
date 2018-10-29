package com.tools.payhelper.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 

* @ClassName: DBManager

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:22

*
 */
public class DBManager {
	private SQLiteDatabase db;
	private DBHelper helper;
	public DBManager(Context context){
        helper = new DBHelper(context);
        db = helper.getWritableDatabase();
    }
	
	 public void addQrCode(QrCodeBean qrCodeBean) {
        db.beginTransaction();// 开始事务
        try {
        	String dt=System.currentTimeMillis()/1000+"";
            db.execSQL("INSERT INTO qrcode VALUES(null,?,?,?,?,?)", new Object[] { qrCodeBean.getMoney(), qrCodeBean.getMark(), qrCodeBean.getType(), qrCodeBean.getPayurl(), dt });
            db.setTransactionSuccessful();// 事务成功
        } finally {
            db.endTransaction();// 结束事务
        }
    }
	public void addOrder(OrderBean ordereBean) {
		 db.beginTransaction();// 开始事务
		 try {
			 String dt=System.currentTimeMillis()/1000+"";
			 db.execSQL("INSERT INTO payorder VALUES(null,?,?,?,?,?,?,?)", new Object[] { ordereBean.getMoney(), ordereBean.getMark(), ordereBean.getType(), ordereBean.getNo(), dt ,ordereBean.getResult() , ordereBean.getTime() });
			 db.setTransactionSuccessful();// 事务成功
		 } finally {
			 db.endTransaction();// 结束事务
		 }
	 }
	public void addTradeNo(String tradeNo,String status) {
		db.beginTransaction();// 开始事务
		try {
			db.execSQL("INSERT INTO tradeno VALUES(null,?,?)", new Object[] { tradeNo, status});
			db.setTransactionSuccessful();// 事务成功
		} finally {
			db.endTransaction();// 结束事务
		}
	}
	
	public void addConfig(String name,String value) {
		db.beginTransaction();// 开始事务
		try {
			db.execSQL("INSERT INTO config VALUES(null,?,?)", new Object[] { name,value });
			db.setTransactionSuccessful();// 事务成功
		} finally {
			db.endTransaction();// 结束事务
		}
	}
	
	public void updateConfig(String name,String value) {
		 db.beginTransaction();// 开始事务
		 try {
			 db.execSQL("UPDATE config SET value=? WHERE name=?", new Object[] {value, name});
			 db.setTransactionSuccessful();// 事务成功
		 } finally {
			 db.endTransaction();// 结束事务
		 }
	 }
	
	public void saveOrUpdateConfig(String name,String value) {
		if(getConfig(name).equals("null")){
			addConfig(name, value);
		}else{
			updateConfig(name, value);
		}
	}
	
	public String getConfig(String name) {
		String sql = "SELECT * FROM config WHERE name='"+name+"'";
		Cursor c = ExecSQLForCursor(sql);
		String value="null";
		if(c.moveToNext()){
			value=c.getString(c.getColumnIndex("value"));
		}
		c.close();
		return value;
	}
	
	public boolean isExistTradeNo(String tradeNo) {
		boolean isExist=false;
		String sql = "SELECT * FROM tradeno WHERE tradeno='"+tradeNo+"'";
		Cursor c = ExecSQLForCursor(sql);
		if(c.getCount()>0){
			isExist=true;
		}
		c.close();
		return isExist;
	}
	
	public boolean isNotifyTradeNo(String tradeNo) {
		boolean isExist=false;
		String sql = "SELECT * FROM tradeno WHERE tradeno='"+tradeNo+"'";
		Cursor c = ExecSQLForCursor(sql);
		if(c.moveToNext()){
			String status=c.getString(c.getColumnIndex("status"));
			if(status.equals("1"))
			isExist=true;
		}
		c.close();
		return isExist;
	}
	
	public void updateTradeNo(String tradeNo,String status) {
		 db.beginTransaction();// 开始事务
		 try {
			 db.execSQL("UPDATE tradeno SET status=? WHERE tradeno=?", new Object[] {status, tradeNo});
			 db.setTransactionSuccessful();// 事务成功
		 } finally {
			 db.endTransaction();// 结束事务
		 }
	 }
	
	public void updateOrder(String no,String result) {
		 db.beginTransaction();// 开始事务
		 try {
			 db.execSQL("UPDATE payorder SET result=?,time=time+1 WHERE tradeno=?", new Object[] {result, no});
			 db.setTransactionSuccessful();// 事务成功
		 } finally {
			 db.endTransaction();// 结束事务
		 }
	 }
	 
	 public ArrayList<QrCodeBean> FindQrCodes(String money,String mark,String type) {
		DecimalFormat df = new DecimalFormat("0.00");
		money=df.format(Double.parseDouble(money));
    	String sql = "SELECT * FROM qrcode WHERE money =" + "'" + money + "' and mark='"+mark+"' and type='"+type+"'";
        ArrayList<QrCodeBean> list = new ArrayList<QrCodeBean>();
        Cursor c = ExecSQLForCursor(sql);
        while (c.moveToNext()) {
            QrCodeBean info = new QrCodeBean();
            info.setMoney(c.getString(c.getColumnIndex("money")));
            info.setMark(c.getString(c.getColumnIndex("mark")));
            info.setType(c.getString(c.getColumnIndex("type")));
            info.setPayurl(c.getString(c.getColumnIndex("payurl")));
            info.setDt(c.getString(c.getColumnIndex("dt")));
            list.add(info);
        }
        c.close();
        return list;
    }
	 public ArrayList<QrCodeBean> FindQrCodes(String mark,String type) {
		 String sql = "SELECT * FROM qrcode WHERE mark='"+mark+"'";
		 ArrayList<QrCodeBean> list = new ArrayList<QrCodeBean>();
		 Cursor c = ExecSQLForCursor(sql);
		 while (c.moveToNext()) {
			 QrCodeBean info = new QrCodeBean();
			 info.setMoney(c.getString(c.getColumnIndex("money")));
			 info.setMark(c.getString(c.getColumnIndex("mark")));
			 info.setType(c.getString(c.getColumnIndex("type")));
			 info.setPayurl(c.getString(c.getColumnIndex("payurl")));
			 info.setDt(c.getString(c.getColumnIndex("dt")));
			 list.add(info);
		 }
		 c.close();
		 return list;
	 }
	public ArrayList<OrderBean> FindOrders(String money,String mark,String type) {
    	String sql = "SELECT * FROM payorder WHERE money =" + "'" + money + "' and mark='"+mark+"' and type='"+type+"'";
    	ArrayList<OrderBean> list = new ArrayList<OrderBean>();
    	Cursor c = ExecSQLForCursor(sql);
    	while (c.moveToNext()) {
    		OrderBean info = new OrderBean();
    		info.setMoney(c.getString(c.getColumnIndex("money")));
    		info.setMark(c.getString(c.getColumnIndex("mark")));
    		info.setType(c.getString(c.getColumnIndex("type")));
    		info.setNo(c.getString(c.getColumnIndex("tradeno")));
    		info.setDt(c.getString(c.getColumnIndex("dt")));
    		info.setResult(c.getString(c.getColumnIndex("result")));
    		info.setTime(c.getInt(c.getColumnIndex("time")));
    		list.add(info);
    	}
    	c.close();
    	return list;
    }
	public ArrayList<OrderBean> FindOrders(String mark) {
		String sql = "SELECT * FROM payorder WHERE mark='"+mark+"'";
		ArrayList<OrderBean> list = new ArrayList<OrderBean>();
		Cursor c = ExecSQLForCursor(sql);
		while (c.moveToNext()) {
			OrderBean info = new OrderBean();
			info.setMoney(c.getString(c.getColumnIndex("money")));
			info.setMark(c.getString(c.getColumnIndex("mark")));
			info.setType(c.getString(c.getColumnIndex("type")));
			info.setNo(c.getString(c.getColumnIndex("tradeno")));
			info.setDt(c.getString(c.getColumnIndex("dt")));
			info.setResult(c.getString(c.getColumnIndex("result")));
    		info.setTime(c.getInt(c.getColumnIndex("time")));
			list.add(info);
		}
		c.close();
		return list;
	}
	public ArrayList<OrderBean> FindOrdersByNo(String no) {
		String sql = "SELECT * FROM payorder WHERE tradeno='"+no+"'";
		ArrayList<OrderBean> list = new ArrayList<OrderBean>();
		Cursor c = ExecSQLForCursor(sql);
		while (c.moveToNext()) {
			OrderBean info = new OrderBean();
			info.setMoney(c.getString(c.getColumnIndex("money")));
			info.setMark(c.getString(c.getColumnIndex("mark")));
			info.setType(c.getString(c.getColumnIndex("type")));
			info.setNo(c.getString(c.getColumnIndex("tradeno")));
			info.setDt(c.getString(c.getColumnIndex("dt")));
			info.setResult(c.getString(c.getColumnIndex("result")));
			info.setTime(c.getInt(c.getColumnIndex("time")));
			list.add(info);
		}
		c.close();
		return list;
	}
	public ArrayList<OrderBean> FindAllOrders() {
		String sql = "SELECT * FROM payorder where result <> 'success' and time<3 ";
		ArrayList<OrderBean> list = new ArrayList<OrderBean>();
		Cursor c = ExecSQLForCursor(sql);
		while (c.moveToNext()) {
			OrderBean info = new OrderBean();
			info.setMoney(c.getString(c.getColumnIndex("money")));
			info.setMark(c.getString(c.getColumnIndex("mark")));
			info.setType(c.getString(c.getColumnIndex("type")));
			info.setNo(c.getString(c.getColumnIndex("tradeno")));
			info.setDt(c.getString(c.getColumnIndex("dt")));
			info.setResult(c.getString(c.getColumnIndex("result")));
			info.setTime(c.getInt(c.getColumnIndex("time")));
			list.add(info);
		}
		c.close();
		return list;
	}
	    
    /**
     * 执行SQL，返回一个游标
     * 
     * @param sql
     * @return
     */
    private Cursor ExecSQLForCursor(String sql) {
        Cursor c = db.rawQuery(sql, null);
        return c;
    }
}
