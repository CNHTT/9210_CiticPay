package com.android.citic.transmanager.trans.finace.settle;

import android.content.Context;
import android.content.IntentFilter;

import com.android.citic.appmanager.Citicapp;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.DateUtil;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.device.printer.PrintManager;
import com.android.citic.transmanager.device.printer.PrintRes;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.finace.ec_query.EC_EnquiryTrans;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.manager.LogoutTrans;
import com.android.citic.transmanager.trans.manager.RevesalTrans;
import com.android.citic.transmanager.trans.manager.ScriptTrans;
import com.android.citic.transmanager.trans.presenter.TransPresenter;
import com.pos.device.printer.Printer;

import java.util.List;
import java.util.Random;

/**
 * Created by zhouqiang on 2017/3/31.
 */

public class SettleTrans extends Trans implements TransPresenter{

    private int sumCount = 0 ;

    public SettleTrans(Context ctx, String transEname , TransInputPara p) {
        super(ctx, transEname);
        iso8583.setHasMac(false);
        para = p ;
        transUI = para.getTransUI() ;
        TransEName = transEname ;
    }

    @Override
    public void start() {
        timeout = 60 * 1000 ;
        transUI.handling(timeout , Tcode.Status.settling_start);
        //settle();
        settle_test();//eidt by liyo
    }

    private void settle_test(){
        Logger.debug("SettleTrans>>settle test");
        setTraceNoInc(true);
        if(MsgID!=null)
            iso8583.setField(0 , MsgID);
        iso8583.setField(11 , cfg.getTraceNo());
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        iso8583.setField(49 , cfg.getCurrencyCode());
        Logger.debug("SettleTrans>>settle>>Field60 = "+Field60);
        iso8583.setField(60 , Field60);
        iso8583.setField(63 , formatOPN(String.valueOf(cfg.getOprNo())));
        retVal = getSettleSUM48();
        if(retVal==0){
            settlePrint();
            TransLog.getInstance().clearAll();
            //批次号强行加1
            cfg.setBatchNo( Integer.parseInt(cfg.getBatchNo()) + 1 );
            cfg.incTraceNo();
            cfg.save();
            transUI.handling(timeout , Tcode.Status.terminal_logonout);
            LogoutTrans logoutTrans = new LogoutTrans(context , Type.LOGOUT , null);
            retVal = logoutTrans.Logout();
            if(retVal == 0){
                transUI.trannSuccess(Tcode.Status.logonout_succ);
            }else {
                transUI.showError(retVal);
            }

        }else {
            transUI.showError(retVal);
        }
    }

    private void settle(){
        Logger.debug("SettleTrans>>settle");
        setTraceNoInc(true);
        if(MsgID!=null)
            iso8583.setField(0 , MsgID);
        iso8583.setField(11 , cfg.getTraceNo());
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        iso8583.setField(49 , cfg.getCurrencyCode());
        Logger.debug("SettleTrans>>settle>>Field60 = "+Field60);
        iso8583.setField(60 , Field60);
        iso8583.setField(63 , formatOPN(String.valueOf(cfg.getOprNo())));
        retVal = getSettleSUM48();
        if(retVal==0){
            iso8583.setField(48 , Field48);
            retVal = OnLineTrans() ;
            if(retVal!=0){
                transUI.showError(retVal);
            }else {
                String rsp = iso8583.getfield(39);
                Logger.debug("SettleTrans>>settle>>rsp="+rsp);
                if(rsp.equals("00")){
                    String f48 = iso8583.getfield(48);
                    Logger.debug("SettleTrans>>settle>>f48="+f48);
                    int flag = 1 ;
                    shell2ReversalUpsend(flag);
                }else {
                    retVal = Integer.parseInt(rsp) ;
                }
            }
        }else {
            transUI.showError(retVal);
        }
    }

    private void shell2ReversalUpsend(int flag){
        Logger.debug("SettleTrans>>shell2ReversalUpsend");
        TransLogData data = TransLog.getScriptResult();
        retVal = 0 ;
        if (data != null) {
            transUI.handling(timeout , Tcode.Status.settle_send_shell);
            Logger.debug("SettleTrans>>shellUpsend>>结算进行脚本上送");
            setTraceNoInc(true);
            ScriptTrans script = new ScriptTrans(context, "SENDSCRIPT");
            retVal = script.sendScriptResult(data);
            if(retVal == 0)
                TransLog.clearScriptResult();
        }

        if(retVal == 0){
            TransLogData revesalData = TransLog.getReversal();
            if (revesalData != null) {
                setTraceNoInc(true);
                Logger.debug("FinanceTrans>>OnlineTrans>>结算进行冲正上送");
                transUI.handling(timeout , Tcode.Status.settle_send_reversal);
                RevesalTrans revesal = new RevesalTrans(context, "REVERSAL");
                for (int i = 0; i < cfg.getReversalCount() ; i++) {
                    retVal = revesal.sendRevesal();
                    if(retVal == 0){
                        break;
                    }else {
                        if(retVal != Tcode.T_socket_err && retVal != Tcode.T_send_err){
                            continue;
                        }
                    }
                }

                if(retVal == Tcode.T_socket_err || retVal == Tcode.T_send_err){
                    //网络错误不能清除冲正，直接返回
                    transUI.showError(retVal);
                }else {
                    if(retVal != 0){
                        //冲正失败，清除冲正，结束交易
                        TransLog.clearReveral();
                        transUI.showError(Tcode.T_reversal_fail);
                    }else {
                        //上送明细开始
                        onlineUpsend(flag) ;
                    }
                }
            }else {
                //上送明细开始
                onlineUpsend(flag) ;
            }
        }else {
            transUI.showError(retVal);
        }
    }

