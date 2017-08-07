package com.android.citic.transmanager.global;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.FileUtil;
import com.android.citic.lib.utils.ISOUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

public class GlobalCfg implements Serializable {

	private static final long serialVersionUID = 1L;
	private static String ConfigPath = "config.dat";
	private static GlobalCfg configManager = null ;

	private static final boolean DEBUG = true ;//是否开启调试日志
	private static String ROOT_FILE_PATH = "/sdcard/";//本程序的所有文件的保存路径
	private static boolean isParaLoaded = false ;//EMVAID是否导入
	private static boolean isCAPKLoaded = false ;//EMVCAPK是否导入

	private String ip = null ;//联机后台台公网端口
	private String ip2 = null ;//联机后台公网IP
	private String port = null ;//联机后内网IP
	private String port2 = null ;//联机后台内网端口
	private int timeout = 60*1000 ;//联机超时时间（单位:毫秒）
	private boolean isPubCommun = false ;//是否开启公网通讯

	private int exitTime = 10 ;//交易结果退出时间(单位：秒)
	private int waitUserTime = 60 ; //等待用户交互超时时间(单位 :秒)

	private String password ;//主管密码
	private String securePass ;//维护密码

	private boolean revocationPassWSwitch = false ;//消费撤销密码开关
	private boolean revocationCardSwitch = false ;//消费撤销用卡开关
	private boolean contactlessSwitch = false; //非接开关
	private int masterKeyIndex ;//主密钥索引号

	private boolean isCheckICC = true ;//是否校验IC卡刷卡

	private String tpdu = null ; // 报文TPDU
	private String header = null ; // 报文头
	private String TermID = null ; // 终端ID
	private String MerchID = null ; // 商户ID
	private int BatchNo = 0 ; // 分支号
	private int TraceNo = 0 ; // 流水号
	private int transFlag = 0 ; // 交易记录标志
	private int oprNo = 0 ; // 操作员编号
	private int PrinterTickNumber = 2 ; // 1-3联 1 商户 2.持卡人(没签名) 3.银行
	private String MerchName = null ; // 商户名称
	private int printEn = 1 ; // 1中文 2英文 3中英文
	private boolean isTrackEncrypt = true ; // 磁道是否加密
	private boolean isSingleKey = false ; //是否是单倍长密钥
	private String CurrencyCode = null ; //货币代码
	private String firmCode = null ; // 商行代码
	private int reversalCount = 3 ;
	private String isPrintCHAcqBank = null ; // 是否打印中文收单行 1打印 0不打印
	private String isPrintCHISSBank = null ; // 是否打印中文发卡行 1打印 0不打印
	private String isPrintEHSelesSlip = null ; // 是否打印英文签购单 1打印 0不打印
	private String maxTrans = null ; // 最大交易笔数
	private String transRepeatCount = null ; // 交易重发次数

	private boolean isTermLogon = false ;//终端状态

	private GlobalCfg() {
		this.ip = "172.1.120.110";
		this.ip2 = "22.5.227.71" ;//22.5.228.211
		this.port = "16681";
		this.port2 = "7010" ;//测试环境
		this.tpdu = "6000040000";
		this.header = "603100000000";
		this.TermID = "07725829";//07100092 07785408
		this.MerchID = "302100007630006";//302100059980001 302100070320047
		this.isTrackEncrypt = false ;
		this.isSingleKey = false;
		this.BatchNo = 22;
		this.TraceNo = 300;
		this.PrinterTickNumber = PrinterTicknum.T_1;
		this.printEn = PrinterLang.L_MIX;
		this.oprNo = 1;
		this.MerchName = "NEW9210 DEMO";
		this.CurrencyCode = "156";
		this.firmCode = "0000";
		this.reversalCount = 3 ;
		this.isPrintCHAcqBank = "0";
		this.isPrintCHISSBank = "0";
		this.isPrintEHSelesSlip = "0";
		this.maxTrans = "999999";
		this.transRepeatCount = "3";
		this.masterKeyIndex = 1 ;
		this.exitTime = 10;
		this.waitUserTime= 60;
		this.timeout = 60*1000 ;
		this.isPubCommun = false ;
		this.password = "123456" ;
		this.securePass = "20170310" ;
		this.revocationPassWSwitch = false ;
		this.revocationCardSwitch = true ;
		this.isCheckICC = true ;
		this.isTermLogon = false ;
	}

