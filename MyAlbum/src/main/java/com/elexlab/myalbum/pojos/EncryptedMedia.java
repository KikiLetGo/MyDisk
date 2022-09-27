package com.elexlab.myalbum.pojos;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.utils.MediaUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by bruceyoung on 17-10-10.
 */

public abstract class EncryptedMedia extends Media {
    private String originalDirPath;

    public EncryptedMedia() {
        super();

    }

    public EncryptedMedia(Media media) {
        super(media);
    }

    public String getOriginalDirPath() {
        return originalDirPath;
    }

    public void setOriginalDirPath(String originalDirPath) {
        this.originalDirPath = originalDirPath;
    }

    @Override
    public void loadThumbnailInto(Context context, ImageView imageView) {
        Glide.with(context)
                //.from(Media.class)
                //.placeholder(R.mipmap.default_image_holder)
                .load(this)
                .into(imageView);
    }



    @Override
    public Bitmap loadBitmap(Context context, int width, int height){

        try {
            Bitmap bitmap = Glide
                    .with(context)
                    //.from(Media.class)
                    .asBitmap()
                    .load(this)
                    .into(width,height)
                    .get();
            return bitmap;
//            FileInputStream inputStream = new FileInputStream(getFile());
//            final Bitmap result = BitmapFactory.decodeStream(FileEncryption.getFileEncryption().getDecryptedInputStream(inputStream));
//            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    protected void loadInfoByFile(){
        this.setDisplayName(this.getFile().getName());
        this.setSize((int) this.getFile().length());
        this.setTitle(this.getFile().getName());
        //media.setLastModify(media.getFile().lastModified());
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(getFile());
            InputStream decryptedInputStream = FileEncryption.getFileEncryption().getDecryptedInputStream(inputStream);
            int[] widthAndHeight = MediaUtils.getImageWidthHeight(decryptedInputStream);
            this.setWidth(widthAndHeight[0]);
            this.setHeight(widthAndHeight[1]);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /** media info:
     * 1.Use prop--Okay(Encrypted medias use it directly)
     * 3.Use file--Last choose
     * @return*/
    @Override
    public void loadMediaInfo(){
        loadInfoByFile();
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

}
