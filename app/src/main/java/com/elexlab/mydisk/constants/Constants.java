package com.elexlab.mydisk.constants;

import android.os.Environment;

import com.elexlab.mydisk.datasource.HeroLib;
import com.elexlab.mydisk.manager.PhoneManager;
import com.elexlab.mydisk.utils.CommonUtil;

public class Constants {
    public static String HOST = "http://192.168.3.22:8888";
    public static String LIST_DIR_BASE = HOST+"/listDir?device=%1$s&dir=%2$s";
   // public static String DOWNLOAD_FILE = HOST+"/download?device="+ PhoneManager.getInstance().getDevice()+"&dir=";
    public static String UPLOAD_FILE = HOST+"/upload?device="+CommonUtil.getDeviceId(HeroLib.getInstance().appContext);

    public static String LIST_PHONES = HOST+"/listPhones?device="+CommonUtil.getDeviceId(HeroLib.getInstance().appContext);


    public interface Path{
        String LOCAL_DISK_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public static String getDownloadFileUrl(){
        return HOST+"/download?device="+ PhoneManager.getInstance().getDevice()+"&dir=";
    }

    public interface DataUnit{
        long B = 1;
        long KB = 1024*B;
        long MB = 1024*KB;
        long GB = 1024*MB;
        long TB = 1024*GB;

    }



}
