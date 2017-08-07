package com.android.citic.transmanager.process;

import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.Log;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.input.InputInfo;
import com.android.citic.transmanager.device.input.InputManager;
import com.android.citic.transmanager.device.pinpad.PinInfo;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.android.citic.transmanager.trans.presenter.TransUI;
import com.pos.device.SDKException;
import com.pos.device.emv.CandidateListApp;
import com.pos.device.emv.CoreParam;
import com.pos.device.emv.EMVHandler;
import com.pos.device.emv.IEMVCallback;
import com.pos.device.emv.IEMVHandler;
import com.pos.device.emv.TerminalMckConfigure;
import com.pos.device.icc.ContactCard;
import com.pos.device.icc.IccReader;
import com.pos.device.icc.OperatorMode;
import com.pos.device.icc.SlotType;
import com.pos.device.icc.VCC;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;
import com.pos.device.ped.Ped;
import com.pos.device.picc.EmvContactlessCard;
import com.pos.device.picc.PiccReader;
import com.secure.api.PadView;

import java.util.concurrent.CountDownLatch;


/**
 * EMV交易流程
 */
public class EmvTransaction {

	private static IccReader icCard = null ;
	private static ContactCard contactCard = null ;
	private static IEMVHandler emvHandler = null ;
	private static PiccReader nfcCard = null ;
	private static EmvContactlessCard emvContactlessCard = null ;
	private int timeout ;

	private long Amount;
	private TransLogData data ;
	private int inputMode ;
	private long otherAmount;

	private String rspCode;// 39
	private String authCode;// 38
	private byte[] rspICCData;// 55
	private int onlineResult;// 成功和失败
	private String pinBlock = "";
	private String ECAmount = "" ;

	final int wOnlineTags[] = { 0x9F26, // AC (Application Cryptogram)
		 0x9F27, // CID
		 0x9F10, // IAD (Issuer Application Data)
		 0x9F37, // Unpredicatable Number
		 0x9F36, // ATC (Application Transaction Counter)
		 0x95, // TVR
		 0x9A, // Transaction Date
		 0x9C, // Transaction Type
		 0x9F02, // Amount Authorised
		 0x5F2A, // Transaction Currency Code
		 0x82, // AIP
		 0x9F1A, // Terminal Country Code
		 0x9F03, // Amount Other
		 0x9F33, // Terminal Capabilities
		 // opt
		 0x9F34, // CVM Result
		 0x9F35, // Terminal Type
		 0x9F1E, // IFD Serial Number
		 0x84, // Dedicated File Name
		 0x9F09, // Application Version #
		 0x9F41, // Transaction Sequence Counter
		 // 0x5F34, // PAN Sequence Number
	0 };
		 // 0X8E, //CVM

	final int wISR_tags[] = { 0x9F33, // Terminal Capabilities
		 0x95, // TVR
		 0x9F37, // Unpredicatable Number
		 0x9F1E, // IFD Serial Number
		 0x9F10, // Issuer Application Data
		 0x9F26, // Application Cryptogram
		 0x9F36, // Application Tranaction Counter
		 0x82, // AIP
		 0xDF31, // 发卡行脚本结果
		 0x9F1A, // Terminal Country Code
		 0x9A, // Transaction Date
	0 };

	final int reversal_tag[] = { 0x95, // TVR
		 0x9F1E, // IFD Serial Number
		 0x9F10, // Issuer Application Data
		 0x9F36, // Application Transaction Counter
		 0xDF31, // 发卡行脚本结果
	0 };

	private TransUI transUI ;
	private TransInputPara para ;

	/**
	 * 初始化内核专用构造器
	 */
	public EmvTransaction() {
		emvHandler = EMVHandler.getInstance();
	}

	/**
	 * EMV流程专用构造器
     */
	public EmvTransaction(TransInputPara p){
		this.emvHandler = EMVHandler.getInstance();
		this.para = p ;
		this.transUI = para.getTransUI() ;
		Logger.debug("amount = "+para.isNeedAmount());
		Logger.debug("online = "+para.isNeedOnline());
		Logger.debug("void = "+para.isVoid());
		Logger.debug("pass = "+para.isNeedPass());
		Logger.debug("eccash = "+para.isECTrans());
		Logger.debug("print = "+para.isNeedPrint());
		if(para.isVoid()){
			this.data = para.getVoidTransData() ;
		}
		if(para.isNeedAmount()){
			this.Amount = para.getAmount();
			this.otherAmount = para.getOtherAmount();
		}
		this.inputMode = para.getInputMode();
		if(inputMode == Trans.ENTRY_MODE_NFC){
			try {
				nfcCard = PiccReader.getInstance();
				emvContactlessCard = EmvContactlessCard.connect() ;
			} catch (SDKException e) {
				e.printStackTrace();
			}
		}if(inputMode == Trans.ENTRY_MODE_ICC){
			try {
				icCard = IccReader.getInstance(SlotType.USER_CARD);
				contactCard = icCard.connectCard(VCC.VOLT_5 , OperatorMode.EMV_MODE) ;
			} catch (SDKException e) {
				e.printStackTrace();
			}
		}
	}

