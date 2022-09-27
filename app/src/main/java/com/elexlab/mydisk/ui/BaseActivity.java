package com.elexlab.mydisk.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.elexlab.myalbum.utils.PermissionsChecker;
import com.elexlab.mydisk.R;


/**
 * Created by BruceYoung on 1/8/17.
 */
public class BaseActivity extends FragmentActivity {
    public void back(View view){
        finish();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    public interface RequestPermissionsCallback{
        void onPermissionGranted();
        void onPermissionDefined();
    }
    private RequestPermissionsCallback requestPermissionsCallback = null;

    public void requestPermission(RequestPermissionsCallback requestPermissionsCallback,String permission,boolean force){

        boolean permissionResult = checkPermission(permission,force);
        if(permissionResult){
            if(requestPermissionsCallback != null){
                requestPermissionsCallback.onPermissionGranted();
            }
        }else{
            this.requestPermissionsCallback = requestPermissionsCallback;
        }
    }


    private static final int REQUEST_CODE = 0;
    public boolean checkPermission(String permission,boolean force){
        if(PermissionsChecker.lacksPermission(this, permission)){
            PermissionsActivity.startActivityForResult(this,force,REQUEST_CODE,permission);
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 拒绝时, 关闭页面, 缺少主要权限, 无法运行
        if (requestCode == REQUEST_CODE) {
            if(resultCode == PermissionsActivity.PERMISSIONS_DENIED){
                if(requestPermissionsCallback != null){
                    requestPermissionsCallback.onPermissionDefined();
                }
                boolean force = data.getBooleanExtra("force",false);
                if(force){
                    finish();
                }
            }else if(resultCode == PermissionsActivity.PERMISSIONS_GRANTED){
                if(requestPermissionsCallback != null){
                    requestPermissionsCallback.onPermissionGranted();
                }
            }
        }
    }

    private Dialog progressDialog = null;
    public void showProgress(boolean cancelable){
        clearProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(cancelable);
        progressDialog.setContentView(R.layout.view_progress_dialog);
        //progressDialog.setTitle(R.string.loading);

    }
    public void showProgress(int resId,boolean cancelable){
        String title = getResources().getString(resId);
        showProgress(title,cancelable);
    }
    public void showProgress(String title,boolean cancelable){
        clearProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;

        progressDialog.setCancelable(cancelable);
        View contentView = LayoutInflater.from(this).inflate(R.layout.view_progress_dialog,null);
        progressDialog.setContentView(contentView,params);
        TextView tvTitle = (TextView) progressDialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }
    private void clearProgress(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public void resetProgress(String progress){
        if(progressDialog == null || !progressDialog.isShowing()){
            return;
        }
        TextView tvProgress = (TextView) progressDialog.findViewById(R.id.tvProgress);
        tvProgress.setVisibility(View.VISIBLE);
        tvProgress.setText(progress);
    }

    public void hiddenProgress(){
        TextView tvProgress = (TextView) progressDialog.findViewById(R.id.tvProgress);
        tvProgress.setVisibility(View.GONE);
    }

    public void resetProgressTitle(String title){
        if(progressDialog == null || !progressDialog.isShowing()){
            return;
        }
        TextView tvTitle = (TextView) progressDialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }
    public void stopProgress(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }

    public void showKeyboard(final View view){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
                view.requestFocus();
            }
        }, 100);
    }

    public void hiddenKeyboard(final View view){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }, 100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //MobclickAgent.onPause(this);
    }
}
