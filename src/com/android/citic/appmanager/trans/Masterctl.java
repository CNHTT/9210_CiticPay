package com.android.citic.appmanager.trans;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.CiticPay;
import com.android.citic.appmanager.Citicapp;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.appmanager.master.BaseActivity;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.lib.utils.AssetsUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.barcode.BarcodeInfo;
import com.android.citic.transmanager.device.barcode.BarcodeListener;
import com.android.citic.transmanager.device.barcode.BarcodeManager;
import com.android.citic.transmanager.device.card.CardInfo;
import com.android.citic.transmanager.device.card.CardListener;
import com.android.citic.transmanager.device.card.CardManager;
import com.android.citic.transmanager.device.input.InputInfo;
import com.android.citic.transmanager.device.input.InputListener;
import com.android.citic.transmanager.device.input.InputManager;
import com.android.citic.transmanager.device.pinpad.PinInfo;
import com.android.citic.transmanager.device.pinpad.PinpadListener;
import com.android.citic.transmanager.device.pinpad.PinpadManager;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.finace.barcode_sale.BarcodeSaleTrans;
import com.android.citic.transmanager.trans.finace.ec_query.EC_EnquiryTrans;
import com.android.citic.transmanager.trans.finace.query.EnquiryTrans;
import com.android.citic.transmanager.trans.finace.quickpass.Quick_PassTrans;
import com.android.citic.transmanager.trans.finace.revocation.VoidTrans;
import com.android.citic.transmanager.trans.finace.sale.SaleTrans;
import com.android.citic.transmanager.trans.finace.settle.SettleTrans;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.manager.DparaTrans;
import com.android.citic.transmanager.trans.manager.LogonTrans;
import com.android.citic.transmanager.trans.manager.LogoutTrans;
import com.android.citic.transmanager.trans.presenter.TransPresenter;
import com.android.citic.transmanager.trans.presenter.TransUI;
import com.android.keyboard.DesertKeybord;
//import com.android.citic.transmanager.device.input.DesertKeybord;

import java.util.concurrent.CountDownLatch;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhouqiang on 2017/3/9.
 */

/**
 * 金融类交易控制
 */
public class Masterctl extends BaseActivity implements TransUI{

    @Bind(R.id.trans_carduse_layout)
    LinearLayout cardUseLayout ;
    @Bind(R.id.trans_confirmcard_layout)
    LinearLayout confirmCardLayout ;
    @Bind(R.id.trans_handling_layout)
    LinearLayout handlingLayout ;
    @Bind(R.id.trans_inputamount_layout)
    LinearLayout inputAmountLayout ;
    @Bind(R.id.trans_inputpass_layout)
    LinearLayout inputPassLayout ;

    @Bind(R.id.amount_edit)
    EditText amountEdit ;
    @Bind(R.id.amount_keyboard)
    DesertKeybord amountKeyboard ;

    @Bind(R.id.carduse_insert_wv)
    WebView carduseInsertWV ;
    @Bind(R.id.carduse_swipe_wv)
    WebView carduseSwipeWV ;
    @Bind(R.id.carduse_pat_wv)
    WebView cardusePatWv ;
    @Bind(R.id.carduse_swipe_area)
    LinearLayout carduseSwipe ;
    @Bind(R.id.carduse_insert_area)
    LinearLayout carduseInsert ;
    @Bind(R.id.carduse_pat_area)
    LinearLayout cardusePat ;

    @Bind(R.id.confirm_cardno_confirm)
    Button confirmCardConfirm ;
    @Bind(R.id.confirm_cardno_cancel)
    Button confirmCardCancel ;
    @Bind(R.id.confirm_cardno_et)
    EditText confirmCardEdit ;

    @Bind(R.id.handling_msg_info)
    TextView handlingMsg ;

    @Bind(R.id.trans_showinfo_layout)
    LinearLayout showinfoLayout ;
    @Bind(R.id.showinfo_confirm)
    Button showinfoConfirm ;
    @Bind(R.id.showinfo_cancel)
    Button showinfoCancel ;
    @Bind(R.id.showinfo_msg_details)
    TextView showinfoMsg ;

    private Context mContext ;
    private Activity mActivity ;

