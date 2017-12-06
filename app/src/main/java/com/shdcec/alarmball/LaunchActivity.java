package com.shdcec.alarmball;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.widget.TextView;

/**
 * 启动页
 * Created by yp on 2017/12/2.
 */

public class LaunchActivity extends AppCompatActivity {
    TextView textView;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    int count;
    String s;
    boolean stopThread = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        sp = getSharedPreferences("startTimes", Context.MODE_PRIVATE);
        count = sp.getInt("startTimes", 0);
        textView = findViewById(R.id.textView_login);
        s = "第：" + count + "次启动";
        textView.setText(s);

        //方法1，直接根据时间跳转
        int time = 0;
        if (count == 0) {
            time = 3000;
            //写入启动次数
            editor = sp.edit();
            editor.putInt("startTimes", ++count);
            editor.apply();
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent1 = new Intent(LaunchActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();
            }
        }, time);
        //方法2，用于每次启动的时候加载耗时任务后直接启动
        //后台处理耗时任务
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                //耗时任务，比如加载网络数据
//                //如果首次启动，就让线程休眠2秒，以便能看到启动页面
//                if (count == 0) {
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    //写入启动次数
//                    editor = sp.edit();
//                    editor.putInt("count", ++count);
//                    editor.apply();
//                }
//                //如果被按下返回键，退出时不再开启MainActivity
//                if (!stopThread) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent intent1 = new Intent(LaunchActivity.this, MainActivity.class);
//                            startActivity(intent1);
//                            finish();
//                        }
//                    });
//                }
//            }
//        }).start();
    }

    private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
//            if ((System.currentTimeMillis() - exitTime) > 2000) {
//                UtilToast.makeText(LaunchActivity.this, "再按一次退出程序", UtilToast.LENGTH_SHORT).show();
//                exitTime = System.currentTimeMillis();
//            } else {
            finish();
            //由于打开MainActivity是在子线程中已经执行，所以这里如果不退出程序的话，finish本活动后，主线程还会让MainActivity启动
//            System.exit(0);
//            //如果推出程序的话，会有卡顿。并且退出程序就不能执行后台
//            stopThread = true;
//            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
