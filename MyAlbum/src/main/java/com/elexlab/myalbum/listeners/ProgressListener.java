package com.elexlab.myalbum.listeners;

/**
 * Created by BruceYoung on 11/5/17.
 */
public interface ProgressListener<T> {
    void onProgress(float progress,T t);
    void onComplete();
    void onError(int code,String message);
}
