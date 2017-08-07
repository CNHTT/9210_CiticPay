//package com.android.citic.transmanager.device.input.util;
//
//import android.app.ActionBar;
//import android.app.Dialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.android.citic.R;
//
//public class CustomDialog extends Dialog {
//
//	/** 是否正在弹框，如果在弹不允许back退出 **/
//	private boolean isDialog;
//
//	public boolean isDialog() {
//		return isDialog;
//	}
//
//	public void setDialog(boolean isDialog) {
//		this.isDialog = isDialog;
//	}
//
//	public CustomDialog(Context context) {
//		super(context);
//	}
//
//	public CustomDialog(Context context, int theme) {
//		super(context, theme);
//	}
//
//	@Override
//	public void show() {
//		super.show();
//	}
//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if (keyCode == event.KEYCODE_BACK)
//			return false;
//		return super.onKeyDown(keyCode, event);
//	}
//
//	@Override
//	public void setOnCancelListener(OnCancelListener listener) {
//		// TODO Auto-generated method stub
//		super.setOnCancelListener(listener);
//	}
//
//	public static class Builder {
//		private Context context;
//		private String title;
//		private String message;
//		private String positiveButtonText;
//		private String negativeButtonText;
//		private View contentView;
//		private OnClickListener positiveButtonClickListener;
//		private OnClickListener negativeButtonClickListener;
//		private Button okBtn;
//
//		public Builder(Context context) {
//			this.context = context;
//		}
//
//		public Builder setMessage(String message) {
//			this.message = message;
//			return this;
//		}
//
//		/**
//		 * Set the Dialog message from resource
//		 * @param title
//		 * @return
//		 */
//		public Builder setMessage(int title) {
//			this.message = (String) context.getText(title);
//			return this;
//		}
//
//		/**
//		 * Set the Dialog title from resource
//		 * @param title
//		 * @return
//		 */
//		public Builder setTitle(int title) {
//			this.title = (String) context.getText(title);
//			return this;
//		}
//
//		/**
//		 * Set the Dialog title from String
//		 * @param title
//		 * @return
//		 */
//
//		public Builder setTitle(String title) {
//			this.title = title;
//			return this;
//		}
//
//		public Builder setContentView(View v) {
//			this.contentView = v;
//			return this;
//		}
//
//		/**
//		 * Set the positive button resource and it's listener
//		 * @param positiveButtonText
//		 * @return
//		 */
//		public Builder setPositiveButton(int positiveButtonText,
//				OnClickListener listener) {
//			this.positiveButtonText = (String) context
//					.getText(positiveButtonText);
//			this.positiveButtonClickListener = listener;
//			return this;
//		}
//
//		public Builder setPositiveButton(String positiveButtonText,
//				OnClickListener listener) {
//			this.positiveButtonText = positiveButtonText;
//			this.positiveButtonClickListener = listener;
//			return this;
//		}
//
//		public Builder setNegativeButton(int negativeButtonText,
//				OnClickListener listener) {
//			this.negativeButtonText = (String) context
//					.getText(negativeButtonText);
//			this.negativeButtonClickListener = listener;
//			return this;
//		}
//
//		public Builder setNegativeButton(String negativeButtonText,
//				OnClickListener listener) {
//			this.negativeButtonText = negativeButtonText;
//			this.negativeButtonClickListener = listener;
//			return this;
//		}
//
//		public CustomDialog create() {
//			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			// instantiate the dialog with the custom Theme
//			final CustomDialog dialog = new CustomDialog(context,R.style.Dialog);
//			View layout = inflater.inflate(R.layout.custom_dialog, null);
//			okBtn = (Button) layout.findViewById(R.id.positiveButton);
//			dialog.addContentView(layout, new ActionBar.LayoutParams(
//					ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
//			// set the dialog title
//			((TextView) layout.findViewById(R.id.title)).setText(title);
//			// set the confirm button
//			if (positiveButtonText != null) {
//				((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
//				if (positiveButtonClickListener != null) {
//					layout.findViewById(R.id.positiveButton)
//							.setOnClickListener(new View.OnClickListener() {
//								public void onClick(View v) {
//									positiveButtonClickListener.onClick(dialog,
//											DialogInterface.BUTTON_POSITIVE);
//								}
//							});
//				}
//			} else {
//				// if no confirm button just set the visibility to GONE
//				layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
//			}
//			// set the cancel button
//			if (negativeButtonText != null) {
//				((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
//				if (negativeButtonClickListener != null) {
//					layout.findViewById(R.id.negativeButton)
//							.setOnClickListener(new View.OnClickListener() {
//								public void onClick(View v) {
//									negativeButtonClickListener.onClick(dialog,
//											DialogInterface.BUTTON_NEGATIVE);
//								}
//							});
//				}
//			} else {
//				// if no confirm button just set the visibility to GONE
//				layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
//			}
//			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(200, 90);
//			layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, -1);
//			if (negativeButtonText != null && positiveButtonText == null)
//				layout.findViewById(R.id.negativeButton).setLayoutParams(layoutParams);
//			if (positiveButtonText != null && negativeButtonText == null)
//				layout.findViewById(R.id.positiveButton).setLayoutParams(layoutParams);
//
//			// set the content message
//			if (message != null) {
//				((TextView) layout.findViewById(R.id.message)).setText(message);
//				// 隐藏确认按钮
//				// okBtn.setVisibility(View.INVISIBLE);
//			} else if (contentView != null) {
//				// if no message set
//				// add the contentView to the dialog body
//				((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
//				((LinearLayout) layout.findViewById(R.id.content)).addView(
//						contentView, new ActionBar.LayoutParams(
//								ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
//			}
//			dialog.setContentView(layout);
//			dialog.setCanceledOnTouchOutside(false);
//			return dialog;
//		}
//	}
//}
