package com.android.citic.appmanager;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.util.DisplayMetrics;

import com.android.citic.R;
import com.android.citic.appmanager.log.Crashandler;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.baidu.BaiduLocation;
import com.android.citic.lib.baidu.LocationInfo;
import com.android.citic.lib.utils.AssetsUtil;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.trans.manager.DparaTrans;
import com.baidu.mapapi.SDKInitializer;
import com.pos.device.SDKException;
import com.pos.device.SDKManager;
import com.pos.device.SDKManagerCallback;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.SlotType;
import com.pos.device.magcard.MagCardReader;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;
import com.pos.device.picc.PiccReader;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class Citicapp extends Application {

	private static Citicapp mInstance;

	private float mScale;

	private ConcurrentHashMap<Object, Object> mCache;

	private ConcurrentHashMap<Object, Object> mQuickCache;//用于对Activity跳转时难以传递的对象进行临时缓存

	private Stack<Activity> allActivityStack;

	private List<Activity> mList = new LinkedList<Activity>();

	private static final int SCREEN_OFF_TIMEOUT_DEFAULT = 60*1000*10;

	private BaiduLocation baiduLocation;

	private LocationInfo locationInfo;

	/**定位前时间*/
	private String beforeLocateTime;

	private static final String TEST_AID = "aid.dat" ;
	private static final String TEST_CAPK = "capk.dat" ;
	private static final String TEST_TRANS = "translog.dat" ;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
		switchLanguage(Locale.US);//edit by liyo
		prepare();
	}

	public static Citicapp getInstance() {
		return mInstance;
	}

	private void switchLanguage(Locale locale) {   //edit by liyo
		Configuration config = getResources().getConfiguration();
		Resources resources = getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		config.locale = locale;
		resources.updateConfiguration(config, dm);
	}

	private void prepare(){
		allActivityStack = new Stack<>();
		mCache = new ConcurrentHashMap<>();
		mQuickCache = new ConcurrentHashMap<>();
		Crashandler crashHandler = Crashandler.getInstance();
		crashHandler.init(getApplicationContext());
		GlobalCfg.setROOT_FILE_PATH( getFilesDir() + "/");
//		initBaiduLocation();
		doFirstStart();
//		AssetsUtil.copyAssetsToData(this , TEST_AID);
//		AssetsUtil.copyAssetsToData(this , TEST_CAPK);
//		AssetsUtil.copyAssetsToData(this , TEST_TRANS);
		SDKManager.init(this, new SDKManagerCallback() {
			@Override
			public void onFinish() {
				Logger.debug("init sdk manager success");
				DparaTrans.loadAIDCAPK2EMVKernel();

				byte[] masterKeyData = com.pos.device.StringUtil.hex2byte("00000000000000000000000000000000");
				int masterKeyIdx = 0;
				int ret = Ped.getInstance().injectKey(KeySystem.MS_DES, KeyType.KEY_TYPE_MASTK, masterKeyIdx, masterKeyData);//the app must be System User can inject success.
				Logger.debug("inject master key ret=" + ret);

				//Inject the encrypted PIN key
				byte[] encryptedKeyData = com.pos.device.StringUtil.hex2byte("89B07B35A1B3F47E51D09B0A025DA707");
				int storeKeyIdx = 0;
				ret = Ped.getInstance().writeKey(KeySystem.MS_DES, KeyType.KEY_TYPE_PINK, masterKeyIdx, storeKeyIdx, Ped.KEY_VERIFY_NONE, encryptedKeyData);
				Logger.debug("inject pin key ret=" + ret);
			}
		});
	}

	private void doFirstStart(){
		try {
			int screenTimeOut = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
			Logger.debug("SCREEN_OFF_TIMEOUT:" + screenTimeOut);
			if(screenTimeOut < SCREEN_OFF_TIMEOUT_DEFAULT){
				Logger.debug("set SCREEN_OFF_TIMEOUT:" + SCREEN_OFF_TIMEOUT_DEFAULT);
				Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, SCREEN_OFF_TIMEOUT_DEFAULT);
			}
		} catch (Settings.SettingNotFoundException e) {
			Logger.debug("SettingNotFoundException" + e);
			e.printStackTrace();
		}
	}

	/** 初始化百度地图定位*/
	private void initBaiduLocation() {
		SDKInitializer.initialize(getApplicationContext());
		baiduLocation = BaiduLocation.getInstance();
		baiduLocation.init(getApplicationContext());
	}

	public LocationInfo getLocationInfo() {
		return locationInfo;
	}

	public void setLocationInfo(LocationInfo locationInfo) {
		this.locationInfo = locationInfo;
	}

	public BaiduLocation getBaiduLocation() {
		return baiduLocation;
	}

	public String getBeforeLocateTime() {
		return beforeLocateTime;
	}

	public void setBeforeLocateTime(String beforeLocateTime) {
		this.beforeLocateTime = beforeLocateTime;
	}

	public int dip2px(float dipValue) {
		return (int) (dipValue * mScale + 0.5f);
	}

	public int px2dip(float pxValue) {
		return (int) (pxValue / mScale + 0.5f);
	}

	/**
	 * 快速缓存一个对象
	 * @param key
	 * @param value
	 */
	public void quickCache(Object key, Object value) {
		mQuickCache.put(key, value);
	}

	/**
	 * 移除缓存对象
	 * @param key
	 * @return
	 */
	public Object popCache(Object key) {
		return mQuickCache.remove(key);
	}

	/**
	 * 弹出缓存对象
	 * @param key
	 * @return
	 */
	public Object popCache_new(Object key) {
		return mQuickCache.get(key);
	}

	public ConcurrentHashMap<Object, Object> getCacher() {
		return mCache;
	}

	 public void pushWholeActivity(Activity activity) {
	 	allActivityStack.push(activity);
	 }

	 public void finishWholeActivity() {
		 for (Activity activity : allActivityStack) {
		 if (activity != null)
		 	activity.finish();
		 }
	 }

	public void addActivity(Activity activity) {
		mList.add(activity);
	}

	public void exit() {
		SDKManager.release();
		//结束栈中
		try {
			for (Activity activity : mList) {
				if (activity != null)
					activity.finish();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
			System.gc();
		}
	}
}
