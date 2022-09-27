package com.elexlab.mydisk.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BruceYoung on 1/2/17.
 */
public class CommonUtil {
    public static float parseToFixLengthFloat(float target,int length){
        int workerValue = (int) Math.pow(10,length);
        float  result   =  (float)(Math.round(target*workerValue))/workerValue;
        return result;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    public static String getUrlParam(String url,String patmKey){
        if(TextUtils.isEmpty(url)||TextUtils.isEmpty(patmKey)){
            EasyLog.e("getUrlParam","url or patmKey is null");
            return null;
        }
        String strUrlParam = truncateUrlPage(url);
        if(TextUtils.isEmpty(strUrlParam)){
            EasyLog.e("getUrlParam","no param in url");
            return null;
        }
        String[] arrSplit=strUrlParam.split("[&]");
        for(String strSplit:arrSplit) {
            String[] arrSplitEqual=null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if(arrSplitEqual.length < 1) {
                continue;
            }
            String key = arrSplitEqual[0];
            String value = arrSplitEqual[1];
            if(TextUtils.isEmpty(key)){
                continue;
            }
            if(key.equals(patmKey)){
                return value;
            }

        }
        return null;
    }

    public static Map<String,String> getUrlParam(String url){

        String strUrlParam = truncateUrlPage(url);
        if(TextUtils.isEmpty(strUrlParam)){
            EasyLog.e("getUrlParam","no param in url");
            return null;
        }
        Map<String,String> map = new HashMap<>();
        String[] arrSplit=strUrlParam.split("[&]");
        for(String strSplit:arrSplit) {
            String[] arrSplitEqual=null;
            arrSplitEqual = strSplit.split("[=]");

            //解析出键值
            if(arrSplitEqual.length <= 1) {
                continue;
            }
            String key = arrSplitEqual[0];
            String value = arrSplitEqual[1];
            if(TextUtils.isEmpty(key)){
                continue;
            }
            map.put(key,value);

        }
        return map;
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

    private static String truncateUrlPage(String strURL)
    {
        String strAllParam=null;
        String[] arrSplit=null;

        arrSplit=strURL.split("[?]");
        if(strURL.length()>1)
        {
            if(arrSplit.length>1)
            {
                if(arrSplit[1]!=null)
                {
                    strAllParam=arrSplit[1];
                }
            }
        }

        return strAllParam;
    }
}
