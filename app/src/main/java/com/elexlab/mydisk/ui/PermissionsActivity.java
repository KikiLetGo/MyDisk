package com.elexlab.mydisk.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.elexlab.myalbum.utils.PermissionsChecker;
import com.elexlab.mydisk.R;


/**
 * Created by BruceYoung on 1/29/17.
 */
public class PermissionsActivity extends Activity{
    public static void startActivityForResult(Activity activity,boolean force, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra("permissions", permissions);
        intent.putExtra("force", force);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }

    private boolean isRequireCheck;
    private boolean force;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null || !getIntent().hasExtra("permissions")) {
            throw new RuntimeException("PermissionsActivity需要使用静态startActivityForResult方法启动!");
        }
        force = getIntent().getBooleanExtra("force",false);

        setContentView(R.layout.activity_permissions);

        isRequireCheck = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isRequireCheck) {
            String[] permissions = getPermissions();
            if (PermissionsChecker.lacksPermissions(this,permissions)) {
                if(Build.VERSION.SDK_INT >= 23){
                    requestPermissions(permissions); // 请求权限

                }
            } else {
                allPermissionsGranted(); // 全部权限都已获取
            }
        } else {
            isRequireCheck = true;
        }
    }
    private static final int PERMISSION_REQUEST_CODE = 0;

    // 请求权限兼容低版本
    private void requestPermissions(String... permissions) {
        ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
    }

    // 返回传递的权限参数
    private String[] getPermissions() {
        return getIntent().getStringArrayExtra("permissions");
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)) {
            isRequireCheck = true;
            allPermissionsGranted();
        } else {
            isRequireCheck = false;
            //showMissingPermissionDialog();
            Intent intent = new Intent();
            intent.putExtra("force",force);
            setResult(PERMISSIONS_DENIED,intent);
            finish();
            if(force){//kill app
                Toast.makeText(PermissionsActivity.this,getResources().getString(R.string.app_can_not_work_without_the_permission),Toast.LENGTH_LONG).show();
            }
        }
    }


    private static final String PACKAGE_URL_SCHEME = "package:"; // 方案
    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    // 含有全部的权限
    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }

    public static final int PERMISSIONS_GRANTED = 0; // 权限授权
    public static final int PERMISSIONS_DENIED = 1; // 权限拒绝
    // 全部权限均已获取
    private void allPermissionsGranted() {
        setResult(PERMISSIONS_GRANTED);
        finish();
    }
}
