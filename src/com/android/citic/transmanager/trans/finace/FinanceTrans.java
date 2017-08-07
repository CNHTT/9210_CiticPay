package com.android.citic.transmanager.trans.finace;

import android.content.Context;
import android.os.Debug;
import android.renderscript.RSInvalidStateException;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.DateUtil;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.pinpad.PinpadManager;
import com.android.citic.transmanager.device.printer.PrintManager;
import com.android.citic.transmanager.process.EmvTransaction;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
import com.android.citic.transmanager.trans.manager.RevesalTrans;
import com.android.citic.transmanager.trans.manager.ScriptTrans;
import com.pos.device.printer.Printer;

/**
 * 金融交易类
 */
public class FinanceTrans extends Trans {

	/** 外部输入方式 */
	public static final int AMOUNT_INPUT = 0x01 ;
	public static final int MASTER_PASSWORD_INPUT = 0x02 ;
	public static final int TRANS_TRACENO_INPUT = 0x03 ;

    /** 外界输入类型 */
	public static final int INMODE_HAND = 0x01;
	public static final int INMODE_MAG = 0x02;
	public static final int INMODE_QR = 0x04;
	public static final int INMODE_IC = 0x08;
	public static final int INMODE_NFC = 0x10;


	public static final int AAC_ARQC = 1 ;
	public static final int AAC_TC = 0 ;

    /** 卡片模式 */
	protected int inputMode = 0x02;// 刷卡模式 1 手输卡号；2刷卡；5 3插IC；7 4非接触卡

    /** 交易相关参数 */
	protected boolean isPinExist = false;// 是否有密码
	protected boolean isICC = false;//是否式IC卡
	protected boolean isReversal;//是否需要冲正
	protected boolean isSaveLog;//是否需要存记录
	protected boolean isDebit;//是否借记
	protected boolean isProcPreTrans;//前置交易
	protected boolean isProcSuffix;//后置交易

    /** 金融交易类构造 */
	public FinanceTrans(Context ctx, String transEname) {
		super(ctx, transEname);
		iso8583.setHasMac(true);
		setTraceNoInc(true);//金融交易无论脱机联机流水号自增
	}

	/** 联机前某些特殊值的处理 **/
	protected void setDatas(int inputMode) {
		Logger.debug("FinanceTrans>>setDatas>>联机前某些特殊值的处理");
		this.inputMode = inputMode;
		if (isPinExist)
			CaptureCode = "12";// 固定
		EntryMode = ISOUtil.padleft(inputMode + "", 2, '0');
		Logger.debug("FinanceTrans>>setDatas>>EntryMode="+EntryMode);
		if (isPinExist){
			EntryMode += "10";
		}else{
			EntryMode += "20";
		}
		if (isPinExist || Track2 != null || Track3 != null) {
			if (cfg.isSingleKey()) {
				SecurityInfo = "20";// 输密码才有
			} else {
				if(TransEName.equals(Trans.Type.VOID)){
					SecurityInfo = "06";
				}else {
					SecurityInfo = "26";
				}
			}
			if (cfg.isTrackEncrypt()){
				SecurityInfo += "10000000000000";
			}else{
				SecurityInfo += "00000000000000";
			}
		}
		if(TransEName.equals(Trans.Type.SALE) || TransEName.equals(Trans.Type.QUICKPASS)){
			appendField60("048");
		}if(TransEName.equals(Trans.Type.ENQUIRY)){
			appendField60("501");
		}if(TransEName.equals(Trans.Type.VOID)){
			appendField60("601");
		}
	}

	/** 从内核获取卡号，有效期，2磁道，1磁道，卡序号 55域数据 */
	protected void setICCData(){
		Logger.debug("FinanceTrans>>setICCData>>从内核获取卡号，有效期，2磁道，1磁道，卡序号 55域数据");
		byte[] temp = new byte[128];
		// 卡号
		int len = TLVUtil.get_tlv_data_kernal(0x5A, temp);
		Pan = ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
		// 有效期
		len = TLVUtil.get_tlv_data_kernal(0x5F24, temp);
		if(len==3)
			ExpDate = ISOUtil.byte2hex(temp, 0, len - 1);
		// 2磁道
		len = TLVUtil.get_tlv_data_kernal(0x57, temp);
		Track2 = ISOUtil.trimf(ISOUtil.byte2hex(temp, 0, len));
		Logger.debug("get track2 from emvkernel = "+Track2);
		// 1磁道
		len = TLVUtil.get_tlv_data_kernal(0x9F1F, temp);
		Track1 = new String(temp, 0, len);
		// 卡序号
		len = TLVUtil.get_tlv_data_kernal(0x5F34, temp);
		PanSeqNo = ISOUtil.padleft(ISOUtil.byte2int(temp, 0, len) + "", 3, '0');

		//撤销交易无序打包55域数据
//		if(!para.isVoid()){
			temp = new byte[512];
			len = TLVUtil.pack_tags(wOnlineTags, temp);
			if (len > 0) {
				ICCData = new byte[len];
				System.arraycopy(temp, 0, ICCData, 0, len);
			} else{
				ICCData = null;
			}
//		}
	}

