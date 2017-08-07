//package com.android.citic.transmanager.trans.finace.quickpass;
//
//import android.content.Context;
//import android.os.Bundle;
//
//import com.android.citic.appmanager.log.Logger;
//import com.android.citic.lib.utils.ISOUtil;
//import com.android.citic.lib.utils.StringUtil;
//import com.android.citic.transmanager.device.card.CardInfo;
//import com.android.citic.transmanager.device.card.CardManager;
//import com.android.citic.transmanager.device.input.InputInfo;
//import com.android.citic.transmanager.device.pinpad.PinInfo;
//import com.android.citic.transmanager.global.GlobalCfg;
//import com.android.citic.transmanager.process.EmvInputPara;
//import com.android.citic.transmanager.process.EmvTransaction;
//import com.android.citic.transmanager.process.QpbocTransaction;
//import com.android.citic.transmanager.trans.Tcode;
//import com.android.citic.transmanager.trans.TransStatus;
//import com.android.citic.transmanager.trans.finace.FinanceTrans;
//import com.android.citic.transmanager.trans.helper.utils.TLVUtil;
//import com.android.citic.transmanager.trans.presenter.TransPresenter;
//import com.android.citic.transmanager.trans.presenter.TransUI;
//import com.pos.device.emv.EMVHandler;
//import com.pos.device.emv.IEMVHandler;
//
///**
// * 消费交易，继承自金融类交易
// */
//
//public class QpassTrans extends FinanceTrans implements TransPresenter {
//
//	public QpassTrans(Context ctx, String transEname , TransUI tui) {
//		super(ctx, transEname);
//		isReversal = false;
//		isSaveLog = true;
//		isDebit = true;
//		isProcPreTrans = true;
////		isProcSuffix = true;
//		transUI = tui ;
//	}
//
//	@Override
//	public void start() {
//		//取金额
//		int timeout = 60 * 1000 ;
//		InputInfo inputInfo = transUI.getOutsideInput(timeout , AMOUNT_INPUT);
//		if(inputInfo.isResultFlag()){
//			Amount = Long.parseLong(inputInfo.getResult());
//			Logger.debug("QpassTrans>>Amount="+Amount);
//
//			//取卡
//			CardInfo cardInfo = transUI.getCardUse(timeout , INMODE_IC|INMODE_NFC);
//			afterGetCardUse(cardInfo);
//		}else {
//			transUI.showError(inputInfo.getErrno());
//		}
//
//		Logger.debug("QpassTrans>>finish");
//		return;
//	}
//
//	private void afterGetCardUse(CardInfo info){
//		if(info.isResultFalg()){
//			int type = info.getCardType() ;
//			switch (type){
//				case CardManager.TYPE_MAG :inputMode = ENTRY_MODE_MAG ;break;
//				case CardManager.TYPE_ICC :inputMode = ENTRY_MODE_ICC ;break;
//				case CardManager.TYPE_NFC :inputMode = ENTRY_MODE_NFC ;break;
//			}
//			if(inputMode == ENTRY_MODE_ICC){
//				isICC();
//			}
//			if(inputMode == ENTRY_MODE_NFC){
//				isNFC();
//			}
//		}else {
//			transUI.showError(info.getErrno());
//		}
//	}
//
//	private void isMag(String[] tracks){
//		Logger.debug("SaleTrans>>T1="+tracks[0]);
//		Logger.debug("SaleTrans>>T2="+tracks[1]);
//		Logger.debug("SaleTrans>>T3="+tracks[2]);
//		String data1 = null;
//		String data2 = null;
//		String data3 = null;
//		int msgLen = 0;
//		if (tracks[0].length() > 0 && tracks[0].length() <= 80) {
//			data1 = new String(tracks[0]);
//		}
//		if (tracks[1].length() >= 13 && tracks[1].length() <= 37) {
//			data2 = new String(tracks[1]);
//			String judge = data2.substring(0, data2.indexOf('='));
//			if(judge.length() < 13 || judge.length() > 19){
//				//读卡错误
//			}else {
//				if (data2.indexOf('=') != -1)
//					msgLen++;
//			}
//		}
//		if (tracks[2].length() >= 15 && tracks[2].length() <= 107) {
//			data3 = new String(tracks[2]);
//		}
//		if (msgLen == 0) {
//			//读卡错误
//		}
//		if (cfg.isCheckICC()) {
//			int splitIndex = data2.indexOf("=");
//			if (data2.length() - splitIndex >= 5) {
//				char iccChar = data2.charAt(splitIndex + 5);
//				if (iccChar == '2' || iccChar == '6') {
//					//该卡IC卡,请刷卡
//				}
//			} else {
//				//读卡错误
//			}
//		}
//		String cardNo = data2.substring(0, data2.indexOf('='));
//		retVal = transUI.showCardConfirm(timeout , cardNo);
//		if(retVal == 0){
//			Pan = cardNo;
//			Track2 = data2;
//			Track3 = data3;
//			PinInfo info = transUI.getPinpadOnlinePin(timeout , cardNo);
//			if(info.isResultFlag()){
//				if(info.isNoPin()){
//					isPinExist = false;
//				}else {
//					if(null == info.getPinblock()){
//						isPinExist = false;
//					}else {
//						isPinExist = true;
//					}
//					PIN = ISOUtil.hexString(info.getPinblock());
//				}
//				prepareOnline(cardNo);
//			}else {
//				transUI.showError(info.getErrno());
//			}
//		}else {
//			transUI.showError(Tcode.T_user_cancel_operation);
//		}
//	}
//
//	private void isICC(){
//		if(GlobalCfg.isEmvParamLoaded()){
//			transUI.handling(timeout , TransStatus.emv_handling);
//			Bundle bundle = new Bundle();
//			bundle.putBoolean(EmvInputPara.IS_NEED_AMOUNT , true);
//			bundle.putLong(EmvInputPara.FIRST_AMOUNT , Amount);
//			bundle.putInt(EmvInputPara.CARD_INPUT_MODE , inputMode);
//			bundle.putLong(EmvInputPara.OTHER_AMOUNT , 0);
//			bundle.putString(EmvInputPara.TRANS_EN_NAME , TransEName);
//			bundle.putInt(EmvInputPara.KERNEL_TYPE , EMVHandler.KERNEL_TYPE_EMV);
//			emv = new EmvTransaction(bundle, null, transUI);
//			retVal = emv.start() ;
//			Logger.debug("EMVTramsProcess return = "+retVal);
//			boolean isQuickPassOnline = false ;
//			if(0 == retVal || 1 == retVal){
//				String ec = emv.getECAmount() ;
//				if(Long.parseLong(ec) < Amount){
//					isQuickPassOnline = true ;
//				}else {
//					isQuickPassOnline = false ;
//				}
//			}else {
//				transUI.showError(retVal);
//			}
//		}else {
//			transUI.showError(Tcode.T_terminal_no_aid);
//		}
//	}
//
//	private void isNFC(){
//		if(GlobalCfg.isEmvParamLoaded()){
//			transUI.handling(timeout , TransStatus.emv_handling);
//			Bundle bundle = new Bundle();
//			bundle.putBoolean(EmvInputPara.IS_NEED_AMOUNT , true);
//			bundle.putLong(EmvInputPara.FIRST_AMOUNT , Amount);
//			bundle.putInt(EmvInputPara.CARD_INPUT_MODE , inputMode);
//			bundle.putLong(EmvInputPara.OTHER_AMOUNT , 0);
//			bundle.putString(EmvInputPara.TRANS_EN_NAME , TransEName);
//			bundle.putInt(EmvInputPara.KERNEL_TYPE , EMVHandler.KERNEL_TYPE_PBOC);
//			qpboc = new QpbocTransaction(bundle , null , transUI);
//			retVal = qpboc.start();
//			Logger.debug("QpbocTransaction return = "+retVal);
//			if(retVal == 0){
//				String cn = qpboc.getCardNO();
//				if(cn == null){
//					transUI.showError(Tcode.T_qpboc_read_err);
//				}else {
//					Pan = cn ;
//					retVal = transUI.showCardConfirm(timeout , cn );
//					if(0 == retVal){
////						long ec = Long.parseLong(qpboc.readECBlance());
////						if(ec < Amount){
////
////						}else {
////
////						}
//						IEMVHandler emvHandler = EMVHandler.getInstance();
//						byte[] temp =  ISOUtil.str2bcd(Pan , false);
//						if(Pan.length()%2 != 0)
//							temp[Pan.length() / 2] |= 0x0f;
//						emvHandler.setDataElement(new byte[]{0x5A} ,temp );
//						Logger.debug("temp = "+ISOUtil.hexString(temp , 0 , temp.length));
//						byte[] res = new byte[32] ;
//						TLVUtil.get_tlv_data_kernal(0x9F10 , res);
//						Logger.debug("9f10 = "+ISOUtil.hexString(res));
//						if( (res[4]&0x30) == (byte)0x20 ){
//							Logger.debug("QPBOC ARQC");
//							setICCData();
//							prepareOnline(qpboc.getCardNO());
//						}else {
//							transUI.showError(Tcode.T_sale_tc_err);
//						}
//					}else {
//						transUI.showError(retVal);
//					}
//				}
//			}else {
//				transUI.showError(retVal);
//			}
//		}else {
//			transUI.showError(Tcode.T_terminal_no_aid);
//		}
//	}
//
//	private void prepareOnline(String fullcard){
//		//设置完55域数据即可请求联机
//		transUI.handling(timeout , TransStatus.online_transing);
//		setDatas(inputMode);
//		//联机处理
//		if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC){
//			retVal = OnlineTrans(emv , fullcard);
//		}else{
//			retVal = OnlineTrans(null , fullcard);
//		}
//		Logger.debug("SaleTrans>>OnlineTrans="+retVal);
//		clearPan();
//		handleOnlineValue(retVal);
//	}
//
//	private void handleOnlineValue(int resultCode){
//		if(resultCode == 0){
//			transUI.trannSuccess(TransStatus.trans_success);
//		}
////		if(array[1].equals("0")){
////			if(array[0].equals("0")){
////				transUI.trannSuccess(TransStatus.sale_success);
////			}else {
////				switch (Integer.parseInt(array[0])){
////					case ONLINE_RET_SOCKET_ERR:
////						transUI.showError(TransStatus.socket_connect_err , 0);
////						break;
////					case ONLINE_RET_SEND_ERR:
////						transUI.showError(TransStatus.socket_send_data_err , 0);
////						break;
////					case ONLINE_RET_RECIVE_EMPTY:
////						transUI.showError(TransStatus.socket_recv_data_err , 0);
////						break;
////					case ONLINE_RET_EMV_REFUSE :
////						transUI.showError(TransStatus.emv_process_refuse , 0);
////						break;
////					case ONLINE_RET_PRINT_CANCLE:
////						//提示缺纸后取消打印
////						break;
////					case ONLINE_RET_PRINTING_ERR:
////						transUI.showError(TransStatus.printint_exception , 0);
////						break;
////				}
////			}
////		}else {
////			transUI.showError(array[1] , 1);
////		}
//	}
//
//	//	public revocation startTrans(Handler handle , String amount) {
////		new TransThread(handle , amount).start();
////	}
////
////	/** 交易流程的线程 **/
////	class TransThread extends Thread {
////		private Handler handle;
////		private String amount;
////		private int emvRet;
////
////		public TransThread(Handler handle , String amount) {
////			this.handle = handle;
////			this.amount = amount;
////		}
////
////		@Override
////		public revocation run() {
////			super.run();
////			Message message = handle.obtainMessage() ;
////			message.what = Trans.TRANS_OVER ;
//////			String result = TransUtil.getAmout(handle, R.string.sale_amount_input_tips);
//////			if (StringUtil.isNullWithTrim(amount)) {
//////				senderrMessage(handle, R.string.sale_amount_input_timeout);
//////				return;
//////			}
////			Amount = Long.parseLong(amount);//设置金额
////			String result = TransUtil.getCard(handle, R.string.sale_use_card_tips,
////					INMODE_MAG|INMODE_IC|INMODE_NFC , true);
////			if (StringUtil.isNullWithTrim(result)) {
////				clearPan();
////				message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_read_card_timeout)};
////				handle.sendMessage(message);
////				return;
////			}if(result.contains(",Key Err")){
////				clearPan();
////				message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_read_terminal_aidcapk_fail)};
////				handle.sendMessage(message);
////				return;
////			}
////			String[] cardInfo = result.split(",");
////			for(int i = 0 ; i < cardInfo.length ; i++){
////				Logger.debug("cardinfo after getting_card = "+cardInfo[i]);
////			}
////			// 输入方式 ,卡号,1磁道信息 , 2磁道信息 , 3磁道信息
////			inputMode = Integer.parseInt(cardInfo[0]);
////			Logger.debug("inputMode after getting_card = "+inputMode);
////			if (inputMode == ENTRY_MODE_MAG) {
////				cardNo = cardInfo[1];// 磁卡卡号
////				Pan = cardNo;
////				Track2 = cardInfo[3];
////				Track3 = cardInfo[4];
////				if (StringUtil.isNullWithTrim(Track3))
////					Track3 = null;
////
////				//提示确认卡号的的窗口
////				result = TransUtil.getNoticeResult(handle,cardNo);
////				Logger.debug("notice card result = " + result);
////				if (result == null) {//取消确认卡号或者直接超时
////					clearPan();
////					handle.sendEmptyMessage(ERR_QUIT);
////					return;
////				}
////				result = TransUtil.getPass(handle, R.string.sale_enter_pass, cardNo);
////				handle.sendEmptyMessage(Trans.WaitOnLineTrans);
////				if(result == null) {//取消输入密码或者直接超时
////					clearPan();
////					handle.sendEmptyMessage(ERR_QUIT);
////					return;
////				}if(result.equals(""))
////					isPinExist = false;
////				else
////					isPinExist = true;
////				if (isPinExist)
////					PIN = result;
////			} else if (inputMode == ENTRY_MODE_ICC) {
////				Logger.debug("icc emv trans starting");
////				if (cardInfo.length == 2 && cardInfo[1].equals("Key Err")) {
////					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_nokey_err)};
////					handle.sendMessage(message);
////					return;
////				}if (cardInfo.length == 1) {
////					// 如果上电失败 通过iccRet执行降级操作
////					emvRet = 2;
////				} else {
////					emv = new EmvTransaction(context, true, Amount, inputMode, 0, handle  , TransEName);
////					emvRet = emv.EMVTramsProcess();
////				}
////				cardNo = emv.getCardNo() ;
////				if (emvRet == 0) {
////					// 脱机批准,保存数据存交易记录,存交易记录
////					setICCData();
////					if (isSaveLog) {
////						TransLogData logData = setTCAcceptLogdata(cardNo);
////						transLog.saveLog(logData);
////						configManager.increFlag();// flag++已存
////					}
////					iccard = IccReader.getInstance(SlotType.USER_CARD);
////					if (iccard != null) {
////						iccard.stopSearchCard();
////						Logger.debug("relase icc");
////						iccard.release();
////						clearPan();
////					}
////					printReceiptOffline(handle);
////				} else if (emvRet == 1) {
////					// 联机处理,已在setdatas中处理55域数据
////					result = emv.getpinBlock();
////					if (result.equals("")) {
////						isPinExist = false;
////					} else
////						isPinExist = true;
////					if (isPinExist)
////						PIN = result;
////					setICCData();
////					handle.sendEmptyMessage(Trans.WaitOnLineTrans);
////				} else if (emvRet == 2) {
////					handle.sendEmptyMessage(Trans.TRANS_IC_DOWNGRADE);
////					RunMag(handle);
////				}else if(emvRet == Trans.EMV_OFFLINE_DATAAUTH){
////					clearPan();
////					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_offlineDataAuth_err)};
////					handle.sendMessage(message);
////					return;
////				}else if(emvRet == Trans.EMV_CARDHOLDER_AUTH_FAIL){
////					clearPan();
////					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_cardHolderVerify_err)};
////					handle.sendMessage(message);
////					return;
////				}else if(emvRet == Trans.EMV_TERMINAL_ANAYSIS_FAIL){
////					clearPan();
////					message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalRiskMana_err)};
////					handle.sendMessage(message);
////					return;
////				}else if(emvRet == Trans.EMV_CONFIGMCARD_CANCEL_OR_TIMEOUT){
////					clearPan();
////					handle.sendEmptyMessage(ERR_QUIT);
////					return;
////				}else {
////					clearPan();
////					message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+emvRet+"]"};
////					handle.sendMessage(message);
////					return;
////				}
////			}else if(inputMode == Trans.ENTRY_MODE_NFC){
////				Logger.debug("nfc emv trans starting");
////				if(GlobalCfg.getInstance().isQPBOC()){
////					qpboc = new QpbocTransaction(context) ;
////					int qpboc_ret = qpboc.qpbocProcess() ;
////					if( qpboc_ret == 0){
////						byte[] temp = new byte[256] ;
////						int len = TLVUtil.get_tlv_data_kernal(0x57 , temp);
////						Logger.debug("temp = "+ISOUtil.hexString(temp)) ;
////						if(len < 7)	//磁道信息没读到
////							return;
////						String CN = ISOUtil.hexString(temp) ;
////						Logger.debug("CN = "+CN.split("D")[0]);
////						Pan = CN.split("D")[0];
////						Logger.debug("Pan = "+Pan);
////						IEMVHandler emvHandler = EMVHandler.getInstance();
////						temp =  ISOUtil.str2bcd(Pan , false);
////						if(Pan.length()%2 != 0)
////							temp[Pan.length() / 2] |= 0x0f;
////						emvHandler.setDataElement(new byte[]{0x5A} ,temp );
//////						LogManager.t("temp before track2 = "+ISOUtil.hexString(temp));
//////						String track2 = ISOUtil.trimf(ISOUtil.bcd2str(temp, 0, len,false));
//////						LogManager.t("track2 = "+track2) ;
////						cardNo = Pan ;
////
//////						result = TransUtil.getNoticeResult(handle,context.getString(R.string.sale_confirm_card_num) + cardNo);
//////						LogManager.t("notice card result = " + result);
//////						if (result == null) {//取消确认卡号或者直接超时
//////							clearPan();
//////							qpbocPrecessCancelbyUsers(handle);
//////							return;
//////						}
////
//////						byte[] res = new byte[8] ;
////						byte[] res = new byte[32] ;
////						TLVUtil.get_tlv_data_kernal(0x9F10 , res);
////						Logger.debug("9f10 = "+ISOUtil.hexString(res));
////						if( (res[4]&0x30) == (byte)0x20 ){
////							Logger.debug("QPBOC ARQC");
////							result = TransUtil.getPass(handle,R.string.sale_enter_pass, cardNo);
////							handle.sendEmptyMessage(Trans.WaitOnLineTrans);
////							Logger.debug("after enter pass result = "+result);
////							if(result.contains("errcode")){
////								clearPan();
////								message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_get_pinblock_err)+":\n"+result} ;
////								noticeRemoveNFCcard(handle , message);
////								return;
////							}if (result.equals(""+ PedRetCode.ENTER_CANCEL)) {
////								clearPan();
////								qpbocPrecessCancelbyUsers(handle);
////								return;
////							}if (result.equals("")) {
////								isPinExist = false;
////							} else
////								isPinExist = true;
////							if (isPinExist)
////								PIN = result;
////							setICCData();
////							handle.sendEmptyMessage(Trans.WaitOnLineTrans);
////						}else if( ((res[4]&0x30)  == (byte)0x10) || ((res[4]&0xC0) == (byte)0x40)){
////							Logger.debug("QPBOC TC");
////							setICCData();
//////							int ret = afterQPBOCTC(handle , cardNo) ;
//////							if( ret == 0){
//////								message.obj = new String[]{SUCC , context.getResources().getString(R.string.sale_over_success)+"\n\n"+
//////										context.getResources().getString(R.string.sale_over_trans_amount)+
//////										"\n\nRMB:"+TransUtil.getStrAmount(Amount)};
//////								noticeRemoveNFCcard(handle , message);
//////								return;
//////							}else {
//////								message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+ret+"]"};
//////								noticeRemoveNFCcard(handle , message);
//////								return;
//////							}
////						} else {
////							Logger.debug("QPBOC REFUSE");
////							message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+res[4]+"]"};
////							noticeRemoveNFCcard(handle , message);
////							return;
////						}
////					}else {
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+qpboc_ret+"]"} ;
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}
////				}else {
////					emv = new EmvTransaction(context, true, Amount, inputMode, 0, handle , TransEName );
////					emvRet = emv.EMVTramsProcess();
////					if(emvRet == 0){
////						setICCData();
////						if (isSaveLog) {
////							TransLogData logData = setTCAcceptLogdata(cardNo);
////							transLog.saveLog(logData);
////							configManager.increFlag();// flag++已存
////						}
////						printReceiptOffline(handle);
////						message.obj = new String[]{SUCC , context.getResources().getString(R.string.sale_over_success)};
////						noticeRemoveNFCcard(handle , message);
////					}else if(emvRet == 1){
////						result = emv.getpinBlock();
////						if (result.equals("")) {
////							isPinExist = false;
////						} else
////							isPinExist = true;
////						if (isPinExist)
////							PIN = result;
////						setICCData();
////						handle.sendEmptyMessage(Trans.WaitOnLineTrans);
////					}else if(emvRet == IEMVHandler.EMV_ERRNO_CARD_BLOCKED || emvRet == IEMVHandler.EMV_ERRNO_APP_BLOCKED){
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_selectApp_err)};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}else if(emvRet == IEMVHandler.EMV_ERRNO_NODATA){
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_readAppData_err)};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}else if(emvRet == Trans.EMV_OFFLINE_DATAAUTH){
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_offlineDataAuth_err)};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}else if(emvRet == Trans.EMV_CARDHOLDER_AUTH_FAIL){
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_cardHolderVerify_err)};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}else if(emvRet == Trans.EMV_TERMINAL_ANAYSIS_FAIL){
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalRiskMana_err)};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}else if(emvRet == Trans.EMV_TERMINAL_ACTION_ANAYSIS_FAIL){
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.emv_terminalAction_err)};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}else if(emvRet == Trans.EMV_CONFIGMCARD_CANCEL_OR_TIMEOUT){
////						clearPan();
////						handle.sendEmptyMessage(ERR_QUIT);
////						return;
////					}else {
////						clearPan();
////						message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_over_fail)+"["+emvRet+"]"};
////						noticeRemoveNFCcard(handle , message);
////						return;
////					}
////				}
////			}
////
////			setDatas(inputMode);
////			int resultCode ;
////			if (inputMode == ENTRY_MODE_ICC || inputMode == ENTRY_MODE_NFC)
////				resultCode = OnlineTrans(handle, emv , cardNo);
////			else
////				resultCode = OnlineTrans(handle, null , cardNo);
////
////			if (iccard != null) {
////				iccard.stopSearchCard();
////				iccard.release();
////			}
////
////			clearPan();
////			if(inputMode == ENTRY_MODE_NFC){
////				PiccReader nfcCard = PiccReader.getInstance() ;
////				handle.sendEmptyMessage(Trans.NFC_REMOVE);
//////				nfcCard.deactive();
////			}
////			int rid = getResurceId(resultCode) ;
////			String info = context.getResources().getString(rid);
////			if(resultCode == 0){
////				info = context.getResources().getString(rid)+"\n\n"+
////						context.getResources().getString(R.string.sale_over_trans_amount)+
////						"\n\nRMB:"+TransUtil.getStrAmount(Amount) ;
////			}
////			message.obj = new String[]{resultCode==0?SUCC:FAIL , info} ;
////			handle.sendMessage(message);
////			return;
////		}
////	}
////
////	private revocation qpbocPrecessCancelbyUsers(Handler mha){
////		PiccReader nfcCard = PiccReader.getInstance() ;
////		mha.sendEmptyMessage(Trans.NFC_REMOVE);
//////		nfcCard.deactive();
////		mha.sendEmptyMessage(ERR_QUIT);
////	}
////
////	private revocation noticeRemoveNFCcard(Handler h , Message m){
////		PiccReader nfcCard = PiccReader.getInstance() ;
////		h.sendEmptyMessage(Trans.NFC_REMOVE);
//////		nfcCard.deactive();
////		h.sendMessage(m) ;
////	}
////
////	// IC卡交易失败执行降级操作
////	private revocation RunMag(Handler handle) {
////		String result = TransUtil.getCard(handle, R.string.sale_prompt_swipe_card, INMODE_MAG, false);
////		if (StringUtil.isNullWithTrim(result)) {
////			clearPan();
////			Message message = handle.obtainMessage() ;
////			message.what = TRANS_OVER ;
////			message.obj = new String[]{FAIL , context.getResources().getString(R.string.sale_swipe_card_timeout)};
////			handle.sendMessage(message);
////			return ;
////		}
////		String[] cardInfo = result.split(",");
////		inputMode = Integer.parseInt(cardInfo[0]);
////		if (inputMode == ENTRY_MODE_MAG) {
////			cardNo = cardInfo[1];// 磁卡卡号
////			Pan = cardNo;
////			Track2 = cardInfo[3];
////			Track3 = cardInfo[4];
////			if (StringUtil.isNullWithTrim(Track3))
////				Track3 = null;
////
////			// 提示确认卡号的的窗口
////			result = TransUtil.getNoticeResult(handle, cardNo);
////			if (result == null) {
////				clearPan();
////				handle.sendEmptyMessage(ERR_QUIT);
////				return ;
////			}
////
////			//获取密码
////			result = TransUtil.getPass(handle, R.string.sale_enter_pass, cardNo);
////			if (result == null) {
////				clearPan();
////				handle.sendEmptyMessage(ERR_QUIT);
////				return ;
////			}
////			if (result.equals(""))
////				isPinExist = false;
////			else
////				isPinExist = true;
////			if (isPinExist)
////				PIN = result;
////		}
////	}
////
////	private revocation printReceiptOffline(Handler handler){
////		int ret ;
////		handler.sendEmptyMessage(Trans.OnLineTransEnd);
////		TransLog.clearReveral(); // 交易记录存完就可以清除冲正了
////		handler.sendEmptyMessage(Trans.TRANS_OVER_PRINTING);
////		PrintManager print = new PrintManager(context, handler);
////		do {
////			ret = print.printTransLog(transLog.getLastTransLog(), false);
////		} while (ret == Printer.PRINTER_STATUS_PAPER_LACK);
////		if (ret == Printer.PRINTER_OK) {
////			configManager.clearFlag();
////			ret = 0;
////		} else if (ret == ONLINE_RET_PRINT_CANCLE) {
////			configManager.clearFlag();
////			ret = ONLINE_RET_PRINT_CANCLE;
////		} else
////			ret = ONLINE_RET_PRINTING_ERR;
////
////		int rid = getResurceId(ret) ;
////		Message message = handler.obtainMessage() ;
////		message.what = TRANS_OVER ;
////		String info = context.getResources().getString(rid) ;
////		if (ret == 0) {
////			info =  context.getResources().getString(rid)+
////					"\n\n"+context.getResources().getString(R.string.sale_over_trans_amount)+
////					"\n\nRMB:"+TransUtil.getStrAmount(Amount);
////		}
////		message.obj = new String[]{ret == 0?SUCC:FAIL , info} ;
////		handler.sendMessage(message);
////	}
////
////	private int getResurceId(int resultCode){
////		int rid ;
////		switch (resultCode){
////			case 0:rid = R.string.sale_over_success ;break;
////			case ONLINE_INTERRUPT:rid = R.string.sale_interrupt ;break;
////			case ONLINE_ICC_RET_ERR:rid = R.string.sale_over_icc_err ;break;
////			case ONLINE_RET_MAC_ERR:rid = R.string.sale_mac_err ;break;
////			case ONLINE_RET_SOCKET_ERR:rid = R.string.socket_fail ;break;
////			case ONLINE_RET_SEND_ERR:rid = R.string.socket_send_msg_err ;break;
////			case ONLINE_RET_RECIVE_ERR_PACKET:rid = R.string.socket_err_packet ;break;
////			case ONLINE_RET_RECIVE_EMPTY:rid = R.string.socket_receive_empty_err ;break;
////			case ONLINE_RET_RECIVE_REFUSE:rid = R.string.sale_trans_refuse ;break;
////			case ONLINE_RET_PRINTING_ERR:rid = R.string.sale_print_err ;break;
////			case ONLINE_RET_PRINT_CANCLE:rid = R.string.sale_print_cancel ;break;
////			case EMV_ERRNO_DECLINE:rid = R.string.sale_trans_refuse ;break;
////			case EMV_ERRNO_CARD_BLOCKED:rid = R.string.sale_trans_refuse ;break;
////			case EMV_ERRNO_APP_BLOCKED:rid = R.string.sale_trans_refuse ;break;
////			case EMV_ERRNO_PIN_BLOCKED:rid = R.string.sale_trans_refuse ;break;
////			case ONLINE_RET_EMV_REFUSE:rid = R.string.sale_emv_refuse ;break;
////			default:rid = R.string.sale_other_err ;break;
////		}
////		return rid ;
////	}
////
//}
