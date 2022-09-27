package com.elexlab.myalbum.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.elexlab.myalbum.pojos.Media;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by BruceYoung on 10/17/17.
 */
public class MediaUtils {
    private final static String TAG = MediaUtils.class.getSimpleName();
    public static void notifyMediaDelete(Context context,File file){
        String params[] = new String[] { file.getPath() };
        context.getContentResolver().delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + " LIKE ?", params);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+file.getAbsolutePath())));

    }

    public static void notifyMediasDelete(Context context,List<File> files){
        String params[] = new String[files.size()];
        for (int i=0;i<files.size();i++){
            params[i] = files.get(i).getPath();
        }
        context.getContentResolver().delete(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.DATA + " LIKE ?", params);

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE));
    }


    public static void notifyMediaInsert(Context context,File file){
        String params[] = new String[] { file.getPath() };
        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    file.getAbsolutePath(), file.getName(), null);
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.Images.Media.DATA,file.getPath());

            context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues);

        } catch (Exception e) {
            e.printStackTrace();
        }

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+file.getAbsolutePath())));

    }

    public static int[] getImageWidthHeight(InputStream inputStream){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap  = BitmapFactory.decodeStream(inputStream,null,options);
        return new int[]{options.outWidth,options.outHeight};
    }
    public static int[] getImageWidthHeight(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();

        /**
         * 最关键在此，把options.inJustDecodeBounds = true;
         * 这里再decodeFile()，返回的bitmap为空，但此时调用options.outHeight时，已经包含了图片的高了
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options); // 此时返回的bitmap为null
        /**
         *options.outHeight为原始图片的高
         */
        return new int[]{options.outWidth,options.outHeight};
    }

    public static int getMediaDuration(File file){
        if (file.length() == 0) return -1;

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(file.getAbsolutePath());


        String durationStr = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if(TextUtils.isEmpty(durationStr)){
            return 0;
        }
        int duration = Integer.parseInt(durationStr);
        return duration;
    }

    public static void loadMediaInfo(Media media){
        ExifInterface exifInterface = null;
        try {
            if(media.getFile() == null || !media.getFile().exists()){
                return;
            }
            exifInterface = new ExifInterface(media.getFile().getAbsolutePath());
            String datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);// 拍摄时间
            String deviceName = exifInterface.getAttribute(ExifInterface.TAG_MAKE);// 设备品牌
            String deviceModel = exifInterface.getAttribute(ExifInterface.TAG_MODEL); // 设备型号
            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
            String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
            if(!TextUtils.isEmpty(datetime)){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                try {
                    long time = simpleDateFormat.parse(datetime).getTime();
                    if(time > 0){
                        media.setLastModify(time);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
