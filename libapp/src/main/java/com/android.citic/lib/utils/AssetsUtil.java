package com.android.citic.lib.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Assets文件工具类
 */
public class AssetsUtil {
	/**
	 * 获取Assets配置信息
	 * @param context 上下问对象
	 * @param fildName 配置文件名
	 * @return Properties
	 */
	public static Properties lodeConfig(Context context, String fildName) {
		Properties prop = new Properties();
		try {
			prop.load(context.getResources().getAssets().open(fildName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return prop;
	}

	/**
	 * 根据配置文件名和配置属性获取值
	 * @param context 上下文对象
	 * @param fildName 配置文件名
	 * @param name 配置的属性名
	 * @return 对应文件对应属性的String
	 */
	public static String lodeConfig(Context context, String fildName, String name) {
		Properties pro = new Properties();
		try {
			pro.load(context.getResources().getAssets().open(fildName));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		return (String) pro.get(name);
	}

	/**
	 * 通过code值获取银行名字，获取失败时返回code
	 * @param mContext
	 * @param code
	 * @return
	 */
	public String getBankName(Context mContext , String code) {
		Properties pro = AssetsUtil.lodeConfig(mContext, "bankcodelist.properties");
		if (pro == null) {
			System.out.println("bankcodelist.properties配置文件错误");
			return null;
		}
		String bname ;
		try {
			if (!StringUtil.isNullWithTrim(pro.getProperty(code)))
				bname = new String(pro.getProperty(code).getBytes("ISO-8859-1"), "utf-8");
			else
				return code;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return code;
		}
		return bname;
	}

	/**
	 * 通过错误code获取具体错误，获取失败时返回code
	 * @param mcontext
	 * @param code
	 * @return
	 */
	public static String getRspCode(Context mcontext, String code) {
		String tiptitle ;
		String tipcontent ;
		Properties pro = AssetsUtil.lodeConfig(mcontext, "props/rspcode.properties");
		if (pro == null) {
			System.out.println("rspcode.properties配置文件错误");
			return null;
		}
		try {
			String prop = pro.getProperty(code);
			String[] propGroup = prop.split(",");
			if (!StringUtil.isNullWithTrim(propGroup[0]))
				tiptitle = new String(propGroup[0].trim().getBytes("ISO-8859-1"), "utf-8");
			else
				tiptitle = code;
			if (!StringUtil.isNullWithTrim(propGroup[1]))
				tipcontent = new String(propGroup[1].trim().getBytes("ISO-8859-1"), "utf-8");
			else
				tipcontent = "";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return code;
		}
		return tiptitle + "\n" + tipcontent;
	}

	/**
	 * 将assets下文件拷贝至本应用程序data下
	 * @param context 上下文对象
	 * @param fileName assets文件名称
     * @return
     */
	public static boolean copyAssetsToData(Context context , String fileName) {
		try {
			AssetManager as = context.getAssets();
			InputStream ins = as.open(fileName);
			String dstFilePath = context.getFilesDir().getAbsolutePath() + "/" + fileName;
			OutputStream outs = context.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
			byte[] data = new byte[1 << 20];
			int length = ins.read(data);
			outs.write(data, 0, length);
			ins.close();
			outs.flush();
			outs.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 获取bundle参数列表
	 * @param c
	 * @param name
	 * @param proName
     * @return
     */
	public static String[] getProps(Context c , String name, String proName) {
		Properties pro = new Properties();
		try {
			pro.load(c.getResources().getAssets().open(name));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return null;
		}
		String prop = pro.getProperty(proName);
		if (prop == null)
			return null;
		String[] results = prop.split(",") ;
		for (int i = 0 ; i < results.length ; i++){
			try {
				results[i] = new String(results[i].trim().getBytes("ISO-8859-1"), "utf-8");
			}catch (UnsupportedEncodingException e){
				e.printStackTrace();
			}
		}
		return results;
	}
}
