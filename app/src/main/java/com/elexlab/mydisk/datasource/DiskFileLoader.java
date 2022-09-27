package com.elexlab.mydisk.datasource;

import android.util.ArraySet;

import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.manager.FileSystemManager;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.manager.PhoneManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.misc.ProgressListener;
import com.elexlab.mydisk.utils.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DiskFileLoader {
    private List<FileInfo> localCaches;
    private List<FileInfo> mirrorCaches;
    private List<FileInfo> mergedCaches;
    private String dir;


    public interface Callback{
        void onLoaded(List<FileInfo> merged,List<FileInfo> locals,List<FileInfo> mirrors);
    }
    public DiskFileLoader(String dir) {
        this.dir = dir;
    }

    public DiskFileLoader(String dir,String device) {
        this.dir = dir;
    }

    public void loadFiles(final String dir, final Callback callback){
        if(mergedCaches != null && localCaches != null && mirrorCaches != null){
            if(callback != null){
                callback.onLoaded(mergedCaches,localCaches,mirrorCaches);
            }
            return;
        }
        if(mirrorCaches != null && localCaches != null){
            mergedCaches = mergeLocalAndMirrorFile(localCaches,mirrorCaches);
            if(callback != null){
                callback.onLoaded(mergedCaches,localCaches,mirrorCaches);
            }
            return;
        }
        if(!MultiDeviceManager.getInstance().isNativeDevice()){
            loadMirrorFiles(new DataSourceCallback<List<FileInfo>>() {
                @Override
                public void onSuccess(List<FileInfo> fileInfos, String... extraParams) {
                    mergedCaches = fileInfos;
                    mirrorCaches = fileInfos;
                    if(callback != null){
                        callback.onLoaded(mergedCaches,localCaches,mirrorCaches);
                    }
                }

                @Override
                public void onFailure(String errMsg, int code) {

                }
            });
            return;
        }else {
            loadLocalFiles(new DataSourceCallback<List<FileInfo>>() {
                @Override
                public void onSuccess(final List<FileInfo> localFileInfos, String... extraParams) {
                    loadMirrorFiles(new DataSourceCallback<List<FileInfo>>() {
                        @Override
                        public void onSuccess(List<FileInfo> fileInfos, String... extraParams) {
                            mergeLocalAndMirrorFile(localFileInfos, fileInfos);
                            if (callback != null) {
                                callback.onLoaded(mergedCaches, localCaches, mirrorCaches);
                            }
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

    }
    private List<FileInfo> mergeLocalAndMirrorFile(List<FileInfo> localFileInfos,List<FileInfo> mirrorFileInfos){

        List<FileInfo> mergedList = new ArrayList<>();

        for(FileInfo localFileInfo:localFileInfos){
            FileInfo result = null;
            for(FileInfo mirrorFileInfo:mirrorFileInfos){
                if(localFileInfo.equals(mirrorFileInfo)){
                    localFileInfo.setStoreLocation(FileInfo.StoreLocation.LOCAL_MIRROR);
                    mirrorFileInfo.setStoreLocation(FileInfo.StoreLocation.LOCAL_MIRROR);
                    result = localFileInfo;
                    break;
                }
            }
            if(result != null){
                mergedList.add(result);
                mirrorFileInfos.remove(result);
            }else{
                mergedList.add(localFileInfo);
            }

        }
        //now mirrorFileInfo only contains fileInfo that not exist in local
        for(FileInfo mirrorFileInfo:mirrorFileInfos){
            mergedList.add(mirrorFileInfo);
        }
        sortFileInfos(mergedList);
        mergedCaches = mergedList;
        return mergedList;
    }
    public void syncDir2Mirror(final ProgressListener<FileInfo> progressListener){
        loadFiles(dir,new DiskFileLoader.Callback(){

            @Override
            public void onLoaded(final List<FileInfo> merged, final List<FileInfo> locals, final List<FileInfo> mirrors) {
                List<FileInfo> localFileInfos = new ArrayList<>();
                for(FileInfo fileInfo:merged){
                    if(fileInfo.getStoreLocation() == FileInfo.StoreLocation.LOCAL){
                        localFileInfos.add(fileInfo);
                    }
                }
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
        });
    }
    private void loadLocalFiles(final DataSourceCallback<List<FileInfo>> callback){
        if(localCaches != null){
            if(callback != null){
                callback.onSuccess(localCaches);
            }
            return;
        }
        DataCondition dataCondition = new DataCondition();
        dataCondition.getParamMap().put("dir",dir);
        new LocalFileDataSource().getDatas(new DataSourceCallback<List<FileInfo>>() {
            @Override
            public void onSuccess(List<FileInfo> localFileInfos, String... extraParams) {
                sortFileInfos(localFileInfos);
                localCaches =localFileInfos;
                if(callback != null){
                    callback.onSuccess(localCaches);
                }
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        }, dataCondition,FileInfo.class);
    }

    public void loadMirrorFiles(final DataSourceCallback<List<FileInfo>> callback){

        if(mirrorCaches != null){
            if(callback != null){
                callback.onSuccess(mirrorCaches);
            }
            return;
        }


        DataCondition dataCondition = new DataCondition();
        dataCondition.addParam("dir",dir);
        new HWCloudDataSource().getDatas(new DataSourceCallback<List<FileInfo>>() {
            @Override
            public void onSuccess(List<FileInfo> fileInfos, String... extraParams) {
                sortFileInfos(fileInfos);
                mirrorCaches = fileInfos;
                if(callback != null){
                    ThreadManager.getInstance().getMainHandler().post(()->{callback.onSuccess(fileInfos);});

                }
            }

            @Override
            public void onFailure(String errMsg, int code) {

            }
        }, dataCondition, null);

        return;
    }

    private void sortFileInfos(List<FileInfo> fileInfos){
        Collections.sort(fileInfos, new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                if (o1.isDir() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDir())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
    }
}
