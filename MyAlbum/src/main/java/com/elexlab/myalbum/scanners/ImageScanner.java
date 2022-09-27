package com.elexlab.myalbum.scanners;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

/**
 * Created by BruceYoung on 10/15/17.
 */
public class ImageScanner extends MediaScanner{
    public ImageScanner() {
        loaderId = LoaderIds.IMAGE_MEDIA;
    }

    private static String[] PROJECTIONS = {
            MediaStore.Images.Media.DATA, // the real path
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.MediaColumns.DATE_MODIFIED
    };
    private static final String WHERE = MediaStore.Images.Media.SIZE + ">0";
    private static final String ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id != loaderId) return null;

        return new CursorLoader(
                context,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                PROJECTIONS,
                WHERE,
                null,
                ORDER_BY
        );
    }
}
