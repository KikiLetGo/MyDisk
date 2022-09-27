package com.elexlab.myalbum.pojos;

import android.content.Context;
import android.graphics.Bitmap;


import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.managers.PasswordManager;
import com.elexlab.myalbum.utils.FileUtils;
import com.elexlab.myalbum.utils.ImageUtils;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by bruceyoung on 17-10-11.
 */

public class EncryptedAlbum extends CustomAlbum {
    private final static String TAG = EncryptedAlbum.class.getSimpleName();
    public EncryptedAlbum(String name) {
        super(name);
    }



    @Override
    public void addPhotosToAlbum(final List<Media> medias, final PhotoAddListener photoAddListener, int mode) {
        recordAlbumProp(medias);
        saveProp();

        //encrypt medias
        encryptPhotos(medias, new PhotoAddListener() {
            @Override
            public void onComplete() {
                for(Media media:medias){
                    int index = whatMediaIndexInAlbum(media);
                    mediaList.add(index,media);
                }
                if(photoAddListener != null){
                    photoAddListener.onComplete();
                }
            }

            @Override
            public void onPhotoAdded(Media media,int index,float percent) {
                if(photoAddListener != null){
                    photoAddListener.onPhotoAdded(media,index,percent);
                }
            }
        });
    }

    @Override
    public void addPhotoToAlbum(Media media, PhotoAddListener photoAddListener, int mode) {
        addPhotoToAlbum(mediaList.size()-1, media,photoAddListener,mode);
    }

    @Override
    public void addPhotoToAlbum(final int index, final Media media, final PhotoAddListener photoAddListener, int mode) {
        final List<Media> medias = new ArrayList<Media>();
        medias.add(media);
        recordAlbumProp(medias);
        saveProp();

        //encrypt medias
        //encryptPhotos(medias,photoAddListener);
        //encrypt medias
        encryptPhotos(medias, new PhotoAddListener() {
            @Override
            public void onComplete() {
                mediaList.addAll(index,medias);
                if(photoAddListener != null){
                    photoAddListener.onComplete();
                }
            }

            @Override
            public void onPhotoAdded(Media media,int index,float percent) {
                if(photoAddListener != null){
                    photoAddListener.onPhotoAdded(media,index,percent);
                }
            }
        });
    }


    private float encryptMediasProgress = 0;
    private void encryptPhotos(final List<Media> medias, final PhotoAddListener photoAddListener){
        final String password = PasswordManager.getInstance().readPassword(com.elexlab.myalbum.MyAlbum.getContext());
        Observable.fromArray(medias.toArray(new Media[]{}))
        .map(new Function<Media, Media>() {
            int i = 0;
            @Override
            public Media apply(Media media) throws Exception {
                EncryptedMedia encryptedMedia = EncryptionManager.getInstance().encryptMedia(password,EncryptedAlbum.this, media);
                medias.set(i,encryptedMedia);
                encryptMediasProgress = (float)i/(float) medias.size();
                //e.onNext(encryptedMedia);
                i++;
                return encryptedMedia;
            }
        })

        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Media>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Media value) {
                if(value != null){
                    photoAddListener.onPhotoAdded(value,0,encryptMediasProgress);
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                if(photoAddListener != null){
                    photoAddListener.onComplete();
                }
            }
        });
    }
    @Override
    public String getAlbumsDir(){
        String albumsDir = (PathUtils.getPublicEncryptionAlbumsSavePath());
        return albumsDir;
    }

    @Override
    public String whatPhotoPathUnderAlbum(Media media) {
        String photoFilePath = getAlbumDirPath() + File.separator + media.getFile().getName();
        return photoFilePath;
    }

    @Override
    public String whatMediaTempPath(){
        String mediaPathDir = PathUtils.getPublicTempPath() + File.separator + "encryption";
        mediaPathDir += File.separator + getName();
        return mediaPathDir;
    }
    public String whatVideoCoverPathInAlbum(Media media){
        String videoCoverDir = getAlbumDirPath() + File.separator + "video_cover";
        PathUtils.checkPath(videoCoverDir);
        String photoFilePath = videoCoverDir + File.separator + media.getFile().getName();
        return photoFilePath;
    }

    @Override
    public boolean isEncryption() {
        return true;
    }


    public CustomAlbum decryptAlbum(String password,
                                    boolean interruptWhenException,
                                    EncryptionManager.AlbumCodecListener albumCodecListener)
            throws EncryptionKeyMissMatchException {
        CustomAlbum decryptedAlbum = EncryptionManager.getInstance().decryptAlbum(this,password,
                com.elexlab.myalbum.MyAlbum.getContext(),interruptWhenException,albumCodecListener);
        loadProp();
        decryptedAlbum.setProp(prop);
        decryptedAlbum.saveProp();
        deleteAlbum();
        return decryptedAlbum;
    }

    /**
     * 90% for decrypt
     * 10% for io(mv files)
     * @param progressListener
     * @throws EncryptionKeyMissMatchException
     */
    @Override
    public void recoverToOriginalPath(final ProgressListener progressListener)
            throws EncryptionKeyMissMatchException{
        CustomAlbum decryptedAlbum =
                decryptAlbum(PasswordManager.getInstance().readPassword( com.elexlab.myalbum.MyAlbum.getContext()),
                false,
                new EncryptionManager.AlbumCodecListener() {
                    @Override
                    public void onMediaCodec(Media media, float process) {
                        if(progressListener != null){
                            progressListener.onProgress(0.9f*process,media);
                        }
                    }
                });

        decryptedAlbum.recoverToOriginalPath(new ProgressListener<Media>() {
            @Override
            public void onProgress(float progress,Media media) {
                if(progressListener != null){
                    progressListener.onProgress(0.1f*progress + 0.9f,media);
                }
            }

            @Override
            public void onComplete() {
                if(progressListener != null){
                    progressListener.onComplete();
                }
            }

            @Override
            public void onError(int code, String message) {
                if(progressListener != null){
                    progressListener.onError(code,message);
                }
            }
        });
        decryptedAlbum.deleteAlbum();
    }

    public static boolean existEncrytedAlbums(){
        String encryptedAlbumDir = PathUtils.getPublicEncryptionAlbumsSavePath();
        File dir = new File(encryptedAlbumDir);
        if(!dir.exists()){
            return false;
        }
        String[] albums = dir.list();
        if(albums == null || albums.length <= 0){
            return false;
        }
        return true;
    }

    @Override
    public Media getCoverMedia() {
        String albumCoverPath = getAlbumCoverPath();

        File albumCoverFile = new File(albumCoverPath);
        if (!albumCoverFile.exists()){
            Media coverMedia = super.getCoverMedia();
            saveAlbumCover(coverMedia);
            return coverMedia;
        }
        Photo photo = new Photo();
        photo.setFile(albumCoverFile);
        return photo;
    }

    private String getAlbumCoverPath() {
        String albumCoverPathDir =  getAlbumDirPath() + File.separator + "album_prop" ;
        PathUtils.checkPath(albumCoverPathDir);
        return albumCoverPathDir + File.separator +"cover.png";
    }

    private void saveAlbumCover(final Media coverMedia) {
        final Context context = com.elexlab.myalbum.MyAlbum.getContext();
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) throws Exception {
                Bitmap bitmap = coverMedia.loadBitmap(context,200,200);
                Bitmap blurBitmap = ImageUtils.fastblur(context,bitmap,30);
                FileUtils.saveBitmap(getAlbumCoverPath(),blurBitmap);
            }
        })
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe();
    }


}
