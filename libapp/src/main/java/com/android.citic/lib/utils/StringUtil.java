package com.android.citic.lib.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * String工具类
 */
public class StringUtil {

	/**
	 * 非空判断
	 * @param str
	 * @return String
	 */
	public static boolean isNullWithTrim(String str) {
		return str == null || str.trim().equals("")||str.trim().equals("null");
	}

	/**
	 * 给字符串加*
	 * @param cardNo 卡号
	 * @param prefix 保留 前几位
	 * @param suffix 保留 后几位
	 * @return 加*后的 String
	 */
	public static String getSecurityNum(String cardNo, int prefix, int suffix) {
		StringBuffer cardNoBuffer = new StringBuffer();
		int len = prefix + suffix;
		if ( cardNo.length() > len) {
			cardNoBuffer.append(cardNo.substring(0, prefix));
			for (int i = 0; i < cardNo.length() - len; i++) {
				cardNoBuffer.append("*");
			}
			cardNoBuffer.append(cardNo.substring(cardNo.length() - suffix, cardNo.length()));
		}
		return cardNoBuffer.toString();
	}

	public static String TwoWei(double s){
		DecimalFormat df = new DecimalFormat("0.00");
		return df.format(s);
	}

	/**
	 * 金额存的时候*100  所有取的时候要/100   格式转换
	 * @param amount
	 * @return  0.00格式
	 */
	public static String TwoWei(String amount){
		DecimalFormat df = new DecimalFormat("0.00");
		double d = 0;
		if(!StringUtil.isNullWithTrim(amount))
			d = Double.parseDouble(amount)/100;
		return df.format(d);
	}
	
	/**
	 * 
	 * @param date   20160607152954
	 * @param oldPattern  yyyyMMddHHmmss
	 * @param newPattern yyyy-MM-dd HH:mm:ss
	 * @return 2016-06-07 15:29:54
	 */
	public static String StringPattern(String date, String oldPattern,
									   String newPattern) {
		if (date == null || oldPattern == null || newPattern == null)
			return "";
		SimpleDateFormat sdf1 = new SimpleDateFormat(oldPattern); // 实例化模板对象
		SimpleDateFormat sdf2 = new SimpleDateFormat(newPattern); // 实例化模板对象
		Date d = null;
		try {
			d = sdf1.parse(date); // 将给定的字符串中的日期提取出来
		} catch (Exception e) { // 如果提供的字符串格式有错误，则进行异常处理
			e.printStackTrace(); // 打印异常信息
		}
		return sdf2.format(d);
	}
}
