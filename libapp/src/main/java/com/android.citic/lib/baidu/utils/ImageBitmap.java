package com.android.citic.lib.baidu.utils;

import java.io.InputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.text.TextUtils;


public class ImageBitmap {	
	
	private static ImageBitmap instance = null;

	public static ImageBitmap getInstance() {
		if (instance == null) {
			synchronized (ImageBitmap.class) {
				if (instance == null) {
					instance = new ImageBitmap();
				}
			}
		}

		return instance;
	}

	private ImageBitmap() {
	}
	
	private final int DEFAULT_SIZE = 150;

	public Bitmap get(String path){
		return get(DEFAULT_SIZE, path);
	}
	
	public Bitmap get(InputStream is){
		return get(DEFAULT_SIZE, is);
	}
	
	public Bitmap get(int resId, Resources res){
		return get(DEFAULT_SIZE, resId, res);
	}
	
	public Bitmap getIcon(Context c ,String path){
		return getIcon(c , DEFAULT_SIZE, path);
	}
	
	public Bitmap get(int size, String path) {
		if (size > 0 && !TextUtils.isEmpty(path)) {
			return BitmapUtil.decodeSampledBitmapFromFile(path, size, size);
		}

		return null;
	}
	
	public Bitmap get(int size, InputStream is){
		if (size > 0 && is != null) {
			return BitmapUtil.decodeSampledBitmapFromStream(is, size, size);
		}

		return null;
	}
	
	public Bitmap get(int size, int resId, Resources res){
		if(size > 0 && resId >= 0){
			return BitmapUtil.decodeSampledBitmapFromResource(res,
					resId, size, size);
		}
		
		return null;
	}
	
	public Bitmap getIcon(Context context ,int size, String path){
		Bitmap bm;
//		/** 是否从asset文件中读取数据*/
//		public static final boolean readAsset = false;
		if (true) {
			bm = get(size, path);
		} else {
			
			try {
				bm = get(size, context.getAssets().open(path));
			} catch (Exception e) {
				System.err.println("open stream fail:" + e);
				return null;
			}
		}
		
		return bm;
	}
}
