package com.shdcec.alarmball.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 创建数据库
 */
public class DBOpenHelper extends SQLiteOpenHelper 
{
	// 创建数据库ballInfo存储报警球号码，位置，状态信息
	final String CREATE_BALL_SQL = "create table ballInfo(ballTel, ballPos, ballState, ballDistance)";
	// 创建数据库smsInfo存储发送号码，接收号码，短信内容，短信类型
	final String CREATE_SMS_SQL = "create table smsInfo(time, pos, fromNum, toNum, smsText, smsType)";
	public DBOpenHelper(Context context, String name,int version)
	{
		super(context, name, null, version);
	}
	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		// first time using database create the table
		db.execSQL(CREATE_BALL_SQL);
		db.execSQL(CREATE_SMS_SQL);
	}
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
	}
 
}
