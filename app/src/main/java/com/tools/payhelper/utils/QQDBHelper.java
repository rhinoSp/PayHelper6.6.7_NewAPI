package com.tools.payhelper.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 

* @ClassName: QQDBHelper

* @Description: TODO(这里用一句话描述这个类的作用)

* @author SuXiaoliang

* @date 2018年6月23日 下午1:27:36

*
 */
public class QQDBHelper extends SQLiteOpenHelper{
	public QQDBHelper(Context context) {  
        super(context, "mark.db", null, 1);  
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS mark" + 
				"(_id INTEGER PRIMARY KEY AUTOINCREMENT, money varchar, mark varchar, status varchar, dt varchar)");
	}
 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
	}  
}