	/** EMV交易流程 **/
	public int start() {
		Logger.debug("EmvTransaction>>EMVTramsProcess");
		timeout = 60 * 1000 ;

		initEmvKernel();

		int ret = emvReadData(true);
		if (ret != 0) {
			return ret;
		}

		if(para.isNeedConfirmCard()){
			Logger.debug("EmvTransaction>>EMVTramsProcess>>提示确认卡号");
			String cn = getCardNo();
			Logger.debug("EmvTransaction>>EMVTramsProcess>>卡号="+cn);
			ret = transUI.showCardConfirm(timeout , cn);
			if(ret != 0){
				return Tcode.T_user_cancel_operation ;
			}
		}

		if(para.isVoid()){//当前为撤销交易
			if(!data.getCardFullNo().equals(getCardNo())){
				//撤销进行卡号判断
				return Tcode.T_void_card_not_same;
			}
		}

		Logger.debug("EmvTransaction>>EMVTramsProcess>>持卡人认证");
		try {
			ret = emvHandler.cardholderVerify();
		} catch (SDKException e) {
			e.printStackTrace();
		}
		Logger.debug("EmvTransaction>>EMVTramsProcess>>cardholderVerify="+ret);
		if (ret != 0) {
			Logger.debug("EmvTransaction>>EMVTramsProcess>>cardholderVerify fail");
			if(para.isVoid()){
				return 0 ;
			}else {
				return Tcode.T_card_holder_auth_err ;
			}
		}

		Logger.debug("EmvTransaction>>EMVTramsProcess>>终端风险分析");
		try {
			ret = emvHandler.terminalRiskManage();
		} catch (SDKException e) {
			e.printStackTrace();
		}
		Logger.debug("EmvTransaction>>EMVTramsProcess>>terminalRiskManage="+ret);
		if (ret != 0) {
			Logger.debug("EmvTransaction>>EMVTramsProcess>>terminalRiskManage fail");
			return Tcode.T_terminal_action_ana_err ;
		}

		Logger.debug("EmvTransaction>>EMVTramsProcess>>是否联机");
		boolean isNeedOnline = false ;
		try {
			isNeedOnline = emvHandler.terminalActionAnalysis();
		} catch (SDKException e) {
			e.printStackTrace();
		}
		Logger.debug("EmvTransaction>>EMVTramsProcess>>terminalActionAnalysis="+isNeedOnline);
		if(isNeedOnline){
			Logger.debug("EmvTransaction>>EMVTramsProcess>>联机交易");
			Logger.debug("EMV完整流程结束");
			return 1;
		}
		Logger.debug("EmvTransaction>>EMVTramsProcess>>脱机批准");
		Logger.debug("EMV完整流程结束");
		return 0;
	}

