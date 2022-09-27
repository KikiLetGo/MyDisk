package com.elexlab.mydisk.cloud;

import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.pojo.FileInfo;

import java.io.File;

public class CloudUtils {

    public static FileInfo parseFile(String objectKey){
        //remove DeviceCode first
        objectKey = objectKey.replaceFirst(".*//","/");

        String[] parts = objectKey.split("/");
        String fileName = parts[parts.length-1];
        String dir = objectKey.replace("/"+fileName,"");
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(fileName);
        fileInfo.setDir(dir);
        return fileInfo;
    }

    public static String fileInfo2Key(FileInfo fileInfo){
        String key = MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode() + File.separator+ fileInfo.getDir()+ File.separator+fileInfo.getName();

        return key;
    }
}
