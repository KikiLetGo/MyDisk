package com.elexlab.mydisk.manager;

import com.elexlab.mydisk.datasource.CloudCostDataSource;
import com.elexlab.mydisk.datasource.DataCondition;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.pojo.CloudCost;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CostManager {
    private static final String TAG = CostManager.class.getName();
    private static CostManager instance = new CostManager();
    public static CostManager getInstance(){
        return instance;
    }

    public void getCurrentMonthCost(DataSourceCallback<CloudCost> dataSourceCallback){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM");
        Date date = new Date(System.currentTimeMillis());
        final String ym = formatter.format(date);

        DataCondition dataCondition = new DataCondition();
        dataCondition.addParam("id",ym);

        CloudCostDataSource cloudCostDataSource = new CloudCostDataSource();
        cloudCostDataSource.getData(new DataSourceCallback<CloudCost>() {
            @Override
            public void onSuccess(CloudCost cloudCost, String... extraParams) {
                if(cloudCost==null){
                    CloudCost c = new CloudCost();
                    c.setId(ym);
                    cloudCostDataSource.addData(c,dataSourceCallback);
                }else{
                    if(dataSourceCallback != null){
                        dataSourceCallback.onSuccess(cloudCost,extraParams);
                    }
                }

            }

            @Override
            public void onFailure(String errMsg, int code) {
                if(dataSourceCallback != null){
                    dataSourceCallback.onFailure(errMsg,code);
                }
            }
        }, dataCondition, CloudCost.class);
    }

    public void updateConst(CloudCost cloudCost,DataSourceCallback<CloudCost>  dataSourceCallback){
        new CloudCostDataSource().updateData(cloudCost,dataSourceCallback);
    }

    public void onRequestAdd(int requestTime){
        getCurrentMonthCost(new DataSourceCallback<CloudCost>() {
            @Override
            public void onSuccess(CloudCost cloudCost, String... extraParams) {
                cloudCost.setRequestCount(cloudCost.getRequestCount()+requestTime);
                new CloudCostDataSource().updateData(cloudCost,null);
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        });
    }

    public void onVolumeAdd(long volume){
        getCurrentMonthCost(new DataSourceCallback<CloudCost>() {
            @Override
            public void onSuccess(CloudCost cloudCost, String... extraParams) {
                cloudCost.setVolume(cloudCost.getVolume()+volume);
                new CloudCostDataSource().updateData(cloudCost,null);
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        });
    }
}

