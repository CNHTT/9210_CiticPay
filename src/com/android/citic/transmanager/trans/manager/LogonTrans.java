package com.android.citic.transmanager.trans.manager;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

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
 * 签到管理类
 */
public class LogonTrans extends Trans implements TransPresenter{

	public LogonTrans(Context ctx , String transEN , TransInputPara p) {
		super(ctx, transEN);
		para = p ;
		setTraceNoInc(false);
		TransEName = transEN ;
		transUI = para.getTransUI();
	}

	@Override
	public void start() {
		timeout = 60 * 1000 ;
		retVal = sign() ;
		if(retVal == 0){
			cfg.setTermLogon(true);
			cfg.save();
			if(!GlobalCfg.isEmvParamLoaded()){
				DparaTrans dTrans = new DparaTrans(context , Type.DOWNPARA , null);
				transUI.handling(timeout , Tcode.Status.downing_aid);
				//retVal = dTrans.DownloadAid();//edit by liyo
				retVal = dTrans.DownloadAidTest();
				if(retVal == 0){
					transUI.handling(timeout , Tcode.Status.downing_capk);
					//retVal = dTrans.DownloadCapk();//edit by liyo
					retVal = dTrans.DownloadCapkTest();
					if(retVal == 0){
						DparaTrans.loadAIDCAPK2EMVKernel();
						transUI.trannSuccess(Tcode.Status.logon_down_succ);
					}else {
						transUI.showError(retVal);
					}
				}else {
					transUI.showError(retVal);
				}
			}else {
				transUI.trannSuccess(Tcode.Status.logon_succ);
			}

		}else {
			transUI.showError(retVal);
		}

		Logger.debug("LogonTrans>>finish");
		return;
	}

	private int sign(){
		transUI.handling(timeout , Tcode.Status.terminal_logon);
		if(false) //edit by liyo
			retVal = SignIn();
		else{  //edit by liyo
			String sKey = "31313131313131313131313131313131313131313131313131313131313131313131313131313131";
			byte[] bKey = ISOUtil.str2bcd(sKey, false);
			Log.v("liyo","sKey");
			Log.v("liyo",sKey);

			Log.v("liyo","bkey");
			Log.v("liyo", ISOUtil.bcd2str(bKey,0,80,false));
			setKeyForTest(bKey);
			retVal = 0;
		}
		if(retVal!=0){
			return retVal ;
		}
		return 0 ;
	}

	private void setFields() {
		if (MsgID != null)
			iso8583.setField(0, MsgID);
		if (TraceNo != null)
			iso8583.setField(11, TraceNo);// 11
		if (LocalTime != null)
			iso8583.setField(12, LocalTime); // 12 hhmmss
		if (LocalDate != null)
			iso8583.setField(13, LocalDate); // 13 MMDD
		iso8583.setField(41, TermID); // 41
		if (MerchID != null)
			iso8583.setField(42, MerchID);// 42
		if (Field60 != null)
			iso8583.setField(60, Field60);// 60
		if (Field62 != null)
			iso8583.setField(62, Field62);// 62
		if (Field63 != null)
			iso8583.setField(63, Field63);// 63
	}

	/**
	 * 签到
	 * @throws
	 **/
	public int SignIn() {
		TransEName = Type.LOGON ;
		setFixedDatas();
		iso8583.setField(11, cfg.getTraceNo());
		iso8583.setField(62,"53657175656E6365204E6F3132333230393832303030373031");
		String f60_3 ;
		if (cfg.isSingleKey()) {
			f60_3 = "001";
		} else if (cfg.isTrackEncrypt()) {
			f60_3 = "004";
		} else {
			f60_3 = "003";
		}
		Field60 = Field60.substring(0, 8) + f60_3;
		iso8583.setField(63, ISOUtil.padleft(cfg.getOprNo()+"",2,'0') + " ");
		setFields();
		retVal = OnLineTrans();
		Logger.debug("LogonTrans>>SignIn>>OnLineTrans finish");
		if (retVal != 0) {
			return retVal ;
		}
		String rspCode = iso8583.getfield(39);
		netWork.close();
		if (rspCode != null && rspCode.equals("00")) {
			String str60 = iso8583.getfield(60);
			cfg.setBatchNo(Integer.parseInt(str60.substring(2 , 8)));
			cfg.save();
			Logger.debug("current batchNo = " + cfg.getBatchNo());
			String strField62 = iso8583.getfield(62);
			if (strField62 == null)
				return Tcode.T_receive_err;
			byte[] field62 = ISOUtil.str2bcd(strField62, false);
			int setKeyRet = setKey(field62);
			if (setKeyRet != 0) {
				return setKeyRet ;
			} else {
				return 0;
			}
		} else {
			if (rspCode == null) {
				return Tcode.T_receive_err;
			} else {
				return Integer.valueOf(rspCode);
			}
		}
	}

