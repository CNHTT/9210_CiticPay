package com.android.citic.appmanager.history;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.citic.R;
import com.android.citic.appmanager.trans.PagerItem;
import com.android.citic.lib.adapter.ListAdapter;
import com.android.citic.lib.utils.DateUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.device.printer.PrintRes;
import com.android.citic.transmanager.global.GlobalCfg;
import com.android.citic.transmanager.trans.Trans;
import com.android.citic.transmanager.trans.helper.translog.TransLogData;

public class HistorylogAdapter extends ListAdapter<TransLogData> {

	private GlobalCfg config;

	public HistorylogAdapter(Activity context) {
		super(context);
		config = GlobalCfg.getInstance();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHold viewHold = null;
		TransLogData item = null;
		if (mList.size() > 0){
			item = mList.get(position);
		}
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.trans_history_loglist_item, null);
			viewHold = new ViewHold();
			viewHold.tv_pan = (TextView) convertView.findViewById(R.id.tv_pan);
			viewHold.tv_voucherno = (TextView) convertView.findViewById(R.id.tv_voucherno);
			viewHold.tv_authno = (TextView) convertView.findViewById(R.id.tv_authno);
			viewHold.tv_amount = (TextView) convertView.findViewById(R.id.tv_amount);
			viewHold.tv_date = (TextView) convertView.findViewById(R.id.tv_date);
			viewHold.tv_batchno = (TextView) convertView.findViewById(R.id.tv_batchno);
			viewHold.tv_status = (TextView) convertView.findViewById(R.id.tv_status);
			viewHold.tv_aac = (TextView) convertView.findViewById(R.id.tv_aac);
			viewHold.tv_right_top = (TextView) convertView.findViewById(R.id.status_flag);
			convertView.setTag(viewHold);
		} else {
			viewHold = (ViewHold) convertView.getTag();
		}

		if (item != null) {

			//显示卡号
			viewHold.tv_pan.setVisibility(View.VISIBLE);
			viewHold.tv_pan.setText(PrintRes.EN.CARD_NO+item.getPan());

			//显示应用密文
			viewHold.tv_aac.setVisibility(View.VISIBLE);
			viewHold.tv_aac.setText(PrintRes.EN.TRANS_AAC+formatTransAAC(item.getAAC()));

			//显示撤销标记
			if(item.getIsVoided()){
				viewHold.tv_right_top.setVisibility(View.VISIBLE);
			}else{
				viewHold.tv_right_top.setVisibility(View.GONE);
			}

			//显示交易类型
			viewHold.tv_status.setVisibility(View.VISIBLE);
			viewHold.tv_status.setText(PrintRes.EN.TRANS_TYPE+formatTransType(item.getEName()));

			//显示时间
			viewHold.tv_date.setVisibility(View.VISIBLE);
			viewHold.tv_date.setText(PrintRes.EN.DATE_TIME+ DateUtil.printStr(item.getLocalDate(), item.getLocalTime()));

			//显示批次号
			if(item.getBatchNo() != null){
				viewHold.tv_batchno.setVisibility(View.VISIBLE);
				viewHold.tv_batchno.setText(PrintRes.EN.BATCH_NO+item.getBatchNo());
			}

			//显示金额
			viewHold.tv_amount.setVisibility(View.VISIBLE);
			if(item.getEName().equals(Trans.Type.EC_ENQUIRY)){
				viewHold.tv_amount.setText(PrintRes.EN.EC_AMOUNT+StringUtil.TwoWei(item.getAmount().toString()));
			}else if(item.getEName().equals(Trans.Type.ENQUIRY)){
				viewHold.tv_amount.setText(PrintRes.EN.CARD_AMOUNT+StringUtil.TwoWei(item.getAmount().toString()));
			}else {
				viewHold.tv_amount.setText(PrintRes.EN.AMOUNT+StringUtil.TwoWei(item.getAmount().toString()));
			}

			//显示流水号
			if(item.getTraceNo() != null){
				viewHold.tv_voucherno.setVisibility(View.VISIBLE);
				viewHold.tv_voucherno.setText(PrintRes.EN.VOUCHER_NO+item.getTraceNo());
			}

			//显示授权码
			if(item.getAuthCode() != null){
				viewHold.tv_authno.setVisibility(View.VISIBLE);
				viewHold.tv_authno.setText(PrintRes.EN.AUTH_NO+item.getAuthCode());
			}

			convertView.setTag(R.id.tag_item_history_trans, item);
		}
		return convertView;
	}

	private String formatTransType(String type){
		if(type.equals(Trans.Type.SALE)){
			return PagerItem.SALE;
		}if(type.equals(Trans.Type.VOID)){
			return PagerItem.VOID;
		}if(type.equals(Trans.Type.ENQUIRY)){
			return PagerItem.ENQUIRY;
		}if(type.equals(Trans.Type.EC_ENQUIRY)){
			return PagerItem.EC_ENQUIRY;
		}if(type.equals(Trans.Type.QUICKPASS)){
			return PagerItem.QUICKPASS ;
		}
		return null;
	}

	private String formatTransAAC(int aac){
		if(aac == 0 ){
			return PrintRes.EN.TRANS_AAC_TC;
		}else {
			return PrintRes.EN.TRANS_AAC_ARQC;
		}
	}

	class ViewHold {
		TextView tv_pan;
		TextView tv_voucherno;
		TextView tv_authno;
		TextView tv_amount;
		TextView tv_date;
		TextView tv_batchno;
		TextView tv_status;
		TextView tv_right_top;
		TextView tv_aac ;
	}
}
