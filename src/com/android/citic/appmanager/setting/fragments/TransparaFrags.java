package com.android.citic.appmanager.setting.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.MergeCursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.appmanager.master.BaseActivity;
import com.android.citic.appmanager.master.NavigationConfig;
import com.android.citic.lib.poplist.PopupListView;
import com.android.citic.lib.poplist.PopupView;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.global.PrinterTicknum;
import com.android.citic.transmanager.trans.Trans;

import java.util.ArrayList;
import java.util.FormatFlagsConversionMismatchException;

import javax.crypto.Mac;

/**
 * Created by zhouqiang on 2017/3/13.
 */

public class TransparaFrags extends Activity{

    private PopupListView popupListView ;
    private ArrayList<PopupView> popupViews = new ArrayList<>();

    private Context mContext ;

    private GlobalCfg cfg ;

    private PopupView metchant ;
    private PopupView trans ;
    private PopupView master ;
    private PopupView password ;
    private PopupView carduse ;
    private PopupView contactlessuse ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_frag_transaction);
        cfg = GlobalCfg.getInstance();
        mContext = TransparaFrags.this ;
        popupListView = (PopupListView) findViewById(R.id.setting_trans_poplist);
        loadViews();
        initPopupViews();
    }

    @Override
    public void onBackPressed() {
        if (popupListView.isItemZoomIn()) {
            popupListView.zoomOut();
        } else {
            super.onBackPressed();
        }
    }

    private void initPopupViews() {
        popupViews.add(metchant);
        popupViews.add(trans);
        popupViews.add(master);
        popupViews.add(password);
        popupViews.add(carduse);
        popupViews.add(contactlessuse);
        popupListView.init(null);
        popupListView.setItemViews(popupViews);
    }

    public void setting_return(View v){
        if (popupListView.isItemZoomIn()) {
            popupListView.zoomOut();
        }
    }

    private void loadViews(){
       metchant = new PopupView(mContext , R.layout.setting_frag_trans_poplist_item_head){
            @Override
            public void setViewsElements(View view) {
                TextView textView = (TextView) view.findViewById(R.id.setting_trans_poplist_tv);
                textView.setText(mContext.getResources().getString(R.string.setting_trans_merchant_info));
            }

            @Override
            public View setExtendView(View view) {
                View extendView;
                if (view == null) {
                    extendView = LayoutInflater.from(mContext).
                            inflate(R.layout.setting_frag_trans_item_merchant, null);
                    readMerchantInfo(extendView);
                } else {
                    extendView = view;
                }
                return extendView ;
            }
        };

       trans =  new PopupView(mContext, R.layout.setting_frag_trans_poplist_item_head) {
            @Override
            public void setViewsElements(View view) {
                TextView textView = (TextView) view.findViewById(R.id.setting_trans_poplist_tv);
                textView.setText(mContext.getResources().getString(R.string.setting_trans_transaction_para));
            }

            @Override
            public View setExtendView(View view) {
                View extendView;
                if (view == null) {
                    extendView = LayoutInflater.from(mContext).
                            inflate(R.layout.setting_frag_trans_item_sys, null);
                    readTransInfo(extendView);
                } else {
                    extendView = view;
                }
                return extendView;
            }
        };

       master =  new PopupView(mContext, R.layout.setting_frag_trans_poplist_item_head) {
            @Override
            public void setViewsElements(View view) {
                TextView textView = (TextView) view.findViewById(R.id.setting_trans_poplist_tv);
                textView.setText(mContext.getResources().getString(R.string.setting_master_change_pwd));
            }

            @Override
            public View setExtendView(View view) {
                View extendView;
                if (view == null) {
                    extendView = LayoutInflater.from(mContext).
                            inflate(R.layout.setting_frag_trans_item_master, null);
                    readMasterInfo(extendView);
                } else {
                    extendView = view;
                }
                return extendView;
            }
        };

       password =  new PopupView(mContext, R.layout.setting_frag_trans_poplist_item_head) {
            @Override
            public void setViewsElements(View view) {
                TextView textView = (TextView) view.findViewById(R.id.setting_trans_poplist_tv);
                textView.setText(mContext.getResources().getString(R.string.setting_trans_input_password));
            }

            @Override
            public View setExtendView(View view) {
                View extendView;
                if (view == null) {
                    extendView = LayoutInflater.from(mContext).
                            inflate(R.layout.setting_frag_trans_item_password, null);
                    readPasswordSwitch(extendView);
                } else {
                    extendView = view;
                }
                return extendView;
            }
        };

      carduse =  new PopupView(mContext, R.layout.setting_frag_trans_poplist_item_head) {
            @Override
            public void setViewsElements(View view) {
                TextView textView = (TextView) view.findViewById(R.id.setting_trans_poplist_tv);
                textView.setText(mContext.getResources().getString(R.string.setting_trans_use_card));
            }

            @Override
            public View setExtendView(View view) {
                View extendView;
                if (view == null) {
                    extendView = LayoutInflater.from(mContext).
                            inflate(R.layout.setting_frag_trans_item_carduse, null);
                    readCarduseSwitch(extendView);
                } else {
                    extendView = view;
                }
                return extendView;
            }
        };

        //edit by liyo
        contactlessuse =  new PopupView(mContext, R.layout.setting_frag_trans_poplist_item_head) {
            @Override
            public void setViewsElements(View view) {
                TextView textView = (TextView) view.findViewById(R.id.setting_trans_poplist_tv);
                textView.setText(mContext.getResources().getString(R.string.setting_trans_use_contactless));
            }

            @Override
            public View setExtendView(View view) {
                View extendView;
                if (view == null) {
                    extendView = LayoutInflater.from(mContext).
                            inflate(R.layout.setting_frag_trans_item_contactlessuse, null);
                    readContactlessSwitch(extendView);
                } else {
                    extendView = view;
                }
                return extendView;
            }
        };
    }

    private void readMerchantInfo(View v){
        final EditText merid = (EditText) v.findViewById(R.id.setting_merchant_merid);
        final EditText mername = (EditText) v.findViewById(R.id.setting_merchant_mername);
        final EditText terid = (EditText) v.findViewById(R.id.setting_merchant_terid);
        merid.setText(cfg.getMerchID());
        mername.setText(cfg.getMerchName());
        terid.setText(cfg.getTermID());
        v.findViewById(R.id.setting_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mid = merid.getText().toString();
                String mname = mername.getText().toString();
                String tid = terid.getText().toString();
                if(StringUtil.isNullWithTrim(mid)||StringUtil.isNullWithTrim(mname)||StringUtil.isNullWithTrim(tid)){
                    AppToast.show(TransparaFrags.this , R.string.setting_input_is_null);
                }else {
                    cfg.setMerchID(mid);
                    cfg.setMerchName(mname);
                    cfg.setTermID(tid);
                    AppToast.show(TransparaFrags.this , R.string.setting_save_success);
                }
            }
        });
    }

    private void readTransInfo(View v){
        final EditText traceEdit = (EditText) v.findViewById(R.id.setting_trans_trace_edit);
        final Spinner printSp = (Spinner) v.findViewById(R.id.setting_trans_print_spinner);
        final EditText tpduEdit = (EditText) v.findViewById(R.id.setting_trans_tupu_edit);
        final Spinner exitSp = (Spinner) v.findViewById(R.id.setting_trans_exit_time);
        final EditText batchEdit = (EditText) v.findViewById(R.id.setting_trans_batch_edit);
        final EditText maxEdit = (EditText) v.findViewById(R.id.setting_trans_mac_edit);
        final EditText firmEdit = (EditText) v.findViewById(R.id.setting_trans_firm_edit);
        final Spinner trackSp = (Spinner) v.findViewById(R.id.setting_trans_track_encrypt);
        final Spinner waitSP = (Spinner) v.findViewById(R.id.setting_trans_wait_user_time);
        final EditText reversal = (EditText) v.findViewById(R.id.setting_trans_reversal_count);
        setAdaper(trackSp , R.array.onoff);
        trackSp.setSelection(cfg.isTrackEncrypt()?0:1);

        setAdaper(printSp , R.array.print_num);
        printSp.setSelection(cfg.getPrinterTickNumber()-1 , true);
        printSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        setAdaper(exitSp , R.array.exit_time);
        exitSp.setSelection( (cfg.getExitTime() / 5) - 1 , true);
        exitSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        setAdaper(waitSP , R.array.wait_time);
        waitSP.setSelection((cfg.getWaitUserTime()/30)-1 , true);
        waitSP.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        traceEdit.setText(cfg.getTraceNo());
        tpduEdit.setText(cfg.getTpdu());
        batchEdit.setText(cfg.getBatchNo());
        maxEdit.setText(cfg.getMaxTrans());
        firmEdit.setText(cfg.getFirmCode());
        reversal.setText(String.valueOf(cfg.getReversalCount()));

        v.findViewById(R.id.setting_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String trace = traceEdit.getText().toString();
                String tpdu = tpduEdit.getText().toString();
                String batch = batchEdit.getText().toString();
                String max = maxEdit.getText().toString();
                String firm = firmEdit.getText().toString();
                String rever = reversal.getText().toString();
                if(StringUtil.isNullWithTrim(trace)||StringUtil.isNullWithTrim(tpdu)||StringUtil.isNullWithTrim(batch)||
                        StringUtil.isNullWithTrim(max)||StringUtil.isNullWithTrim(firm)||StringUtil.isNullWithTrim(rever)){
                    AppToast.show(TransparaFrags.this , R.string.setting_input_is_null);
                }else {
                    cfg.setTraceNo(Integer.parseInt(trace));
                    cfg.setBatchNo(Integer.parseInt(batch));
                    cfg.setTpdu(tpdu);
                    cfg.setMaxTrans(max);
                    cfg.setFirmCode(firm);
                    cfg.setReversalCount(Integer.parseInt(rever));
                    cfg.setPrinterTickNumber(printSp.getSelectedItemPosition()+1);
                    cfg.setExitTime((exitSp.getSelectedItemPosition()+1)*5);
                    cfg.setWaitUserTime((waitSP.getSelectedItemPosition()+1)*30);
                    cfg.setTrackEncrypt(trackSp.getSelectedItemPosition()==0?true:false);
                    cfg.save();
                    AppToast.show(TransparaFrags.this , R.string.setting_save_success);
                }
            }
        });
    }

    private void readMasterInfo(View v){
        final EditText oldEdot = (EditText) v.findViewById(R.id.setting_master_oldpwd_edit);
        final EditText newEdit = (EditText) v.findViewById(R.id.setting_master_newpwd_edit);
        v.findViewById(R.id.setting_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String old = oldEdot.getText().toString();
                String news = newEdit.getText().toString();
                if(old.equals(cfg.getPassword())){
                    if(!StringUtil.isNullWithTrim(news)){
                        cfg.setPassword(news);
                        cfg.save();
                        AppToast.show(TransparaFrags.this , R.string.setting_save_success);
                    }else {
                        AppToast.show(TransparaFrags.this , R.string.setting_master_new_pass_null);
                    }
                }else {
                    AppToast.show(TransparaFrags.this , R.string.setting_master_pass_err);
                }
            }
        });
    }

    private int itemIndex ;

    private void readPasswordSwitch(View v){
        final Spinner spinner = (Spinner) v.findViewById(R.id.setting_trans_input_pass_revocation);
        setAdaper(spinner , R.array.onoff);
        spinner.setSelection(cfg.getRevocationPassSwitch()?0:1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                itemIndex = i ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                itemIndex = 0 ;
            }
        });
        v.findViewById(R.id.setting_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfg.setRevocationPassWSwitch(itemIndex==0?true:false);
                cfg.save();
                AppToast.show(TransparaFrags.this , R.string.setting_save_success);
            }
        });
    }

    private void readCarduseSwitch(View v){
        Spinner spinner = (Spinner) v.findViewById(R.id.setting_trans_input_pass_revocation);
        setAdaper(spinner , R.array.onoff);
        spinner.setSelection(cfg.getRevocationCardSwitch()?0:1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                itemIndex = i ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                itemIndex = 0 ;
            }
        });
        v.findViewById(R.id.setting_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfg.setRevocationCardSwitch(itemIndex==0?true:false);
                cfg.save();
                AppToast.show(TransparaFrags.this , R.string.setting_save_success);
            }
        });
    }

    private void readContactlessSwitch(View v){
        Spinner spinner = (Spinner) v.findViewById(R.id.setting_trans_use_contactless);
        setAdaper(spinner , R.array.onoff);
        spinner.setSelection(cfg.getContactlessSwitch()?0:1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                itemIndex = i ;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                itemIndex = 0 ;
            }
        });
        v.findViewById(R.id.setting_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cfg.setContactlessSwitch(itemIndex==0?true:false);
                cfg.save();
                AppToast.show(TransparaFrags.this , R.string.setting_save_success);
            }
        });
    }

    private void setAdaper(Spinner spinner , int arrayID){
        String[] array = getResources().getStringArray(arrayID);
        ArrayAdapter adapter = new ArrayAdapter(this , android.R.layout.simple_spinner_dropdown_item , array);
        spinner.setAdapter(adapter);
    }
}
