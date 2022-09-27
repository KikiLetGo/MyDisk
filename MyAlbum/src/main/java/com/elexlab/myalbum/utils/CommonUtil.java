package com.elexlab.myalbum.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 1/2/17.
 */
public class CommonUtil {
    public static float parseToFixLengthFloat(float target,int length){
        int workerValue = (int) Math.pow(10,length);
        float  result   =  (float)(Math.round(target*workerValue))/workerValue;
        return result;
    }

    public static void playVideo(Context context,String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String type = "video/*";
        Uri uri = Uri.parse(path);
        intent.setDataAndType(uri, type);
        context.startActivity(intent);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
                if(pn.equals(packageName)){
                    return true;
                }
            }
        }
        return false;
    }

    public static String getPackageName(Context context) {
        try {
            String pkName = context.getPackageName();

            return pkName;
        } catch (Exception e) {
        }
        return null;
    }
}