	/** 设置交易报文8583各域值，设置完后判断冲正等，即可联机 **/
	protected void setFields() {
        Logger.debug("FinanceTrans>>setFields>>设置8583报文域");
		int[] trackLen = new int[2];
		byte[] encryTrack = new byte[256];
		if (MsgID != null)
			iso8583.setField(0, MsgID);//消息类型
		if (Pan != null)
		 	iso8583.setField(2, Pan);//2*
		if (ProcCode != null)
			iso8583.setField(3, ProcCode); // 3*
		if (Amount > 0) {//金额大于0，不用输金额的另作处理
			String AmoutData = "";
			AmoutData = ISOUtil.padleft(Amount + "", 12, '0');
			iso8583.setField(4, AmoutData);// 4*
		}
		if (TraceNo != null)
			iso8583.setField(11, TraceNo);// 11*

		if (LocalTime != null)
			iso8583.setField(12, getLocalTime()); // 12* hhmmss
		if (LocalDate != null)
			iso8583.setField(13, getLocalDate()); // 13* MMDD
		if (ExpDate != null)
			iso8583.setField(14, ExpDate); // 14* YYMM
		if (SettleDate != null)
			iso8583.setField(15, SettleDate); // 15* MMDD
		if (EntryMode != null)
			iso8583.setField(22, EntryMode);// 22*
		if (PanSeqNo != null)
			iso8583.setField(23, PanSeqNo); // 23*
		if (SvrCode != null)
			iso8583.setField(25, SvrCode); // 25*
		if (CaptureCode != null)
			iso8583.setField(26, CaptureCode); // 26*
		if (AcquirerID != null)
			iso8583.setField(32, AcquirerID); // 32*

		if (Track2 != null && cfg.isTrackEncrypt()) {
            Track2 = PinpadManager.getInstance().getEac(0 , Track2);
		}
		iso8583.setField(35, Track2);// 35* 二磁道数据

		if (Track3 != null && cfg.isTrackEncrypt()) {
            Track3 = PinpadManager.getInstance().getEac(0 , Track3 );
		}
		iso8583.setField(36, Track3); // 36* 三磁道数据

		if (RRN != null)
			iso8583.setField(37, RRN); // 37*
		if (AuthCode != null)
			iso8583.setField(38, AuthCode); // 38*
		if (RspCode != null)
			iso8583.setField(39, RspCode);// 39*
		if (TermID != null)
			iso8583.setField(41, TermID); // 41*
		if (MerchID != null)
			iso8583.setField(42, MerchID);// 42*
		if (Field44 != null)
			iso8583.setField(44, Field44);// 44*
		if (Field48 != null)
			iso8583.setField(48, Field48);// 48*
		if (CurrencyCode != null)
			iso8583.setField(49, CurrencyCode);// 49*
		if (PIN != null)
			iso8583.setField(52, PIN);// 52*
		if (SecurityInfo != null)
			iso8583.setField(53, SecurityInfo);// 53*
		if (ExtAmount != null)
			iso8583.setField(54, ExtAmount);// 54*
		if (ICCData != null)
			iso8583.setField(55, ISOUtil.byte2hex(ICCData));// 55*
		if (Field60 != null)
			iso8583.setField(60, Field60);// 60*
		if (Field61 != null)
			iso8583.setField(61, Field61);// 61*
		if (Field62 != null)
			iso8583.setField(62, Field62);// 62*
		if (Field63 != null)
			iso8583.setField(63, Field63);// 63*
	}



	/** 联机处理 **/
	protected int OnlineTrans(EmvTransaction emvTrans , String cardno) {
		byte[] tag9f27 = new byte[1];
		byte[] tag9b = new byte[2];
		setFields();
        if (isProcPreTrans) {
            // 前置交易
            Logger.debug("FinanceTrans>>OnlineTrans>>前置交易");
            TransLogData revesalData = TransLog.getReversal();
            if (revesalData != null) {
                // 发起上一笔交易的冲正(进行至少三次尝试)
				transUI.handling(timeout , Tcode.Status.terminal_reversal);
				Logger.debug("FinanceTrans>>OnlineTrans>>存在冲正则需要上送冲正");
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
					return retVal ;
				}else {
					if(retVal != 0){
						//冲正失败，清除冲正，结束交易
						TransLog.clearReveral();
						return Tcode.T_reversal_fail ;
					}
				}
            }
        }

		transUI.handling(timeout , Tcode.Status.connecting_center);
        Logger.debug("FinanceTrans>>OnlineTrans>>connect");
        if (connect() == -1){
			return Tcode.T_socket_err ;
		}

