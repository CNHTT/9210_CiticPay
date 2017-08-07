package com.android.citic.transmanager.global;

/**
 * Created by zhouqiang on 2017/3/10.
 */

public class PrinterLang {
    /**
     * 打印中文
     */
    public static final int L_CH = 1 ;

    /**
     * 打印英文
     */
    public static final int L_EN = 2 ;

    /**
     * 打印混合
     */
    public static final int L_MIX = 3 ;

    public PrinterLang(){}

    private int lang ;

    public void setLang(int l){
        this.lang = l ;
    }

    public int getLang(){
        return lang ;
    }
}
