package com.android.citic.transmanager.process;

import android.os.SystemClock;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.device.input.InputInfo;
import com.android.citic.transmanager.device.input.InputManager;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.android.citic.transmanager.trans.presenter.TransUI;
import com.pos.device.SDKException;
import com.pos.device.emv.CoreParam;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVCallback;
import com.pos.device.emv.IEMVHandler;
import com.pos.device.emv.TerminalMckConfigure;
import com.pos.device.picc.EmvContactlessCard;
import com.pos.device.qpboc.QPbocHandler;
import com.pos.device.qpboc.QPbocParameters;


/**
 * Created by zhouqiang on 2016/11/21.
 */

public class QpbocTransaction {
    private EmvContactlessCard emvContactlessCard = null ;
    private IEMVHandler emvHandler = null;
    private QPbocHandler qPbocHandler = null ;

    private TransUI transUI ;
    private TransInputPara para ;

    private long q_amount ;
    private long q_otheramount ;

    private String EC_AMOUNT ;

    private int timeout ;

    public QpbocTransaction(TransInputPara p){
        para = p ;
        transUI = p.getTransUI() ;
        Logger.debug("type = "+para.getTransType());
        Logger.debug("amount = "+para.isNeedAmount());
        Logger.debug("online = "+para.isNeedOnline());
        Logger.debug("void = "+para.isVoid());
        Logger.debug("pass = "+para.isNeedPass());
        Logger.debug("eccash = "+para.isECTrans());
        Logger.debug("print = "+para.isNeedPrint());
        if(para.isNeedAmount()){
            q_amount = para.getAmount();
            q_otheramount = para.getOtherAmount();
        }
        emvHandler = EMVHandler.getInstance();
        qPbocHandler = QPbocHandler.getInstance();
        try {
            emvContactlessCard = EmvContactlessCard.connect() ;
        } catch (SDKException e) {
            e.printStackTrace();
        }
    }

    public int start(){
        timeout = 60 * 1000 ;
        initEmvKernel();
        if(para.isECTrans()){
            emvHandler.pbocECenable(true);
            Logger.debug("set pboc EC enable");
        }
        emvHandler = EMVHandler.getInstance();
        emvHandler.setEMVInitCallback(emvInitListener);
        emvHandler.setApduExchangeCallback(apduExchangeListener);
        emvHandler.setDataElement(new byte[] { (byte) 0x9c }, new byte[] { 0x00 });
        byte[] transactionProperty = {(byte) 0x26 , 0 , 0 , (byte) 0x80} ;//走QPBOC流程
        boolean statusCheckSupported = true ;
        QPbocParameters parameters = new QPbocParameters() ;
        parameters.setStatusCheckSupported(statusCheckSupported?(byte)1:0);
        parameters.setTransactionProperty(transactionProperty);
        int ret = qPbocHandler.setParameter(parameters);
        Logger.debug("qpboc.setParameter ret = " +ret);
//        int tarnsCounter = Integer.valueOf(GlobalManager.getInstance().getTraceNo()) ;
        int transType  = 0 ;
        int amount  = Integer.valueOf(String.valueOf(para.getAmount())) ;
        Logger.debug("qPbocHandler.preTramsaction amount="+amount);
        ret = qPbocHandler.preTramsaction(Integer.valueOf(ISOUtil.padleft(2 + "", 6, '0')) , (byte)transType , amount) ;
        Logger.debug("qpboc.preTransaction ret = " +ret);
        if(para.isECTrans()){
            byte[] apdu = ISOUtil.hex2byte("80CA9F7900");
            byte[] recv = exeAPDU(apdu);
            EC_AMOUNT = fromApdu2Amount(ISOUtil.hexString(recv));
            Logger.debug("取电子现金余额（9F79）="+EC_AMOUNT);
        }
        ret = qPbocHandler.readData(0);
        Logger.debug("qpboc.readData ret = "+ret);
        if(ret!=0){
            return Tcode.T_qpboc_errno ;
        }
        return ret ;
    }

