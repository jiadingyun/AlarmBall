package com.shdcec.alarmball.dialog;

import android.Manifest;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;

import com.shdcec.alarmball.R;

public class AddBallActivity extends Activity {
    final static int PICK_CONTACT = 0;
    final static int RESULT_BALL_INFO_ACTIVITY = 0;
    final static String EXTRA_BALLTEL = "extra_balltel";
    final static String EXTRA_BALLPOS = "extra_ballpos";
    Button openContactButton, okButton, cancelButton;
    EditText ballTelEditText, ballPosEditText;
    String ballTelString, ballPosString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.addball);
        /*
         * 设置窗口弹出淡出动画
		 */
//        Window window = getWindow();
//        window.setWindowAnimations(R.style.popActivityInOut);
        /*
         * 调整弹出框宽度
		 */
        //获取屏幕宽度
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        // 屏幕宽度（像素）
        int screenWidth = metric.widthPixels;
        LayoutParams windowLayoutParams = getWindow().getAttributes();//设定Activity宽度
        windowLayoutParams.width = (int) (screenWidth * 0.9);
		/*
		 * 获取组件
		 */
        openContactButton = findViewById(R.id.opencontact);
        okButton = findViewById(R.id.okButton);
        cancelButton = findViewById(R.id.cancelButton);
        ballTelEditText = findViewById(R.id.setballtel);
        ballPosEditText = findViewById(R.id.setballpos);
		/*
		 * 联系人按钮
		 * 从系统联系人界面中选择联系人号码
		 */
        openContactButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //联系人URI使用了硬编码，使用ContactsContract.Contacts.CONTENT_URI常量来增强移植性
                Intent intent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });
		/*
		 * 确认添加按钮
		 * 将报警球号码和位置传回给BallInfoActivity
		 */
        okButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                //获取报警球号码和位置
                ballTelString = ballTelEditText.getText().toString();
                ballPosString = ballPosEditText.getText().toString();
                //传回给BallInfoActivity
                Intent intent = getIntent();
                intent.putExtra(EXTRA_BALLTEL, ballTelString);
                intent.putExtra(EXTRA_BALLPOS, ballPosString);
                AddBallActivity.this.setResult(RESULT_BALL_INFO_ACTIVITY, intent);
                //关闭窗口
                AddBallActivity.this.finish();
            }
        });
		/*
		 * 取消按钮
		 * 关闭弹出窗口
		 */
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭窗口
                AddBallActivity.this.finish();
            }
        });
    }

    /*
     * 监听联系人界面获取的联系人号码
     * 将此号码显示在报警球设定弹出窗口中
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {
                    /**
                     * android6.0系统后增加运行时权限，需要动态添加内存卡读取权限
                     */
                    if (Build.VERSION.SDK_INT >= 23) {

                        int permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS);

                        //判断是否已有对应权限
                        //用户主动赋予过一次后，该应用就一直具有该权限，除非在应用管理中撤销
                        if (permission != PackageManager.PERMISSION_GRANTED) {
                            //没有权限，则需要申请权限

                            //todo:当用户选择“拒绝权限申请，并不再提示”后，仍可能点击该按键
                            // 因此需要弹出提示框，提醒用户该功能需要权限
                            //用户拒绝后，在此点击联系人，再次打开权限申请。这就要用到shouldShowRequestPermissionRationale方法
                            if (!shouldShowRequestPermissionRationale(android.Manifest.permission.READ_CONTACTS)) {
                                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 0);
                                return;
                            }
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS}, 0);
                            return;
                        }

                        //有权限则直接获取电话号码，并拨号
                        //获取数据
                        Uri contactData = data.getData();
                        //查询联系人信息
                        Cursor cursor = managedQuery(contactData, null, null, null, null);
                        //如果查询到指定联系人
                        if (cursor.moveToFirst()) {
                            String phoneNumber = "未输入号码";
                            //获取联系人ID
                            String contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                            //获取联系人姓名，作为位置
                            String ballPos = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                            //根据联系人ID得到联系人详细信息
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactID,
                                    null, null);

                            if (phones.moveToFirst()) {
                                //获取联系人号码
                                phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                //去除号码里除数字其它字符
                                phoneNumber = phoneNumber.trim();
                                String tempPhoneNumber = "";
                                if (phoneNumber != null && !"".equals(phoneNumber)) {
                                    for (int i = 0; i < phoneNumber.length(); i++) {
                                        if (phoneNumber.charAt(i) >= 48 && phoneNumber.charAt(i) <= 57) {
                                            tempPhoneNumber += phoneNumber.charAt(i);
                                        }
                                    }
                                }
                                phoneNumber = tempPhoneNumber;
                            }
                            phones.close();
                            //号码和位置显示在弹出窗口
                            EditText balltelEditText = findViewById(R.id.setballtel);
                            EditText ballPosEditText = findViewById(R.id.setballpos);
                            balltelEditText.setText(phoneNumber);
                            ballPosEditText.setText(ballPos);
                        }
                        if (Build.VERSION.SDK_INT <14) {//不添加的话Android4.0以上系统运行会报错
                            cursor.close();
                        }
                    }


                }
                break;
        }
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
    }
}
