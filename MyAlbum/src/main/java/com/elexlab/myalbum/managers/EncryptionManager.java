package com.elexlab.myalbum.managers;

import android.content.Context;
import android.graphics.Bitmap;


import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.DataAlbum;
import com.elexlab.myalbum.pojos.EncryptedAlbum;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.EncryptedPhoto;
import com.elexlab.myalbum.pojos.EncryptedVideo;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.pojos.CustomAlbum;
import com.elexlab.myalbum.pojos.Photo;
import com.elexlab.myalbum.pojos.Video;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.FileUtils;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/10/17.
 */
public class EncryptionManager {
    private final static String TAG = EncryptionManager.class.getSimpleName();
    private static EncryptionManager instance = new EncryptionManager();
    public static EncryptionManager getInstance(){
        return instance;
    }
    private EncryptionManager(){}

    private FileEncryption fileEncryption =FileEncryption.getFileEncryption();// new ConcealFileEncryption();//new DESFileEncryption();//new XORFileEncryption();


    public interface AlbumCodecListener{
        void onMediaCodec(Media media,float process);
    }

    public EncryptedAlbum encryptAlbum(Album album, String password,
                                       Context context,
                                       boolean interruptWhenException,
                                       AlbumCodecListener albumCodecListener)
            throws EncryptionKeyMissMatchException {
        EncryptedAlbum encryptedAlbum = new EncryptedAlbum(album.getName());
        encryptedAlbum.setType(album.getType());
        //encryptedAlbum.setMediaList(album.getMediaList());

        for (int i = 0; i<album.getMediaList().size(); i++) {
            Media media = album.getMediaList().get(i);
            File fromFile = media.getFile();
            //String encryptionPath = PathUtils.getPublicEncryptionAlbumsSavePath();
            String encryptionPath = encryptedAlbum.whatMediaTempPathUnderAlbum(media);//encryptedAlbum.whatPhotoPathUnderAlbum(media);
            //file path
            File destFile = new File(encryptionPath);
            try {
                fileEncryption.encrypt(password,media.getFile(),destFile,null);
                destFile.setLastModified(fromFile.lastModified());
            }catch (EncryptionKeyMissMatchException exception){
                if(interruptWhenException){
                    encryptedAlbum.deleteAlbum();
                    throw exception;
                }
            }
            //FileUtils.deleteFileWithMedia(fromFile,context);
            //update file to encrypted
            EncryptedMedia encryptedMedia = null;
            if(media.isVideo()){
                EncryptedVideo encryptedVideo = new EncryptedVideo(media);
                Bitmap coverBitmap = media.loadBitmap(context,Media.COVER_WIDTH,Media.COVER_HEIGHT);
                String coverPath = encryptedAlbum.whatVideoCoverPathInAlbum(media);
                FileUtils.saveBitmap(coverPath,coverBitmap);
                encryptedVideo.setCoverFile(new File(coverPath));
                encryptedMedia = encryptedVideo;

            }else{
                encryptedMedia = new EncryptedPhoto(media);
            }
            encryptedMedia.setFile(destFile);
            //media.setFile(destFile);
            encryptedAlbum.getMediaList().add(i,encryptedMedia);

            //delete old file
            //MediaManager.getInstance().deleteMedia(media,context);

            EasyLog.d(TAG,"encrypt file over:"+fromFile.getName());

            if(albumCodecListener != null){
                float process = (float)i/(float) album.getMediaList().size();
                albumCodecListener.onMediaCodec(media,process);
            }
        }
        for(Media media:encryptedAlbum.getMediaList()){
            File destFile = new File(encryptedAlbum.whatPhotoPathUnderAlbum(media));
            File tempFile = media.getFile();
            tempFile.renameTo(destFile);
            media.setFile(destFile);
        }

        album.deleteAlbum();

        FileUtils.deleteAllFileAndDir(encryptedAlbum.whatMediaTempPath());

        return encryptedAlbum;
    }

