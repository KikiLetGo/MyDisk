package com.elexlab.myalbum.managers;

import android.app.Activity;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.text.TextUtils;


import com.elexlab.myalbum.data.LocalAlbumDataSource;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.mediaprocessor.transaction.ReEncryptionAlbumsRedo;
import com.elexlab.myalbum.mediaprocessor.transaction.ReEncryptionDoProcessor;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverInterf;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.DataAlbum;
import com.elexlab.myalbum.pojos.DistAlbums;
import com.elexlab.myalbum.pojos.EncryptedAlbum;
import com.elexlab.myalbum.pojos.EncryptedPhoto;
import com.elexlab.myalbum.pojos.EncryptedVideo;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.pojos.CustomAlbum;
import com.elexlab.myalbum.pojos.Photo;
import com.elexlab.myalbum.pojos.SysAlbum;
import com.elexlab.myalbum.pojos.Video;
import com.elexlab.myalbum.scanners.ImageScanner;
import com.elexlab.myalbum.scanners.LocalMediaScanner;
import com.elexlab.myalbum.scanners.MediaReceiver;
import com.elexlab.myalbum.scanners.MediaScanner;
import com.elexlab.myalbum.scanners.MyFileObserver;
import com.elexlab.myalbum.scanners.VideoScanner;
import com.elexlab.myalbum.utils.AlbumAndPhotoUtils;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.MediaFormatUtils;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/5/17.
 */
public class AlbumManager{
    private final static String TAG = AlbumManager.class.getSimpleName();
    private static AlbumManager instance = new AlbumManager();
    public static AlbumManager getInstance() {
        return instance;
    }
    private Map<String,String> nameMap = new HashMap<>();
    private static List<MyFileObserver> myFileObservers = new ArrayList<>();
    LocalAlbumDataSource localAlbumDataSource = new LocalAlbumDataSource();

    private AlbumManager(){
        nameMap.put("Camera","相机");
        nameMap.put("Screenshots","截屏");
        nameMap.put("WeiXin","微信");
        nameMap.put("Download","下载");
        listenMedias();
    }



    public interface AlbumLoadListener {
        void onLoaded(List<Album> albumList);
    }

    public interface AlbumLoadDistListener {
        void onLoaded(DistAlbums allAlbum);
    }

    //private List<Album> albumList = null;
    private DistAlbums distAlbums = null;


