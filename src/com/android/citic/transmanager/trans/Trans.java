package com.android.citic.transmanager.trans;

import android.content.Context;


import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.AssetsUtil;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.NetworkUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.lib.utils.TypeUtils;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.process.EmvTransaction;
import com.android.citic.transmanager.process.QpbocTransaction;
import com.android.citic.transmanager.trans.helper.iso8583.ISO8583;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.presenter.TransUI;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public abstract class Trans {
	protected Context context;
	protected ISO8583 iso8583; // 8583组包解包
	protected NetworkUtil netWork; // 网络操作对象
	protected TransLog transLog;// 交易记录的集合
	protected GlobalCfg cfg;//配置文件操作实例
	protected TransUI transUI ;//MODEL与VIEW层接口实例
	protected int timeout ;//等待页面超时时间
	protected int retVal ; // 返回
	protected EmvTransaction emv ;// EMV流程控制实例
	protected QpbocTransaction qpboc ;//QPBOC流程控制实例
	protected TransInputPara para ;//交易相关参数集合

	/** 全局表示是否正在打印，通过这个表示判断二次打印无效并且无法退出当前打印的activity **/
	public static boolean printFlag = false;

	public interface Type{
		public static final String SALE = "SALE" ;
		public static final String ENQUIRY = "ENQUIRY" ;
		public static final String VOID = "VOID" ;
		public static final String EC_ENQUIRY = "EC_ENQUIRY" ;
		public static final String QUICKPASS = "QUICKPASS" ;
		public static final String SETTLE = "SETTLE" ;
		public static final String LOGON = "LOGON" ;
		public static final String LOGOUT = "LOGOUT" ;
		public static final String DOWNPARA = "DOWNPARA" ;
		public static final String BARCODE = "BARCODE" ;
	}

	/** 报文域定义 */
	protected String MsgID; // 0
	protected String Pan; // 2*
	protected String ProcCode; // 3
	protected long Amount; // 4*
	protected String TraceNo;// 11域交易流水号
	protected String LocalTime; // 12 hhmmss*
	protected String LocalDate; // 13 MMDD*
	protected String ExpDate; // 14 YYMM*
	protected String SettleDate; // 15 MMDD*
	protected String EntryMode; // 22*
	protected String PanSeqNo; // 23*
	protected String SvrCode; // 25
	protected String CaptureCode; // 26
	protected String AcquirerID; // 32*
	protected String Track1;
	protected String Track2; // 35
	protected String Track3; // 36
	protected String RRN; // 37*
	protected String AuthCode; // 38*
	protected String RspCode; // 39 正常交易不存冲正具体设置
	protected String TermID; // 41
	protected String MerchID; // 42
	protected String Field44; // 44 *
	protected String Field48; // 48 *
	protected String CurrencyCode; // 49*
	protected String PIN; // 52
	protected String SecurityInfo; // 53
	protected String ExtAmount; // 54
	protected byte[] ICCData; // 55*
	protected String Field60; // 60
	protected String Field61; // 61
	protected String Field62; // 62
	protected String Field63; // 63
	protected String TransCName; // 交易中文名
	protected String TransEName; // 交易英文名 主键 交易初始化设置
	protected String BatchNo;// 批次号 60_2
	protected boolean isTraceNoInc = false; // 流水号是否自增
	protected boolean isFallBack; // 是否允许IC卡降级为磁卡
	protected boolean isUseOrgVal = false; // 使用原交易的第3域和 60.1域

	protected String F60_1;
	protected String F60_3;

	public static final int ENTRY_MODE_HAND = 1; // 手动
	public static final int ENTRY_MODE_MAG = 2; // 刷卡
	public static final int ENTRY_MODE_ICC = 5; // IC卡
	public static final int ENTRY_MODE_NFC = 7; // NFC
	public static final int ENTRY_MODE_BARCODE = 9;

	/** 消费 55域数据 **/
	public static final int wOnlineTags[] = { 0x9F26, // AC (Application Cryptogram)
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
			0x4F,

			0x5F34, // PAN Sequence Number
			0x50,//应用标签
			0 };
	// 0X8E, //CVM

	/** 发卡脚本结果上送 55域 **/
	public static final int wISR_tags[] = { 0x9F33, // Terminal Capabilities
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

	/** 冲正 **/
	public static final int reversal_tag[] = { 0x95, // TVR
			0x9F1E, // IFD Serial Number
			0x9F10, // Issuer Application Data
			0x9F36, // Application Transaction Counter
			0xDF31, // 发卡行脚本结果
			0 };

	// hhmmss*
	protected String getLocalTime() {
		return DateToStr(new Date(), "HHmmss");
	}

	// 13 MMDD*
	protected String getLocalDate() {
		return DateToStr(new Date(), "MMdd");
	}

	 //14 YYMM*
	 protected String getExpDate(){
	 	return DateToStr(new Date(),"YYMM");
	 }

	/**
	 * 日期转换成字符串
	 * @param date
	 * @return str
	 */
	public static String DateToStr(Date date, String formatString) {
		String str = null;
		try {
			SimpleDateFormat format = new SimpleDateFormat(formatString);// formatString
			str = format.format(date);
		} catch (Exception e) {
		}
		return str;
	}

	/***
	 * Trans 构造
	 * @param ctx
	 * @param transEname
	 */
	public Trans(Context ctx, String transEname) {
		this.context = ctx;
		this.TransEName = transEname;
		loadConfig();
		transLog = TransLog.getInstance();
	}

	//加载初始设置
	private void loadConfig() {
		cfg = GlobalCfg.getInstance();
		TermID = cfg.getTermID();
		MerchID = cfg.getMerchID();
		CurrencyCode = cfg.getCurrencyCode();
		BatchNo = ISOUtil.padleft("" + cfg.getBatchNo(), 6, '0');
		TraceNo = ISOUtil.padleft("" + cfg.getTraceNo(), 6, '0');
		boolean isPub = cfg.getPubCommun() ;
		String ip = isPub?cfg.getIp():cfg.getIP2();
		int port = TypeUtils.Object2Int(isPub?cfg.getPort():cfg.getPort2());
		int timeout = cfg.getTimeout();
		String tpdu = cfg.getTpdu();
		String header = cfg.getHeader();
		if(!TransEName.equals(Type.EC_ENQUIRY) && !TransEName.equals(Type.QUICKPASS)){
			setFixedDatas();
		}
		netWork = new NetworkUtil(ip, port, timeout, context);
		iso8583 = new ISO8583(this.context, tpdu, header);
	}

	//设置消息类型及60域3个子域数据
	protected void setFixedDatas() {
		Logger.debug("Trans>>setFixedDatas>>设置消息类型及60域3个子域数据");
		if (null == TransEName)
			return;
		Properties pro = AssetsUtil.lodeConfig(context, "props/trans.properties");
		if (pro == null) {
			return;
		}
		String prop = pro.getProperty(TransEName);
		String[] propGroup = prop.split(",");
		if (!StringUtil.isNullWithTrim(propGroup[0])){
			MsgID = propGroup[0];
		}else{
			MsgID = null;
		}
		Logger.debug("Trans>>setFixedDatas>>MsgID="+MsgID);
		if (isUseOrgVal == false) {
			if (!StringUtil.isNullWithTrim(propGroup[1])){
				ProcCode = propGroup[1];
			}else{
				ProcCode = null;
			}
		}
		Logger.debug("Trans>>setFixedDatas>>ProcCode="+ProcCode);
		if (!StringUtil.isNullWithTrim(propGroup[2])){
			SvrCode = propGroup[2];
		}else{
			SvrCode = null;
		}
		Logger.debug("Trans>>setFixedDatas>>SvrCode="+SvrCode);
		if (isUseOrgVal == false) {
			if (!StringUtil.isNullWithTrim(propGroup[3])){
				F60_1 = propGroup[3];
			}else{
				F60_1 = null;
			}
		}
		Logger.debug("Trans>>setFixedDatas>>F60_1="+F60_1);
		if (!StringUtil.isNullWithTrim(propGroup[4])) {
			F60_3 = propGroup[4];
		}else {
			F60_3 = null;
		}
		Logger.debug("Trans>>setFixedDatas>>F60_3="+F60_3);
		if (F60_1 != null && F60_3 != null){
			Field60 = F60_1 + cfg.getBatchNo() + F60_3;
		}
		Logger.debug("Trans>>setFixedDatas>>Field60="+Field60);
		try {
			TransCName = new String(propGroup[5].getBytes("ISO-8859-1"), "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Logger.debug("Trans>>setFixedDatas>>TransCName="+TransCName);
	}

	//获取流水号是否自增
	public boolean isTraceNoInc() {
		return isTraceNoInc;
	}

	//设置流水号是否自增
	public void setTraceNoInc(boolean isTraceNoInc) {
		this.isTraceNoInc = isTraceNoInc;
	}

	//追加60域内容
	protected void appendField60(String f60) {
		Field60 = Field60 + f60;
	}

	// 连接
	protected int connect() {
		return netWork.Connect();
	}

	// 发送
	protected int send() {
		byte[] pack = iso8583.packetISO8583();
		if (pack == null)
			return -1;
		Logger.debug("交易:"+TransEName+"\n发送报文:"+ISOUtil.hexString(pack));
		return netWork.Send(pack);
	}

	// 接收
	protected byte[] recive() {
		byte[] recive = null;
		try {
			recive = netWork.Recive(2048, 0);
		} catch (IOException e) {
			return null;
		}
		if(recive!=null){
			Logger.debug("交易:"+TransEName+"\n接收报文:"+ISOUtil.hexString(recive));
		}
		return recive;
	}

	//联机处理
	protected int OnLineTrans() {
		if (connect() == -1)
			return Tcode.T_socket_err;
		if (send() == -1)
			return Tcode.T_send_err;
		byte[] respData = recive();
		netWork.close();
		if (respData == null)
			return Tcode.T_receive_err;

		int ret = iso8583.unPacketISO8583(respData);// 解包
		// 交易处理
		if (ret == 0) {
			// 合法处理
			if (isTraceNoInc) {
				Logger.debug("流水号自增");
				cfg.incTraceNo();
			}
		}
		return ret;
	}

	/** 清除关键信息 **/
	protected void clearPan() {
		Pan = null;
		Track2 = null;
		Track3 = null;
		System.gc();//显示调用清除内存
	}
}
