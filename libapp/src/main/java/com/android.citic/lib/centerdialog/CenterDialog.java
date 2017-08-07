package com.android.citic.lib.centerdialog;

import android.app.Dialog;
import android.content.Context;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.example.libapp.R;

/**
 * Created by zhouqiang on 2017/3/10.
 */

public class CenterDialog {

    public static Dialog show(Context mContext , int resID , int root){
        final Dialog pd = new Dialog(mContext, R.style.CenterDialog);
        pd.setContentView(resID);
        LinearLayout layout = (LinearLayout) pd.findViewById(root);
        layout.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.up2down));
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        return pd ;
    }
}