    public CustomAlbum decryptAlbum(EncryptedAlbum album, String password,
                                    Context context,
                                    boolean interruptWhenException,
                                    AlbumCodecListener albumCodecListener)
            throws EncryptionKeyMissMatchException{
        CustomAlbum decryptedAlbum = new CustomAlbum(album.getName());
        //decryptedAlbum.setMediaList(album.getMediaList());

        for (int i = 0; i<album.getMediaList().size(); i++) {
            Media media = album.getMediaList().get(i);
            File fromFile = media.getFile();
            //String encryptionPath = PathUtils.getPublicEncryptionAlbumsSavePath();
            String decryptionPath = decryptedAlbum.whatMediaTempPathUnderAlbum(media);//decryptedAlbum.whatPhotoPathUnderAlbum(media);
            //file path
            File destFile = new File(decryptionPath);
            try {
                fileEncryption.decrypt(password,media.getFile(),destFile,null);
                destFile.setLastModified(fromFile.lastModified());
            }catch (EncryptionKeyMissMatchException exception){
                if(interruptWhenException){
                    decryptedAlbum.deleteAlbum();
                    throw exception;
                }
            }

            //update file to encrypted
            Media decryptedMedia = null;
            if(media.isVideo()){
                decryptedMedia = new Video(media);

            }else{
                decryptedMedia = new Photo(media);
            }

            //update file to decrypted
            decryptedMedia.setFile(destFile);

            decryptedAlbum.getMediaList().add(i,decryptedMedia);

            //delete old file
            //MediaManager.getInstance().deleteMedia(media,context);
            EasyLog.d(TAG,"decrypt file over:"+fromFile.getName());

            if(albumCodecListener != null){
                float process = (float)i/(float) album.getMediaList().size();
                albumCodecListener.onMediaCodec(media,process);
            }
        }
        for(Media media:decryptedAlbum.getMediaList()){
            File destFile = new File(decryptedAlbum.whatPhotoPathUnderAlbum(media));
            media.getFile().renameTo(destFile);
            media.setFile(destFile);
        }
        FileUtils.deleteAllFileAndDir(decryptedAlbum.whatMediaTempPath());
        return decryptedAlbum;
    }

    public EncryptedMedia encryptMedia(String password,EncryptedAlbum encryptedAlbum,Media media)throws EncryptionKeyMissMatchException{
        EncryptedMedia encryptedMedia = null;

        Context context = com.elexlab.myalbum.MyAlbum.getContext();
        File fromFile = media.getFile();
        //String encryptionPath = encryptedAlbum.whatPhotoPathUnderAlbum(media);// PathUtils.getPublicEncryptionPhotoSavePath(context);
        String encryptionPath = encryptedAlbum.whatMediaTempPathUnderAlbum(media);// PathUtils.getPublicEncryptionPhotoSavePath(context);
        //encryptionPath += File.separator + fromFile.getName();
        File destTempFile = new File(encryptionPath);
        fileEncryption.encrypt(password,media.getFile(),destTempFile,null);
        destTempFile.setLastModified(fromFile.lastModified());

        //update to new path
        if(media.isVideo()){
            EncryptedVideo encryptedVideo = new EncryptedVideo(media);
            Bitmap coverBitmap = media.loadBitmap(context,Media.COVER_WIDTH,Media.COVER_HEIGHT);
            String coverPath = encryptedAlbum.whatVideoCoverPathInAlbum(media);
            FileUtils.saveBitmap(coverPath,coverBitmap);
            encryptedVideo.setCoverFile(new File(coverPath));
            encryptedMedia = encryptedVideo;

        }else{
            encryptedMedia = new EncryptedPhoto(media);
        }

        File destFile = new File(encryptedAlbum.whatPhotoPathUnderAlbum(media));
        destTempFile.renameTo(destFile);
        encryptedMedia.setFile(destFile);
        //delete old media
        MediaManager.getInstance().deleteMedia(media,context);
        //MediaManager.getInstance().updatePhoto(media);
        EasyLog.d(TAG,"encrypt file over:"+fromFile.getName());
        return encryptedMedia;
    }


    public EncryptedMedia encryptMedia(String password,Media media,String destPath,FileEncryption.CodecListener codecListener)
            throws EncryptionKeyMissMatchException{
        EncryptedMedia encryptedMedia = null;
        File fromFile = media.getFile();
        File destFile = new File(destPath);
        fileEncryption.encrypt(password,media.getFile(),destFile,codecListener);
        destFile.setLastModified(fromFile.lastModified());
        //update to new path
        if(media.isVideo()){
            encryptedMedia = new EncryptedVideo(media);
        }else{
            encryptedMedia = new EncryptedPhoto(media);
        }
        encryptedMedia.setFile(destFile);
        EasyLog.d(TAG,"decrypt media over:"+fromFile.getName());
        return encryptedMedia;
    }

