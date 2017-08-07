package com.android.citic.appmanager.setting.fragments;

import android.app.Activity;
import android.util.DisplayMetrics;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.citic.R;

/**
 * Created by zhouqiang on 2017/3/10.
 */

public class DeviceFrags {

    private Activity mContext ;
    private RelativeLayout rLayout ;

    public DeviceFrags(Activity c , RelativeLayout l , String title){
        this.mContext = c ;
        this.rLayout = l ;
        rLayout.removeAllViews();
        rLayout.inflate(mContext , R.layout.setting_frag_deviceinfo , rLayout);
        ((TextView)rLayout.findViewById(R.id.setting_title_tv)).setText(title);
        readDeviceInfo();
    }

    private void readDeviceInfo(){
        ((TextView)rLayout.findViewById(R.id.setting_deviceinfo_product)).
                setText(android.os.Build.PRODUCT);
        ((TextView)rLayout.findViewById(R.id.setting_deviceinfo_model)).
                setText(android.os.Build.MODEL);
        ((TextView)rLayout.findViewById(R.id.setting_deviceinfo_platform)).
                setText(android.os.Build.VERSION.RELEASE);
        ((TextView)rLayout.findViewById(R.id.setting_deviceinfo_cpuabi)).
                setText(android.os.Build.CPU_ABI);
        ((TextView)rLayout.findViewById(R.id.setting_deviceinfo_sdk)).
                setText(android.os.Build.VERSION.SDK);
        DisplayMetrics dm = new DisplayMetrics() ;
        mContext.getWindowManager().getDefaultDisplay().getMetrics(dm);
        ((TextView)rLayout.findViewById(R.id.setting_deviceinfo_display)).
                setText(dm.widthPixels+"*"+dm.heightPixels);
    }
}
