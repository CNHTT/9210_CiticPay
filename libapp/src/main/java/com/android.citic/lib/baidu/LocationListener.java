package com.android.citic.lib.baidu;

import android.content.Context;
import android.util.Log;

import com.android.citic.lib.baidu.utils.GsonTools;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * 百度地图定位回调监听
 */
public class LocationListener implements BDLocationListener {
	private LocationListener4Caller caller = new LocationListener4Caller();
	
	public LocationListener(Context context){
		caller.setContext(context);
	}

	public void setCaller(LocationListener4Caller.ILocationListener4Caller Icaller) {
		caller.setCaller(Icaller);
	}

	public void reset(){
		caller.reset();
	}
	
	@Override
	public void onReceiveLocation(BDLocation location) {
		LocationInfo locationInfo = new LocationInfo.Builder()
				.setAddress(location.getAddrStr())
				.setProvince(location.getProvince())
				.setCity(location.getCity())
				.setCityCode(location.getCityCode())
				.setErrCode(location.getLocType())
				.setLatitude(location.getLatitude())
				.setLongitude(location.getLongitude())
				.setRadius(location.getRadius()).setTime(location.getTime())
				.build();
		
		Log.v("baidu", GsonTools.createJsonStrByGson(locationInfo));
		caller.receive(locationInfo);
	}
}
