package com.elexlab.mydisk.datasource;

import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.pojo.FileInfo;

import java.io.File;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LocalFileDataSource extends AsyncDataSource<FileInfo> {
    @Override
    public void addData(FileInfo fileInfo, DataSourceCallback<FileInfo> dataSourceCallback) {

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
        File file = new File(dir);
        List<FileInfo> fileInfos = new ArrayList<>();
        if(!file.exists()){
            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(fileInfos);
            }
            return;
        }
        if(file.listFiles() != null){
            for(File f:file.listFiles()){
                if(f.getName() != null && f.getName().startsWith(".")){
                    continue;
                }
                FileInfo fileInfo = new FileInfo();

                fileInfo.setName(f.getName());
                fileInfo.setDir(dir);
                fileInfo.setStoreLocation(FileInfo.StoreLocation.LOCAL);
                if(f.isDirectory()){
                    fileInfo.setFileType(FileInfo.FileType.DIR);
                }else{
                    fileInfo.setFileType(FileInfo.FileType.DOCUMENT);

                }
                fileInfos.add(fileInfo);
            }
        }

        if(dataSourceCallback != null){
            dataSourceCallback.onSuccess(fileInfos);
        }
    }

    @Override
    public void cancelRequest(String tag) {

    }

    @Override
    public void cancelAllRequest() {

    }
}