    public Media decryptMedia(String password,Media media,String destPath,FileEncryption.CodecListener codecListener)throws EncryptionKeyMissMatchException{
        Media decryptedMedia = null;
        File fromFile = media.getFile();
        File destFile = new File(destPath);
        fileEncryption.decrypt(password,media.getFile(),destFile,codecListener);
        destFile.setLastModified(fromFile.lastModified());
        //update to new path
        if(media.isVideo()){
            decryptedMedia = new Video(media);
        }else{
            decryptedMedia = new Photo(media);
        }
        decryptedMedia.setFile(destFile);
        decryptedMedia.setDuration(media.getDuration());
        EasyLog.d(TAG,"decrypt media over:"+fromFile.getName());
        return decryptedMedia;
    }

    public interface DecryptAlbumListener{
        void onPhotoDecrypted(Media media);
        void onError(Album album,Media media);
        void onAlbumDecryptedComplete(Album album);
    }
    public Album decryptAlbum(String password,Album album,Context context,DecryptAlbumListener decryptAlbumListener)
            throws EncryptionKeyMissMatchException{
        Album decryptedAlbum = new DataAlbum();
        decryptedAlbum.setName(album.getName());
        decryptedAlbum.setType(album.getType());
        for (Media media :album.getMediaList()) {
            try {
                File fromFile = media.getFile();
                String decryptionPath = PathUtils.getPublicDecryptionPhotoSavePath(context);
                decryptionPath += File.separator + fromFile.getName();
                File destFile = new File(decryptionPath);
                fileEncryption.decrypt(password,media.getFile(),destFile,null);
                destFile.setLastModified(fromFile.lastModified());
                Media decryptedMedia = media.isVideo()?new Video():new Photo();
                decryptedMedia.setFile(destFile);
                decryptedMedia.setDuration(media.getDuration());
                //decryptedMedia.setOriginalDirPath(media.getOriginalDirPath());
                decryptedAlbum.addMedia(decryptedMedia);
                //media.setFile(destFile);
                //MediaManager.getInstance().updatePhoto(media);
                EasyLog.d(TAG,"encrypt file over:"+fromFile.getName());
                if(decryptAlbumListener != null){
                    decryptAlbumListener.onPhotoDecrypted(decryptedMedia);
                }
            }catch (Exception e){
                if(decryptAlbumListener != null){
                    decryptAlbumListener.onError(album, media);
                }
            }

        }

        if(decryptAlbumListener != null){
            decryptAlbumListener.onAlbumDecryptedComplete(decryptedAlbum);
        }

        return decryptedAlbum;
    }

    public interface KeyDetectListener{
        void onProgress(float progress);
        void onComplete(boolean match);
    }
    public void detectKeyValid(final String key, final KeyDetectListener keyDetectListener){

        Observable.create(new ObservableOnSubscribe<Float>() {
            @Override
            public void subscribe(ObservableEmitter<Float> e) throws Exception {
                Media detectMedia = AlbumManager.getInstance().pickupOnEncryptedMedia();
                if(detectMedia == null){
                    e.onComplete();
                    return;
                }
                //max 3 times
                for(int i=0;i<10;i++){
                    String tempPath = PathUtils.getPublicTempPath()
                            + File.separator + System.currentTimeMillis();
                    File tempFile = new File(tempPath);
                    try {
                        fileEncryption.decrypt(key,detectMedia.getFile(),tempFile,null);
                        tempFile.setLastModified(detectMedia.getFile().lastModified());
                        e.onComplete();
                        return;
                    }catch (EncryptionKeyMissMatchException exception){
                        EasyLog.i(TAG,"EncryptionKeyMissMatchException so key not valid");
                    }finally {
                        tempFile.delete();
                    }
                    e.onNext((float)i/(float)10);
                }
                e.onError(new EncryptionKeyMissMatchException("key not valid",detectMedia.getFile()));
            }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Float>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Float value) {
                if(keyDetectListener != null){
                    keyDetectListener.onProgress(value);
                }
            }

            @Override
            public void onError(Throwable e) {
                if(keyDetectListener != null){
                    keyDetectListener.onComplete(false);
                }
            }

            @Override
            public void onComplete() {
                if(keyDetectListener != null){
                    keyDetectListener.onComplete(true);
                }
            }
        });
    }
}
