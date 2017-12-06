package com.shdcec.alarmball.utility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;
import android.widget.ScrollView;
/*
 * 使历史数据ListView在scrolView下可以获得滚动权限
 * 自定义InnerListView继承ListView
 * 当触摸InnerListView时，ScrollView让出权限
 */
public class InnerListView extends ListView {

	
	public InnerListView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
    public InnerListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	public InnerListView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	ScrollView parentScrollView;

    public ScrollView getParentScrollView() {
        return parentScrollView;
    }

    public void setParentScrollView(ScrollView parentScrollView) {
        this.parentScrollView = parentScrollView;
    }

    private int maxHeight;

    public int getMaxHeight() {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // TODO Auto-generated method stub

        if (maxHeight > -1) {

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);

        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev)
    {
    	//当指触到listview的时候，让父ScrollView交出ontouch权限，也就是让父scrollview 停住不能滚动
    	setParentScrollAble(false);
        return super.onInterceptTouchEvent(ev);
    }

    /*
     * 剥夺父ScrollView权限
     */
    private void setParentScrollAble(boolean flag) {

        parentScrollView.requestDisallowInterceptTouchEvent(!flag);
    }

}
