package com.elexlab.myalbum.scanners;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.telephony.mbms.FileInfo;

import androidx.annotation.WorkerThread;

import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.pojos.SysAlbum;
import com.elexlab.myalbum.utils.MediaUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalMediaScanner {
    private Context mContext;

    private Filter<Long> mSizeFilter;
    private Filter<String> mMimeFilter;
    private Filter<Long> mDurationFilter;
    private boolean mFilterVisibility;

    public LocalMediaScanner(Context context) {
        this.mContext = context;
    }

    private static final String WHERE = MediaStore.Images.Media.SIZE + ">0";
    private static final String ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";

    /**
     * Image attribute.
     */
    private static final String[] IMAGES = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.MediaColumns.DATE_MODIFIED
    };

    /**
     * Scan for image files.
     */
    @WorkerThread
    private List<Media> scanImageFile() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                IMAGES,
                WHERE,
                null,
                ORDER_BY);
        List<Media> medias = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Media media = cursorToMedia(cursor);
                medias.add(media);
            }
            cursor.close();
        }
        return medias;
    }

    /**
     * Video attribute.
     */
    private static final String[] VIDEOS = {
            MediaStore.Video.Media.DATA,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.LATITUDE,
            MediaStore.Video.Media.LONGITUDE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.MediaColumns.DATE_MODIFIED
    };

    /**
     * Scan for image files.
     */
    @WorkerThread
    private List<Media> scanVideoFile() {
        ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                VIDEOS,
                WHERE,
                null,
                ORDER_BY);
        List<Media> medias = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Media media = cursorToMedia(cursor);
                medias.add(media);
            }
            cursor.close();
        }
        return medias;
    }

    /**
     * Scan the list of pictures in the library.
     */
    @WorkerThread
    public List<Media> getAllImage() {

        List<Media> mediaList = scanImageFile();

        return mediaList;
    }

    /**
     * Scan the list of videos in the library.
     */
    @WorkerThread
    public List<Media> getAllVideo() {
        Map<String, Album> AlbumMap = new HashMap<>();
        List<Media> mediaList = scanVideoFile();

        return mediaList;
    }

    /**
     * Get all the multimedia files, including videos and pictures.
     */
//    @WorkerThread
//    public ArrayList<Album> getAllMedia() {
//
//        List<Media> images = scanVideoFile();
//        List<Media> videos = scanVideoFile();
//
//
//
//        for (Map.Entry<String, Album> folderEntry : AlbumMap.entrySet()) {
//            Album Album = folderEntry.getValue();
//            Collections.sort(Album.getAlbumFiles());
//            Albums.add(Album);
//        }
//        return Albums;
//    }

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
}
