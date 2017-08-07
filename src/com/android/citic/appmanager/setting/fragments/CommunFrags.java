package com.android.citic.appmanager.setting.fragments;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.nfc.NdefRecord;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.android.citic.R;
import com.android.citic.lib.ipview.IPEditText;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.global.GlobalCfg;


/**
 * Created by zhouqiang on 2017/3/10.
 */

public class CommunFrags implements View.OnClickListener{

    private Activity mActivity = null ;
    private RelativeLayout rLayout = null ;

    private IPEditText pubIpEdit ;
    private EditText pubPorEdit ;
    private IPEditText inIpEdit ;
    private EditText inPorEdit ;
    private Spinner spinner ;
    private ToggleButton pubSwitch ;

    private GlobalCfg cfg ;

    private int timeout ;

    public CommunFrags(Activity c , RelativeLayout l , String title){
        this.mActivity = c ;
        this.rLayout = l ;
        rLayout.removeAllViews();
        rLayout.inflate(mActivity, R.layout.setting_frag_comun, rLayout);
        ((TextView)rLayout.findViewById(R.id.setting_title_tv)).setText(title);
        rLayout.findViewById(R.id.setting_save).setOnClickListener(this);
        pubIpEdit = (IPEditText) rLayout.findViewById(R.id.setting_public_ip);
        pubPorEdit = (EditText) rLayout.findViewById(R.id.setting_public_port);
        inIpEdit = (IPEditText) rLayout.findViewById(R.id.setting_inner_ip);
        inPorEdit = (EditText) rLayout.findViewById(R.id.setting_inner_port);
        spinner = (Spinner) rLayout.findViewById(R.id.setting_commun_timeout);
        pubSwitch = (ToggleButton) rLayout.findViewById(R.id.setting_public_switch);
        cfg = GlobalCfg.getInstance() ;
        readHistory();
    }

    private void readHistory(){
        pubIpEdit.setIPText(getIPArray(cfg.getIp()));
        pubPorEdit.setText(cfg.getPort());
        inIpEdit.setIPText(getIPArray(cfg.getIP2()));
        inPorEdit.setText(cfg.getPort2());
        pubSwitch.setChecked(cfg.getPubCommun());
        String[] array = mActivity.getResources().getStringArray(R.array.commun_timeout);
        ArrayAdapter adapter = new ArrayAdapter(mActivity , android.R.layout.simple_spinner_dropdown_item , array);
        spinner.setAdapter(adapter);
        spinner.setSelection((cfg.getTimeout()/1000/30)-1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                timeout = (i + 1) * 30 * 1000 ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private String[] getIPArray(String ip){
        String[] iparray = new String[4] ;
        iparray[0] = ip.substring(0 , ip.indexOf(".") ) ;
        String temp = ip.substring(iparray[0].length()+1 , ip.length()) ;
        iparray[1] = temp.substring(0 , temp.indexOf(".")) ;
        temp = temp.substring(iparray[1].length()+1 , temp.length());
        iparray[2] = temp.substring(0 , temp.indexOf(".")) ;
        iparray[3] = temp.substring(iparray[2].length()+1 , temp.length());
        return iparray ;
    }

    @Override
    public void onClick(View view) {
        if(R.id.setting_save == view.getId()){
            save();
        }
    }

    private void save(){
        String ip = pubIpEdit.getIPText() ;
        String port = pubPorEdit.getText().toString() ;
        String ip2 = inIpEdit.getIPText() ;
        String port2 = inPorEdit.getText().toString() ;
        if(StringUtil.isNullWithTrim(ip)||StringUtil.isNullWithTrim(port)
                ||StringUtil.isNullWithTrim(ip2)||StringUtil.isNullWithTrim(port2)){
            AppToast.show(mActivity , R.string.setting_input_is_null);
        }else {
            cfg.setIp(ip);
            cfg.setIp2(ip2);
            cfg.setPort(port);
            cfg.setPort2(port2);
            cfg.setTimeout(timeout);
            cfg.setPubCommun(pubSwitch.isChecked()?true:false);
            cfg.save();
            AppToast.show(mActivity , R.string.setting_save_success);
        }
    }
}
