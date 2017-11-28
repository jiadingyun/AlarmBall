package com.shdcec.alarmball;


import java.util.ArrayList;
import java.util.Map;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.telephony.SmsMessage;
import android.text.InputFilter;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shdcec.alarmball.ball.Ball;
//import com.shdcec.alarmball.data.BallInfoDb;
//import com.shdcec.alarmball.data.DBOpenHelper;
//import com.shdcec.alarmball.phone.SendSms;

public class MainActivity extends Activity {
    private final static String DB_NAME = "AlarmBall.db3";                        //数据库名称
    private final static int OLD_VERSION = 1;                                     //数据库版本
    private final static int TOAST_DURATION = 5000;                          //5秒Toast
    private final static int LONG_TOAST_DURATION = 8000;                          //8秒Toast
    private final static int MAX_DISTANCE = 800;                                  //最大报警距离
    private final static int MIN_DISTANCE = 100;                                  //最小报警距离
    private final static String SHARED_PREFERENCES = "AlarmBall";                 //SharedPreferences标志位
    private final static String ORDER_DOWN_NUMBER = "#01";                       //下装后台命令
    private final static String ORDER_SET_DISTANCE = "#02";                       //设置报警距离命令
    private final static String ORDER_QUERY = "#10";                       //查询命令
    private final static String ORDER_OPEN_DET = "#21";                       //打开探头命令
    private final static String ORDER_CLOSE_DET = "#20";                       //关闭探头命令
    private final static String BALL_PSD = "1234";                                //报警球默认密码
    private final static String BALLTEL_SHAREDPREFERENCES = "balltel";            //选中报警球号码SharedPreferences标志位
    private final static String BALLPOS_SHAREDPREFERENCES = "ballpos";            //选中报警球位置SharedPreferences标志位
    private final static String BALLSTATE_SHAREDPREFERENCES = "ballstate";        //选中报警球状态SharedPreferences标志位
    private final static String BALLDISTANCE_SHAREDPREFERENCES = "balldistance";  //选中报警球报警距离SharedPreferences标志位
    private final static String SERVERNUMBER_SHAREDPREFERENCES = "servernumber";  //后台号码SharedPreferences标志位
    private final static String START_TIMES = "starttimes";                       //程序启动次数SharedPreferences标志位
    private final static String SEND_STATE_BROADCAST_INTENT = "SENT_SMS_ACTION";  //短信发送监听Intent的action
    private final static String OPERATION_SETDISTANCE = "setdistance";            //设置报警距离数据库标志位
    private final static String UPDATE_LATER = "updatelater";                     //以后更新SharedPreferences标示
    private int startTimes;                             //程序启动次数
    private ArrayList<Map<String, String>> listItems;   //符合要求数据库记录
    private Intent sendIntent;                          //用于启动BroadcastReceiver监听短信发送状态
    private Ball chooseBall;                            //选中的报警球对象
//    private DBOpenHelper dbHelper;
//    private BallInfoDb ballInfoDb;
//    private SendSms sendSms = new SendSms();
    private SharedPreferences preferences;              //启动次数
    private SharedPreferences.Editor editor;
    private SMSMonitor sMonitor;
    //    private UpdateManager mUpdateManager;
    //组件
    private Button ballinfoButton;
    private Button queryballinfoButton;
    private Button opendetButton;
    private Button closedetButton;
    private Button hisButton;
    private TextView balltelTextView;
    private TextView ballposTextView;
    private TextView ballstateTextView;
    private TextView downnumberTextView;
    private TextView setdistanceTextView;
    private String balltelString;
    private String ballposString;
    private String ballstateString;
    private String downnumberString;
    private String setdistanceString;
    private String balldistanceString;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        final String NOT_CHOOSE_YET = getString(R.string.notchoiceyet);
        final String CHOOSE_BALL_FIRST = getString(R.string.chooseballfirst);
        super.onCreate(savedInstanceState);
        // 全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.activity_main);
        //统计启动次数
        preferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        editor = preferences.edit();
        startTimes = preferences.getInt(START_TIMES, 0);
        editor.putInt(START_TIMES, ++startTimes);
        editor.commit();
        //检查更新
//        mUpdateManager = new UpdateManager(this);
//        mUpdateManager.checkUpdateInfo();
        //获取组件
        ballinfoButton = (Button) findViewById(R.id.ballinfo);
        queryballinfoButton = (Button) findViewById(R.id.queryballinfo);
        opendetButton = (Button) findViewById(R.id.opendet);
        closedetButton = (Button) findViewById(R.id.closedet);
        hisButton = (Button) findViewById(R.id.histroyButton);
        balltelTextView = (TextView) findViewById(R.id.balltel);
        ballposTextView = (TextView) findViewById(R.id.ballpos);
        ballstateTextView = (TextView) findViewById(R.id.ballstate);
        downnumberTextView = (TextView) findViewById(R.id.downnumbertext);
        setdistanceTextView = (TextView) findViewById(R.id.setdistancetext);
        final Intent sendIntent = new Intent(SEND_STATE_BROADCAST_INTENT);
        //打开历史查询界面
//        hisButton.setOnClickListener(new OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
//                startActivity(intent);
//                overridePendingTransition(R.anim.dync_in_from_right, R.anim.dync_out_to_left);
//            }
//        });
        //打开BallInfoActivity等待返回报警球状态数据
        ballinfoButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, BallInfoActivity.class);
                startActivityForResult(intent, 0);
                overridePendingTransition(R.anim.dync_in_from_right, R.anim.dync_out_to_left);
            }
        });
        //发送查询报警球信息短信
        queryballinfoButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (balltelString == NOT_CHOOSE_YET) {
                    Toast.makeText(MainActivity.this, CHOOSE_BALL_FIRST, Toast.LENGTH_SHORT).show();
                    return;
                }
                String orderString = ORDER_QUERY;
