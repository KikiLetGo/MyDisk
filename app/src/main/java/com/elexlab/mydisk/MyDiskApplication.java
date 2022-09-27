package com.elexlab.mydisk;

import android.app.Application;
import android.os.Handler;

import com.elexlab.myalbum.MyAlbum;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.HeroLib;


public class MyDiskApplication extends Application {
    private static Handler mainHandler;
    public  static Handler getMainHandler(){
        return mainHandler;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        HeroLib.getInstance().appContext = this;
        mainHandler = new Handler();
        ThreadManager.getInstance().setMainHandler(new Handler());
        MyAlbum.init(this);
    }
}
