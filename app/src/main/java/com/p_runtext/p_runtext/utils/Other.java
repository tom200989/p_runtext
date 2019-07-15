package com.p_runtext.p_runtext.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

import java.util.List;

/*
 * Created by qianli.ma on 2019/7/11 0011.
 */
public class Other {

    /**
     * 获取顶层ac全名
     *
     * @param context 域
     * @return ac全名
     */
    public static String getTopActivityName(Context context) {
        ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Activity.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTasks = am.getRunningTasks(5);
        return runningTasks.get(0).topActivity.getClassName();
    }

    /**
     * 判断APP是否在前台
     *
     * @param context 域
     * @return T:在前台
     */
    public static boolean isAppForground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    //在前台
                    return true;
                } else if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND //
                                   || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE // 
                                   || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE //
                                   || appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_TOP_SLEEPING) {// 锁屏的时候
                    //在后台
                    return false;
                }
            }
        }
        return false;
    }
}
