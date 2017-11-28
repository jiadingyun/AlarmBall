package com.shdcec.alarmball;

import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.shdcec.alarmball.ball.Ball;
import com.shdcec.alarmball.data.BallInfoDb;
import com.shdcec.alarmball.data.DBOpenHelper;
import com.shdcec.alarmball.dialog.AddBallActivity;


public class BallInfoActivity extends Activity {
    final static String DB_NAME = "AlarmBall.db";
    final static int OLD_VERSION = 1;
    final static String OPERATION_MANUAL = "manul";
    final static int REQUEST_ADD_BALL_ACTIVITY = 0;
    final static int REQUEST_MODIFY_BALL_ACTIVITY = 1;
    final static int RESULT_ADDBALLACTIVITY = 0;
    final static int RESULT_MODIFYBALLACTIVITY = 0;
    final static String MENU_DETAIL = "详细信息";
    final static String EXTRA_BALLTEL = "extra_balltel";
    final static String EXTRA_BALLPOS = "extra_ballpos";
    final static String BUNDLE_BALLTEL = "balltel";
    final static String BUNDLE_BALLSTATE = "ballstate";
    final static String BUNDLE_BALLDISTANCE = "balldistance";
    final static String BUNDLE_BALLPOS = "ballpos";

    private String balltel, ballpos, ballstate, balldistance;
    private DBOpenHelper dbHelper;
    private Ball chooseBall;
    private Ball insertBall;
    private Ball modifyBall;
    private BallInfoDb ballInfoDb;
    private ArrayList<Map<String, String>> listItems;
    private SMSMonitor ballMonitor;
    private String oldBallTel;

    ListView ballinfoListView;
    Button addBallButton, backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
		 * /全屏
		 */
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.ballinfo);
		/*
		 * 获取组件
		 */
        ballinfoListView = (ListView) findViewById(R.id.ballinfolist);
        addBallButton = (Button) findViewById(R.id.addBallButton);
        backButton = (Button) findViewById(R.id.backButton);
		/*
		 * /连接数据库，查找报警球信息
		 */
        dbHelper = new DBOpenHelper(this, DB_NAME, OLD_VERSION);
        //调用查询报警球工具类，返回查询结果
        ballInfoDb = new BallInfoDb();
        listItems = ballInfoDb.Query(dbHelper);
        //将显示结果下载在ListView上
        final SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.ballinfolist,
                new String[]{"balltel", "ballpos", "ballstate", "balldistance"},
                new int[]{R.id.balltel, R.id.ballpos, R.id.ballstate, R.id.balldistance});
        ballinfoListView.setAdapter(adapter);
		/*
		 * 打开添加报警球界面
		 */
        addBallButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
				//打开添加报警球对话框
				Intent intent = new Intent(BallInfoActivity.this, AddBallActivity.class);
				startActivityForResult(intent, REQUEST_ADD_BALL_ACTIVITY);
				//overridePendingTransition(R.anim.dync_in_from_down, R.anim.dync_out_to_up);
            }
        });
		/*
		 * 返回主界面
		 */
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BallInfoActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.dync_in_from_left, R.anim.dync_out_to_right);
                BallInfoActivity.this.finish();
            }
        });
		/*
		 * 获取点击ListView信息，封装起来返回给MainActivity
		 */
        ballinfoListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //获取ListView中对应的balltel，ballpos，ballstate，打包到Ball对象里
                balltel = listItems.get(position).get("balltel");
                ballpos = listItems.get(position).get("ballpos");
                ballstate = listItems.get(position).get("ballstate");
                balldistance = listItems.get(position).get("balldistance");
                chooseBall = new Ball(balltel, ballpos, ballstate, balldistance);
                //使用Bundle将chooseBall信息传回MainActivity
                Intent intent = getIntent();
                Bundle dataBundle = new Bundle();
                dataBundle.putSerializable("chooseball", chooseBall);
                intent.putExtras(dataBundle);
                BallInfoActivity.this.setResult(0, intent);
                finish();
            }
        });
		/*
		 * 长按ListView某一项弹出选择 1,详细信息    2，修改    3，删除
		 */
        ballinfoListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            //String[] items = new String[] { MENU_MODIFY, MENU_DELETE };
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view,
                                           int position, long id) {
                balltel = listItems.get(position).get("balltel");
                ballpos = listItems.get(position).get("ballpos");
                ballstate = listItems.get(position).get("ballstate");
                balldistance = listItems.get(position).get("balldistance");
                oldBallTel = balltel;
 				/*
 				 * 打开选择窗口
 				 */
                LinearLayout longClickLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.balllongclick, null);
                //获取组件
                Button detailButton = longClickLayout.findViewById(R.id.detailButton);
                Button modifyButton = longClickLayout.findViewById(R.id.modifyButton);
                Button deleteButton = longClickLayout.findViewById(R.id.deleteButton);
                //打开窗口
                final AlertDialog longAlertDialog = new AlertDialog.Builder(BallInfoActivity.this).setView(longClickLayout).create();
                Window dialogWindow = longAlertDialog.getWindow();
                dialogWindow.setWindowAnimations(R.style.popActivityInOut);
                longAlertDialog.setCanceledOnTouchOutside(true);
                longAlertDialog.show();
                //打开详细信息界面
                detailButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
