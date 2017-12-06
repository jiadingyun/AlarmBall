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
import com.shdcec.alarmball.utility.UtilToast;

/*
 * 查询SQLite中报警球信息,返回ArrayList型供调用页面使用
 * 插入内容
 * 删除内容
 * 修改内容
 */
public class BallInfoDb {
    public static final String TABLE_NAME = "ballInfo";
    //列名称
    public static final String BALL_TEL = "ballTel";
    public static final String BALL_POS = "ballPos";
    public static final String BALL_STATE = "ballState";
    public static final String BALL_DISTANCE = "ballDistance";
    //操作
    public static final String OPERATION_FROM_APP = "APP";
    public static final String OPERATION_FROM_SMS = "sms";
    public static final String OPERATION_SET_DISTANCE = "setDistance";
    private static final String INSERT_OK = "已添加报警球信息";
    private static final String UPDATE_OK = "已修改报警球信息";
    private static final String DELETE_OK = "已删除报警球信息";
    MediaPlayer mp;

    /**
     * 查询数据库
     *
     * @return ArrayList<Map<String, String>>
     */
    public ArrayList<Map<String, String>> Query(SQLiteOpenHelper dbHelper) {
        Cursor cursor = dbHelper.getWritableDatabase().query(TABLE_NAME, null, null, null, null, null, null);
        ArrayList<Map<String, String>> listItems = new ArrayList<Map<String, String>>();
        while (cursor.moveToNext()) {
            Map<String, String> listItem = new HashMap<>();
            listItem.put(BALL_TEL, cursor.getString(cursor.getColumnIndex(BALL_TEL)));
            listItem.put(BALL_POS, cursor.getString(1));
            listItem.put(BALL_STATE, cursor.getString(2));
            listItem.put(BALL_DISTANCE, cursor.getString(3));
            listItems.add(listItem);
        }
        cursor.close();
        return listItems;
    }

