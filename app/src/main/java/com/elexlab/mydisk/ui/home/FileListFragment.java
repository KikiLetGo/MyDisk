package com.elexlab.mydisk.ui.home;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elexlab.mydisk.R;
import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.DiskFileLoader;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.manager.PhoneManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.files.FilesBrowserAdapter;
import com.elexlab.mydisk.ui.misc.ProgressListener;
import com.elexlab.mydisk.utils.CommonUtil;

import java.text.DecimalFormat;
import java.util.List;

public class FileListFragment  extends Fragment {
    private interface ShowMode{
        int NO_MIRROR = 0;
        int WITH_MIRROR = 1;
    }
    public FileListFragment(){

    }

    private int viewMode = ShowMode.WITH_MIRROR;
    private HomeViewModel homeViewModel;
    private String dir = Constants.Path.LOCAL_DISK_ROOT;

    private DiskFileLoader diskFileLoader;
    private FilesBrowserAdapter filesBrowserAdapter;
    private ImageView ivMode;
    private Handler handler = new Handler();
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_filelist, container, false);

        View llShowMirror = root.findViewById(R.id.llShowMirror);
        ivMode = root.findViewById(R.id.ivMode);
        llShowMirror.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchModel();
            }
        });

        final RecyclerView rcvFiles = root.findViewById(R.id.rcvFiles);
        rcvFiles.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getArguments() != null) {
            String currentDir = getArguments().getString("dir");
            if(currentDir != null){
                dir = currentDir;
            }
        }
        diskFileLoader = new DiskFileLoader(dir);
        filesBrowserAdapter = new FilesBrowserAdapter(this);
        rcvFiles.setAdapter(filesBrowserAdapter);
        loadFiles();
        final View llSyncALl = root.findViewById(R.id.llSyncALl);
        final ImageView ivSyncAll = root.findViewById(R.id.ivSyncAll);
        llSyncALl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgress("同步中",false);
                diskFileLoader.syncDir2Mirror(new ProgressListener<FileInfo>() {
                    @Override
                    public void onProgress(final float progress, FileInfo fileInfo) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                String pstr = new DecimalFormat( "0.00" ).format(progress*100);
                                resetProgress(String.valueOf(pstr+"%"));

                            }
                        });
                    }

                    @Override
                    public void onComplete() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                stopProgress();
                                Toast.makeText(getContext(),"文件夹内文件同步成功～",Toast.LENGTH_LONG).show();
                                filesBrowserAdapter.notifyDataSetChanged();
                                llSyncALl.setOnClickListener(null);
                                ivSyncAll.setImageResource(R.drawable.ic_done);
                            }
                        });


                    }

                    @Override
                    public void onError(int code, final String message) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                stopProgress();
                                Toast.makeText(getContext(),"同步发生错误:"+message,Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });
            }
        });
        if(!MultiDeviceManager.getInstance().isNativeDevice()){
            ivMode.setVisibility(View.GONE);
            llSyncALl.setVisibility(View.GONE);
            viewMode = ShowMode.WITH_MIRROR;
        }
        return root;
    }
    public void switchModel(){
        if(viewMode == ShowMode.WITH_MIRROR){
            viewMode = ShowMode.NO_MIRROR;
            ivMode.setImageResource(R.mipmap.ic_eye_close);

        }else if(viewMode == ShowMode.NO_MIRROR){
            viewMode = ShowMode.WITH_MIRROR;
            ivMode.setImageResource(R.drawable.ic_eye);
        }

        loadFiles();
    }

    private void loadFiles(){
        diskFileLoader.loadFiles(dir, new DiskFileLoader.Callback() {
            @Override
            public void onLoaded(List<FileInfo> merged, List<FileInfo> locals, List<FileInfo> mirrors) {

                switch (viewMode){
                    case ShowMode.WITH_MIRROR:{
                        filesBrowserAdapter.resetFileList(merged);
                        break;
                    }
                    case ShowMode.NO_MIRROR:{
                        filesBrowserAdapter.resetFileList(locals);
                        break;
                    }
                }
            }
        });
    }

    private ProgressDialog progressDialog;
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
        tvTitle.setText(title);
    }
    private void clearProgress(){
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }
    }
    private void resetProgress(String progress){
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