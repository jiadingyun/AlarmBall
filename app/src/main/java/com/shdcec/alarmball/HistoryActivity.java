package com.shdcec.alarmball;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.shdcec.alarmball.data.DBOpenHelper;
import com.shdcec.alarmball.data.SmsDb;

@SuppressWarnings("deprecation")
public class HistoryActivity extends TabActivity 
{
	final static String DB_NAME = "AlarmBall.db3";
	final static String QUERY_TIME = "按日期查询";
	final static String SHARED_PREFERENCES = "AlarmBall";
	final static String STARTDATE_SHARED = "startdate";
	final static String STARTDATE_YEAR_SHARED  = "startdate-year";
	final static String STARTDATE_MONTH_SHARED = "startdate-month";
	final static String STARTDATE_DAY_SHARED   = "startdate-day";
	final static String ENDDATE_SHARED   = "enddate";
	final static String ENDDATE_YEAR_SHARED  = "enddate-year";
	final static String ENDDATE_MONTH_SHARED = "enddate-month";
	final static String ENDDATE_DAY_SHARED   = "enddate-day";
	final static String DATEDIALOG_BUTTON_OK     = "确定";
	final static String DATEDIALOG_BUTTON_CANCEL = "取消";
			
	final int OLD_VERSION = 1;
	final String SEND_TAB_ID    = "sendsms";
	final String RECEIVE_TAB_ID = "receivesms";
	final String ALARM_TAB_ID   = "alarmsms";
	private ArrayList<Map<String, String>> listItems;
	
	Calendar c;
	Button startDateButton;
	Button endDateButton;
	Button backButton;
	ListView sendsmsListView;
	ListView receiveListView;
	ListView alarmListView;
	DBOpenHelper dbHelper;
	SmsDb smsDb;
	String dateString;
	String startDateString,endDateString;
	int dialogyear,dialogmonth,dialogday;
 	SharedPreferences sharedPreferences;
	SharedPreferences.Editor editor;
	
