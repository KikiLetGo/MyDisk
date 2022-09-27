package com.elexlab.mydisk.ui.misc;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.widget.Toast;

import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.BaseActivity;
import com.elexlab.mydisk.utils.EasyLog;

import java.util.ArrayList;

public class ShareReceiveActivity extends BaseActivity {
    private final static String TAG = ShareReceiveActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action)&&type!=null){
            dealPicStream(intent);
        }else if (Intent.ACTION_SEND_MULTIPLE.equals(action)&&type!=null){
            dealMultiplePicStream(intent);
        }
    }


    void dealPicStream(Intent intent){
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        String filePath = getImageAbsolutePath(this,uri);
        EasyLog.d(TAG,filePath);
        FileInfo fileInfo = new FileInfo(filePath);
        showProgress("上传中",false);
        hiddenProgress();
        ThreadManager.getInstance().getHttpHandler().post(()->{
            Client.getInstance().uploadFile(fileInfo, new DataSourceCallback<FileInfo>() {
                @Override
                public void onSuccess(FileInfo fileInfo, String... extraParams) {
                    ThreadManager.getInstance().getMainHandler().post(()->{
                        stopProgress();
                        Toast.makeText(ShareReceiveActivity.this,"文件已同步到云端~",Toast.LENGTH_LONG).show();
                        finish();
                    });
                }

                @Override
                public void onFailure(String errMsg, int code) {

                }
            });
        });

    }
    private int now = 0;
    void dealMultiplePicStream(Intent intent){
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(intent.EXTRA_STREAM);
        showProgress("上传中",false);
        int all = uris.size();
        now = 0;
        for(Uri uri:uris){
            String filePath = getImageAbsolutePath(this,uri);
            EasyLog.d(TAG,filePath);
            FileInfo fileInfo = new FileInfo(filePath);
            ThreadManager.getInstance().getHttpHandler().post(()->{
                Client.getInstance().uploadFile(fileInfo, new DataSourceCallback<FileInfo>() {
                    @Override
                    public void onSuccess(FileInfo fileInfo, String... extraParams) {
                        now++;
                            ThreadManager.getInstance().getMainHandler().post(()->{
                                resetProgress(now+"/"+all);
                                if(now>=all) {
                                    stopProgress();
                                    Toast.makeText(ShareReceiveActivity.this,"文件已同步到云端~",Toast.LENGTH_LONG).show();
                                    finish();
                                }
                            });
                    }
                    @Override
                    public void onFailure(String errMsg, int code) {

                    }
                });
            });
        }
    }

    public static String getImageAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null)
            return null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.parseLong(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri))
                return imageUri.getLastPathSegment();
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};  //告诉Provider要返回的内容（列Column）
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
