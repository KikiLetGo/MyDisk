package com.elexlab.myalbum.scanners;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.elexlab.myalbum.pojos.Media;


/**
 * Created by BruceYoung on 10/15/17.
 */
public class VideoScanner extends MediaScanner {
    private static final int URL_LOAD_LOCAL_VIDEO = 1;

    public VideoScanner() {
        loaderId = LoaderIds.VIDEO_MEDIA;
    }

    private static final Uri MEDIA_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private static String[] PROJECTIONS = {
            MediaStore.Video.Media.DATA, // the real path
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.MIME_TYPE,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.WIDTH,
            MediaStore.Video.Media.HEIGHT,
            MediaStore.Video.Media.DATE_MODIFIED
    };
    private static final String WHERE = MediaStore.Video.Media.SIZE + ">0";
    private static final String ORDER_BY = MediaStore.Video.Media.DISPLAY_NAME + " DESC";
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != loaderId) return null;

        return new CursorLoader(
                context,
                MEDIA_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY
        );
    }

    @Override
    protected Media cursorToMedia(Cursor cursor) {
        Media media = super.cursorToMedia(cursor);
        media.setDuration(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
        return media;
    }
}
