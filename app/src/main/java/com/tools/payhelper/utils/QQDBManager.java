package com.tools.payhelper.utils;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 

* @ClassName: QQDBManager

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:40

*
 */
public class QQDBManager {
	private SQLiteDatabase db;
	private QQDBHelper helper;
	public QQDBManager(Context context){
        helper = new QQDBHelper(context);
        db = helper.getWritableDatabase();
    }
	
	 public void addQQMark(String money,String mark) {
        db.beginTransaction();// 开始事务
        try {
        	String dt=System.currentTimeMillis()+"";
            db.execSQL("INSERT INTO mark VALUES(null,?,?,?,?)", new Object[] { money, mark, "0", dt});
            db.setTransactionSuccessful();// 事务成功
        } finally {
            db.endTransaction();// 结束事务
        }
    }
	public void updateOrder(String money,String mark) {
		 db.beginTransaction();// 开始事务
		 try {
			 db.execSQL("UPDATE mark SET status='1' WHERE money=? and mark=?", new Object[] {money, mark});
			 db.setTransactionSuccessful();// 事务成功
		 } finally {
			 db.endTransaction();// 结束事务
		 }
	}
	
	public ArrayList<QrCodeBean> FindQQMark(String money,String mark) {
		String sql = "SELECT * FROM mark WHERE money='"+money+"' and mark='"+mark+"' and status='0'";
		ArrayList<QrCodeBean> list = new ArrayList<QrCodeBean>();
		Cursor c = ExecSQLForCursor(sql);
		while (c.moveToNext()) {
			QrCodeBean info = new QrCodeBean();
			info.setMoney(c.getString(c.getColumnIndex("money")));
			info.setMark(c.getString(c.getColumnIndex("mark")));
			info.setDt(c.getString(c.getColumnIndex("dt")));
			list.add(info);
		}
		c.close();
		return list;
	}
	public QrCodeBean GetQQMark() {
		String sql = "SELECT * FROM mark WHERE status='0' order by dt desc";
		Cursor c = ExecSQLForCursor(sql);
		QrCodeBean info = new QrCodeBean();
		if (c.moveToNext()) {
			info.setMoney(c.getString(c.getColumnIndex("money")));
			info.setMark(c.getString(c.getColumnIndex("mark")));
			info.setDt(c.getString(c.getColumnIndex("dt")));
		}
		c.close();
		return info;
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
