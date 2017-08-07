package com.android.citic.transmanager.trans.finace.barcode_sale;

import android.content.Context;

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
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.android.citic.transmanager.trans.presenter.TransPresenter;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVHandler;
import com.android.citic.transmanager.device.barcode.*;

import java.util.concurrent.CountDownLatch;

/**
 * 消费交易，继承自金融类交易
 */

public class BarcodeSaleTrans extends FinanceTrans implements TransPresenter {

	public BarcodeSaleTrans(Context ctx, String transEname , TransInputPara p) {
		super(ctx, transEname);
		para = p ;
		transUI = para.getTransUI() ;
		isReversal = true;
		isSaveLog = true;
		isDebit = true;
		isProcPreTrans = true;
//		isProcSuffix = true;
	}

	@Override
	public void start() {
		//取金额
		Logger.debug("getOutsideAmt");
		int timeout = 60 * 1000 ;
		InputInfo inputInfo = transUI.getOutsideInput(timeout , AMOUNT_INPUT);
		if(inputInfo.isResultFlag()){
			Logger.debug(inputInfo.getResult());
			Amount = Long.parseLong(inputInfo.getResult());
			para.setAmount(Amount);
			para.setOtherAmount(0);

			Logger.debug("getBarcodeUse");
			inputMode = ENTRY_MODE_BARCODE;
			BarcodeInfo barcodeInfo = transUI.getBarcodeUse(timeout,false);
			if(barcodeInfo.getResult() == 0){
				if(retVal == 0){
					retVal = FakeOnlineBarcodeTrans(new String(barcodeInfo.getCode()));
					if(retVal == 0)
						transUI.trannSuccess(Tcode.Status.success);
					else
						transUI.showError(retVal);
				}else {
					transUI.showError(retVal);
				}
			}else {
				Logger.debug("barcodeSaleTrans>>showError");
				transUI.showError(barcodeInfo.getResult());
			}
		}else {
			Logger.debug("barcodeSaleTrans>>get errno");
			transUI.showError(inputInfo.getErrno());
		}

		Logger.debug("barcodeSaleTrans>>finish");
		return;
	}



}