		if (isReversal) {
			Logger.debug("FinanceTrans>>OnlineTrans>>存冲正");
			TransLogData Reveral = setReveralData();
			TransLog.saveReversal(Reveral); // 存冲正
		}

		Logger.debug("FinanceTrans>>OnlineTrans>>send");
		retVal = send();
		if (retVal == -1){
			return Tcode.T_send_err ;
		}

		cfg.increFlag(); // flag++

		if(retVal == 0){
			if (isTraceNoInc) {
				Logger.debug("流水号自增");
				Logger.debug("FinanceTrans>>OnlineTrans>>报文发送成功则流水就需要增加");
				cfg.incTraceNo();
			}
		}

		transUI.handling(timeout , Tcode.Status.send_over_2_recv);
		Logger.debug("FinanceTrans>>OnlineTrans>>receive");
		byte[] respData = recive();
		netWork.close();
		if (respData == null){
			return Tcode.T_receive_err ;
		}

		Logger.debug("FinanceTrans>>OnlineTrans>>unPacketISO8583");
		retVal = iso8583.unPacketISO8583(respData);// 解包
		if(retVal!=0){
			if(retVal == Tcode.T_package_mac_err){
				if(isReversal){
					//返回报文校验MAC错误，更新冲正原因A0
					Logger.debug("返回报文校验MAC错误，更新冲正原因A0");
					TransLogData newR = updateNewReversal(TransLog.getReversal() , "A0");
					TransLog.clearReveral();//清除旧冲正，保存更新冲正原因的冲正
					TransLog.saveReversal(newR);
				}
			}
			return retVal ;
		}
		Logger.debug("unPacketISO8583 39filed ="+iso8583.getfield(39));
		if (retVal == 0) {// 合法处理
			RspCode = iso8583.getfield(39);
			AuthCode = iso8583.getfield(38);
			String strICC = iso8583.getfield(55);
			if (strICC != null && (!strICC.trim().equals(""))){
				ICCData = ISOUtil.str2bcd(strICC, false);
			}else{
				ICCData = null;
			}
		}

		if ((inputMode == ENTRY_MODE_ICC||inputMode == ENTRY_MODE_NFC) && emvTrans != null && retVal == 0 && !TransEName.equals(Trans.Type.ENQUIRY) && !TransEName.equals(Type.VOID)) {
			Logger.debug("FinanceTrans>>OnlineTrans>>afterOnline");
			isICC = true;
			retVal = emvTrans.afterOnline(RspCode, AuthCode, ICCData, retVal);
			int lenOf9f27 = TLVUtil.get_tlv_data_kernal(0x9F27, tag9f27);
			Logger.debug("FinanceTrans>>OnlineTrans>>IC二次授权 = " + tag9f27);
			if (lenOf9f27 != 1) {
				// IC失败处理 如果39域是00 更新冲正文件 39域 06
			}
			int len9b = TLVUtil.get_tlv_data_kernal(0x9b, tag9b);
			Logger.debug("FinanceTrans>>OnlineTrans>>发卡行脚本结果 = " + tag9b);
			if (len9b == 2 && (tag9b[0] & 0x04) != 0) {
				// 存发卡行脚本结果
				byte[] temp = new byte[256];
				int len = TLVUtil.pack_tags(wISR_tags, temp);
				if (len > 0) {
					ICCData = new byte[len];
					System.arraycopy(temp, 0, ICCData, 0, len);
				} else{
					ICCData = null;
				}
				TransLogData scriptResult = setScriptData();
				TransLog.saveScriptResult(scriptResult);
			}
		}

		if (retVal != 0){
			return retVal ;
		}

