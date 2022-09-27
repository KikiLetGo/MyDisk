package com.elexlab.mydisk.ui.home;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.elexlab.mydisk.R;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.SettingDataSource;
import com.elexlab.mydisk.manager.CostManager;
import com.elexlab.mydisk.manager.FileSystemManager;
import com.elexlab.mydisk.manager.MediaManager;
import com.elexlab.mydisk.pojo.CloudCost;
import com.elexlab.mydisk.pojo.Setting;
import com.elexlab.mydisk.ui.files.FileBrowserActivity;
import com.elexlab.mydisk.ui.settings.SettingActivity;
import com.elexlab.mydisk.ui.wiget.CostDashBoard;
import com.obs.services.model.BucketStorageInfo;

import java.text.DecimalFormat;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    private TextView tvAllCost;
    private TextView tvStorageSize;
    private TextView tvStorageCost;
    private TextView tvVolume;
    private TextView tvVolumeCost;
    private TextView tvRequestCount;
    private TextView tvRequestCost;
    private CostDashBoard costDashBoard;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        root.findViewById(R.id.llFile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FileBrowserActivity.startActivity(getContext());
            }
        });
        root.findViewById(R.id.llImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaManager.getInstance().backupImages(getContext());
            }
        });
        root.findViewById(R.id.llVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaManager.getInstance().backupVideos(getContext());
            }
        });
        root.findViewById(R.id.llContact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaManager.getInstance().backupContact(getContext());
            }
        });

        if(!checkSetting()){

            return root;
        }



        FileSystemManager.getInstance().isMirrorCreated(getContext(), new DataSourceCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean, String... extraParams) {
                if(!aBoolean){
                    AlertDialog.Builder builder =  new AlertDialog.Builder(getContext());
                    builder.setTitle("未能检测到远程镜像，是否创建？");
                    final Dialog dialog = builder.setNegativeButton("不是现在", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                            .setPositiveButton("麻溜创建", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                   createMirrorDisk();
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();


                }
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        });
        //FileSystemManager.getInstance().checkAndCreateMirrorDisk(getContext());
        calculateCost(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkSetting();
    }

    private boolean checkSetting(){
        Setting setting = new SettingDataSource().querySettingSync();
        if(setting == null || ! setting.valid()){
            Intent intent = new Intent(getActivity(),SettingActivity.class);
            getActivity().startActivity(intent);
            return false;
        }
        return true;
    }

    private void calculateCost(View root){
        costDashBoard = root.findViewById(R.id.costDashBoard);
        tvStorageSize = root.findViewById(R.id.tvStorageSize);
        tvStorageCost = root.findViewById(R.id.tvStorageCost);
        tvVolume = root.findViewById(R.id.tvVolume);
        tvVolumeCost = root.findViewById(R.id.tvVolumeCost);
        tvRequestCount = root.findViewById(R.id.tvRequestCount);
        tvRequestCost = root.findViewById(R.id.tvRequestCost);
        tvAllCost = root.findViewById(R.id.tvAllCost);



        CostManager.getInstance().getCurrentMonthCost(new DataSourceCallback<CloudCost>() {
            @Override
            public void onSuccess(CloudCost cloudCost, String... extraParams) {


                Client.getInstance().getBucketStorageInfo(new DataSourceCallback<BucketStorageInfo>() {
                    @Override
                    public void onSuccess(BucketStorageInfo bucketStorageInfo, String... extraParams) {
                        ThreadManager.getInstance().getMainHandler().post(()->{
                            long storageSize = bucketStorageInfo.getSize();

                            if(storageSize < Constants.DataUnit.GB){//less than 1G
                                tvStorageSize.setText(storageSize / (Constants.DataUnit.MB) + "MB");
                            }else{
                                tvStorageSize.setText(storageSize / (Constants.DataUnit.GB) + "GB");
                            }
                            float storageCost = ((float) storageSize/(Constants.DataUnit.GB))*0.08f;//0.08￥ per GB
                            tvStorageCost.setText(new DecimalFormat("##0.000").format(storageCost)+"￥");


                            if(cloudCost.getVolume() < Constants.DataUnit.GB){//less than 1G
                                tvVolume.setText(cloudCost.getVolume() / (Constants.DataUnit.MB) + "MB");
                            }else{
                                tvVolume.setText(cloudCost.getVolume() / (Constants.DataUnit.GB) + "GB");

                            }
                            float volumeCost = ((float)cloudCost.getVolume()/(Constants.DataUnit.GB))*0.5f;//0.5￥ per GB
                            tvVolumeCost.setText(new DecimalFormat("##0.000").format(volumeCost)+"￥");

                            float requestCost = (cloudCost.getRequestCount()*0.1f) / 10000f;//0.1￥ per 10k requests
                            tvRequestCount.setText(String.valueOf(cloudCost.getRequestCount()));
                            tvRequestCost.setText(new DecimalFormat("##0.000").format(requestCost)+"￥");

                            costDashBoard.setCost(storageCost,volumeCost,requestCost);
                            String allCost = new DecimalFormat("##0.000").format(storageCost + volumeCost + requestCost)+"￥";
                            tvAllCost.setText(allCost);


                        });
                    }
                    @Override
                    public void onFailure(String errMsg, int code) {

                    }
                });

            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        });


    }

    private void createMirrorDisk(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_loading,null);

        final AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(view)
                .create();
        dialog.show();
        FileSystemManager.getInstance().createMirrorDisk(new DataSourceCallback() {
            @Override
            public void onSuccess(Object o, String... extraParams) {
                dialog.dismiss();
                Toast.makeText(getContext(),"创建成功",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String errMsg, int code) {
                dialog.dismiss();
                Toast.makeText(getContext(),"创建失败～",Toast.LENGTH_LONG).show();


            }
        });
    }

}