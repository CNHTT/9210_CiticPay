package com.android.citic.transmanager.trans.finace.ec_query;

import android.content.Context;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.transmanager.device.card.CardInfo;
import com.android.citic.transmanager.device.card.CardManager;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.process.EmvTransaction;
import com.android.citic.transmanager.process.QpbocTransaction;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.finace.FinanceTrans;
import com.android.citic.transmanager.trans.presenter.TransPresenter;


public class EC_EnquiryTrans extends FinanceTrans implements TransPresenter{

	public EC_EnquiryTrans(Context ctx, String transEname , TransInputPara p) {
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
		Logger.debug("EC_EnquiryTrans>>getCarduse.....");
		int inputmod = 0;
		//if(cfg.getContactlessSwitch())
		//	inputmod = INMODE_IC|INMODE_NFC;
		//else
			inputmod = INMODE_IC;
		CardInfo cardInfo = transUI.getCardUse(timeout , inputmod);
		afterGetCardUse(cardInfo);

		Logger.debug("EC_EnquiryTrans>>finish");
		return;
	}

	private void afterGetCardUse(CardInfo info){
		setTraceNoInc(false);
		if(info.isResultFalg()){
			int type = info.getCardType() ;
			switch (type){
				case CardManager.TYPE_MAG :inputMode = ENTRY_MODE_MAG ;break;
				case CardManager.TYPE_ICC :inputMode = ENTRY_MODE_ICC ;break;
				case CardManager.TYPE_NFC :inputMode = ENTRY_MODE_NFC ;break;
			}
			Logger.debug("EC_EnquiryTrans>>inputMode = "+inputMode);
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
			Logger.debug("EC_EnquiryTrans>>QpbocTransaction=" + retVal);
			if (retVal == 0) {
				if (0 == retVal) {
					String ec_amount = qpboc.getEC_AMOUNT();
					if(ec_amount == null){
						transUI.showError(Tcode.T_read_ec_amount_err);
					}else {
						retVal = offlineTrans(ec_amount , qpboc.getCardNO());
						if(0 == retVal){
							transUI.trannSuccess(Tcode.Status.ecenquiry_succ);
						}else {
							transUI.showError(retVal);
						}
					}
				} else {
					transUI.showError(retVal);
				}
			} else {
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
			Logger.debug("EC_EnquiryTrans>>EmvTransaction=" + retVal);
			if (retVal == 0 || retVal == 1) {
				String ec_amount = emv.getECAmount();
				if(ec_amount == null){
					transUI.showError(Tcode.T_read_ec_amount_err);
				}else {
					Pan = emv.getCardNo() ;
					retVal = offlineTrans(ec_amount , emv.getCardNo());
					if(0 == retVal){
						transUI.trannSuccess(Tcode.Status.ecenquiry_succ);
					}else {
						transUI.showError(retVal);
					}
				}
			} else {
				transUI.showError(retVal);
			}
		}else {
			transUI.showError(Tcode.T_terminal_no_aid);
		}
	}

//	/** 交易流程的线程 **/
//	class TransThread extends Thread {
//		private Handler handle;
//		private int emvRet;
//
//		public TransThread(Handler handle) {
//			this.handle = handle;
//		}
//
//		@Override
//		public revocation run() {
//			super.run();
//			Message message = handle.obtainMessage() ;
//			message.what = Trans.TRANS_OVER ;
//			Amount = Long.parseLong("100");//设置金额
//			String result = TransUtil.getCard(handle, R.string.sale_use_card_tips, INMODE_MAG|INMODE_IC|INMODE_NFC , true);
//			if (StringUtil.isNullWithTrim(result)) {
//				clearPan();
//				message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_read_card_timeout)};
//				handle.sendMessage(message);
//				return;
//			}if(result.contains(",Key Err")){
//				clearPan();
//				message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_read_terminal_aidcapk_fail)};
//				handle.sendMessage(message);
//				return;
//			}
//			String[] cardInfo = result.split(",");
//			for(int i = 0 ; i < cardInfo.length ; i++){
//				LogManager.t("cardinfo after getting_card = "+cardInfo[i]);
//			}
//			// 输入方式 ,卡号,1磁道信息 , 2磁道信息 , 3磁道信息
//			inputMode = Integer.parseInt(cardInfo[0]);
//			LogManager.t("inputMode after getting_card = "+inputMode);
//			if (inputMode == ENTRY_MODE_MAG) {
//				cardNo = cardInfo[1];// 磁卡卡号
//				Pan = cardNo;
//				Track2 = cardInfo[3];
//				Track3 = cardInfo[4];
//				if (StringUtil.isNullWithTrim(Track3))
//					Track3 = null;
//			} else if (inputMode == ENTRY_MODE_ICC) {
//				LogManager.t("icc emv trans starting");
//				if (cardInfo.length == 2 && cardInfo[1].equals("Key Err")) {
//					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_nokey_err)};
//					handle.sendMessage(message);
//					return;
//				}if (cardInfo.length == 1) {
//					// 如果上电失败 通过iccRet执行降级操作
//					emvRet = 2;
//				} else {
//					countLatch = new CountDownLatch(1);
//					emv = new EmvTransaction(context, false, Amount, inputMode, 0, handle  , TransEName);
//					emvRet = emv.EMVTramsProcess();
//				}if (emvRet == 0 || emvRet == 1) {
//					// 联机处理,已在setdatas中处理55域数据
//					cardNo = emv.getCardNo() ;
//				} else if (emvRet == 2) {
//					handle.sendEmptyMessage(Trans.TRANS_IC_DOWNGRADE);
//					RunMag(handle);
//				}else if(emvRet == Trans.EMV_OFFLINE_DATAAUTH){
//					clearPan();
//					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_offlineDataAuth_err)};
//					handle.sendMessage(message);
//					return;
//				}else if(emvRet == Trans.EMV_CARDHOLDER_AUTH_FAIL){
//					clearPan();
//					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_cardHolderVerify_err)};
//					handle.sendMessage(message);
//					return;
//				}else if(emvRet == Trans.EMV_TERMINAL_ANAYSIS_FAIL){
//					clearPan();
//					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalRiskMana_err)};
//					handle.sendMessage(message);
//					return;
//				}else if(emvRet == Trans.EMV_CONFIGMCARD_CANCEL_OR_TIMEOUT){
//					clearPan();
//					handle.sendEmptyMessage(ERR_QUIT);
//					return;
//				}else {
//					clearPan();
//					message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+emvRet+"]"};
//					handle.sendMessage(message);
//					return;
//				}
//			}else if(inputMode == Trans.ENTRY_MODE_NFC){
//				LogManager.t("nfc emv trans starting");
//				if(ConfigManager.getInstance().isQPBOC()){
//					qpboc = new QpbocTransaction(context) ;
//					int qpboc_ret = qpboc.qpbocProcess() ;
//					if( qpboc_ret == 0){
//						byte[] temp = new byte[256] ;
//						int len = TLVUtil.get_tlv_data_kernal(0x57 , temp);
//						LogManager.t("temp = "+ ISOUtil.hexString(temp)) ;
//						if(len < 7)	//磁道信息没读到
//							return;
//						String CN = ISOUtil.hexString(temp) ;
//						LogManager.t("CN = "+CN.split("D")[0]);
//						Pan = CN.split("D")[0];
//						LogManager.t("Pan = "+Pan);
//						IEMVHandler emvHandler = EMVHandler.getInstance();
//						temp =  ISOUtil.str2bcd(Pan , false);
//						if(Pan.length()%2 != 0)
//							temp[Pan.length() / 2] |= 0x0f;
//						emvHandler.setDataElement(new byte[]{0x5A} ,temp );
//						cardNo = Pan ;
//					}else {
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+qpboc_ret+"]"} ;
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}
//				}else {
//					emv = new EmvTransaction(context, false, Amount, inputMode, 0, handle , TransEName );
//					emvRet = emv.EMVTramsProcess();
//					if (emvRet == 0 || emvRet == 1) {
//						// 联机处理,已在setdatas中处理55域数据
//						cardNo = emv.getCardNo() ;
//					}else if(emvRet == IEMVHandler.EMV_ERRNO_CARD_BLOCKED || emvRet == IEMVHandler.EMV_ERRNO_APP_BLOCKED){
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_selectApp_err)};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}else if(emvRet == IEMVHandler.EMV_ERRNO_NODATA){
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_readAppData_err)};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}else if(emvRet == Trans.EMV_OFFLINE_DATAAUTH){
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_offlineDataAuth_err)};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}else if(emvRet == Trans.EMV_CARDHOLDER_AUTH_FAIL){
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_cardHolderVerify_err)};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}else if(emvRet == Trans.EMV_TERMINAL_ANAYSIS_FAIL){
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalRiskMana_err)};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}else if(emvRet == Trans.EMV_TERMINAL_ACTION_ANAYSIS_FAIL){
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalAction_err)};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}else if(emvRet == Trans.EMV_CONFIGMCARD_CANCEL_OR_TIMEOUT){
//						clearPan();
//						handle.sendEmptyMessage(ERR_QUIT);
//						return;
//					}else {
//						clearPan();
//						message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+emvRet+"]"};
//						noticeRemoveNFCcard(handle , message);
//						return;
//					}
//				}
//			}
//
//			if(ENTRY_MODE_NFC == inputMode || ENTRY_MODE_MAG == inputMode){
//				result = TransUtil.getPass(handle, R.string.sale_enter_pass, cardNo);
//				if(result.contains("errcode")){
//					clearPan();
//					message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_get_pinblock_err)+":\n"+result} ;
//					noticeRemoveNFCcard(handle , message);
//					return;
//				}if (result.equals(""+ PedRetCode.ENTER_CANCEL)) {
//					clearPan();
//					qpbocPrecessCancelbyUsers(handle);
//					return;
//				}if (result.equals("")) {
//					isPinExist = false;
//				} else
//					isPinExist = true;
//				if (isPinExist)
//					PIN = result;
//			}
//			if(ENTRY_MODE_ICC == inputMode || ENTRY_MODE_NFC == inputMode)
//				setICCData();
//			handle.sendEmptyMessage(Trans.WaitOnLineTrans);
//			setDatas(inputMode);
//			int resultCode ;
//			if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC)
//				resultCode = OnlineTrans(handle, emv , cardNo);
//			else
//				resultCode = OnlineTrans(handle, null , cardNo);
//
//			if (iccard != null) {
//				iccard.stopSearchCard();
//				iccard.release();
//			}
//
//			clearPan();
//			if(inputMode == ENTRY_MODE_NFC){
//				PiccReader nfcCard = PiccReader.getInstance() ;
//				handle.sendEmptyMessage(Trans.NFC_REMOVE);
//			}
//			int rid = getResurceId(resultCode) ;
//			String info = context.getResources().getString(rid);
//			if(resultCode == 0){
//				info = context.getResources().getString(rid)+"\n\n"+
//						context.getResources().getString(R.string.enquiry_amount)+
//						"\n\nRMB:"+ TransUtil.getStrAmount(TransLog.getInstance().getLastTransLog().getAmount()) ;
//			}
//			message.obj = new String[]{resultCode==0?SUCC:FAIL , info} ;
//			handle.sendMessage(message);
//			return;
//		}
//	}

//	private revocation qpbocPrecessCancelbyUsers(Handler mha){
//		PiccReader nfcCard = PiccReader.getInstance() ;
//		mha.sendEmptyMessage(Trans.NFC_REMOVE);
//		mha.sendEmptyMessage(ERR_QUIT);
//	}

//	private revocation noticeRemoveNFCcard(Handler h , Message m){
//		PiccReader nfcCard = PiccReader.getInstance() ;
//		h.sendEmptyMessage(Trans.NFC_REMOVE);
//		h.sendMessage(m) ;
//	}

//	// IC卡交易失败执行降级操作
//	private revocation RunMag(Handler handle) {
//		String result = TransUtil.getCard(handle, R.string.sale_prompt_swipe_card, INMODE_MAG, false);
//		if (StringUtil.isNullWithTrim(result)) {
//			clearPan();
//			Message message = handle.obtainMessage() ;
//			message.what = TRANS_OVER ;
//			message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_swipe_card_timeout)};
//			handle.sendMessage(message);
//			return ;
//		}
//		String[] cardInfo = result.split(",");
//		inputMode = Integer.parseInt(cardInfo[0]);
//		if (inputMode == ENTRY_MODE_MAG) {
//			cardNo = cardInfo[1];// 磁卡卡号
//			Pan = cardNo;
//			Track2 = cardInfo[3];
//			Track3 = cardInfo[4];
//			if (StringUtil.isNullWithTrim(Track3))
//				Track3 = null;
//
//			// 提示确认卡号的的窗口
//			result = TransUtil.getNoticeResult(handle, cardNo);
//			if (result == null) {
//				clearPan();
//				handle.sendEmptyMessage(ERR_QUIT);
//				return ;
//			}
//
//			//获取密码
//			result = TransUtil.getPass(handle, R.string.sale_enter_pass, cardNo);
//			if (result == null) {
//				clearPan();
//				handle.sendEmptyMessage(ERR_QUIT);
//				return ;
//			}
//			if (result.equals(""))
//				isPinExist = false;
//			else
//				isPinExist = true;
//			if (isPinExist)
//				PIN = result;
//		}
//	}

//	private revocation printReceiptOffline(Handler handler){
//		int ret ;
//		handler.sendEmptyMessage(Trans.OnLineTransEnd);
//		TransLog.clearReveral(); // 交易记录存完就可以清除冲正了
//		handler.sendEmptyMessage(Trans.TRANS_OVER_PRINTING);
//		PrintTrans print = new PrintTrans(context, handler);
//		do {
//			ret = print.printTransLog(transLog.getLastTransLog(), false);
//		} while (ret == Printer.PRINTER_STATUS_PAPER_LACK);
//		if (ret == Printer.PRINTER_OK) {
//			configManager.clearFlag();
//			ret = 0;
//		} else if (ret == ONLINE_RET_PRINT_CANCLE) {
//			configManager.clearFlag();
//			ret = ONLINE_RET_PRINT_CANCLE;
//		} else
//			ret = ONLINE_RET_PRINTING_ERR;
//
//		int rid = getResurceId(ret) ;
//		Message message = handler.obtainMessage() ;
//		message.what = TRANS_OVER ;
//		String info = context.getResources().getString(rid) ;
//		if (ret == 0) {
//			info =  context.getResources().getString(rid)+
//					"\n\n"+context.getResources().getString(R.string.sale_over_trans_amount)+
//					"\n\nRMB:"+ TransUtil.getStrAmount(Amount);
//		}
//		message.obj = new String[]{ret == 0?SUCC:FAIL , info} ;
//		handler.sendMessage(message);
//	}

//	private int getResurceId(int resultCode){
//		int rid ;
//		switch (resultCode){
//			case 0:rid = R.string.enquiry_success ;break;
//			case ONLINE_INTERRUPT:rid = R.string.sale_interrupt ;break;
//			case ONLINE_ICC_RET_ERR:rid = R.string.sale_over_icc_err ;break;
//			case ONLINE_RET_MAC_ERR:rid = R.string.sale_mac_err ;break;
//			case ONLINE_RET_SOCKET_ERR:rid = R.string.socket_fail ;break;
//			case ONLINE_RET_SEND_ERR:rid = R.string.socket_send_msg_err ;break;
//			case ONLINE_RET_RECIVE_ERR_PACKET:rid = R.string.socket_err_packet ;break;
//			case ONLINE_RET_RECIVE_EMPTY:rid = R.string.socket_receive_empty_err ;break;
//			case ONLINE_RET_RECIVE_REFUSE:rid = R.string.sale_trans_refuse ;break;
//			case ONLINE_RET_PRINTING_ERR:rid = R.string.sale_print_err ;break;
//			case ONLINE_RET_PRINT_CANCLE:rid = R.string.sale_print_cancel ;break;
//			case EMV_ERRNO_DECLINE:rid = R.string.sale_trans_refuse ;break;
//			case EMV_ERRNO_CARD_BLOCKED:rid = R.string.sale_trans_refuse ;break;
//			case EMV_ERRNO_APP_BLOCKED:rid = R.string.sale_trans_refuse ;break;
//			case EMV_ERRNO_PIN_BLOCKED:rid = R.string.sale_trans_refuse ;break;
//			case ONLINE_RET_EMV_REFUSE:rid = R.string.sale_emv_refuse ;break;
//			default:rid = R.string.sale_other_err ;break;
//		}
//		return rid ;
//	}
}
