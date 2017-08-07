package com.android.citic.transmanager.trans.finace.revocation;

import android.content.Context;
import android.widget.Toast;

import com.android.citic.R;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.card.CardInfo;
import com.android.citic.transmanager.device.card.CardManager;
import com.android.citic.transmanager.device.input.InputInfo;
import com.android.citic.transmanager.device.pinpad.PinInfo;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.process.EmvTransaction;
import com.android.citic.transmanager.process.QpbocTransaction;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.android.citic.transmanager.trans.presenter.TransPresenter;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVHandler;

/**
 * Created by zhouqiang on 2016/12/6.
 */

public class VoidTrans extends FinanceTrans implements TransPresenter{

    private TransLogData data ;
    public VoidTrans(Context ctx, String transEname , TransInputPara p) {
        super(ctx, transEname);
        para = p ;
        transUI = para.getTransUI() ;
        isReversal = false ;
        isSaveLog = true;
        isDebit = true;
        isProcPreTrans = true;

    }

    @Override
    public void start() {
        timeout = 60 * 1000 ;
        //取密码
        Logger.debug("VoidTrans>>getOutsideInput>>主管密码....");
        InputInfo info = transUI.getOutsideInput(timeout , MASTER_PASSWORD_INPUT);
        if(info.isResultFlag()){
            String master_pass = info.getResult();
            Logger.debug("manage pwd="+cfg.getPassword());
            if(master_pass.equals(cfg.getPassword())){
                Logger.debug("VoidTrans>>getOutsideInput>>交易流水号....");
                info = transUI.getOutsideInput(timeout , TRANS_TRACENO_INPUT );
                if(info.isResultFlag()){
                    TransLog log = TransLog.getInstance() ;
                    data = log.searchTransLogByTraceNo(info.getResult());
                    if(data==null){
                        transUI.showError(Tcode.T_not_find_trans);
                    }else {
                        if(data.getIsVoided()){
                            transUI.showError(Tcode.T_trans_is_voided);
                        }else {
                            boolean isSale = data.getEName().equals(Trans.Type.SALE)?true:false;
                            if(isSale){
                                //展示交易信息
                                para.setVoidTransData(data);
                                retVal = transUI.showTransInfo(timeout , data);
                                if(0 == retVal){
                                    Amount = data.getAmount();
                                    RRN = data.getRRN();
                                    AuthCode = data.getAuthCode();
                                    Field61 = data.getBatchNo()+data.getTraceNo();
                                    //edit by liyo
                                    int inputmod = 0;
                                    if(cfg.getContactlessSwitch())
                                        inputmod = INMODE_IC|INMODE_NFC|INMODE_MAG;
                                    else
                                        inputmod = INMODE_IC|INMODE_MAG;
                                    //取卡
                                    CardInfo cardInfo = transUI.getCardUse(timeout , inputmod);
                                    afterGetCardUse(cardInfo , data);
                                }else{
                                    transUI.showError(Tcode.T_user_cancel_operation);
                                }
                            }else {
                                transUI.showError(Tcode.T_original_trans_can_not_void);
                            }
                        }
                    }
                }else {
                    transUI.showError(info.getErrno());
                }
            }else {
                transUI.showError(Tcode.T_master_pass_err);
            }
        }else {
            transUI.showError(info.getErrno());
        }


        Logger.debug("VoidTrans>>finish");
        return;
    }

    private void afterGetCardUse(CardInfo info , TransLogData data){
        if(info.isResultFalg()){
            int type = info.getCardType() ;
            switch (type){
                case CardManager.TYPE_MAG :inputMode = ENTRY_MODE_MAG ;break;
                case CardManager.TYPE_ICC :inputMode = ENTRY_MODE_ICC ;break;
                case CardManager.TYPE_NFC :inputMode = ENTRY_MODE_NFC ;break;
            }
            Logger.debug("VoidTrans>>inputMode = "+inputMode);
            para.setInputMode(inputMode);
            if(inputMode == ENTRY_MODE_MAG){
                isMag(info.getTrackNo());
            }
            else if(inputMode == ENTRY_MODE_ICC){
                isICC();
            }
            else if(inputMode == ENTRY_MODE_NFC){
                isNFC();
            }
        }else {
            transUI.showError(info.getErrno());
        }
    }

