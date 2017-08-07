package com.android.citic.appmanager;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.history.HistoryTrans;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.appmanager.setting.Settings;
import com.android.citic.appmanager.trans.Masterctl;
import com.android.citic.appmanager.trans.PagerItem;
import com.android.citic.lib.baidu.BaiduLocation;
import com.android.citic.lib.baidu.LocationInfo;
import com.android.citic.lib.baidu.LocationListener4Caller;
import com.android.citic.lib.centerdialog.CenterDialog;
import com.android.citic.lib.searchfilter.ClearEditText;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.winphone.DesertImginfo;
import com.android.winphone.DesertPageradapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class CiticPay extends Activity implements View.OnClickListener,
        ViewPager.OnPageChangeListener,AdapterView.OnItemClickListener{

    //@Bind(R.id.title_location_tv)
    //TextView location ;
    //@Bind(R.id.title_search_et)
    //ClearEditText search ;
    @Bind(R.id.title_settigns_ib)
    ImageButton settings ;
    //@Bind(R.id.title_search_back)
    //ImageButton searchBack ;
    @Bind(R.id.pagernum)
    TextView wp_main_pagernum ;
    @Bind(R.id.viewpager)
    ViewPager wp_main_pager ;

    private Context mInstance ;
    public static final String START_TRANS_KEY = "start_trans_key" ;
    private Dialog mDialog ;
    private Citicapp mApp ;
    private GlobalCfg cfg ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.citic_pay);
        Citicapp.getInstance().addActivity(this);
        mInstance = this ;
        mApp = Citicapp.getInstance();
        ButterKnife.bind(this);
        initViews();
        Log.v("liyo","onCreate citicpay");
//      startBaiduLocation();
        cfg = GlobalCfg.getInstance();
        if(!cfg.isTermLogon()){
            start2Trans(PagerItem.LOGON);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
        System.exit(0);
    }

    /** ======================私有方法==================*/
    private int[] wp_ids ;
    private ArrayList<DesertImginfo> wp_data ;
    private int current_page_num = 0 ;
    private void initViews(){
        //searchBack.setOnClickListener(this);
        settings.setOnClickListener(this);
        //search.setOnClickListener(this);
        shake();
        wp_ids = new int[]{R.layout.winphone_page, R.id.wp_main_page_content,
                R.layout.winphone_item, R.id.wp_item_iv, R.id.wp_item_rl, R.id.wp_item_tv};
        wp_data = new ArrayList<>();
        wp_main_pagernum.setText("1");
        String[] items = getResources().getStringArray(R.array.trans_EN);
        int[] imgs = {R.drawable.icon_query, R.drawable.icon_sale,
                R.drawable.icon_barcode,R.drawable.icon_sale_revocation,
                R.drawable.icon_ec_query, R.drawable.icon_ec_sale,
                R.drawable.icon_settle, R.drawable.icon_loginin,
                R.drawable.icon_logonout, R.drawable.icon_down_paras,
                R.drawable.icon_trans_record, R.drawable.icon_settings};
        int[] bgs = {R.drawable.icon_item_bg1 , R.drawable.icon_item_bg2,
                R.drawable.icon_item_bg6, R.drawable.icon_item_bg3,
                R.drawable.icon_item_bg4 ,R.drawable.icon_item_bg5 ,
                R.drawable.icon_item_bg6, R.drawable.winphone_item_bg2,
                R.drawable.icon_item_bg3, R.drawable.winphone_item_bg1,
                R.drawable.icon_item_bg5, R.drawable.winphone_item_bg2} ;
        for (int i = 0 ; i < items.length ; i ++){
            wp_data.add(new DesertImginfo(items[i] , imgs[i] , bgs[i]));
        }
        wp_main_pager.setAdapter(new DesertPageradapter(this, wp_data , wp_ids , this));
        wp_main_pager.setPageMargin(50);
        wp_main_pager.setOnPageChangeListener(this);
    }

    private void chcekMaintainPwd(){
        mDialog = CenterDialog.show(this , R.layout.setting_check_maintain_pwd , R.id.setting_checkpwd_root_layout);
        final EditText editText = (EditText) mDialog.findViewById(R.id.setting_checkpwd_edit);
        TextView confirm = (TextView) mDialog.findViewById(R.id.setting_checkpwd_confirm);
        TextView cancel = (TextView) mDialog.findViewById(R.id.setting_checkpwd_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pass = editText.getText().toString();
                if(pass.equals(cfg.getSecurePass())){
                    mDialog.dismiss();
                    start2Trans(PagerItem.SETTING);
                }else {
                    editText.setText("");
                    AppToast.show(CiticPay.this , R.string.setting_maintain_pwd_err);
                }
            }
        });
    }

    private void shake() {
        //search.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }
/*****
    private void startBaiduLocation(){
        BaiduLocation bl = mApp.getBaiduLocation() ;
        bl.startLocation(new LocationListener4Caller.ILocationListener4Caller() {
            @Override
            public void finish(LocationInfo locationInfo) {
                if(locationInfo.getCity()!=null){
                    Logger.debug("address = "+locationInfo.getAddress());
                    Logger.debug("city = "+locationInfo.getCity());
                    Logger.debug("province = "+locationInfo.getProvince());
                    Logger.debug("time = "+locationInfo.getTime());
                    location.setText(locationInfo.getCity());
                }else {
                    location.setText(mInstance.getString(R.string.app_locationing));
                }
            }
        } , 1000);
    }
******/
    private void start2Trans(String ch){
        Bundle bundle = new Bundle();
        bundle.putString(START_TRANS_KEY , ch);
        Intent i = new Intent();
        String trantype = ch2en(ch);
        switch (trantype){
            case PagerItem.TRANSLOG:i.setClass(mInstance , HistoryTrans.class);break;
            case PagerItem.SETTING:i.setClass(mInstance , Settings.class);break;
            default:i.setClass(mInstance , Masterctl.class);break;
        }
        i.putExtras(bundle);
        mInstance.startActivity(i);
    }

    private String ch2en(String ch){
        String[] chs = getResources().getStringArray(R.array.trans);
        int index = 0 ;
        for (int i = 0 ; i < chs.length ; i++){
            if(chs[i].equals(ch)){
                index = i ;
            }
        }

        return getResources().getStringArray(R.array.trans_EN)[index] ;
    }
    /** ======================接口回调方法==================*/

    @Override
    public void onClick(View view) {
        if(view.equals(settings)){
            chcekMaintainPwd();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        current_page_num = position ;
        wp_main_pagernum.setText("" + (int) (position + 1));
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String temp = ((TextView)view.findViewById(R.id.wp_item_tv)).getText().toString();
        Logger.debug("listview onItemClick = "+temp);
        start2Trans(temp);
    }
}
