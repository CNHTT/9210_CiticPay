package com.android.citic.transmanager.paras;

import com.pos.device.emv.TerminalAidInfo;

import java.io.Serializable;

public class TerminalAid implements Serializable {

	/**
	 * case 0xDF18: //联机PIN支持能力1:支持;0不支持 boolean ifSurportOnlinePin case 0x9F7B:
	 * //电子现金交易最大金额 long lEcMaxTxnAmount
	 */
	private TerminalAidInfo terminalAidInfo;

	public TerminalAidInfo getTerminalAidInfo() {
		return terminalAidInfo;
	}

	public void setTerminalAidInfo(TerminalAidInfo terminalAidInfo) {
		this.terminalAidInfo = terminalAidInfo;
	}

	private boolean isSurportOnlinePin;
	private int lEcMaxTxnAmount;

	public boolean isSurportOnlinePin() {
		return isSurportOnlinePin;
	}

	public void setisSurportOnlinePin(boolean ifSurportOnlinePin) {
		this.isSurportOnlinePin = ifSurportOnlinePin;
	}

	public int getlEcMaxTxnAmount() {
		return lEcMaxTxnAmount;
	}

	public void setlEcMaxTxnAmount(int lEcMaxTxnAmount) {
		this.lEcMaxTxnAmount = lEcMaxTxnAmount;
	}
}
