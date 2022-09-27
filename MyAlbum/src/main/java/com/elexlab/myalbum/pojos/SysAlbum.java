package com.elexlab.myalbum.pojos;

import java.util.List;

/**
 * Created by BruceYoung on 10/13/17.
 */
public class SysAlbum extends Album{
    private String albumDirPath;
    public SysAlbum() {
    }

    public SysAlbum(String name) {
        super(name, AlbumType.SYSTEM);
    }

    @Override
    public void addPhotoToAlbum(Media media, PhotoAddListener photoAddListener, int mode) {
        throw new UnsupportedOperationException("can not add media into with SysAlbum!");

    }

    @Override
    public void addPhotoToAlbum(int index, Media media, PhotoAddListener photoAddListener, int mode) {
        throw new UnsupportedOperationException("can not add media into with SysAlbum!");

    }

    @Override
    public void addPhotosToAlbum(List<Media> medias, PhotoAddListener photoAddListener, int mode) {
        throw new UnsupportedOperationException("can not add medias into with SysAlbum!");
    }

    @Override
    public Album loadProp() {
        if(prop != null){
            return this;
        }
        prop = new Prop();
        prop.setCreateTime(getCreateTime());
        prop.setSort(0);
        return this;
    }

    @Override
    public void saveProp() {

    }

    public void setAlbumDirPath(String albumDirPath) {
        this.albumDirPath = albumDirPath;
    }

    public String getAlbumDirPath(){
        return albumDirPath;
    }
}
