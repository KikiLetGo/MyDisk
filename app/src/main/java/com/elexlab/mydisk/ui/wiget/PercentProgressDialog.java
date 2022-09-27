package com.elexlab.mydisk.ui.wiget;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.elexlab.mydisk.R;

public class PercentProgressDialog {
    private Context context;
    private ProgressDialog progressDialog;

    public PercentProgressDialog(Context context) {
        this.context = context;
    }

    public void showProgress(String title, boolean cancelable){
        clearProgress();
        progressDialog = new ProgressDialog(context);
        progressDialog.show();

        WindowManager.LayoutParams params = progressDialog.getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;

        progressDialog.setCancelable(cancelable);
        View contentView = LayoutInflater.from(context).inflate(R.layout.view_progress_dialog,null);
        progressDialog.setContentView(contentView,params);
        TextView tvTitle = (TextView) progressDialog.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
    }
    public void clearProgress(){
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
    public void stopProgress(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
}
