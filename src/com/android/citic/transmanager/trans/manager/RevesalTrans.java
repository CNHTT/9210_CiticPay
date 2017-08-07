package com.android.citic.transmanager.trans.manager;

import android.content.Context;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.trans.Tcode;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;

/**
 * 冲正交易
 */
public class RevesalTrans extends Trans {

	public RevesalTrans(Context ctx, String transEname) {
		super(ctx, transEname);
		isUseOrgVal = true; // 使用原交易的60.1 60.3
		iso8583.setHasMac(true);
		isTraceNoInc = false; // 冲正不需要自增流水号
	}

	protected void setFields(TransLogData data) {
		if (MsgID != null)
			iso8583.setField(0, MsgID);
		if (data.getPan() != null)
			iso8583.setField(2, data.getPan());
		if (data.getProcCode() != null)
			iso8583.setField(3, data.getProcCode()); // 3
		if (data.getAmount() >= 0) {
			String AmoutData = "";
			AmoutData = ISOUtil.padleft(data.getAmount() + "", 12, '0');
			iso8583.setField(4, AmoutData);// 4
		}
		if (data.getTraceNo() != null)
			iso8583.setField(11, data.getTraceNo());// 11
		if (data.getExpDate() != null)
			iso8583.setField(14, data.getExpDate()); // 14 YYMM
		if (data.getEntryMode() != null)
			iso8583.setField(22, data.getEntryMode());// 22
		if (data.getPanSeqNo() != null)
			iso8583.setField(23, data.getPanSeqNo()); // 23
		if (data.getSvrCode() != null)
			iso8583.setField(25, data.getSvrCode()); // 25
		if (data.getAuthCode() != null)
			iso8583.setField(38, data.getAuthCode()); // 38
		if (data.getRspCode() != null)
			iso8583.setField(39, data.getRspCode());// 39
		if (TermID != null)
			iso8583.setField(41, TermID); // 41
		if (MerchID != null)
			iso8583.setField(42, MerchID);// 42
		if (data.getCurrencyCode() != null)
			iso8583.setField(49, data.getCurrencyCode());// 49
		if (data.getICCData() != null)
			iso8583.setField(55, ISOUtil.byte2hex(data.getICCData()));// 55
		if (data.getField60() != null)
			iso8583.setField(60, data.getField60());// 60
	}

	public int sendRevesal() {
		TransLogData data = TransLog.getReversal();
		setFields(data);
		retVal = OnLineTrans();
		if (retVal == 0) {
			RspCode = iso8583.getfield(39);
			if (RspCode.equals("00") || RspCode.equals("12")
					|| RspCode.equals("25")) {
				return retVal;
			} else {
				data.setRspCode("06");
				TransLog.saveReversal(data);
				return Tcode.T_receive_refuse;
			}
		} else if (retVal == Tcode.T_package_mac_err) {
			data.setRspCode("A0");
			TransLog.saveReversal(data);
		} else if(retVal == Tcode.T_receive_err){
			data.setRspCode("08");
			TransLog.saveReversal(data);
		} else if(retVal == Tcode.T_package_illegal){
			data.setRspCode("08");
			TransLog.saveReversal(data);
		}else {
			Logger.debug("Revesal result :" + retVal);
		}
		return retVal;
	}
}
