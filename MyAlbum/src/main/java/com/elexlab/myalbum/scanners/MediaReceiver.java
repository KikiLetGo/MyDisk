package com.elexlab.myalbum.scanners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;


import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.MediaUtils;

import java.io.File;

/**
 * Created by BruceYoung on 11/12/17.
 */
public class MediaReceiver extends BroadcastReceiver{
    private final static String TAG = MediaReceiver.class.getSimpleName();
    public interface MediaChangedListener {
        void onMediaAdded(Media media);
        void onMediaDeleted(Media media);
    }
    private static MediaChangedListener mediaChangedListener;

    public static void setMediaChangedListener(MediaChangedListener listener) {
        mediaChangedListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Cursor cursor = context.getContentResolver().query(intent.getData(),
                null, null, null, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex("_data"));
        EasyLog.d(TAG,"path:"+path);
        Media media = new Media();
        media.setFile(new File(path));
        media.setLastModify(media.getFile().lastModified());
        media.setTitle(media.getFile().getName());
        media.setSize((int) media.getFile().length());
        int[] widthAndHeight = MediaUtils.getImageWidthHeight(media.getFile().getAbsolutePath());
        media.setWidth(widthAndHeight[0]);
        media.setHeight(widthAndHeight[1]);


        if(mediaChangedListener != null){
            mediaChangedListener.onMediaAdded(media);
        }
    }


}