    private void onlineUpsend(int flag){
        transUI.handling(timeout , Tcode.Status.settling_send_trans);
        Logger.debug("SettleTrans>>onlineUpsend");
        TransEName = "UPSEND" ;
        setFixedDatas();
        setTraceNoInc(false);
        List<TransLogData> list = TransLog.getInstance().getData() ;
        if(list!=null && list.size() > 0){
            if(flag == 1){//对账平
                for (int i = 0; i < list.size() ; i++) {
                    TransLogData data = list.get(i);
                    String type = data.getEName();
                    boolean need = type.equals(Type.SALE) || type.equals(Type.VOID) || type.equals(Type.QUICKPASS);
                    if (need) {
                        if (data.isICC() || data.isNFC()) {
                            //逐条上送
                            setFileds(data);
                            retVal = OnLineTrans();
                            if (retVal == 0) {
                                sumCount++;
                                String rsp = iso8583.getfield(39);
                                Logger.debug("SettleTrans>>onlineUpsend>>rsp = " + rsp);
                                if (!rsp.equals("00")) {
                                    retVal = Tcode.T_settle_tc_send_err;
                                    break;
                                }
                            } else {
                                Logger.debug("上送交易明细失败");
                                break;
                            }
                        }
                    }
                }
            }else {//对账不平
//                iso8583.clearData();
//                int mag_amount = 0 ;
//                int mag_count = 0 ;
//
//                mag_count ++ ;
//                mag_amount += data.getAmount() ;
//                if(mag_count / 8 == 0){
//                    //上送
//                    String a = String.valueOf(mag_amount);
//                    a = ISOUtil.padleft(a + "", 12, '0') ;
//                    if(MsgID!=null)
//                        iso8583.setField(0 , MsgID);
//                    iso8583.setField(4, a);
//                    iso8583.setField(22 , "0210");
//                    iso8583.setField(41 , cfg.getTermID());
//                    iso8583.setField(42 , cfg.getMerchID());
//                    appendField60("60");
//                    iso8583.setField(60 , Field60);
//                    Field62 = "610000"+a+cfg.getCurrencyCode();
//                    Field62 = BCD2ASC(Field62.getBytes());
//                    Logger.debug("SettleTrans>>setFileds>>Field62="+Field62);
//                    iso8583.setField(62 , Field62);
//                }
            }


            if(retVal == 0){
                settleOver();
            }else {
                transUI.showError(retVal);
            }
        }else {
            transUI.showError(Tcode.T_batch_no_trans);
        }
    }

    private void settleOver(){
        transUI.handling(timeout , Tcode.Status.settling_over);
        Logger.debug("SettleTrans>>settleOver");
        TransEName = "UPSEND" ;
        setFixedDatas();
        setTraceNoInc(true);
        iso8583.clearData();
        if(MsgID!=null)
            iso8583.setField(0 , MsgID);
        iso8583.setField(11 , cfg.getTraceNo());
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        Logger.debug("SettleTrans>>settleOver>>sumCount"+sumCount);
        iso8583.setField(48 , ISOUtil.padleft(String.valueOf(sumCount) , 4 , '0'));
        iso8583.setField(60 , "00"+cfg.getBatchNo()+"207");
        retVal = OnLineTrans();
        if(retVal!=0){
            transUI.showError(retVal);
        }else {
            String rsp = iso8583.getfield(39);
            Logger.debug("SettleTrans>>settleOver>>rsp = "+rsp);
            if(rsp.equals("00")){
                retVal = settlePrint();//打印结算单明细单
                if(retVal == 0){
                    //结算结束，清除本批次
                    TransLog.getInstance().clearAll();
                    //批次号强行加1
                    cfg.setBatchNo( Integer.parseInt(cfg.getBatchNo()) + 1 );
                    cfg.incTraceNo();
                    cfg.save();
                    transUI.handling(timeout , Tcode.Status.terminal_logonout);
                    LogoutTrans logoutTrans = new LogoutTrans(context , Type.LOGOUT , null);
                    retVal = logoutTrans.Logout();
                    if(retVal == 0){
                        transUI.trannSuccess(Tcode.Status.logonout_succ);
                    }else {
                        transUI.showError(retVal);
                    }
                }else {
                    //结算结束，清除本批次
                    TransLog.getInstance().clearAll();
                    transUI.showError(retVal);
                }
            }else {
                transUI.showError(Integer.parseInt(rsp));
            }
        }
    }

