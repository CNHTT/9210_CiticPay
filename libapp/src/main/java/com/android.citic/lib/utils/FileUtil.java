package com.android.citic.lib.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/***
 * 序列化文件工具类
 * 将对象和二进制文件类型相互转换
 */
public class FileUtil {

	/**
	 * 文件转化为Object
	 * @param fileName
	 * @return byte[]
	 */
	public static Object file2Object(String fileName) throws IOException,ClassNotFoundException {
		File file = new File(fileName);
		if (!file.exists())
			return null;
		FileInputStream fis = new FileInputStream(fileName);
		ObjectInputStream ois = new ObjectInputStream(fis) ;
		Object object = ois.readObject();
		if (fis != null) {
			fis.close();
		}
		if (ois != null) {
			ois.close();
		}
		return object;
	}

	/**
	 * 把Object输出到文件
	 * @param obj
	 * @param outputFile
	 */
	public static void object2File(Object obj, String outputFile) throws IOException {
		File dir = new File(outputFile);
		if (!dir.exists()) {
			// 在指定的文件夹中创建文件
			dir.createNewFile();
		}
		FileOutputStream fos = new FileOutputStream(dir) ;
		ObjectOutputStream oos = new ObjectOutputStream(fos) ;
//		fos = context.openFileOutput(dir.getName() , Context.MODE_WORLD_READABLE);
		oos.writeObject(obj);
		oos.flush();
		fos.getFD().sync();
		if (oos != null) {
			oos.close();
		}
		if (fos != null) {
			fos.close();
		}
	}
}
