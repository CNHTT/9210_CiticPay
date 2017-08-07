package com.android.citic.transmanager.trans.helper.iso8583;

/**
 * 域属性的格式
 */
public class FieldAttr {
	private int lenType; // 长度的格式 BCD ASCII BIN
	private int lenAttr; // 长度的属性 no LL LLL
	private int dataType; // ASCII (N CN)BCD BIN
	private int dataLen; // 内容的长度

	public int getLenType() {
		return lenType;
	}

	public void setLenType(int lenType) {
		this.lenType = lenType;
	}

	public int getLenAttr() {
		return lenAttr;
	}

	public void setLenAttr(int lenAttr) {
		this.lenAttr = lenAttr;
	}

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

	public int getDataLen() {
		return dataLen;
	}

	public void setDataLen(int dataLen) {
		this.dataLen = dataLen;
	}

}