    private void isICC(){
        if(GlobalCfg.isEmvParamLoaded()){
            transUI.handling(timeout , Tcode.Status.handling);
            emv = new EmvTransaction(para);
            retVal = emv.start() ;
            Logger.debug("EMVTramsProcess return = "+retVal);
            if(1 == retVal || retVal == 0){
                if (StringUtil.isNullWithTrim(emv.getPinBlock())) {
                    isPinExist = false;
                } else{
                    isPinExist = true;
                }if (isPinExist){
                    PIN = emv.getPinBlock();
                }
                setICCData();
                prepareOnline(emv.getCardNo());
            }else {
                transUI.showError(retVal);
            }
        }else {
            transUI.showError(Tcode.T_terminal_no_aid);
        }
    }

    private void isNFC(){
        if(GlobalCfg.isEmvParamLoaded()){
            transUI.handling(timeout , Tcode.Status.handling);
            qpboc = new QpbocTransaction(para);
            retVal = qpboc.start() ;
            Logger.debug("EMVTramsProcess return = "+retVal);
            if(0 == retVal){
                String cn = qpboc.getCardNO();
                if(cn == null){
                    transUI.showError(Tcode.T_qpboc_read_err);
                }else {
                    if(!cn.equals(data.getCardFullNo())){
                        transUI.showError(Tcode.T_void_card_not_same);
                    }else {
                        Pan = cn ;
                        retVal = transUI.showCardConfirm(timeout , cn );
                        if(0 == retVal){
                            PinInfo info = transUI.getPinpadOnlinePin(timeout ,String.valueOf(Amount), cn);
                            afterQpbocGetPin(info);
                        }else {
                            transUI.showError(retVal);
                        }
                    }
                }
            }else {
                transUI.showError(retVal);
            }
        }else {
            transUI.showError(Tcode.T_terminal_no_aid);
        }
    }

    private void afterQpbocGetPin(PinInfo info){
        if(info.isResultFlag()){
            if(info.isNoPin()){
                isPinExist = false;
            }else {
                isPinExist = true ;
                PIN = ISOUtil.hexString(info.getPinblock()) ;
            }
            IEMVHandler emvHandler = EMVHandler.getInstance();
            byte[] temp =  ISOUtil.str2bcd(Pan , false);
            if(Pan.length()%2 != 0)
                temp[Pan.length() / 2] |= 0x0f;
            emvHandler.setDataElement(new byte[]{0x5A} ,temp );
            Logger.debug("temp = "+ISOUtil.hexString(temp , 0 , temp.length));
            byte[] res = new byte[32] ;
            TLVUtil.get_tlv_data_kernal(0x9F10 , res);
            setICCData();
            prepareOnline(qpboc.getCardNO());//edit by liyo
        }else {
            transUI.showError(info.getErrno());
        }
    }

    /** 磁卡选项 */
    private void isMag(String[] tracks){
        Logger.debug("SaleTrans>>T1="+tracks[0]);
        Logger.debug("SaleTrans>>T2="+tracks[1]);
        Logger.debug("SaleTrans>>T3="+tracks[2]);
        String data1 = null;
        String data2 = null;
        String data3 = null;
        int msgLen = 0;
        if (tracks[0].length() > 0 && tracks[0].length() <= 80) {
            data1 = new String(tracks[0]);
        }
        if (tracks[1].length() >= 13 && tracks[1].length() <= 37) {
            data2 = new String(tracks[1]);
            if(!data2.contains("=")){
                retVal = Tcode.T_search_card_err ;
            }else {
                String judge = data2.substring(0, data2.indexOf('='));
                if(judge.length() < 13 || judge.length() > 19){
                    retVal = Tcode.T_search_card_err ;
                }else {
                    if (data2.indexOf('=') != -1)
                        msgLen++;
                }
            }
        }
        if (tracks[2].length() >= 15 && tracks[2].length() <= 107) {
            data3 = new String(tracks[2]);
        }
        if(retVal!=0){
            transUI.showError(retVal);
        }else {
            if (msgLen == 0) {
                transUI.showError(Tcode.T_search_card_err);
            }else {
                if (cfg.isCheckICC()) {
                    int splitIndex = data2.indexOf("=");
                    if (data2.length() - splitIndex >= 5) {
                        char iccChar = data2.charAt(splitIndex + 5);
                        if (iccChar == '2' || iccChar == '6') {
                            //该卡IC卡,请刷卡
                            transUI.showError(Tcode.T_ic_not_allow_swipe);
                        }else {
                            afterMAGJudge(data2 , data3);
                        }
                    } else {
                        transUI.showError(Tcode.T_search_card_err);
                    }
                }else {
                    afterMAGJudge(data2 , data3);
                }
            }
        }
    }