    private final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media._ID
    };

    public void preloadAlbums(Activity activity){
        HandlerThread tempThread = new HandlerThread("tempThread");
        tempThread.start();
        Handler handler = new Handler(tempThread.getLooper());
        handler.post(()->{
            loadAlbumsWithDist(activity,null);

        });
    }
    private void configMediaObserver(){
        for(Album album:distAlbums.getSysAlbumList()){
            MyFileObserver myFileObserver = new MyFileObserver(new File(album.getAlbumDirPath())) ;
            myFileObserver.startWatching();
            myFileObservers.add(myFileObserver);
        }
    }

    private int state = 0;
    private Queue<AlbumLoadDistListener> waitLoadListenerQueue = new ArrayDeque<>();
    public void loadAlbumsWithDist(final Context context, final AlbumLoadDistListener albumLoadDistListener) {
        if(state == 1){
            waitLoadListenerQueue.add(albumLoadDistListener);
            return;
        }
        if (distAlbums != null) {
            if (albumLoadDistListener != null) {
                albumLoadDistListener.onLoaded(distAlbums);
            }
            return;
        }
        state=1;//loading
        distAlbums = new DistAlbums();
        //my albums
        try {
            List<Album> myAlbums = loadMyAlbum(false);
            //2.encrypted albums
            myAlbums.addAll(loadMyAlbum(true));
            AlbumAndPhotoUtils.sortAlbums(myAlbums);
            distAlbums.setMyAlbumList(myAlbums);
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadLocalAlbums(context, albumList -> {
            distAlbums.setSysAlbumList(albumList);
            //albumList.addAll(sysAlbumList);
            state = 2;//loaded
            if (albumLoadDistListener != null) {
                albumLoadDistListener.onLoaded(distAlbums);
            }
            AlbumLoadDistListener listener;
            while((listener=waitLoadListenerQueue.poll())!=null){
                listener.onLoaded(distAlbums);
            }
            configMediaObserver();
        });

        return;
    }




    public List<Album> loadMyAlbum(boolean isEncrypted) {
        String albumsDirPath;
        if (!isEncrypted) {
            albumsDirPath = PathUtils.getPublicAlbumsSavePath();
        } else {
            albumsDirPath = PathUtils.getPublicEncryptionAlbumsSavePath();
        }
        File albumsDir = new File(albumsDirPath);
        List<Album> albumList = new ArrayList<>();

        if (albumsDir.exists()) {
            File[] albumFiles = albumsDir.listFiles();
            if(albumFiles != null){
                for (File albumDir : albumFiles) {
                    if (!albumDir.exists() || !albumDir.isDirectory()) {
                        EasyLog.i(TAG, "albumDir not valid:" + albumDir);
                        continue;
                    }
                    Album album;
                    if (isEncrypted) {
                        album = new EncryptedAlbum(albumDir.getName());
                    } else {
                        album = new CustomAlbum(albumDir.getName());
                    }
                    File[] photosFiles = albumDir.listFiles();
                    if (photosFiles == null || photosFiles.length <= 0) {
                        albumDir.delete();
                        continue;
                    } else if (photosFiles.length == 1 && photosFiles[0].getName().endsWith(".json")) {
                        albumDir.delete();
                        continue;
                    }
                    List<Media> mediaList = new ArrayList<Media>();
                    for (File photoFile : photosFiles) {
                        if (!photoFile.exists() || photoFile.isDirectory()) {
                            EasyLog.i(TAG, "photoFile not valid:" + photoFile);
                            continue;
                        }
                        if (Album.PROP_FILE_NAME.equals(photoFile.getName())) {
                            continue;
                        }
                        Media media = null;
                        if (MediaFormatUtils.isVideoFileType(photoFile.getName())) {
                            if (isEncrypted) {
                                EncryptedVideo encryptedVideo = new EncryptedVideo();
                                encryptedVideo.setFile(photoFile);
                                encryptedVideo
                                        .setCoverFile(new File(((EncryptedAlbum) album).whatVideoCoverPathInAlbum(encryptedVideo)));
                                media = encryptedVideo;
                            } else {
                                media = new Video();
                                media.setFile(photoFile);
                            }
                        } else {
                            media = isEncrypted ? new EncryptedPhoto() : new Photo();
                            media.setFile(photoFile);
                        }
                        album.loadProp();
                        media.setFileMapping(album.getMediaFileMapping(media));
                        media.loadMediaInfo();
                        mediaList.add(media);
                    }

                    album.setMediaList(mediaList);
                    album.loadProp();
                    sortPhotosForAlbum(album);
                    albumList.add(album);
                }
            }

        }
        return albumList;
    }

    private void sortPhotosForAlbum(Album album) {
        AlbumAndPhotoUtils.sortAlbumByFileCreateTime(album);
    }

    private void loadLocalAlbums(Context context,final AlbumLoadListener albumLoadListener){

        Observable.create((ObservableOnSubscribe<List<Album>>) e -> {
            localAlbumDataSource.loadLocalAlbums(context);
            e.onNext(localAlbumDataSource.getLocalAlbums());
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(albumMap -> {
            albumLoadListener.onLoaded(albumMap);
        });
    }



    private Map<String, Album> systemTempAlbumMap;

    private void addMediasToAlbum(List<Media> medias, boolean isVideo) {
        //Map<String,Album> albumMap = new HashMap<String,Album>();
        for (Media media : medias) {
            File file = media.getFile();
            //获取该图片的父路径名
            String parentName = file.getParentFile().getName();

            //is mindin album?
            if (file.getParent().contains(PathUtils.getPublicBasePath())) {
                continue;
            }

            Album album = systemTempAlbumMap.get(parentName);
            if (album == null) {
                SysAlbum sysAlbum = new SysAlbum(parentName);
                sysAlbum.setAlbumDirPath(file.getParent());
                album = sysAlbum;
                systemTempAlbumMap.put(parentName, album);
            }

            Media mindInMedia = isVideo ? new Video(media) : new Photo(media);
            mindInMedia.setFile(file);
            album.addMedia(mindInMedia);
        }
    }

    public void createAlbum(Album album) {
        album.getProp().setSort(0);
        album.saveProp();
        for (Album myAlbum : distAlbums.getMyAlbumList()) {
            if (myAlbum.getProp() == null) {
                continue;
            }
            myAlbum.getProp().setSort(myAlbum.getProp().getSort() + 1);
            myAlbum.saveProp();
        }
        distAlbums.getMyAlbumList().add(0, album);
    }

    public void handleBadSorts() {
        int sort = 0;
        for (Album myAlbum : distAlbums.getMyAlbumList()) {
            myAlbum.getProp().setSort(sort);
            myAlbum.saveProp();
            sort++;
        }
    }

    public void updateAlbum(Album album, String oldName) {
        if (oldName != null && !oldName.endsWith(album.getName())) {
            String albumsPath = PathUtils.getPublicAlbumsSavePath();
            new File(albumsPath, oldName).renameTo(new File(albumsPath, album.getName()));
        }

    }

    public void deleteAlbum(Album album, int mode, ProgressListener progressListener)
            throws EncryptionKeyMissMatchException {
        if (mode == Album.DeletePhotoMode.NO_RECOVRE) {
            album.deleteAlbum();
            if (progressListener != null) {
                progressListener.onComplete();
            }

        } else if (mode == Album.DeletePhotoMode.RECOVRE) {
            //step 1.recover media in album
            album.recoverToOriginalPath(progressListener);

            //step 2.delete album
            album.deleteAlbum();
        }

    }

    public void onPhotoDelete(Media media) {
        if (distAlbums == null) {
            return;
        }
        List<Album> albumList = distAlbums.getAllAlbumList();
        if (media == null || albumList == null || albumList.size() <= 0) {
            return;
        }
        for (Album album : albumList) {
            if (album == null) {
                continue;
            }
            //virtual album -> 'all'
            if (album instanceof DataAlbum) {
                continue;
            }
            Media mediaForDelete = null;
            for (Media mediaInAlbum : album.getMediaList()) {
                if (media.equals(mediaInAlbum)) {
                    mediaForDelete = mediaInAlbum;
                    break;
                }
            }
            if (mediaForDelete != null) {
                album.getMediaList().remove(mediaForDelete);
                break;
            }
        }
    }

    public interface AlbumsCodecListener {
        void onAlbumCodecFinish(Album album, float process);

        void onMediaCodecFinish(Media media, float process);

        void onComplete(List<Album> albumList);
    }


    private float reEncryptAllEncryptedAlbumsProcess;
    private int encryptedMediaCounts;
    public void reEncryptAllEncryptedAlbums(String oldPass,String newPass,final AlbumsCodecListener albumsCodecListener)
        throws EncryptionKeyMissMatchException {
        ReEncryptionAlbumsRedo redo = new ReEncryptionAlbumsRedo();
        redo.start(com.elexlab.myalbum.MyAlbum.getContext());
        ReEncryptionDoProcessor reEncryptionDoProcessor = new ReEncryptionDoProcessor();
        reEncryptionDoProcessor.setProgressListener(new ProgressListener<Media>() {
            @Override
            public void onProgress(float process,Media media) {
                if(albumsCodecListener != null){
                        albumsCodecListener.onMediaCodecFinish(media,reEncryptAllEncryptedAlbumsProcess + process);
                }
            }

            @Override
            public void onComplete() {
                if(albumsCodecListener != null){
                    albumsCodecListener.onComplete(null);
                }
            }

            @Override
            public void onError(int code, String message) {

            }
        });
        redo.setNewPass(newPass);
        redo.setOldPass(oldPass);
        redo.updateCheckpoint(ReEncryptionDoProcessor.CheckPoint.NOTHING);
        reEncryptionDoProcessor.process(redo);
    }


    public Media pickupOnEncryptedMedia(){
        if(distAlbums == null){
            return null;
        }
        if(distAlbums.getMyAlbumList() == null){
            return null;
        }
        List<Media> encryptedMedias = new ArrayList<Media>();
        for(Album album:distAlbums.getMyAlbumList()){
            if(album.isEncryption() && album.getMediaList() != null && album.getMediaList().size()>0){
                //return album.getMediaList().get(0);
                encryptedMedias.addAll(album.getMediaList());
            }
        }
        int mediaSize = encryptedMedias.size();

        if(mediaSize <= 0){
            return null;
        }

        int index = new Random().nextInt(mediaSize-1);
        return encryptedMedias.get(index);
    }

    public boolean checkIfAlbumNameExist(String name){
        if(TextUtils.isEmpty(name)){
            return true;
        }
        List<Album> albumList = distAlbums.getMyAlbumList();

        for(Album album:albumList){
           if(name.equals(album.getName())){
               return true;
           }
        }
        return false;
    }

    public boolean checkIfAlbumNameExist(String name,Album exceptAlbum){
        if(TextUtils.isEmpty(name)){
            return true;
        }
        List<Album> albumList = distAlbums.getMyAlbumList();

        for(Album album:albumList){
            if(name.equalsIgnoreCase(album.getName()) && !album.equals(exceptAlbum)){
                return true;
            }
        }
        return false;
    }

    private void listenMedias(){
        MediaReceiver.setMediaChangedListener(new MediaReceiver.MediaChangedListener() {
            @Override
            public void onMediaAdded(Media media) {
                for(Album album:distAlbums.getAllAlbumList()){
                    if(album.isMediaInThisAlbum(media)){
                        Media systemMedia =Media.autoCreateMedia(media);
                        album.addMedia(album.whatMediaIndexInAlbum(systemMedia),systemMedia);
                        ObserverManager.getInstance().notify(EventType.PHOTO_ADDED,0,0,media);
                        break;
                    }
                }
            }

            @Override
            public void onMediaDeleted(Media media) {

            }
        });
    }

    public String nameMapping(String enName){
        if(enName == null || ! nameMap.containsKey(enName)){
            return enName;
        }
        return nameMap.get(enName);
    }

    public LocalAlbumDataSource getLocalAlbumDataSource() {
        return localAlbumDataSource;
    }
}
