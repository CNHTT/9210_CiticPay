package com.android.citic.transmanager.trans.manager;

import android.content.Context;

import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;

/**
 * 脚本处理交易
 */
public class ScriptTrans extends Trans {

	public ScriptTrans(Context ctx, String transEname) {
		super(ctx, transEname);
		iso8583.setHasMac(true);
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
		if (TraceNo != null)
			iso8583.setField(11, TraceNo);// 11
		if (data.getEntryMode() != null)
			iso8583.setField(22, data.getEntryMode());// 22
		if (data.getAcquirerID() != null)
			iso8583.setField(32, data.getAcquirerID()); // 32
		if (data.getRRN() != null)
			iso8583.setField(37, data.getRRN()); // 37
		if (data.getAuthCode() != null)
			iso8583.setField(38, data.getAuthCode()); // 38
		if (TermID != null)
			iso8583.setField(41, TermID); // 41
		if (MerchID != null)
			iso8583.setField(42, MerchID);// 42
		if (data.getCurrencyCode() != null)
			iso8583.setField(49, data.getCurrencyCode());// 49
		if (data.getICCData() != null)
			iso8583.setField(55, ISOUtil.byte2hex(data.getICCData()));// 55
		// 60.1 00 60.2 批次号 60.3 951
		// Field60 = "00" + config.getBatchNo() + "951";
		if (Field60 != null)
			iso8583.setField(60, Field60);// 60
		// 61.1 原交易批次号 61.2 原pos流水号 61.3 原交易日期
		Field61 = data.getTraceNo() + data.getBatchNo() + data.getLocalDate();
		iso8583.setField(61, Field61);// 61
	}

	public int sendScriptResult(TransLogData data) {
		setFields(data);
		int ret = OnLineTrans();
		return ret;
	}
}