	/**
	 * EMV读数据
	 * @param ifOfflineDataAuth
	 * @return
	 */
	private int emvReadData(boolean ifOfflineDataAuth) {
		Logger.debug("EmvTransaction>>emvReadData>>start");
		if(para.isECTrans()){
			emvHandler.pbocECenable(true);
			Logger.debug("set pboc EC enable true");
		}else {
			emvHandler.pbocECenable(false);
			Logger.debug("set pboc EC enable false");
		}
		emvHandler.setEMVInitCallback(emvInitListener);
		emvHandler.setApduExchangeCallback(apduExchangeListener);
		emvHandler.setDataElement(new byte[] { (byte) 0x9c }, new byte[] { 0x00 });

		Logger.debug("EmvTransaction>>emvReadData>>应用选择");
		int ret  = emvHandler.selectApp(Integer.parseInt(ISOUtil.padleft(2 + "", 6, '0')));
		Logger.debug("EmvTransaction>>emvReadData>>selectApp = " + ret);
		if (ret != 0) {
			Logger.debug("EmvTransaction>>emvReadData>>selectApp fail");
			return Tcode.T_select_app_err ;
		}

		if(para.isECTrans()){
			byte[] apdu = ISOUtil.hex2byte("80CA9F7900");
			byte[] recv = exeAPDU(apdu);
			ECAmount = fromApdu2Amount(ISOUtil.hexString(recv));
			Logger.debug("获取电子现金余额（9F79）="+ECAmount);
			apdu = ISOUtil.hex2byte("80CA9F7800");
			recv = exeAPDU(apdu);
			Logger.debug("电子现金单笔限额（9F78）="+fromApdu2Amount(ISOUtil.hexString(recv)));
			apdu = ISOUtil.hex2byte("80CA9F5D00");
			recv = exeAPDU(apdu);
			Logger.debug("获取电子现金余额(9F5D) = "+fromApdu2Amount(ISOUtil.hexString(recv)));
		}

		Logger.debug("EmvTransaction>>emvReadData>>读应用数据");
		try {
			ret = emvHandler.readAppData();
		} catch (SDKException e) {
			e.printStackTrace();
		}
		Logger.debug("EmvTransaction>>emvReadData>>readAppData = " + ret);
		if (ret != 0) {
			Logger.debug("EmvTransaction>>emvReadData>>readAppData fail");
			return Tcode.T_read_app_data_err ;
		}

		Logger.debug("EmvTransaction>>emvReadData>>脱机数据认证");
		if (ifOfflineDataAuth) {
			try {
				ret = emvHandler.offlineDataAuthentication();
			} catch (SDKException e) {
				e.printStackTrace();
			}
			Logger.debug("EmvTransaction>>emvReadData>>offlineDataAuthentication=" + ret);
			if (ret != 0) {
				Logger.debug("EmvTransaction>>emvReadData>>offlineDataAuthentication fail");
				return Tcode.T_offline_dataauth_err ;
			}
		}
		Logger.debug("EmvTransaction>>emvReadData>>finish");
		return 0;
	}

	/**
	 * EMV联机后处理
	 * @param rspCode
	 * @param authCode
	 * @param rspICCData
	 * @param onlineResult
     * @return
     */
	public int afterOnline(String rspCode, String authCode, byte[] rspICCData, int onlineResult) {
		Logger.debug("enter afterOnline");
		Logger.debug("rspCode = " + rspCode);
		Logger.debug("authCode = " + authCode);
		if(rspICCData!=null)
			Logger.debug("rspICCData = " + ISOUtil.byte2hex(rspICCData));

		this.rspCode = rspCode;
		this.authCode = authCode;
		this.rspICCData = rspICCData;
		this.onlineResult = onlineResult;

		boolean onlineTransaction = false ;
		try {
			onlineTransaction = emvHandler.onlineTransaction();
		} catch (SDKException e) {
			e.printStackTrace();
		}
		Logger.debug("onlineTransaction =" + onlineTransaction);
		if(onlineTransaction){
			return 0 ;
		}else {
			return -1 ;
		}
	}

	public String getCardNo(){
		byte[] temp = new byte[256];
		int len = TLVUtil.get_tlv_data_kernal(0x5A, temp);
		return ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
	}

	public String getPinBlock(){
		return pinBlock ;
	}

	public String getECAmount(){
		return ECAmount ;
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
			Logger.debug("setSupportForcedOnlineCapability true");
		}else {
			configure.setSupportForcedOnlineCapability(false);
			Logger.debug("setSupportForcedOnlineCapability false");
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

	private IEMVCallback.EMVInitListener emvInitListener = new IEMVCallback.EMVInitListener() {
		@Override
		public int candidateAppsSelection() {
			Logger.debug("======candidateAppsSelection=====");
			int[] numData = new int[1];
			CandidateListApp[] listapp = new CandidateListApp[32];
			try {
				listapp = emvHandler.getCandidateList();
			} catch (SDKException e) {
				e.printStackTrace();
			}
			int ret = 0 ;
			if (listapp.length > 0) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < numData[0]; i++) {
					Logger.debug("应用名称："+listapp[i].toString());
					if (i == 0)
						sb.append(new String(listapp[i].gettCandAppName()));
					else
						sb.append(","+new String(listapp[i].gettCandAppName()));
				}
				if (numData[0] > 1) {
					// 弹框提示用户选择应用
					//TODO
					Logger.debug("卡片多应用选择");
					transUI.showCardApplist(timeout , sb.toString().split(","));
				}
				Logger.debug("EMV>>多应用选择>>用户选择的应用ret="+ret);
				return ret;
			} else{
				return -1;
			}
		}

		@Override
		public void multiLanguageSelection() {
			byte[] tag = new byte[] { 0x5F, 0x2D };
			int ret = emvHandler.checkDataElement(tag);
			// 从内核读aid 0x9f06 设置是否支持联机PIN
			Logger.debug("===multiLanguageSelection==ret:"+ ret);
		}

