package com.elexlab.mydisk.constants;

/**
 * Created by BruceYoung on 10/8/17.
 */
public class ActivityTransCodes {
    public interface RequestCode{
        int PHOTO_CHOOSE = 1;
        int TAKE_PICTRUE = PHOTO_CHOOSE + 1;
        int UPDATE_ALBUM= TAKE_PICTRUE + 1;
    }
    public interface ResultCode{
        int PHOTO_CHOOSED = 1;
        int ALBUM_PERMISSION_CHANGED = PHOTO_CHOOSED + 1;
    }
}
