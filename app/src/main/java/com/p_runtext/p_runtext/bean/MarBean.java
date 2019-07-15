package com.p_runtext.p_runtext.bean;

import android.graphics.Color;

/*
 * Created by qianli.ma on 2019/7/10 0010.
 */
public class MarBean {
    
    private float textSizeRate = 0.03f; // 字体大小比例(相对屏幕)
    private int textColor = Color.RED; // 字体的颜色
    private int backGroundColor = Color.WHITE;// 背景色
    private boolean isRepeat;// 是否重复滚动
    private int startSide;// 开始滚动的位置(0是从最左面开始 1是从最末尾开始)
    private int direction;// 滚动方向 0 向左滚动   1向右滚动
    private int speed;// 速度等级
    private String text;// 文本
    private int gravity;// 对齐方式
    private String reverseLanguages;
    private boolean isReverse;

    public MarBean() {
    }

    public float getTextSizeRate() {
        return textSizeRate;
    }

    public void setTextSizeRate(float textSizeRate) {
        this.textSizeRate = textSizeRate;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getBackGroundColor() {
        return backGroundColor;
    }

    public void setBackGroundColor(int backGroundColor) {
        this.backGroundColor = backGroundColor;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public int getStartSide() {
        return startSide;
    }

    public void setStartSide(int startSide) {
        this.startSide = startSide;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getGravity() {
        return gravity;
    }

    public void setGravity(int gravity) {
        this.gravity = gravity;
    }

    public String getReverseLanguages() {
        return reverseLanguages;
    }

    public void setReverseLanguages(String reverseLanguages) {
        this.reverseLanguages = reverseLanguages;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public void setReverse(boolean reverse) {
        isReverse = reverse;
    }
}
