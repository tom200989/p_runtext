package com.p_runtext.p_runtext.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Handler;
import android.os.LocaleList;
import android.os.Message;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.p_runtext.p_runtext.R;
import com.p_runtext.p_runtext.utils.DipPx;
import com.p_runtext.p_runtext.utils.ScreenSize;

import java.util.Locale;

/* url1: https://blog.csdn.net/weixin_37730482/article/details/80675427 */
/* url2: https://www.jb51.net/article/90968.htm */
public class RootMaMarText extends SurfaceView implements SurfaceHolder.Callback {

    private float textSizeRate = 0.03f; // 字体大小比例(相对屏幕)
    private int textColor = Color.RED; // 字体的颜色
    private int backGroundColor = Color.WHITE;// 背景色
    private boolean isRepeat;// 是否重复滚动
    private int startSide;// 开始滚动的位置(0是从最左面开始 1是从最末尾开始)
    private int direction;// 滚动方向 0 向左滚动   1向右滚动
    private int speed;// 速度等级
    private String text;// 文本
    private int gravity;// 对齐方式

    public Context context;
    private SurfaceHolder holder;
    private TextPaint textPaint;
    private MaThread thread;
    private int textWidth = 0, textHeight = 0;
    public int currentX = 0;// 当前x的位置
    public int sepX = 2;// 每一步滚动的距离
    private int widgetWidth;// 控件宽度
    private boolean isNeedScroll = true;// 是否需要滚动
    private String reverseLanguages;
    private boolean isReverse;

    public RootMaMarText(Context context) {
        this(context, null);
    }