		@Override
		public int getAmount(int[] transAmount, int[] cashBackAmount) {
			Logger.debug("====getAmount======"+Amount);
			if (para.isNeedAmount()) {
				if(Amount <= 0){//没输入金额
					// 调用输入金额
					Logger.debug("EMV>>需要输入金额且金额参数未0>>EMV流程执行获取金额的回调");
					InputInfo info = transUI.getOutsideInput(timeout , InputManager.INPUT_TYPE_NUMBERDECIMEL);
					if(info.isResultFlag()){
						Amount = (int) (Double.parseDouble(info.getResult()) * 100);
						Logger.debug("EMV>>用户输入的金额="+Amount);
					}else {
						//TODO
					}
					otherAmount = 0;
				}else {
					transAmount[0] = Integer.valueOf(Amount + "");
					cashBackAmount[0] = Integer.valueOf(otherAmount + "");
				}
			}
			return 0;
		}

		@Override
		public int getPin(int[] pinLen, byte[] cardPin) {
			Logger.debug("=====getOfflinePin======");
			// 读PED倒计时并为零 才继续执行
			// 请输入OFFLINE PIN
			transUI.getPinpadOfflinePin(timeout ,String.valueOf(Amount) , getCardNo());
			return 0 ;
		}

		@Override
		public int pinVerifyResult(int tryCount) {
			Logger.debug("======pinVerifyResult======="+tryCount);
			// 处理脱机pin 是否成功 失败提示重试次数
			//TODO
			Ped.getInstance().removeKeyBoardView();
			transUI.showOfflinePinResult(tryCount);
			return 0;
		}

		@Override
		public int checkOnlinePIN() {
			Logger.debug("checkOnlinePIN pass="+para.isNeedPass());
			if(para.isNeedPass()){
				Logger.debug("=====checkOnlinePIN=======");
				byte[] val = new byte[16];
				String cardNum = "" ;
				try {
					val = emvHandler.getDataElement(new byte[] { 0x5A });
				} catch (SDKException e) {
					e.printStackTrace();
				}if (val!=null) {
					cardNum = ISOUtil.trimf(ISOUtil.byte2hex(val, 0, val.length));
					Logger.debug("EMV>>获取联机PIN>>卡号="+ cardNum );
				}
				PinInfo info = transUI.getPinpadOnlinePin(timeout, String.valueOf(Amount) , cardNum);
				if(info.isResultFlag()){
					if(info.isNoPin()){
						return -1 ;
					}else {
						pinBlock = ISOUtil.hexString(info.getPinblock()) ;
						Logger.debug("EMV>>获取联机PIN>>pinBlock="+pinBlock);
					}
				}else {
					pinBlock = null ;
					return -1 ;
				}
				return 0;
			}else {
				Logger.debug("EMV>>checkOnlinePIN>>ret=0");
				return 0 ;
			}
		}

		/** 核对身份证证件 **/
		@Override
		public int checkCertificate() {
			Logger.debug("=====checkCertificate====");
			return 0;
		}

		@Override
		public int onlineTransactionProcess(byte[] brspCode, byte[] bauthCode,
											int[] authCodeLen, byte[] bauthData, int[] authDataLen,
											byte[] script, int[] scriptLen, byte[] bonlineResult) {
			Logger.debug("==onlineTransactionProcess========");
			brspCode[0] = 0;
			brspCode[1] = 0;
			authCodeLen[0] = 0;
			scriptLen[0] = 0;
			authDataLen[0] = 0;
			bonlineResult[0] = (byte) onlineResult;
			if (rspCode == null || rspCode.equals("") || onlineResult != 0) {
				return 0;
			} else {
				System.arraycopy(rspCode.getBytes(), 0, brspCode, 0, 2);
			}if (authCode == null || authCode.equals("")) {
				authCodeLen[0] = 0;
			} else {
				authCodeLen[0] = authCode.length();
				System.arraycopy(authCode.getBytes(), 0, bauthCode, 0, authCodeLen[0]);
			}if (rspICCData != null && rspICCData.length > 0) {
				authDataLen[0] = TLVUtil.get_tlv_data(rspICCData, rspICCData.length, 0x91, bauthData, false);
				byte[] scriptTemp = new byte[256];
				int scriptLen1 = TLVUtil.get_tlv_data(rspICCData, rspICCData.length, 0x71, scriptTemp, true);
				System.arraycopy(scriptTemp, 0, script, 0, scriptLen1);
				int scriptLen2 = TLVUtil.get_tlv_data(rspICCData, rspICCData.length, 0x72, scriptTemp, true);
				System.arraycopy(scriptTemp, 0, script, scriptLen1, scriptLen2);
				scriptLen[0] = scriptLen1 + scriptLen2;
			}
			bonlineResult[0] = (byte) onlineResult;
			Logger.debug("onlineTransactionProcess return_exit 0.");
			return 0;
		}

