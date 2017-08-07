package com.android.citic.appmanager.master;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.citic.R;
import com.baidu.mapapi.map.Text;


/**
 * Activity的导航条
 */
public class TopNavigation extends LinearLayout {

	private LinearLayout mNavigationView;
	private LinearLayout mNavLeft;
	private LinearLayout mNavRight;
	private ImageView mLeftIcon;
	private ImageView mRightIcon ;
	private TextView nav_left_tx ;
	private TextView mTitle;
	private TextView mRightContent;
	private RelativeLayout top_layout;
	private TextView mTimeView ;

	public TopNavigation(Context context) {
		super(context);
		init(context);
	}

	public TopNavigation(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mNavigationView = (LinearLayout) inflate(context, R.layout.top_navigation, this);
		top_layout = (RelativeLayout) findViewById(R.id.top_layout);
		mNavLeft = (LinearLayout) findViewById(R.id.nav_left);
		mNavRight = (LinearLayout) findViewById(R.id.nav_right);
		mLeftIcon = (ImageView) mNavigationView.findViewById(R.id.nav_left_img);
		mTitle = (TextView) mNavigationView.findViewById(R.id.nav_title_tv);
		mRightContent = (TextView) findViewById(R.id.nav_right_text);
		mTimeView = (TextView) findViewById(R.id.nav_right_text);
	}

	/**
	 * 设置导航条左边图标
	 * @param resId
	 */
	public void setLeftIcon(int resId) {
		mLeftIcon.setBackgroundResource(resId);
	}

	public void setLeftIcon(Drawable drawable) {
		mLeftIcon.setBackgroundDrawable(drawable);
	}

	/**
	 * 设置导航条右边图标
	 * @param resId
	 */
	public void setRightIcon(int resId) {
		mRightIcon.setImageResource(resId);
	}

	public void setRightIcon(Drawable drawable) {
		mRightIcon.setImageDrawable(drawable);
	}

	/**
	 * 设置导航条文字内容
	 * @param resId
	 */
	public void setTitle(int resId) {
		mTitle.setText(resId);
	}

	public void setTitle(String title) {
		mTitle.setTextColor(Color.parseColor("#60000000"));
		mTitle.setTextSize(20);
		mTitle.setText(title);
	}

	/**
	 * 设置导航条时间
	 * @param str
     */
	public void setTime(String str){
		mTimeView.setText(str);
	}

	public void setTimeColor(int id){
		mTimeView.setTextColor(id);
	}

	public void setTimeVisible(int flag){
		mTimeView.setVisibility(flag);
	}

	/**
	 * 设置导航条文字内容
	 * @param resId
	 */
	public void setRightContent(int resId) {
		mRightContent.setText(resId);
	}

	public void setRightContent(String title) {
		mRightContent.setText(title);
	}

	public void setOnLeftIconClickListener(OnClickListener clickListener) {
		mLeftIcon.setOnClickListener(clickListener);
	}

	public void setOnRightIconClickListener(OnClickListener clickListener) {
		mNavRight.setOnClickListener(clickListener);
	}

	public RelativeLayout getTop_layout() {
		return top_layout;
	}

	public void setTop_layout(RelativeLayout top_layout) {
		this.top_layout = top_layout;
	}

	public TextView getNav_left_tx() {
		return nav_left_tx;
	}

	public void setNav_left_tx(TextView nav_left_tx) {
		this.nav_left_tx = nav_left_tx;
	}

	public void setOnLeftTxClickListener(OnClickListener clickListener) {
		nav_left_tx.setOnClickListener(clickListener);
	}

	public ImageView getLeftIcon() {
		return mLeftIcon;
	}

	public ImageView getRightIcon() {
		return mRightIcon;
	}

	public LinearLayout getLeftLayout() {
		return mNavLeft;
	}

	public TextView getRightTxt() {
		return mRightContent;
	}

	public LinearLayout getRightLayout() {
		return mNavRight;
	}

	public void setLeftIconVisible(int visibility) {
		mLeftIcon.setVisibility(visibility);
		if(visibility == View.VISIBLE){
			mLeftIcon.setEnabled(true);
			mLeftIcon.setClickable(true);
		}else {
			mLeftIcon.setClickable(false);
			mLeftIcon.setEnabled(false);
		}
	}

	public void setRightIconVisible(int visibility) {
		mRightIcon.setVisibility(visibility);
	}

	public void setLeftPadding(int leftPadding) {
		mRightIcon.setPadding(leftPadding, mLeftIcon.getPaddingTop(),
				mLeftIcon.getPaddingRight(), mLeftIcon.getPaddingBottom());
	}

	public void setRightPadding(int rightPadding) {
		mRightIcon.setPadding(mRightIcon.getPaddingLeft(),
				mRightIcon.getPaddingTop(), rightPadding,
				mRightIcon.getPaddingBottom());
	}
}
