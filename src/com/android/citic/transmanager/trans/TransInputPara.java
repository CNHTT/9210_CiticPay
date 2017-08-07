package com.android.citic.transmanager.trans;

import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.presenter.TransUI;

/**
 * Created by zhouqiang on 2017/3/30.
 */

public class TransInputPara {
    private boolean isNeedAmount = false;//是否需要金额
    private boolean isNeedPass = false;//是否需要密码
    private boolean isNeedConfirmCard = false; //是否需要确认卡号
    private long amount ;//金额
    private long otherAmount ;//第二金额
    private boolean isNeedOnline = false;//是否强制联机
    private int inputMode ;// 外界输入模式
    private String transType ;//交易类型
    private boolean isVoid = false;// 是否是撤销交易
    private boolean isNeedPrint = false;// 是否需要打印
    private boolean isECTrans = false; //是否是电子现金交易
    private TransLogData voidTransData ;// 当前撤销数据
    private TransUI transUI ;// UIP层接口实例

    public boolean isNeedAmount() {
        return isNeedAmount;
    }

    public void setNeedAmount(boolean needAmount) {
        isNeedAmount = needAmount;
    }

    public boolean isNeedPass() {
        return isNeedPass;
    }

    public void setNeedPass(boolean needPass) {
        isNeedPass = needPass;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(long otherAmount) {
        this.otherAmount = otherAmount;
    }

    public boolean isNeedOnline() {
        return isNeedOnline;
    }

    public void setNeedOnline(boolean needOnline) {
        isNeedOnline = needOnline;
    }

    public int getInputMode() {
        return inputMode;
    }

    public void setInputMode(int inputMode) {
        this.inputMode = inputMode;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public boolean isVoid() {
        return isVoid;
    }

    public void setVoid(boolean aVoid) {
        isVoid = aVoid;
    }

    public TransLogData getVoidTransData() {
        return voidTransData;
    }

    public void setVoidTransData(TransLogData voidTransData) {
        this.voidTransData = voidTransData;
    }

    public TransUI getTransUI() {
        return transUI;
    }

    public void setTransUI(TransUI transUI) {
        this.transUI = transUI;
    }

    public boolean isNeedConfirmCard() {
        return isNeedConfirmCard;
    }

    public void setNeedConfirmCard(boolean needConfirmCard) {
        isNeedConfirmCard = needConfirmCard;
    }

    public boolean isECTrans() {
        return isECTrans;
    }

    public void setECTrans(boolean ECTrans) {
        isECTrans = ECTrans;
    }

    public boolean isNeedPrint() {
        return isNeedPrint;
    }

    public void setNeedPrint(boolean needPrint) {
        isNeedPrint = needPrint;
    }
}
