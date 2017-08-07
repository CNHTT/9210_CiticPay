package com.android.citic.lib.baidu.utils;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;


/**
 * Toast显示工具
 */
public class ToastUtil {
	private static Toast mToast = null;
	
	/**
	 * 显示一个短时间的Toast
	 * @param context
	 * @param text
	 * @since 1.0
	 */
	public static void showShortToast(Context context, String text){
		showToast(context, text, Toast.LENGTH_SHORT);
	}
	
	/**
	 * 显示一个中间的Toast
	 * @param context
	 * @param text
	 * @param duration
	 * @since 1.0
	 */
	public static void showCenterToast(Context context, String text, int duration){
		if(TextUtils.isEmpty(text)){
			return;
		}
		
		if (mToast == null) {  
	    	mToast = Toast.makeText(context, text, duration);  
		} else {
			mToast.setText(text);
			mToast.setDuration(duration);
		}
		mToast.setGravity(Gravity.CENTER, 0, 0);
		mToast.show(); 
	}
	
	/**
	 * 显示Toast
	 * @param context
	 * @param text
	 * @param duration
	 * @since 1.0
	 */
	public static void showToast(Context context, String text, int duration) {
		if(TextUtils.isEmpty(text)){
			return;
		}
		
		if (mToast == null) {  
	    	mToast = Toast.makeText(context, text, duration);  
		} else {
			mToast.setText(text);
			mToast.setDuration(duration);
		}  
		mToast.show(); 
	 }
	
	/**
	 * 显示一个短时间的Toast
	 * @param context
	 * @param resId
	 * @since 1.0
	 */
	public static void showShortToast(Context context, int resId){
		String msg = null;
		try {
			msg = context.getString(resId);
			showShortToast(context, msg);
		} catch (NotFoundException nfe) {
			Log.e("ToastUtil", "找不到相关的string资源");
		} catch (Exception e) {
			Log.e("ToastUtil", "显示Toast出错："+e.getMessage());
		}
	}
	
	/**
	 * 显示一个中间的Toast
	 * @param context
	 * @param resId
	 * @param duration
	 * @since 1.0
	 */
	public static void showCenterToast(Context context, int resId, int duration){
		String msg = null;
		try {
			msg = context.getString(resId);
			showCenterToast(context, msg, duration);
		} catch (NotFoundException nfe) {
			Log.e("ToastUtil", "找不到相关的string资源");
		} catch (Exception e) {
			Log.e("ToastUtil", "显示Toast出错："+e.getMessage());
		}
	}
	
	/**
	 * 显示Toast
	 * @param context
	 * @param resId
	 * @param duration
	 * @since 1.0
	 */
	public static void showToast(Context context, int resId, int duration) {  
		String msg = null;
		try {
			msg = context.getString(resId);
			showToast(context, msg, duration);
		} catch (NotFoundException nfe) {
			Log.e("ToastUtil", "找不到相关的string资源");
		} catch (Exception e) {
			Log.e("ToastUtil", "显示Toast出错："+e.getMessage());
		}
	}
	
}
