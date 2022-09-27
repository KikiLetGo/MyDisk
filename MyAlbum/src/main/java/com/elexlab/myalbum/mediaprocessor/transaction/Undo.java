package com.elexlab.myalbum.mediaprocessor.transaction;

import android.content.Context;

import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.PathUtils;


/**
 * Created by BruceYoung on 10/24/17.
 */
public class Undo extends Do{


    public Undo(Media media, int action) {
        super(media, action);
    }

    public void start(Context context) {

    }

    @Override
    public void rollback(Context context) {

    }

    @Override
    public void commit() {

    }

    @Override
    public String getLogPath(Context context) {
        return PathUtils.getUndoLogPath(context);
    }
}