    private IEMVCallback.ApduExchangeListener apduExchangeListener = new IEMVCallback.ApduExchangeListener() {
        @Override
        public int apduExchange(byte[] sendData, int[] recvLen, byte[] recvData) {
            Logger.debug("==apduExchangeListener==");
            Logger.debug("sendData = " + ISOUtil.byte2hex(sendData));
            int[] status = new int[1];
            long start = SystemClock.uptimeMillis();
            while(true){
                if(SystemClock.uptimeMillis()-start>30*1000){
                    break;
                }
                try {
                    if(emvContactlessCard == null){
                        break;
                    }else {
                        status[0] = emvContactlessCard.getStatus() ;
                    }
                } catch (SDKException e) {
                    e.printStackTrace();
                }
                if(status[0]== EmvContactlessCard.STATUS_EXCHANGE_APDU){
                    break;
                }
                try {
                    Thread.sleep(6);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            int len = 0 ;
            try {
                if(emvContactlessCard == null){
                    return -1 ;
                }else {
                    byte[] rawData = emvContactlessCard.transmit(sendData);
                    if(rawData!=null){
                        Logger.debug("rawData = " + ISOUtil.hexString(rawData));
                        len = rawData.length ;
                    }
                    if (len < 2 || rawData[len-2] != (byte)0x90) {
                        return -1;
                    }
                    System.arraycopy(rawData, 0, recvData, 0, rawData.length);
                }
            } catch (SDKException e) {
                e.printStackTrace();
            }
            if (len >= 0) {
                recvLen[0] = len;
                Logger.debug("Data received from card:" + ISOUtil.byte2hex(recvData,0,recvLen[0]));
                return 0;
            }
            return -1;
        }
    };

    public String getCardNO(){
        byte[] temp = new byte[256] ;
        int len = TLVUtil.get_tlv_data_kernal(0x57 , temp);
        if(len < 7)	{//磁道信息没读到
            return null;
        }else {
            return ISOUtil.hexString(temp).split("D")[0] ;
        }
    }

    public String getEC_AMOUNT(){
        return EC_AMOUNT ;
    }

    private IEMVCallback.EMVInitListener emvInitListener = new IEMVCallback.EMVInitListener() {
        @Override
        public int candidateAppsSelection() {
            Logger.debug("==candidateAppsSelection==");
            return 0 ;
        }

        @Override
        public void multiLanguageSelection() {
            Logger.debug("==multiLanguageSelection==");
            byte[] tag = new byte[] { 0x5F, 0x2D };
            int ret = emvHandler.checkDataElement(tag);
            // 从内核读aid 0x9f06 设置是否支持联机PIN
            Logger.debug("==multiLanguageSelection==ret:"+ ret);
        }

        @Override
        public int getAmount(int[] transAmount, int[] cashBackAmount) {
            Logger.debug("==getAmount==");
            Logger.debug("====getAmount======"+q_amount);
            if (para.isNeedAmount()) {
                if(q_amount <= 0){//没输入金额
                    // 调用输入金额
                    Logger.debug("EMV>>需要输入金额且金额参数未0>>EMV流程执行获取金额的回调");
                    InputInfo info = transUI.getOutsideInput(timeout , InputManager.INPUT_TYPE_NUMBERDECIMEL);
                    if(info.isResultFlag()){
                        q_amount = (int) (Double.parseDouble(info.getResult()) * 100);
                        Logger.debug("EMV>>用户输入的金额="+q_amount);
                    }else {
                        //TODO
                    }
                    q_otheramount = 0;
                }else {
                    transAmount[0] = Integer.valueOf(q_amount + "");
                    cashBackAmount[0] = Integer.valueOf(q_otheramount + "");
                }
            }
            return 0;
        }

        @Override
        public int getPin(int[] pinLen, byte[] cardPin) {
            Logger.debug("==getPin==");
            return 0;
        }

        @Override
        public int pinVerifyResult(int tryCount) {
            Logger.debug("==pinVerifyResult===");
            return 0;
        }

        @Override
        public int checkOnlinePIN() {
            Logger.debug("==checkOnlinePIN==");
            return 0;
        }

        /** 核对身份证证件 **/
        @Override
        public int checkCertificate() {
            Logger.debug("==checkCertificate==");
            return 0;
        }

        @Override
        public int onlineTransactionProcess(byte[] brspCode, byte[] bauthCode,
                                            int[] authCodeLen, byte[] bauthData,
                                            int[] authDataLen, byte[] script,
                                            int[] scriptLen, byte[] bonlineResult) {
            Logger.debug("==onlineTransactionProcess=");
            return 0;
        }

        @Override
        public int issuerReferralProcess() {
            Logger.debug("=issuerReferralProcess==");
            return 0;
        }

        @Override
        public int adviceProcess(int firstFlg) {
            Logger.debug("==adviceProcess==");
            return 0;
        }

        @Override
        public int checkRevocationCertificate(int caPublicKeyID, byte[] RID, byte[] destBuf) {
            Logger.debug("==checkRevocationCertificate===");
            return -1;
        }

        /**
         * 黑名单
         */
        @Override
        public int checkExceptionFile(int panLen, byte[] pan, int panSN) {
            Logger.debug("==checkExceptionFile==");
            return -1;
        }

        /**
         * 判断IC卡脱机的累计金额 超过就强制联机
         */
        @Override
        public int getTransactionLogAmount(int panLen, byte[] pan, int panSN) {
            Logger.debug("==getTransactionLogAmount===");
            return 0;
        }
    };

    private byte[] exeAPDU(byte[] apdu){
        byte[] rawData = null ;
        int recvlen = 0 ;
        int[] status = new int[1];
        long start = SystemClock.uptimeMillis();
        while(true){
            if(SystemClock.uptimeMillis()-start>30*1000){
                break;
            }
            try {
                status[0] = emvContactlessCard.getStatus() ;
            } catch (SDKException e) {
                e.printStackTrace();
            }
            if(status[0]== EmvContactlessCard.STATUS_EXCHANGE_APDU){
                break;
            }
            try {
                Thread.sleep(6);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            rawData = emvContactlessCard.transmit(apdu) ;
            if(rawData!=null){
                Logger.debug("rawData = "+ ISOUtil.hexString(rawData));
                recvlen = rawData.length ;
            }
        } catch (SDKException e) {
            e.printStackTrace();
        }
        byte[] recv = new byte[32] ;
        if(rawData!=null){
            System.arraycopy(rawData,0,recv,0,recvlen);
        }else {
            recv = null ;
        }
        return recv ;
    }

    private String fromApdu2Amount(String hex){
        int len = hex.length() ;
        if(len > 2 && hex.contains("9F79") && hex.contains("9000")){
            int offset = 4 ;
            int l = Integer.parseInt(hex.substring(offset , offset+2));
            return hex.substring(offset+2 , offset+2+l*2);
        }
        return null ;
    }

    /** 初始化Kernel **/
    public boolean initEmvKernel() {
        emvHandler.initDataElement();
        emvHandler.setKernelType(EMVHandler.KERNEL_TYPE_PBOC);

        // 配置MCK,支持项默认为支持，不支持的请设置为-1
        TerminalMckConfigure configure = new TerminalMckConfigure();
        configure.setTerminalType(0x22);
        configure.setTerminalCapabilities(new byte[] { (byte) 0xE0,
                (byte) 0xF8, (byte) 0xC8 });
        configure.setAdditionalTerminalCapabilities(new byte[] { 0x60, 0x00,
                (byte) 0xF0, (byte) 0xA0, 0x01 });

        configure.setSupportCardInitiatedVoiceReferrals(false);
        configure.setSupportForcedAcceptanceCapability(false);
        if(para.isNeedOnline()){
            configure.setSupportForcedOnlineCapability(true);
        }else {
            configure.setSupportForcedOnlineCapability(false);
        }
        configure.setPosEntryMode(0x05);

        int ret = emvHandler.setMckConfigure(configure);
        if (ret != 0) {
            Logger.debug("setMckConfigure failed");
            return false;
        }
        CoreParam coreParam = new CoreParam();
        coreParam.setTerminalId("POS00001".getBytes());
        coreParam.setMerchantId("000000000000000".getBytes());
        coreParam.setMerchantCateCode(new byte[] { 0x00, 0x01 });
        coreParam.setMerchantNameLocLen(35);
        coreParam.setMerchantNameLoc("Band Card Test Center,Beijing,China".getBytes());
        coreParam.setTerminalCountryCode(new byte[] { 0x01, 0x56 });
        coreParam.setTransactionCurrencyCode(new byte[] { 0x01, 0x56 });
        coreParam.setReferCurrencyCode(new byte[] { 0x01, 0x56 });
        coreParam.setTransactionCurrencyExponent(0x02);
        coreParam.setReferCurrencyExponent(0x02);
        coreParam.setReferCurrencyCoefficient(1000);
        coreParam.setTransactionType(EMVHandler.EMVTransType.EMV_GOODS); //161014@Skyh 新接口

//		emvHandler.pbocSMenable(true);
//		Logger.debug("set pboc SM enable");

        ret = emvHandler.setCoreInitParameter(coreParam);
        if (ret != 0) {
            Logger.debug("setCoreInitParameter error");
            return false;
        }
        return true;
    }
}
