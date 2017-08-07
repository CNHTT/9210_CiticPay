package com.android.citic.appmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.citic.R;
import com.android.citic.appmanager.trans.PagerItem;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.transmanager.global.GlobalCfg;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhouqiang on 2017/4/12.
 */

public class Operator extends Activity implements View.OnClickListener{
    @Bind(R.id.admin_account)
    EditText account ;
    @Bind(R.id.admin_password)
    EditText pass ;
    @Bind(R.id.admin_in_out)
    Button btn ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.citic_opt);
        ButterKnife.bind(this);
        btn.setOnClickListener(this);
        Citicapp.getInstance().addActivity(this);
    }

    @Override
    public void onClick(View view) {
        String a = account.getText().toString();
        String p = pass.getText().toString();
        if(p.equals("0000")){
            GlobalCfg cfg = GlobalCfg.getInstance();
            cfg.setOprNo(Integer.parseInt(a));
            cfg.save();
            loadSucc();
        }else {
            AppToast.show(this , R.string.operator_pwd_err);
        }
    }

    private void loadSucc(){
        Intent intent = new Intent();
        intent.setClass(this , CiticPay.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            System.gc();
            Citicapp.getInstance().exit();
        }
        return super.onKeyDown(keyCode, event);
    }
}
