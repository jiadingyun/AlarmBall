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
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.shdcec.alarmball.R;
import com.shdcec.alarmball.utility.UtilToast;

public class AddBallActivity extends Activity {
    final static int PICK_CONTACT = 0;
    final static int RESULT_BALL_INFO_ACTIVITY = 0;
    final static String EXTRA_BALLTEL = "extra_balltel";
    final static String EXTRA_BALLPOS = "extra_ballpos";
    private static final int REQUEST_PERMISSION_SETTING = 0;//权限请求
    Button openContactButton, okButton, cancelButton;
    EditText ballTelEditText, ballPosEditText;
    String ballTelString, ballPosString;
    private Uri contactData;//选中的联系人

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                /**
                 * android6.0系统后增加运行时权限，需要动态添加内存卡读取权限
                 */
                int permission = ActivityCompat.checkSelfPermission(AddBallActivity.this, android.Manifest.permission.READ_CONTACTS);
                //判断是否已有对应权限
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    //没有权限，则需要申请权限
                    //因此需要弹出提示框，提醒用户该功能需要权限
                    //用户主动赋予过一次后，该应用就一直具有该权限，除非在应用管理中撤销
                    //用户拒绝，但不选择“不再提示”就会再次执行询问
                    ActivityCompat.requestPermissions(AddBallActivity.this, new String[]{android.Manifest.permission.READ_CONTACTS}, 0);
                    // 用户拒绝，选择“不再提示”需要去onRequestPermissionsResult中判断，
                    // 如果用户拒绝并选择“不再提示”，能做的只有打开权限设置页面
                    return;
                }
                //授权成功后 进入联系人选择页面
                //联系人URI使用了硬编码，使用ContactsContract.Contacts.CONTENT_URI常量来增强移植性
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(intent, PICK_CONTACT);
            }
        });
        //确认添加按钮，将报警球号码和位置传回给BallInfoActivity
        okButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取报警球号码和位置
                ballTelString = ballTelEditText.getText().toString();
                ballPosString = ballPosEditText.getText().toString();
                Intent intent = new Intent();
                intent.putExtra(EXTRA_BALLTEL, ballTelString);
                intent.putExtra(EXTRA_BALLPOS, ballPosString);
                setResult(RESULT_BALL_INFO_ACTIVITY, intent);
                finish();
            }
        });
        //取消按钮,关闭弹出窗口
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭窗口
                finish();
            }
        });
    }

    @Override//监听联系人界面获取的联系人号码，并显示在报警球设定弹出窗口中
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_CONTACT:
                if (resultCode == Activity.RESULT_OK) {

                    contactData = data.getData();
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
                    if (Build.VERSION.SDK_INT < 14) {//不添加的话Android4.0以上系统运行会报错
                        cursor.close();
                    }
                }
                break;
        }
    }

    @Override //处理申请权限结果
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            // TODO: 需要解决首次申请权限后不能直接安装，而是跳出的问题
            case REQUEST_PERMISSION_SETTING:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //联系人URI使用了硬编码，使用ContactsContract.Contacts.CONTENT_URI常量来增强移植性
                    Intent intent = new Intent(Intent.ACTION_PICK,
                            ContactsContract.Contacts.CONTENT_URI);
                    startActivityForResult(intent, PICK_CONTACT);
                } else {
                    // ActivityCompat 位于 support.v7 包中，因为运行时权限是 6.0 的新特性，
                    // 使用该类可以省略对版本的判断
                    // 当权限申请被拒绝并且shouldShowRequestPermissionRationale() 返回 false 就表示勾选了不再询问。
                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS)) {
                        // 现在我唯一能做的就是跳转到App的设置界面，让用户手动开启权限了。
                        UtilToast.customToastGravity(this, "点击权限，并打开全部权限", 5000, Gravity.CENTER);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    } else
                        UtilToast.customToastGravity(AddBallActivity.this,
                                "禁用该权限,将无法选取取联系人!", 5000, Gravity.CENTER);
                }

        }
    }
}
