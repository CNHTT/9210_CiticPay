package com.android.keyboard;

/**
 * Created by he on 2017/4/21.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Vector;


public class DesertKeybord extends View {
    private DesertKeyListener keyBoardListener = null;
    private boolean bNormal = false;
    private Canvas curCanvas = null;
    private Paint paintPar = new Paint();
    private String clear = "Cancel";
    private String confirm = "Confirm";
    private String backspace = "Backspace";
    Vector<com.android.keyboard.DesertKeybord.keyDefine> keys = new Vector();
    ArrayList<com.android.keyboard.DesertKeybord.KeyDef> mKeys = new ArrayList();
    private int view_w = 0;
    private int view_h = 0;

    public void setListener(DesertKeyListener keyBoardListener) {
        this.keyBoardListener = keyBoardListener;
        this.initKeyNormal();
    }

    public DesertKeybord(Context context) {
        super(context);
    }

    public DesertKeybord(Context context, AttributeSet attr) {
        super(context, attr);
    }

    private void setColor(int color) {
        if(this.curCanvas != null) {
            this.paintPar.setColor(Color.rgb((color & 16711680) >> 16, (color & '\uff00') >> 8, (color & 255) >> 0));
            this.paintPar.setStyle(Style.FILL);
        }

    }

    public void setLanguage(String flag) {
        if(flag.equals("ZH") || flag.equals("zh")) {
            this.clear = "取消";
            this.confirm = "确认";
            this.backspace = "退格";
        }

        if(flag.equals("EN") || flag.equals("en")) {
            this.clear = "CANCEL";
            this.confirm = "CONFIRM";
            this.backspace = "BACKSPACE";
        }

    }

    private void drawRect(int x, int y, int w, int h) {
        if(this.curCanvas != null) {
            this.curCanvas.drawRect((float)x, (float)y, (float)(x + w - 1), (float)(y + h - 1), this.paintPar);
        }

    }

    public void setFont(String family, int size) {
        if(this.curCanvas != null) {
            this.paintPar.setTextAlign(Align.LEFT);
            this.paintPar.setAntiAlias(true);
            this.paintPar.setTextSize((float)size);
        }

    }

    public void drawString(String text, int x, int y) {
        if(this.curCanvas != null) {
            FontMetrics f = this.paintPar.getFontMetrics();
            float fontHeight = f.bottom - f.top;
            float baseline = (float)y + fontHeight / 2.0F - f.bottom;
            this.curCanvas.drawText(text, (float)x, baseline, this.paintPar);
        }

    }

    private void unSelect() {
        for(int k = 0; k < this.keys.size(); ++k) {
            (this.keys.elementAt(k)).flag = 0;
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean retProcess = false;
        int count = event.getPointerCount();

        for(int i = 0; i < count; ++i) {
            int id = event.getPointerId(i);
            boolean action = false;
            float x = (float)((int)event.getX(i));
            float y = (float)((int)event.getY(i));
            int var9 = event.getAction();
            if(this.processTouchEvent(id, var9, x, y)) {
                retProcess = true;
            } else if(var9 == 1) {
                this.unSelect();
            }
        }

        return retProcess;
    }

    private boolean processTouchEvent(int id, int action, float x, float y) {
        if(id > 0) {
            return false;
        } else {
            int k;
            RectF button;
            for(k = 0; k < this.keys.size(); ++k) {
                button = (this.keys.elementAt(k)).getRect();
                if((this.keys.elementAt(k)).flag != 1 || button.left > x || button.right < x || button.top > y || button.bottom < y) {
                    (this.keys.elementAt(k)).flag = 0;
                }
            }

            for(k = 0; k < this.keys.size(); ++k) {
                button = (this.keys.elementAt(k)).getRect();
                if(button.left <= x && button.right >= x && button.top <= y && button.bottom >= y) {
                    if((this.keys.elementAt(k)).flag == 0 && action == 0) {
                        (this.keys.elementAt(k)).flag = 1;
                        this.invalidate();
                        this.keyBoardListener.onVibrate(50);
                        return true;
                    }

                    if((this.keys.elementAt(k)).flag == 1 && action == 1) {
                        (this.keys.elementAt(k)).flag = 0;
                        this.invalidate();
                        this.onChar((this.keys.elementAt(k)).keyVal);
                        return true;
                    }
                }
            }

            return false;
        }
    }

    private void onChar(int keyVal) {
        if(this.bNormal) {
            this.keyBoardListener.onInputKey(keyVal);
        } else if(keyVal == 130) {
            this.keyBoardListener.onClr();
        } else if(keyVal == 128) {
            this.keyBoardListener.onCannel();
        } else {
            if(keyVal == 129) {
                this.setVisibility(View.VISIBLE);//edit by liyo
            }

            if(keyVal >= 0 && keyVal <= 9) {
                this.keyBoardListener.onChar();
            }

        }
    }

    private void initKeyNormal() {
        ArrayList mTemp = new ArrayList();
        mTemp.add(new KeyDef("0", 0));
        mTemp.add(new KeyDef("1", 1));
        mTemp.add(new KeyDef("2", 2));
        mTemp.add(new KeyDef("3", 3));
        mTemp.add(new KeyDef("4", 4));
        mTemp.add(new KeyDef("5", 5));
        mTemp.add(new KeyDef("6", 6));
        mTemp.add(new KeyDef("7", 7));
        mTemp.add(new KeyDef("8", 8));
        mTemp.add(new KeyDef("9", 9));
        this.mKeys.clear();
        this.mKeys.addAll(mTemp);
        this.bNormal = true;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.v("onMeasure", this.getHeight() + "   " + this.getWidth());
        if(this.view_w == this.getWidth() || this.view_h != this.getHeight()) {
            this.view_w = this.getWidth();
            this.view_h = this.getHeight();
            this.keys.clear();
            this.invalidate();
        }

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.curCanvas = canvas;
        Rect bounds = canvas.getClipBounds();
        this.setColor(0);
        this.drawRect(bounds.left, bounds.top, bounds.width(), bounds.height());
        int text_width = bounds.width() / 4;
        int text_width_3clo = bounds.width() / 3;
        int text_height = bounds.height() / 5;
        int var7;
        if(this.keys.size() == 0 && this.mKeys.size() > 0) {
            byte k = 1;
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 0 + text_width_3clo / 2, bounds.top + text_height * 0 + text_height / 2, text_width_3clo * 2, text_height, "SZFP", 135));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 3 / 2 + text_width_3clo / 2, bounds.top + text_height * 0 + text_height / 2, text_width_3clo * 2, text_height, this.backspace, 130));
            int var10004 = bounds.left + text_width_3clo * 0 + text_width_3clo / 2;
            int var10005 = bounds.top + text_height * 1 + text_height / 2;
            var7 = k + 1;
            this.keys.add(new keyDefine(var10004, var10005, text_width_3clo, text_height, this.mKeys.get(k)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 1 + text_width_3clo / 2, bounds.top + text_height * 1 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 2 + text_width_3clo / 2, bounds.top + text_height * 1 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 0 + text_width_3clo / 2, bounds.top + text_height * 2 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 1 + text_width_3clo / 2, bounds.top + text_height * 2 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 2 + text_width_3clo / 2, bounds.top + text_height * 2 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 0 + text_width_3clo / 2, bounds.top + text_height * 3 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 1 + text_width_3clo / 2, bounds.top + text_height * 3 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 2 + text_width_3clo / 2, bounds.top + text_height * 3 + text_height / 2, text_width_3clo, text_height, this.mKeys.get(var7++)));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 0 + text_width_3clo / 2, bounds.top + text_height * 4 + text_height / 2, text_width_3clo, text_height, "0", 0));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 1 + text_width_3clo / 2, bounds.top + text_height * 4 + text_height / 2, text_width_3clo, text_height, this.clear, 128));
            this.keys.add(new keyDefine(bounds.left + text_width_3clo * 2 + text_width_3clo / 2, bounds.top + text_height * 4 + text_height / 2, text_width_3clo, text_height, this.confirm, 129));
        }

        this.setColor(0);
        canvas.drawLine((float)bounds.left, (float)bounds.top, (float)bounds.right, (float)bounds.top, this.paintPar);
        canvas.drawLine((float)(bounds.right - 1), (float)bounds.top, (float)(bounds.right - 1), (float)bounds.bottom, this.paintPar);
        canvas.drawLine((float)(bounds.right - 1), (float)(bounds.bottom - 1), (float)bounds.left, (float)(bounds.bottom - 1), this.paintPar);
        canvas.drawLine((float)bounds.left, (float)bounds.top, (float)bounds.left, (float)bounds.bottom, this.paintPar);

        for(var7 = text_width; var7 < bounds.width(); var7 += text_width) {
            canvas.drawLine((float)var7, 0.0F, (float)var7, (float)bounds.height(), this.paintPar);
        }

        for(var7 = text_height; var7 < bounds.height(); var7 += text_height) {
            canvas.drawLine(0.0F, (float)var7, (float)bounds.width(), (float)var7, this.paintPar);
        }

        this.setFont("宋体", 30);
        this.paintPar.setTextAlign(Align.CENTER);

        for(var7 = 0; var7 < this.keys.size(); ++var7) {
            if((this.keys.elementAt(var7)).keyVal >= 0 && (this.keys.elementAt(var7)).keyVal <= 9) {
                this.setFont("宋体", 50);
                this.paintPar.setTextAlign(Align.CENTER);
            } else {
                this.setFont("宋体", 40);
                this.paintPar.setTextAlign(Align.CENTER);
            }

            if((this.keys.elementAt(var7)).flag == 1) {
                this.setColor(0);
            } else {
                switch((this.keys.elementAt(var7)).keyVal) {
                    case 128:
                        this.setColor(Color.parseColor("#FF0000")); //CANCEL
                        break;
                    case 129:
                        this.setColor(Color.parseColor("#00FF00"));  //CONFIRM
                        break;
                    case 130:
                        this.setColor(Color.parseColor("#ffd700")); //BACKSPACE
                        break;
                    case 131:
                        this.setColor(-16776961);
                        break;
                    case 132:
                        this.setColor(-16776961);
                        break;
                    case 133:
                        this.setColor(-16776961);
                        break;
                    case 134:
                        this.setColor(-16776961);
                        break;
                    case 135:
                        this.setColor(Color.parseColor("#98fb98"));  //KEYBOARD
                        break;
                    default:
                        this.setColor(Color.rgb(255, 251, 240));//NUM KEY BACKGROUND
                }
            }

            canvas.drawRoundRect((this.keys.elementAt(var7)).getRect(), 5.0F, 5.0F, this.paintPar);
            this.setColor(0);
            this.drawString((this.keys.elementAt(var7)).text, (this.keys.elementAt(var7)).x, (this.keys.elementAt(var7)).y);
        }

    }

    private class KeyDef {
        public String key = "";
        public int keyvalue = 0;

        public KeyDef(String key, int val) {
            this.key = key;
            this.keyvalue = val;
        }
    }

    private class keyDefine {
        protected int x = 0;
        protected int y = 0;
        protected String text = "";
        protected int w = 0;
        protected int h = 0;
        protected int flag = 0;
        protected int keyVal;

        public keyDefine(int x, int y, int w, int h, KeyDef key) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.flag = 0;
            this.text = key.key;
            this.keyVal = key.keyvalue;
        }

        public keyDefine(int x, int y, int w, int h, String val, int keyVal) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.flag = 0;
            this.text = val;
            this.keyVal = keyVal;
        }

        public RectF getRect() {
            RectF textRect = new RectF();
            int tw = this.w - 6;
            int th = this.h - 6;
            textRect.left = (float)(this.x - tw / 2);
            textRect.top = (float)(this.y - th / 2);
            textRect.right = (float)(this.x + tw / 2);
            textRect.bottom = (float)(this.y + th / 2);
            return textRect;
        }
    }
}
