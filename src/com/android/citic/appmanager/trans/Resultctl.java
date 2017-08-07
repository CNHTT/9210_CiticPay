package com.android.citic.appmanager.trans;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.CiticPay;
import com.android.citic.appmanager.Operator;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.appmanager.master.BaseActivity;
import com.android.citic.appmanager.master.NavigationConfig;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.trans.helper.translog.TransLog;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhouqiang on 2017/3/16.
 */
@NavigationConfig(titleId = R.string.result_title)
public class Resultctl extends BaseActivity{

    @Bind(R.id.result_detail)
    TextView detail ;
    @Bind(R.id.result_title)
    TextView title ;
    @Bind(R.id.result_face_img)
    ImageView face ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_ctl);
        setReturnVisible(View.INVISIBLE);
        ButterKnife.bind(this);
        Bundle b = getIntent().getExtras() ;
        if(null != b){
            String t = b.getString(Masterctl.TRANS_RESULT_KEY) ;
            title.setText(t);
            Logger.debug("t="+t);
            if(!t.equals("LOGON SUCCESS") && !t.equals("LOGOUT SUCCESS") && !t.equals("SUCCESS")
                    && !t.equals("DOWNLOAD SUCCESS") && !t.equals("LOGOUT SUCCESS")
                    && !t.equals("LOGON AND DOWNLOAD PARAM SUCCESS")){

                title.append("\n"+ StringUtil.TwoWei(
                        TransLog.getInstance().getLastTransLog().
                                getAmount().toString()));
            }if(t.equals("LOGOUT SUCCESS")){
                AppToast.show(this , "SUCCESS");
                Intent intent = new Intent();
                intent.setClass(this , Operator.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer(GlobalCfg.getInstance().getExitTime() * 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
    }
}
