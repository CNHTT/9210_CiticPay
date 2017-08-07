package com.android.citic.transmanager.trans.presenter;

import com.android.citic.transmanager.device.barcode.BarcodeInfo;
import com.android.citic.transmanager.device.card.CardInfo;
import com.android.citic.transmanager.device.input.InputInfo;
import com.android.citic.transmanager.device.pinpad.PinInfo;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;

import java.util.concurrent.CountDownLatch;

/**
 * Created by zhouqiang on 2017/3/15.
 */

public interface TransUI {
    /**
     * 获取外界输入UI接口(提示用户输入信息)
     * @return
     */
    InputInfo getOutsideInput(int timeout , int type);

    /**
     * 获取外界卡片UI接口(提示用户用卡)
     * @return
     */
    CardInfo getCardUse(int timeout , int mode);

    /**
     * 获取密码键盘输入联机PIN
     * @param timeout
     * @param amount
     * @param cardNo
     */
    PinInfo getPinpadOnlinePin(int timeout , String amount , String cardNo);

    /**
     * 获取密码键盘输入脱机PIN
     * @param timeout
     * @param amount
     * @param cardNo
     */
    PinInfo getPinpadOfflinePin(int timeout , String amount , String cardNo);

    /**
     * 人机交互显示UI接口(卡号确认)
     * @param cn 卡号
     */
    int showCardConfirm(int timeout ,String cn);

    /**
     * 人机交互显示UI接口(多应用卡片选择)
     * @param timeout
     * @param list
     * @return
     */
    int showCardApplist(int timeout ,String[] list);

    /**
     * 人机交互显示UI接口(耗时处理操作)
     * @param timeout
     * @param status TransStatus 状态标志以获取详细错误信息
     */
    void handling(int timeout , int status);

    /**
     * 人机交互显示UI接口
     * @param timeout
     * @param logData 详细交易日志
     */
    int showTransInfo(int timeout , TransLogData logData);

    /**
     * 交易成功处理结果
     * @param code
     */
    void trannSuccess(int code);

    /**
     * 人机交互显示UI接口(显示交易出错错误信息)
     * @param errcode 实际代码错误返回码
     */
    void showError(int errcode);

    /**
     * 提示脱机密码结果
     * @param count
     */
    void showOfflinePinResult(int count);

    BarcodeInfo getBarcodeUse(int timeout , boolean mode);
}
