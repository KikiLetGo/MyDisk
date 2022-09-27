package com.elexlab.myalbum.scanners;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;

/**
 * Created by BruceYoung on 10/17/17.
 */
public class AllMediaSanner extends MediaScanner{
    private static String[] PROJECTIONS = {
            MediaStore.MediaColumns.DATA, // the real path
            MediaStore.MediaColumns.TITLE,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.DATE_MODIFIED,
    };
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(id != LoaderIds.ALL_MEDIA){
            return null;
        }
        String selection = MediaStore.Files.FileColumns.MIME_TYPE + "= ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? "
                + " or " + MediaStore.Files.FileColumns.MIME_TYPE + " = ? ";

        String[] selectionArgs = new String[]{"image/ico", "image/jpeg", "image/png", "video/mp4"};

        CursorLoader cursorLoader =
                new CursorLoader(context,
                MediaStore.Files.getContentUri("external"), PROJECTIONS,
                selection, selectionArgs, PROJECTIONS[2] + " DESC");
        return cursorLoader;
    }
}
