package com.elexlab.myalbum.scanners;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.elexlab.myalbum.utils.EasyLog;

/**
 * Created by BruceYoung on 11/12/17.
 */
public class MediaContentObserver extends ContentObserver{
    private static final String TAG = MediaContentObserver.class.getSimpleName();
    private Context mContext;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public MediaContentObserver(Context context,Handler handler) {
        super(handler);
        this.mContext = context;
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);


    }
}
