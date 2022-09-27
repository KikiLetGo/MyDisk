package com.elexlab.myalbum.data;

import android.content.Context;

import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverInterf;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.pojos.Photo;
import com.elexlab.myalbum.pojos.SysAlbum;
import com.elexlab.myalbum.pojos.Video;
import com.elexlab.myalbum.scanners.ImageScanner;
import com.elexlab.myalbum.scanners.LocalMediaScanner;
import com.elexlab.myalbum.scanners.MediaScanner;
import com.elexlab.myalbum.scanners.VideoScanner;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LocalAlbumDataSource extends DynamicDataSource<Album> implements ObserverInterf {
    private static final String TAG = LocalAlbumDataSource.class.getSimpleName();
    private Map<String, Album> localAlbums = new HashMap<>();

    public LocalAlbumDataSource() {
        ObserverManager.getInstance().regist(this);
    }

    public List<Album> getLocalAlbums() {
        List albums = new ArrayList(localAlbums.values());
        Collections.sort(albums, new Comparator<Album>() {
            @Override
            public int compare(Album o1, Album o2) {
                if(o1 == null || o2 == null){
                    return 0;
                }
                return o2.getMediaList().size()-o1.getMediaList().size();
            }
        });
        return albums;
    }

    public void loadLocalAlbums(Context context){
        LocalMediaScanner localMediaScanner = new LocalMediaScanner(context);
        List<Media> images = localMediaScanner.getAllImage();
        List<Media> videos = localMediaScanner.getAllVideo();
        addMedias2Album(images);
        addMedias2Album(videos);
    }

    private void addMedias2Album(List<Media> medias) {
        for (Media media : medias) {
            addMedia2Album(media,-1);
        }
    }

    private Album addMedia2Album(Media media, int pos) {
        File file = media.getFile();
        String dirName = file.getParentFile().getName();
        String dirPath = file.getParentFile().getAbsolutePath();
        if (file.getParent().contains(PathUtils.getPublicBasePath())) {
            return null;
        }
        if (!localAlbums.containsKey(dirPath)) {//create if no exist
            SysAlbum sysAlbum = new SysAlbum(dirName);
            sysAlbum.setAlbumDirPath(dirPath);
            localAlbums.put(dirPath,sysAlbum);
        }
        Album album = localAlbums.get(dirPath);
        if(pos == -1){
            album.addMedia(media);
        }else{
            album.addMedia(pos,media);
        }
        return album;
    }

    private Album removeMediaFromAlbum(Media media){
        File file = media.getFile();
        String dirName = file.getParentFile().getName();
        String dirPath = file.getParentFile().getAbsolutePath();
        if (!localAlbums.containsKey(dirPath)) {//create if no exist
            EasyLog.i(TAG,"Do not remove a not exist media from album!!!");
            return null;
        }
        Album album = localAlbums.get(dirPath);
        album.removeMedia(media);
        return album;

    }

    @Override
    public boolean onDataChange(int type, long param1, long param2, Object obj) {
        switch (type){
            case EventType.MEDIA_ADDED:{
                File file = (File) obj;
                Media media = new Media(file);
                Album album = addMedia2Album(media,0);
                DataListener<Album> listener;
                for(DataListener dataListener:listeners){
                    dataListener.onDataChanged(album);
                }

                break;
            }
            case EventType.MEDIA_DELETE:{
                File file = (File) obj;
                Media media = new Media(file);
                Album album = removeMediaFromAlbum(media);
                DataListener<Album> listener;
                for(DataListener dataListener:listeners){
                    dataListener.onDataChanged(album);
                }
                break;
            }
        }
        return false;
    }
}
