package com.p_runtext.p_runtext.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.p_runtext.p_runtext.R;
import com.p_runtext.p_runtext.utils.ScreenSize;
import com.p_runtext.p_runtext.utils.TimerHelper;

import java.util.Locale;

/*
 * Created by qianli.ma on 2019/7/12 0012.
 */
@SuppressLint("AppCompatCustomView")
public class RootMaMarText extends TextView {

    private final int R_TO_L = 0;// 从右到左
    private final int L_TO_R = 1;// 从左到右
    private final int SO_SLOW = 100;// 非常慢
    private final int SLOW = 80;// 慢
    private final int NORMAL = 60;// 正常
    private final int FAST = 40;// 快
    private final int SO_FAST = 25;// 非常快
    private final int GRAVIRY_START = 0;// 靠左(阿拉伯语靠右)
    private final int GRAVIRY_CENTER = 1;// 居中
    private final int GRAVIRY_END = 2;// 靠右(阿拉伯语靠左)

    private float textSizeRate = 0.02f; // 字体大小比例(相对屏幕)
    private int textColor = Color.RED; // 字体的颜色
    private int backGroundColor = Color.WHITE;// 背景色
    private int direction;// 滚动方向 0 从右向左  1 从左向右
    private int speed;// 速度等级
    private String text;// 文本
    private int gravity;// 对齐方式
    private String reverseLanguages;// 需要反转的语言
    private int testDuration = -1;// 提供给开发的测试间距
    private int step = 5;// 步长(默认5)

    private Paint paint;
    private int baseline;
    private TimerHelper timerHelper;
    private Context context;
    private int x;// 初始位置
    private int stringWidth;// 字符宽度
    private int widgetWidth;// 控件宽度
    private boolean isStart;// 是否已经开启了timer
    private boolean isInited;// 是否已经初始化过原始文本
    private boolean isSpecialLanguage;// 是否为特殊语言
    private String dyText;// 动态设置时临时存储的变量
    private String specialLanguages;// 特殊的语言适配(设置了特殊语言, 则使用原生的textview)

    public RootMaMarText(Context context) {
        this(context, null, 0);
    }

