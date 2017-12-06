package com.shdcec.alarmball.dialog;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.shdcec.alarmball.R;

public class ModifyBallActivity extends Activity
{
	final static int PICK_CONTACT = 0;
	final static int RESULT_BALLINFOACTICITY = 1;
	final static String EXTRA_BALLTEL = "extra_balltel";
	final static String EXTRA_BALLPOS = "extra_ballpos";
	Button openContactButton,okButton,cancelButton;
	EditText ballTelEditText,ballPosEditText;
	String ballTelString,ballPosString;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		/*
		 * 无边框无标题
		 */
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.modifyball);
		/*
		 * 设置窗口弹出淡出动画
		 */
		Window window = getWindow();
		window.setWindowAnimations(R.style.popActivityInOut);
		/*
		 * 调整弹出框宽度
		 */
		//获取屏幕宽度
		 DisplayMetrics metric = new DisplayMetrics();
	     getWindowManager().getDefaultDisplay().getMetrics(metric);
	     int screenWidth = metric.widthPixels;     // 屏幕宽度（像素）
	     //设定Activity宽度
	     android.view.WindowManager.LayoutParams windowLayoutParams = getWindow().getAttributes();
	     windowLayoutParams.width = (int) (screenWidth * 0.9);
		/*
		 * 获取组件
		 */
		ballTelEditText = (EditText) findViewById(R.id.setballtel);
		ballPosEditText = (EditText) findViewById(R.id.setballpos);
		cancelButton = (Button) findViewById(R.id.cancelButton);		
		openContactButton = (Button) findViewById(R.id.opencontact);
		okButton = (Button) findViewById(R.id.okButton);
		/*
		 * 获得原有报警球号码和位置
		 */
		Intent intent = getIntent();
		Bundle date = intent.getExtras();	
		String oldBallTel = date.getString(EXTRA_BALLTEL);
		String oldBallPos = date.getString(EXTRA_BALLPOS);
		ballTelEditText.setText(oldBallTel);
		ballPosEditText.setText(oldBallPos);
		/*
		 * 联系人按钮
		 * 从系统联系人界面中选择联系人号码
		 */
		openContactButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//联系人URI使用了硬编码，使用ContactsContract.Contacts.CONTENT_URI常量来增强移植性
				Intent intent = new Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});
		/*
		 * 确认修改按钮
		 * 将报警球号码和位置传回给BallInfoActivity
		 */
		okButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//获取报警球号码和位置
				ballTelString = ballTelEditText.getText().toString();
				ballPosString = ballPosEditText.getText().toString();
				//传回给BallInfoActivity
				Intent intent = getIntent();
				intent.putExtra(EXTRA_BALLTEL, ballTelString);
				intent.putExtra(EXTRA_BALLPOS, ballPosString);
				ModifyBallActivity.this.setResult(RESULT_BALLINFOACTICITY, intent);
				//关闭窗口
				ModifyBallActivity.this.finish();
			}
		});
		/*
		 * 取消按钮
		 * 关闭弹出窗口
		 */
		cancelButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//关闭窗口
				ModifyBallActivity.this.finish();
			}
		});
	}
	/*
	 * 监听联系人界面获取的联系人号码
	 * 将此号码显示在报警球设定弹出窗口中
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		switch (requestCode)
		{
		case PICK_CONTACT:
			if (resultCode == Activity.RESULT_OK)
			{
				//获取数据
				Uri contactData = data.getData();
				CursorLoader cursorLoader = new CursorLoader(this, contactData, null, null, null, null);
				//查询联系人信息
				Cursor cursor = cursorLoader.loadInBackground();
				//如果查询到指定联系人
				if (cursor.moveToFirst())
				{
					String phoneNumber = "未输入号码";
					//获取联系人ID
					String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
					//获取联系人姓名，作为位置
					String ballPos = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
					//根据联系人ID得到联系人详细信息
					Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							null, 
							ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID, 
							null, null);
					if (phones.moveToFirst())
					{
						//获取联系人号码
						phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
						//去除号码里除数字其它字符
						phoneNumber = phoneNumber.trim();
						String tempPhoneNumber = "";
						if (phoneNumber != null && !"".equals(phoneNumber))
						{
							for (int i = 0; i < phoneNumber.length(); i++)
							{
								if (phoneNumber.charAt(i) >= 48 && phoneNumber.charAt(i) <= 57)
								{
									tempPhoneNumber += phoneNumber.charAt(i);
								}
							}
						}
						phoneNumber = tempPhoneNumber;
					}
					phones.close();
					//号码和位置显示在弹出窗口
					EditText balltelEditText = (EditText) findViewById(R.id.setballtel);
					EditText ballPosEditText = (EditText) findViewById(R.id.setballpos);
					balltelEditText.setText(phoneNumber);
					ballPosEditText.setText(ballPos);
				}
				cursor.close();
			}
			break;
		}
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}
}
