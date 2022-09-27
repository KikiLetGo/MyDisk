package com.elexlab.mydisk.manager;

import com.alibaba.fastjson.JSONArray;
import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.HeroLib;
import com.elexlab.mydisk.utils.CommonUtil;
import com.elexlab.mydisk.utils.HttpUtils;

import java.util.List;

public class PhoneManager {
    private static PhoneManager instance = new PhoneManager();
    public static PhoneManager getInstance(){
        return instance;
    }
    private String device = CommonUtil.getDeviceId(HeroLib.getInstance().appContext);
    public void listPhones(final DataSourceCallback<List<String>> dataSourceCallback){
        HttpUtils.GET(Constants.LIST_PHONES, new HttpUtils.HttpRequestListener() {
            @Override
            public void onResponse(String s) {
                List<String> phones = JSONArray.parseArray(s,String.class);
                if(dataSourceCallback != null){
                    dataSourceCallback.onSuccess(phones);
                }
            }

            @Override
            public void onErrorResponse(String msg) {
                if(dataSourceCallback != null){
                    dataSourceCallback.onFailure(msg,0);
                }
            }
        });
    }

    public boolean isCurrentDevice(String device){
        return CommonUtil.getDeviceId(HeroLib.getInstance().appContext).equals(device);
    }

    public boolean isCurrentDevice(){
        return CommonUtil.getDeviceId(HeroLib.getInstance().appContext).equals(device);
    }

    public void setDevice(String currentDevice) {
        this.device = currentDevice;
    }

    public String getDevice() {
        return device;
    }

    public void set2CurrentDevice(String currentDevice) {
        this.device = currentDevice;
    }
}
