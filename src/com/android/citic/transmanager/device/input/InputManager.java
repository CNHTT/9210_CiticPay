package com.android.citic.transmanager.device.input;

import android.app.Instrumentation;
import android.content.Context;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.citic.appmanager.log.Logger;
import com.android.citic.lib.utils.ISOUtil;
import com.android.citic.lib.utils.StringUtil;
import com.android.citic.transmanager.trans.Tcode;
import com.android.keyboard.DesertKeyListener;
import com.android.keyboard.DesertKeyValue;
import com.android.keyboard.DesertKeybord;


import org.apache.commons.lang.StringUtils;

import java.text.DecimalFormat;

/**
 * Created by zhouqiang on 2017/3/16.
 */

public class InputManager {

    /**
     * 外界输入类型控制定义
     */
    public static final int INPUT_TYPE_NUMBER = 0; // 数字
    public static final int INPUT_TYPE_NUMBERDECIMEL = 1;// 0.00 金额格式(不含小数点)
    public static final int INPUT_TYPE_HEXNUMBER = 2; // 16 0-9 a-f
    public static final int INPUT_TYPE_CharNumber = 3; // 字母数字
    public static final int INPUT_TYPE_NONE = 4; // all
    public static final int INPUT_TYPE_PWD_1 = 16; // 输入立即变*//0x10
    public static final int INPUT_TYPE_PWD_2 = 32;// 延迟1-2S变* 0x20
    public static final int INPUT_TYPE_ALLOWNULL = 64; // 是否允许为空 0x40

    private DesertKeybord desertKeybord ;
    private EditText editText ;
    private Context context ;
    private int type ;

    public InputManager(Context c , DesertKeybord k , EditText e , int t){
        this.context = c ;
        this.desertKeybord = k ;
        this.editText = e ;
        this.type = t ;
        init();
    }

    private InputListener listener ;
    private int timeout ;

    public void getInput(int to , InputListener l){
        this.listener = l ;
        this.timeout = to ;
        if(null == l){
            listener.fail(Tcode.T_invoke_para_err);
        }
    }

    final class KeyListener implements DesertKeyListener {
        @Override
        public void onVibrate(int i) {

        }

        @Override
        public void onChar() {

        }

        @Override
        public void onInputKey(int i) {
            switch(i) {
                case DesertKeyValue.KEY_CLR:
                    sendKeyCode(KeyEvent.KEYCODE_DEL);
                    break;
                case DesertKeyValue.KEY_ESC:
                    listener.fail(Tcode.T_user_cancel_input);
                    break;
                case DesertKeyValue.KEY_OK:
                    handleConfirmMsg();
                    break;
                case DesertKeyValue.KEY_0:
                    sendKeyCode(KeyEvent.KEYCODE_0);
                    break;
                case DesertKeyValue.KEY_1:
                    sendKeyCode(KeyEvent.KEYCODE_1);
                    break;
                case DesertKeyValue.KEY_2:
                    sendKeyCode(KeyEvent.KEYCODE_2);
                    break;
                case DesertKeyValue.KEY_3:
                    sendKeyCode(KeyEvent.KEYCODE_3);
                    break;
                case DesertKeyValue.KEY_4:
                    sendKeyCode(KeyEvent.KEYCODE_4);
                    break;
                case DesertKeyValue.KEY_5:
                    sendKeyCode(KeyEvent.KEYCODE_5);
                    break;
                case DesertKeyValue.KEY_6:
                    sendKeyCode(KeyEvent.KEYCODE_6);
                    break;
                case DesertKeyValue.KEY_7:
                    sendKeyCode(KeyEvent.KEYCODE_7);
                    break;
                case DesertKeyValue.KEY_8:
                    sendKeyCode(KeyEvent.KEYCODE_8);
                    break;
                case DesertKeyValue.KEY_9:
                    sendKeyCode(KeyEvent.KEYCODE_9);
                    break;
            }
        }

        @Override
        public void onClr() {

        }

        @Override
        public void onCannel() {

        }

        @Override
        public void onEnter(String s) {

        }
    }

    private void handleConfirmMsg(){
        switch (type){
            case INPUT_TYPE_NUMBERDECIMEL :
                String result = editText.getText().toString().trim() ;
                if (!isNULL && !result.equals("0.00")) {
                    Logger.debug("InputManager>>result="+result);
                    listener.success( StringUtils.replace(result, ".", "") );
                }
                break;
            case INPUT_TYPE_PWD_1:
                result = editText.getText().toString().trim() ;
                if (!isNULL && !StringUtil.isNullWithTrim(result)) {
                    Logger.debug("InputManager>>result="+result);
                    listener.success(result);
                }
                break;
            case INPUT_TYPE_NUMBER:
                result = editText.getText().toString().trim() ;
                if (!isNULL && !StringUtil.isNullWithTrim(result)) {
                    Logger.debug("InputManager>>result="+result);
                    if(result.length() < 6){
                        result = ISOUtil.padleft(result , 6 , '0');
                    }
                    listener.success(result);
                }
                break;
            default:
                break;
        }
    }

