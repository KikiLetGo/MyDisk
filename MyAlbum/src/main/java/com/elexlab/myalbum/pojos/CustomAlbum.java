package com.elexlab.myalbum.pojos;

import android.text.TextUtils;


import com.alibaba.fastjson.JSON;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.managers.MediaManager;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/13/17.
 */
public class CustomAlbum extends Album{
    private final static String TAG = CustomAlbum.class.getSimpleName();
    public CustomAlbum() {
    }

    public CustomAlbum(String name) {
        super(name,AlbumType.MINE);
    }

    @Override
    public void addPhotosToAlbum(List<Media> medias, final PhotoAddListener photoAddListener, int transMode) {
        recordAlbumProp(medias);
        saveProp();

        switch (transMode){
            case  TransPhotoMode.COPY:{
                cpFileToMyDir(medias,photoAddListener);
                break;
            }
            case  TransPhotoMode.MOVE:{
                mvFileToMyDir(medias,photoAddListener);
                break;
            }
            default:break;
        }
        EasyLog.d(TAG,"trans file finish");
        for(Media media:medias){
            EasyLog.d(TAG,"addmedia:"+media.getDisplayName());
            int index = whatMediaIndexInAlbum(media);
            EasyLog.d(TAG,"index:"+index);

            mediaList.add(index, media);
            EasyLog.d(TAG,"addmedia:"+media.getDisplayName()+ " finish");

        }
        EasyLog.d(TAG," mediaList.add finish");

        if(photoAddListener != null){
            EasyLog.d(TAG,"photoAddListener.onComplete()");
            photoAddListener.onComplete();
        }
    }

    @Override
    public void addPhotoToAlbum(Media media, PhotoAddListener photoAddListener, int transMode) {
        addPhotoToAlbum(mediaList.size()-1, media,photoAddListener,transMode);
    }

    @Override
    public void addPhotoToAlbum(int index, Media media, PhotoAddListener photoAddListener, int transMode) {
        List<Media> medias = new ArrayList<Media>();
        medias.add(media);
        recordAlbumProp(medias);
        saveProp();

        switch (transMode){
            case  TransPhotoMode.COPY:{
                cpFileToMyDir(medias,photoAddListener);
                break;
            }
            case  TransPhotoMode.MOVE:{
                mvFileToMyDir(medias,photoAddListener);
                break;
            }
            default:break;
        }
        mediaList.addAll(index, medias);
        if(photoAddListener != null){
            photoAddListener.onComplete();
        }
    }


    @Override
    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
        loadProp();

    }


    protected void mvFileToMyDir(List<Media> mediaList, PhotoAddListener photoAddListener){
        for(int i=0;i<mediaList.size();i++){
            Media media = mediaList.get(i);
            File file = media.getFile();
            String newPath = whatPhotoPathUnderAlbum(media);
            //FileUtils.cpFile(file.getAbsolutePath(),newPath);
            //FileUtils.deleteFileWithMedia(file,HeroLib.getInstance().appContext);
            FileUtils.cpFile(file.getAbsolutePath(),newPath);
            MediaManager.getInstance().deleteMedia(media, com.elexlab.myalbum.MyAlbum.getContext());
            File newFile = new File(newPath);
            media.setFile(newFile);
            if(photoAddListener != null){
                float process = (float)i/(float) mediaList.size();
                photoAddListener.onPhotoAdded(media,0,process);
            }
        }
    }
    protected void cpFileToMyDir(final List<Media> mediaList,final  PhotoAddListener photoAddListener){
        for(int i=0;i<mediaList.size();i++){
            final  Media media = mediaList.get(i);
            final File file = media.getFile();
            final String newPath = whatPhotoPathUnderAlbum(media);
            FileUtils.cpFile(file.getAbsolutePath(),newPath);
            //FileUtils.cpFileWithMetaKeep(file.getAbsolutePath(),newPath);
            File newFile = new File(newPath);
            media.setFile(newFile);
            if(photoAddListener != null){
                float process = (float) i /(float) mediaList.size();
                photoAddListener.onPhotoAdded(media,0,process);
            }

        }
    }
    protected void recordAlbumProp(List<Media> mediaList){
        //prop = new Album.Prop();
        loadProp();
        if(prop == null){
            EasyLog.i(TAG,"prop is null!");
            //return;
            prop = new Prop();
            prop.setCreateTime(new File(getAlbumDirPath()).lastModified());
        }else{
            prop.setCreateTime(getCreateTime());
        }
        List<Prop.FileMapping> fileMappings = prop.getFileMappings();
        for (Media media : mediaList){
            File file = media.getFile();
            String fileName =file.getName();
            String originalPath = media.getOriginalPath();
            Prop.FileMapping fileMapping = new Prop.FileMapping();
            fileMapping.setFileName(fileName);
            fileMapping.setOriginalFilePath(originalPath);
            fileMapping.setDuration(media.getDuration());
            fileMapping.setLastModified(media.getLastModify());
            fileMappings.add(fileMapping);
        }
        prop.setFileMappings(fileMappings);
        setProp(prop);
    }

    public EncryptedAlbum encryptAlbum(String password,
                                       boolean interruptWhenException,
                                       EncryptionManager.AlbumCodecListener albumCodecListener)
            throws EncryptionKeyMissMatchException {
        EncryptedAlbum encryptedAlbum =
                EncryptionManager.getInstance().encryptAlbum(this, password,
                        com.elexlab.myalbum.MyAlbum.getContext(),
                        interruptWhenException,
                        albumCodecListener);
        loadProp();
        encryptedAlbum.setProp(prop);
        encryptedAlbum.saveProp();
        deleteAlbum();
        return encryptedAlbum;
    }

    @Override
    public Album loadProp(){
        if(prop != null){
            return this;
        }
        String dirPath = getAlbumDirPath();
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if(TextUtils.isEmpty(name)){
                    return false;
                }
                if(!name.equals(PROP_FILE_NAME)){
                    return false;
                }
                return true;
            }
        });
        if(files == null || files.length <= 0){
            return this;
        }
        String json = "";
        File propFile = files[0];
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(propFile));
            String line;
            while ((line = fileReader.readLine())!=null){
                json += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(fileReader != null){
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        prop = JSON.parseObject(json,Prop.class);
        return this;
    }

    @Override
    public Prop getProp() {
        loadProp();
        if(prop == null){
            recordAlbumProp(mediaList);
        }
        return prop;
    }

    public void saveProp(){
        String json = JSON.toJSONString(prop);
        File file = new File(getPropFilePath());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
