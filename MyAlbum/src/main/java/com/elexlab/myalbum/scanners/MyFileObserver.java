package com.elexlab.myalbum.scanners;

import android.os.Bundle;
import android.os.FileObserver;

import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.utils.EasyLog;

import java.io.File;

public class MyFileObserver extends FileObserver {
    private static final String TAG  = MyFileObserver.class.getSimpleName();
    private String dirPath;
    public MyFileObserver(File file) {
        super(file.getAbsolutePath(), FileObserver.ALL_EVENTS);
        dirPath = file.getAbsolutePath();
        EasyLog.d(TAG, "MyFileObserver: "+dirPath+",,"+FileObserver.ALL_EVENTS);
    }

    @Override
    public void onEvent(int event,  String path) {
       //EasyLog.v(TAG, "onEvent:"+ path+",event="+event);
        if(path == null){
            return;
        }
        if(path.endsWith(".tmp")){
            EasyLog.i(TAG,"tmp file,ignore it");
            return;
        }
        switch (event){
            case FileObserver.CREATE:{
                EasyLog.d(TAG, "file create path:"+ path+",event="+event);
                ObserverManager.getInstance().notify(EventType.MEDIA_ADDED,0,0,new File(dirPath+File.separator+path));
                break;
            }
            case FileObserver.DELETE:{
                EasyLog.d(TAG, "file delete path:"+ path+",event="+event);
                break;
            }
            case FileObserver.MOVED_TO:{
                EasyLog.d(TAG, "file [move to] path:"+ path+",event="+event);
                ObserverManager.getInstance().notify(EventType.MEDIA_ADDED,0,0,new File(dirPath+File.separator+path));
                break;
            }
            case FileObserver.MOVED_FROM:{
                EasyLog.d(TAG, "file [move from] path:"+ path+",event="+event);
                ObserverManager.getInstance().notify(EventType.MEDIA_DELETE,0,0,new File(dirPath+File.separator+path));

                break;
            }
        }
    }
}