	public static final boolean getDebug(){
		return DEBUG ;
	}

	public static String getROOT_FILE_PATH() {
		return ROOT_FILE_PATH;
	}

	public static void setROOT_FILE_PATH(String rOOT_FILE_PATH) {
		ROOT_FILE_PATH = rOOT_FILE_PATH;
	}

	public static boolean isEmvParamLoaded() {
		return isParaLoaded ;
	}

	public static void setEmvParamLoaded() {
		isParaLoaded = true ;
	}

	public static boolean isCAPKLoaded(){
		return isCAPKLoaded ;
	}

	public static void setEMVCAPKLoaded(){
		isCAPKLoaded = true ;
	}

	public static GlobalCfg getInstance() {
		String fullPath = getROOT_FILE_PATH() + ConfigPath;
		if (configManager == null) {
			try {
				configManager = (GlobalCfg) FileUtil.file2Object(fullPath);
			} catch (FileNotFoundException e) {
				Logger.debug("File not found");
				configManager = null;
			} catch (IOException e) {
				Logger.debug("IO Exception");
				configManager = null;
			} catch (ClassNotFoundException e) {
				Logger.debug("ClassNotFoundException");
				configManager = null;
			}
			if (configManager == null) {
				Logger.debug("get config from path failed");
				configManager = new GlobalCfg();
			}
		}
		return configManager ;
	}

	public boolean isTermLogon() {
		return isTermLogon;
	}

	public void setTermLogon(boolean termLogon) {
		isTermLogon = termLogon;
	}

	/** 主密钥索引 */
	public int getMasterKeyIndex() {
		return masterKeyIndex;
	}

	public void setMasterKeyIndex(int masterKeyIndex) {
		this.masterKeyIndex = masterKeyIndex;
	}

	/** 货币代码 */
	public String getCurrencyCode() {
		return CurrencyCode;
	}

	public void setCurrencyCode(String cur){
		this.CurrencyCode = cur ;
	}

	/** 是否磁道加密 */
	public boolean isTrackEncrypt() {
		return isTrackEncrypt;
	}

	public void setTrackEncrypt(boolean is){
		this.isTrackEncrypt = is ;
	}

	/** 是否单倍长密钥 */
	public boolean isSingleKey() {
		return isSingleKey;
	}

	public void setSingleKey(boolean is){
		this.isSingleKey = is ;
	}

	/** 通信公网ip */
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	/** 通信公网端口 */
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	/** 是否开启公网通讯 */
	public void setPubCommun(boolean is){
		this.isPubCommun = is ;
	}

	public boolean getPubCommun(){
		return isPubCommun ;
	}

	/** 消费撤销输密开关 */
	public boolean getRevocationPassSwitch(){
		return revocationPassWSwitch ;
	}

	public void setRevocationPassWSwitch(boolean is){
		this.revocationPassWSwitch = is ;
	}

	/** 消费撤销用卡开关 */
	public void setRevocationCardSwitch(boolean is){
		this.revocationCardSwitch = is ;
	}

	public boolean getRevocationCardSwitch(){
		return revocationCardSwitch ;
	}

	/** 非接开关 */
	public void setContactlessSwitch(boolean is){
		this.contactlessSwitch = is ;
	}

	public boolean getContactlessSwitch(){
		return contactlessSwitch ;
	}

	/** 通信内网IP */
	public String getIP2(){
		return ip2 ;
	}

	public void setIp2(String s){
		this.ip2 = s ;
	}

	/** 通信内网端口 */
	public String getPort2(){
		return port2 ;
	}

	public void setPort2(String s){
		this.port2 = s ;
	}

	/** 交易结果退出时间 */
	public int getExitTime(){
		return exitTime ;
	}

	public void setExitTime(int e){
		this.exitTime = e ;
	}

	/** 冲正次数选择 */
	public int getReversalCount() {
		return reversalCount;
	}

	public void setReversalCount(int reversalCount) {
		this.reversalCount = reversalCount;
	}

	/** 等待用户交互超时时间 */
	public int getWaitUserTime() {
		return waitUserTime;
	}

	public void setWaitUserTime(int waitUserTime) {
		this.waitUserTime = waitUserTime;
	}

	/** 通信超时时间 */
	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	/** 主管密码 */
	public String getPassword(){
		return password ;
	}

	public void setPassword(String pass){
		this.password = pass ;
	}

