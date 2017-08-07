//package com.android.citic.transmanager.trans.utils;
//
//import android.app.Activity;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.DialogInterface.OnClickListener;
//import android.content.res.Configuration;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
//import android.text.Editable;
//import android.text.InputFilter;
//import android.text.InputType;
//import android.text.Selection;
//import android.text.Spanned;
//import android.text.TextWatcher;
//import android.text.method.DigitsKeyListener;
//import android.text.method.PasswordTransformationMethod;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.OnTouchListener;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.android.szfp.MainActivity;
//import com.android.szfp.R;
//import com.android.szfp.libview.utils.ISOUtil;
//import com.android.szfp.libview.utils.StringUtil;
//import com.android.szfp.manage.LogManager;
//import com.android.szfp.master.CustomDialog;
//import com.android.szfp.master.CustomDialog.Builder;
//import com.android.szfp.timer.MyTimerCancleListener;
//import com.android.szfp.timer.MyTimerTask;
//import com.android.szfp.trans.Trans;
//import com.android.szfp.trans.finace.FinanceTrans;
//
//import org.apache.commons.lang.StringUtils;
//
//import java.text.DecimalFormat;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.CountDownLatch;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class TransUtil {
//	static Context tcontext;
//	public static boolean isInvoke;
//	private static EditText editDialog;
//	private static TextView textTip;
//	private static Timer timer;
//	public static String result;
//
//	private static boolean isPass1;
//	private static boolean isPass2;
//	public static boolean isNull;
//	/** 全局表示是否正在打印，通过这个表示判断二次打印无效并且无法退出当前打印的activity **/
//	public static boolean printFlag = false;
//
//	// isPassword 0.非密码格式 1.密码格式 2.密码格式 带延迟
//	public static CustomDialog BuildDialog(Context context, String tips,
//										   int min, int max, int inputType, TextWatcher textWatcher,
//										   CountDownLatch latch) {
//		final int minLen = min;
//		final int _inputType = inputType;
//		final CountDownLatch clatch = latch;
//		tcontext = context;
//		CustomDialog.Builder builder = new Builder(context);
//		if ((inputType & 0x40) != 0) {
//			isNull = true;
//		}
//		builder.setTitle(R.string.prompt)
//				.setNegativeButton(R.string.cancel, new OnClickListener() {
//
//					@Override
//					public revocation onClick(DialogInterface arg0, int arg1) {
//						// tcontext.startActivity(new Intent((Activity)
//						// tcontext,
//						// MainActivity.class));
//						// ((Activity) tcontext).finish();
//						arg0.cancel();
//					}
//				}).setPositiveButton(R.string.confirm, new OnClickListener() {
//
//			@Override
//			public revocation onClick(DialogInterface arg0, int arg1) {
//				InputMethodManager imm = (InputMethodManager) tcontext
//						.getSystemService(Context.INPUT_METHOD_SERVICE);
//				imm.toggleSoftInput(0,
//						InputMethodManager.HIDE_NOT_ALWAYS);
//				if (editDialog.getText().toString().length() < minLen) {
//					// 最小输入长度
//					Toast.makeText(
//							tcontext,
//							TransUtil
//									.getStringByInt(tcontext,
//											R.string.sale_amount_len_err,
//											minLen + ""),
//							Toast.LENGTH_LONG).show();
//					return;
//				}
//				if (_inputType == Trans.INPUT_TYPE_NUMBERDECIMEL) {
//					if (!isNull
//							&& !editDialog.getText().toString().trim()
//							.equals("0.00"))
//						result = StringUtils.replace(editDialog
//								.getText().toString().trim(), ".", "");
//					else {
//						// 金额不能为空
//						Toast.makeText(tcontext,
//								R.string.sale_amount_empty_err,
//								Toast.LENGTH_LONG).show();
//						return;
//					}
//				} else if (!isNull
//						&& !editDialog.getText().toString().trim()
//						.equals("")) {
//					result = StringUtils.replace(editDialog.getText()
//							.toString().trim(), ".", "");
//				} else {
//					// 输入内容不能为空
//					Toast.makeText(tcontext,
//							R.string.sale_content_empty_err,
//							Toast.LENGTH_LONG).show();
//					return;
//				}
//				clatch.countDown();
//			}
//		});
//		LayoutInflater inflater = (LayoutInflater) context
//				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		View view = inflater.inflate(R.layout.custom_dialog_normal_edit, null);
//		editDialog = (EditText) view.findViewById(R.id.edit_dialog_input);
//		textTip = (TextView) view.findViewById(R.id.text_dialog_input_tips);
//
//		if ((inputType & 0x10) != 0) {
//			isPass1 = true;
//		} else {
//			if ((inputType & 0x20) != 0) {
//				isPass2 = true;
//			}
//		}
//
//		switch (inputType) {
//			case Trans.INPUT_TYPE_NUMBER:
//				if (isPass1)
//					editDialog.setTransformationMethod(PasswordTransformationMethod
//							.getInstance());
//				else if (isPass2) {
//					editDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
//					editDialog.addTextChangedListener(new EditChangedPwdListener(
//							editDialog, inputType, min));
//				} else
//					editDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
//				break;
//			case Trans.INPUT_TYPE_CharNumber:
//				if (isPass1)
//					editDialog.setInputType(InputType.TYPE_CLASS_TEXT
//							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//				else if (isPass2)
//					editDialog.addTextChangedListener(new EditChangedPwdListener(
//							editDialog, inputType, min));
//				editDialog.setKeyListener(CharNumberListener);
//				break;
//			case Trans.INPUT_TYPE_NONE:
//				if (isPass1)
//					editDialog.setInputType(InputType.TYPE_CLASS_TEXT
//							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//				else if (isPass2) {
//					editDialog.setInputType(InputType.TYPE_CLASS_TEXT);
//					editDialog.addTextChangedListener(new EditChangedPwdListener(
//							editDialog, inputType, min));
//				} else
//					editDialog.setInputType(InputType.TYPE_CLASS_TEXT);
//				break;
//			case Trans.INPUT_TYPE_HEXNUMBER:
//				if (isPass1)
//					editDialog.setInputType(InputType.TYPE_CLASS_TEXT
//							| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//				else if (isPass2)
//					editDialog.addTextChangedListener(new EditChangedPwdListener(
//							editDialog, inputType, min));
//				editDialog.setKeyListener(CharHexListener);
//				break;
//			case Trans.INPUT_TYPE_NUMBERDECIMEL:
//				// editDialog.setOnKeyListener(mOnKeyListener);
//				// tv_content.setVisibility(View.VISIBLE);
//				// editDialog.setTextColor(Color.WHITE);
//				editDialog.setText("0.00");
//				editDialog.setCursorVisible(false);
//				editDialog.setOnTouchListener(new OnTouchListener() {
//					public boolean onTouch(View v, MotionEvent event) {
//						editDialog.requestFocus();
//						return false;
//					}
//				});
//				editDialog.setRawInputType(Configuration.KEYBOARD_12KEY);
//				editDialog.addTextChangedListener(textWatcher);
//				break;
//			case Trans.INPUT_TYPE_PWD_1:
//				editDialog.setInputType(InputType.TYPE_CLASS_TEXT
//						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//				break;
//			case Trans.INPUT_TYPE_PWD_2:
//				editDialog.addTextChangedListener(new EditChangedPwdListener(
//						editDialog, inputType, min));
//				break;
//			default:
//				break;
//		}
//
//		textTip = (TextView) view.findViewById(R.id.text_dialog_input_tips);
//		textTip.setText(tips);
//		editDialog.setFilters(new InputFilter[] { new InputFilter.LengthFilter(max) });
//		builder.setContentView(view);
//		return builder.create();
//	}
//
//	//提示信息的弹框
//	private static CustomDialog BuildDialog(Context ctx, String notice,
//											OnClickListener positiveClick) {
//		CustomDialog.Builder builder = new Builder(ctx);
//		builder.setTitle(R.string.prompt).setMessage(notice)
//				.setPositiveButton(R.string.confirm, positiveClick);
//		return builder.create();
//	}
//
//	//提示信息升级版 确认继续执行 取消关闭dialog
//	private static CustomDialog BuildDialog(Context ctx, String notice,
//											OnClickListener positiveClick, OnClickListener negativeClick) {
//		CustomDialog.Builder builder = new Builder(ctx);
//		builder.setTitle(R.string.prompt).setMessage(notice)
//				.setPositiveButton(R.string.confirm, positiveClick)
//				.setNegativeButton(R.string.cancel, negativeClick);
//		return builder.create();
//	}
//
//	private static CustomDialog BuildDialog(Context ctx, String[] appname,
//											OnClickListener negativeClick, ListView list) {
//		CustomDialog.Builder builder = new Builder(ctx);
//		builder.setTitle(R.string.sale_chose_app_list).setContentView(list)
//				.setNegativeButton(R.string.cancel, negativeClick);
//		return builder.create();
//	}
//
//	public static CustomDialog input(Context context, String tips, int min,
//									 int max, int inputType, int timeout, CountDownLatch latch,
//									 TextWatcher textWatcher, TimerTask task) {
//		CustomDialog dialog = BuildDialog(context, tips, min, max, inputType,
//				textWatcher, latch);
//		timer = new Timer();
//		timer.schedule(task, (long) timeout);
//		System.out.println("启动了一个超时时间为" + timeout + "的定时器");
//		return dialog;
//	}
//
//	//err notice
//	public static CustomDialog noticeDialog(Context ctx, String notice,
//											int timeout, TimerTask timerTask, OnClickListener PositiveClick,
//											CountDownLatch latch) {
//		tcontext = ctx;
//		CustomDialog dialog = BuildDialog(ctx, notice, PositiveClick);
//		timer = new Timer();
//		if (timerTask == null) {
//			timerTask = new MyTimerTask(dialog, (Activity) ctx,
//					MainActivity.class);
//			dialog.setOnCancelListener(new MyTimerCancleListener(timerTask,
//					latch));
//		}
//		timer.schedule(timerTask, (long) timeout);
//		return dialog;
//	}
//
//	//normal notice
//	public static CustomDialog noticeNormalDialog(Context ctx, String notice,
//												  int timeout, TimerTask timerTask, OnClickListener PositiveClick,
//												  OnClickListener negativeClick) {
//		result = null;
//		tcontext = ctx;
//		CustomDialog dialog = BuildDialog(ctx, notice, PositiveClick,
//				negativeClick);
//		timer = new Timer();
//		LogManager.t("start a timer which its timeout = " + timeout );
//		if (timerTask == null)
//			timerTask = new MyTimerTask(dialog, (Activity) ctx,
//					MainActivity.class);
//		timer.schedule(timerTask, (long) timeout);
//		return dialog;
//	}
//
//	//app notice
//	public static CustomDialog showAllApp(Context ctx, String[] appname,
//										  CountDownLatch latch, OnClickListener negativeClick, ListView list,
//										  TimerTask timerTask, long timeout) {
//		CustomDialog dialog = BuildDialog(ctx, appname, negativeClick, list);
//		if (timerTask == null) {
//			timerTask = new MyTimerTask(dialog, (Activity) ctx,
//					MainActivity.class);
//			dialog.setOnCancelListener(new MyTimerCancleListener(timerTask,
//					latch));
//		}
//		timer.schedule(timerTask, (long) timeout);
//		return dialog;
//	}
//
//	//amount notice
//	public static String getAmout(Handler handle, int tips) {
//		return ShowDialog(handle, Trans.AMOUTDIALOG, 1, 10, tips, 50000,
//				Trans.INPUT_TYPE_NUMBERDECIMEL);
//	}
//
//	//card notice
//	public static String getCard(Handler handle, int tips, int mode,
//								 boolean isCheckICC) {
//		if ((mode & FinanceTrans.INMODE_HAND) != 0)
//			return ShowDialog(handle, Trans.CARDDIALOG_HAND, 13, 19, tips,
//					50000, Trans.INPUT_TYPE_HEXNUMBER, mode, isCheckICC);
//		else
//			return UseCard(handle, tips, mode, isCheckICC);
//	}
//
//	//pass notice
//	public static String getPass(Handler handle, int tips, String cardNo) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle bundle = new Bundle();
//		bundle.putInt("tips", tips);
//		bundle.putString("cardNo", cardNo);
//		msg.setData(bundle);
//		msg.what = Trans.PWDDIALOG;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	//app notice
//	public static int getApplistResult(Handler handle,
//			String applist) {
//		LogManager.t("getApplistResult = "+applist);
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle data = new Bundle();
//		data.putString("applist", applist);
//		msg.setData(data);
//		msg.what = Trans.Applist;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		if (TransUtil.result != null) {
//			LogManager.t("TransUtil.result = "+TransUtil.result);
//			if(!TransUtil.result.contains(",")){
//				return Integer.parseInt(TransUtil.result);
//			}
//			return 0 ;
//		}
//		else
//			return -2;
//	}
//
//	public static String getNoticeResult(Handler handle,
//										 String tips) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle bundle = new Bundle();
//		bundle.putString("tips", tips);
//		msg.setData(bundle);
//		msg.what = Trans.NOTICE;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	public static String getNoticePrinterErr(Handler handle,
//											 String tips) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle bundle = new Bundle();
//		bundle.putString("tips", tips);
//		msg.setData(bundle);
//		msg.what = Trans.PRINT_LACKPAPER;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	public static String getNoticeResultNoCancle(Handler handle, String tips) {
//		CountDownLatch latch = new CountDownLatch(1);
//		// return ShowDialog(handle);//ped
//		Message msg = new Message();
//		Bundle bundle = new Bundle();
//		bundle.putString("tips", tips);
//		msg.setData(bundle);
//		msg.what = Trans.PRINT_NOTICE;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	public static String getPedNoticeResult(Handler handle,
//											String tips) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle bundle = new Bundle();
//		bundle.putString("tips", tips);
//		msg.setData(bundle);
//		msg.what = Trans.PED_NOTICE;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	// /**
//	// * 货币EditText监听(0.00格式)
//	// *
//	// * @author deng
//	// *
//	// */
//	// public static class EditChangeXsTwoListener implements TextWatcher {
//	//
//	// private EditText mEditText;
//	// private Context mContext;
//	// private int mmax;
//	// private int mmin;
//	// private boolean re = false;
//	//
//	// Handler mHandle = new Handler() {
//	//
//	// @Override
//	// public revocation handleMessage(Message msg) {
//	// super.handleMessage(msg);
//	// switch (msg.what) {
//	// case 1:
//	// tv_content.setText("" + msg.obj);
//	// break;
//	// }
//	// }
//	// };
//	//
//	// public EditChangeXsTwoListener(Context context, EditText editText,
//	// int max, int min) {
//	// this.mContext = context;
//	// this.mmax = max;
//	// this.mmin = min;
//	// this.mEditText = editText;
//	// this.mEditText.setText("0.00");
//	// mEditText.setCursorVisible(false);
//	// }
//	//
//	// public revocation onTextChanged(CharSequence s, int start, int before,
//	// int count) {
//	// String content = s.toString();
//	// // if (!tosBj(mContext, content, mmax, mmin))
//	// // return;
//	// if (re) {
//	// add(mHandle);
//	// } else
//	// del(content, mHandle);
//	// }
//	//
//	// @Override
//	// public revocation beforeTextChanged(CharSequence s, int start, int count,
//	// int after) {
//	// if (after > 0)
//	// re = true;
//	// else
//	// re = false;
//	// }
//	//
//	// @Override
//	// public revocation afterTextChanged(Editable s) {
//	// }
//	// }
//
//	//输入货币格式化
//	public static class CurrencyFormatInputFilter implements InputFilter {
//		Pattern mPattern = Pattern.compile("(0|[1-9]+[0-9]*)(\\.[0-9]{1,2})?");
//
//		@Override
//		public CharSequence filter(CharSequence source, int start, int end,
//								   Spanned dest, int dstart, int dend) {
//			String result = dest.subSequence(0, dstart) + source.toString()
//					+ dest.subSequence(dend, dest.length());
//			Matcher matcher = mPattern.matcher(result);
//			if (!matcher.matches())
//				return dest.subSequence(dstart, dend);
//			return null;
//		}
//	}
//
//	// 输入
//	private static revocation add(Handler mh) {
//
//		String content = editDialog.getText().toString();
//		if (StringUtil.isNullWithTrim(content))
//			content = "0.00";
//		String newContent = null;
//		StringBuffer sb = new StringBuffer();
//		content = content.replace(".", "");
//		String firstInt = null;
//		if (content.length() > 3) {
//			newContent = content;
//			firstInt = content.substring(0, newContent.length() - 2);
//			String end = newContent.substring(newContent.length() - 2,
//					newContent.length());
//			sb.append(firstInt);
//			sb.append(".");
//			sb.append(end);
//		} else if (content.length() <= 3) {
//			content = ISOUtil.padleft(content, 3, '0');
//			firstInt = content.substring(0, 1);// 取个位
//			String end = content.substring(1, 3);// 取后两位12
//			newContent = content;
//			String before = newContent.substring(0, 1);
//			end = newContent.substring(1, 3);
//			sb.append(before);
//			sb.append(".");
//			sb.append(end);
//		}
//
//		DecimalFormat df = new DecimalFormat("0.00");
//		Message msg = new Message();
//		msg.obj = df.format(Double.parseDouble((sb.toString())));
//		msg.what = 1;
//		mh.sendMessage(msg);
//		LogManager.t("add result:" + Double.parseDouble(sb.toString()));
//	}
//
//	// 货币输入时。删除到0.00屏蔽删除按钮
//	// static OnKeyListener mOnKeyListener = new OnKeyListener() {
//	//
//	// @SuppressWarnings("static-access")
//	// @Override
//	// public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
//	// PCILog.d("test","key:"+arg1);
//	// if (arg1 == arg2.KEYCODE_DEL||arg1==arg2.KEYCODE_FORWARD_DEL)
//	// if (!StringUtil.isNullWithTrim(editDialog.getText().toString()))
//	// if ("0.00".equals(editDialog.getText().toString()))
//	// return true;
//	//
//	// return false;
//	// }
//	// };
//
//	// 只能输入字母和数字
//	public static DigitsKeyListener CharNumberListener = new DigitsKeyListener() {
//
//		@Override
//		public int getInputType() {
//			return InputType.TYPE_TEXT_VARIATION_PASSWORD;
//		}
//
//		@Override
//		protected char[] getAcceptedChars() {
//			char[] data = getStringData(R.string.sale_login_only_can_input)
//					.toCharArray();
//			return data;
//		}
//
//	};
//
//	// 只能输入16进制 0-9 a-f
//	public static DigitsKeyListener CharHexListener = new DigitsKeyListener() {
//		@Override
//		public int getInputType() {
//			return InputType.TYPE_TEXT_VARIATION_PASSWORD;
//		}
//
//		@Override
//		protected char[] getAcceptedChars() {
//			char[] data = getStringData(R.string.sale_login_only_hex_input)
//					.toCharArray();
//			return data;
//		}
//	};
//
//	public static String getStringData(int id) {
//		return tcontext.getResources().getString(id);
//	}
//
//	// 删除
//	private static revocation del(String content, Handler mh) {
//		StringBuffer sb = new StringBuffer();
//		if (StringUtil.isNullWithTrim(content))
//			content = "0.00";
//		content = content.replace(".", "");
//		String firstInt = null;
//		if (content.length() <= 3) {
//			content = ISOUtil.padleft(content, 3, '0'); // 0.01
//		}
//		firstInt = content.substring(0, content.length() - 2);
//		String end = content.substring(content.length() - 2, content.length());
//		sb.append(firstInt);
//		sb.append(".");
//		sb.append(end);
//		DecimalFormat df = new DecimalFormat("0.00");
//		Message msg = new Message();
//		msg.obj = df.format(Double.parseDouble((sb.toString())));
//		msg.what = 1;
//		mh.sendMessage(msg);
//	}
//
//	public static revocation WaitInputData(CountDownLatch latch) {
//		try {
//			latch.await();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static String UseCard(Handler handle, int tips, int mode,
//								 boolean isCheckICC) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle data = new Bundle();
//		data.putInt("content", tips);
//		data.putBoolean("isCheckICC", isCheckICC);
//		data.putInt("inputMode", mode);
//		msg.setData(data);
//		msg.what = Trans.CARDDIALOG_USE;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	public static String ShowDialog(Handler handle, int msgWhat, int min,
//									int max, int tips, int timeout, int inputType, int inputMode,
//									boolean isCheckICC) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle data = new Bundle();
//		data.putInt("tips", tips);
//		data.putInt("min", min);
//		data.putInt("max", max);
//		data.putInt("timeout", timeout);
//		data.putInt("inputType", inputType);
//		data.putInt("inputMode", inputMode);
//		data.putBoolean("isCheckICC", isCheckICC);
//		msg.setData(data);
//		msg.what = msgWhat;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	public static String ShowDialog(Handler handle, int msgWhat, int min,
//									int max, int tips, int timeout, int inputType) {
//		CountDownLatch latch = new CountDownLatch(1);
//		Message msg = new Message();
//		Bundle data = new Bundle();
//		data.putInt("tips", tips);
//		data.putInt("min", min);
//		data.putInt("max", max);
//		data.putInt("timeout", timeout);
//		data.putInt("inputType", inputType);
//		msg.setData(data);
//		msg.what = msgWhat;
//		msg.obj = latch;
//		handle.sendMessage(msg);
//		WaitInputData(latch);// 阻塞
//		return TransUtil.result;
//	}
//
//	public static String getStrAmount(long Amount) {
//		double f1 = Double.valueOf(Amount + "");
//		DecimalFormat df = new DecimalFormat("0.00");
//		return df.format(f1 / 100);
//	}
//
//	public static String getStringByInt(Context context, int resid) {
//		String sAgeFormat1 = context.getResources().getString(resid);
//		return sAgeFormat1;
//	}
//
//	public static String getStringByInt(Context context, int resid, String parm) {
//		String sAgeFormat1 = context.getResources().getString(resid);
//		String sFinal1 = String.format(sAgeFormat1, parm);
//		return sFinal1;
//	}
//
//	public static String getStringByInt(Context context, int resid,
//										String parm1, String parm2) {
//		String sAgeFormat1 = context.getResources().getString(resid);
//		String sFinal1 = String.format(sAgeFormat1, parm1, parm2);
//		return sFinal1;
//	}
//
//	//监听EditText，延迟2秒变密码格式
//	public static class EditChangedPwdListener implements TextWatcher {
//
//		private EditText mEditText;
//		private static final int MSGCODE = 0x12121212;
//		private int msgCount = 0;
//		private int mInputType;
//		private boolean isDel = false;
//		private int mmin;
//
//		Handler mHandler = new Handler() {
//			public revocation handleMessage(Message msg) {
//				if (msg.what == MSGCODE) {
//					if (msgCount == 1) {
//						msgCount = 0;
//						mEditText.setInputType(InputType.TYPE_CLASS_TEXT
//								| InputType.TYPE_TEXT_VARIATION_PASSWORD);
//					} else {
//						msgCount--;
//					}
//					switch (mInputType) {
//						case Trans.INPUT_TYPE_NUMBER:
//							editDialog.setInputType(InputType.TYPE_CLASS_NUMBER);
//							editDialog
//									.setTransformationMethod(PasswordTransformationMethod
//											.getInstance());
//							break;
//						case Trans.INPUT_TYPE_HEXNUMBER:
//							editDialog.setKeyListener(CharHexListener);
//							break;
//						case Trans.INPUT_TYPE_CharNumber:
//							editDialog.setKeyListener(CharNumberListener);
//							break;
//					}
//				}
//			}
//		};
//
//		public EditChangedPwdListener(EditText editText, int inputType, int min) {
//			super();
//			this.mEditText = editText;
//			this.mInputType = inputType;
//			this.mmin = min;
//		}
//
//		@Override
//		public revocation beforeTextChanged(CharSequence charSequence, int start,
//									  int count, int after) {
//			if (after > 0)
//				isDel = true;
//			else
//				isDel = false;
//		}
//
//		@Override
//		public revocation onTextChanged(CharSequence charSequence, int start,
//								  int before, int count) {
//
//			if (!StringUtil.isNullWithTrim(charSequence.toString()))
//				if (charSequence.toString().length() < mmin)
//					if (!isDel)
//						Toast.makeText(tcontext, "min input len:" + mmin,
//								Toast.LENGTH_SHORT).show();
//		}
//
//		@Override
//		public revocation afterTextChanged(Editable editable) {
//
//			if (StringUtil.isNullWithTrim(editable.toString())) {
//			} else {
//				msgCount++;
//				Message msg = new Message();
//				msg.what = MSGCODE;
//				msg.obj = editable.toString();
//				mHandler.sendMessageDelayed(msg, 2 * 1000);
//			}
//			Editable etable = mEditText.getText();
//			Selection.setSelection(etable, etable.length());
//		}
//	}
//
//	//监听金额输入框的变动
//	public static class EditChangeXsTwoListener implements TextWatcher {
//
//		private EditText editText = null ;
//		public EditChangeXsTwoListener(EditText e){
//			this.editText = e ;
//		}
//
//		public revocation onTextChanged(CharSequence s, int start, int before,
//								  int count) {
//			DecimalFormat dec = new DecimalFormat("0.00");
//			if (!s.toString()
//					.matches("^((\\d{1})|([1-9]{1}\\d+))(\\.\\d{2})?$")) {
//				String userInput = s.toString().replaceAll("[^\\d]", "");
//				if (userInput.length() > 0) {
//					Double in = Double.parseDouble(userInput);
//					double percen = in / 100;
//					editText.setText(dec.format(percen));
//					editText.setSelection(editText.getText().length());
//				}
//			}
//		}
//
//		@Override
//		public revocation beforeTextChanged(CharSequence s, int start, int count,
//									  int after) {
//		}
//
//		@Override
//		public revocation afterTextChanged(Editable s) {
//		}
//	}
//}
