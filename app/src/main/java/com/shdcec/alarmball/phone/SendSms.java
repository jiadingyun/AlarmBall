package com.shdcec.alarmball.phone;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;

import com.shdcec.alarmball.MainActivity;
import com.shdcec.alarmball.data.BallInfoDb;
import com.shdcec.alarmball.data.DBOpenHelper;
import com.shdcec.alarmball.data.SmsDb;
import com.shdcec.alarmball.dialog.AddBallActivity;

/*
 * 发送短信
 * PendingIntent监听发送状态
 * 获取发送短信时间
 * 获取报警球位置
 * 存储指令内容
 */
public class SendSms {
    private ArrayList<Map<String, String>> ballPosList;
    private String smsDetail;

    private DBOpenHelper dbHelper;
    private BallInfoDb ballInfoDb = new BallInfoDb();
    private SmsDb smsDb = new SmsDb();
    private static final String DB_NAME = "AlarmBall.db";
    private static final String ORDER_DOWN_NUMBER = "#01";    //下装后台号码
    private static final String ORDER_SET_DIS = "#02";    //设置距离
    private static final String ORDER_QUERY = "#10";    //查询
    private static final String ORDER_OPEN_DET = "#21";    //打开探头
    private static final String ORDER_CLOSE_DET = "#20";    //关闭探头

    /**
     * 发送短信
     *
     * @param activity
     * @param intent
     * @param phone   手机号码
     * @param text    发送的信息
     */
    public void SendSmsText(Activity activity, Intent intent, String phone, String text) {
        /**
         * android6.0系统后增加运行时权限，需要动态添加内存卡读取权限
         */
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.SEND_SMS);
        //判断是否已有对应权限
        if (permission != PackageManager.PERMISSION_GRANTED) {
            //没有权限，则需要申请权限
            //因此需要弹出提示框，提醒用户该功能需要权限
            //用户主动赋予过一次后，该应用就一直具有该权限，除非在应用管理中撤销
            //用户拒绝，但不选择“不再提示”就会再次执行询问
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.SEND_SMS}, 0);
            // 用户拒绝，选择“不再提示”需要去onRequestPermissionsResult中判断，
            // 如果用户拒绝并选择“不再提示”，能做的只有打开权限设置页面
            return;
        }
        //发送短信
        SmsManager smsManager = SmsManager.getDefault();
        PendingIntent sentPI = PendingIntent.getBroadcast(activity, 0, intent, 0);
        smsManager.sendTextMessage(phone, null, text, sentPI, null);
        //获取当前时间
        SimpleDateFormat timeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        String nowTime = timeDateFormat.format(curDate);
        //获取报警球位置
        dbHelper = new DBOpenHelper(activity, DB_NAME, 1);
        ballPosList = ballInfoDb.QueryColumns(dbHelper, BallInfoDb.BALL_TEL, phone);
        String ballPos = ballPosList.get(0).get(BallInfoDb.BALL_POS);
        //区分短信内容
        //1-3个字符--命令类别
        //5-8个字符--指令密码
        //第10个字符开始指令具体内容
        char[] textChar = text.toCharArray();
        String orderClass = new String(textChar, 0, 3);  //短信格式 #01,1234,13333333333
        if (orderClass.equalsIgnoreCase(ORDER_DOWN_NUMBER)) {
            int numberlength = textChar.length - 9;    //从#到密码结束共9个字符，余下的为后台号码
            String downnumber = new String(textChar, 9, numberlength);
            smsDetail = "下装后台:" + downnumber;
        }
        if (orderClass.equalsIgnoreCase(ORDER_SET_DIS)) {
            String setdistance = new String(textChar, 9, 3);//短信格式 #01,1234,500
            int distance = (int) (Double.parseDouble(setdistance) / 100);
            String showdistance = Integer.toString(distance);
            smsDetail = "设定报警距离:" + showdistance + "米";
        }
        if (orderClass.equalsIgnoreCase(ORDER_QUERY)) {
            smsDetail = "查询";
        }
        if (orderClass.equalsIgnoreCase(ORDER_OPEN_DET)) {
            smsDetail = "打开探头";
        }
        if (orderClass.equalsIgnoreCase(ORDER_CLOSE_DET)) {
            smsDetail = "关闭探头";
        }
        //存储短信内容至数据库
        smsDb.Insert(dbHelper, nowTime, ballPos, null, phone, smsDetail, SmsDb.SMS_TYPE_SEND);
        dbHelper.close();
    }
}
