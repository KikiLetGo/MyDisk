package com.elexlab.myalbum.notify;

/**
 * Created by BruceYoung on 10/7/17.
 */
public interface EventType {
    int START_VALUE = 0;
    int ALBUM_LOAD_FINISH = START_VALUE + 1;
    int PHOTO_DELETED = ALBUM_LOAD_FINISH + 1;
    int PHOTO_ADDED = PHOTO_DELETED + 1;
    int MEDIA_ADDED = PHOTO_ADDED + 1;
    int MEDIA_DELETE = MEDIA_ADDED + 1;

    int ALBUM_CREATED = MEDIA_DELETE + 1;
    int ALBUM_DELETED = ALBUM_CREATED + 1;
    int ALBUM_UPDATE = ALBUM_DELETED + 1;
    int DEVICE_SWITCHING = ALBUM_UPDATE + 1;
    int DEVICE_CHANGED = DEVICE_SWITCHING + 1;


}
