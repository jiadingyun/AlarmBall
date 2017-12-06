package com.shdcec.alarmball.phone;

import java.util.ArrayList;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.shdcec.alarmball.data.BallInfoDb;
import com.shdcec.alarmball.data.DBOpenHelper;

/**
 * 拦截系统广播，使系统接收不到报警球发来的短信
 */
public class AbortBroadcastReceiver extends BroadcastReceiver {
    final static String DB_NAME = "AlarmBall.db";
    private int oldVersion = 1;
    private String ballTel;
    private String smsText;
    private BallInfoDb ballInfoDb;
    private ArrayList<Map<String, String>> ballArrayList;
    private DBOpenHelper dbHelper;

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("back monitor");
        //判断是否是接收短信激活的服务
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            //接收由SMS传过来的数据
            Bundle bundle = intent.getExtras();
            //判断SMS中是否有数据
            if (bundle != null) {
                //获取所有短信信息，短信可能拆分成多条，所以用数组
                //pdus=protocol description units的简写,也就是短信们
                Object[] pdus = (Object[]) bundle.get("pdus");
                //构建短信对象array，并依据收到的对象长度决定大小
                SmsMessage[] messages = new SmsMessage[pdus.length];
                //逐位将收到的对象填入短信对象中
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                //遍历短信对象，获取发送方号码
                for (SmsMessage message : messages) {
                    ballTel = message.getDisplayOriginatingAddress();
                    smsText = message.getDisplayMessageBody();
                }
                //如果发送方号码为14位，表示收到的号码前有+86前缀，取后11位为发送号码
                if (ballTel.length() == 14) {
                    char[] Tel = ballTel.toCharArray();
                    ballTel = new String(Tel, 3, 11);
                }
                //如果发送方号码为报警球号码，截取短信，存入客户端
                dbHelper = new DBOpenHelper(context, DB_NAME, oldVersion);
                ballInfoDb = new BallInfoDb();
                ballArrayList = ballInfoDb.QueryColumns(dbHelper, BallInfoDb.BALL_TEL, ballTel);
                //提取短信首字符
                char[] firstSms = smsText.toCharArray();
                String firString = new String(firstSms, 0, 1);
                //确认是报警球号码所发
                if (!ballArrayList.isEmpty()) {
                    //如果收到的短信为OK！或者以B打头的状态短信，截取短信
                    if ((smsText.length() == 3 && smsText.equalsIgnoreCase("OK!")) ||
                            (smsText.length() == 33 && firString.equalsIgnoreCase("B"))) {
                        //取消广播，使系统收不到此短信
                        abortBroadcast();
                    }
                }
                dbHelper.close();
            }
        }
    }
}
