package com.android.citic.transmanager.device.barcode;

/**
 * Created by liyo on 2017/5/2.
 */

import android.os.Bundle;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.pos.device.scanner.*;

import java.lang.reflect.Array;
import java.text.Format;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class BarcodeManager {
    public static final String SCANNER_PLAY_BEEP = "play_beep";
    public static final String SCANNER_CONTINUE_SCAN = "continue_scan";
    public static final int SCANNER_SUCCESS = 0;
    public static final int SCANNER_EXIT = -1;
    public static final int SCANNER_PARAM_INVALID = -2;
    public static final int SCANNER_TIMEOUT = -3;

    private static boolean mode ;

    private static BarcodeManager instance ;
    private BarcodeListener listener ;
    private Scanner scanner;
    private BarcodeManager(){};
    private CountDownLatch mLatch ;

    public static BarcodeManager getInstance(boolean m){
        mode = m;
        if(null == instance){
            instance = new BarcodeManager();
        }
        return instance ;
    }



    public void getBarcode(final int timeout, BarcodeListener l) throws InterruptedException {
        final BarcodeInfo barcodeInfo = new BarcodeInfo();
        if(l == null){
            barcodeInfo.setResult(SCANNER_EXIT);
            listener.callback(barcodeInfo);
        }
        else {
            this.listener = l;
            mLatch = new CountDownLatch(1);
            //new Thread(){
                //public void run(){
                    scanner = Scanner.getInstance();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean(SCANNER_PLAY_BEEP,true);
                    bundle.putBoolean(SCANNER_CONTINUE_SCAN,false);
                    scanner.initScanner(bundle);
                    scanner.startScan(timeout, new OnScanListener() {
                        @Override
                        public void onScanResult(int i, byte[] bytes) {
                            Logger.debug("++++++scaner have returned data++++++");
                            Logger.debug("scaner dat:"+new String(bytes));
                            Logger.debug("scaner rst:"+Integer.toString(i));
                            barcodeInfo.setResult(i);
                            if(i == 0) {
                                barcodeInfo.setCode(bytes);
                            }
                            Logger.debug("scanner reduce");
                            //scanner.stopScan(); //if you call stopscan, the Scanlistener will callback twice
                            mLatch.countDown();
                        }
                    });

                    mLatch.await();
                    listener.callback(barcodeInfo);
                //}
            //}.start();

        }
        return;
    }
}
