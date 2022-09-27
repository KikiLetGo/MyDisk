package com.elexlab.myalbum.scanners;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.provider.MediaStore;


import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.MediaUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/15/17.
 */
public abstract class MediaScanner implements LoaderManager.LoaderCallbacks<Cursor>{
    protected interface LoaderIds{
        int ALL_MEDIA = 1;
        int IMAGE_MEDIA = 2;
        int VIDEO_MEDIA = 3;
        int ALL_IMAGE_MEDIA = 4;
        int ALL_VIDEO_MEDIA = 5;
    }
    protected int loaderId = -1;
    public interface MediaLoadListener{
        //void onMediaLoaded(Media media);
        void onLoadFinished(List<Media> medias);
    }
    protected MediaLoadListener mediaLoadListener;
    protected Context context;

    public MediaScanner setMediaLoadListener(MediaLoadListener mediaLoadListener){
        this.mediaLoadListener = mediaLoadListener;
        return this;
    }

    public void startScan(Activity context){
        this.context = context;
        context.getLoaderManager().initLoader(loaderId, null, this);
    }

    public void destroyScanner(Activity context){
        context.getLoaderManager().destroyLoader(loaderId);
        mediaLoadListener = null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader,final Cursor cursor) {
        final List<Media> medias = new ArrayList<Media>();

        Observable.create(new ObservableOnSubscribe<List<Media>>() {


            @Override
            public void subscribe(ObservableEmitter<List<Media>> e) throws Exception {
                if (cursor != null && cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    do {
                        Media media = cursorToMedia(cursor);
                        if(media != null && media.getFile().exists()){
                            medias.add(media);
                        }
                    } while (cursor.moveToNext());
                    e.onComplete();
                }else{
                    e.onComplete();
                }
            }
        })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<Media>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(List<Media> value) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                if(mediaLoadListener != null){
                    mediaLoadListener.onLoadFinished(medias);
                }
            }
        });


    }

    protected Media cursorToMedia(Cursor cursor) {
        Media media = new Media();

        String realPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
        File file = new File(realPath);
        media.setFile(file);

        media.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)));
        media.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)));


        media.setSize(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)));
        media.setWidth(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)));
        media.setHeight(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)));
        long dateModify = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
        media.setLastModify(dateModify*1000);

        MediaUtils.loadMediaInfo(media);


        if(media.getWidth() <= 0 || media.getHeight() <=0){
            int[] widthAndHeight = MediaUtils.getImageWidthHeight(media.getFile().getAbsolutePath());
            media.setWidth(widthAndHeight[0]);
            media.setHeight(widthAndHeight[1]);
        }
        return media;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
