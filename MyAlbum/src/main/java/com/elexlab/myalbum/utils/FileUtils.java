package com.elexlab.myalbum.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;


import com.elexlab.myalbum.MyAlbum;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 11/30/16.
 */
public class FileUtils {
    private static final String TAG = FileUtils.class.getSimpleName();
    public static List<String> getFilePathsUnderPath(String path){
        if(path == null){
           EasyLog.e(TAG,"path is null");
            return null;
        }
        File dir = new File(path);
        if(!(dir.exists())){
            EasyLog.e(TAG,"path is not exist!path:"+path);
            return null;
        }
        List<String> filePaths = new ArrayList<String>();
        String[] names = dir.list();
        for(int i=0;i<names.length;i++){
            filePaths.add(path+"/"+names[i]);
        }
        return filePaths;
    }

    public static boolean cpFile(String oldPath,String newPath){


        InputStream inStream = null;
        FileOutputStream fs = null;
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件不存在时
                inStream = new FileInputStream(oldPath); //读入原文件
                fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
            }
            return true;
        } catch (Exception e) {
            EasyLog.d(TAG,"mvFile fail!");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(inStream != null){
                    inStream.close();
                }
                if(fs != null){
                    fs.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static void cpFileWithMetaKeep(String oldPath,String newPath){
        File oldFile = new File(oldPath);
        long lastModified = oldFile.lastModified();

        String tempPath = PathUtils.getExternalCachePath(MyAlbum.getContext()) + File.separator + oldFile.getName();
        cpFile(oldPath,tempPath);
        File tempFile = new File(tempPath);

        boolean success = tempFile.setLastModified(lastModified);

        //mv to newPath
        StringBuilder mvCmdline = new StringBuilder("mv ");
        mvCmdline.append(tempPath);
        mvCmdline.append(" ");
        mvCmdline.append(newPath);
        Process mvProcess = null;
        try {
            mvProcess = Runtime.getRuntime().exec(mvCmdline.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static void mvFile(String oldPath,String newPath){
//        File oldFile = new File(oldPath);
//        //mv to newPath
//        StringBuilder mvCmdline = new StringBuilder("mv ");
//        mvCmdline.append(oldFile);
//        mvCmdline.append(" ");
//        mvCmdline.append(newPath);
//        Process mvProcess = null;
//        try {
//            mvProcess = Runtime.getRuntime().exec(mvCmdline.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public static boolean mvFile(String oldPath,String newPath){
        if(!cpFile(oldPath,newPath)){
            return false;
        }
        return new File(oldPath).delete();
    }


    public static boolean deleteFileWithMedia(File file, Context context){
        if (file.exists()) {

            if (!file.delete()) {
                return false;
            }

            String params[] = new String[] { file.getPath() };
            context.getContentResolver().delete(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.Media.DATA + " LIKE ?", params);

            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+file.getAbsolutePath())));

            return true;
        } else {
            return false;
        }
    }

    public static boolean addFileWithMedia(File file, Context context){
        if (file.exists()) {
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+file.getAbsolutePath())));

            return true;
        } else {
            return false;
        }
    }
    public static String saveBitmap(String filePath, Bitmap bitmap){

        FileOutputStream fileOutputStream = null;
        File f = new File(filePath);
        if(f.exists()){
            f.delete();
        }
        try{
            f.createNewFile();
            fileOutputStream = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            EasyLog.d(TAG,"maybe save bitmap success");
            return filePath;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(fileOutputStream != null){
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return null;
    }

    public static void clearFileUnderDir(String dirPath){
        File dir = new File(dirPath);
        if(!dir.exists()){
            EasyLog.e(TAG,"dirPath not exist!");
            return;
        }
        if(!dir.isDirectory()){
            EasyLog.e(TAG,"dirPath not a dir!");
            return;
        }
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(File file:dir.listFiles()){
            if (file == null || !file.exists()) {
                continue;
            }
            if(file.isDirectory()){
                clearFileUnderDir(file.getAbsolutePath());
            }
            file.delete();
        }
    }

    public static void deleteAllFileAndDir(String dirPath){
        clearFileUnderDir(dirPath);
        new File(dirPath).delete();
    }


    public static long getTotalSizeOfFilesInDir(String path) {
        return getTotalSizeOfFilesInDir(new File(path));
    }
        // 递归方式 计算文件的大小
    public static long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }

}
