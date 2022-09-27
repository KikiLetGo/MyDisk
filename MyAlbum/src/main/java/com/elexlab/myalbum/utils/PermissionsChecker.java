package com.elexlab.myalbum.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

/**
 * Created by BruceYoung on 1/29/17.
 */
public class PermissionsChecker {


    // 判断权限集合
    public static boolean lacksPermissions(Context context,String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(context,permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    public static boolean lacksPermission(Context context,String permission) {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
