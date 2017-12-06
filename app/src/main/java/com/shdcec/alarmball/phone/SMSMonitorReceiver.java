package com.shdcec.alarmball.phone;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
//import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.widget.Toast;

import com.shdcec.alarmball.MainActivity;
import com.shdcec.alarmball.R;
import com.shdcec.alarmball.data.BallInfoDb;
import com.shdcec.alarmball.data.DBOpenHelper;
import com.shdcec.alarmball.data.SmsDb;

/**
 * 开机自动启动服务，在mainfest中注册
 * 监听短信
 * 遍历数据库检查发送号码是否报警球号码
 * 报警球发来短信不存入手机短信库
 * 存储短信在客户端中
 */
public class SMSMonitorReceiver extends BroadcastReceiver {

    //判断短信类型
    private static final String SET_SUCCESS = "设定成功";
    private static final String BALL_SAFE = "安全";
    private static final String BALL_NEW_ALARM = "新报警";
    private static final String BALL_STILL_ALARM = "持续报警";
    private static final String SAFE = "11";
    private static final String NEW_ALARM = "00";
    private static final String STILL_ALARM = "01";

    private static final String PACKAGE_NAME = "com.shdcec.alarmball";
    private static final String HISTORY_ACTIVITY_NAME = "com.shdcec.alarmball.HistoryActivity";
    private static final int NOTIFICATION_NUM = 50;
    static int NOTIFICATION_ID = 1;

    private String mainActivityBallTel;
    private String ballTel;
    private String ballState;
    private String numballState;
    private String sqlString;
    private String smsType;
    private String smsText;
    private String showSmsText;
    private String alarmDistance;
    private BallInfoDb ballInfoDb;
    private SmsDb smsDb;
    private ArrayList<Map<String, String>> ballArrayList;
    private DBOpenHelper dbHelper;
    private SimpleDateFormat timeDateFormat;
    private NotificationManager nmManager;
    //private Notification notification;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private MediaPlayer mPlayer;

