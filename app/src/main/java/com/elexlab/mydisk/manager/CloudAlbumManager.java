package com.elexlab.mydisk.manager;


import android.media.Image;
import android.media.MediaMetadataRetriever;

import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.DistAlbums;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.pojos.SysAlbum;
import com.elexlab.myalbum.pojos.Video;
import com.elexlab.myalbum.utils.MediaUtils;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudFileManager;
import com.elexlab.mydisk.cloud.CloudUtils;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.utils.FileOpenUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CloudAlbumManager {
    private final static String TAG = CloudAlbumManager.class.getSimpleName();
    private static CloudAlbumManager instance = new CloudAlbumManager();

    public static CloudAlbumManager getInstance() {
        return instance;
    }

    private CloudAlbumManager(){

    }

    private DistAlbums distAlbums = null;
    public CloudAlbumManager reload(){
        distAlbums = null;
        return this;
    }
    public void loadAlbum(DataSourceCallback<DistAlbums> dataSourceCallback){
        if(distAlbums!=null){
            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(distAlbums);
            }
            return;
        }
        Map<String, FileInfo> cloudFileMap = CloudFileManager.getInstance().getCloudFileMap();
        Set<String> keys = cloudFileMap.keySet();
        Map<String, List<FileInfo>> mediaMap = new HashMap<>();

        DistAlbums distAlbums = new DistAlbums();
        for(String key:keys){
            if(!key.startsWith(MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode())){
               continue;
            }
            FileInfo fileInfo = cloudFileMap.get(key);
            String dir = fileInfo.getDir();
            if(!mediaMap.containsKey(dir)){
                mediaMap.put(dir,new ArrayList<FileInfo>());
            }
            mediaMap.get(dir).add(fileInfo);
        }

        for(String key:mediaMap.keySet()){
            List<FileInfo> fileInfos = mediaMap.get(key);
            List<Media> mediaList = new ArrayList<>();
            for(FileInfo fileInfo:fileInfos){
                Media media=null;
                if(FileOpenUtils.isImage(fileInfo.getName())){
                    media = FileInfo.fileInfo2Media(fileInfo);
                }else if(FileOpenUtils.isVideo(fileInfo.getName())){
                    media = new Video(FileInfo.fileInfo2Media(fileInfo));
                    MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                    retriever.setDataSource(fileInfo.getUrl(), new HashMap<>());
                    String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                    long duration = Long.parseLong( time );
                    media.setDuration((int) duration);
                }

                mediaList.add(media);
            }
            String albumName = CloudUtils.parseFile(key).getName();
            Album album = new SysAlbum(albumName);
            album.setMediaList(mediaList);
            distAlbums.getSysAlbumList().add(album);
        }
        Collections.sort(distAlbums.getSysAlbumList(), (o1, o2) -> o1.getName().compareTo(o2.getName()));
        if(dataSourceCallback != null){
            dataSourceCallback.onSuccess(distAlbums);
        }
    }
}
