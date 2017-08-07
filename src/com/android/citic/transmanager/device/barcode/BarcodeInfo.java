package com.android.citic.transmanager.device.barcode;

/**
 * Created by liyo on 2017/5/2.
 */

public class BarcodeInfo {
    private int type;
    private byte[] code;
    private int result ;

    public void setCode(byte[] code){
        this.code = code;
    }

    public void setType(int type){
        this.type = type;
    }

    public void setResult(int result){
        this.result = result;
    }

    public byte[] getCode(){
        return this.code;
    }

    public int getType(){
        return this.type;
    }

    public int getResult(){
        return this.result;
    }

}