		@Override
		public int issuerReferralProcess() {
			Logger.debug("=====issuerReferralProcess======");
			return 0;
		}

		@Override
		public int adviceProcess(int firstFlg) {
			Logger.debug("=====adviceProcess======");
			return 0;
		}

		@Override
		public int checkRevocationCertificate(int caPublicKeyID, byte[] RID,
											  byte[] destBuf) {
			Logger.debug("===checkRevocationCertificate==");
			return -1;
		}

		/**
		 * 黑名单
		 */
		@Override
		public int checkExceptionFile(int panLen, byte[] pan, int panSN) {
			Logger.debug("==checkExceptionFile=");
			return -1;
		}

		/**
		 * 判断IC卡脱机的累计金额 超过就强制联机
		 */
		@Override
		public int getTransactionLogAmount(int panLen, byte[] pan, int panSN) {
			Logger.debug("======getTransactionLogAmount===");
			return 0;
		}
	};

	private IEMVCallback.ApduExchangeListener apduExchangeListener = new IEMVCallback.ApduExchangeListener() {
		@Override
		public int apduExchange(byte[] sendData, int[] recvLen, byte[] recvData) {
			Logger.debug("==apduExchangeListener===");
			Logger.debug("sendData:" + ISOUtil.byte2hex(sendData));
			if(inputMode == Trans.ENTRY_MODE_NFC){//非接
				int[] status = new int[1];
				long start = SystemClock.uptimeMillis();
				while(true){
					if(SystemClock.uptimeMillis()-start>30*1000){
						break;
					}try {
						status[0] = emvContactlessCard.getStatus();
					} catch (SDKException e) {
						e.printStackTrace();
					}if(status[0]== EmvContactlessCard.STATUS_EXCHANGE_APDU){
						break;
					}try {
						Thread.sleep(6);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				int len = 0 ;
				try {
					byte[] rawData = emvContactlessCard.transmit(sendData) ;
					if(rawData!=null){
						Logger.debug("rawData = " + ISOUtil.hexString(rawData));
						len = rawData.length ;
					}if ( len <=  0 ) {
						return -1;
					}
					System.arraycopy(rawData, 0, recvData, 0, rawData.length);
				} catch (SDKException e) {
					e.printStackTrace();
				}if (len >= 0) {
					recvLen[0] = len;
					Logger.debug("Data received from card:" + ISOUtil.byte2hex(recvData,0,recvLen[0]));
					return 0;
				}
				return -1;
			}if( Trans.ENTRY_MODE_ICC == inputMode ){//IC
				int len = 0 ;
				try {
					if(contactCard == null || icCard == null){
						return -1 ;
					}else {
						byte[] rawData = icCard.transmit(contactCard , sendData) ;
						if(rawData!=null){
							Logger.debug("rawData = "+ ISOUtil.hexString(rawData));
							len = rawData.length ;
						}if ( len <= 0 ) {
							return -1;
						}
						System.arraycopy(rawData, 0, recvData, 0, rawData.length);
					}
				} catch (SDKException e) {
					e.printStackTrace();
				}if (len >= 0) {
					recvLen[0] = len;
					Logger.debug("Data received from card:"+ ISOUtil.byte2hex(recvData,0,recvLen[0]));
					return 0;
				}
				return -1;
			}
		return -1 ;
		}
	};

	private byte[] exeAPDU(byte[] apdu){
		byte[] rawData = null ;
		int recvlen = 0 ;
		try {
			rawData = icCard.transmit(contactCard , apdu) ;
			if(rawData!=null){
				Logger.debug("rawData = "+ ISOUtil.hexString(rawData));
				recvlen = rawData.length ;
			}
		} catch (SDKException e) {
			e.printStackTrace();
		}
		byte[] recv = new byte[recvlen];
		if(rawData!=null){
			System.arraycopy(rawData,0,recv,0,recvlen);
		}else {
			recv = null ;
		}
		return recv ;
	}

	private String fromApdu2Amount(String hex){
		int len = hex.length() ;
		if(len > 2 && ( (hex.contains("9F79") && hex.contains("9000"))  ||
				(hex.contains("9F78") && hex.contains("9000"))) ||
				(hex.contains("9F5D") && hex.contains("9000"))){
			int offset = 4 ;
			int l = Integer.parseInt(hex.substring(offset , offset+2));
			return hex.substring(offset+2 , offset+2+l*2);
		}
		return null ;
	}
}
