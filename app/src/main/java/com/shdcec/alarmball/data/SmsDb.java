package com.shdcec.alarmball.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public class SmsDb {
    //表名称
    public static final String TABLE_NAME = "smsInfo";
    //列名
    public static final String SMS_TIME = "time";
    public static final String SMS_POS = "pos";
    public static final String SMS_FROM_NUM = "fromNum";
    public static final String SMS_TO_NUM = "toNum";
    public static final String SMS_TEXT = "smsText";
    public static final String SMS_TYPE = "smsType";
    //短信类型
    public static final String SMS_TYPE_SEND = "send";
    public static final String SMS_TYPE_RECEIVE = "receive";
    public static final String SMS_TYPE_ALARM = "alarm";


    /**
     * 查询查询短信数据库
     * @param dbHelper SQLiteOpenHelper
     * @param sqlString SQL语句
     * @return
     */
    public ArrayList<Map<String, String>> Query(SQLiteOpenHelper dbHelper, String sqlString) {
        Cursor cursor = dbHelper.getReadableDatabase().rawQuery(sqlString,null);
        ArrayList<Map<String, String>> listItems = new ArrayList<>();
        while (cursor.moveToNext()) {
            Map<String, String> listItem = new HashMap<>();
            listItem.put(SMS_TIME, cursor.getString(0));
            listItem.put(SMS_POS, cursor.getString(1));
            listItem.put(SMS_FROM_NUM, cursor.getString(2));
            listItem.put(SMS_TO_NUM, cursor.getString(3));
            listItem.put(SMS_TEXT, cursor.getString(4));
            listItem.put(SMS_TYPE, cursor.getString(5));
            listItems.add(listItem);
        }
        cursor.close();
        return listItems;
    }
    /**
     * 向数据库插入短信信息
     * @param dbHelper SQLiteOpenHelper
     * @param time 发送时间
     * @param pos 位置
     * @param fromNum 发送号码
     * @param toNum 接收号码
     * @param smsText 短信内容
     * @param smsType 短信类型
     */
    public void Insert(SQLiteOpenHelper dbHelper, String time, String pos, String fromNum, String toNum,
                       String smsText, String smsType) {

        ContentValues values = new ContentValues();
        //插入数据//新建报警球报警距离都为空
        values.put(SMS_TIME, time);
        values.put(SMS_POS, pos);
        values.put(SMS_FROM_NUM, fromNum);
        values.put(SMS_TO_NUM, toNum);
        values.put(SMS_TEXT, smsText);
        values.put(SMS_TYPE, smsType);
        dbHelper.getWritableDatabase()
                .insert(TABLE_NAME, null, values);
//        values.clear();//如果添加第二个就要clear
    }

    /**
     * 删除最新时间的短信信息
     */
    public void Delete(SQLiteOpenHelper dbHelper) {
        dbHelper.getWritableDatabase().delete(TABLE_NAME, SMS_TIME + "= ?",new String[]{"max(time)"} );
    }
}
