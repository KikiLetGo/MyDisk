package com.elexlab.myalbum.pojos;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bruceyoung on 17-10-11.
 */

public class DataAlbum extends Album{

    @Override
    public void addPhotoToAlbum(Media media, PhotoAddListener photoAddListener, int mode) {
        throw new UnsupportedOperationException("can not add media into with DataAlbum!");

    }

    @Override
    public void addPhotoToAlbum(int index, Media media, PhotoAddListener photoAddListener, int mode) {
        throw new UnsupportedOperationException("can not add media into with DataAlbum!");

    }

    @Override
    public void addPhotosToAlbum(List<Media> medias, PhotoAddListener photoAddListener, int mode) {
        throw new UnsupportedOperationException("can not add medias into with DataAlbum!");
    }

    @Override
    public Album loadProp() {
        return this;
    }

    @Override
    public void saveProp() {

    }

    @Override
    public Album updatePropValue() {
        prop = new Prop();
        Map<String,String> originalPathMapping = new HashMap<>();
        List<Prop.FileMapping> fileMappings = new ArrayList<Prop.FileMapping>();
        for (Media media : getMediaList()){
            File file = media.getFile();
            String fileName =file.getName();
            String originalPath = file.getAbsolutePath();
            originalPathMapping.put(fileName,originalPath);
            Prop.FileMapping fileMapping = new Prop.FileMapping();
            fileMapping.setFileName(fileName);
            fileMapping.setOriginalFilePath(file.getAbsolutePath());
            fileMappings.add(fileMapping);
        }
        prop.setFileMappings(fileMappings);
        prop.setCreateTime(getCreateTime());
        setProp(prop);
        return this;
    }
}
