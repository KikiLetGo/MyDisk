package com.elexlab.mydisk.datasource;

import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudUtils;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.utils.EasyLog;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class HWCloudDataSource extends AsyncDataSource<FileInfo>{
    private final String TAG = HWCloudDataSource.class.getSimpleName();

    @Override
    public void addData(FileInfo fileInfo, final DataSourceCallback<FileInfo> dataSourceCallback) {
        ThreadManager.getInstance().getHttpHandler().post(()->{

            Client.getInstance().uploadFile(fileInfo,dataSourceCallback);
        });

    }

    @Override
    public void addData(DataCondition dataCondition, DataSourceCallback<FileInfo> dataSourceCallback) {

    }

    @Override
    public void deleteData(FileInfo fileInfo, DataSourceCallback<FileInfo> dataSourceCallback) {

    }

    @Override
    public void deleteData(DataCondition dataCondition, DataSourceCallback<FileInfo> dataSourceCallback) {

    }

    @Override
    public void updateData(FileInfo fileInfo, DataSourceCallback<FileInfo> dataSourceCallback) {

    }

    @Override
    public void updateData(DataCondition dataCondition, DataSourceCallback<FileInfo> dataSourceCallback) {

    }

    @Override
    public void getData(DataSourceCallback<FileInfo> dataSourceCallback, DataCondition dataCondition, Class<FileInfo> clazz) {

    }

    @Override
    public void getDatas(DataSourceCallback<List<FileInfo>> dataSourceCallback, DataCondition dataCondition, Class<FileInfo> clazz) {

        String dir = (String) dataCondition.getParamMap().get("dir");
        if(dir == null){
            EasyLog.e(TAG,"dir not exist!");
            return;
        }
        EasyLog.d(TAG,"dir:"+dir);
        String prefix = MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode()+ File.separator+dir+ File.separator;
        EasyLog.d(TAG,"prefix:"+prefix);

        ListObjectsRequest request = new ListObjectsRequest(Client.BUCKET_NAME);
        request.setPrefix(prefix);
        request.setDelimiter("/");
        request.setMaxKeys(1000);
        ThreadManager.getInstance().getHttpHandler().post(()->{
            ObjectListing result = Client.getInstance().listObjects(request);
            List<FileInfo> fileInfos = new ArrayList<>();
            for(String p : result.getCommonPrefixes()){
                EasyLog.i(TAG, "Objects in folder [" + p + "]:");
                FileInfo fileInfo = CloudUtils.parseFile(p);
                fileInfo.setFileType(FileInfo.FileType.DIR);
                fileInfo.setStoreLocation(FileInfo.StoreLocation.MIRROR);
                fileInfos.add(fileInfo);
            }

            for(ObsObject obsObject : result.getObjects()){
                EasyLog.i(TAG, obsObject.getObjectKey() + "  " +  "(size = " + obsObject.getMetadata().getContentLength() + ")");
                if(prefix.equals(obsObject.getObjectKey())){
                    continue;
                }
                FileInfo fileInfo = CloudUtils.parseFile(obsObject.getObjectKey());
                fileInfo.setFileType(FileInfo.FileType.DOCUMENT);
                fileInfo.setStoreLocation(FileInfo.StoreLocation.MIRROR);
                fileInfos.add(fileInfo);

            }


            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(fileInfos);
            }

        });



    }





    @Override
    public void cancelRequest(String tag) {

    }

    @Override
    public void cancelAllRequest() {

    }
}