    private void afterMAGJudge(String data2 , String data3){
        String cardNo = data2.substring(0, data2.indexOf('='));
        if(!cardNo.equals(para.getVoidTransData().getCardFullNo())){
            transUI.showError(Tcode.T_void_card_not_same);
        }else {
            retVal = transUI.showCardConfirm(timeout , cardNo);
            if(retVal == 0){
                Pan = cardNo;
                Track2 = data2;
                Track3 = data3;
                PinInfo info = transUI.getPinpadOnlinePin(timeout ,String.valueOf(Amount), cardNo);
                if(info.isResultFlag()){
                    if(info.isNoPin()){
                        isPinExist = false;
                    }else {
                        if(null == info.getPinblock()){
                            isPinExist = false;
                        }else {
                            isPinExist = true;
                        }
                        PIN = ISOUtil.hexString(info.getPinblock());
                    }
                    prepareOnline(cardNo); //edit by liyo
                }else {
                    transUI.showError(info.getErrno());
                }
            }else {
                transUI.showError(Tcode.T_user_cancel_operation);
            }
        }
    }


    private void prepareOnline(String cardno){
        //设置完55域数据即可请求联机
        transUI.handling(timeout , Tcode.Status.connecting_center);
        setDatas(inputMode);
        //联机处理
        if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC){
            //retVal = OnlineTrans(emv , cardno); //edit by liyo
            retVal = FakeOnlineTrans(emv , cardno);
        }else{
            //retVal = OnlineTrans(null , cardno); //edit by liyo
            retVal = FakeOnlineTrans(emv , cardno);
        }
        Logger.debug("VoidTrans>>OnlineTrans="+retVal);
        clearPan();
        if(retVal == 0){
            data.setVoided(true);
            int index = TransLog.getInstance().getCurrentIndex(data);
            TransLog.getInstance().updateTransLog(index,data);
            transUI.trannSuccess(Tcode.Status.void_succ);
        }else {
            transUI.showError(retVal);
        }
    }

