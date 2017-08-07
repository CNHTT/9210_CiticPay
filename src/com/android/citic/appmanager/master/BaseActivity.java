package com.android.citic.appmanager.master;

import android.app.Instrumentation;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;

import com.android.citic.appmanager.Citicapp;
import com.android.citic.appmanager.log.Logger;

/**
 * 交易控制界面父
 */
public class BaseActivity extends FragmentActivity {

	protected TopNavigation mDefaultTopNavigation;
	private LinearLayout mActivityLayout;
	private CDT cdt ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		super.onCreate(savedInstanceState);
		mDefaultTopNavigation = new TopNavigation(this);
//		mDefaultTopNavigation.getLeftLayout().setOnClickListener(
//				new OnClickListener() {
//
//					@Override
//					public void onClick(View arg0) {
//						back();
//					}
//				});

//		mDefaultTopNavigation.setOnLeftIconClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						back();
//					}
//				});

		// MainApplication.getInstance().pushWholeActivity(this);
		Citicapp.getInstance().addActivity(this);
	}

	@Override
	public void setContentView(int layoutResID) {
		View content = LayoutInflater.from(this).inflate(layoutResID, null);
		if (mActivityLayout != null) {
			mActivityLayout.removeAllViews();
		}
		mActivityLayout = new LinearLayout(this);
		mActivityLayout.setOrientation(LinearLayout.VERTICAL);
		initNavigationByConfig();
		LinearLayout.LayoutParams layoutParams;
		mActivityLayout.addView(mDefaultTopNavigation);
		layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.FILL_PARENT);
		mActivityLayout.addView(content, layoutParams);
		super.setContentView(mActivityLayout);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			finish();
			return false ;
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return super.dispatchTouchEvent(ev);
	}

	private void initNavigationByConfig() {
		NavigationConfig navigationConfig = getClass().getAnnotation(NavigationConfig.class);
		if (navigationConfig != null) {
			if (navigationConfig.leftIconId() != -1)
				mDefaultTopNavigation.setLeftIcon(navigationConfig.leftIconId());
			if (navigationConfig.rightIconId() != -1)
				mDefaultTopNavigation.setRightIcon(navigationConfig.rightIconId());
			if (navigationConfig.titleId() != -1)
				mDefaultTopNavigation.setTitle(navigationConfig.titleId());
			else if (navigationConfig.titleValue() != null)
				mDefaultTopNavigation.setTitle(navigationConfig.titleValue());
		}
	}

	protected void setReturnVisible(int visible){
		mDefaultTopNavigation.setLeftIconVisible(visible);
		if(visible == View.VISIBLE){
			mDefaultTopNavigation.setOnLeftIconClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					back();
				}
			});
		}
	}

	protected void setNaviTitle(int rid){
		mDefaultTopNavigation.setTitle(rid);
	}

	protected void setNaviTitle(String str){
		mDefaultTopNavigation.setTitle(str);
	}

	private final class CDT extends CountDownTimer{

		public CDT(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			long left = millisUntilFinished/1000 ;
			if(left <= 30){
				mDefaultTopNavigation.setTimeColor(Color.RED);
			}else{
				mDefaultTopNavigation.setTimeColor(Color.GREEN);
			}
			mDefaultTopNavigation.setTime(String.valueOf(millisUntilFinished/1000));
			mDefaultTopNavigation.setTimeVisible(View.VISIBLE);
		}

		@Override
		public void onFinish() {
			back();
		}
	}

	protected void startTimer(int s){
		mDefaultTopNavigation.setTime("");
		mDefaultTopNavigation.setTimeVisible(View.VISIBLE);
		if(cdt!=null)
			cdt.cancel();
		cdt = new CDT(s , 1000);
		cdt.start();
	}

	protected void stopTimer(){
		if(cdt!=null)
			cdt.cancel();
		mDefaultTopNavigation.setTime("");
		mDefaultTopNavigation.setTimeVisible(View.GONE);
	}

	protected void refreashTimer(int s){
		mDefaultTopNavigation.setTime("");
		mDefaultTopNavigation.setTimeVisible(View.VISIBLE);
		if(cdt!=null)
			cdt.cancel();
		cdt = new CDT(s , 1000);
		cdt.start();
	}

	public TopNavigation getTopNavigation() {
		return mDefaultTopNavigation;
	}

	protected void back() {
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					Logger.debug("Exception when onBack"+e.toString());
				}
			}
		}.start();
	}
}