    //private Uri uri;
    @SuppressWarnings({"static-access", "deprecation"})
    @SuppressLint("SimpleDateFormat")
    @Override
    public void onReceive(Context context, Intent intent) {
        /**
         * 判断是否是接收短信激活的服务
         */
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            //获取当前时间
            timeDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date curDate = new Date(System.currentTimeMillis());
            String nowTime = timeDateFormat.format(curDate);
            //接收由SMS传过来的数据
            Bundle bundle = intent.getExtras();
            /**
             * 判断SMS中是否有数据
             * 解析短信，获取短信发送号码，短信正文
             */
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
                //遍历短信对象，获取发送方号码，短信正文
                for (SmsMessage message : messages) {
                    ballTel = message.getDisplayOriginatingAddress();
                    smsText = message.getDisplayMessageBody();
                }
                //如果发送方号码为14位，表示收到的号码前有+86前缀，取后11位为发送号码
                if (ballTel.length() == 14) {
                    char[] balltel = ballTel.toCharArray();
                    ballTel = new String(balltel, 3, 11);
                }
                //如果发送方号码为报警球号码，截取短信，存入客户端
                dbHelper = new DBOpenHelper(context, MainActivity.DB_NAME, 1);
                ballInfoDb = new BallInfoDb();
                ballArrayList = ballInfoDb.QueryColumns(dbHelper,BallInfoDb.BALL_TEL,ballTel);
                //提取短信首字符
                char[] firstsms = smsText.toCharArray();
                String firString = new String(firstsms, 0, 1);
                //如果发送号码可以在报警球数据库中遍历到，则为报警球号码所发
                if (!ballArrayList.isEmpty()) {
                    /**
                     * 存储消息
                     * 位置信息从数据库中取
                     * 状态信息根据短信内容修改
                     * 插入数据库短信类型（接收还是报警）根据短信内容修改
                     */
                    //报警球位置从数据库获取
                    String ballPos = ballArrayList.get(0).get(BallInfoDb.BALL_POS);
                    //如果回的消息为OK！
                    if (smsText.length() == 3 && smsText.equalsIgnoreCase("OK!")) {
                        //短信类型为receive
                        smsType = SmsDb.SMS_TYPE_RECEIVE;
                        showSmsText = SET_SUCCESS;
                        //存储短信
                        smsDb = new SmsDb();
                        smsDb.Insert(dbHelper, nowTime, ballPos, ballTel, null, showSmsText, smsType);
                    } else if (smsText.length() == 33 && firString.equalsIgnoreCase("B")) {
                        /*
						 * 如果回的消息33位，并且以B打头的为报警球回复的状态短信
						 * B0000000,700,197,00040,00,2,300,1
						 * B0000000 --1到8位，报警球硬件编号
						 * 700      --10到12位，报警距离
						 * 197      --14到16位，感应电压
						 * 00040    --18到22位，报警次数
						 * 00       --24到25位，报警球状态
						 * 2        --27位，报警探头
						 * 300      --29-31位，报警时距离
						 * 1        --33位，声音短信状态
						 */
                        char[] sms = smsText.toCharArray();
                        //得到报警状态
                        numballState = new String(sms, 23, 2);
                        //得到报警距离
                        alarmDistance = new String(sms, 9, 3);
                        double sendDistance = (Double.parseDouble(alarmDistance) / 100);
                        alarmDistance = Double.toString(sendDistance);
                        //状态11，表示恢复安全或误报，存储短信, 更新数据库信息
                        if (numballState.equalsIgnoreCase(SAFE)) {
                            showSmsText = BALL_SAFE;
                            smsType = SmsDb.SMS_TYPE_RECEIVE;
                            ballState = showSmsText;
                        }
                        //状态00，表示新报警，存储短信, 更新数据库信息
                        if (numballState.equalsIgnoreCase(NEW_ALARM)) {
                            showSmsText = BALL_NEW_ALARM;
                            smsType = SmsDb.SMS_TYPE_ALARM;
                            ballState = showSmsText;
                        }
                        //状态01，表示新报警，存储短信, 更新数据库信息
                        if (numballState.equalsIgnoreCase(STILL_ALARM)) {
                            showSmsText = BALL_STILL_ALARM;
                            smsType = SmsDb.SMS_TYPE_ALARM;
                            ballState = showSmsText;
                        }
                        //更新报警球数据库
                        ballInfoDb = new BallInfoDb();
                        ballInfoDb.modifyBallInfo(BallInfoDb.OPERATION_FROM_SMS, context, dbHelper,
                                ballTel, ballPos, ballState, alarmDistance, ballTel);
                        //更新SharedPreferences
                        preferences = context.getSharedPreferences(MainActivity.SHARED_PREFERENCES, Context.MODE_PRIVATE);
                        mainActivityBallTel = preferences.getString(BallInfoDb.BALL_TEL, null);
                        if (mainActivityBallTel.equalsIgnoreCase(ballTel)) {
                            //更新SharedPreferences
                            editor = preferences.edit();
                            editor.putString(BallInfoDb.BALL_STATE, ballState);
                            editor.putString(BallInfoDb.BALL_DISTANCE, alarmDistance);
                            editor.apply();
                        }
                        //存储短信
                        smsDb = new SmsDb();
                        smsDb.Insert(dbHelper, nowTime, ballPos, ballTel, null, showSmsText, smsType);
                    }
					/*
					 * /判断程序是否在前台运行
					 */
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    ComponentName cn = am != null ? am.getRunningTasks(1).get(0).topActivity : null;
                    String currentPackageName = cn != null ? cn.getPackageName() : null;
                    //只有当短信符合报警球发来的要求格式时才考虑通知问题
                    if ((smsText.length() == 3 && smsText.equalsIgnoreCase("OK!"))
                            || (smsText.length() == 33 && firString.equalsIgnoreCase("B"))) {
                        if (!TextUtils.isEmpty(currentPackageName) && currentPackageName.equals(PACKAGE_NAME)) {
                            //程序在前台运行
                            //弹出信息提醒
                            Toast.makeText(context, ballPos + "报警球:" + showSmsText, Toast.LENGTH_LONG).show();
                            //声音提示

                            if (showSmsText == SET_SUCCESS || showSmsText == BALL_SAFE) {
                                //ok或安全，一般声音
                                mPlayer = MediaPlayer.create(context, R.raw.safe);
                            } else {
                                //报警，报警声
                                mPlayer = MediaPlayer.create(context, R.raw.alarm);
                            }
                            mPlayer.start();
                        } else {
                            //程序不在前台运行
                            //只有当收到报警短信时，通知栏通知
                            Intent toIntent;
                            //当短信长度为33时，并以B打头，表示收到的可能包含报警短信
                            if (smsText.length() == 33 && firString.equalsIgnoreCase("B")) {
                                nmManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
                                //如果回复的是报警短信，通过notification通知，并发出报警声音
                                if (ballState.equalsIgnoreCase(BALL_STILL_ALARM) || ballState.equalsIgnoreCase(BALL_NEW_ALARM)) {
                                    try {
                                        toIntent = new Intent(context, Class.forName(HISTORY_ACTIVITY_NAME));
                                        toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);   //加载HistoryActivity为新的Task使界面及时更新
                                        PendingIntent pi = PendingIntent.getActivity(context, 0, toIntent, 0);
                                        Notification notification = new Notification.Builder(context)
                                                .setTicker("报警球状态更新")
                                                .setSmallIcon(R.drawable.alarmballfornotice)
                                                .setContentTitle("报警球状态更新")
                                                .setContentText(ballPos + "报警球:" + ballState + "  号码:" + ballTel)
                                                .setWhen(System.currentTimeMillis())
                                                .setDefaults(Notification.DEFAULT_SOUND)
                                                .setContentIntent(pi)
                                                .setAutoCancel(true)
                                                .getNotification();
                                        nmManager.notify(ballTel, (NOTIFICATION_ID++) % NOTIFICATION_NUM, notification);
                                        //nmManager.notify(1, notification);
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } else if (ballState.equalsIgnoreCase(BALL_SAFE)) {
                                    //如果收到恢复安全的短信，取消对应球的所有notification
                                    for (int i = 0; i <= NOTIFICATION_NUM; i++) {
                                        nmManager.cancel(ballTel, i);
                                    }
                                }
                            }
                        }
                    }
                }
                dbHelper.close();
            }
        }
    }
}
