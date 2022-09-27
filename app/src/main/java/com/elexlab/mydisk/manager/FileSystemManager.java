package com.elexlab.mydisk.manager;

import android.content.Context;
import android.os.Environment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.HWCloudDataSource;
import com.elexlab.mydisk.datasource.HeroLib;
import com.elexlab.mydisk.model.FileDir;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.utils.CommonUtil;
import com.elexlab.mydisk.utils.EasyLog;
import com.elexlab.mydisk.utils.HttpUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class FileSystemManager {
    private static final String TAG = FileSystemManager.class.getName();
    private static FileSystemManager instance = new FileSystemManager();
    public static FileSystemManager getInstance(){
        return instance;
    }

    public interface FileActionListener{
        void onCompletion(FileInfo fileInfo,String msg);
        void onError(FileInfo fileInfo,String msg);
    }

    public void checkAndCreateMirrorDisk(Context context){
        String deviceId = CommonUtil.getDeviceId(context);
        EasyLog.d(TAG,"deviceId:"+deviceId);
        HttpUtils.GET(Constants.HOST + "/checkMirrorDisk?device="+deviceId, new HttpUtils.HttpRequestListener() {
            @Override
            public void onResponse(String s) {
                EasyLog.d(TAG,"is MirrorDisk created:"+s);
                if(!Boolean.valueOf(s)){
                    createMirrorDisk(null);
                }

            }

            @Override
            public void onErrorResponse(String msg) {

            }
        });
    }
    public void isMirrorCreated(Context context,final DataSourceCallback<Boolean> callback){
        String deviceId = CommonUtil.getDeviceId(context);

        HttpUtils.GET(Constants.HOST + "/checkMirrorDisk?device="+deviceId, new HttpUtils.HttpRequestListener() {
            @Override
            public void onResponse(String s) {
                EasyLog.d(TAG,"is MirrorDisk created:"+s);
                callback.onSuccess(Boolean.valueOf(s));


            }

            @Override
            public void onErrorResponse(String msg) {

            }
        });

    }


    public void uploadFiles(List<FileInfo> localFileInfos,final ProgressListener<FileInfo> progressListener){
        final Iterator<FileInfo> fileInfoIterator = localFileInfos.iterator();
        final int allCount = localFileInfos.size();
        if(fileInfoIterator.hasNext()){
            FileInfo fileInfo = fileInfoIterator.next();

            FileSystemManager.getInstance().uploadFile(fileInfo, new FileSystemManager.FileActionListener() {
                private int nowCount = 0;
                @Override
                public void onCompletion(FileInfo uploadedFileInfo,String msg) {
                    nowCount = nowCount+1;
                    float progress = ((float)nowCount)/((float)allCount);
                    if(progressListener != null){
                        progressListener.onProgress(progress,uploadedFileInfo);
                    }
                    if(fileInfoIterator.hasNext()){
                        FileInfo fileInfo = fileInfoIterator.next();
                        FileSystemManager.getInstance().uploadFile(fileInfo,this);
                    }else{
                        if(progressListener != null){
                            progressListener.onComplete();
                        }
                    }
                }

                @Override
                public void onError(FileInfo fileInfo,String msg) {
                    if(progressListener != null){
                        progressListener.onError(0,msg);
                    }
                }
            });

        }else{
            if(progressListener != null){
                progressListener.onComplete();
            }
        }

    }
    public void uploadFile(final FileInfo fileInfo,final FileActionListener listener){
       new HWCloudDataSource().addData(fileInfo, new DataSourceCallback<FileInfo>() {
           @Override
           public void onSuccess(FileInfo fileInfo, String... extraParams) {

               if(listener!= null){
                   listener.onCompletion(fileInfo,"success");
               }

           }

           @Override
           public void onFailure(String errMsg, int code) {
               if(listener!= null){
                   listener.onError(fileInfo,errMsg);
               }

           }
       });
    }

    public void createMirrorDisk(final DataSourceCallback dataSourceCallback){

        final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        final FileDir rootDir = new FileDir();
        Executors.newCachedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                deepSearchFiles(path,rootDir);
                String allFileJson = JSON.toJSONString(rootDir, SerializerFeature.DisableCircularReferenceDetect);
                long endTime = System.currentTimeMillis();
                EasyLog.d(TAG,"costTime:"+(endTime-startTime));
                EasyLog.d(TAG,"allFileJson:"+allFileJson);
                try {
                    FileWriter fileWriter = new FileWriter(new File(path+"/fileStructs.json"));
                    fileWriter.write(allFileJson);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Map<String,String> form = new HashMap<>();
                form.put("fileDir",allFileJson);
                form.put("externalStorageDirectory",path);
                form.put("deviceId",CommonUtil.getDeviceId(HeroLib.getInstance().appContext));
                HttpUtils.POST(Constants.HOST + "/createMirrorDisk", form, new HttpUtils.HttpRequestListener() {
                    @Override
                    public void onResponse(String s) {
                        dataSourceCallback.onSuccess(s);
                    }

                    @Override
                    public void onErrorResponse(String msg) {
                        dataSourceCallback.onFailure(msg,0);

                    }
                });


            }
        });



    }
    private void deepSearchFiles(String path,FileDir fileDir){
        List<String> documents = new ArrayList<>();
        List<FileDir> fileDirs = new ArrayList<>();
        File dir = new File(path);
        for(File file:dir.listFiles()){
            if(file.getName().startsWith(".")){
                continue;
            }
            if(file.isDirectory()){
                FileDir sonFileDir = new FileDir();
                sonFileDir.setName(file.getName());
                deepSearchFiles(file.getAbsolutePath(),sonFileDir);
                fileDirs.add(sonFileDir);
            }else{
                documents.add(file.getName());
            }
        }
        fileDir.setDocuments(documents);
        fileDir.setDirs(fileDirs);
    }




}
