package com.android.citic.transmanager.trans.helper.iso8583;

/**
 * 定义属性的类型
 */
public class FieldTypesDefine {
	//数据类型的属性
	public static final int FIELDATTR_TYPE_N = 0; 		// N 左补
	public static final int FIELDATTR_TYPE_CN = 1; 		// CN 右补
	public static final int FIELDATTR_TYPE_BIN = 2; 	// Bin
	public static final int FIELDATTR_TYPE_ASCII = 3; 	// ASCII

	//长度的属性
	public static final int FIELDATTR_LEN_TYPE_NO=0;
	public static final int FIELDATTR_LEN_TYPE_LL=1;
	public static final int FIELDATTR_LEN_TYPE_LLL=2;

}