	public String getSecurePass() {
		return securePass;
	}

	public void setSecurePass(String securePass) {
		this.securePass = securePass;
	}

	/** 是否校验IC卡刷卡操作 */
	public boolean isCheckICC(){
		return isCheckICC ;
	}

	public void setCheckICC(boolean is){
		this.isCheckICC = is ;
	}

	/** 报文TPDU */
	public String getTpdu() {
		return tpdu;
	}

	public void setTpdu(String tpdu) {
		this.tpdu = tpdu;
	}

	/** 报文头 */
	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	/** 终端ID */
	public String getTermID() {
		return TermID;
	}

	public void setTermID(String termID) {
		TermID = termID;
	}

	/** 商户ID */
	public String getMerchID() {
		return MerchID;
	}

	public void setMerchID(String merchID) {
		MerchID = merchID;
	}

	/** 交易分支号 */
	public String getBatchNo() {
		return ISOUtil.padleft(BatchNo + "", 6, '0');
	}

	public void setBatchNo(int batchNo) {
		BatchNo = batchNo;
	}

	/** 交易流水号 */
	public String getTraceNo() {
		return ISOUtil.padleft(TraceNo + "", 6, '0');
	}

	public void setTraceNo(int traceNo) {
		TraceNo = traceNo;
	}

	/** 流水号自增设置 */
	public void incTraceNo() {
		if (TraceNo == 999999)
			TraceNo = 0;
		TraceNo = TraceNo + 1;
		save();
	}

	/** 打印凭条联数 */
	public int getPrinterTickNumber() {
		return PrinterTickNumber;
	}

	public void setPrinterTickNumber(int n) {
		this.PrinterTickNumber = n ;
	}

	/** 打印凭条语言设置 */
	public int getPrintEn() {
		return printEn;
	}

	public void setPrintEn(PrinterLang lang) {
		if(lang == null){
			lang = new PrinterLang() ;
			lang.setLang(PrinterLang.L_MIX);
		}
		this.printEn = lang.getLang();
	}

	/** 商户名称 */
	public String getMerchName() {
		return MerchName;
	}

	public void setMerchName(String merchName) {
		MerchName = merchName;
	}

	/** 操作员号 */
	public int getOprNo() {
		return oprNo;
	}

	public void setOprNo(int oprNo) {
		this.oprNo = oprNo;
	}

	/** 商行代码 */
	public String getFirmCode() {
		return firmCode;
	}

	public void setFirmCode(String firmCode) {
		this.firmCode = firmCode;
	}

	/** 是否打印中文收单行 */
	public String getIsPrintCHAcqBank() {
		return isPrintCHAcqBank;
	}

	public void setIsPrintCHAcqBank(String isPrintCHAcqBank) {
		this.isPrintCHAcqBank = isPrintCHAcqBank;
	}

	/** 是否打印中文发卡行 */
	public String getIsPrintCHISSBank() {
		return isPrintCHISSBank;
	}

	public void setIsPrintCHISSBank(String isPrintCHISSBank) {
		this.isPrintCHISSBank = isPrintCHISSBank;
	}

	/** 是否打印英文签购单 */
	public String getIsPrintEHSelesSlip() {
		return isPrintEHSelesSlip;
	}

	public void setIsPrintEHSelesSlip(String isPrintEHSelesSlip) {
		this.isPrintEHSelesSlip = isPrintEHSelesSlip;
	}

	/** 最大交易笔数 */
	public String getMaxTrans() {
		return maxTrans;
	}

	public void setMaxTrans(String maxTrans) {
		this.maxTrans = maxTrans;
	}

	/** 交易重发次数 */
	public String getTransRepeatCount() {
		return transRepeatCount;
	}

	public void setTransRepeatCount(String transRepeatCount) {
		this.transRepeatCount = transRepeatCount;
	}

	public void save() {
		String FullName = getROOT_FILE_PATH() + ConfigPath;
		try {
			File file = new File(FullName);
			if (file.exists())
				file.delete();
			FileUtil.object2File(configManager, FullName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 0 1 2 0正常 1已发 2已存交易记录
	 * @return
	 */
	public int getFlag() {
		return transFlag;
	}

	/** 增加标记 **/
	public void increFlag() {
		if (transFlag != 2){
			transFlag++;
		}else{
			transFlag = 0;
		}
		save();
	}

	public void clearFlag() {
		transFlag = 0;
		save();
	}
}