    public RootMaMarText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RootMaMarText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs, defStyleAttr);// 初始化属性
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        // 属性提取
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RootMaMarText, defStyleAttr, 0);
        textColor = a.getColor(R.styleable.RootMaMarText_marTextcolor, Color.BLACK);
        textSizeRate = a.getFloat(R.styleable.RootMaMarText_marTextSizeRate, 0.03f);
        backGroundColor = a.getColor(R.styleable.RootMaMarText_marBackground, Color.RED);
        isRepeat = a.getBoolean(R.styleable.RootMaMarText_marIsRepeat, true);
        startSide = a.getInt(R.styleable.RootMaMarText_marStartSide, 0);// 默认左侧开始
        direction = a.getInt(R.styleable.RootMaMarText_marDirection, 0);// 默认向左滚动
        text = a.getString(R.styleable.RootMaMarText_marText);
        speed = a.getInt(R.styleable.RootMaMarText_marSpeedLevel, 240);
        reverseLanguages = a.getString(R.styleable.RootMaMarText_marReverseLanguage);
        isReverse = a.getBoolean(R.styleable.RootMaMarText_marIsReverse, false);
        gravity = a.getInt(R.styleable.RootMaMarText_marGravity, 1);
        // 重新根据语言来决定是否反转
        startSide = direction = resetReverseLanguage() ? 1 : 0;
        a.recycle();
        // 设置holder
        holder = this.getHolder();
        holder.addCallback(this);
        textPaint = new TextPaint();
        setTextPaint(text);
        setFocusable(true);
        requestFocus();
    }

    /**
     * 根据［isReverse］以及［reverseLanguages］重新设定是否反转
     *
     * @return T: 需要反转
     */
    private boolean resetReverseLanguage() {
        // 如果用户主动设置反转 --> 则反转
        if (isReverse) {
            return true;
        }

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        // 获取到系统当前语言fr,de,zh...
        String currentLanguage = locale.getLanguage();
        Log.i("ma_lang", "lang: " + currentLanguage);
        // 遍历
        if (!TextUtils.isEmpty(reverseLanguages)) {
            if (reverseLanguages.contains(";")) {
                String[] split = reverseLanguages.split(";");
                for (String needLanguage : split) {
                    if (needLanguage.equalsIgnoreCase(currentLanguage)) {
                        return true;
                    }
                }
            } else {
                return reverseLanguages.equalsIgnoreCase(currentLanguage);
            }
        }
        return false;
    }

    /**
     * 设置文本属性
     *
     * @param text 文本
     */
    protected void setTextPaint(String text) {
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTextSize(calTextsize());
        textPaint.setColor(textColor);
        textPaint.setStrokeWidth(0.5f);
        textPaint.setFakeBoldText(true);
        textWidth = (int) textPaint.measureText(text);
        Paint.FontMetrics fontMetrics = textPaint.getFontMetrics();
        textHeight = (int) fontMetrics.bottom;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        currentX = startSide == 0 ? 0 : width - getPaddingLeft() - getPaddingRight();
    }

    /**
     * 计算文本大小
     *
     * @return 文本大小
     */
    private float calTextsize() {
        ScreenSize.SizeBean size = ScreenSize.getSize(context);
        int height = size.height;
        int textSize = (int) (height * textSizeRate);
        return Math.abs(textSize);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.holder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        widgetWidth = getWidth();
        start();// 获取到控件宽度后才启动绘制线程
        thread.isRun = true;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.isRun = false;
        thread = null;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /* -------------------------------------------- class -------------------------------------------- */

    /**
     * 滚动线程
     */
    private class MaThread extends Thread {

        private boolean isRun;//是否在运行

        private MaThread() {
            isRun = true;
        }

        @Override
        public void run() {
            while (isRun) {
                startDraw();
            }
        }
    }

    /**
     * 开始绘制
     */
    private void startDraw() {
        try {
            synchronized (SurfaceHolder.class) {
                if (TextUtils.isEmpty(text)) {
                    Thread.sleep(1000);// 睡眠时间为1秒
                    return;
                }
                Canvas canvas = holder.lockCanvas();
                int paddingLeft = getPaddingLeft();
                int paddingTop = getPaddingTop();
                int paddingRight = getPaddingRight();
                int paddingBottom = getPaddingBottom();

                int contentWidth = getWidth() - paddingLeft - paddingRight;
                int contentHeight = getHeight() - paddingTop - paddingBottom;

                int centeYLine = paddingTop + contentHeight / 2;// 中心线

                /* 核心代码 */
                // 字长 > 控件长 --> 则需要滚动
                isNeedScroll = textWidth > widgetWidth;
                currentX = isNeedScroll ? currentX : setCurrentXByGravity();// 不需要滚动时让字体根据对齐方式设置
                // 如果检测到需要滚动才开始滚动
                if (isNeedScroll) {
                    if (direction == 0) {// 向左滚动
                        if (currentX <= -textWidth) {
                            if (!isRepeat) {// 如果是不重复滚动
                                mHandler.sendEmptyMessage(ROLL_OVER);
                            }
                            currentX = contentWidth;
                        } else {
                            currentX -= sepX;
                        }
                    } else {//  向右滚动
                        if (currentX >= contentWidth) {
                            if (!isRepeat) {//如果是不重复滚动
                                mHandler.sendEmptyMessage(ROLL_OVER);
                            }
                            currentX = -textWidth;
                        } else {
                            currentX += sepX;
                        }
                    }
                }

                if (canvas != null) {
                    canvas.drawColor(backGroundColor);
                    int halfTextHeight = DipPx.dip2px(getContext(), textHeight) / 2;
                    canvas.drawText(text, currentX, centeYLine + halfTextHeight, textPaint);
                }

                holder.unlockCanvasAndPost(canvas);// 结束锁定画图，并提交改变。

                int a = textWidth / text.trim().length();
                int b = a / sepX;
                int c = speed / b == 0 ? 1 : speed / b;

                Thread.sleep(c);// 睡眠时间为移动的频率
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据对齐方式来决定位置
     *
     * @return 需要偏移的left
     */
    private int setCurrentXByGravity() {
        if (gravity == 0) {// 左侧对齐
            return 0;

        } else if (gravity == 1) {// 居中
            return widgetWidth / 2 - textWidth / 2;

        } else if (gravity == 2) {// 右侧
            if (textWidth > widgetWidth) {
                return -(textWidth - widgetWidth);
            } else if (textWidth == widgetWidth) {
                return 0;
            } else {
                return widgetWidth - textWidth;
            }
        } else {
            return 0;
        }
    }

    public static final int ROLL_OVER = 100;// 滚动message标记

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case ROLL_OVER:// 滚动结束
                    stop();
                    break;
            }
        }
    };

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 动态开始滚动
     */
    public void start() {
        if (thread != null && thread.isRun) {
            return;
        }
        thread = new MaThread();// 创建一个绘图线程
        thread.start();
    }

    /**
     * 动态设置文本
     *
     * @param text 文本
     */
    public void setText(String text) {
        if (!TextUtils.isEmpty(text)) {
            setTextPaint(text);
        }
    }

    /**
     * 动态重置滚动
     */
    public void reset() {
        int contentWidth = getWidth() - getPaddingLeft() - getPaddingRight();
        if (startSide == 0)
            currentX = 0;
        else
            currentX = contentWidth;
    }

    /**
     * 动态停止滚动
     */
    public void stop() {
        if (thread != null) {
            thread.isRun = false;
            thread.interrupt();
        }
        thread = null;
    }

}
