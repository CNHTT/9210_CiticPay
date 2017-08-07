package com.android.citic.transmanager.trans.manager;

import android.content.Context;

import com.android.citic.appmanager.Citicapp;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.device.pinpad.PinpadManager;
import com.android.citic.transmanager.device.pinpad.WorkKeyinfo;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.presenter.TransPresenter;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;

/**
 * 签退管理类
 */
public class LogoutTrans extends Trans implements TransPresenter{

	public LogoutTrans(Context ctx , String transEN , TransInputPara p) {
		super(ctx, transEN);
		para = p ;
		setTraceNoInc(false);
		TransEName = transEN ;
		if(para != null){
			transUI = para.getTransUI();
		}
	}

	@Override
	public void start() {
		timeout = 60 * 1000 ;
		transUI.handling(timeout , Tcode.Status.terminal_logonout);
		retVal = Logout();
		if(retVal!=0){
			transUI.showError(retVal);
		}else {
			transUI.trannSuccess(Tcode.Status.logonout_succ);
		}

		Logger.debug("LogoutTrans>>finish");
		return;
	}

	/**
	 * 签退
	 * @throws
	 **/
	public int Logout() {
		TransEName = Type.LOGOUT ;
		/*******fake logout********/ //edit by liyo
		if(true) {
			cfg.setTermLogon(false);
			cfg.save();
			return 0;
		}
		/****************/
		setFixedDatas();
		iso8583.clearData();
		iso8583.setField(0, MsgID);
		iso8583.setField(11, cfg.getTraceNo());
		iso8583.setField(41, cfg.getTermID());
		iso8583.setField(42, cfg.getMerchID());
		Logger.debug("Filed60 = "+Field60);
		iso8583.setField(60, Field60);
		iso8583.setField(63, ISOUtil.padleft(cfg.getOprNo()+"",2,'0') + " ");
		retVal = OnLineTrans();
		Logger.debug("LogonTrans>>Logout>>OnLineTrans finish");
		if (retVal != 0) {
			return retVal ;
		}
		String rspCode = iso8583.getfield(39);
		netWork.close();
		if (rspCode != null && rspCode.equals("00")) {
			Logger.debug("LogoutTrans>>Logout>>签退成功");
			cfg.setTermLogon(false);
			cfg.save();
			return 0 ;
		} else {
			if (rspCode == null) {
				return Tcode.T_receive_err;
			} else {
				return Integer.valueOf(rspCode);
			}
		}
	}
}