    public ArrayList<Map<String, String>> QueryColumns(SQLiteOpenHelper dbHelper, String columns, String content) {
        Cursor cursor = dbHelper.getWritableDatabase()
                .query(TABLE_NAME, null, columns + " = ?", new String[]{content==null?"":content}, null, null, null);
        ArrayList<Map<String, String>> listItems = new ArrayList<>();
        if (cursor.moveToNext()) {
            do {
                Map<String, String> listItem = new HashMap<>();
                listItem.put(BALL_TEL, cursor.getString(cursor.getColumnIndex(BALL_TEL)));
                listItem.put(BALL_POS, cursor.getString(1));
                listItem.put(BALL_STATE, cursor.getString(2));
                listItem.put(BALL_DISTANCE, cursor.getString(3));
                listItems.add(listItem);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return listItems;
    }

    //比较是否有此号码报警球，如果有，删除该报警球信息
    public void DeleteBallInfo(Context context, SQLiteOpenHelper dbHelper, String ballTelString) {
        final ArrayList<Map<String, String>> beforeinsertlistItems = QueryColumns(dbHelper, BALL_TEL, ballTelString);
        //将报警球信息插入数据库
        if (beforeinsertlistItems.isEmpty()) {
        } else {
            //有此报警球，删除
            Delete(dbHelper, ballTelString);
            Toast.makeText(context, DELETE_OK, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * 比较是否有此号码报警球，如果没有，插入该报警球信息
     */
    public void InsertBallInfo(Context context, SQLiteOpenHelper dbHelper,
                               String ballTelString, String ballPosString, String ballStateString) {
        final String BALL_EXIST = context.getString(R.string.ballexist);
        //遍历SQLite，查询该报警球号是否已在数据库中
        final ArrayList<Map<String, String>> beforeinsertlistItems = QueryColumns(dbHelper, BALL_TEL, ballTelString);
        //将报警球信息插入数据库
        if (beforeinsertlistItems.isEmpty()) {
            //将此报警球信息插入SQLite中
            Insert(dbHelper, ballTelString, ballPosString, ballStateString);
            Toast.makeText(context, INSERT_OK, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, BALL_EXIST, Toast.LENGTH_SHORT).show();
            return;
        }
    }

    //todo:插入距离
    // 比较是否有此号码报警球，根据情况修改报警球信息
    public void modifyBallInfo(String operationType, Context context, SQLiteOpenHelper dbHelper,
                               String ballTelString, String ballPosString, String ballStateString,
                               String ballDistanceString, String oldBallTelString) {
        final String BALL_EXIST = context.getString(R.string.ballexist);

        //遍历SQLite，查询该报警球号是否已在数据库中
        final ArrayList<Map<String, String>> beforeModifyList = QueryColumns(dbHelper, BALL_TEL, ballTelString);
        //更新数据库
        //修改的号不在数据库中，可以直接更新数据库
        if (beforeModifyList.isEmpty()) {
            Update(context, operationType, dbHelper, ballTelString, ballPosString,
                    ballStateString, ballDistanceString, oldBallTelString);
        } else {
            //号没有修改，肯定能在数据库中遍历到，也不会和其它报警球重复，直接更新数据库
            if (ballTelString.equals(oldBallTelString)) {
                Update(context, operationType, dbHelper, ballTelString, ballPosString,
                        ballStateString, ballDistanceString, oldBallTelString);
            } else {
                //号和其它报警球重复了，不能更新数据库
                UtilToast.showShort(context, BALL_EXIST);
                return;
            }
        }
    }


    /**
     * 插入报警球信息
     *
     * @param dbHelper  SQLiteOpenHelper
     * @param ballTel   报警球号码
     * @param ballPos   报警球位置,
     * @param ballState 报警球状态
     */
    private void Insert(SQLiteOpenHelper dbHelper, String ballTel, String ballPos, String ballState) {

        ContentValues values = new ContentValues();
        //新建报警球报警距离都为空
        values.put(BALL_TEL, ballTel);
        values.put(BALL_POS, ballPos);
        values.put(BALL_STATE, ballState);
        dbHelper.getWritableDatabase()
                .insert("ballInfo", null, values);
//        values.clear();//如果添加第二个就要clear
    }

    //修改报警球信息
    //Update(SQLiteOpenHelper, 新的报警球号码, 新的报警球位置, 新报警球状态，新报警球报警距离，报警球原号码)

    /**
     * 修改报警球信息
     * @param context
     * @param operationType 操作类型
     * @param dbHelper
     * @param newBallTel 新的报警球号码
     * @param newBallPos 新的报警球位置
     * @param newBallState 新报警球状态
     * @param newBallDistance 新报警球报警距离
     * @param ballTel 报警球原号码
     */
    private void Update(Context context, String operationType, SQLiteOpenHelper dbHelper, String newBallTel,
                        String newBallPos, String newBallState, String newBallDistance, String ballTel) {
        //人工修改的报警球信息只更新号码和位置
        //通过短信更新的报警球信息更新所有信息
        //通过发送报警距离的命令，只更新报警距离
        if (operationType.equalsIgnoreCase(OPERATION_FROM_APP)) {
            ContentValues values = new ContentValues();
            values.put(BALL_TEL, newBallTel);
            values.put(BALL_POS, newBallPos);
            dbHelper.getWritableDatabase().update(TABLE_NAME, values, BALL_TEL + " = ?", new String[]{ballTel});
            UtilToast.showShort(context, UPDATE_OK);
        } else if (operationType.equalsIgnoreCase(OPERATION_FROM_SMS)) {
            ContentValues values = new ContentValues();
            values.put(BALL_TEL, newBallTel);
            values.put(BALL_POS, newBallPos);
            values.put(BALL_STATE, newBallState);
            values.put(BALL_DISTANCE, newBallDistance);
            dbHelper.getWritableDatabase().update(TABLE_NAME, values, BALL_TEL + " = ?", new String[]{ballTel});
        } else if (operationType.equalsIgnoreCase(OPERATION_SET_DISTANCE)) {
            ContentValues values = new ContentValues();
            values.put(BALL_DISTANCE, newBallDistance);
            dbHelper.getWritableDatabase().update(TABLE_NAME, values, BALL_TEL + " = ?", new String[]{ballTel});
        }

    }

    //删除报警球信息
    //Delete(SQLiteOpenHelper, 报警球号码)
    private void Delete(SQLiteOpenHelper dbHelper, String ballTel) {
        dbHelper.getWritableDatabase().delete(TABLE_NAME, BALL_TEL + " = ?", new String[]{ballTel});

    }

}