		if (RspCode.equals("00")) {
			if ((inputMode == ENTRY_MODE_ICC||inputMode==ENTRY_MODE_NFC) && emvTrans != null) {
				if(TransEName.equals("ENQUIRY")){
					tag9f27[0] = 0x40 ;
				}
				if (tag9f27[0] != 0x40) {
                    Logger.debug("FinanceTrans>>OnlineTrans>>后台批准，被卡片拒绝，要保留冲正");
					// 后台批准，被卡片拒绝，要保留冲正
					cfg.clearFlag(); // 重置flag状态
				}

                Logger.debug("FinanceTrans>>OnlineTrans>>取55域数据");
				byte[] temp = new byte[512];
				int len = TLVUtil.pack_tags(wOnlineTags, temp);

				if (len > 0) {
					ICCData = new byte[len];
					System.arraycopy(temp, 0, ICCData, 0, len);
				} else {
					ICCData = null;
				}
			}

			Logger.debug("FinanceTrans>>OnlineTrans>>交易成功，进行脚本上送");
            TransLogData data = TransLog.getScriptResult();
            if (data != null) {
                ScriptTrans script = new ScriptTrans(context, "SENDSCRIPT");
                int ret = script.sendScriptResult(data);
                if (ret == 0)
                    TransLog.clearScriptResult();
            }

			// 存交易记录
            Logger.debug("FinanceTrans>>OnlineTrans>>存交易记录");
			if (isSaveLog) {
				TransLogData logData = setLogData(cardno);
				transLog.saveLog(logData);
				Logger.debug("存交易交易记录成功");
				cfg.increFlag();// flag++已存
			}

            Logger.debug("FinanceTrans>>OnlineTrans>>交易记录存完就可以清除冲正");
            TransLog.clearReveral();

			if(para.isNeedPrint()){
				transUI.handling(timeout , Tcode.Status.printing_recept);
				Logger.debug("FinanceTrans>>OnlineTrans>>开始打单");
				PrintManager printManager = PrintManager.getmInstance(context);
				do{
					retVal = printManager.start(transLog.getLastTransLog(), false);
				}while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
				if (retVal == Printer.PRINTER_OK) {
					cfg.clearFlag();
					retVal = 0 ;
				} else if(retVal == Printer.PRINTER_STATUS_PAPER_LACK){
					cfg.clearFlag();
					transUI.handling(timeout , Tcode.Status.printer_lack_paper);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					retVal = 0 ;
				} else {
					cfg.clearFlag();
					return retVal ;
				}
			}else {
				return 0 ;
			}
		} else {
            Logger.debug("FinanceTrans>>OnlineTrans>>交易被后台拒绝 清除冲正");
			TransLog.clearReveral();//清除冲正
			cfg.clearFlag(); // 重置flag状态
			return formatRsp(RspCode);
		}

