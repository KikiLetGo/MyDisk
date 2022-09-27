package com.elexlab.mydisk.cloud;


import android.util.Log;

import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.manager.CostManager;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObsObject;

import java.util.HashMap;
import java.util.Map;

public class CloudFileManager {
    private final static String TAG = CloudFileManager.class.getSimpleName();
    private static CloudFileManager instance = new CloudFileManager();
    private Map<String, FileInfo> cloudFileMap = new HashMap<>();
    public static CloudFileManager getInstance() {
        return instance;
    }

    private CloudFileManager(){
        onDeviceChanged(null);
    }
    private void preloadAllCloudFile(){
        cloudFileMap.clear();
        ObjectListing result = Client.getInstance().listObjects(MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode());
        for(ObsObject obsObject : result.getObjects()){
            String objectKey = obsObject.getObjectKey();
            Log.i("ListObjects","\t" + objectKey);
            Log.i("ListObjects","\t" + obsObject.getOwner());
            FileInfo fileInfo = CloudUtils.parseFile(objectKey);
            cloudFileMap.put(objectKey,fileInfo);
        }
    }

    public boolean fileInCloud(FileInfo fileInfo){
        String targetKey = CloudUtils.fileInfo2Key(fileInfo);
        if(cloudFileMap.containsKey(targetKey)){
            return true;
        }
        return false;
    }

    public Map<String,FileInfo> getCloudFileMap(){
        return cloudFileMap;
    }

    public void onDeviceChanged(Runnable completeCallback){
        ThreadManager.getInstance().getHttpHandler().post(()->{
            preloadAllCloudFile();
            if(completeCallback != null){
                completeCallback.run();
            }
        });
    }
}
