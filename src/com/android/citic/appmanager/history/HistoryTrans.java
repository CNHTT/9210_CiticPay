package com.android.citic.appmanager.history;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.citic.R;
import com.android.citic.appmanager.log.Logger;
import com.android.citic.appmanager.master.BaseActivity;
import com.android.citic.appmanager.master.NavigationConfig;
import com.android.citic.lib.toastview.AppToast;
import com.android.citic.lib.utils.DateUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.card.CardInfo;
import com.android.citic.transmanager.device.printer.PrintManager;
import com.android.citic.transmanager.trans.TransInputPara;
import com.android.citic.transmanager.trans.helper.translog.TransLog;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;
import com.pos.device.printer.Printer;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

@NavigationConfig(titleId = R.string.history_transaction_detail)
public class HistoryTrans extends BaseActivity implements AdapterView.OnItemClickListener{

    @Bind(R.id.history_lv)
    ListView lv_trans ;
    @Bind(R.id.history_nodata)
    View view_nodata ;
    @Bind(R.id.history_search_edit)
    EditText search_edit ;
    @Bind(R.id.history_search)
    ImageView search ;
    @Bind(R.id.history_search_layout)
    LinearLayout z ;

    private Dialog mDialog = null ;
    ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_history);
        setReturnVisible(View.INVISIBLE);
        ButterKnife.bind(this);
        listView = (ListView) findViewById(R.id.history_lv);
        HistoryAdapter adapter = new HistoryAdapter(this);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        //edit by liyo
        search.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                String vouchno = search_edit.getText().toString();
                SetLvPosition(vouchno);
            }
        });
    }

    private void SetLvPosition(String vouchno){ //edit by liyo
        TransLog log = TransLog.getInstance() ;
        int pos = log.getSize()-log.searchPosByTraceNo(vouchno)-1;
        if(pos < 0)
            return;
        listView.setSelection(pos);
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(mDialog!=null){
                mDialog.dismiss();
            }
            switch (msg.what){
                case 0 :
                    AppToast.show(HistoryTrans.this , R.string.recept_print_success);
                    break;
                case Printer.PRINTER_STATUS_PAPER_LACK:
                    AppToast.show(HistoryTrans.this , R.string.recept_print_paper_out);
                    break;
                default:
                    AppToast.show(HistoryTrans.this , R.string.setting_mkey_unknow_err);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Logger.debug("count = "+parent.getCount()+"..postion = "+position);
        TransLogData data = (TransLogData) parent.getItemAtPosition(position);
        initPopupWindow(data);
    }

    private PopupWindow pWindow = null ;
    private View pView = null ;
    private void initPopupWindow(final TransLogData data){
        DisplayMetrics dMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
        int width = dMetrics.widthPixels ;
        int height = dMetrics.heightPixels;
        if(pWindow!=null)
            pWindow.dismiss();
        pView = this.getLayoutInflater().inflate(R.layout.notice_popwindow, null,false);
        pWindow = new PopupWindow(pView,width/3,height/5,true);
        pWindow.setAnimationStyle(R.anim.right2left);
        pWindow.setOutsideTouchable(true);
        pWindow.setBackgroundDrawable(new BitmapDrawable());
        pView.findViewById(R.id.pop_window_reprint).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
                re_print(1 , data);
            }
        });
        pView.findViewById(R.id.pop_window_reprint_10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pWindow.dismiss();
                re_print(10 , data);
            }
        });
        pWindow.showAtLocation(search, Gravity.TOP, height/2, width);
    }

    int ret = 0 ;
    private void re_print(final int num , final TransLogData data){
        mDialog = show();
        new Thread(){
            public void run(){
                do {
                    for (int i = 0 ; i < num ; i ++){
                        PrintManager pm = PrintManager.getmInstance(HistoryTrans.this) ;
                        ret = pm.start(data , true);
                    }
                } while (ret == Printer.PRINTER_STATUS_PAPER_LACK);
                mHandler.sendEmptyMessage(ret);
            }
        }.start();
    }

    public Dialog show(){
        final Dialog pd = new Dialog(this, R.style.Prompt);
        pd.setContentView(R.layout.notice_process);
        RelativeLayout layout = (RelativeLayout) pd.findViewById(R.id.r_dialog);
        layout.startAnimation(AnimationUtils.loadAnimation(this, R.anim.up_2_down));
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        return pd ;
    }
}
