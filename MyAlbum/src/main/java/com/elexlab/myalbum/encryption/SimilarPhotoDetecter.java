package com.elexlab.myalbum.encryption;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import com.elexlab.myalbum.pojos.Media;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/5/17.
 */
public abstract class SimilarPhotoDetecter {
    protected Context context;

    public interface DetectionListener {
        void onDetectionFinished(List<List<Media>> similarPhotosList);
        void onSimilarPhotoFound(List<Media> similarMedias);
        void onProcessing(float process);
    }
    private DetectionListener detectionListener;

    public SimilarPhotoDetecter setDetectionListener(DetectionListener detectionListener) {
        this.detectionListener = detectionListener;
        return this;
    }

    public SimilarPhotoDetecter(Context context) {
        this.context = context;
    }

    public void detectSimilarPhotos(final List<Media> mediaList){
        if(mediaList == null || mediaList.size() <= 0){
            return;
        }
        //n*(n-1)/2
        int photoCounts = mediaList.size();
        int compareCounts = photoCounts*(photoCounts-1)/2;
        int currentCompareNum = 0;
        final List<List<Media>> similarPhotosList = new ArrayList<List<Media>>();
        for(int i = 0; i< mediaList.size(); i++){
            final Media media = mediaList.get(i);
            if(media.isVideo()){
                continue;
            }
            Bitmap bitmap1 = BitmapFactory.decodeFile(media.getFile().getAbsolutePath());

            for(int j = i+1; j< mediaList.size(); j++){
                final Media nextMedia = mediaList.get(j);
                if(nextMedia.isVideo()){
                    continue;
                }
                final Bitmap bitmap2 = BitmapFactory.decodeFile(nextMedia.getFile().getAbsolutePath());

                int diff = detectSimilarity(bitmap1,bitmap2);
                if(diff < 10){
                    List<Media> similarMediaList = new ArrayList<Media>();
                    similarMediaList.add(media);
                    similarMediaList.add(nextMedia);
                    similarPhotosList.add(similarMediaList);
                    if(detectionListener != null){
                        detectionListener.onSimilarPhotoFound(similarMediaList);
                    }
                    //return;
                }
                currentCompareNum ++;
                float process =  (100*((float)currentCompareNum/(float)compareCounts));
                if(detectionListener != null){
                    detectionListener.onProcessing(process);
                }


            }
        }
        if(detectionListener != null){
            detectionListener.onDetectionFinished(similarPhotosList);
        }
    }

    protected interface  DetectSimilarityListener{
        void onDetectionFinished(int diff);
    }
    protected abstract int detectSimilarity(File file1,File file2);
    protected abstract int detectSimilarity(Bitmap bitmap1, Bitmap bitmap2);
}