	int year,month,day;
	String nowString;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		final String SEND_TAB    = getString(R.string.sendtab);
		final String RECEIVE_TAB = getString(R.string.receivetab);
		final String ALARM_TAB   = getString(R.string.alarmtab);
		/*
		 * 全屏
		 */
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		setContentView(R.layout.history);
		/*
		 * 获取组件
		 */
		startDateButton = (Button) findViewById(R.id.StartDateButton);
		endDateButton   = (Button) findViewById(R.id.EndDateButton);
		backButton      = (Button) findViewById(R.id.backButton);
		sendsmsListView = (ListView) findViewById(R.id.sendsmslist);
		receiveListView = (ListView) findViewById(R.id.receivesmslist);
		alarmListView   = (ListView) findViewById(R.id.alarmsmslist);
		/*
		 * 返回主界面
		 */
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//进入历史数据界面
	    		Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
				startActivity(intent);
				//动画
				overridePendingTransition(R.anim.dync_in_from_left, R.anim.dync_out_to_right);
				HistoryActivity.this.finish();
			}
		}); 
		/*
		 * 填充TabHost
		 */
		final TabHost tabHost = getTabHost();
		TabSpec receivesmsSpec = tabHost.newTabSpec(RECEIVE_TAB_ID)
				.setIndicator(RECEIVE_TAB)
				.setContent(R.id.receivesmslinear);
		TabSpec alarmsmsSpec = tabHost.newTabSpec(ALARM_TAB_ID)
				.setIndicator(ALARM_TAB)
				.setContent(R.id.alarmsmslinear);
		TabSpec sendsmsSpec = tabHost.newTabSpec(SEND_TAB_ID)
				.setIndicator(SEND_TAB)
				.setContent(R.id.sendsmslinear);
		/*
		 * 添加顺序决定排列顺序
		 */
		tabHost.addTab(alarmsmsSpec);
		tabHost.addTab(receivesmsSpec);
		tabHost.addTab(sendsmsSpec);
		/*
		 * 声明SharedPreferences
		 */
		sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
		editor = sharedPreferences.edit();
		//SetStartDateEndDate
		SetStartDateEndDate();
		/*
		 * 更新ListView内容
		 */
		dbHelper = new DBOpenHelper(this, DB_NAME, OLD_VERSION);
		smsDb = new SmsDb();
		RefreshList();
	
		/*
		 * 更新标签颜色
		 */
		updateTab(tabHost);
		tabHost.setOnTabChangedListener(new OnTabChangeListener()
		{
			
			@Override
			public void onTabChanged(String tabId)
			{
				updateTab(tabHost);
			}
		});
		/*
		 * 获取时间按钮,单击按钮设定查询起始终止日期
		 */
		//开始日期设定
		startDateButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				selectDate(STARTDATE_YEAR_SHARED,STARTDATE_MONTH_SHARED,STARTDATE_DAY_SHARED,STARTDATE_SHARED,startDateButton);
				/*
				//构造开始日期设定窗口,日期为最近1次设置日期，如没有显示为当天
				MyDatePickerDialog startDatePickerDialog = new MyDatePickerDialog(HistoryActivity.this, null
						, inityear
						, initmonth
						, initday); 
				//确认按钮，将日期写入按钮文字及SharedPreferences
				startDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, DATEDIALOG_BUTTON_OK, new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						//更新按钮文字
						String showDialogMonth,showDialogDay;
						if (dialogmonth < 10)
							showDialogMonth = "0" + dialogmonth;
						else 
							showDialogMonth = "" + dialogmonth;
						if (dialogday < 10)
							showDialogDay = "0" + dialogday;
						else
							showDialogDay = "" + dialogday;
						dateString = dialogyear + "-" + showDialogMonth + "-" + showDialogDay;
						startDateButton.setText(dateString);
						//写入SharedPreferences
						editor.putString(STARTDATE_SHARED, dateString);
						editor.putInt(STARTDATE_YEAR_SHARED , dialogyear);
						editor.putInt(STARTDATE_MONTH_SHARED, dialogmonth-1);  //用于传给日期控件，月份-1
						editor.putInt(STARTDATE_DAY_SHARED  , dialogday);
						editor.commit();
						//更新ListView
						RefreshList();
					}
				});
				//取消按钮，什么也不做
				startDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, DATEDIALOG_BUTTON_CANCEL, (android.content.DialogInterface.OnClickListener)null);
				startDatePickerDialog.show();*/
			}
		});
		//结束日期设定 
		endDateButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				selectDate(ENDDATE_YEAR_SHARED,ENDDATE_MONTH_SHARED,ENDDATE_DAY_SHARED,ENDDATE_SHARED,endDateButton);
				//构造开始日期设定窗口
				/*MyDatePickerDialog endDatePickerDialog = new MyDatePickerDialog(HistoryActivity.this, null
						, inityear
						, initmonth
						, initday); 
				//确认按钮，将日期写入按钮文字及SharedPreferences
				endDatePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, DATEDIALOG_BUTTON_OK, new DialogInterface.OnClickListener() 
				{
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						//更新按钮文字
						String showDialogMonth,showDialogDay;
						if (dialogmonth < 10)
							showDialogMonth = "0" + dialogmonth;
						else 
							showDialogMonth = "" + dialogmonth;
						if (dialogday < 10)
							showDialogDay = "0" + dialogday;
						else
							showDialogDay = "" + dialogday;
						dateString = dialogyear + "-" + showDialogMonth + "-" + showDialogDay;
						endDateButton.setText(dateString);
						//写入SharedPreferences
						editor.putString(ENDDATE_SHARED, dateString);
						editor.putInt(ENDDATE_YEAR_SHARED , dialogyear);
						editor.putInt(ENDDATE_MONTH_SHARED, dialogmonth-1);  //用于传给日期控件，月份-1
						editor.putInt(ENDDATE_DAY_SHARED  , dialogday);
						System.out.println("datestring:" + dateString);
						editor.commit();
						//更新ListView
						RefreshList();
					}
				});
				//取消按钮，什么也不做
				endDatePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, DATEDIALOG_BUTTON_CANCEL, (android.content.DialogInterface.OnClickListener)null);
				endDatePickerDialog.show();*/
			}
		}); 
		
	}
	/*
     * 更新Tab标签的颜色，和字体的颜色 
     * @param tabHost 
     */  
    private void updateTab(final TabHost tabHost) {  
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {  
            View view = tabHost.getTabWidget().getChildAt(i);  
            TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);  
            if (tabHost.getCurrentTab() == i) {//选中  
                view.setBackgroundResource(R.color.candyblue);//选中后的背景
                tv.setTextColor(this.getResources().getColorStateList(android.R.color.white));  
            } else {//不选中  
            	view.setBackgroundResource(R.color.candygreen);//非选择的背景
                tv.setTextColor(this.getResources().getColorStateList(android.R.color.white));  
            }  
        }  
    }  
	/*
	 * 退出界面时断开数据库
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		if (dbHelper != null) 
		{
			dbHelper.close();
		}
	}
	/*
	 * 每次加载消除notification里通知
	 * 结束日期设定为当天值
	 */
	@Override
	protected void onResume()
	{
		//清除所有通知
		NotificationManager notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll();
		//SetStartDateEndDate
		SetStartDateEndDate();
		super.onResume();
	}
	/*
	 * 获取当天日期
	 * 设定开始日期为前次设定日期，结束日期为当天日期
	 * 更新按钮文字
	 */
	private void SetStartDateEndDate()
	{
		/*
		 * 获取当天日期
		 */
		c = Calendar.getInstance();
		year  = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH)+1;
		day   = c.get(Calendar.DAY_OF_MONTH);
		String tempMonth,tempDay;
		if (month < 10)
			tempMonth = "0" + month;
		else 
			tempMonth = "" + month;
		if (day < 10)
			tempDay = "0" + day;
		else
			tempDay = "" + day;
		nowString = year + "-" + tempMonth + "-" + tempDay;
		//结束日期，与结束年月日写入SharedPreferences
		editor.putString(ENDDATE_SHARED, nowString);
		editor.putInt(ENDDATE_YEAR_SHARED , year);
		editor.putInt(ENDDATE_MONTH_SHARED, month-1);  
		editor.putInt(ENDDATE_DAY_SHARED  , day);
		editor.commit();
		//开始与结束日期
		startDateString = sharedPreferences.getString(STARTDATE_SHARED, nowString);
		endDateString   = sharedPreferences.getString(ENDDATE_SHARED, nowString);
		//更新按钮文字
		startDateButton.setText(startDateString);
		endDateButton.setText(endDateString);
	}
	/*
	 * 刷新ListView
	 */
	private void RefreshList()
	{
		//获取更新的开始结束日期
		startDateString = sharedPreferences.getString(STARTDATE_SHARED, nowString);
		endDateString   = sharedPreferences.getString(ENDDATE_SHARED, nowString);
		//todo:更新发送短信ListView
		String sqlStringSend = "select * from smsInfo where smsType = 'send' and time between '" + startDateString +
				"' and '" + endDateString + " 24:59:59' order by time desc";
		listItems = smsDb.Query(dbHelper, sqlStringSend);
		SimpleAdapter sendAdapter = new SimpleAdapter(HistoryActivity.this, listItems, R.layout.sendsms,
				new String[] {SmsDb.SMS_TIME, SmsDb.SMS_POS, SmsDb.SMS_TEXT, SmsDb.SMS_TO_NUM },
				new int[] {R.id.timetext, R.id.postext, R.id.smstext, R.id.tomumtext});
		sendsmsListView.setAdapter(sendAdapter);
		//todo:更新接收短信ListView
		String sqlStringReceive = "select * from smsInfo where (smsType = 'receive' or smsType = 'alarm') and time between '"+ startDateString +"' " +
				"and '"+ endDateString +" 24:59:59' order by time desc";
		listItems = smsDb.Query(dbHelper,sqlStringReceive);
		SimpleAdapter receiveAdapter = new SimpleAdapter(HistoryActivity.this, listItems, R.layout.receivesms,
				new String[] {"time", "pos", "smsText", "fromNum" },
				new int[] {R.id.timetext, R.id.postext, R.id.smstext, R.id.fromnumtext});
		receiveListView.setAdapter(receiveAdapter);
		//todo:更新报警短信ListView
		String sqlStringAlarm = "select * from smsInfo where smsType = 'alarm' and time between '"+ startDateString +"' " +
				"and '"+ endDateString +" 24:59:59' order by time desc";
		listItems = smsDb.Query(dbHelper, sqlStringAlarm);
		SimpleAdapter alarmAdapter = new SimpleAdapter(this, listItems, R.layout.receivesms, 
				new String[] {"time", "pos", "smsText", "fromNum" },
				new int[] {R.id.timetext, R.id.postext, R.id.smstext, R.id.fromnumtext});
		alarmListView.setAdapter(alarmAdapter);
	}
	/*
	 * 打开日期并选择
	 */
	private void selectDate(String yearString, String monthString, String dayString, String spdateString, final Button dateButton)
	{
		final String spYear = yearString;
		final String spMonth = monthString;
		final String spDay = dayString;
		final String spDate = spdateString;
		//弹出框初始日期
		int inityear = sharedPreferences.getInt(spYear, c.get(Calendar.YEAR));
		int initmonth = sharedPreferences.getInt(spMonth, c.get(Calendar.MONTH));
		int initday = sharedPreferences.getInt(spDay, c.get(Calendar.DAY_OF_MONTH));
		String initDate = sharedPreferences.getString(spdateString, nowString);
		dialogyear  = inityear;
		dialogmonth = initmonth+1;
		dialogday = initday;
		//获取组件
		LinearLayout dateLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.hisquerytime, null);
		DatePicker datePicker = (DatePicker) dateLayout.findViewById(R.id.datepicker);
		Button okButton = (Button) dateLayout.findViewById(R.id.okButton);
		Button backButton = (Button) dateLayout.findViewById(R.id.cancelButton);
		final TextView titleTextView = (TextView) dateLayout.findViewById(R.id.title);
		//设定组件开始日期
		titleTextView.setText(initDate);
		datePicker.init(inityear, initmonth, initday, new OnDateChangedListener()
		{
			@Override
			public void onDateChanged(DatePicker view, int year, int monthOfYear,
					int dayOfMonth)
			{
				dialogyear  = year;
				dialogmonth = monthOfYear+1;
				dialogday = dayOfMonth;
				//更新弹出框标题文字
				String showDialogMonth,showDialogDay;
				if (dialogmonth < 10)
					showDialogMonth = "0" + dialogmonth;
				else 
					showDialogMonth = "" + dialogmonth;
				if (dialogday < 10)
					showDialogDay = "0" + dialogday;
				else
					showDialogDay = "" + dialogday;
				String showDateString = dialogyear + "-" + showDialogMonth + "-" + showDialogDay;
				titleTextView.setText(showDateString);
			}
		});
		//打开选择日期窗口
		final AlertDialog dateDialog = new AlertDialog.Builder(HistoryActivity.this)
		.setView(dateLayout)
		.create();
		//设置窗口进入退出动画
		Window dialogWindow = dateDialog.getWindow();
		dialogWindow.setWindowAnimations(R.style.popActivityInOut);
		dateDialog.setCanceledOnTouchOutside(true);
		dateDialog.show();
		//确认按钮，将日期写入按钮文字及SharedPreferences
		okButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//更新按钮文字
				String showDialogMonth,showDialogDay;
				if (dialogmonth < 10)
					showDialogMonth = "0" + dialogmonth;
				else 
					showDialogMonth = "" + dialogmonth;
				if (dialogday < 10)
					showDialogDay = "0" + dialogday;
				else
					showDialogDay = "" + dialogday;
				dateString = dialogyear + "-" + showDialogMonth + "-" + showDialogDay;
				dateButton.setText(dateString);
				//写入SharedPreferences
				editor.putString(spDate, dateString);
				editor.putInt(spYear , dialogyear);
				editor.putInt(spMonth, dialogmonth-1);  //用于传给日期控件，月份-1
				editor.putInt(spDay  , dialogday);
				editor.commit();
				//更新ListView
				RefreshList();
				dateDialog.cancel();
			}
		});
		//返回按钮
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dateDialog.cancel();
			}
		});
	}
	/*
	 * 自定义DatePickerDialog
	 * 重写onDateChanged函数，使滚动滚轮时，实时获取时间
	 
	class MyDatePickerDialog extends DatePickerDialog
	{
		public MyDatePickerDialog(Context context, OnDateSetListener callBack,
				int year, int monthOfYear, int dayOfMonth)
		{
			super(context, callBack, year, monthOfYear, dayOfMonth);
			// TODO Auto-generated constructor stub
			dialogyear  = year;
			dialogmonth = monthOfYear+1;
			dialogday = dayOfMonth;
			System.out.println("init:" + dialogyear + dialogmonth + dialogday);
		}

		@Override
		public void onDateChanged(DatePicker view, int year, int month, int day) 
		{
			// TODO Auto-generated method stub
			super.onDateChanged(view, year, month, day);
			dialogyear  = year;
			dialogmonth = month+1;
			dialogday = day;
			System.out.println("change:" + dialogyear + dialogmonth + dialogday);
		}
	}*/
	

}
