package com.elexlab.mydisk.ui.gallery;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverInterf;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.pojo.Device;

import java.util.List;


public class PhoneGalleryDialog extends Dialog implements ObserverInterf {
    private Context context;
    private String strShop;


    public PhoneGalleryDialog(@NonNull Context context) {
        super(context);
        this.context=context;
    }
    public PhoneGalleryDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context=context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置布局
        setContentView(R.layout.phone_gallery_dialog);

        MultiDeviceManager.getInstance().listDevices(new DataSourceCallback<List<Device>>() {
            @Override
            public void onSuccess(List<Device> devices, String... extraParams) {
                ThreadManager.getInstance().getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        showDevices(devices);
                    }
                });
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        });



    }

    private void showDevices(List<Device> devices){
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        LinearLayout llPhones = findViewById(R.id.llPhones);
        for(Device device:devices){
            View view = mInflater.inflate(R.layout.item_phone,
                    llPhones, false);
            TextView tvName = view.findViewById(R.id.tvName);
            tvName.setText(device.getName());
            llPhones.addView(view);
            view.setOnClickListener(v -> {

                ObserverManager.getInstance().regist(this);
                dismiss();
                showProgress(getContext().getString(R.string.device_switching),false);

                MultiDeviceManager.getInstance().switchDevice(device);

            });
        }


    }

    ProgressDialog progressDialog;
    private void clearProgress(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
    private void showProgress(String title,boolean cancelable){
        clearProgress();
        progressDialog = new ProgressDialog(getContext());
        progressDialog.show();

        WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;

        progressDialog.setCancelable(cancelable);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.view_progress_dialog,null);
        progressDialog.setContentView(contentView,params);
        TextView tvTitle = (TextView) progressDialog.findViewById(R.id.tvTitle);
        progressDialog.findViewById(R.id.tvProgress).setVisibility(View.GONE);
        tvTitle.setText(title);

        progressDialog.setOnDismissListener(dialog -> {
            ObserverManager.getInstance().unregist(this);
        });
    }



    @Override
    public boolean onDataChange(int type, long param1, long param2, Object obj) {
        if(type == EventType.DEVICE_CHANGED){
            clearProgress();
            Toast.makeText(getContext(),
                    getContext().getText(R.string.device_changed)+":"
                            + MultiDeviceManager.getInstance().getCurrentDevice().getName(),
                    Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}


