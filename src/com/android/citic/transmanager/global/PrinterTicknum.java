package com.android.citic.transmanager.global;

/**
 * Created by zhouqiang on 2017/3/10.
 */

public class PrinterTicknum {
    /**
     * 打印1联
     */
    public static final int T_1 = 1 ;

    /**
     * 打印2联
     */
    public static final int T_2 = 2 ;

    /**
     * 打印3联
     */
    public static final int T_3 = 3 ;

    private int tn ;

    public PrinterTicknum(){}

    public void setTN(int num){
        this.tn = num ;
    }

    public int getTN(){
        return tn ;
    }
}
