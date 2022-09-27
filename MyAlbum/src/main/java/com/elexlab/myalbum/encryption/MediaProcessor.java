package com.elexlab.myalbum.encryption;

import android.content.Context;


import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.mediaprocessor.transaction.MediaReEncryptionRedo;
import com.elexlab.myalbum.mediaprocessor.transaction.Redo;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by BruceYoung on 10/24/17.
 */
public class MediaProcessor {
    private final static String TAG = MediaProcessor.class.getSimpleName();
    private interface DoLog{
        String RE_ENCRYPT_ALBUMS = "reencrypt albums";
        String RE_ENCRYPT_MEDIA = "reencrypt media";
        String ENCRYPT_ALBUM = "encrypt album where id=%s";
        String DENCRYPT_ALBUM = "decrypt album where id=%s";
    }

    public static MediaProcessor newInstance(){
        MediaProcessor mediaProcessor = new MediaProcessor();
        return mediaProcessor;
    }
    private MediaProcessor(){

    }

    public interface MediaProcessListener{
        void onMediaProcessFinish(Media media, float process);
        void onComplete();
    }
    private float reEncryptAllEncryptedAlbumsProcess;
    //private int encryptedMediaCounts;
    public void reencryptAllEncryptedAlbums(String oldPass,String newPass,
                                            final List<Album> albumList,
                                            final MediaProcessListener mediaProcessListener)
            throws EncryptionKeyMissMatchException {
        final Context context = MyAlbum.getContext();
        reEncryptAllEncryptedAlbumsProcess = 0;
        //1.create redo
        EasyLog.d(TAG,"***************create redo map***************");
        Map<Media, Redo> redoMap = new HashMap<>();
        for(Album album:albumList){
            for(Media media:album.getMediaList()){
                Redo reencryptMediaRedo = new MediaReEncryptionRedo(media,oldPass,newPass);
                redoMap.put(media,reencryptMediaRedo);
                reencryptMediaRedo.start(MyAlbum.getContext());
            }
        }
        //2.decrypt medias
        EasyLog.d(TAG,"decrypt medias");
        for(Album album:albumList){
            EasyLog.d(TAG,"decrypt album:"+album.getName());
            for(int i=0;i<album.getMediaList().size();i++){
                Media media = album.getMediaList().get(i);
                EasyLog.d(TAG,"decrypt media:"+media.getDisplayName());
                String tempPath = PathUtils.getPublicTempPath() + File.separator + "albums";
                PathUtils.checkPath(tempPath);
                tempPath = tempPath + File.separator + album.getName();//album.whatMediaTempPathUnderAlbum(media);
                Media decryptedMedia = EncryptionManager.getInstance().decryptMedia(oldPass,media,tempPath,null);
                album.getMediaList().set(i,decryptedMedia);

                EasyLog.d(TAG,"decrypt media over:"+media.getDisplayName());

                if(mediaProcessListener != null){
                    float process = 0.5f * (float) i/(float) album.getMediaList().size();
                    mediaProcessListener.onMediaProcessFinish(media,reEncryptAllEncryptedAlbumsProcess + process);
                }
            }
        }
        reEncryptAllEncryptedAlbumsProcess = 0.5f;
        //3.encrypt medias
        for(Album album:albumList){
            EasyLog.d(TAG,"encrypt album:"+album.getName());
            for(int i=0;i<album.getMediaList().size();i++){
                Media media = album.getMediaList().get(i);
                EasyLog.d(TAG,"encrypt media:"+media.getDisplayName());

                String tempPath = album.whatMediaTempPathUnderAlbum(media);
               PathUtils.checkPath(tempPath);
                Media encryptedMedia = EncryptionManager.getInstance().encryptMedia(newPass,media,tempPath,null);
                album.getMediaList().set(i,encryptedMedia);
                Redo reencryptMediaRedo = redoMap.get(media);
                if(reencryptMediaRedo == null){
                    EasyLog.e(TAG,"mediaReencryptRedo is null!media:"+media.getFile().getAbsolutePath());
                    continue;
                }
                reencryptMediaRedo.commit();

                encryptedMedia.mvToByRename(album.whatPhotoPathUnderAlbum(encryptedMedia));

                EasyLog.d(TAG,"encrypt media over:"+media.getDisplayName());

                if(mediaProcessListener != null){
                    float process = 0.5f * (float) i/(float) album.getMediaList().size();
                    mediaProcessListener.onMediaProcessFinish(media,reEncryptAllEncryptedAlbumsProcess + process);
                }
            }
        }
        EasyLog.d(TAG,"**********reencrypt album over***************");
        if(mediaProcessListener != null){
            mediaProcessListener.onComplete();
        }

    }

//    public void reencryptAllEncryptedMedia(String oldPass,String newPass, final Media media,
//                                            final MediaProcessListener mediaProcessListener){
//
//        String tempPath = PathUtils.getPublicTempPath() + File.separator + "albums";
//        try {
//            Media decryptedMedia = EncryptionManager.getInstance().decryptMedia(oldPass,media,tempPath);
//
//            String tempPath = album.whatMediaTempPathUnderAlbum(media);
//            Media encryptedMedia = EncryptionManager.getInstance().encryptMedia(newPass,decryptedMedia,tempPath);
//
//        } catch (EncryptionKeyMissMatchException e) {
//            e.printStackTrace();
//        }
//
//    }
}