    private int settlePrint(){
        saveSettleLog();
        Logger.debug("SettleTrans>>settlePrint>>开始打单");
        transUI.handling(timeout , Tcode.Status.printing_recept);
        PrintManager print = PrintManager.getmInstance(context);
        do{
            retVal = print.startPrintSettle(transLog.getLastTransLog());
        }while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
        if (retVal == Printer.PRINTER_OK) {
            cfg.clearFlag();
            retVal = 0 ;
        } else {
            cfg.clearFlag();
        }
        return retVal ;
    }

    private void saveSettleLog(){
        TransLogData LogData = new TransLogData();
        LogData.setOprNo(cfg.getOprNo());
        LogData.setEName(TransEName);
        LogData.setTraceNo(cfg.getTraceNo());
        LogData.setBatchNo(cfg.getBatchNo());
        LogData.setLocalDate(DateUtil.getYear() + getLocalDate());
        LogData.setLocalTime(getLocalTime());
        LogData.setAAC(FinanceTrans.AAC_ARQC);
        transLog.saveLog(LogData);
        cfg.increFlag();// flag++已存
        Logger.debug("save log logSize="+ TransLog.getInstance().getSize());
    }

    private void setFileds(TransLogData data){
        iso8583.clearData();
        if(MsgID!=null)
            iso8583.setField(0 , MsgID);
        iso8583.setField(2 , data.getCardFullNo());
        String a = String.valueOf(data.getAmount());
        a = ISOUtil.padleft(a + "", 12, '0');
        Logger.debug("SettleTrans>>setFileds>>Amount="+a);
        iso8583.setField(4, a);
        iso8583.setField(11 , data.getTraceNo());
        Logger.debug("SettleTrans>>setFileds>>EntryMode"+data.getEntryMode());
        if(data.getEntryMode() == null){
            iso8583.setField(22 , "0510");
        }else {
            iso8583.setField(22 , data.getEntryMode());
        }
        iso8583.setField(41 , cfg.getTermID());
        iso8583.setField(42 , cfg.getMerchID());
        if(data.getICCData() != null){
            iso8583.setField(55 , ISOUtil.byte2hex(data.getICCData()));
        }
        appendField60("60");
        iso8583.setField(60 , Field60);
        Field62 = "610000"+a+cfg.getCurrencyCode();
        Field62 = BCD2ASC(Field62.getBytes());
        Logger.debug("SettleTrans>>setFileds>>Field62="+Field62);
        iso8583.setField(62 , Field62);
    }

    private int getSettleSUM48(){
        List<TransLogData> list = TransLog.getInstance().getData();
        long debitAmount = 0 ;
        int debitCounts = 0 ;
        long creditAmount = 0 ;
        int creditCounts = 0 ;

        String f48_1;
        String f48_2;
        String f48_3;
        String f48_4;
        String f_waibi = "0000000000000000000000000000000";
        if(list!=null && list.size() > 0){
            Logger.debug("本批次共记录了"+list.size()+"条交易日志");
            for (int i = 0  ; i < list.size() ; i++){
                String trans = list.get(i).getEName() ;
                Logger.debug("list["+i+"] type = "+trans);
                if(trans.equals(Trans.Type.SALE)){
                    debitAmount += list.get(i).getAmount();
                    debitCounts ++;
                }
                if(trans.equals(Type.QUICKPASS)){
                    debitAmount += list.get(i).getAmount();
                    debitCounts ++;
                }
                if(trans.equals(Type.VOID)){
                    creditAmount += list.get(i).getAmount();
                    creditCounts ++ ;
                }
            }
            f48_1 = ISOUtil.padleft(String.valueOf(debitAmount) , 12 , '0');
            f48_2 = ISOUtil.padleft(String.valueOf(debitCounts) , 3 , '0');
            f48_3 = ISOUtil.padleft(String.valueOf(creditAmount) , 12 , '0');
            f48_4 = ISOUtil.padleft(String.valueOf(creditCounts) , 3 , '0');
            Field48 = f48_1+f48_2+f48_3+f48_4+"0" + f_waibi;
            Logger.debug("结算48域="+Field48);
            return 0 ;
        }else {
            return Tcode.T_batch_no_trans ;
        }
    }

    private String formatOPN(String opn){
        String two = ISOUtil.padleft(opn , 2 , '0');
        return two + " " ;
    }

    public final static char[] BToA = "0123456789abcdef".toCharArray() ;
    public static String BCD2ASC(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            int h = ((bytes[i] & 0xf0) >>> 4);
            int l = (bytes[i] & 0x0f);
            temp.append(BToA[h]).append( BToA[l]);
        }
        return temp.toString() ;
    }
}
