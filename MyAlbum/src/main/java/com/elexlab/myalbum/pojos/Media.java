package com.elexlab.myalbum.pojos;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.elexlab.myalbum.utils.MediaFormatUtils;
import com.elexlab.myalbum.utils.MediaUtils;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

import java.io.File;
import java.io.Serializable;

/**
 * Created by BruceYoung on 10/15/17.
 */
public class Media implements Serializable{
    public static final int COVER_WIDTH = 400;
    public static final int COVER_HEIGHT = 400;
    private String title;
    private String displayName;
    private int width;
    private int height;
    private int size;
    private long lastModify;
    private int duration;
    private File file;
    @PrimaryKey(AssignType.BY_MYSELF)
    private String filePath;
    public Media() {
    }

    public Media(File file) {
        setFile(file);
    }

    public Media(Media media) {
        setFile(media.getFile());
        setDisplayName(media.getDisplayName());
        setHeight(media.getHeight());
        setWidth(media.getWidth());
        setSize(media.getSize());
        setTitle(media.getTitle());
        setDuration(media.getDuration());
        setLastModify(media.getLastModify());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        if(!TextUtils.isEmpty(displayName)){
            return displayName;
        }
        if(file != null){
            return file.getName();
        }
        return null;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getLastModify() {
        return lastModify;
    }

    public void setLastModify(long lastModify) {
        this.lastModify = lastModify;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        this.filePath = file.getPath();
    }

    protected Album.Prop.FileMapping fileMapping;

    public void setFileMapping(Album.Prop.FileMapping fileMapping) {
        this.fileMapping = fileMapping;
    }

    /**
     * media info:
     * 1.Use exifInterface--best!
     * 2.Use prop--Okay(Encrypted medias use it directly)
     * 3.Use file--Last choose
     * @return
     */
    public void loadMediaInfo(){
        loadInfoByFile();
        //1.exifInterface-best
        MediaUtils.loadMediaInfo(this);
        //2.prop-okay
        if(getLastModify() <= 0){//find it from fileMapping
            if(fileMapping != null){
                if(fileMapping.getFileName().equals(getFile().getName())){
                    setDuration(fileMapping.getDuration());
                    if(fileMapping.getLastModified() >0){
                        setLastModify(fileMapping.getLastModified());
                        setDuration(fileMapping.getDuration());
                    }
                }
            }
        }
        //3.file-last choose
        if(getLastModify() <= 0){
            if(getFile() != null && getFile().exists()){
                setLastModify(getFile().lastModified());
            }
        }
    }
    protected void loadInfoByFile(){
        this.setDisplayName(this.getFile().getName());
        this.setSize((int) this.getFile().length());
        this.setTitle(this.getFile().getName());
        //media.setLastModify(media.getFile().lastModified());
        int[] widthAndHeight = MediaUtils.getImageWidthHeight(this.getFile().getPath());
        this.setWidth(widthAndHeight[0]);
        this.setHeight(widthAndHeight[1]);
    }


    public static Media autoCreateMedia(Media media){
        if(MediaFormatUtils.isVideoFileType(media.getFile().getPath())){
            Video video = new Video(media);
            return video;
        }else {
            Photo photo = new Photo(media);
            return photo;
        }
    }

    public String getOriginalPath(){
        return getFile().getAbsolutePath();
    }

    public boolean isVideo(){
        return false;
    }

    public void loadThumbnailInto(Context context, ImageView imageView) {
        Glide.with(context).load(getFile())
                .into(imageView);
    }
    public Bitmap loadBitmap(Context context, int width, int height){
        try {
            Bitmap bitmap =  Glide.with(context).asBitmap().load(getFile()).into(width,height).get();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    public void mvToByRename(String mvToPath){
        getFile().renameTo(new File(mvToPath));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null){
            return false;
        }
        if(!(obj instanceof Media)){
            return super.equals(obj);
        }
        Media media = (Media) obj;
        return media.getFile().getAbsolutePath().equals(this.file.getAbsolutePath());
    }
}
