package com.shdcec.alarmball.phone;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.widget.Toast;

import com.shdcec.alarmball.R;
import com.shdcec.alarmball.data.DBOpenHelper;
import com.shdcec.alarmball.data.SmsDb;

/**
 * 监听命令短信的发送状态
 */
public class SendStateReceiver extends BroadcastReceiver
{
	final static String DB_NAME = "AlarmBall.db";
	final static int OLD_VERSION = 1; 
	MediaPlayer mp;
	DBOpenHelper dbHelper;
	SmsDb smsDb;
	@Override
	public void onReceive(Context context, Intent intent)
	{
		switch (getResultCode()) 
		{  
        case Activity.RESULT_OK:  
        	//提示音
        	mp = MediaPlayer.create(context, R.raw.sentmessage);
        	Toast.makeText(context, "指令发送成功", Toast.LENGTH_SHORT).show();  
        	break;  
        default:  
        	Toast.makeText(context, "指令发送失败", Toast.LENGTH_LONG).show();
        	//如果短信发送失败，把短信从数据库删除
        	dbHelper = new DBOpenHelper(context, DB_NAME, OLD_VERSION);
        	smsDb = new SmsDb();
        	smsDb.Delete(dbHelper);
        	dbHelper.close();
        	break;  
        }
		mp.start();
	}
}