//                sendSms.SendSmsText(MainActivity.this, sendIntent, balltelString, orderString);
            }
        });
        //打开探头
        opendetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (balltelString == NOT_CHOOSE_YET) {
                    Toast.makeText(MainActivity.this, CHOOSE_BALL_FIRST, Toast.LENGTH_SHORT).show();
                    return;
                }
                String orderString = ORDER_OPEN_DET + "," + BALL_PSD;
//                sendSms.SendSmsText(MainActivity.this, sendIntent, balltelString, orderString);
            }
        });
        //关闭探头
        closedetButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (balltelString == NOT_CHOOSE_YET) {
                    Toast.makeText(MainActivity.this, CHOOSE_BALL_FIRST, Toast.LENGTH_SHORT).show();
                    return;
                }
                String orderString = ORDER_CLOSE_DET + "," + BALL_PSD;
//                sendSms.SendSmsText(MainActivity.this, sendIntent, balltelString, orderString);
            }
        });
    }


    // 处理从BallInfoActivity选择的报警球信息
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        final String SET_DISTANCE = getString(R.string.setdistance);
        final String METER = getString(R.string.meter);
        final String AFTER_SET_DISTANCE = getString(R.string.aftersetdistance);
        if (requestCode == 0 && resultCode == 0) {
            try {
                //得到返回数据
                chooseBall = (Ball) intent.getSerializableExtra("chooseball");
                balltelString = chooseBall.getballtel();
                ballposString = chooseBall.getballpos();
                ballstateString = chooseBall.getballstate();
                balldistanceString = chooseBall.getballdistance();
                //显示数据
                balltelTextView.setText(balltelString);
                ballposTextView.setText(ballposString);
                ballstateTextView.setText(ballstateString);
                if (balldistanceString == "" || balldistanceString.length() == 0)
                    setdistanceTextView.setText(SET_DISTANCE);
                else
                    setdistanceTextView.setText(AFTER_SET_DISTANCE + balldistanceString + METER);
                //存储数据
                editor.putString(BALLTEL_SHAREDPREFERENCES, balltelString);
                editor.putString(BALLPOS_SHAREDPREFERENCES, ballposString);
                editor.putString(BALLSTATE_SHAREDPREFERENCES, ballstateString);
                editor.putString(BALLDISTANCE_SHAREDPREFERENCES, balldistanceString);
                editor.commit();
                //更新设置报警距离默认距离
                setdistanceString = balldistanceString;
            } catch (Exception e) {
            }
        }
    }

    //打开下载后台号码设置窗口
    public void downnumber(View source) {
        final String DOWN_NUMBER = getString(R.string.downnumber);
        final String DOWN_NUMBER_BUTTON = getString(R.string.downnumberpopupbutton);

        String popupinputString = downnumberString;
        String titleString = DOWN_NUMBER;
        String confirmButtonName = DOWN_NUMBER_BUTTON;
        String smsFront = ORDER_DOWN_NUMBER + "," + BALL_PSD;
        String sharedString = SERVERNUMBER_SHAREDPREFERENCES;
        openPopupWindow(popupinputString, titleString, confirmButtonName, smsFront, sharedString, downnumberTextView);
    }

    //打开设置报警距离设置窗口
    public void setdistance(View source) {
        final String METER = getString(R.string.meter);
        final String SET_DISTANCE = getString(R.string.setdistance);
        final String SET_DISTANCE_BUTTON = getString(R.string.setdistancepopupbutton);

        String popupinputString = setdistanceString;
        String titleString = SET_DISTANCE + "（" + METER + "）";
        String confirmButtonName = SET_DISTANCE_BUTTON;
        String smsFront = ORDER_SET_DISTANCE + "," + BALL_PSD;
        String sharedString = BALLDISTANCE_SHAREDPREFERENCES;
        openPopupWindow(popupinputString, titleString, confirmButtonName, smsFront, sharedString, setdistanceTextView);
    }

    //openPopupWindow(主界面按钮显示的文本，弹出框标题，确认按钮文字，消息前缀，存储到SharedPreferences的标识，相关桌面TextView控件)
    private void openPopupWindow(String popinput, String title, String confirmButtonName, final String smsFront, final String sharedPreferencessString
            , final TextView textView) {
        final String NOT_CHOOSE_YET = getString(R.string.notchoiceyet);
        final String DOWN_NUMBER = getString(R.string.downnumber);
        final String CHOOSE_BALL_FIRST = getString(R.string.chooseballfirst);
        final String METER = getString(R.string.meter);
        final String AFTER_DOWN_NUMBER = getString(R.string.afterdownnumber);
        final String AFTER_SET_DISTANCE = getString(R.string.aftersetdistance);
        final String DISTANCE_OUT_OF_RANGE = getString(R.string.distanceoutofrange);
        final String SET_DISTANCE = getString(R.string.setdistance);
        final String ONLY_SET_BUTTON = getString(R.string.onlysetbutton);

        if (balltelString == NOT_CHOOSE_YET) {
            Toast.makeText(this, CHOOSE_BALL_FIRST, Toast.LENGTH_SHORT).show();
            return;
        }
        //设定弹出框输入框默认值
        LinearLayout layout = new LinearLayout(this);
        final EditText editText;
        if (textView == downnumberTextView) {
            layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialogdownnumber, null);
            editText = (EditText) layout.findViewById(R.id.setservernum);
        } else {
            layout = (LinearLayout) getLayoutInflater().inflate(R.layout.dialogsetdistance, null);
            editText = (EditText) layout.findViewById(R.id.setalarmdistance);
            editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        }
        if (popinput == DOWN_NUMBER || popinput == SET_DISTANCE) {
            popinput = "";
        }
        editText.setText(popinput);
        //创建对话框
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setView(layout)
                //发送短信
                .setPositiveButton(confirmButtonName, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取输入框中内容,如果设置报警距离，首先判断是否值在1-8之间，再将用户输入值*100再发送
                        String inputString = editText.getText().toString();
                        String sendinputString = inputString;
                        if (textView == setdistanceTextView && inputString.length() != 0) {
                            int senddistance = (int) (Double.parseDouble(inputString) * 100);
                            if (senddistance >= MIN_DISTANCE && senddistance <= MAX_DISTANCE)
                                sendinputString = Integer.toString(senddistance);
                            else {
                                Toast.makeText(MainActivity.this, DISTANCE_OUT_OF_RANGE, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        //如果输入框有内容，发送信息，将号码存储在手机中，并更新界面信息，将报警距离写入数据库
                        if (inputString != "" && inputString.length() != 0) {
                            //发送短信
                            String orderString = smsFront + "," + sendinputString;
//                            sendSms.SendSmsText(MainActivity.this, sendIntent, balltelString, orderString);
                            //保存信息到SharedPreferences
                            editor.putString(sharedPreferencessString, inputString);
                            editor.commit();
                            //更新界面信息
                            if (textView == downnumberTextView) {
                                downnumberString = inputString;
                                textView.setText(AFTER_DOWN_NUMBER + downnumberString);
                            }
                            if (textView == setdistanceTextView) {
                                setdistanceString = inputString;
                                setdistanceTextView.setText(AFTER_SET_DISTANCE + setdistanceString + METER);
                            }
                            //将报警距离写入数据库
//                            dbHelper = new DBOpenHelper(MainActivity.this, DB_NAME, OLD_VERSION);
//                            ballInfoDb = new BallInfoDb();
//                            ballInfoDb.ModifyBallInfo(OPERATION_SETDISTANCE, MainActivity.this, dbHelper, null, null, null, setdistanceString, balltelString);
//                            dbHelper.close();
                        } else {
                            //输入入框无内容，更新界面信息，不发送短信
                            if (textView == downnumberTextView) {
                                downnumberString = inputString;
                                textView.setText(DOWN_NUMBER);
                            }
                            if (textView == setdistanceTextView) {
                                setdistanceString = inputString;
                                setdistanceTextView.setText(SET_DISTANCE);
                            }
                        }

                    }
                })
                //不发短信，只设定后台号码
                .setNegativeButton(ONLY_SET_BUTTON, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取输入框中内容
                        String inputString = editText.getText().toString();
                        if (textView == setdistanceTextView && inputString.length() != 0) {
                            int senddistance = (int) (Double.parseDouble(inputString) * 100);
                            if (senddistance >= MIN_DISTANCE && senddistance <= MAX_DISTANCE) {
                            } else {
                                Toast.makeText(MainActivity.this, DISTANCE_OUT_OF_RANGE, Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                        //如果输入框有内容，将号码存储在手机中，并更新界面信息
                        if (inputString != "" && inputString.length() != 0) {
                            //保存信息到SharedPreferences
                            editor.putString(sharedPreferencessString, inputString);
                            editor.commit();
                            //更新界面信息
                            if (textView == downnumberTextView) {
                                downnumberString = inputString;
                                textView.setText(AFTER_DOWN_NUMBER + downnumberString);
                            }
                            if (textView == setdistanceTextView) {
                                setdistanceString = inputString;
                                textView.setText(AFTER_SET_DISTANCE + setdistanceString + METER);
                            }

                        } else {
                            //输入入框无内容，更新界面信息
                            if (textView == downnumberTextView) {
                                downnumberString = inputString;
                                textView.setText(DOWN_NUMBER);
                            }
                            if (textView == setdistanceTextView) {
                                setdistanceString = inputString;
                                textView.setText(SET_DISTANCE);
                            }
                        }
                    }
                })
                .create();
        //窗口透明
        /*Window alertdialogWindow = alertDialog.getWindow();
    	WindowManager.LayoutParams lp = alertdialogWindow.getAttributes();
    	lp.alpha = 0.8f;
    	alertdialogWindow.setAttributes(lp);
    	*/
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.show();
    }

    /*
     * 内部类，监听接收短信更新报警球状态
     * 截取报警球所发短信，取消广播，不让系统接收短信
     * 更新主界面信息
     */
    class SMSMonitor extends BroadcastReceiver {
        final static String DB_NAME = "AlarmBall.db3";
        final static int OLD_VERSION = 1;
        final static String BALL_SAFE = "安全";
        final static String BALL_NEW_ALARM = "新报警";
        final static String BALL_STILL_ALARM = "持续报警";
        final static String SAFE = "11";
        final static String NEW_ALARM = "00";
        final static String STILL_ALARM = "01";
        final String SET_DISTANCE = getString(R.string.setdistance);
        final String METER = getString(R.string.meter);
        final String AFTER_SET_DISTANCE = getString(R.string.aftersetdistance);

        private String ballTel;
        private String smsText;
        private String ballState;
        private String mainActivityBallTel;
        private String alarmDistance;
//        private DBOpenHelper dbHelper;
//        private BallInfoDb ballInfoDb;
        private ArrayList<Map<String, String>> ballArrayList;


        @Override
        public void onReceive(Context context, Intent intent) {
            //判断是否是接收短信激活的服务
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                //接收由SMS传过来的数据
                Bundle bundle = intent.getExtras();
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
                    //获取发送方号码
                    for (SmsMessage message : messages) {
                        ballTel = message.getDisplayOriginatingAddress();
                        smsText = message.getDisplayMessageBody();
                    }
                    if (ballTel.length() == 14) {
                        char[] balltel = ballTel.toCharArray();
                        ballTel = new String(balltel, 3, 11);
                    }
                    //遍历数据库，确认发送方号码为报警球号码
//                    dbHelper = new DBOpenHelper(context, DB_NAME, OLD_VERSION);
//                    ballInfoDb = new BallInfoDb();
//                    String sqlString = "select * from ballinfo where balltel = '" + ballTel + "'";
//                    ballArrayList = ballInfoDb.Query(dbHelper, sqlString);
                    //提取短信首字符
                    char[] firstsms = smsText.toCharArray();
                    String firString = new String(firstsms, 0, 1);
                    //确认是报警球号码所发
                    if (ballArrayList.isEmpty()) {
                    } else {
						/*
						 * 状态信息根据短信内容修改
						 * B0000000,700,197,00040,00,2,300,1
						 * B0000000--1到8位，报警球硬件编号
						 * 700--10到12位，报警距离
						 * 197--14到16位，感应电压
						 * 00040--18到22位，报警次数
						 * 00--24到25位，报警球状态
						 * 2--27位，报警探头
						 * 300--29-31位，报警时距离
						 * 1--33位，声音短信状态
						 */
                        if (smsText.length() == 33 && firString.equalsIgnoreCase("B")) {
                            char[] sms = smsText.toCharArray();
                            //得到报警状态
                            ballState = new String(sms, 23, 2);
                            //得到报警距离
                            alarmDistance = new String(sms, 9, 3);
                            double senddistance = (double) (Double.parseDouble(alarmDistance) / 100);
                            alarmDistance = Double.toString(senddistance);
                            //状态11，表示恢复安全或误报，改变报警球状态
                            if (ballState.equalsIgnoreCase(SAFE)) {
                                ballState = BALL_SAFE;
                            }
                            //状态00，表示新报警，改变报警球状态
                            if (ballState.equalsIgnoreCase(NEW_ALARM)) {
                                ballState = BALL_NEW_ALARM;
                            }
                            //状态01，表示新报警，存储短信, 更新数据库信息
                            if (ballState.equalsIgnoreCase(STILL_ALARM)) {
                                ballState = BALL_STILL_ALARM;
                            }
                            //更新SharedPreferences和主界面信息
                            preferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
                            mainActivityBallTel = preferences.getString(BALLTEL_SHAREDPREFERENCES, null);
                            //判断发送号码是否为当前选择报警球号码
                            if (mainActivityBallTel.equalsIgnoreCase(ballTel)) {
                                //更新Sharedpreferences
                                //editor = preferences.edit();
                                editor.putString(BALLSTATE_SHAREDPREFERENCES, ballState);
                                editor.commit();
                                //更新主界面信息
                                //报警球状态
                                ballstateTextView.setText(ballState);
                                //设定距离显示
                                if (alarmDistance == "" || alarmDistance.length() == 0)
                                    setdistanceTextView.setText(SET_DISTANCE);
                                else
                                    setdistanceTextView.setText(AFTER_SET_DISTANCE + alarmDistance + METER);
                                //设定距离默认值
                                setdistanceString = alarmDistance;
                            }
                        }
                    }
//                    dbHelper.close();
                }
            }
        }

    }

    /*
     * 每次加载界面
     * 启动监听器
     * 获取Sharedpreferences
     * 确认选中的报警球在数据库中
     * 更新界面组件
     */
    @Override
    protected void onResume() {
        final String NOT_CHOOSE_YET = getString(R.string.notchoiceyet);
        final String DOWN_NUMBER = getString(R.string.downnumber);
        final String SET_DISTANCE = getString(R.string.setdistance);
        final String METER = getString(R.string.meter);
        final String AFTER_DOWN_NUMBER = getString(R.string.afterdownnumber);
        final String AFTER_SET_DISTANCE = getString(R.string.aftersetdistance);

        balltelTextView = (TextView) findViewById(R.id.balltel);
        ballposTextView = (TextView) findViewById(R.id.ballpos);
        ballstateTextView = (TextView) findViewById(R.id.ballstate);
        downnumberTextView = (TextView) findViewById(R.id.downnumbertext);
        setdistanceTextView = (TextView) findViewById(R.id.setdistancetext);
        //启动MainActivity短信监听器和BallInfoActivity内部短信监听器
        sMonitor = new SMSMonitor();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(600);
        registerReceiver(sMonitor, filter);
        //获取只能被本应用程序读写的SharedPreferences对象
        preferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        editor = preferences.edit();
        //获取SharedPreferences中存储的报警球信息，后台手机号,显示在主界面上,如果获取的报警球信息无法在数据库中遍历到，显示未选择
        balltelString = preferences.getString(BALLTEL_SHAREDPREFERENCES, NOT_CHOOSE_YET);
        ballposString = preferences.getString(BALLPOS_SHAREDPREFERENCES, NOT_CHOOSE_YET);
        ballstateString = preferences.getString(BALLSTATE_SHAREDPREFERENCES, NOT_CHOOSE_YET);
        //遍历数据库，查找是否有对应报警球
//        dbHelper = new DBOpenHelper(MainActivity.this, DB_NAME, OLD_VERSION);
//        ballInfoDb = new BallInfoDb();
//        String sqlString = "select * from ballinfo where balltel = '" + balltelString + "'";
//        listItems = ballInfoDb.Query(dbHelper, sqlString);
//        if (listItems.isEmpty()) {
//            balltelString = NOT_CHOOSE_YET;
//            ballposString = NOT_CHOOSE_YET;
//            ballstateString = NOT_CHOOSE_YET;
//        }
//        dbHelper.close();
        balltelTextView.setText(balltelString);
        ballposTextView.setText(ballposString);
        ballstateTextView.setText(ballstateString);
        //获取SharedPreferences中存储的后台号码和报警距离，根据内容显示
        downnumberString = preferences.getString(SERVERNUMBER_SHAREDPREFERENCES, "");
        setdistanceString = preferences.getString(BALLDISTANCE_SHAREDPREFERENCES, "");
        if (downnumberString == "" || downnumberString.length() == 0)
            downnumberTextView.setText(DOWN_NUMBER);
        else
            downnumberTextView.setText(AFTER_DOWN_NUMBER + downnumberString);
        if (setdistanceString == "" || setdistanceString.length() == 0)
            setdistanceTextView.setText(SET_DISTANCE);
        else
            setdistanceTextView.setText(AFTER_SET_DISTANCE + setdistanceString + METER);
        super.onResume();
    }

    // 退出界面时，暂停短信监视器
    @Override
    protected void onPause() {
        unregisterReceiver(sMonitor);
        super.onPause();
    }

    //menu菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SubMenu checkUpdateMenu = menu.addSubMenu("检查更新");
        return super.onCreateOptionsMenu(menu);
    }

    //    //程序更新
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item)
//    {
//        if (item.getTitle() == "检查更新")
//        {
//            saveUpdateLater(false);
//            mUpdateManager.checkUpdateInfo();
//        }
//        return super.onOptionsItemSelected(item);
//    }
    //存储以后更新标志位
    private void saveUpdateLater(boolean update) {
        preferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        editor = preferences.edit();
        editor.putBoolean(UPDATE_LATER, update);
        editor.commit();
    }

}
