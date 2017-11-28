package com.shdcec.alarmball.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDb {
    //根据sqlString查询数据库
    //Query(SQLiteOpenHelper,SQL语句)
    public ArrayList<Map<String, String>> Query(SQLiteOpenHelper dbHelper, String sqlString) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sqlString, null);
        ArrayList<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext()) {
            Map<String, String> listitem = new HashMap<String, String>();
            listitem.put("time", cursor.getString(0));
            listitem.put("pos", cursor.getString(1));
            listitem.put("fromnum", cursor.getString(2));
            listitem.put("tonum", cursor.getString(3));
            listitem.put("smstext", cursor.getString(4));
            listitem.put("smsclass", cursor.getString(5));
            listItems.add(listitem);
        }
        return listItems;
    }

    //向数据库插入短信信息
    //Insert(SQLiteOpenHelper, 发送时间，发送号码，接收号码，短信内容，短信类型)
    public void Insert(SQLiteOpenHelper dbHelper, String time, String pos, String fromnum, String tonum, String smstext, String smsclass) {
        dbHelper.getReadableDatabase().execSQL("insert into smsinfo values(?, ?, ?, ?, ?, ?)",
                new String[]{time, pos, fromnum, tonum, smstext, smsclass});
    }

    //删除短信信息
    public void Delete(SQLiteOpenHelper dbHelper, String sqlString) {
        dbHelper.getReadableDatabase().execSQL(sqlString);
    }
}
