package com.android.citic.transmanager.paras;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmvAidInfo implements Serializable {

	/**
	 * struct aid_info ->class TerminalAidInfo application id.. describe IC卡应用目录
	 * RID ddol data object list TL ~TL TLV Tag Len Value CAPK 公钥 CA认证中心
	 * CaPublickKey 公钥 TerminalAid
	 */
	public static final String FILENAME = "aid.dat";

	private List<TerminalAid> aidInfoList;

	public EmvAidInfo() {
		aidInfoList = new ArrayList<TerminalAid>();
	}

	public List<TerminalAid> getAidInfoList() {
		return aidInfoList;
	}

	public void setAidInfoList(List<TerminalAid> aidInfoList) {
		this.aidInfoList = aidInfoList;
	}
}
