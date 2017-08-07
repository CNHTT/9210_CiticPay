package com.android.citic.lib.baidu;


import android.content.Context;
import android.util.Log;

import com.android.citic.lib.baidu.utils.ToastUtil;


public class LocationListener4Caller {

	private Context context;
	private ILocationListener4Caller caller;
	private boolean isProcessing = false; //是否正在处理定位结果
	private final int locSum = 3;//每次交易所允许的定位次数，超过该次数后仍定位失败，则提示“定位失败”
	private int locCount = 0;
	
	
	public LocationListener4Caller() {
		super();
	}

	public LocationListener4Caller(Context context, ILocationListener4Caller caller) {
		this.context = context;
		this.caller = caller;
	}

	public void receive(LocationInfo locationInfo){
		switch(locationInfo.getErrCode()){//这些错误码是百度地图API中的错误码
		case 161://网络定位成功
		case 66://离线定位
		case 61: //GPS定位成功
			Log.v("baidu" , "latitude = "+locationInfo.getLatitude());
			Log.v("baidu" , "longitude = "+locationInfo.getLongitude());
			if(caller != null){
				isProcessing = true;
				caller.finish(locationInfo);
				caller = null;//caller必须置空，因为百度定位有时会同一时刻发起多次定位，置空后就可保证只接收一次定位，并只作一次定位处理
				BaiduLocation.getInstance().stopLocation();
			}
			break;
		
		case 62:
			notifyLocateFail("定位失败，请检查运营商网络或者wifi网络是否正常开启");			
			break;
			
		case 67:
			notifyLocateFail("离线定位失败");
			break;
			
		case 63:
		case 68:
			notifyLocateFail("网络异常，请确认当前手机网络是否通畅");
			break;
			
		case 167:
			notifyLocateFail("服务端定位失败，请您检查是否禁用获取位置信息权限");
			break;
			
		default:
			notifyLocateFail(locationInfo.getErrCode()+"：定位失败！");
			break;
		}
	}
	
	private void notifyLocateFail(String content){
		
		if(locCount < locSum){
			locCount++;
		} else {

			if(!isProcessing){
				ToastUtil.showShortToast(context, content);
			}
			
			BaiduLocation.getInstance().stopLocation();
		}		
	}
	
	public void setContext(Context context) {
		this.context = context;
	}

	public void setCaller(ILocationListener4Caller caller) {
		this.caller = caller;
	}

	public void reset(){
		caller = null;
		isProcessing = false;
		locCount = 0;
	}
	
	/**
	 * 定位回调监听
	 */
	public interface ILocationListener4Caller {
		public void finish(LocationInfo locationInfo);
	}
}