    public RootMaMarText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RootMaMarText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        initAttrs(attrs, defStyleAttr);
    }

    /**
     * 初始化属性
     *
     * @param attrs        属性
     * @param defStyleAttr 属性
     */
    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        // 属性提取
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RootMaMarText, defStyleAttr, 0);
        // 优先判断是否为特殊语言
        specialLanguages = a.getString(R.styleable.RootMaMarText_marSpecialLanguage);
        isSpecialLanguage = resetTvBySpecialLanguage(specialLanguages);
        textColor = a.getColor(R.styleable.RootMaMarText_marTextcolor, Color.YELLOW);
        textSizeRate = a.getFloat(R.styleable.RootMaMarText_marTextSizeRate, textSizeRate);
        backGroundColor = a.getColor(R.styleable.RootMaMarText_marBackground, Color.BLACK);
        direction = a.getInt(R.styleable.RootMaMarText_marDirection, R_TO_L);// 默认从［右-左］滚动
        text = a.getString(R.styleable.RootMaMarText_marText);
        text = TextUtils.isEmpty(text) ? " " : text;
        speed = a.getInt(R.styleable.RootMaMarText_marSpeedLevel, SO_SLOW);
        reverseLanguages = a.getString(R.styleable.RootMaMarText_marReverseLanguage);
        gravity = a.getInt(R.styleable.RootMaMarText_marGravity, GRAVIRY_CENTER);
        // 重新根据语言来决定是否反转
        direction = resetReverseLanguage() ? L_TO_R : R_TO_L;
        // 测试间隔
        testDuration = a.getInt(R.styleable.RootMaMarText_test_duration, -1);
        // 步长
        step = a.getInt(R.styleable.RootMaMarText_marStep, 5);
        step = step > 10 ? 10 : step;
        a.recycle();
    }

    /**
     * 处理传递过来的特殊语言
     *
     * @param specialLanguages 特殊语言
     * @return 集合
     */
    private boolean resetTvBySpecialLanguage(String specialLanguages) {
        // 1.获取本地语言
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        // 2.获取到系统当前语言ru,pl...
        String currentLanguage = locale.getLanguage();
        // 3.遍历
        if (!TextUtils.isEmpty(specialLanguages)) {
            if (specialLanguages.contains(";")) {
                // 如果包含了［;］则切割
                String[] specis = specialLanguages.split(";");
                for (String needLanguage : specis) {
                    if (needLanguage.equalsIgnoreCase(currentLanguage)) {
                        return true;
                    }
                }
            }else {
                // 否则直接判断
                return specialLanguages.equalsIgnoreCase(currentLanguage);
            }
        } 
        return false;
    }

    /**
     * 根据［isReverse］以及［reverseLanguages］重新设定是否反转
     *
     * @return T: 需要反转
     */
    private boolean resetReverseLanguage() {
        // 如果用户主动设置反转 --> 则反转
        if (direction == L_TO_R) {
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

    /**
     * 初始化画笔
     */
    private void initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setTextSize(calTextsize());
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(textColor);
        paint.setStyle(Paint.Style.FILL);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, calTextsize());
    }

    @Override
    protected void onDetachedFromWindow() {// 在外部销毁该view前先停止定时器
        // TOAT: 适配特殊语言
        if (isSpecialLanguage) {
            super.onDetachedFromWindow();
            return;
        }
        stopTimer();// 停止定时器
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // TOAT: 适配特殊语言
        if (isSpecialLanguage) {
            super.onSizeChanged(w, h, oldw, oldh);
            return;
        }
        super.onSizeChanged(w, h, oldw, oldh);
        // 此处是为了防止setMartext执行速度比onSizeChange的情况下 -- 需要以动态设置的优先
        if (!TextUtils.isEmpty(dyText)) {
            text = dyText;// 以动态设置的优先
        }
        init();// 开始初始化
    }

    /**
     * 根据语言系统进行设置X的初始值
     *
     * @return x 起始坐标
     */
    private int initX() {
        /* 字符长度 <= 控件长度 -- 静态显示的情况 */
        if (stringWidth <= widgetWidth) {
            if (gravity == 1) {// CENTER
                paint.setTextAlign(Paint.Align.CENTER);
                return getWidth() / 2;
            }

            if (gravity == 0) {// START
                if (direction == R_TO_L) {
                    paint.setTextAlign(Paint.Align.LEFT);
                    return 0;
                } else {
                    paint.setTextAlign(Paint.Align.RIGHT);
                    return widgetWidth;
                }
            }

            if (gravity == 2) {// END
                if (direction == R_TO_L) {
                    paint.setTextAlign(Paint.Align.RIGHT);
                    return widgetWidth;
                } else {
                    paint.setTextAlign(Paint.Align.LEFT);
                    return 0;
                }
            }

            paint.setTextAlign(Paint.Align.CENTER);
            return widgetWidth / 2;

        } else {/* 字符长度 > 控件长度 -- 动态显示 */
            if (direction == R_TO_L) {
                paint.setTextAlign(Paint.Align.LEFT);
                return widgetWidth;
            } else {
                paint.setTextAlign(Paint.Align.RIGHT);
                return 0;
            }
        }
    }

    /**
     * 获取字符以及控件的宽度
     */
    private void getAllWidth() {
        stringWidth = (int) paint.measureText(text);
        widgetWidth = getWidth();
    }

    /**
     * 开始初始化
     */
    private void init() {
        // 初始化画笔
        initPaint();
        // 计算字符实际宽度
        getAllWidth();
        // 计算初始化x的位置
        x = initX();
        // 初始化原始文本样式
        initOriText();
    }

    /**
     * 初始化原始文本样式
     * <p>
     * (只能从这里设置1次, 不能放在OnDraw里设置,
     * 因为每个setxxxx的方法一定会调用invalidate, 这样的话就等于递归触发onDraw)
     */
    private void initOriText() {
        if (!isInited) {/* 只初始化一次 */
            // 绘制原文本不变
            setLines(1);
            setMaxLines(1);
            setText(text);
            setGravity(Gravity.CENTER);
            setTextColor(backGroundColor);
            setBackgroundColor(backGroundColor);
            isInited = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TOAT: 适配特殊语言
        if (isSpecialLanguage) {
            super.onDraw(canvas);
            return;
        }
        // 第一次绘制时启动线程
        if (!isStart) {
            start();
        }

        super.onDraw(canvas);
        if (isInited) {
            // 绘制自己的文本
            baseline = getBaseline();
            paint.setTextSize(calTextsize());
            canvas.drawText(text, x, baseline, paint);
        }

    }

    /**
     * 开始
     */
    private void start() {
        isStart = true;
        stopTimer();
        timerHelper = new TimerHelper((Activity) context) {
            @Override
            public void doSomething() {
                calNewX();// 计算新的X坐标
                invalidate();// 刷新
                ((View) getParent()).invalidate();// 在自身view刷新后, 此处要让父布局也刷新, 否则父布局的属性会失效
            }
        };
        timerHelper.start(testDuration != -1 ? testDuration : speed);
    }

    /**
     * 计算新的X坐标
     */
    private void calNewX() {
        if (stringWidth > widgetWidth) {
            if (direction == R_TO_L) {// 从右到左
                if (x > -stringWidth) {
                    x -= step;
                } else {
                    x = widgetWidth;
                }

            } else {// 从左到右
                if (x < widgetWidth + stringWidth) {
                    x += step;
                } else {
                    x = 0;
                }
            }
        }
    }

    /**
     * 注销定时器
     */
    private void stopTimer() {
        if (timerHelper != null) {
            timerHelper.stop();
            timerHelper = null;
        }
    }

    /* -------------------------------------------- public -------------------------------------------- */

    /**
     * 动态设置text
     *
     * @param marText 需要重新设置的文本
     */
    public void setMartext(String marText) {
        // TOAT: 适配特殊语言
        if (isSpecialLanguage) {
            setText(marText);
            return;
        }
        dyText = marText;// 假设本方法比onSizeChange执行速度快的情况 -- 此处需要在onSizeChange重新判断以本方法的text为准
        text = marText;// 假设本方法比onSizeChange执行速度慢的情况那么需要使用martext去覆盖本地的text
        init();// 重新初始化
        // 刷新
        invalidate();
    }

    /**
     * 停止
     */
    private void stop() {
        stopTimer();
    }
}
