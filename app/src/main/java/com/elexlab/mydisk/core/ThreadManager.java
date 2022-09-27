package com.elexlab.mydisk.core;

import android.os.Handler;
import android.os.HandlerThread;

public class ThreadManager {
    private static final String TAG = ThreadManager.class.getSimpleName();
    private static ThreadManager instance = new ThreadManager();
    public static ThreadManager getInstance(){
        return instance;
    }


    Handler httpHandler;
    Handler jsonParseHandler;
    Handler mainHandler;
    Handler mediaPlayerHandler;
    Handler operationHandler;
    private ThreadManager() {
        HandlerThread httpThread = new HandlerThread("httpThread");
        httpThread.start();
        httpHandler = new Handler(httpThread.getLooper());



        HandlerThread jsonParseThread = new HandlerThread("jsonParseThread");
        jsonParseThread.start();
        jsonParseHandler = new Handler(jsonParseThread.getLooper());

        HandlerThread mediaPlayThread = new HandlerThread("mediaPlayThread");
        mediaPlayThread.start();
        mediaPlayerHandler = new Handler(mediaPlayThread.getLooper());

        HandlerThread operationThread = new HandlerThread("operationThread");
        operationThread.start();
        operationHandler = new Handler(operationThread.getLooper());

    }

    public Handler getHttpHandler() {
        return httpHandler;
    }


    public Handler getJsonParseHandler() {
        return jsonParseHandler;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public void setMainHandler(Handler mainHandler) {
        this.mainHandler = mainHandler;
    }

    public Handler getOperationHandler() {
        return operationHandler;
    }


    public Handler getMediaPlayerHandler() {
        return mediaPlayerHandler;
    }
}