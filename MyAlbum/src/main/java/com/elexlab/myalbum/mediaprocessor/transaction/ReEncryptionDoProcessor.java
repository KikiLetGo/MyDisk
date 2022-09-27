package com.elexlab.myalbum.mediaprocessor.transaction;


import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.managers.AlbumManager;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.EncryptedPhoto;
import com.elexlab.myalbum.pojos.EncryptedVideo;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.FileUtils;
import com.elexlab.myalbum.utils.MediaFormatUtils;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by BruceYoung on 11/8/17.
 */
public class ReEncryptionDoProcessor implements DoProcessor{
    private static final String TAG = ReEncryptionDoProcessor.class.getSimpleName();
    private ProgressListener<Media> progressListener;

    public void setProgressListener(ProgressListener<Media> progressListener) {
        this.progressListener = progressListener;
    }

    public interface CheckPoint{
        int NOTHING = 0;
        int TEMP_FILE_MOVED = 1;
        int RE_ENCRYPTED = 2;
        int TEMP_FILE_DELETED = 3;
    }
    @Override
    public void process(Do doSome) {
        int checkPoint = doSome.getCheckpoint();
        switch (checkPoint){
            case CheckPoint.NOTHING:{
                mvFileToTemp((ReEncryptionAlbumsRedo) doSome);
                break;
            }
            case CheckPoint.TEMP_FILE_MOVED:{
                try {
                    reEncryption(doSome);
                } catch (EncryptionKeyMissMatchException e) {
                    e.printStackTrace();
                }
                break;
            }
            case CheckPoint.RE_ENCRYPTED:{
                deleteTempFiles(doSome);
                break;
            }
            case CheckPoint.TEMP_FILE_DELETED:{
                doSome.commit();
                break;
            }
            default:break;
        }
    }
    private void mvFileToTemp(ReEncryptionAlbumsRedo reEncryptionAlbumsRedo){
        String tempFileDir = PathUtils.getReEncryptionTempPath();
        File tempDir = new File(tempFileDir);
        List<Album> encryptedAlbums = AlbumManager.getInstance().loadMyAlbum(true);
        Map<String,String> fileMapping = new HashMap<String, String>();
        for(Album album:encryptedAlbums){
            List<Media> medias = album.getMediaList();
            for(Media media:medias){
                String fromPath = media.getFile().getPath();
                String tempPath = tempDir + File.separator + media.getDisplayName();
                FileUtils.cpFile(fromPath,tempPath);
                fileMapping.put(fromPath,tempPath);
            }
        }
        reEncryptionAlbumsRedo.setFileMappings(fileMapping);
        reEncryptionAlbumsRedo.updateCheckpoint(CheckPoint.TEMP_FILE_MOVED);
        try {
            reEncryption(reEncryptionAlbumsRedo);
        } catch (EncryptionKeyMissMatchException e) {
            e.printStackTrace();
        }
    }

    private void reEncryption(Do redo) throws EncryptionKeyMissMatchException {
        ReEncryptionAlbumsRedo reEncryptionAlbumsRedo = (ReEncryptionAlbumsRedo) redo;
        String oldPassword = reEncryptionAlbumsRedo.getOldPass();
        String newPassword = reEncryptionAlbumsRedo.getNewPass();
        File reEncryptionTempDir = new File(PathUtils.getReEncryptionTempPath());
        if(!reEncryptionTempDir.exists()){
            EasyLog.e(TAG,"reEncryptionTempDir not exists");
            return;
        }
        Map<String,String> fileMappings = reEncryptionAlbumsRedo.getFileMappings();
        Set<String> fromPathSet = fileMappings.keySet();
        int i = 0;
        for (String fromPath:fromPathSet){
            i++;
            String reEncryptionTempPath = fileMappings.get(fromPath);
            File reEncryptionTempFile = new File(reEncryptionTempPath);
            if(!reEncryptionTempFile.exists()){
                EasyLog.e(TAG,"reEncryptionTempPath not exist");
                continue;
            }
            Media media = null;
            if(!MediaFormatUtils.isVideoFileType(fromPath)){
                media = new EncryptedPhoto();
            }else{
                media = new EncryptedVideo();
            }
            media.setFile(reEncryptionTempFile);
            String tempPath = PathUtils.getPublicTempPath();
            tempPath += File.separator + media.getDisplayName();
            Media decryptedMedia = EncryptionManager.getInstance()
                    .decryptMedia(oldPassword, media, tempPath, new FileEncryption.CodecListener() {
                @Override
                public void onProcess(final float progress) {


                }
            });
            EncryptedMedia encryptedMedia = EncryptionManager.getInstance()
                    .encryptMedia(newPassword, decryptedMedia, fromPath, new FileEncryption.CodecListener() {
                @Override
                public void onProcess(float progress) {

                }
            });

            float progress = (float) i/(float) fromPathSet.size();
            if(progressListener != null){
                progressListener.onProgress(progress,media);
            }
        }
        redo.updateCheckpoint(CheckPoint.RE_ENCRYPTED);
        deleteTempFiles(redo);
    }


    private void deleteTempFiles(Do redo){
        String tempFileDir = PathUtils.getReEncryptionTempPath();
        FileUtils.deleteAllFileAndDir(tempFileDir);
        redo.updateCheckpoint(CheckPoint.TEMP_FILE_DELETED);
        redo.commit();
        if(progressListener != null){
            progressListener.onComplete();
        }
    }
}
