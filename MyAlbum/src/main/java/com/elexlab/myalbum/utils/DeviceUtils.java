package com.elexlab.myalbum.utils;

import android.app.Service;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import java.util.Locale;

/**
 * Created by BruceYoung on 12/31/16.
 */
public class DeviceUtils {
    /**
     * dip to pix
     *
     * @param context
     * @param dpValue
     * @return pix
     */
    public static int dipToPx(Context context, float dpValue) {
        final float d = 0.5f;
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + d);
    }

    public static String getCountryCode(){
       String country = Locale.getDefault().getISO3Country();
        return country;
    }

    public static int getScreenWidth(Context context){
        WindowManager wm = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);

        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        return width;
    }

    /**
     * 获取手机屏幕密度
     */
    private static float density = 0;
    public static final float getDensity(Context context) {
        if(density!=0){
            return density;
        }
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        density = dm.density;
        return density;
    }

    public static String getUniqueId(Context context){
        try {
            String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            String id = androidID + Build.SERIAL;
            return id;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void vibrate(Context context, long milliseconds) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    /**
     * 获取网络连接状态
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
    /**
     * dp 2 px
     */
    public static int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px 2 dp
     */
    public static int px2dp(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static String getDeviceId(Context context){
        String imei = "";
        final int targetSdkVersion = context.getApplicationInfo().targetSdkVersion;
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
            imei = Settings.System.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }else{
            TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            imei = TelephonyMgr.getDeviceId();
        }
        return imei;
    }

    public static String getDeviceName(Context context){
        String deviceName = Settings.Global.getString(context.getContentResolver(), Settings.Global.DEVICE_NAME);
        return deviceName;
    }
}
