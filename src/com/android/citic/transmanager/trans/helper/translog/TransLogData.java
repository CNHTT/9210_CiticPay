package com.android.citic.transmanager.trans.helper.translog;

import java.io.Serializable;

public class TransLogData implements Serializable {

	public String getIssuerName() {
		return IssuerName;
	}

	public void setIssuerName(String issuerName) {
		IssuerName = issuerName;
	}

	private String Pan; // 2* （加粗 S 刷卡方式和22域有关联）
	private String ProcCode; // 3
	private Long Amount; // 4* （加粗）
	private String TraceNo; // 11*
	private String LocalTime; // 12 hhmmss*
	private String LocalDate; // 13 MMDD*
	private String ExpDate; // 14 YYMM*
	private String SettleDate; // 15 MMDD*
	private String EntryMode; // 22*
	private String PanSeqNo; // 23*
	private String SvrCode;// 25域 服务点条件吗
	private String AcquirerID; // 32*
	private String RRN; // 37*
	private String AuthCode; // 38*
	private String RspCode; // 39 正常交易不存冲正具体设置
	private String Field44; // 44 * (发卡行和 收单行 11 11)
	/** private String Field48; // 48 */
	private String CurrencyCode; // 49*
	private String IssuerName;// 发卡组织 63域 前三位
	private String Refence; // 备注 63 从第三位开始到最后
	private int oprNo; // 操作员号
	private String TransEName; // 交易名
	private byte[] ICCData; // 55*
	private String Field60;
	private boolean isICC;
	private boolean isNFC ;
	private boolean isBarcode;

	private String cardFullNo ;
	private int AAC ;//应用密文

	public int getAAC() {
		return AAC;
	}

	public void setAAC(int AAC) {
		this.AAC = AAC;
	}

	public void setCardFullNo(String cardFullNo){
		this.cardFullNo = cardFullNo ;
	}

	public String getCardFullNo(){
		return cardFullNo ;
	}

	public boolean isNFC(){
		return isNFC ;
	}

	public void setNFC(boolean isNFC){
		this.isNFC = isNFC ;
	}

	public boolean isICC() {
		return isICC;
	}

	public void setICC(boolean isICC) {
		this.isICC = isICC;
	}

	public boolean isBarcode() {
		return isBarcode;
	}

	public void setBarcode(boolean isBarcode) {
		this.isBarcode = isBarcode;
	}

	private int RecState;/** 初始状态为0，已上送成功1，已上送但是失败2 **/

	private boolean isVoided;/** 是否已撤销 **/

	private boolean isAdjusted;/** 是否已调整 **/

	private boolean isFallBack;/** 降级 IC卡读取不到，改刷卡 **/

	/** 小费 **/
	private long TipAmout = 0;
	/** 原交易流水号 **/
	private String BatchNo;

	public String getBatchNo() {
		return BatchNo;
	}

	public void setBatchNo(String batchNo) {
		BatchNo = batchNo;
	}

	public boolean getIsVoided(){
		return isVoided ;
	}

	public void setVoided(boolean isVoided){
		this.isVoided = isVoided ;
	}

	public int getRecState() {
		return RecState;
	}

	public void setRecState(int recState) {
		RecState = recState;
	}

	public String getEName() {
		return TransEName;
	}

	public void setEName(String eName) {
		TransEName = eName;
	}

	public String getField60() {
		return Field60;
	}

	public void setField60(String field60) {
		Field60 = field60;
	}

	public String getPan() {
		return Pan;
	}

	public void setPan(String pan) {
		Pan = pan;
	}

	public Long getAmount() {
		return Amount;
	}

	public void setAmount(Long amount) {
		Amount = amount;
	}

	public byte[] getICCData() {
		return ICCData;
	}

	public void setICCData(byte[] iCCData) {
		ICCData = iCCData;
	}

	public String getTraceNo() {
		return TraceNo;
	}

	public void setTraceNo(String traceNo) {
		TraceNo = traceNo;
	}

	public String getLocalTime() {
		return LocalTime;
	}

	public void setLocalTime(String localTime) {
		LocalTime = localTime;
	}

	public String getLocalDate() {
		return LocalDate;
	}

	public void setLocalDate(String localDate) {
		LocalDate = localDate;
	}

	public String getExpDate() {
		return ExpDate;
	}

	public void setExpDate(String expDate) {
		ExpDate = expDate;
	}

	public String getSettleDate() {
		return SettleDate;
	}

	public void setSettleDate(String settleDate) {
		SettleDate = settleDate;
	}

	public String getEntryMode() {
		return EntryMode;
	}

	public void setEntryMode(String entryMode) {
		EntryMode = entryMode;
	}

	public String getPanSeqNo() {
		return PanSeqNo;
	}

	public void setPanSeqNo(String panSeqNo) {
		PanSeqNo = panSeqNo;
	}

	public String getAcquirerID() {
		return AcquirerID;
	}

	public void setAcquirerID(String acquirerID) {
		AcquirerID = acquirerID;
	}

	public String getRRN() {
		return RRN;
	}

	public void setRRN(String rRN) {
		RRN = rRN;
	}

	public String getAuthCode() {
		return AuthCode;
	}

	public void setAuthCode(String authCode) {
		AuthCode = authCode;
	}

	public String getRspCode() {
		return RspCode;
	}

	public void setRspCode(String rspCode) {
		RspCode = rspCode;
	}

	public String getField44() {
		return Field44;
	}

	public void setField44(String field44) {
		Field44 = field44;
	}

	public String getProcCode() {
		return ProcCode;
	}

	public void setProcCode(String procCode) {
		ProcCode = procCode;
	}

	public String getCurrencyCode() {
		return CurrencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}

	public String getRefence() {
		return Refence;
	}

	public void setRefence(String refence) {
		Refence = refence;
	}

	public int getOprNo() {
		return oprNo;
	}

	public void setOprNo(int oprNo) {
		this.oprNo = oprNo;
	}

	public String getSvrCode() {
		return SvrCode;
	}

	public void setSvrCode(String svrCode) {
		SvrCode = svrCode;
	}
	/**
	 * private String Field60; // 60 private String Field61; // 61 private
	 * String Field62; // 62 private String Field63; // 63
	 **/
}
