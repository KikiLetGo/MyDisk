package com.elexlab.myalbum.managers;

import android.app.Activity;
import android.content.Context;


import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.scanners.AllImageScanner;
import com.elexlab.myalbum.scanners.ImageScanner;
import com.elexlab.myalbum.scanners.MediaScanner;
import com.elexlab.myalbum.utils.FileUtils;

import java.util.List;

/**
 * Created by BruceYoung on 10/17/17.
 */
public class MediaManager {
    private static MediaManager instance = new MediaManager();
    public static MediaManager getInstance(){
        return instance;
    }

    public void loadAllMedia(MediaScanner.MediaLoadListener mediaLoadListener){

    }
    public void loadAllPhotoMedia(final Activity context, final MediaScanner.MediaLoadListener mediaLoadListener){
        final AllImageScanner allImageScanner = new AllImageScanner();
        allImageScanner.setMediaLoadListener(new MediaScanner.MediaLoadListener() {
            @Override
            public void onLoadFinished(List<Media> medias) {
                if(mediaLoadListener != null){
                    mediaLoadListener.onLoadFinished(medias);
                }
                allImageScanner.destroyScanner(context);
            }
        });
        allImageScanner.startScan(context);
    }

    public void loadPhotoMedia(final Activity context, final MediaScanner.MediaLoadListener mediaLoadListener){
        final MediaScanner mediaScanner = new ImageScanner();
        mediaScanner.setMediaLoadListener(new MediaScanner.MediaLoadListener() {
            @Override
            public void onLoadFinished(List<Media> medias) {
                if(mediaLoadListener != null){
                    mediaLoadListener.onLoadFinished(medias);
                }
                mediaScanner.destroyScanner(context);
            }
        });
        mediaScanner.startScan(context);
    }

    public boolean deleteMedia(Media media, Context context){
        boolean success = FileUtils.deleteFileWithMedia(media.getFile(),context);
        if(!success){
            return false;
        }
        AlbumManager.getInstance().onPhotoDelete(media);
        return true;
    }
}
