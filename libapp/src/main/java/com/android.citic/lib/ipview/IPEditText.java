package com.android.citic.lib.ipview;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.citic.lib.utils.StringUtil;
import com.example.libapp.R;

/**
 * IP输入框
 */

public class IPEditText extends LinearLayout {

	private EditText mFirstIP;
	private EditText mSecondIP;
	private EditText mThirdIP;
	private EditText mFourthIP;

	private String mText;
	private String mText1;
	private String mText2;
	private String mText3;
	private String mText4;
	private String tips;

	private SharedPreferences mPreferences;

	public IPEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		/**
		 * 初始化控件
		 */
		View view = LayoutInflater.from(context).inflate(R.layout.ip_edit_text, this);
		mFirstIP = (EditText) findViewById(R.id.ip_first);
		mSecondIP = (EditText) findViewById(R.id.ip_second);
		mThirdIP = (EditText) findViewById(R.id.ip_third);
		mFourthIP = (EditText) findViewById(R.id.ip_fourth);
		tips = context.getResources().getString(R.string.ip_err);
		mPreferences = context.getSharedPreferences("config_IP",Context.MODE_PRIVATE);
		OperatingEditText(context);
	}

	/**
	 * 获得EditText中的内容,当每个Edittext的字符达到三位时,自动跳转到下一个EditText,当用户点击.时,
	 * 下一个EditText获得焦点
	 */
	private void OperatingEditText(final Context context) {
		mFirstIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				if (s != null && s.length() > 0) {
					if (s.length() > 2 || s.toString().trim().contains(".")) {
						if (s.toString().trim().contains(".")) {
							mText1 = s.toString().substring(0, s.length() - 1);
							mFirstIP.setText(mText1);
						} else {
							mText1 = s.toString().trim();
						}
						if (Integer.parseInt(mText1) > 255) {
							// Toast.makeText(context,tips,Toast.LENGTH_LONG).show();
							return;

						}
						Editor editor = mPreferences.edit();
						editor.putInt("IP_FIRST", mText1.length());
						editor.commit();

						mSecondIP.setFocusable(true);
						mSecondIP.requestFocus();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// if(!StringUtil.isNullWithTrim(s.toString()))
				// if(s.length()<3)
				// try {
				// mText1=ISOUtil.padleft(s.toString(),3, '0');
				// } catch (ISOException e) {
				// e.printStackTrace();
				// }
			}
		});

		mSecondIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				if (s != null && s.length() > 0) {
					if (s.length() > 2 || s.toString().trim().contains(".")) {
						if (s.toString().trim().contains(".")) {
							mText2 = s.toString().substring(0, s.length() - 1);
							mSecondIP.setText(mText2);
						} else {
							mText2 = s.toString().trim();
						}
						if (Integer.parseInt(mText2) > 255) {
							// Toast.makeText(context,tips,Toast.LENGTH_LONG).show();
							return;
						}
						Editor editor = mPreferences.edit();
						editor.putInt("IP_SECOND", mText2.length());
						editor.commit();

						mThirdIP.setFocusable(true);
						mThirdIP.requestFocus();
					}
				}

				/**
				 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
				 */
				if (start == 0
						&& s.length() == 0
						&& ! StringUtil.isNullWithTrim(mFirstIP.getText()
								.toString()) && mFirstIP.length() > 2) {
					mFirstIP.setFocusable(true);
					mFirstIP.requestFocus();
					mFirstIP.setSelection(mPreferences.getInt("IP_FIRST", 0));
				}

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mThirdIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				if (s != null && s.length() > 0) {
					if (s.length() > 2 || s.toString().trim().contains(".")) {
						if (s.toString().trim().contains(".")) {
							mText3 = s.toString().substring(0, s.length() - 1);
							mThirdIP.setText(mText3);
						} else {
							mText3 = s.toString().trim();
						}

						if (Integer.parseInt(mText3) > 255) {
							// Toast.makeText(context,tips,Toast.LENGTH_LONG).show();
							return;
						}

						Editor editor = mPreferences.edit();
						editor.putInt("IP_THIRD", mText3.length());
						editor.commit();

						mFourthIP.setFocusable(true);
						mFourthIP.requestFocus();
					}
				}

				/**
				 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
				 */
				if (start == 0 && s.length() == 0
						&& !StringUtil.isNullWithTrim(mSecondIP.getText().toString())
						&& mSecondIP.length() > 2) {
					mSecondIP.setFocusable(true);
					mSecondIP.requestFocus();
					mSecondIP.setSelection(mPreferences.getInt("IP_SECOND", 0));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		mFourthIP.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				/**
				 * 获得EditTe输入内容,做判断,如果大于255,提示不合法,当数字为合法的三位数下一个EditText获得焦点,
				 * 用户点击啊.时,下一个EditText获得焦点
				 */
				if (s != null && s.length() > 0) {
					mText4 = s.toString().trim();

					if (Integer.parseInt(mText4) > 255) {
						// Toast.makeText(context,tips,Toast.LENGTH_LONG).show();
					}

					Editor editor = mPreferences.edit();
					editor.putInt("IP_FOURTH", mText4.length());
					editor.commit();
				}

				/**
				 * 当用户需要删除时,此时的EditText为空时,上一个EditText获得焦点
				 */
				if (start == 0 && s.length() == 0
						&& !StringUtil.isNullWithTrim(mThirdIP.getText().toString())
						&& mThirdIP.length() > 2) {
					mThirdIP.setFocusable(true);
					mThirdIP.requestFocus();
					mThirdIP.setSelection(mPreferences.getInt("IP_THIRD", 0));
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});
	}

	public String getIPText() {
		if (!StringUtil.isNullWithTrim(mFirstIP.getText().toString())
				&& !StringUtil.isNullWithTrim(mSecondIP.getText().toString())
				&& !StringUtil.isNullWithTrim(mThirdIP.getText().toString())
				&& !StringUtil.isNullWithTrim(mFourthIP.getText().toString()))
			return mFirstIP.getText().toString() + "."
					+ mSecondIP.getText().toString() + "."
					+ mThirdIP.getText().toString() + "."
					+ mFourthIP.getText().toString();
		else
			return null;
	}

	public void setIPText(String[] ip){
		mFirstIP.setText(ip[0]);
		mSecondIP.setText(ip[1]);
		mThirdIP.setText(ip[2]);
		mFourthIP.setText(ip[3]);
	}

	public void setLiveOrDeath(boolean isLive){
		if(!isLive){
			mFirstIP.setTextColor(Color.GRAY);
			mSecondIP.setTextColor(Color.GRAY);
			mThirdIP.setTextColor(Color.GRAY);
			mFourthIP.setTextColor(Color.GRAY);
		}else {
			mFirstIP.setTextColor(Color.BLACK);
			mSecondIP.setTextColor(Color.BLACK);
			mThirdIP.setTextColor(Color.BLACK);
			mFourthIP.setTextColor(Color.BLACK);
		}
		mFirstIP.setEnabled(isLive);
		mSecondIP.setEnabled(isLive);
		mThirdIP.setEnabled(isLive);
		mFourthIP.setEnabled(isLive);
	}

	public boolean isOk() {
		if (StringUtil.isNullWithTrim(mFirstIP.getText().toString())
				|| StringUtil.isNullWithTrim(mSecondIP.getText().toString())
				|| StringUtil.isNullWithTrim(mThirdIP.getText().toString())
				|| StringUtil.isNullWithTrim(mFourthIP.getText().toString())) {
			return false;
		}
		return true;
	}
}
