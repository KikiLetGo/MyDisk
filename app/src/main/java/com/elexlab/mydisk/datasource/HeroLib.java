package com.elexlab.mydisk.datasource;

import android.app.Activity;
import android.content.Context;



/**
 * Created by Young on 2016/12/8.
 */
public class HeroLib {
    private static HeroLib instance = new HeroLib();
    public static HeroLib getInstance(){
        return instance;
    }

    private HeroLib(){

    }
    public Context appContext;
    private Class<Activity> appActivityClass;

    public Class<Activity> getAppActivityClass() {
        return appActivityClass;
    }

    public void setAppActivityClass(Class appActivityClass) {
        this.appActivityClass = appActivityClass;
    }

    public Context getAppContext() {
        return appContext;
    }

    public void init(Context appContext) {
        this.appContext = appContext;
        //DownloadWorker.getInstance();//start up
        //FileDownloader.init(appContext);
    }
}
