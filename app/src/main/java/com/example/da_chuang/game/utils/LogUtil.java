package com.example.da_chuang.game.utils;

import android.util.Log;

/**
 * @author Seulf
 * @version 1.0 2021/3/3
 */
public class LogUtil {
    private static LogUtil log;

    private LogUtil() {
    }

    public static void i(Object str) {
        printLog(Log.INFO, str);
    }

    public static void d(Object str) {
        printLog(Log.DEBUG, str);
    }

    public static void v(Object str) {
        printLog(Log.VERBOSE, str);
    }

    public static void w(Object str) {
        printLog(Log.WARN, str);
    }

    public static void e(Object str) {
        printLog(Log.ERROR, str);
    }

    /**
     * 调用系统的打印
     *
     * @param index
     * @param str
     */
    private static void printLog(int index, Object str) {

        if (log == null) {
            log = new LogUtil();
        }
        String name = log.getFunctionName();
        if (name != null) {
            str = name + " - " + str;
        }
        String tag = "AppName";
        switch (index) {
            case Log.VERBOSE:
                Log.v(tag, str.toString());
                break;
            case Log.DEBUG:
                Log.d(tag, str.toString());
                break;
            case Log.INFO:
                Log.i(tag, str.toString());
                break;
            case Log.WARN:
                Log.w(tag, str.toString());
                break;
            case Log.ERROR:
                Log.e(tag, str.toString());
                break;
            default:
                break;
        }
    }

    /**
     * 获取调用log方法的名字
     *
     * @return Name
     */
    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();
        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }
            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }
            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }
            return "[ " + Thread.currentThread().getName() + ":( "
                    + st.getFileName() + ":" + st.getLineNumber() + ") "
                    + st.getMethodName() + " ]";
        }
        return null;
    }
}

