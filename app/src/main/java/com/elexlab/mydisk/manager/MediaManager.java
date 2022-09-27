package com.elexlab.mydisk.manager;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.utils.DeviceUtils;
import com.elexlab.mydisk.MyDiskApplication;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.pojo.Contact;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.wiget.PercentProgressDialog;
import com.elexlab.mydisk.utils.CommonUtil;
import com.elexlab.mydisk.utils.EasyLog;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MediaManager {
    private static final String TAG = MediaManager.class.getName();
    private static MediaManager instance = new MediaManager();
    public static MediaManager getInstance(){
        return instance;
    }
    public void backupImages(final Context context){
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,null, null, null,null);
        List<FileInfo> fileInfos = new ArrayList<>();
        while (cursor.moveToNext()) {

            String img = cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            EasyLog.d("MediaManager",img);
            FileInfo fileInfo = new FileInfo(img);
            fileInfos.add(fileInfo);
        }
        backUpFiles(context,fileInfos);


    }

    public void backupVideos(Context context){
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,null, null, null,null);
        List<FileInfo> fileInfos = new ArrayList<>();

        while (cursor.moveToNext()) {


            String img = cursor.getString(
                    cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            EasyLog.d("MediaManager",img);
            FileInfo fileInfo = new FileInfo(img);
            fileInfos.add(fileInfo);
        }
        backUpFiles(context,fileInfos);

    }

    public void backupContact(final Context context){
        Cursor cursor = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY);
        Map<String,Contact> contactsMap = new HashMap<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String phoneNumber = cursor.getString(1);
                EasyLog.d("MediaManager",name);
                EasyLog.d("MediaManager",phoneNumber);
                if(!contactsMap.containsKey(name)){
                    contactsMap.put(name,new Contact(name));
                }
                contactsMap.get(name).getPhoneNumbers().add(phoneNumber);
            }
            cursor.close();
        }
        String contactsJson = JSONArray.toJSONString(contactsMap.values());

        byte[] data = contactsJson.getBytes();
        String deviceCode = MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode();
        String key = deviceCode+ "/db/contacts.json";

        View view = LayoutInflater.from(context).inflate(R.layout.view_loading,null);
        final AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        dialog.show();

        Client.getInstance().uploadData(data, key, new DataSourceCallback() {
            @Override
            public void onSuccess(Object object, String... extraParams) {
                ThreadManager.getInstance().getMainHandler().post(()->{
                    dialog.dismiss();
                    Toast.makeText(context,"上传成功～",Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onFailure(String errMsg, int code) {
                ThreadManager.getInstance().getMainHandler().post(()->{
                    dialog.dismiss();
                    Toast.makeText(context,"上传发生错误:"+errMsg,Toast.LENGTH_LONG).show();
                });
            }
        });


    }

    private void backUpFiles(final Context context,List<FileInfo> fileInfos){
        final PercentProgressDialog percentProgressDialog = new PercentProgressDialog(context);
        percentProgressDialog.showProgress("同步中",false);


        FileSystemManager.getInstance().uploadFiles(fileInfos, new ProgressListener<FileInfo>() {
            @Override
            public void onProgress(final float progress, FileInfo fileInfo) {
                MyDiskApplication.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        String pstr = new DecimalFormat( "0.00" ).format(progress*100);
                        percentProgressDialog.resetProgress(String.valueOf(pstr+"%"));
                    }
                });

            }

            @Override
            public void onComplete() {
                MyDiskApplication.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        percentProgressDialog.stopProgress();
                        Toast.makeText(context,"文件夹内文件同步成功～",Toast.LENGTH_LONG).show();
                    }
                });


            }

            @Override
            public void onError(int code, final String message) {
                MyDiskApplication.getMainHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        percentProgressDialog.stopProgress();
                        Toast.makeText(context,"同步发生错误:"+message,Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
    }
}
