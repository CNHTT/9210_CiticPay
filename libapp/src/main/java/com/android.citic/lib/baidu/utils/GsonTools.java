package com.android.citic.lib.baidu.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class GsonTools {

	public GsonTools() {
		// TODO Auto-generated constructor stub
	}

	/***
	 *用Gson创建json字符串
	 */
	public static String createJsonStrByGson(Object value) {
		Gson gson = new Gson();
		String str = gson.toJson(value);
		return str;
	}
	
	/**
	 * 使用Gson解析Object
	 * 
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> T getObject(String jsonString, Class<T> cls) {
		T t = null;
		Gson gson = new Gson();
		t = gson.fromJson(jsonString, cls);
		return t;
	}

	/***
	 * 使用Gson解析List<Object>
	 * 
	 * @param jsonString
	 * @param cls
	 * @return
	 */
	public static <T> List<T> getObjects(String jsonString, Class<T> cls) {
		List<T> list = new ArrayList<T>();
		Gson gson = new Gson();
		list = gson.fromJson(jsonString, new TypeToken<List<T>>(){}.getType());
		return list;
	}
	
	public static <T> T toList(String Data, TypeToken<T> type) {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.fromJson(Data, type.getType());
    }

	/***
	 * 使用Gson解析List<String>
	 * 
	 * @param jsonString
	 * @return
	 */
	public static List<String> getListString(String jsonString) {
		List<String> list = new ArrayList<String>();
		Gson gson = new Gson();
		list = gson.fromJson(jsonString, new TypeToken<List<String>>() {
		}.getType());
		return list;
	}

	/***
	 * 使用Gson解析List<Map<String, Object>>
	 * 
	 * @param jsonString
	 * @return
	 */
	public static List<Map<String, Object>> getListMap(String jsonString) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Gson gson = new Gson();
		list = gson.fromJson(jsonString,
				new TypeToken<List<Map<String, Object>>>() {
				}.getType());
		return list;
	}

}
