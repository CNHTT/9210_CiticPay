package com.android.citic.appmanager.setting.fragments;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.pinpad.MasterKeyinfo;
import com.android.citic.transmanager.device.pinpad.PinpadManager;
import com.android.citic.transmanager.global.GlobalCfg;
import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;

/**
 * Created by zhouqiang on 2017/3/10.
 */

public class KeysparaFrags implements View.OnClickListener{

    private Activity mActivity = null ;
    private RelativeLayout rLayout = null ;

    private GlobalCfg cfg = null ;

    private EditText indexEdit ;
    private EditText dataEdit ;
    private RadioButton desButton ;
    private RadioButton smButton ;

    public KeysparaFrags(Activity a , RelativeLayout l , String title){
        this.mActivity = a ;
        this.rLayout = l ;
        rLayout.removeAllViews();
        rLayout.inflate(mActivity , R.layout.setting_frag_keys , rLayout);
        ((TextView)rLayout.findViewById(R.id.setting_title_tv)).setText(title);
        rLayout.findViewById(R.id.setting_save).setOnClickListener(this);
        indexEdit = (EditText) rLayout.findViewById(R.id.setting_mkey_index);
        dataEdit = (EditText) rLayout.findViewById(R.id.setting_mkey_data);
        desButton = (RadioButton) rLayout.findViewById(R.id.setting_mkey_type_des);
        smButton = (RadioButton) rLayout.findViewById(R.id.setting_mkey_type_sm);
        cfg = GlobalCfg.getInstance() ;
        readHistory();
    }

    private void readHistory(){
        indexEdit.setText(String.valueOf(cfg.getMasterKeyIndex()));
        desButton.setChecked(true);
        smButton.setChecked(false);
    }

    @Override
    public void onClick(View view) {
        if(R.id.setting_save == view.getId()){
            save();
        }
    }

    private void save(){
        String index = indexEdit.getText().toString();
        String data = dataEdit.getText().toString();
        if(StringUtil.isNullWithTrim(index)||StringUtil.isNullWithTrim(data)){
            AppToast.show(mActivity , R.string.setting_input_is_null);
        }else {
            KeySystem ks = desButton.isChecked()? KeySystem.MS_DES: KeySystem.MS_SM4 ;
            int idx = Integer.parseInt(index);
            if(data.length() == 32){
                cfg.setMasterKeyIndex(idx);
                cfg.save();
                MasterKeyinfo masterKeyinfo = new MasterKeyinfo();
                masterKeyinfo.setKeySystem(ks);
                masterKeyinfo.setKeyType(KeyType.KEY_TYPE_MASTK);
                masterKeyinfo.setMasterIndex(idx);
                masterKeyinfo.setPlainKeyData(ISOUtil.str2bcd(data , false));
                int ret = PinpadManager.getInstance().loadMKey(masterKeyinfo);
                Logger.debug("load mk ret = "+ret);
                if(0 == ret){
                    AppToast.show(mActivity , R.string.setting_save_success);
                }else {
                    AppToast.show(mActivity , mActivity.getResources().getString
                            (R.string.setting_mkey_unknow_err)+"["+ret+"]");
                }
            }else {
                AppToast.show(mActivity , R.string.setting_mkey_len_err);
            }
        }
    }
}
