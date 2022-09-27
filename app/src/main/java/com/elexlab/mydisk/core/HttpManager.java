package com.elexlab.mydisk.core;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by BruceYoung on 15/11/13.
 */
public class HttpManager {
    private static HttpManager instance = new HttpManager();
    public static HttpManager getInstance(){
        return instance;
    }
    RequestQueue mQueue ;

    public RequestQueue getRequestQueue(Context context){
        if(mQueue==null){
            mQueue = Volley.newRequestQueue(context);
        }
        return mQueue;
    }
}