	private int setKey(byte[] keyData) {
		Logger.debug("setKey>>keyData data = "+ISOUtil.hexString(keyData));
		Logger.debug("setKey>>keyData len = "+keyData.length);
		Logger.debug("LogonTrans>>setKey");
		WorkKeyinfo workKeyinfo = new WorkKeyinfo() ;
		workKeyinfo.setMasterKeyIndex(cfg.getMasterKeyIndex());
		workKeyinfo.setWorkKeyIndex(cfg.getMasterKeyIndex());
		workKeyinfo.setMode(Ped.KEY_VERIFY_KVC);

		workKeyinfo.setKeySystem(KeySystem.MS_DES);

		byte[] temp ; // 临时存储数组
		int keyLen;//密钥长度
//		if(keyData.length!=60)
//			return -1 ;//银联下发三个密钥
//		if(keyData.length!=40)
//			return -2 ;//中信银行下发两个密钥
		keyLen = 20 ;

		//注入PINK
		temp = new byte[keyLen];
		System.arraycopy(keyData, 0, temp, 0, keyLen);
		long start = System.currentTimeMillis();
		workKeyinfo.setKeyType(KeyType.KEY_TYPE_PINK);
		byte[] pink = new byte[16];
		System.arraycopy(temp , 0 , pink , 0 , 16);
		workKeyinfo.setPrivacyKeyData(temp);
		retVal = PinpadManager.loadWKey(workKeyinfo);
		Logger.debug("LogonTrans>>setKey>>PINK="+retVal);
		long end = System.currentTimeMillis();
		Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
		if (retVal != 0) {
			return retVal;
		}

		//注入MACK
		System.arraycopy(keyData, keyLen, temp, 0, keyLen);
		start = System.currentTimeMillis();
		workKeyinfo.setKeyType(KeyType.KEY_TYPE_MACK);
		byte[] mack = new byte[16];
		System.arraycopy(temp , 0 , mack , 0 , 16);
		workKeyinfo.setPrivacyKeyData(temp);
		retVal = PinpadManager.loadWKey(workKeyinfo);
		Logger.debug("LogonTrans>>setKey>>MACK="+retVal);
		end = System.currentTimeMillis();
		Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
		if (retVal != 0) {
			return retVal;
		}

		//注入EACK
		if(cfg.isTrackEncrypt()){
			System.arraycopy(keyData, keyLen*2, temp, 0, keyLen);
			start = System.currentTimeMillis();
			//71657F39F8B3D6562CC515E0403BEB676CCCB22E
			//国密不支持，使用MAC区域去保存，注意索引
			workKeyinfo.setKeyType(KeyType.KEY_TYPE_EAK);
			workKeyinfo.setPrivacyKeyData(temp);
			retVal = PinpadManager.loadWKey(workKeyinfo);
			Logger.debug("LogonTrans>>setKey>>EACK="+retVal);
			end = System.currentTimeMillis();
			Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
			if (retVal != 0) {
				return retVal;
			}
		}

		return 0;
	}

	private int setKeyForTest(byte[] keyData) { //eidt by liyo
		Logger.debug("setKey>>keyData data = "+ISOUtil.hexString(keyData));
		Logger.debug("setKey>>keyData len = "+keyData.length);
		Logger.debug("LogonTrans>>setKey");
		WorkKeyinfo workKeyinfo = new WorkKeyinfo() ;
		workKeyinfo.setMasterKeyIndex(cfg.getMasterKeyIndex());
		workKeyinfo.setWorkKeyIndex(cfg.getMasterKeyIndex());
		workKeyinfo.setMode(Ped.KEY_VERIFY_NONE);

		workKeyinfo.setKeySystem(KeySystem.MS_DES);

		byte[] temp ; // 临时存储数组
		int keyLen;//密钥长度
//		if(keyData.length!=60)
//			return -1 ;//银联下发三个密钥
//		if(keyData.length!=40)
//			return -2 ;//中信银行下发两个密钥
		keyLen = 16 ;

		Log.v("liyo","start inject pink");
		//注入PINK
		temp = new byte[keyLen];
		System.arraycopy(keyData, 0, temp, 0, keyLen);

		long start = System.currentTimeMillis();
		workKeyinfo.setKeyType(KeyType.KEY_TYPE_PINK);
		byte[] pink = new byte[16];
		System.arraycopy(temp , 0 , pink , 0 , 16);
		workKeyinfo.setPrivacyKeyData(temp);
		retVal = PinpadManager.loadWKey(workKeyinfo);
		Logger.debug("LogonTrans>>setKey>>PINK="+retVal);
		long end = System.currentTimeMillis();
		Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
		if (retVal != 0) {
			Log.v("liyo","inject pink err");
			Log.v("liyo","retval:"+retVal);
			return retVal;
		}

		//注入MACK
		System.arraycopy(keyData, keyLen, temp, 0, keyLen);
		start = System.currentTimeMillis();
		workKeyinfo.setKeyType(KeyType.KEY_TYPE_MACK);
		byte[] mack = new byte[16];
		System.arraycopy(temp , 0 , mack , 0 , 16);
		workKeyinfo.setPrivacyKeyData(temp);
		retVal = PinpadManager.loadWKey(workKeyinfo);
		Logger.debug("LogonTrans>>setKey>>MACK="+retVal);
		end = System.currentTimeMillis();
		Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
		if (retVal != 0) {
			Log.v("liyo","inject mackey err");
			return retVal;
		}

		//注入EACK
		if(cfg.isTrackEncrypt()){
			System.arraycopy(keyData, keyLen*2, temp, 0, keyLen);
			start = System.currentTimeMillis();
			//71657F39F8B3D6562CC515E0403BEB676CCCB22E
			//国密不支持，使用MAC区域去保存，注意索引
			workKeyinfo.setKeyType(KeyType.KEY_TYPE_EAK);
			workKeyinfo.setPrivacyKeyData(temp);
			retVal = PinpadManager.loadWKey(workKeyinfo);
			Logger.debug("LogonTrans>>setKey>>EACK="+retVal);
			end = System.currentTimeMillis();
			Logger.debug("LogonTrans>>setKey>>TIME="+(end - start));
			if (retVal != 0) {
				Log.v("liyo","inject EACK err");
				return retVal;
			}
		}
		Log.v("liyo","inject wk ok");
		return 0;
	}
}
