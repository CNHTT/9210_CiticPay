package com.android.citic.transmanager.trans.finace.quickpass;

import android.content.Context;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.device.card.CardInfo;
import com.android.citic.transmanager.device.card.CardManager;
import com.android.citic.transmanager.device.input.InputInfo;
import com.android.citic.transmanager.device.pinpad.PinInfo;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.process.EmvTransaction;
import com.android.citic.transmanager.process.QpbocTransaction;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.android.citic.transmanager.trans.presenter.TransPresenter;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVHandler;

public class Quick_PassTrans extends FinanceTrans implements TransPresenter{

	public Quick_PassTrans(Context ctx, String transEname , TransInputPara p) {
		super(ctx, transEname);
		para = p ;
		transUI = para.getTransUI() ;
		isReversal = false;
		isSaveLog = true;
		isDebit = true;
		isProcPreTrans = true;
//		isProcSuffix = true;
	}

	@Override
	public void start() {
		//取卡
		timeout = 60 * 1000 ;
		Logger.debug("Quick_PassTrans>>getOutsideInput.....");
		InputInfo info = transUI.getOutsideInput(timeout , AMOUNT_INPUT);
		if(info.isResultFlag()){
			Amount = Long.parseLong(info.getResult()) ;
			para.setAmount(Amount);
			para.setOtherAmount(0);
			Logger.debug("Quick_PassTrans>>Amount="+Amount);

			Logger.debug("Quick_PassTrans>>getCarduse.....");
			CardInfo cardInfo = transUI.getCardUse(timeout , INMODE_NFC|INMODE_IC);
			afterGetCardUse(cardInfo);
		}else {
			transUI.showError(info.getErrno());
		}


		Logger.debug("Quick_PassTrans>>finish");
		return;
	}

	private void afterGetCardUse(CardInfo info){
		if(info.isResultFalg()){
			int type = info.getCardType() ;
			switch (type){
				case CardManager.TYPE_MAG :inputMode = ENTRY_MODE_MAG ;break;
				case CardManager.TYPE_ICC :inputMode = ENTRY_MODE_ICC ;break;
				case CardManager.TYPE_NFC :inputMode = ENTRY_MODE_NFC ;break;
			}
			Logger.debug("Quick_PassTrans>>inputMode = "+inputMode);
			para.setInputMode(inputMode);
			if(inputMode == ENTRY_MODE_NFC){
				isNFC();
			}
			if(inputMode == ENTRY_MODE_ICC){
				isICC();
			}
		}else {
			transUI.showError(info.getErrno());
		}
	}

	private void isNFC(){
		if(GlobalCfg.isEmvParamLoaded()) {
			transUI.handling(timeout, Tcode.Status.handling);
			qpboc = new QpbocTransaction(para);
			retVal = qpboc.start();
			Logger.debug("Quick_PassTrans>>QpbocTransaction=" + retVal);
			if(retVal == 0){
				byte[] temp = new byte[256];
				TLVUtil.get_tlv_data_kernal(0x57, temp);
				Pan = ISOUtil.hexString(temp).split("D")[0];
				byte[] res = new byte[32] ;
				TLVUtil.get_tlv_data_kernal(0x9F10 , res);
				Logger.debug("9f10 = "+ISOUtil.hexString(res));
				if( (res[4]&0x30) == (byte)0x20 ){
					Logger.debug("QPBOC ARQC");
					setFixedDatas();
					isReversal = true ;
					PinInfo info = transUI.getPinpadOnlinePin(timeout ,String.valueOf(Amount), Pan);
					afterQpbocGetPin(info);
				}else if( ((res[4]&0x30)  == (byte)0x10) || ((res[4]&0xC0) == (byte)0x40)){
					Logger.debug("QPBOC TC");
					setICCData();
					retVal = offlineTrans("00" , qpboc.getCardNO());
					if(0 == retVal){
						transUI.trannSuccess(Tcode.Status.quickpass_succ);
					}else {
						transUI.showError(retVal);
					}
				} else {
					Logger.debug("QPBOC REFUSE");
					transUI.showError(Tcode.T_pboc_refuse);
				}
			}else {
				transUI.showError(retVal);
			}
		}else {
			transUI.showError(Tcode.T_terminal_no_aid);
		}
	}

	private void isICC(){
		if(GlobalCfg.isEmvParamLoaded()) {
			transUI.handling(timeout, Tcode.Status.handling);
			emv = new EmvTransaction(para);
			retVal = emv.start();
			Logger.debug("Quick_PassTrans>>EmvTransaction=" + retVal);
			if(retVal == 1){
				Logger.debug("Quick_PassTrans>>Online>>转为联机");
				setFixedDatas();
				isReversal = true ;
				Pan = emv.getCardNo() ;
				PinInfo info = transUI.getPinpadOnlinePin(timeout,String.valueOf(Amount),Pan);
				if(info.isResultFlag()){
					if(info.isNoPin()){
						isPinExist = false ;
					}else {
						isPinExist = true;
						PIN = ISOUtil.hexString(info.getPinblock());
					}
					//设置55域数据
					setICCData();
					prepareOnline(emv.getCardNo());
				}else {
					transUI.showError(info.getErrno());
				}
			}else if(retVal == 0){
				Logger.debug("Quick_PassTrans>>Offline");
				setICCData();
				retVal = offlineTrans("00" , emv.getCardNo());
				if(0 == retVal){
					transUI.trannSuccess(Tcode.Status.quickpass_succ);
				}else {
					transUI.showError(retVal);
				}
			}else {
				transUI.showError(retVal);
			}
		}else {
			transUI.showError(Tcode.T_terminal_no_aid);
		}
	}

	/** pboc后续处理 */
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
			setICCData();
			prepareOnline(qpboc.getCardNO());
		}else {
			transUI.showError(info.getErrno());
		}
	}

	/** 准备联机 */
	private void prepareOnline(String fullcard){
		//设置完55域数据即可请求联机
		transUI.handling(timeout , Tcode.Status.connecting_center);
		setDatas(inputMode);
		//联机处理
		if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC){
			//retVal = OnlineTrans(emv , fullcard);
			retVal = FakeOnlineTrans(emv , fullcard);//edit by liyo
		}else{
			//retVal = OnlineTrans(null , fullcard);
			retVal = FakeOnlineTrans(null , fullcard); //edit by liyo
		}
		Logger.debug("SaleTrans>>OnlineTrans="+retVal);
		clearPan();
		if(retVal == 0){
			transUI.trannSuccess(Tcode.Status.sale_succ);
		}else {
			transUI.showError(retVal);
		}
	}
}
