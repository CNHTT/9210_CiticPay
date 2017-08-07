package com.android.citic.appmanager.log;

import android.util.Log;

import com.android.citic.transmanager.global.GlobalCfg;

/**
 * Created by zhouqiang on 2017/3/8.
 */

public class Logger {
    private final static char[] mChars = "0123456789ABCDEF".toCharArray();
    public static final String TAG = "liyo" ;

    public static void debug(String msg){
        if(GlobalCfg.getDebug()){
            Log.v(TAG , msg);
        }
    }

    public static String str2HexStr(String str){
        StringBuilder sb = new StringBuilder();
        byte[] bs = str.getBytes();

        for (int i = 0; i < bs.length; i++){
            sb.append(mChars[(bs[i] & 0xFF) >> 4]);
            sb.append(mChars[bs[i] & 0x0F]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }
}
