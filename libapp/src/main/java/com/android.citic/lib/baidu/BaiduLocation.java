package com.android.citic.lib.baidu;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/***
 * 百度地图定位
 */
public class BaiduLocation {

	public LocationClient mLocationClient;
	public LocationListener mLocationListener;
	public Vibrator mVibrator;
	private LocationClientOption option;

	private static BaiduLocation instance = null;

	public static BaiduLocation getInstance() {
		if (instance == null) {
			synchronized (BaiduLocation.class) {
				if (instance == null) {
					instance = new BaiduLocation();
				}
			}
		}
		return instance;
	}

	private BaiduLocation() {
	}

	public void init(Context context) {
		Log.v("baidu" , "init");
		mLocationClient = new LocationClient(context);
		mLocationListener = new LocationListener(context);
		mLocationClient.registerLocationListener(mLocationListener);

		mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
	}

	/**
	 * 默认参数值初始化定位参数
	 * <p>
	 * 定位模式：高精度，坐标系：gcj02（国测局加密经纬度坐标）
	 * @since 1.0
	 */
	public void initLocation() {
		Log.v("baidu" , "initLocation");
		initLocation(LocationMode.Hight_Accuracy, "gcj02", 0);
	}

	/**
	 * 初始化定位参数
	 * @param mode
	 *            定位模式
	 * @param type
	 *            经纬度坐标加密方式
	 * @param span
	 *            发起定位请求的间隔时间
	 * @since 1.0
	 */
	public void initLocation(LocationMode mode, String type, int span) {
		Log.v("baidu" , "initLocation");
		option = new LocationClientOption();
		option.setLocationMode(mode);
		option.setCoorType(type);
		option.setScanSpan(span);
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setTimeOut(5000);
		mLocationClient.setLocOption(option);
	}

	/**
	 * 开启定位
	 */
	public void startLocation(LocationListener4Caller.ILocationListener4Caller caller , int ref_time) {
		mLocationListener.setCaller(caller);
		if (mLocationClient.isStarted()) {
			return;
		}
//		MainApp.getInstance().setBeforeLocateTime(TimeUtils.getAbsoluteTime());
		//设置发起定位请求的间隔ref_time需要大于等于1000ms才是有效的
		initLocation(LocationMode.Hight_Accuracy, "gcj02", ref_time);
		Log.v("baidu" , "startLocation");
		try {
			mLocationClient.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发起定位请求
	 */
	public void requestLocation() {
		Log.v("baidu" , "requestLocation");
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.requestLocation();
		} else {
			Log.v("baidu-locSD5", "locClient is null or not started");
		}
	}

	/**
	 * 发起离线定位
	 */
	public void requestOfflineLocation(){
		Log.v("baidu" , "requestOfflineLocation");
		if (mLocationClient != null && mLocationClient.isStarted()) {
			mLocationClient.requestOfflineLocation();
		} else {
			Log.v("baidu-locSD5", "locClient is null or not started");
		}
	}
	
	/**
	 * 关闭定位
	 */
	public void stopLocation() {
		if (option != null) {
			mLocationClient.setLocOption(option);
		}
		Log.v("baidu" , "stopLocation");
		mLocationClient.stop();
		mLocationListener.reset();
	}
}
