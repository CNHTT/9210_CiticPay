package com.android.citic.lib.toastview;

import android.app.Activity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.libapp.R;

public final class AppToast {
	
	public static void show(Activity activity , int content){
		LayoutInflater inflater_3 = activity.getLayoutInflater();
		View view_3 = inflater_3.inflate(R.layout.app_toast,(ViewGroup) activity.findViewById(R.id.toast_layout));
		ImageView face = (ImageView) view_3.findViewById(R.id.app_t_iv);
		if(activity.getResources().getString(content).contains("SAVE SUCCESS")){
			face.setBackgroundResource(R.drawable.face_laugh);
		}else {
			face.setBackgroundResource(R.drawable.face_cry);
		}
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(view_3);
		((TextView)view_3.findViewById(R.id.toast_tv)).setText(activity.getResources().getString(content));
		toast.show();
	}
	
	public static void show(Activity activity , String str){
		LayoutInflater inflater_3 = activity.getLayoutInflater();
		View view_3 = inflater_3.inflate(R.layout.app_toast,(ViewGroup) activity.findViewById(R.id.toast_layout));
		ImageView face = (ImageView) view_3.findViewById(R.id.app_t_iv);
		if(str.contains("SUCCESS") || str.contains("VERIFY OFFLINE PIN SUCCESS")){
			face.setBackgroundResource(R.drawable.face_laugh);
		}else {
			face.setBackgroundResource(R.drawable.face_cry);
		}
		Toast toast = new Toast(activity);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(view_3);
		((TextView)view_3.findViewById(R.id.toast_tv)).setText(str);
		toast.show();
	}
}
