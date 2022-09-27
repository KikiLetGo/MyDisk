package com.elexlab.myalbum.pojos;

import android.text.TextUtils;


import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.managers.AlbumManager;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.myalbum.utils.FileUtils;
import com.elexlab.myalbum.utils.MediaUtils;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/3/17.
 */

public abstract class Album implements Serializable{
    private final static String TAG = Album.class.getSimpleName();
    public interface AlbumType{
        int SYSTEM = 0;
        int MINE = 1;
    }
    public interface TransPhotoMode{
        int COPY = 0;
        int MOVE = 1;
    }
    public interface DeletePhotoMode{
        int RECOVRE = 0;
        int NO_RECOVRE = 1;
    }
    private String id;
    private String name;
    protected List<Media> mediaList = new ArrayList<>();
    private Media coverMedia;
    private int type = AlbumType.SYSTEM;

    public static final String PROP_FILE_NAME = "prop.json";
    protected Prop prop;

    Album(){

    }
    public Album(String name,int type) {
        super();
        this.name = name;
        this.type = type;

        if(type == AlbumType.SYSTEM){
            return;
        }
        if(TextUtils.isEmpty(name)){
            throw new IllegalStateException("album name could not be empty");
        }
        //check and create dir (if not exist)
        File dir = new File(getAlbumDirPath());
        if(!dir.exists()){
            dir.mkdir();
        }
    }

    public String getId() {
        return String.valueOf(getAlbumDirPath());
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShowName(){
        return AlbumManager.getInstance().nameMapping(name);
    }

    public long getCreateTime() {

        loadProp();
        if(prop == null){
            File dir = new File(getAlbumDirPath());
            return dir.lastModified();
        }
        return prop.getCreateTime();
    }



    public List<Media> getMediaList() {
        return mediaList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isEncryption() {
        return false;
    }

    public Prop getProp() {
        loadProp();
        return prop;
    }

    public void setProp(Prop prop) {
        this.prop = prop;
    }

    public Media getCoverMedia() {
        if(coverMedia == null){
            if(mediaList != null && mediaList.size() > 0){
                return mediaList.get(0);
            }
        }
        return coverMedia;
    }

    public int getPhotosCounts (){
        if(mediaList == null){
            return 0;
        }
        return mediaList.size();
    }

    public void setCoverMedia(Media coverMedia) {
        this.coverMedia = coverMedia;
    }

    public void setMediaList(List<Media> mediaList) {
        this.mediaList = mediaList;
    }

    public static interface PhotoAddListener{
        void onComplete();
        void onPhotoAdded(Media media,int index,float percent);
    }

    public void addMedia(Media media){
        mediaList.add(media);
    }

    public void addMedia(int index, Media media){
        mediaList.add(index, media);
    }

    public void removeMedia(Media media){
        mediaList.remove(media);
    }
    public void addPhotos(List<Media> medias){
        mediaList.addAll(medias);
    }

    public abstract void addPhotoToAlbum(Media media, PhotoAddListener photoAddListener, int transMode);

    public abstract void addPhotoToAlbum(int index, Media media, PhotoAddListener photoAddListener, int transMode);

    public abstract void addPhotosToAlbum(List<Media> medias, PhotoAddListener photoAddListener, int transMode);

    public String getAlbumsDir(){
        String albumsDir = (PathUtils.getPublicAlbumsSavePath());
        return albumsDir;
    }
    public String getAlbumDirPath(){
        String albumDirPath = getAlbumsDir();
        albumDirPath += File.separator + getName();
        return albumDirPath;
    }



    public String whatPhotoPathUnderAlbum(Media media){
        String photoFilePath = getAlbumDirPath() + File.separator + media.getFile().getName();
        return photoFilePath;
    }

    public String whatMediaTempPathUnderAlbum(Media media){
        String mediaPathDir = whatMediaTempPath();
        PathUtils.checkPath(mediaPathDir);
        String mediaPath = mediaPathDir + File.separator + media.getFile().getName();
        return mediaPath;
    }

    public String whatMediaTempPath(){
        String mediaPathDir = PathUtils.getPublicTempPath() + File.separator + getName();
        return mediaPathDir;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null){
            return false;
        }
        if(!(obj instanceof Album)){
            return super.equals(obj);
        }
        Album album = (Album) obj;
        if(TextUtils.isEmpty(album.getName())){
            return false;
        }
        if(type != album.getType()){
            return false;
        }

        if(album.getAlbumDirPath().equals(getAlbumDirPath())){
            return true;
        }
        if(album.getCreateTime() == getCreateTime()){
            return true;
        }
        return false;
    }


    public void recoverToOriginalPath(ProgressListener progressListener) throws EncryptionKeyMissMatchException {
        loadProp();
        if(prop == null){
            EasyLog.e(TAG,"prop not exist!");
            if(progressListener != null){
                progressListener.onError(-1,"prop not exist!");
            }
            return;
        }
        for(int i=0;i < prop.getFileMappings().size();i++){
            Prop.FileMapping fileMapping = prop.getFileMappings().get(i);
            if(fileMapping == null){
                EasyLog.e(TAG,"fileMapping not exist!");
                continue;
            }
            String originalPath = fileMapping.getOriginalFilePath();
            File originalFile = new File(originalPath);
            if(originalFile.exists()){
                EasyLog.i(TAG,"original file exist,not need to recover");
                continue;
            }
            for(Media media:mediaList){
                if(fileMapping.getFileName().equals(media.getFile().getName())){
                    FileUtils.cpFile(media.getFile().getAbsolutePath(),originalPath);
                    MediaUtils.notifyMediaInsert(MyAlbum.getContext(),originalFile);
                    if(progressListener != null){
                        progressListener.onProgress((float) i/(float) prop.getFileMappings().size(),media);
                    }
                    break;
                }

            }

        }
        if(progressListener != null){
            progressListener.onComplete();
        }
    }
    public void deleteAlbum(){
        FileUtils.deleteAllFileAndDir(getAlbumDirPath());
    }

    public void parseProp(){

    }

    public static class Prop implements Serializable{
        private long createTime;
        private int sort;
        private List<FileMapping> fileMappings = new ArrayList<FileMapping>();

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }


        //        public Map<String, String> getOriginalFileMapping() {
//            return originalFileMapping;
//        }
//
//        public void setOriginalFileMapping(Map<String, String> originalFileMapping) {
//            this.originalFileMapping = originalFileMapping;
//        }


        public List<FileMapping> getFileMappings() {
            return fileMappings;
        }

        public void setFileMappings(List<FileMapping> fileMappings) {
            this.fileMappings = fileMappings;
        }

        public static class FileMapping implements Serializable{
            private String fileName;
            private String originalFilePath;
            private int duration;
            private long lastModified;


            public String getFileName() {
                return fileName;
            }

            public void setFileName(String fileName) {
                this.fileName = fileName;
            }

            public String getOriginalFilePath() {
                return originalFilePath;
            }

            public void setOriginalFilePath(String originalFilePath) {
                this.originalFilePath = originalFilePath;
            }
            public int getDuration() {
                return duration;
            }

            public void setDuration(int duration) {
                this.duration = duration;
            }

            public long getLastModified() {
                return lastModified;
            }

            public void setLastModified(long lastModified) {
                this.lastModified = lastModified;
            }
        }
    }

