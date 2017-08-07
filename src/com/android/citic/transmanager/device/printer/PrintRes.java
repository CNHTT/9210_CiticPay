package com.android.citic.transmanager.device.printer;

/**
 * Created by zhouqiang on 2017/4/4.
 */

public class PrintRes {
    public interface CH{
        public static final String WANNING = "警告:该固件是测试版本，不能用于商业用途，在此版本上进行交易可能危害到持卡人的用卡安全。" ;
        public static final String MERCHANT_COPY = "商户存根     MERCHANT COPY" ;
        public static final String CARDHOLDER_COPY = "持卡人存根     CARDHOLDER COPY" ;
        public static final String BANK_COPY = "银行存根     BANK COPY" ;
        public static final String MERCHANT_NAME = "商户名称(MERCHANT NAME):" ;
        public static final String MERCHANT_ID = "商户编号(MERCHANT NO):" ;
        public static final String TERNIMAL_ID = "终端编号(TERMINAL NO):" ;
        public static final String OPERATOR_NO = "操作员号(OPERATOR NO):" ;
        public static final String CARD_NO = "卡号(CARD NO):" ;
        public static final String ISSUER = "发卡行(ISSUER):  中信银行" ;
        public static final String ACQUIRER = "收单行(ACQ):" ;
        public static final String TRANS_AAC = "应用密文(AAC):" ;
        public static final String TRANS_AAC_ARQC = "联机交易" ;
        public static final String TRANS_AAC_TC = "脱机交易" ;
        public static final String TRANS_TYPE = "交易类型(TXN. TYPE):" ;
        public static final String CARD_EXPDATE = "卡有效期(EXP. DATE):" ;
        public static final String BATCH_NO = "批次号(BATCH NO):" ;
        public static final String VOUCHER_NO = "凭证号(VOUCHER NO):" ;
        public static final String AUTH_NO = "授权码(AUTH NO):" ;
        public static final String DATE_TIME = "日期/时间(DATE/TIME):" ;
        public static final String REF_NO = "交易参考号(REF. NO):" ;
        public static final String AMOUNT = "金额(AMOUNT):" ;
        public static final String EC_AMOUNT = "电子现金余额(AMOUNT):" ;
        public static final String CARD_AMOUNT = "卡余额(AMOUNT):" ;
        public static final String RMB = "RMB:" ;
        public static final String REFERENCE = "备注/REFERENCE" ;
        public static final String REPRINT = "***** 重打印 *****" ;
        public static final String CARDHOLDER_SIGN = "持卡人签名" ;
        public static final String AGREE_TRANS = "本人同意以上交易" ;
        public static final String SETTLE_SUMMARY = "结算总计单" ;
        public static final String SETTLE_LIST = "类型/TYPE      笔数/SUM      金额/AMOUNT" ;
        public static final String SETTLE_INNER_CARD = "内卡：对账平" ;
        public static final String SETTLE_OUTER_CARD = "外卡：对账平" ;
        public static final String SETTLE_DETAILS = "结算明细单" ;
        public static final String SETTLE_DETAILS_LIST_CH = "凭证号   类型   授权码   金额   卡号" ;
        public static final String SETTLE_DETAILS_LIST_EN = "VOUCHER     TYPE     AUTHNO     AMOUNT    CARDNO" ;

    }

    public interface EN{
        public static final String WANNING = "WARNING! Developent version of fireware,used for software development only,for commercial purposes is strictly prohibited!" ;
        public static final String MERCHANT_COPY = "         MERCHANT COPY" ;
        public static final String CARDHOLDER_COPY = "          CARDHOLDER COPY" ;
        public static final String BANK_COPY = "         BANK COPY" ;
        public static final String MERCHANT_NAME = "MERCHANT NAME:" ;
        public static final String MERCHANT_ID = "MERCHANT NO:" ;
        public static final String TERNIMAL_ID = "TERMINAL NO:" ;
        public static final String OPERATOR_NO = "OPERATOR NO:" ;
        public static final String CARD_NO = "CARD NO:" ;
        public static final String ISSUER =   "ISSUER:  SZFP" ;
        public static final String ACQUIRER = "ACQ      :  SZFP" ;
        public static final String TRANS_AAC = "AC:" ;
        public static final String TRANS_AAC_ARQC = "ONLINE" ;
        public static final String TRANS_AAC_TC = "OFFLINE" ;
        public static final String TRANS_TYPE = "TXN. TYPE:" ;
        public static final String CARD_EXPDATE = "EXP. DATE:" ;
        public static final String BATCH_NO = "BATCH NO:" ;
        public static final String VOUCHER_NO = "VOUCHER NO:" ;
        public static final String AUTH_NO = "AUTH NO:" ;
        public static final String DATE_TIME = "DATE/TIME:" ;
        public static final String REF_NO = "REF. NO:" ;
        public static final String AMOUNT = "AMOUNT:" ;
        public static final String EC_AMOUNT = "AMOUNT:" ;
        public static final String CARD_AMOUNT = "AMOUNT:" ;
        public static final String RMB = "$" ;
        public static final String REFERENCE = "REFERENCE" ;
        public static final String REPRINT = "       ***** REPRINT *****" ;
        public static final String CARDHOLDER_SIGN = "CARDHOLDER SIGN" ;
        public static final String AGREE_TRANS = "AGREE ABOVE INFO" ;
        public static final String SETTLE_SUMMARY = "SETTLE SUMMARY" ;
        public static final String SETTLE_LIST = "TYPE      SUM      AMOUNT" ;
        public static final String SETTLE_INNER_CARD = "LOCAL   CARD：FLAT" ;
        public static final String SETTLE_OUTER_CARD = "FOREIGN CARD：FLAT" ;
        public static final String SETTLE_DETAILS = "SETTLE DETAILS" ;
        public static final String SETTLE_DETAILS_LIST_CH = "VOUCHER     TYPE     AUTHNO     AMOUNT   CARDNO" ;
        public static final String SETTLE_DETAILS_LIST_EN = "VOUCHER     TYPE     AUTHNO     AMOUNT   CARDNO" ;

    }

    public static final String[] TRANSCH = {
        "余额查询",
        "消费",
        "消费撤销",
        "电子现金余额查询",
        "快速消费",
        "结算",
        "预授权",
        "预授权完成",
        "预授权完成撤销",
        "预授权撤销",
        "预授权完成请求",
        "退货",
    };

    public static final String[] TRANSEN = {
            "ENQUIRY",
            "SALE",
            "VOID",
            "EC_ENQUIRY",
            "QUICKPASS",
            "SETTLE",
            "AUTH",
            "AUTHCOMP",
            "VOIDAUTHCOMP",
            "VOIDAUTH",
            "AUTHCOMPASK",
            "REFUND",
    };
}
