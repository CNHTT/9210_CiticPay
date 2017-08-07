package com.android.citic.appmanager.setting.fragments;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.transmanager.global.GlobalCfg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by zhouqiang on 2017/3/10.
 */

public class ErrlogFrags {

    private Context mContext ;
    private RelativeLayout rLayout ;
    private Spinner spinner ;
    private TextView content ;
    private ScrollView scrollView ;

    public ErrlogFrags(Context a , RelativeLayout l , String title){
        this.mContext = a ;
        this.rLayout = l ;
        rLayout.removeAllViews();
        rLayout.inflate(a , R.layout.setting_frag_errlog, rLayout);
        ((TextView)rLayout.findViewById(R.id.setting_title_tv)).setText(title);
        spinner = (Spinner) rLayout.findViewById(R.id.setting_errlog_list);
        content = (TextView) rLayout.findViewById(R.id.setting_errlog_detail);
        scrollView = (ScrollView) rLayout.findViewById(R.id.setting_errlog_sv);
        readErrlogs();
    }

    private void readErrlogs(){
        File file = new File(GlobalCfg.getROOT_FILE_PATH()+"errlog/");
        if(file.exists() && file.isDirectory()){
            String[] list = file.list() ;
            if(list == null || list.length == 0){
                scrollView.setVisibility(View.GONE);
            }else {
                scrollView.setVisibility(View.VISIBLE);
                ArrayAdapter adapter = new ArrayAdapter(mContext , android.R.layout.simple_spinner_dropdown_item , list);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        String err = readErrlog(adapterView.getItemAtPosition(i).toString()) ;
                        int usage = err.indexOf("TYPE=user");
                        content.setText(err.substring(usage , err.length())) ;
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });
            }
        }
    }

    private String readErrlog(String filename){
        String result = "";
        String apkPathString = GlobalCfg.getROOT_FILE_PATH()+"errlog/";
        File judgeFile = new File(apkPathString + filename);
        if (!judgeFile.exists()) {
            return null ;
        } else {
            try {
                FileInputStream fileInputStream = new FileInputStream(apkPathString + "/"+filename);
                byte[] buffer = new byte[fileInputStream.available() + 1 ];
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                int len ;
                while ((len = fileInputStream.read(buffer)) == -1);
                outputStream.write(buffer, 0, len);
                byte[] byteStream = outputStream.toByteArray();
                result = new String(byteStream);
                fileInputStream.close();
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        return result;
    }
}
