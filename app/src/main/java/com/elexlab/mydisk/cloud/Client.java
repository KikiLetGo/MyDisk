package com.elexlab.mydisk.cloud;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.utils.DeviceUtils;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.HeroLib;
import com.elexlab.mydisk.datasource.SettingDataSource;
import com.elexlab.mydisk.manager.CostManager;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.pojo.Device;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.pojo.Setting;
import com.elexlab.mydisk.ui.settings.SettingActivity;
import com.elexlab.mydisk.utils.EasyLog;
import com.obs.services.ObsClient;
import com.obs.services.exception.ObsException;
import com.obs.services.model.BucketStorageInfo;
import com.obs.services.model.HttpMethodEnum;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;
import com.obs.services.model.ObjectMetadata;
import com.obs.services.model.ObsObject;
import com.obs.services.model.ProgressListener;
import com.obs.services.model.ProgressStatus;
import com.obs.services.model.PutObjectRequest;
import com.obs.services.model.ServerAlgorithm;
import com.obs.services.model.ServerEncryption;
import com.obs.services.model.SseCHeader;
import com.obs.services.model.SseKmsHeader;
import com.obs.services.model.TemporarySignatureRequest;
import com.obs.services.model.TemporarySignatureResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Client {
    private final static String TAG = Client.class.getSimpleName();

    public static String BUCKET_NAME = "my-icloud";

    private static Client client = new Client();

    public static Client getInstance(){
        return client;
    }

    private ObsClient obsClient;

    private Client() {
        Setting setting = new SettingDataSource().querySettingSync();
        String endPoint = setting.getEndpoint();
        String ak = setting.getAk();
        String sk = setting.getSk();
        BUCKET_NAME = setting.getBucketName();
        obsClient = new ObsClient(ak, sk, endPoint);

    }



    public ObjectListing listObjects(ListObjectsRequest request){
        CostManager.getInstance().onRequestAdd(1);
        return obsClient.listObjects(request);

    }

    public ObjectListing listObjects(String prefix){
        ListObjectsRequest request = new ListObjectsRequest(BUCKET_NAME);
        request.setPrefix(prefix);
        ObjectListing result = obsClient.listObjects(request);
        CostManager.getInstance().onRequestAdd(1);
        return result;

    }

    public void uploadData(byte[] data, String key,  DataSourceCallback dataSourceCallback){

        if(!MultiDeviceManager.getInstance().isNativeDevice()){
            ThreadManager.getInstance().getMainHandler().post(()->{
                Toast.makeText(MyAlbum.getContext(),"当前镜像选择非本机，无法同步到云端",Toast.LENGTH_LONG).show();
            });
            return;
        }
        ThreadManager.getInstance().getHttpHandler().post(()->{
            PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, key);
            InputStream is = new ByteArrayInputStream(data);
            request.setInput(is);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength((long) data.length);//1MB
            request.setMetadata(metadata);
            request.setProgressListener(new ProgressListener() {
                @Override
                public void progressChanged(ProgressStatus status) {
                    // 获取上传平均速率
                    EasyLog.i("PutObject", "AverageSpeed:" + status.getAverageSpeed());
                    // 获取上传进度百分比
                    EasyLog.i("PutObject", "TransferPercentage:" + status.getTransferPercentage());
                    if(status.getTransferPercentage()==100){
                        if(dataSourceCallback !=null ){
                            dataSourceCallback.onSuccess(null);
                        }
                    }
                }
            });
            // 每上传1MB数据反馈上传进度
            request.setProgressInterval(1024 * 1024L);
            request.setSseKmsHeader(getSseKmsHeader());
            obsClient.putObject(request);
        });

        CostManager.getInstance().onRequestAdd(1);
    }



    public void uploadFile(FileInfo fileInfo, DataSourceCallback<FileInfo> dataSourceCallback){
        if(!MultiDeviceManager.getInstance().isNativeDevice()){
            ThreadManager.getInstance().getMainHandler().post(()->{
                Toast.makeText(MyAlbum.getContext(),"当前镜像选择非本机，无法同步到云端",Toast.LENGTH_LONG).show();
            });
            return;
        }
        String deviceCode = MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode();

        // localfile为待上传的本地文件路径，需要指定到具体的文件名
        String objectName = deviceCode+File.separator+fileInfo.getDir()+File.separator+fileInfo.getName();
        EasyLog.i(TAG, "uploadFile:"+objectName);
        PutObjectRequest request = new PutObjectRequest(BUCKET_NAME, objectName);
        request.setFile(new File(fileInfo.getPath())); // localfile为上传的本地文件路径，需要指定到具体的文件名
        request.setProgressListener(new ProgressListener() {

            @Override
            public void progressChanged(ProgressStatus status) {
                // 获取上传平均速率
                EasyLog.i("PutObject", "AverageSpeed:" + status.getAverageSpeed());
                // 获取上传进度百分比
                EasyLog.i("PutObject", "TransferPercentage:" + status.getTransferPercentage());
                if(status.getTransferPercentage()==100){
                    CloudFileManager.getInstance().getCloudFileMap().put(objectName,fileInfo);
                    if(dataSourceCallback !=null ){
                        dataSourceCallback.onSuccess(fileInfo);
                    }
                }
            }
        });
        // 每上传1MB数据反馈上传进度
        request.setProgressInterval(1024 * 1024L);

        //request.setSseCHeader(getSseCHeader());
        request.setSseKmsHeader(getSseKmsHeader());
        obsClient.putObject(request);

        CostManager.getInstance().onRequestAdd(1);


    }


    public String tempUrl(String objectKey,long expireSeconds){

        TemporarySignatureRequest request = new TemporarySignatureRequest(HttpMethodEnum.GET, expireSeconds);
        request.setBucketName(BUCKET_NAME);
        request.setObjectKey(objectKey);
        TemporarySignatureResponse response = obsClient.createTemporarySignature(request);
        String url = response.getSignedUrl();
        EasyLog.i("CreateTemporarySignature", "Creating bucket using temporary signature url:");
        EasyLog.i("CreateTemporarySignature", "\t" + url);
        return url;
    }

    public void downloadFile(String objectKey,DataSourceCallback<byte[]> dataSourceCallback){
        ThreadManager.getInstance().getOperationHandler().post(()->{

        // 创建ObsClient实例

            ObsObject obsObject = obsClient.getObject(BUCKET_NAME, objectKey);

            // 读取对象内容
            Log.i("GetObject", "Object content:");
            InputStream input = obsObject.getObjectContent();
            byte[] b = new byte[1024];
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try {
                int len;
                while ((len=input.read(b)) != -1){
                    bos.write(b, 0, len);
                }

                Log.i("GetObject", new String(bos.toByteArray()));
                bos.close();
                input.close();
                CostManager.getInstance().onVolumeAdd(bos.size());

                if(dataSourceCallback != null){
                    dataSourceCallback.onSuccess(bos.toByteArray());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        CostManager.getInstance().onRequestAdd(1);
    }





    public void getBucketStorageInfo(DataSourceCallback<BucketStorageInfo> dataSourceCallback){
        ThreadManager.getInstance().getHttpHandler().post(()->{
            try {
                BucketStorageInfo bucketStorageInfo = obsClient.getBucketStorageInfo(BUCKET_NAME);
                long objCounts = bucketStorageInfo.getObjectNumber();
                long size = bucketStorageInfo.getSize();
                EasyLog.d(TAG,"bucket:"+BUCKET_NAME+" size:"+size+" objectCounts:"+objCounts);
                if(dataSourceCallback != null){
                    dataSourceCallback.onSuccess(bucketStorageInfo);
                }
            }catch (ObsException exception){
                exception.printStackTrace();
                if(dataSourceCallback != null){
                    dataSourceCallback.onFailure(exception.getErrorMessage(),exception.getResponseCode());
                }
            }catch (IllegalArgumentException exception){
                exception.printStackTrace();
                if(dataSourceCallback != null){
                    dataSourceCallback.onFailure(exception.getMessage(),0);
                }
            }

        });


        CostManager.getInstance().onRequestAdd(1);
    }

    private SseCHeader getSseCHeader(){
        // 设置SSE-C算法加密对象
        SseCHeader sseCHeader = new SseCHeader();
        sseCHeader.setAlgorithm(ServerAlgorithm.AES256);
        // 设置SSE-C方式下使用的密钥，用于加解密对象，该值是密钥进行base64encode后的值
        sseCHeader.setSseCKeyBase64("bmswTGQxY3RhWnM0ejRaV09tN2pVTFFWVHErYmpMb3g=");
        return sseCHeader;
    }

    private SseKmsHeader getSseKmsHeader(){
        SseKmsHeader sseKmsHeader = new SseKmsHeader();
        sseKmsHeader.setEncryption(ServerEncryption.OBS_KMS);
        return sseKmsHeader;
    }


}
