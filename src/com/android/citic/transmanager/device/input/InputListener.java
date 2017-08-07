package com.android.citic.transmanager.device.input;

/**
 * Created by zhouqiang on 2017/3/16.
 */

public interface InputListener {
    /**
     * 获取用户主动输入成功
     * @param input
     */
    void success(String input);

    /**
     * 获取用户主动输入失败
     */
    void fail(int errno);
}