//    final class TransThread extends Thread{
//
//        private TransLogData data = null ;
//        private Handler handler = null ;
//        private int emvRet ;
//        public TransThread(Handler h , TransLogData d ){
//            this.handler = h ;
//            this.data = d ;
//        }
//
//        public void run(){
//            Message message = handler.obtainMessage() ;
//            message.what = Trans.REVER_OVER ;
//            Amount = data.getAmount() ;
//            String result = TransUtil.getCard(handler , R.string.sale_use_card_tips ,
//                    INMODE_MAG|INMODE_IC|INMODE_NFC , true);
//            if (StringUtil.isNullWithTrim(result)) {
//                clearPan();
//                message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_read_card_timeout)};
//                handler.sendMessage(message);
//                return;
//            }if(result.contains(",Key Err")){
//                clearPan();
//                message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_read_terminal_aidcapk_fail)};
//                handler.sendMessage(message);
//                return;
//            }
//            String[] cardInfo = result.split(",");
//            inputMode = Integer.parseInt(cardInfo[0]);
//            if(inputMode == ENTRY_MODE_MAG){
//                cardNo = cardInfo[1];// 磁卡卡号
//                Pan = cardNo;
//                Track2 = cardInfo[3];
//                Track3 = cardInfo[4];
//                if (StringUtil.isNullWithTrim(Track3))
//                    Track3 = null;
//            }else if(inputMode == ENTRY_MODE_ICC){
//                LogManager.t("icc emv trans starting");
//                if (cardInfo.length == 2 && cardInfo[1].equals("Key Err")) {
//                    message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_nokey_err)};
//                    handler.sendMessage(message);
//                    return;
//                }if (cardInfo.length == 1) {
//                    // 如果上电失败 通过iccRet执行降级操作
//                    emvRet = 2;
//                } else {
//                    countLatch = new CountDownLatch(1);
//                    emv = new EmvTransaction(context, true, Amount, inputMode, 0, handler  , TransEName);
//                    emvRet = emv.EMVTramsProcess();
//                }if (emvRet == 0 || emvRet == 1) {
//                    // 联机处理,已在setdatas中处理55域数据
//                    cardNo = emv.getCardNo() ;
//                } else if (emvRet == 2) {
//                    handler.sendEmptyMessage(Trans.TRANS_IC_DOWNGRADE);
//                    RunMag(handler);
//                }else if(emvRet == Trans.EMV_OFFLINE_DATAAUTH){
//                    clearPan();
//                    message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_offlineDataAuth_err)};
//                    handler.sendMessage(message);
//                    return;
//                }else if(emvRet == Trans.EMV_CARDHOLDER_AUTH_FAIL){
//                    clearPan();
//                    message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_cardHolderVerify_err)};
//                    handler.sendMessage(message);
//                    return;
//                }else if(emvRet == Trans.EMV_TERMINAL_ANAYSIS_FAIL){
//                    clearPan();
//                    message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalRiskMana_err)};
//                    handler.sendMessage(message);
//                    return;
//                }else if(emvRet == Trans.EMV_CONFIGMCARD_CANCEL_OR_TIMEOUT){
//                    clearPan();
//                    handler.sendEmptyMessage(ERR_QUIT);
//                    return;
//                }else {
//                    clearPan();
//                    message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+emvRet+"]"};
//                    handler.sendMessage(message);
//                    return;
//                }
//            }else if(inputMode == ENTRY_MODE_NFC) {
//                LogManager.t("nfc emv trans starting");
//                if(ConfigManager.getInstance().isQPBOC()){
//                    qpboc = new QpbocTransaction(context) ;
//                    int qpboc_ret = qpboc.qpbocProcess() ;
//                    if( qpboc_ret == 0){
//                        byte[] temp = new byte[256] ;
//                        int len = TLVUtil.get_tlv_data_kernal(0x57 , temp);
//                        LogManager.t("temp = "+ ISOUtil.hexString(temp)) ;
//                        if(len < 7)	//磁道信息没读到
//                            return;
//                        String CN = ISOUtil.hexString(temp) ;
//                        LogManager.t("CN = "+CN.split("D")[0]);
//                        Pan = CN.split("D")[0];
//                        LogManager.t("Pan = "+Pan);
//                        IEMVHandler emvHandler = EMVHandler.getInstance();
//                        temp =  ISOUtil.str2bcd(Pan , false);
//                        if(Pan.length()%2 != 0)
//                            temp[Pan.length() / 2] |= 0x0f;
//                        emvHandler.setDataElement(new byte[]{0x5A} ,temp );
//                        cardNo = Pan ;
//                    }else {
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+qpboc_ret+"]"} ;
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }
//                }else {
//                    emv = new EmvTransaction(context, true, Amount, inputMode, 0, handler , TransEName );
//                    emvRet = emv.EMVTramsProcess();
//                    if (emvRet == 0 || emvRet == 1) {
//                        // 联机处理,已在setdatas中处理55域数据
//                        cardNo = emv.getCardNo() ;
//                    }else if(emvRet == IEMVHandler.EMV_ERRNO_CARD_BLOCKED || emvRet == IEMVHandler.EMV_ERRNO_APP_BLOCKED){
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_selectApp_err)};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }else if(emvRet == IEMVHandler.EMV_ERRNO_NODATA){
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_readAppData_err)};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }else if(emvRet == Trans.EMV_OFFLINE_DATAAUTH){
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_offlineDataAuth_err)};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }else if(emvRet == Trans.EMV_CARDHOLDER_AUTH_FAIL){
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_cardHolderVerify_err)};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }else if(emvRet == Trans.EMV_TERMINAL_ANAYSIS_FAIL){
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalRiskMana_err)};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }else if(emvRet == Trans.EMV_TERMINAL_ACTION_ANAYSIS_FAIL){
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalAction_err)};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }else if(emvRet == Trans.EMV_CONFIGMCARD_CANCEL_OR_TIMEOUT){
//                        clearPan();
//                        handler.sendEmptyMessage(ERR_QUIT);
//                        return;
//                    }else {
//                        clearPan();
//                        message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+emvRet+"]"};
//                        noticeRemoveNFCcard(handler , message);
//                        return;
//                    }
//                }
//            }
//            LogManager.t("cardNo = "+cardNo);
//            Pan = cardNo ;
//            LogManager.t("cardfullno = "+data.getCardFullNo());
//            if(ENTRY_MODE_MAG == inputMode || (ENTRY_MODE_NFC == inputMode && ConfigManager.getInstance().isQPBOC())){
//                result = TransUtil.getNoticeResult(handler,cardNo);
//                if (result == null) {
//                    clearPan();
//                    handler.sendEmptyMessage(ERR_QUIT);
//                    return;
//                }if(!cardNo.equals(data.getCardFullNo())){
//                    clearPan();
//                    message.obj = new String[]{FAIL , context.getString(R.string.rever_rever_fail)+"\n"+
//                            context.getResources().getString(R.string.rever_card_not_same)} ;
//                    handler.sendMessage(message);
//                    return;
//                }
//
//                result = TransUtil.getPass(handler, R.string.sale_enter_pass, cardNo);
//                if(result.contains("errcode")){
//                    clearPan();
//                    message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_get_pinblock_err)+":\n"+result} ;
//                    noticeRemoveNFCcard(handler , message);
//                    return;
//                }if (result.equals(""+ PedRetCode.ENTER_CANCEL)) {
//                    clearPan();
//                    qpbocPrecessCancelbyUsers(handler);
//                    return;
//                }if (result.equals("")) {
//                    isPinExist = false;
//                } else
//                    isPinExist = true;
//                if (isPinExist)
//                    PIN = result;
//            }
//
//            if(ENTRY_MODE_ICC == inputMode || ENTRY_MODE_NFC == inputMode)
//                setICCData();
//            setDatas(inputMode);
//            handler.sendEmptyMessage(Trans.WaitOnLineTrans);
//            if(configManager.getIsOnlineResult()){
//                int resultCode ;
//                if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC)
//                    resultCode = OnlineTrans(handler, emv , cardNo);
//                else
//                    resultCode = OnlineTrans(handler, null , cardNo);
//                TransLog log = TransLog.getInstance();
//                data.setVoided(true);
//                int current = log.getCurrentIndex(data) ;
//                if(current!=-1)
//                    log.updateTransLog(current, data);
//                int rid = getResurceId(resultCode) ;
//                String info = context.getResources().getString(rid);
//                if(resultCode == 0){
//                    info = context.getResources().getString(rid)+"\n\n"+
//                            context.getResources().getString(R.string.rever_rever_amount)+
//                            "\n\nRMB:"+ TransUtil.getStrAmount(TransLog.getInstance().getLastTransLog().getAmount()) ;
//                }
//                message.obj = new String[]{resultCode==0?SUCC:FAIL , info} ;
//                handler.sendMessage(message);
//                return;
//            }else {
//                TransLog log = TransLog.getInstance();
//                data.setVoided(true);
//                int current = log.getCurrentIndex(data) ;
//                if(current!=-1)
//                    log.updateTransLog(current, data);
//                //存下当笔撤销交易
//                TransLogData logData = setLogData(data ,cardNo);
//                log.saveLog(logData);
//                configManager.incTraceNo();
//                configManager.increFlag();
//                //打印
//                handler.sendEmptyMessage(Trans.REVER_PRINTING);
//                int ret ;
//                PrintTrans print = new PrintTrans(context, handler);
//                do {
//                    ret = print.printTransLog(transLog.getLastTransLog(), false);
//                } while (ret == Printer.PRINTER_STATUS_PAPER_LACK);
//                if (ret == Printer.PRINTER_OK) {
//                    configManager.clearFlag();
//                    message.obj = new String[]{SUCC , context.getResources().getString(R.string.rever_rever_succ)
//                    +"\n\n"+context.getString(R.string.rever_rever_amount)+"\n\n"+"RMB:"+TransUtil.getStrAmount(data.getAmount())};
//                } else if (ret == ONLINE_RET_PRINT_CANCLE) {
//                    configManager.clearFlag();
//                    message.obj = new String[]{FAIL , context.getString(R.string.sale_print_cancel)};
//                } else{
//                    message.obj = new String[]{FAIL , context.getString(R.string.sale_print_err)};
//                }
//                handler.sendMessage(message);
//            }
//        }
//    }

//    private void qpbocPrecessCancelbyUsers(Handler mha){
//        PiccReader nfcCard = PiccReader.getInstance() ;
//        mha.sendEmptyMessage(Trans.NFC_REMOVE);
//        mha.sendEmptyMessage(ERR_QUIT);
//    }

//    private void noticeRemoveNFCcard(Handler h , Message m){
//        PiccReader nfcCard = PiccReader.getInstance() ;
//        h.sendEmptyMessage(Trans.NFC_REMOVE);
//        h.sendMessage(m) ;
//    }

//    private int getResurceId(int resultCode){
//        int rid ;
//        switch (resultCode){
//            case 0:rid = R.string.rever_rever_succ ;break;
//            case ONLINE_INTERRUPT:rid = R.string.sale_interrupt ;break;
//            case ONLINE_ICC_RET_ERR:rid = R.string.sale_over_icc_err ;break;
//            case ONLINE_RET_MAC_ERR:rid = R.string.sale_mac_err ;break;
//            case ONLINE_RET_SOCKET_ERR:rid = R.string.socket_fail ;break;
//            case ONLINE_RET_SEND_ERR:rid = R.string.socket_send_msg_err ;break;
//            case ONLINE_RET_RECIVE_ERR_PACKET:rid = R.string.socket_err_packet ;break;
//            case ONLINE_RET_RECIVE_EMPTY:rid = R.string.socket_receive_empty_err ;break;
//            case ONLINE_RET_RECIVE_REFUSE:rid = R.string.sale_trans_refuse ;break;
//            case ONLINE_RET_PRINTING_ERR:rid = R.string.sale_print_err ;break;
//            case ONLINE_RET_PRINT_CANCLE:rid = R.string.sale_print_cancel ;break;
//            case EMV_ERRNO_DECLINE:rid = R.string.sale_trans_refuse ;break;
//            case EMV_ERRNO_CARD_BLOCKED:rid = R.string.sale_trans_refuse ;break;
//            case EMV_ERRNO_APP_BLOCKED:rid = R.string.sale_trans_refuse ;break;
//            case EMV_ERRNO_PIN_BLOCKED:rid = R.string.sale_trans_refuse ;break;
//            case ONLINE_RET_EMV_REFUSE:rid = R.string.sale_emv_refuse ;break;
//            default:rid = R.string.sale_other_err ;break;
//        }
//        return rid ;
//    }

//    private void RunMag(Handler handle) {
//        String result = TransUtil.getCard(handle, R.string.sale_prompt_swipe_card, INMODE_MAG, false);
//        if (StringUtil.isNullWithTrim(result)) {
//            clearPan();
//            Message message = handle.obtainMessage() ;
//            message.what = TRANS_OVER ;
//            message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_swipe_card_timeout)};
//            handle.sendMessage(message);
//            return ;
//        }
//        String[] cardInfo = result.split(",");
//        inputMode = Integer.parseInt(cardInfo[0]);
//        if (inputMode == ENTRY_MODE_MAG) {
//            cardNo = cardInfo[1];
//            Pan = cardNo;
//        }
//    }
}