    private static final String TRANS_STATUS_PROP = "status/status.properties";
    private static final String ERRNO_STATUS_PROP = "status/err.properties" ;
    private String Trans_Types  ;
    private TransPresenter transPresenter ;
    private int inputType ;
    private Message message ;
    private CountDownLatch mLatch ;
    private int retVal ;
    private int TIMEOUT = 60*1000 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.master_ctl);
        setReturnVisible(View.INVISIBLE);
        ButterKnife.bind(this);
        mContext = this ;
        mActivity = Masterctl.this ;
        loadWebGif();
        Bundle b = getIntent().getExtras() ;
        if(null!=b){
            String ch = b.getString(CiticPay.START_TRANS_KEY) ;
            setNaviTitle(ch);
            Trans_Types = ch2en(ch);
            startTrans();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        CardManager.getInstance(0).releaseAll();
        System.gc();
    }

    private void startTrans(){
        TransInputPara para = new TransInputPara();
        para.setTransUI(this);
        if(Trans_Types.equals(Trans.Type.SALE)){
            para.setTransType(Trans.Type.SALE);
            para.setNeedAmount(true);
            para.setNeedConfirmCard(true);
            para.setNeedOnline(true);
            para.setNeedPass(true);
            para.setNeedPrint(true);
            transPresenter = new SaleTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.ENQUIRY)){
            para.setTransType(Trans.Type.ENQUIRY);
            para.setNeedConfirmCard(true);
            para.setNeedOnline(true);
            para.setNeedPass(true);
            transPresenter = new EnquiryTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.VOID)){
            para.setTransType(Trans.Type.VOID);
            para.setNeedConfirmCard(true);
            para.setNeedOnline(true);
            para.setNeedPrint(true);
            para.setNeedPass(true);
            para.setVoid(true);
            transPresenter = new VoidTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.EC_ENQUIRY)){
            para.setTransType(Trans.Type.EC_ENQUIRY);
            para.setNeedConfirmCard(true);
            para.setECTrans(true);
            transPresenter = new EC_EnquiryTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.QUICKPASS)){
            para.setTransType(Trans.Type.QUICKPASS);
            para.setNeedAmount(true);
            para.setECTrans(true);
            para.setNeedPrint(true);
            transPresenter = new Quick_PassTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.DOWNPARA)){
            para.setTransType(Trans.Type.DOWNPARA);
            transPresenter = new DparaTrans(this ,Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.LOGON)){
            para.setTransType(Trans.Type.LOGON);
            transPresenter = new LogonTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.SETTLE)){
            para.setTransType(Trans.Type.SETTLE);
            para.setNeedPrint(true);
            transPresenter = new SettleTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.LOGOUT)){
            para.setTransType(Trans.Type.LOGOUT);
            transPresenter = new LogoutTrans(this , Trans_Types , para);
        }else if(Trans_Types.equals(Trans.Type.BARCODE)){
            Logger.debug("startTrans = barcode");
            para.setTransType(Trans.Type.BARCODE);
            para.setNeedPrint(true);
            transPresenter = new BarcodeSaleTrans(this , Trans_Types , para);
        }

        synchronized (this){
            new Thread(){
                public void run(){
                    transPresenter.start();
                }
            }.start();
        }
    }

    private static final int MSG_USER_INPUT = 0x10 ;
    private static final int MSG_CARDUSE = 0x11 ;
    private static final int MSG_CONFIRM_CARD = 0x12 ;
    private static final int MSG_HANDLING = 0x13 ;
    private static final int MSG_SHOW_ERR = 0x14 ;
    private static final int MSG_TRANS_SUCCESS = 0x15 ;
    private static final int MSG_INPUT_PIN = 0x16 ;
    private static final int MSG_SHOW_TRANS_INFO = 0x17 ;
    private static final int MSG_SHOW_OFFLINE_RET = 0x18 ;
    private static final int MSG_Barcodeuse_RET = 0x19 ; //eidt by liyo
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            clearAllViews();
            switch (msg.what){
                case MSG_USER_INPUT :
//                    startTimer(TIMEOUT);
                    if((int)msg.obj == R.string.trans_input_traceno){
                        amountEdit.setInputType(InputType.TYPE_CLASS_NUMBER);
                        amountEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
                    }
                    setNaviTitle(mContext.getString((int)msg.obj));
                    inputAmountLayout.setVisibility(View.VISIBLE);
                    break;
                case MSG_CARDUSE :
//                    startTimer(TIMEOUT);
                    cardUseLayout.setVisibility(View.VISIBLE);
                    int mode = (int)msg.obj ;
                    if( (mode & FinanceTrans.INMODE_IC ) != 0){
                       carduseInsert.setVisibility(View.VISIBLE);
                    }if( (mode & FinanceTrans.INMODE_MAG ) != 0){
                        carduseSwipe.setVisibility(View.VISIBLE);
                    }if( (mode & FinanceTrans.INMODE_NFC ) != 0){
                        cardusePat.setVisibility(View.VISIBLE);
                    }
                    setNaviTitle(R.string.trans_use_bank_card);
                    break;
                case MSG_SHOW_TRANS_INFO:
//                    startTimer(TIMEOUT);
                    setNaviTitle(R.string.trans_showinfo_trans);
                    showinfoLayout.setVisibility(View.VISIBLE);
                    TransLogData data = (TransLogData) msg.obj ;
                    showinfoMsg.append(mContext.getString(R.string.recept_card_no)+data.getCardFullNo()+"\n");
                    showinfoMsg.append(mContext.getString(R.string.recept_batch_no)+data.getBatchNo()+"\n");
                    showinfoMsg.append(mContext.getString(R.string.recept_trace_no)+data.getTraceNo()+"\n");
                    showinfoMsg.append(mContext.getString(R.string.recept_amoun)+ StringUtil.TwoWei(data.getAmount().toString())+"\n");
                    showinfoMsg.append(mContext.getString(R.string.recept_tranc_type)+data.getEName()+"\n");
                    break;
                case MSG_CONFIRM_CARD :
//                    startTimer(TIMEOUT);
                    confirmCardEdit.setText((String)msg.obj);
                    setNaviTitle(R.string.trans_confirm_card);
                    confirmCardLayout.setVisibility(View.VISIBLE);
                    break;
                case MSG_INPUT_PIN :
                    setNaviTitle(R.string.trans_input_password);
                    inputPassLayout.setVisibility(View.VISIBLE);
                    break;
                case MSG_HANDLING :
                    String status = getStatusInfo(String.valueOf((int)msg.obj));
                    setNaviTitle(status);
                    handlingLayout.setVisibility(View.VISIBLE);
                    handlingMsg.setText(status);
                    break;
                case MSG_SHOW_ERR :
                    AppToast.show(mActivity , getErrnoInfo(String.valueOf((int)msg.obj)));
                    mActivity.finish();
                    break;
                case MSG_TRANS_SUCCESS :
                    String detail = getStatusInfo(String.valueOf((int)msg.obj));
                    Logger.debug("displayTransResult");
                    displayTransResult(detail);
                    break;
                case MSG_SHOW_OFFLINE_RET:
                    if ((int)msg.obj == 0) {
                        AppToast.show(mActivity , "VERIFY OFFLINE PIN SUCCESS");
                    } else if ((int)msg.obj == 1) {
                        AppToast.show(mActivity , "You've only got last opportunity");
                    } else {
                        AppToast.show(mActivity , "You've only got" +(int)msg.obj+ "opportunities");
                    }
                    break;
                case MSG_Barcodeuse_RET:
                    setNaviTitle(R.string.trans_use_bank_card);
            }
        }
    };

    /** ==========================交易接口公共方法====================*/
    @Override
    public InputInfo getOutsideInput(int timeout , int type) {
        Logger.debug("Masterctl>>getOutsideInput");
        this.mLatch = new CountDownLatch(1) ;
        message = mHandler.obtainMessage() ;
        message.what = MSG_USER_INPUT ;
        if(FinanceTrans.AMOUNT_INPUT == type){
            message.obj = R.string.trans_input_amount ;
            mHandler.sendMessage(message);
            this.inputType = InputManager.INPUT_TYPE_NUMBERDECIMEL ;
        }if(FinanceTrans.MASTER_PASSWORD_INPUT == type){
            message.obj = R.string.trans_input_master_pass ;
            mHandler.sendMessage(message);
            this.inputType = InputManager.INPUT_TYPE_PWD_1 ;
        }if(FinanceTrans.TRANS_TRACENO_INPUT == type){
            message.obj = R.string.trans_input_traceno ;
            mHandler.sendMessage(message);
            this.inputType = InputManager.INPUT_TYPE_NUMBER ;
        }
        final InputInfo info = new InputInfo() ;
        final InputManager inputManager = new InputManager(this , amountKeyboard , amountEdit , inputType);
        inputManager.getInput(timeout, new InputListener() {
            @Override
            public void success(String input) {
                info.setResult(input);
                info.setResultFlag(true);
                mLatch.countDown();
            }

            @Override
            public void fail(int errno) {
                info.setResultFlag(false);
                info.setErrno(errno);
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return info;
    }

    @Override
    public CardInfo getCardUse(int timeout , int mode) {
        Logger.debug("Masterctl>>getCardUse");
        this.mLatch = new CountDownLatch(1) ;
        message = mHandler.obtainMessage();
        message.what = MSG_CARDUSE ;
        message.obj = mode ;
        mHandler.sendMessage(message);
        final CardInfo cInfo = new CardInfo() ;
        final CardManager cardManager = CardManager.getInstance(mode);
        cardManager.getCard(timeout, new CardListener() {
            @Override
            public void callback(CardInfo cardInfo) {
                cInfo.setResultFalg(cardInfo.isResultFalg());
                cInfo.setNfcType(cardInfo.getNfcType());
                cInfo.setCardType(cardInfo.getCardType());
                cInfo.setTrackNo(cardInfo.getTrackNo());
                cInfo.setCardAtr(cardInfo.getCardAtr());
                cInfo.setErrno(cardInfo.getErrno());
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return cInfo;
    }

    @Override
    public int showTransInfo(int timeout, TransLogData logData) {
        Logger.debug("Masterctl>>showTransInfo");
        this.mLatch = new CountDownLatch(1);
        message = mHandler.obtainMessage() ;
        message.what = MSG_SHOW_TRANS_INFO;
        message.obj = logData ;
        mHandler.sendMessage(message);
        showinfoConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retVal = 0 ;
                mLatch.countDown();
            }
        });
        showinfoCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retVal = -1 ;
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return retVal ;
    }

    @Override
    public PinInfo getPinpadOnlinePin(int timeout, String amount, String cardNo) {
        Logger.debug("Masterctl>>getPinpadOnlinePin");
        this.mLatch = new CountDownLatch(1);
        mHandler.sendEmptyMessage(MSG_INPUT_PIN);
        final PinInfo pinInfo = new PinInfo();
        PinpadManager pinpadManager = PinpadManager.getInstance();
        pinpadManager.getPin(timeout,amount,cardNo, new PinpadListener() {
            @Override
            public void callback(PinInfo info) {
                pinInfo.setResultFlag(info.isResultFlag());
                pinInfo.setErrno(info.getErrno());
                pinInfo.setNoPin(info.isNoPin());
                pinInfo.setPinblock(info.getPinblock());
                mLatch.countDown();
            }
        });
        try {
            this.mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return pinInfo ;
    }

    @Override
    public PinInfo getPinpadOfflinePin(int timeout, String amount, String cardNo) {
        Logger.debug("Masterctl>>getPinpadOfflinePin");
        this.mLatch = new CountDownLatch(1);
        mHandler.sendEmptyMessage(MSG_INPUT_PIN);
        final PinInfo pinInfo = new PinInfo();
        PinpadManager pinpadManager = PinpadManager.getInstance();
        pinpadManager.getOfflinePin(timeout,amount,cardNo, new PinpadListener() {
            @Override
            public void callback(PinInfo info) {
//                pinInfo.setResultFlag(info.isResultFlag());
//                pinInfo.setErrno(info.getErrno());
//                mLatch.countDown();
            }
        });
//        try {
//            mLatch.await();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return null ;
    }

    @Override
    public void showOfflinePinResult(int count) {
        message = mHandler.obtainMessage() ;
        message.what = MSG_SHOW_OFFLINE_RET ;
        message.obj = count ;
        mHandler.sendMessage(message);
    }

    @Override
    public int showCardApplist(int timeout, String[] list) {
        Logger.debug("Masterctl>>showCardApplist");
        return 0;
    }

    @Override
    public void handling(int timeout , int status) {
        Logger.debug("Masterctl>>handling");
        message = mHandler.obtainMessage() ;
        message.what = MSG_HANDLING ;
        message.obj = status ;
        mHandler.sendMessage(message);
    }

    @Override
    public void showError(int errcode) {
        Logger.debug("Masterctl>>showErrorFromTS");
        message = mHandler.obtainMessage() ;
        message.what = MSG_SHOW_ERR ;
        message.obj = errcode ;
        mHandler.sendMessage(message);
    }

    @Override
    public int showCardConfirm(int timeout , String cn) {
        Logger.debug("Masterctl>>showCardConfirm");
        this.mLatch = new CountDownLatch(1) ;
        message = mHandler.obtainMessage();
        message.what = MSG_CONFIRM_CARD ;
        message.obj = cn ;
        mHandler.sendMessage(message);
        confirmCardConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retVal = 0 ;
                mLatch.countDown();
            }
        });
        confirmCardCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retVal = -1 ;
                mLatch.countDown();
            }
        });
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override
    public void trannSuccess(int status) {
        Logger.debug("Masterctl>>trannSuccess");
        //处理成功的结果显示
        message = mHandler.obtainMessage() ;
        message.what = MSG_TRANS_SUCCESS ;
        message.obj = status ;
        mHandler.sendMessage(message);
    }


    //edit by liyo
    @Override
    public BarcodeInfo getBarcodeUse(int timeout , boolean mode){
        Logger.debug("Masterctl>>getBarcodeUse");
        this.mLatch = new CountDownLatch(1) ;
        message = mHandler.obtainMessage();
        message.what = MSG_Barcodeuse_RET ;
        message.obj = mode ;
        mHandler.sendMessage(message);
        final BarcodeInfo cInfo = new BarcodeInfo() ;
        final BarcodeManager cardManager = BarcodeManager.getInstance(mode);
        try {
            cardManager.getBarcode(timeout, new BarcodeListener() {
                @Override
                public void callback(BarcodeInfo barcodeInfo) {
                    cInfo.setResult(barcodeInfo.getResult());
                    if(barcodeInfo.getResult() == 0) {
                        cInfo.setCode(barcodeInfo.getCode());
                        cInfo.setType(barcodeInfo.getType());
                    }
                    Logger.debug("mLatch.countDown reduce");
                    mLatch.countDown();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*
        try {
            Logger.debug("mLatch.await start");
            mLatch.await();
            Logger.debug("mLatch.await over");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        return cInfo;
    }
    /** ===================私有方法=============================== **/

    private String getErrnoInfo(String id){
        Logger.debug("errno = "+id);
        String[] pros = AssetsUtil.getProps(this , ERRNO_STATUS_PROP , id);
        if(pros == null){
            return "unknow err["+String.valueOf(id)+"]";
        }else {
            return pros[0] ;
        }
    }

    private String getStatusInfo(String status){
        return AssetsUtil.getProps(this , TRANS_STATUS_PROP , status)[0];
    }

    private String ch2en(String ch){
        String[] chs = getResources().getStringArray(R.array.trans);
        int index = 0 ;
        for (int i = 0 ; i < chs.length ; i++){
            if(chs[i].equals(ch)){
                index = i ;
            }
        }

        return getResources().getStringArray(R.array.trans_EN)[index] ;
    }

    private void clearAllViews(){
        cardUseLayout.setVisibility(View.GONE);
        carduseSwipe.setVisibility(View.GONE);
        cardusePat.setVisibility(View.GONE);
        carduseInsert.setVisibility(View.GONE);
        inputAmountLayout.setVisibility(View.GONE);
        inputPassLayout.setVisibility(View.GONE);
        handlingLayout.setVisibility(View.GONE);
        confirmCardLayout.setVisibility(View.GONE);
        showinfoLayout.setVisibility(View.GONE);
        if(inputType != InputManager.INPUT_TYPE_NUMBERDECIMEL){
            amountEdit.setText("");
        }
    }

    private void loadWebGif(){
        carduseSwipeWV.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                "<img width=\"128\" height=\"128\" src='file:///android_asset/gif/dyn_swipe.gif'/></div></body></html>", "text/html", "UTF-8",null);
        carduseInsertWV.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                "<img width=\"128\" height=\"128\" src='file:///android_asset/gif/dyn_insert.gif'/></div></body></html>", "text/html", "UTF-8",null);
        cardusePatWv.loadDataWithBaseURL(null,"<HTML><body bgcolor='#FFF'><div align=center>" +
                "<img width=\"128\" height=\"128\" src='file:///android_asset/gif/dyn_pat.gif'/></div></body></html>", "text/html", "UTF-8",null);
    }

    public static final String TRANS_RESULT_KEY = "TRANS_RESULT_KEY" ;
    private void displayTransResult(String string){
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(TRANS_RESULT_KEY , string);
        intent.putExtras(bundle);
        intent.setClass(mContext , Resultctl.class);
        mContext.startActivity(intent);
        mActivity.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            mActivity.finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
