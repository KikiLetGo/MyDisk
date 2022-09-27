package com.elexlab.myalbum.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by BruceYoung on 11/30/16.
 */
public class PathUtils {
    public static final String package_name = "";


    public static String getVideoPath(Context context) {
        String dirPath = context.getExternalFilesDir("video").getPath() ;
        checkPath(dirPath);
        return dirPath;
    }

    public static String getAudioPath(Context context) {
        String dirPath = context.getExternalFilesDir("audio").getPath() ;
        checkPath(dirPath);
        return dirPath;
    }

    public static String getImagePath(Context context) {
        String dirPath = context.getExternalFilesDir("image").getPath() ;
        checkPath(dirPath);
        return dirPath;
    }

    public static String getCachePath(Context context) {
        String dirPath = context.getExternalFilesDir("cache").getPath() ;
        return dirPath;
    }

    public static String getExternalCachePath(Context context) {
        String dirPath = context.getExternalCacheDir().getAbsolutePath();
        return dirPath;
    }

    public static String getPublicPhotoSavePath(Context context){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/photos";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getPublicBasePath(){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath();
        return dirPath;
    }

    public static String getPublicAlbumsSavePath(){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/albums";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getPublicEncryptionPhotoSavePath(Context context){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/encryption/photos";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getPublicEncryptionAlbumsSavePath(){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/encryption/albums";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getPublicDecryptionPhotoSavePath(Context context){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/decryption/photos";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getPublicTempPath(){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/temp";
        checkPath(dirPath);
        return dirPath;
    }


    //media processor transaction
    public static String getRedoLogPath(Context context){
        String dirPath = context.getExternalFilesDir("transaction").getPath()+"/redo";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getUndoLogPath(Context context){
        String dirPath = context.getExternalFilesDir("transaction").getPath()+"/undo";
        checkPath(dirPath);
        return dirPath;
    }

    public static String getReEncryptionTempPath(){
        String dirPath = Environment.getExternalStoragePublicDirectory("mindin_album").getPath()+"/re_encryption_temp";
        checkPath(dirPath);
        return dirPath;
    }

    /**
     * 创建目录
     *
     * @param path
     */
    public static void checkPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
    }
}
