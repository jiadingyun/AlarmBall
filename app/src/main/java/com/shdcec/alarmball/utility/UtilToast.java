package com.shdcec.alarmball.utility;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * 功能：常用工具类
 *
 * @创建： Created by yaping on 2017/12/5 0005.
 */

public class UtilToast {
    private static boolean isShow = true;//默认显示
    private static android.widget.Toast mToast = null;//全局唯一的Toast
//    public UtilToast() {
//        throw new UnsupportedOperationException("不能被实例化");
//    }
    /**
     * 全局控制是否显示Toast
     * @param isShowToast
     */
    public static void controlShow(boolean isShowToast){
        isShow = isShowToast;
    }
    /**
     * 取消Toast显示
     */
    public void cancelToast() {
        if(isShow && mToast != null){
            mToast.cancel();
        }
    }
    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, CharSequence message) {
        if (isShow){
            if (mToast == null) {
                mToast = android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT);
            } else {
                mToast.setText(message);
            }
            mToast.show();
        }
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, CharSequence message) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            } else {
                mToast.setText(message);
            }
            mToast.show();
        }
    }
    /**
     * 长时间显示Toast
     *
     * @param context
     * @param resId 资源ID:getResources().getString(R.string.xxxxxx);
     */
    public static void showLong(Context context, int resId) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, resId, Toast.LENGTH_LONG);
            } else {
                mToast.setText(resId);
            }
            mToast.show();
        }
    }
    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration 单位:毫秒
     */
    public static void showTime(Context context, CharSequence message, int duration) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, duration);
            } else {
                mToast.setText(message);
            }
            mToast.show();
        }
    }
    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param resId 资源ID:getResources().getString(R.string.xxxxxx);
     * @param duration 单位:毫秒
     */
    public static void showTime(Context context, int resId, int duration) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, resId, duration);
            } else {
                mToast.setText(resId);
            }
            mToast.show();
        }
    }
    /**
     * 自定义Toast的位置
     * @param context
     * @param message
     * @param duration 单位:毫秒
     * @param gravity
     */
    public static void customToastGravity(Context context, CharSequence message, int duration,int gravity) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, duration);
            } else {
                mToast.setText(message);
            }
            mToast.setGravity(gravity, 0, 0);
            mToast.show();
        }
    }

    /**
     * 自定义带图片和文字的Toast，最终的效果就是上面是图片，下面是文字
     * @param context
     * @param message
     * @param iconResId 图片的资源id,如:R.drawable.icon
     * @param duration
     * @param gravity
     */
    public static void showToastWithImageAndText(Context context, CharSequence message, int iconResId,int duration,int gravity) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, duration);
            } else {
                mToast.setText(message);
            }
            mToast.setGravity(gravity, 0, 0);
            LinearLayout toastView = (LinearLayout) mToast.getView();
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(iconResId);
            toastView.addView(imageView, 0);
            mToast.show();
        }
    }

    /**
     * 自定义Toast,针对类型CharSequence
     * @param context
     * @param message
     * @param duration
     * @param view
     * @param isGravity true,表示后面的三个布局参数生效,false,表示不生效
     * @param gravity
     * @param xOffset
     * @param yOffset
     * @param isMargin true,表示后面的两个参数生效，false,表示不生效
     * @param horizontalMargin
     * @param verticalMargin
     */
    public static void customToastAll(Context context, CharSequence message, int duration, View view, boolean isGravity, int gravity, int xOffset, int yOffset, boolean isMargin, float horizontalMargin, float verticalMargin) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, message, duration);
            } else {
                mToast.setText(message);
            }
            if(view != null){
                mToast.setView(view);
            }
            if(isMargin){
                mToast.setMargin(horizontalMargin, verticalMargin);
            }
            if(isGravity){
                mToast.setGravity(gravity, xOffset, yOffset);
            }
            mToast.show();
        }
    }

    /**
     * 自定义Toast,针对类型resId
     * @param context
     * @param resId
     * @param duration
     * @param view :应该是一个布局，布局中包含了自己设置好的内容
     * @param isGravity true,表示后面的三个布局参数生效,false,表示不生效
     * @param gravity
     * @param xOffset
     * @param yOffset
     * @param isMargin true,表示后面的两个参数生效，false,表示不生效
     * @param horizontalMargin
     * @param verticalMargin
     */
    public static void customToastAll(Context context, int resId, int duration,View view,boolean isGravity,int gravity, int xOffset, int yOffset,boolean isMargin,float horizontalMargin, float verticalMargin) {
        if (isShow){
            if (mToast == null) {
                mToast = Toast.makeText(context, resId, duration);
            } else {
                mToast.setText(resId);
            }
            if(view != null){
                mToast.setView(view);
            }
            if(isMargin){
                mToast.setMargin(horizontalMargin, verticalMargin);
            }
            if(isGravity){
                mToast.setGravity(gravity, xOffset, yOffset);
            }
            mToast.show();
        }
    }
}
