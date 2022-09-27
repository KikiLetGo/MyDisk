package com.elexlab.myalbum;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.telephony.mbms.FileInfo;

import com.elexlab.myalbum.scanners.MediaContentObserver;
import com.elexlab.myalbum.scanners.MyFileObserver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MyAlbum {
    private static MyAlbum instance;
    private static Context context;
    public static void init(Context ctx){
        context = ctx;
        instance = new MyAlbum();

    }
    public static Context getContext(){
        return context;
    }

    public MyAlbum() {
    }


}