    private void sendKeyCode(final int keyCode){
        new Thread () {
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                }catch(Exception e){
                    Logger.debug("Exception when sendPointerSync");
                }
            }
        }.start();
    }

    private boolean isNULL ;
    private boolean isPass1 ;
    private boolean isPass2 ;
    private void init(){
        desertKeybord.setListener(new KeyListener());
        desertKeybord.setVisibility(View.VISIBLE);
        if(context.getResources().getConfiguration().locale.getCountry().equals("CN")){
            desertKeybord.setLanguage("ch");
        }else {
            desertKeybord.setLanguage("en");
        }
        final int inputType = type ;
        if(( inputType & 0x40 ) != 0)
            isNULL = true ;
        if ((inputType & 0x10) != 0) {
            isPass1 = true;
        } else {
            if ((inputType & 0x20) != 0) {
                isPass2 = true;
            }
        }

        editText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
        switch (inputType) {
            case INPUT_TYPE_NUMBER:
//                if (isPass1){
//                    editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                } else if (isPass2) {
//                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    editText.addTextChangedListener(new TransUtil.EditChangedPwdListener(editText, inputType, 1));
//                } else{
//                    editText.setInputType(InputType.TYPE_NULL);
//                }
                break;
            case INPUT_TYPE_CharNumber:
                if (isPass1){
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }else if (isPass2){
                    editText.addTextChangedListener(new EditChangedPwdListener(editText, inputType, 1));
                }
                editText.setKeyListener(CharNumberListener);
                break;
            case INPUT_TYPE_NONE:
                if (isPass1)
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                else if (isPass2) {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                    editText.addTextChangedListener(new EditChangedPwdListener(editText, inputType, 1));
                } else{
                    editText.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                break;
            case INPUT_TYPE_HEXNUMBER:
                if (isPass1){
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }else if (isPass2){
                    editText.addTextChangedListener(new EditChangedPwdListener(editText, inputType, 1));
                }
                editText.setKeyListener(CharHexListener);
                break;
            case INPUT_TYPE_NUMBERDECIMEL:
                editText.setText("0.00");
                editText.setCursorVisible(false);
                editText.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        editText.requestFocus();
                        return false;
                    }
                });
                editText.setRawInputType(Configuration.KEYBOARD_12KEY);
                editText.addTextChangedListener(new EditChangeXsTwoListener(editText));
                break;
            case INPUT_TYPE_PWD_1:
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                break;
            case INPUT_TYPE_PWD_2:
                editText.addTextChangedListener(new EditChangedPwdListener(editText, inputType, 1));
                break;
            default:
                break;
        }
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true ;
            }
        });
        editText.setSelection(editText.getText().length());
        editText.setFilters(new InputFilter[] { new InputFilter.LengthFilter( 10 ) });
    }

    private static final String onlyHexString = "abcdefABCDEF0123456789" ;
    private static final String onlyCanInput = "qwertyuioplkjhgfdsazxcvbnmQWERTYUIOPLKJHGFDSAZXCVBNM1234567890" ;
    //监听EditText，延迟2秒变密码格式
    public class EditChangedPwdListener implements TextWatcher {

        private EditText mEditText;
        private static final int MSGCODE = 0x12121212;
        private int msgCount = 0;
        private int mInputType;
        private boolean isDel = false;
        private int mmin;

        Handler mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == MSGCODE) {
                    if (msgCount == 1) {
                        msgCount = 0;
                        mEditText.setInputType(InputType.TYPE_CLASS_TEXT
                                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        msgCount--;
                    }
                    switch (mInputType) {
                        case InputManager.INPUT_TYPE_NUMBER:
                            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                            break;
                        case InputManager.INPUT_TYPE_HEXNUMBER:
                            editText.setKeyListener(CharHexListener);
                            break;
                        case InputManager.INPUT_TYPE_CharNumber:
                            editText.setKeyListener(CharNumberListener);
                            break;
                    }
                }
            }
        };

        public EditChangedPwdListener(EditText editText, int inputType, int min) {
            super();
            this.mEditText = editText;
            this.mInputType = inputType;
            this.mmin = min;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            if (after > 0)
                isDel = true;
            else
                isDel = false;
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

            if (!StringUtil.isNullWithTrim(charSequence.toString()))
                if (charSequence.toString().length() < mmin)
                    if (!isDel)
                        Toast.makeText(context, "min input len:" + mmin, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (StringUtil.isNullWithTrim(editable.toString())) {
            } else {
                msgCount++;
                Message msg = new Message();
                msg.what = MSGCODE;
                msg.obj = editable.toString();
                mHandler.sendMessageDelayed(msg, 2 * 1000);
            }
            Editable etable = mEditText.getText();
            Selection.setSelection(etable, etable.length());
        }
    }

    // 只能输入16进制 0-9 a-f
    public DigitsKeyListener CharHexListener = new DigitsKeyListener() {
        @Override
        public int getInputType() {
            return InputType.TYPE_TEXT_VARIATION_PASSWORD;
        }

        @Override
        protected char[] getAcceptedChars() {
            char[] data = onlyHexString.toCharArray();
            return data;
        }
    };

    // 只能输入字母和数字
    public DigitsKeyListener CharNumberListener = new DigitsKeyListener() {

        @Override
        public int getInputType() {
            return InputType.TYPE_TEXT_VARIATION_PASSWORD;
        }

        @Override
        protected char[] getAcceptedChars() {
            char[] data = onlyCanInput.toCharArray();
            return data;
        }
    };

    //监听金额输入框的变动
    public static class EditChangeXsTwoListener implements TextWatcher {

        private EditText editText = null ;
        public EditChangeXsTwoListener(EditText e){
            this.editText = e ;
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            DecimalFormat dec = new DecimalFormat("0.00");
            if (!s.toString()
                    .matches("^((\\d{1})|([1-9]{1}\\d+))(\\.\\d{2})?$")) {
                String userInput = s.toString().replaceAll("[^\\d]", "");
                if (userInput.length() > 0) {
                    Double in = Double.parseDouble(userInput);
                    double percen = in / 100;
                    editText.setText(dec.format(percen));
                    editText.setSelection(editText.getText().length());
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
