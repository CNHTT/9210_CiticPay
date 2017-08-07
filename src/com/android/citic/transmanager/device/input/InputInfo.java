package com.android.citic.transmanager.device.input;

/**
 * Created by zhouqiang on 2017/3/17.
 */

public class InputInfo {
    private boolean resultFlag ;

    private int errno ;

    private String result ;

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
