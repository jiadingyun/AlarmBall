package com.shdcec.alarmball;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.shdcec.alarmball.data.BallInfoDb;
import com.shdcec.alarmball.data.DBOpenHelper;
import com.shdcec.alarmball.data.SmsDb;
import com.shdcec.alarmball.utility.InnerListView;

public class BallDetailActivity extends Activity {
    Button backButton;
    TextView ballTelEditText, ballPosEditText, alarmTimesTextView, ballDistanceTextView, ballStateTextView;
    InnerListView hisInfoListView;
    ScrollView scrollView;

    DBOpenHelper dbHelper;
    BallInfoDb ballInfoDb;
    SmsDb smsDb;

    final static int OLD_VERSION = 1;
    final static String ZERO = "0";
    final static String NO_DISTANCE = "未获取";
    String ballTel, ballState, ballPos, ballDistance, alarmTimes;
    ArrayList<Map<String, String>> ballAlarmLists, ballReceiveLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * 全屏
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        setContentView(R.layout.balldetail);
		/*
		 * 获取组件
		 */
        ballTelEditText = findViewById(R.id.ballTelText);
        ballPosEditText = findViewById(R.id.ballPosText);
        alarmTimesTextView = findViewById(R.id.alarmTimesText);
        ballDistanceTextView = findViewById(R.id.ballDistanceText);
        ballStateTextView = findViewById(R.id.ballStateText);
        backButton = findViewById(R.id.backButton);
        scrollView = findViewById(R.id.ScrollView);
        hisInfoListView = findViewById(R.id.hisListView);
		/*
		 * 设定ListView默认高度
		 */
        hisInfoListView.setMinimumHeight(100);
		/*
		 * 返回报警球列表界面
		 */
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BallDetailActivity.this, BallInfoActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.dync_in_from_left, R.anim.dync_out_to_right);
                BallDetailActivity.this.finish();
            }
        });
		/*
		 * 获得BallInfoActivity送来的选中报警球信息
		 * 遍历数据库，获取报警球基本信息，历史报警短信
		 * 显示信息
		 */
        //获取选中报警球号码
        Intent intent = getIntent();
        Bundle date = intent.getExtras();
        ballTel = date.getString(BallInfoDb.BALL_TEL);
        ballPos = date.getString(BallInfoDb.BALL_POS);
        ballDistance = date.getString(BallInfoDb.BALL_DISTANCE);
        ballState = date.getString(BallInfoDb.BALL_STATE);
        //从短信表获取选中报警球报警次数，报警历史
        String sqlString = "select * from smsInfo where fromNum = '" + ballTel + "' and smsType = 'alarm'";
        dbHelper = new DBOpenHelper(this, MainActivity.DB_NAME, MainActivity.OLD_VERSION);
        smsDb = new SmsDb();
        ballAlarmLists = smsDb.Query(dbHelper,sqlString);
        //报警次数
        alarmTimes = Integer.toString(ballAlarmLists.size());
        //显示
        ballTelEditText.setText(ballTel);
        ballStateTextView.setText(ballState);
        ballPosEditText.setText(ballPos);
        if (ballDistance.isEmpty()) {
            ballDistance = NO_DISTANCE;
        }
        ballDistanceTextView.setText(ballDistance);
        alarmTimesTextView.setText(alarmTimes);
        //填充ListView
        sqlString = "select * from smsInfo where (" +
                "fromNum = '" + ballTel + "' or toNum = '" + ballTel + "') and smsText <> '设定成功' " +
                "order by time desc";
        ballReceiveLists = smsDb.Query(dbHelper,sqlString);
        SimpleAdapter receiveAdapter = new SimpleAdapter(BallDetailActivity.this, ballReceiveLists, R.layout.simpleballhis,
                new String[]{SmsDb.SMS_TIME, SmsDb.SMS_TEXT},
                new int[]{R.id.alarmTime, R.id.smsText});
        hisInfoListView.setAdapter(receiveAdapter);
        hisInfoListView.setParentScrollView(scrollView);
        hisInfoListView.setMaxHeight(400);
    }

    /*
     * 退出界面，关闭数据库连接
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