		return retVal ;
	}

	private void setRspFields() //edit by liyo
	{
		Logger.debug("FinanceTrans>>setRspFields>>设置8583报文域");
		int[] trackLen = new int[2];
		byte[] encryTrack = new byte[256];
		if (MsgID != null)
			iso8583.setRspField(0, MsgID);//消息类型
		if (Pan != null)
			iso8583.setRspField(2, Pan);//2*
		if (ProcCode != null)
			iso8583.setRspField(3, ProcCode); // 3*
		if (Amount > 0) {//金额大于0，不用输金额的另作处理
			String AmoutData = "";
			AmoutData = ISOUtil.padleft(Amount + "", 12, '0');
			iso8583.setRspField(4, AmoutData);// 4*
		}

		if (TraceNo != null)
			iso8583.setRspField(11, TraceNo);// 11*
		if (LocalTime != null)
			iso8583.setRspField(12, LocalTime); // 12* hhmmss
		if (LocalDate != null)
			iso8583.setRspField(13, LocalDate); // 13* MMDD
		if (ExpDate != null)
			iso8583.setRspField(14, ExpDate); // 14* YYMM
		if (SettleDate != null)
			iso8583.setRspField(15, SettleDate); // 15* MMDD
		if (EntryMode != null)
			iso8583.setRspField(22, EntryMode);// 22*
		if (PanSeqNo != null)
			iso8583.setRspField(23, PanSeqNo); // 23*
		if (SvrCode != null)
			iso8583.setRspField(25, SvrCode); // 25*
		if (CaptureCode != null)
			iso8583.setRspField(26, CaptureCode); // 26*
		if (AcquirerID != null)
			iso8583.setRspField(32, AcquirerID); // 32*

		if (Track2 != null && cfg.isTrackEncrypt()) {
			Track2 = PinpadManager.getInstance().getEac(0 , Track2);
		}
		iso8583.setRspField(35, Track2);// 35* 二磁道数据

		if (Track3 != null && cfg.isTrackEncrypt()) {
			Track3 = PinpadManager.getInstance().getEac(0 , Track3 );
		}
		iso8583.setRspField(36, Track3); // 36* 三磁道数据

		if (RRN != null)
			iso8583.setRspField(37, RRN); // 37*
		if (AuthCode != null)
			iso8583.setRspField(38, AuthCode); // 38*
		if (RspCode != null)
			iso8583.setRspField(39, RspCode);// 39*
		if (TermID != null)
			iso8583.setRspField(41, TermID); // 41*
		if (MerchID != null)
			iso8583.setRspField(42, MerchID);// 42*
		if (Field44 != null)
			iso8583.setRspField(44, Field44);// 44*
		if (Field48 != null)
			iso8583.setRspField(48, Field48);// 48*
		if (CurrencyCode != null)
			iso8583.setRspField(49, CurrencyCode);// 49*
		if (PIN != null)
			iso8583.setRspField(52, PIN);// 52*
		if (SecurityInfo != null)
			iso8583.setRspField(53, SecurityInfo);// 53*
		if (ExtAmount != null)
			iso8583.setRspField(54, ExtAmount);// 54*
		if (ICCData != null)
			iso8583.setRspField(55, ISOUtil.byte2hex(ICCData));// 55*
		if (Field60 != null)
			iso8583.setRspField(60, Field60);// 60*
		if (Field61 != null)
			iso8583.setRspField(61, Field61);// 61*
		if (Field62 != null)
			iso8583.setRspField(62, Field62);// 62*
		if (Field63 != null)
			iso8583.setRspField(63, Field63);// 63*
	}

	/** 假联机处理 **/
	protected int FakeOnlineBarcodeTrans(String cardno) { //edit by liyo

		setFields();
		setRspFields();//edit by liyo

		cfg.increFlag(); // flag++
		retVal = 0;
		if(retVal == 0){
			if (isTraceNoInc) {
				Logger.debug("流水号自增");
				Logger.debug("FinanceTrans>>OnlineTrans>>报文发送成功则流水就需要增加");
				cfg.incTraceNo();
			}
		}




		if (true) { //edit by liyo

			// 存交易记录
			Logger.debug("FinanceTrans>>OnlineTrans>>存交易记录");
			isSaveLog = true; //eidt by liyo
			if (isSaveLog) {
				TransLogData logData = setLogData(cardno);
				transLog.saveLog(logData);
				Logger.debug("存交易交易记录成功");
				cfg.increFlag();// flag++已存
			}

			Logger.debug("FinanceTrans>>OnlineTrans>>交易记录存完就可以清除冲正");
			TransLog.clearReveral();

			if(para.isNeedPrint()){
				transUI.handling(timeout , Tcode.Status.printing_recept);
				Logger.debug("FinanceTrans>>OnlineTrans>>开始打单");
				PrintManager printManager = PrintManager.getmInstance(context);
				do{
					retVal = printManager.start(transLog.getLastTransLog(), false);
				}while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
				if (retVal == Printer.PRINTER_OK) {
					cfg.clearFlag();
					retVal = 0 ;
				} else if(retVal == Printer.PRINTER_STATUS_PAPER_LACK){
					cfg.clearFlag();
					transUI.handling(timeout , Tcode.Status.printer_lack_paper);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					retVal = 0 ;
				} else {
					cfg.clearFlag();
					return retVal ;
				}
			}else {
				return 0 ;
			}
		} else {
			Logger.debug("FinanceTrans>>OnlineTrans>>交易被后台拒绝 清除冲正");
			TransLog.clearReveral();//清除冲正
			cfg.clearFlag(); // 重置flag状态
			return formatRsp(RspCode);
		}

		return retVal ;
	}

	/**fake online trans**/
	protected int FakeOnlineTrans(EmvTransaction emvTrans , String cardno) { //edit by liyo
		byte[] tag9f27 = new byte[1];
		byte[] tag9b = new byte[2];
		setFields();
		setRspFields();//edit by liyo

		cfg.increFlag(); // flag++
		retVal = 0;
		if(retVal == 0){
			if (isTraceNoInc) {
				Logger.debug("流水号自增");
				Logger.debug("FinanceTrans>>OnlineTrans>>报文发送成功则流水就需要增加");
				cfg.incTraceNo();
			}
		}


/*
		Logger.debug("FinanceTrans>>OnlineTrans>>unPacketISO8583");
		retVal = iso8583.unPacketISO8583(respData);// 解包
		if(retVal!=0){
			if(retVal == Tcode.T_package_mac_err){
				if(isReversal){
					//返回报文校验MAC错误，更新冲正原因A0
					Logger.debug("返回报文校验MAC错误，更新冲正原因A0");
					TransLogData newR = updateNewReversal(TransLog.getReversal() , "A0");
					TransLog.clearReveral();//清除旧冲正，保存更新冲正原因的冲正
					TransLog.saveReversal(newR);
				}
			}
			return retVal ;
		}
		Logger.debug("unPacketISO8583 39filed ="+iso8583.getfield(39));
		if (retVal == 0) {// 合法处理
			RspCode = iso8583.getfield(39);
			AuthCode = iso8583.getfield(38);
			String strICC = iso8583.getfield(55);
			if (strICC != null && (!strICC.trim().equals(""))){
				ICCData = ISOUtil.str2bcd(strICC, false);
			}else{
				ICCData = null;
			}
		}

		if ((inputMode == ENTRY_MODE_ICC||inputMode == ENTRY_MODE_NFC) && emvTrans != null && retVal == 0 && !TransEName.equals(Trans.Type.ENQUIRY) && !TransEName.equals(Type.VOID)) {
			Logger.debug("FinanceTrans>>OnlineTrans>>afterOnline");
			isICC = true;
			retVal = emvTrans.afterOnline(RspCode, AuthCode, ICCData, retVal);
			int lenOf9f27 = TLVUtil.get_tlv_data_kernal(0x9F27, tag9f27);
			Logger.debug("FinanceTrans>>OnlineTrans>>IC二次授权 = " + tag9f27);
			if (lenOf9f27 != 1) {
				// IC失败处理 如果39域是00 更新冲正文件 39域 06
			}
			int len9b = TLVUtil.get_tlv_data_kernal(0x9b, tag9b);
			Logger.debug("FinanceTrans>>OnlineTrans>>发卡行脚本结果 = " + tag9b);
			if (len9b == 2 && (tag9b[0] & 0x04) != 0) {
				// 存发卡行脚本结果
				byte[] temp = new byte[256];
				int len = TLVUtil.pack_tags(wISR_tags, temp);
				if (len > 0) {
					ICCData = new byte[len];
					System.arraycopy(temp, 0, ICCData, 0, len);
				} else{
					ICCData = null;
				}
				TransLogData scriptResult = setScriptData();
				TransLog.saveScriptResult(scriptResult);
			}
		}

		if (retVal != 0){
			return retVal ;
		}
*/
		//if (RspCode.equals("00")) {
		if (true) { //edit by liyo
			/*
			if ((inputMode == ENTRY_MODE_ICC||inputMode==ENTRY_MODE_NFC) && emvTrans != null) {
				if(TransEName.equals("ENQUIRY")){
					tag9f27[0] = 0x40 ;
				}
				if (tag9f27[0] != 0x40) {
					Logger.debug("FinanceTrans>>OnlineTrans>>后台批准，被卡片拒绝，要保留冲正");
					// 后台批准，被卡片拒绝，要保留冲正
					cfg.clearFlag(); // 重置flag状态
				}

				Logger.debug("FinanceTrans>>OnlineTrans>>取55域数据");
				byte[] temp = new byte[512];
				int len = TLVUtil.pack_tags(wOnlineTags, temp);

				if (len > 0) {
					ICCData = new byte[len];
					System.arraycopy(temp, 0, ICCData, 0, len);
				} else {
					ICCData = null;
				}
			}

			Logger.debug("FinanceTrans>>OnlineTrans>>交易成功，进行脚本上送");
			TransLogData data = TransLog.getScriptResult();
			if (data != null) {
				ScriptTrans script = new ScriptTrans(context, "SENDSCRIPT");
				int ret = script.sendScriptResult(data);
				if (ret == 0)
					TransLog.clearScriptResult();
			}

			*/
		 	// 存交易记录
			Logger.debug("FinanceTrans>>OnlineTrans>>存交易记录");
			isSaveLog = true; //eidt by liyo
			if (isSaveLog) {
				TransLogData logData = setLogData(cardno);
				transLog.saveLog(logData);
				Logger.debug("存交易交易记录成功");
				cfg.increFlag();// flag++已存
			}

			Logger.debug("FinanceTrans>>OnlineTrans>>交易记录存完就可以清除冲正");
			TransLog.clearReveral();

			if(para.isNeedPrint()){
				transUI.handling(timeout , Tcode.Status.printing_recept);
				Logger.debug("FinanceTrans>>OnlineTrans>>开始打单");
				PrintManager printManager = PrintManager.getmInstance(context);
				do{
					retVal = printManager.start(transLog.getLastTransLog(), false);
				}while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
				if (retVal == Printer.PRINTER_OK) {
					cfg.clearFlag();
					retVal = 0 ;
				} else if(retVal == Printer.PRINTER_STATUS_PAPER_LACK){
					cfg.clearFlag();
					transUI.handling(timeout , Tcode.Status.printer_lack_paper);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					retVal = 0 ;
				} else {
					cfg.clearFlag();
					return retVal ;
				}
			}else {
				return 0 ;
			}
		} else {
			Logger.debug("FinanceTrans>>OnlineTrans>>交易被后台拒绝 清除冲正");
			TransLog.clearReveral();//清除冲正
			cfg.clearFlag(); // 重置flag状态
			return formatRsp(RspCode);
		}

		return retVal ;
	}
	/**
	 * 设置从服务器传过来的交易信息 Data From server
	 * @return TransLog
	 */


	private TransLogData setLogData(String cardno) {
		TransLogData LogData = new TransLogData();
		LogData.setCardFullNo(cardno);
		if(inputMode != ENTRY_MODE_BARCODE) //edit by liyo
			LogData.setPan(StringUtil.getSecurityNum(Pan, 6, 4));//2*
		LogData.setOprNo(cfg.getOprNo());
		LogData.setBatchNo(BatchNo);
		LogData.setEName(TransEName);
		if(ICCData != null){
			LogData.setICCData(ICCData);
		}
		Logger.debug("setVoided");
		LogData.setVoided(false);
		if(inputMode == ENTRY_MODE_NFC){
			LogData.setNFC(true);
		}else if(inputMode == ENTRY_MODE_ICC) {
			LogData.setICC(true);
		}else if(inputMode == ENTRY_MODE_BARCODE)
			LogData.setBarcode(true);
		Logger.debug("setamount");
        if(TransEName.equals(Trans.Type.ENQUIRY)){
			//eidt by liyo
            //String f54 = iso8583.getfield(54) ;
			//LogData.setAmount(Long.parseLong(f54.substring(f54.indexOf('C')+1 , f54.length())));
			LogData.setAmount(Long.parseLong("10000"));//edit by liyo
        }else{
            LogData.setAmount(Long.parseLong(iso8583.getfield(4)));
			/*
			Logger.debug("getfield(63)");
            String field63 = iso8583.getfield(63);
			Logger.debug(field63);
            String IssuerName = field63.substring(0, 3);
            String ref = field63.substring(3, field63.length());
            LogData.setRefence(ref);
            LogData.setIssuerName(IssuerName);
            */
		}

		Logger.debug("setAAC");
		LogData.setAAC(FinanceTrans.AAC_ARQC);
		Logger.debug("getfield(11)");
		LogData.setTraceNo(iso8583.getfield(11));
		Logger.debug("getfield(12)");
        LogData.setLocalTime(getLocalTime());
		Logger.debug("getfield(13)");
		Logger.debug("date "+DateUtil.getYear() + iso8583.getfield(13));
		Logger.debug("time "+iso8583.getfield(12));

        LogData.setLocalDate(DateUtil.getYear() + getLocalDate());
		Logger.debug("getfield(14)");
        LogData.setExpDate(iso8583.getfield(14));
		Logger.debug("getfield(15)");
        LogData.setSettleDate(iso8583.getfield(15));
		Logger.debug("FinanceTrans>>setLogData>>EntryMode="+EntryMode);
        LogData.setEntryMode(iso8583.getfield(22));
		Logger.debug("getfield(23)");
        LogData.setPanSeqNo(iso8583.getfield(23));
		Logger.debug("getfield(32)");
        LogData.setAcquirerID(iso8583.getfield(32));
		Logger.debug("getfield(37)");
        LogData.setRRN(iso8583.getfield(37));
		Logger.debug("getfield(38)");
        LogData.setAuthCode(iso8583.getfield(38));
		Logger.debug("getfield(39)");
        LogData.setRspCode(iso8583.getfield(39));
		Logger.debug("getfield(44))");
        LogData.setField44(iso8583.getfield(44));
		Logger.debug("getfield(49)");
        LogData.setCurrencyCode(iso8583.getfield(49));
		Logger.debug("return");
		return LogData;
	}

	protected int offlineTrans(String ec_amount , String cardNo){
		if (isSaveLog) {
			TransLogData LogData = new TransLogData();
			LogData.setCardFullNo(cardNo);
			if(para.getTransType().equals(Trans.Type.EC_ENQUIRY)){
				LogData.setAmount(Long.parseLong(ec_amount));
			}else {
				LogData.setAmount(Amount);
			}
			LogData.setPan(StringUtil.getSecurityNum(Pan, 6, 4));
			LogData.setOprNo(cfg.getOprNo());
			LogData.setEName(TransEName);
			LogData.setEntryMode(ISOUtil.padleft(inputMode + "", 2, '0')+"10");
			LogData.setTraceNo(cfg.getTraceNo());
			LogData.setBatchNo(cfg.getBatchNo());
			LogData.setLocalDate(DateUtil.getYear() + getLocalDate());
			LogData.setLocalTime(getLocalTime());
			LogData.setAAC(FinanceTrans.AAC_TC);
			if(inputMode == ENTRY_MODE_NFC){
				LogData.setNFC(true);
			}if(inputMode == ENTRY_MODE_ICC) {
				LogData.setICC(true);
			}
			if(ICCData != null){
				LogData.setICCData(ICCData);
			}
			transLog.saveLog(LogData);
			cfg.increFlag();// flag++已存
			Logger.debug("流水号自增");
			//if(isTraceNoInc){
				cfg.incTraceNo();//脱机消费成功，流水号自增
			//}
			Logger.debug("save log logSize="+ TransLog.getInstance().getSize());
		}
		if(para.isNeedPrint()){
			Logger.debug("FinanceTrans>>offlineTrans>>开始打单");
			transUI.handling(timeout , Tcode.Status.printing_recept);
			PrintManager print = PrintManager.getmInstance(context);
			do{
				retVal = print.start(transLog.getLastTransLog(), false);
			}while (retVal == Printer.PRINTER_STATUS_PAPER_LACK);
			if (retVal == Printer.PRINTER_OK) {
				cfg.clearFlag();
				retVal = 0 ;
			} else {
				cfg.clearFlag();
			}
			return retVal ;
		}else {
			return 0 ;
		}
	}

    /** 设置发卡行脚本数据 */
	private TransLogData setScriptData() {
		TransLogData LogData = new TransLogData();
		LogData.setPan(StringUtil.getSecurityNum(Pan, 6, 4));//2*
		LogData.setICCData(ICCData);
		LogData.setBatchNo(BatchNo);
        LogData.setAmount(Long.parseLong(iso8583.getfield(4)));
        LogData.setTraceNo(iso8583.getfield(11));
        LogData.setLocalTime(iso8583.getfield(12));
        LogData.setLocalDate(iso8583.getfield(13));
        LogData.setEntryMode(iso8583.getfield(22));
        LogData.setPanSeqNo(iso8583.getfield(23));
        LogData.setAcquirerID(iso8583.getfield(32));
        LogData.setRRN(iso8583.getfield(37));
        LogData.setAuthCode(iso8583.getfield(38));
        LogData.setCurrencyCode(iso8583.getfield(49));
		return LogData ;
	}

    /** 设置冲正数据 */
	private TransLogData setReveralData() {
		TransLogData LogData = new TransLogData();
		LogData.setPan(Pan);
		LogData.setProcCode(ProcCode);
		LogData.setAmount(Amount);
		LogData.setTraceNo(TraceNo);
		LogData.setExpDate(ExpDate);
		LogData.setEntryMode(EntryMode);
		LogData.setPanSeqNo(PanSeqNo);
		// 25域 38域
		LogData.setSvrCode(SvrCode);// 25
		LogData.setAuthCode(AuthCode);// 38域
		LogData.setRspCode("98");
		LogData.setCurrencyCode(CurrencyCode);
        byte[] temp = new byte[156];
        if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC) {
            int len = TLVUtil.pack_tags(reversal_tag, temp);
            if (len > 0) {
                ICCData = new byte[len];
                System.arraycopy(temp, len, ICCData, 0, len);
                LogData.setICCData(ICCData);
            } else
                ICCData = null;
        }
		LogData.setField60(Field60);
		return LogData;
	}

	private TransLogData updateNewReversal(TransLogData data , String f39){
		TransLogData tld = new TransLogData() ;
		tld.setPan(data.getPan());
		tld.setProcCode(data.getProcCode());
		tld.setAmount(data.getAmount());
		tld.setTraceNo(data.getTraceNo());
		tld.setExpDate(data.getExpDate());
		tld.setEntryMode(data.getEntryMode());
		tld.setPanSeqNo(data.getPanSeqNo());
		// 25域 38域
		tld.setSvrCode(data.getSvrCode());// 25
		tld.setAuthCode(data.getAuthCode());// 38域
		tld.setRspCode(f39);
		tld.setCurrencyCode(data.getCurrencyCode());
		if(data.getICCData() != null){
			tld.setICCData(data.getICCData());
		}
		tld.setField60(data.getField60());
		return tld ;
	}

	private int formatRsp(String rsp){
		if(rsp.equals("5A")){
			return 200 ;
		}else if(rsp.equals("5B")){
			return 201 ;
		}else if(rsp.equals("6A")){
			return 202 ;
		}else if(rsp.equals("A0")){
			return 203 ;
		}else if(rsp.equals("D1")){
			return 204 ;
		}else if(rsp.equals("D2")){
			return 205 ;
		}else if(rsp.equals("D3")){
			return 206 ;
		}else if(rsp.equals("D4")){
			return 207 ;
		}else if(rsp.equals("N6")){
			return 208 ;
		}else if(rsp.equals("N7")){
			return 209 ;
		}else {
			return Integer.parseInt(rsp) ;
		}
	}

//	/**
//	 * 从内核取卡号和磁道信息
//	 * @return 0成功 -1失败
//	 */
//	protected int get_icc_data() {
//		int len;
//		byte[] temp = new byte[256];
//		len = TLVUtil.get_tlv_data_kernal(0x5A, temp); // 卡号
//		if (len == 0)
//			return -1;
//		Pan = ISOUtil.byte2hex(temp, 0, len);
//		len = TLVUtil.get_tlv_data_kernal(0x5F24, temp); // 卡有效期
//		if (len > 0)
//			ExpDate = ISOUtil.byte2hex(temp, 0, len);
//		len = TLVUtil.get_tlv_data_kernal(0x5F34, temp);// 卡序号
//		if (len > 0)
//			PanSeqNo = ISOUtil.byte2int(temp, 0, len) + "";
//		len = TLVUtil.get_tlv_data_kernal(0x57, temp); // 2磁道
//		if (len > 0)
//			Track2 = ISOUtil.byte2hex(temp, 0, len).replace('F', ' ').trim();
//		len = TLVUtil.get_tlv_data_kernal(0x9F1F, temp); // 1磁道
//		if (len > 0) {
//			Track1 = new String(temp, 0, len);
//		}
//		return 0;
//	}
}
