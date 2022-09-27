package com.elexlab.myalbum.notify;

/**
 * Created by BruceYoung on 15/11/10.
 */
public interface ObserverInterf {
    public static interface ObserverType {
        public static int START_VALUE = 0;
        public static int MUSIC_PALY_STATE_CHANGE = START_VALUE + 1;

        //play list
        int PLAY_LIST_START_VALUE = 4000;
        int FAVORITE_PLAY_LIST_CHANGED = PLAY_LIST_START_VALUE + 1;

        //download
        int DOWNLOAD_START_VALUE = 6000;
        int DOWNLOAD_START = DOWNLOAD_START_VALUE+1;
        int DOWNLOAD_SUCCESS = DOWNLOAD_START+1;

        //kivimusic player notify

        int KIVI_MUSIC_PLAYER_START_VALUE = 20000;
        int I_NEED_DOWN_LOAD_A_MUSIC = KIVI_MUSIC_PLAYER_START_VALUE + 1;
        int CURRENT_PLAYING_MUSIC_CAHNGE = I_NEED_DOWN_LOAD_A_MUSIC + 1;
    }

    //type means observe type,param1,param2,obj is the data that we want to send
    public boolean onDataChange(int type, long param1, long param2, Object obj);
}
