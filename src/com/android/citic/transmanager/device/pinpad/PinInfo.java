package com.android.citic.transmanager.device.pinpad;

/**
 * Created by zhouqiang on 2017/3/17.
 */

public class PinInfo {
    private boolean resultFlag ;

    private int errno ;

    private byte[] pinblock ;

    private boolean isNoPin ;

    public boolean isNoPin() {
        return isNoPin;
    }

    public void setNoPin(boolean noPin) {
        isNoPin = noPin;
    }

    public boolean isResultFlag() {
        return resultFlag;
    }

    public void setResultFlag(boolean resultFlag) {
        this.resultFlag = resultFlag;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public byte[] getPinblock() {
        return pinblock;
    }

    public void setPinblock(byte[] pinblock) {
        this.pinblock = pinblock;
    }
}
