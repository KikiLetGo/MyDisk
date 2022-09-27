package com.elexlab.mydisk.utils;

import android.util.Log;

/**
 * This Log Util is used for debug log
 * when you set outputPriority>FUNCALL
 * int release apk,it won't print any log
 *
 * Created by BruceYoung on 15/11/12.
 */
public class EasyLog {

    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARNING = 4;
    public static final int ERROR = 5;
    public static final int FUNCALL = 6;

    public static int outputPriority = VERBOSE;

//    static {
//        if(!BuildConfig.DEBUG){
//            outputPriority = FUNCALL;
//        }
//    }


    public static void v(String TAG, String info){
        if (outputPriority >= VERBOSE )
            return;
        Log.v(TAG, getFileLineMethod() + " " + info);
    }

    public static void d(String TAG, String info){
        if (outputPriority >= DEBUG )
            return;

        Log.d(TAG, getFileLineMethod()+ " " + info);
    }

    public static void i(String TAG, String info){
        if (outputPriority >= INFO )
            return;
        Log.i(TAG, getFileLineMethod()+ " " + info);
    }

    public static void w(String TAG, String info){
        if (outputPriority >= WARNING)
            return;

        Log.w(TAG, getFileLineMethod()+ " " + info);
    }

    public static void e(String TAG, String info){
        if (outputPriority >= ERROR)
            return;
        Log.e(TAG, getFileLineMethod()+ " " + info);
    }

    public static boolean can()
    {

        return outputPriority==0;
    }

    public static String getFileLineMethod()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        StringBuffer toStringBuffer = new StringBuffer("[").append(
                traceElement.getFileName()).append(" | ").append(
                traceElement.getLineNumber()).append(" | ").append(
                traceElement.getMethodName()).append("]");
        return toStringBuffer.toString();

    }

    public static String _FILE_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getFileName();

    }

    public static String _CLASS_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getClassName();
    }


    public static String _FUNC_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getMethodName();
    }


    public static int _LINE_()
    {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[2];
        return traceElement.getLineNumber();
    }
}
