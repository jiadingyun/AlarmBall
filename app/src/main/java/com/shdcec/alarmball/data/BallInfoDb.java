package com.shdcec.alarmball.data;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.shdcec.alarmball.R;

/*
 * 查询SQLite中报警球信息,返回ArrayList型供调用页面使用
 * 插入内容
 * 删除内容
 * 修改内容
 */
public class BallInfoDb {
    //列名称
    final static String BALL_TEL = "ballTel";
    final static String BALL_POS = "ballPos";
    final static String BALL_STATE = "ballState";
    final static String BALL_DISTANCE = "ballDistance";
    //修改
    final static String OPERATION_MANUAL = "manul";
    final static String OPERATION_FROM_SMS = "sms";
    final static String OPERATION_SETDISTANCE = "setdistance";
    final static String INSERT_OK = "已添加报警球信息";
    final static String UPDATE_OK = "已修改报警球信息";
    final static String DELETE_OK = "已删除报警球信息";
    MediaPlayer mp;

    /**
     * 查询数据库
     * @return ArrayList<Map<String, String>>
     */
    public ArrayList<Map<String, String>> Query(SQLiteOpenHelper dbHelper) {
        Cursor cursor = dbHelper.getWritableDatabase().query("ballInfo", null,null,null,null,null,null);
        ArrayList<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext()) {
            Map<String, String> listitem = new HashMap<String, String>();
            listitem.put(BALL_TEL, cursor.getString(cursor.getColumnIndex("BALL_TEL")));
            listitem.put(BALL_POS, cursor.getString(1));
            listitem.put(BALL_STATE, cursor.getString(2));
            listitem.put(BALL_DISTANCE, cursor.getString(3));
            listItems.add(listitem);
        }
        return listItems;
    }

    /**
     * 比较是否有此号码报警球，如果有，删除该报警球信息
     */
    public void DeleteBallInfo(Context context, SQLiteOpenHelper dbHelper, String balltelString) {
//        String sqlString = "select * from ballinfo where balltel = '" + balltelString + "'";
//        final ArrayList<Map<String, String>> beforeinsertlistItems = Query(dbHelper, sqlString);
//        //将报警球信息插入数据库
//        if (beforeinsertlistItems.isEmpty()) {
//        } else {
//            //有此报警球，删除
//            Delete(dbHelper, balltelString);
//            Toast.makeText(context, DELETE_OK, Toast.LENGTH_SHORT).show();
//            return;
//        }
    }

    /**
     * 比较是否有此号码报警球，如果没有，插入该报警球信息
     */
    public void InsertBallInfo(Context context, SQLiteOpenHelper dbHelper,
                               String balltelString, String ballposString, String ballstateString) {
//        final String BALL_EXIST = context.getString(R.string.ballexist);
//        //遍历SQLite，查询该报警球号是否已在数据库中
//        String sqlString = "select * from ballinfo where balltel = '" + balltelString + "'";
//        final ArrayList<Map<String, String>> beforeinsertlistItems = Query(dbHelper, sqlString);
//        //将报警球信息插入数据库
//        if (beforeinsertlistItems.isEmpty()) {
//            //将此报警球信息插入SQLite中
//            Insert(dbHelper, balltelString, ballposString, ballstateString);
//            Toast.makeText(context, INSERT_OK, Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(context, BALL_EXIST, Toast.LENGTH_SHORT).show();
//            return;
//        }
    }

    /**
     * 比较是否有此号码报警球，根据情况修改报警球信息
     */
    public void ModifyBallInfo(String operationClass, Context context, SQLiteOpenHelper dbHelper, String balltelString, String ballposString, String ballstateString, String balldistanceString, String oldballtelString) {
//        final String BALL_EXIST = context.getString(R.string.ballexist);
//
//        //遍历SQLite，查询该报警球号是否已在数据库中
//        String sqlString = "select * from ballinfo where balltel = '" + balltelString + "'";
//        final ArrayList<Map<String, String>> beforemodifylistItems = Query(dbHelper, sqlString);
//        //更新数据库
//        //修改的号不在数据库中，可以直接更新数据库
//        if (beforemodifylistItems.isEmpty()) {
//            Update(context, operationClass, dbHelper, balltelString, ballposString,
//                    ballstateString, balldistanceString, oldballtelString);
//        } else {
//            //号没有修改，肯定能在数据库中遍历到，也不会和其它报警球重复，直接更新数据库
//            if (balltelString.equals(oldballtelString)) {
//                Update(context, operationClass, dbHelper, balltelString, ballposString,
//                        ballstateString, balldistanceString, oldballtelString);
//            } else {
//                //号和其它报警球重复了，不能更新数据库
//                Toast.makeText(context, BALL_EXIST, Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
    }


    /**
     * 向数据库插入报警球信息
     *
     * @param dbHelper SQLiteOpenHelper
     * @param ballTel 报警球号码
     * @param ballPos 报警球位置,
     * @param ballState 报警球状态
     */
    private void Insert(SQLiteOpenHelper dbHelper, String ballTel,String ballPos, String ballState) {

        ContentValues values = new ContentValues();
        //插入数据//新建报警球报警距离都为空
        values.put(BALL_TEL,ballTel);
        values.put(BALL_POS,ballPos);
        values.put(BALL_STATE,ballState);
        dbHelper.getWritableDatabase()
                .insert("ballInfo",null,values);
//        values.clear();//如果添加第二个就要clear
    }

    //修改报警球信息
    //Update(SQLiteOpenHelper, 新的报警球号码, 新的报警球位置, 新报警球状态，新报警球报警距离，报警球原号码)
    private void Update(Context context, String operationClass, SQLiteOpenHelper dbHelper, String newballtel,
                        String newballpos, String newballstate, String newballdistance, String balltel) {
        //人工修改的报警球信息只更新号码和位置
        //通过短信更新的报警球信息更新所有信息
        //通过发送报警距离的命令，只更新报警距离
//        if (operationClass.equalsIgnoreCase(OPERATION_MANUAL)) {
//            dbHelper.getReadableDatabase().execSQL("update ballinfo set balltel = ?,ballpos = ? where balltel = ?",
//                    new String[]{newballtel, newballpos, balltel});
//            Toast.makeText(context, UPDATE_OK, Toast.LENGTH_SHORT).show();
//        } else if (operationClass.equalsIgnoreCase(OPERATION_FROM_SMS)) {
//            dbHelper.getReadableDatabase().execSQL("update ballinfo set balltel = ?,ballpos = ?,ballstate = ?,balldistance = ? where balltel = ?",
//                    new String[]{newballtel, newballpos, newballstate, newballdistance, balltel});
//        } else if (operationClass.equalsIgnoreCase(OPERATION_SETDISTANCE)) {
//            dbHelper.getReadableDatabase().execSQL("update ballinfo set balldistance = ? where balltel = ?",
//                    new String[]{newballdistance, balltel});
//        }

    }

    //删除报警球信息
    //Delete(SQLiteOpenHelper, 报警球号码)
    private void Delete(SQLiteOpenHelper dbHelper, String balltel) {
//        dbHelper.getReadableDatabase().execSQL("delete from ballinfo where balltel = ?",
//                new String[]{balltel});
    }

}
