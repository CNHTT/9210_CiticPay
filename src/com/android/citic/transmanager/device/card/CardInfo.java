package com.android.citic.transmanager.device.card;

/**
 * Created by zhouqiang on 2017/3/14.
 */

public class CardInfo {

    private boolean resultFalg ;

    /** 成功返回 */
    private int cardType ;
    private byte[] cardAtr ;
    private String[] trackNo ;
    private int nfcType ;

    /**
     * 失败返回
     */
    private int errno ;

    public CardInfo(){}

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public byte[] getCardAtr() {
        return cardAtr;
    }

    public void setCardAtr(byte[] cardAtr) {
        this.cardAtr = cardAtr;
    }

    public String[] getTrackNo() {
        return trackNo;
    }

    public void setTrackNo(String[] trackNo) {
        this.trackNo = trackNo;
    }

    public int getNfcType() {
        return nfcType;
    }

    public void setNfcType(int nfcType) {
        this.nfcType = nfcType;
    }

    public boolean isResultFalg() {
        return resultFalg;
    }

    public void setResultFalg(boolean resultFalg) {
        this.resultFalg = resultFalg;
    }

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }
}