//						Intent intent = new Intent(BallInfoActivity.this, BallDetailActivity.class);
//						//将选中报警球号码传给详细信息界面
//						intent.putExtra(BUNDLE_BALLTEL, balltel);
//						intent.putExtra(BUNDLE_BALLPOS, ballpos);
//						intent.putExtra(BUNDLE_BALLSTATE, ballstate);
//						intent.putExtra(BUNDLE_BALLDISTANCE, balldistance);
//						//启动详细信息界面
//						startActivity(intent);
//						//动画
//						overridePendingTransition(R.anim.dync_in_from_right, R.anim.dync_out_to_left);
//						longAlertDialog.cancel();
//						finish();
                    }
                });
                //弹出修改窗口
                modifyButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//						//将原有报警球号码和位置传给弹出窗口，并启动
//						Intent intent = new Intent(BallInfoActivity.this, ModifyBallActivity.class);
//						intent.putExtra(EXTRA_BALLTEL, balltel);
//						intent.putExtra(EXTRA_BALLPOS, ballpos);
//						startActivityForResult(intent, REQUEST_MODIFY_BALL_ACTIVITY);
//						longAlertDialog.cancel();
                    }
                });
                //删除报警球信息
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ballInfoDb.DeleteBallInfo(BallInfoActivity.this, dbHelper, balltel);
                        //刷新LIstView
                        RefreshList();
                        longAlertDialog.cancel();
                    }
                });

                return true;

            }
        });
    }

    /*
     * 退出本界面时断开数据库连接
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    /*
     * 更新ListView内容,更新listItems
     */
    private void RefreshList() {
        ListView ballinfoListView = (ListView) findViewById(R.id.ballinfolist);
//		String sqlString = "select * from ballinfo";
        ballInfoDb = new BallInfoDb();
        listItems = ballInfoDb.Query(dbHelper);
        SimpleAdapter adapter = new SimpleAdapter(this, listItems, R.layout.ballinfolist,
                new String[]{"balltel", "ballpos", "ballstate", "balldistance"},
                new int[]{R.id.balltel, R.id.ballpos, R.id.ballstate, R.id.balldistance});
        ballinfoListView.setAdapter(adapter);
    }

    /*
     * 插入或修改SQLite中报警球信息
     * Modifyballinfo(操作类型, 布局文件, 选中时报警球手机号)
     */
    private void Modifyball(String doString, String ballTel, String ballPos, String oldballtel) {
        final String TOAST_INPUT_INFO = getString(R.string.toastinputinfo);
        final String STATE_NORMAL = getString(R.string.ballstatenormal);
        //获取用户输入的报警球号码和报警球位置
        if (ballTel.length() == 0 || ballPos.length() == 0) {
            Toast.makeText(BallInfoActivity.this, TOAST_INPUT_INFO, Toast.LENGTH_SHORT).show();
            return;
        }
        //插入报警球信息
        if (doString == "Insert") {
            //把报警球信息加入到Ball对象
            insertBall = new Ball(ballTel, ballPos, STATE_NORMAL, null);
            //比较后插入报警球信息
            ballInfoDb.InsertBallInfo(this, dbHelper, insertBall.getballtel(), insertBall.getballpos(), insertBall.getballstate());
        }
        //修改报警球信息
        if (doString == "Modify") {
            //把报警球信息加入到Ball对象
            modifyBall = new Ball(ballTel, ballPos, null, null);
            //比较后更新报警球信息
            ballInfoDb.ModifyBallInfo(OPERATION_MANUAL, this, dbHelper, modifyBall.getballtel(), modifyBall.getballpos(), null, null, oldballtel);
        }

    }

    /*
     * 内部类，监听接收BallInfoService发出的广播
     * 如果是报警球发来的带有报警球信息的短信，更新报警球列表信息
     */
    class SMSMonitor extends BroadcastReceiver {
        final static String DB_NAME = "AlarmBall.db";
        final static int OLD_VERSION = 1;
        final static String BALL_SAFE = "安全";
        final static String BALL_NEW_ALARM = "新报警";
        final static String SAFE = "11";
        final static String NEW_ALARM = "00";

        private String ballTel;
        private String smsText;
        private DBOpenHelper dbHelper;
        private BallInfoDb ballInfoDb;
        private ArrayList<Map<String, String>> ballArrayList;


        @Override
        public void onReceive(Context context, Intent intent) {
            //System.out.println("ballinfo monitor start");
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
                        char[] balltel = ballTel.toCharArray();
                        ballTel = new String(balltel, 3, 11);
                    }
                    //遍历数据库，确认发送方号码为报警球号码
                    dbHelper = new DBOpenHelper(context, DB_NAME, OLD_VERSION);
                    ballInfoDb = new BallInfoDb();
                    String sqlString = "select * from ballinfo where balltel = '" + ballTel + "'";
                    ballArrayList = ballInfoDb.Query(dbHelper);
                    //确认是报警球号码所发，并且带有报警球信息
                    if (ballArrayList.isEmpty()) {
                    } else {
                        //提取短信首字符
                        char[] firstsms = smsText.toCharArray();
                        String firString = new String(firstsms, 0, 1);
                        if (smsText.length() == 33 && firString.equalsIgnoreCase("B")) {
                            RefreshList();
                        }
                    }
                    dbHelper.close();
                }
            }
        }

    }

    /*
     * 每次加载界面启动短信监听器
     */
    @Override
    protected void onResume() {
        //System.out.println("ballinfo monitor in");

        ballMonitor = new SMSMonitor();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.setPriority(700);
        registerReceiver(ballMonitor, filter);
        super.onResume();
    }

    /*
     * 只有当报警球设定界面为当前界面时才需要启动监听器更新ListView
     */
    @Override
    protected void onPause() {
        //退出界面时，暂停短信监视器
        unregisterReceiver(ballMonitor);
        super.onPause();
    }

    /*
     * 获取从联系人界面返回的结果
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        String doString;
        Bundle data;
        String ballTelString, ballPosString;
        switch (requestCode) {
            case REQUEST_ADD_BALL_ACTIVITY:
                try {
                    //获取报警球信息
                    data = intent.getExtras();
                    ballTelString = data.getString(EXTRA_BALLTEL);
                    ballPosString = data.getString(EXTRA_BALLPOS);
                    //插入报警球信息
                    doString = "Insert";
                    Modifyball(doString, ballTelString, ballPosString, null);
                } catch (Exception e) {
                }

                break;
            case REQUEST_MODIFY_BALL_ACTIVITY:
                try {
                    //获取报警球信息
                    data = intent.getExtras();
                    ballTelString = data.getString(EXTRA_BALLTEL);
                    ballPosString = data.getString(EXTRA_BALLPOS);
                    //修改报警球信息
                    doString = "Modify";
                    Modifyball(doString, ballTelString, ballPosString, oldBallTel);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                break;
        }
        //刷新LIstView
        RefreshList();
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);


    }

}