    public String getPropFilePath(){
        return getAlbumDirPath() + File.separator + PROP_FILE_NAME;
    }

    public abstract Album loadProp();

    public abstract void saveProp();
    public void renameAlbum(String newName){
        if(name == null || name.equals(newName)){
            return;
        }
        String albumsDir = getAlbumsDir();
        new File(albumsDir,name).renameTo(new File(albumsDir,newName));
        name = newName;
        //update mediaList
        for(Media media : mediaList){
            String newPath = whatPhotoPathUnderAlbum(media);
            media.setFile(new File(newPath));
        }
    }

    public Album updatePropValue(){
        return this;
    }

    public void exchangeSort(Album album){
        int thisSort = getProp().getSort();
        int thatSort = album.getProp().getSort();
        if(thisSort == thatSort){
            EasyLog.e(TAG,"thisSort = thatSort!=>"+thatSort);
            AlbumManager.getInstance().handleBadSorts();
            return;
        }
        getProp().setSort(thatSort);
        saveProp();

        album.getProp().setSort(thisSort);
        album.saveProp();
    }

    public int getSize(){
        int totalSize = 0;
        for(Media media:mediaList){
            totalSize += media.getSize();
        }
        return totalSize;
    }

    public Prop.FileMapping getMediaFileMapping(Media media){
        for(Prop.FileMapping fileMapping:prop.getFileMappings()){
            if(fileMapping.getFileName().equals(media.getDisplayName())){
                return fileMapping;
            }
        }
        return null;
    }



    public boolean isMediaInThisAlbum(Media media){
        if(media == null || media.getFile() == null || !media.getFile().exists()){
            EasyLog.e(TAG,"media not exist!");
            return false;
        }
        if(media.getFile().getParent().equals(getAlbumDirPath())){
            return true;
        }
        return false;
    }

    public int whatMediaIndexInAlbum(Media media){
        if(mediaList == null || mediaList.size()<=0){
            return 0;
        }
        for(int i=0;i<mediaList.size();i++){
            Media albumMedia = mediaList.get(i);
            if(media.getLastModify() < albumMedia.getLastModify()){
                continue;
            }
            return i;
        }
        return mediaList.size();
    }
}
