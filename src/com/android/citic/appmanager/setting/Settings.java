package com.android.citic.appmanager.setting;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.CiticPay;
import com.android.citic.appmanager.Citicapp;
import com.android.citic.appmanager.master.BaseActivity;
import com.android.citic.appmanager.setting.fragments.CommunFrags;
import com.android.citic.appmanager.setting.fragments.DeviceFrags;
import com.android.citic.appmanager.setting.fragments.ErrlogFrags;
import com.android.citic.appmanager.setting.fragments.KeysparaFrags;
import com.android.citic.appmanager.setting.fragments.TransparaFrags;
import com.android.citic.lib.centerdialog.CenterDialog;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.transmanager.global.GlobalCfg;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by zhouqiang on 2017/3/9.
 */

public class Settings extends BaseActivity implements AdapterView.OnItemClickListener{

    @Bind(R.id.setting_gridview)
    GridView mGrid ;
    @Bind(R.id.setting_root)
    RelativeLayout rootLayout ;

    private Dialog mDialog ;

    private long mkeyTime = 0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void setting_return(View v){
        init();
    }

    private void init(){
        setContentView(R.layout.settings);
        ButterKnife.bind(this);
        Bundle bundle = getIntent().getExtras() ;
        if(bundle!=null){
            setNaviTitle(bundle.getString(CiticPay.START_TRANS_KEY));
        }
        mGrid.setAdapter(formatAdapter());
        mGrid.setOnItemClickListener(this);
        setReturnVisible(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        setReturnVisible(View.VISIBLE);
        super.onResume();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mkeyTime) > 2000) {
                mkeyTime = System.currentTimeMillis();
                AppToast.show(Settings.this , R.string.setting_exit_confirm);
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        setReturnVisible(View.GONE);
        String title = getResources().getStringArray(R.array.setting_items)[i];
        switch (i){
            case 0 :new CommunFrags(this , rootLayout , title);break;
            case 1 :startTranspara();break;
            case 2 :new KeysparaFrags(this , rootLayout , title);break;
            case 3 :changeMaintainPwd();break;
            case 4 :new ErrlogFrags(this , rootLayout , title);break;
            case 5 :new DeviceFrags(this , rootLayout , title);break;
        }
    }

    private static final String MAP_TV = "MAP_TV" ;
    private static final String MAP_IV = "MAP_IV" ;
    private ArrayList<HashMap<String,Object>> list ;

    private ListAdapter formatAdapter(){
        list = new ArrayList<>();
        String[] names = getResources().getStringArray(R.array.setting_items);
        int[] rids = {R.drawable.icon_setting_communication_para,
                R.drawable.icon_setting_trans_para,
                R.drawable.icon_setting_keys_para,
                R.drawable.icon_setting_maintain_key,
                R.drawable.icon_setting_err_logs,
                R.drawable.icon_setting_device_info
        } ;
        for (int i = 0 ; i < names.length ; i++){
            HashMap<String,Object> map = new HashMap<>();
            map.put(MAP_TV , names[i]);
            map.put(MAP_IV , rids[i]);
            list.add(map);
        }
        return new SimpleAdapter(this , list , R.layout.setting_item,
                new String[]{MAP_IV , MAP_TV},new int[]{R.id.setting_item_iv,R.id.setting_item_tv});
    }

    private void changeMaintainPwd(){
        mDialog = CenterDialog.show(this , R.layout.setting_change_maintain_pwd , R.id.setting_changepwd_root_layout);
        final EditText newEdit = (EditText) mDialog.findViewById(R.id.setting_changepwd_edit_new);
        final EditText oldEdit = (EditText) mDialog.findViewById(R.id.setting_changepwd_edit_old);
        TextView confirm = (TextView) mDialog.findViewById(R.id.setting_changepwd_confirm);
        TextView cancel = (TextView) mDialog.findViewById(R.id.setting_changepwd_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
                setReturnVisible(View.VISIBLE);
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String np = newEdit.getText().toString();
                String op = oldEdit.getText().toString();
                if(op.equals(GlobalCfg.getInstance().getSecurePass())){
                    if(np.equals("")||np == null){
                        AppToast.show(Settings.this , R.string.setting_input_is_null);
                    }else {
                        mDialog.dismiss();
                        setReturnVisible(View.VISIBLE);
                        GlobalCfg.getInstance().setSecurePass(np);
                        AppToast.show(Settings.this , R.string.setting_change_maintain_pwd_succ);
                    }
                }else {
                   newEdit.setText("");
                   oldEdit.setText("");
                   AppToast.show(Settings.this , R.string.setting_check_oldpwd_err);
                }
            }
        });
    }

    private void startTranspara(){
        Intent intent = new Intent();
        intent.setClass(this , TransparaFrags.class);
        startActivity(intent);
    }
}
