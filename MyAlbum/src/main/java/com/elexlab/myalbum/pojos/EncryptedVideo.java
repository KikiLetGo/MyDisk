package com.elexlab.myalbum.pojos;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elexlab.myalbum.R;

import java.io.File;

/**
 * Created by BruceYoung on 10/15/17.
 */
public class EncryptedVideo extends EncryptedMedia {
    private File coverFile;

    public EncryptedVideo() {
    }

    public EncryptedVideo(Media media) {
        super(media);
    }

    public File getCoverFile() {
        return coverFile;
    }

    public void setCoverFile(File coverFile) {
        this.coverFile = coverFile;
    }

    @Override
    public void loadThumbnailInto(Context context, ImageView imageView) {
        Glide.with(context)
                .load(coverFile)
                //.placeholder(R.mipmap.default_image_holder)
                .into(imageView);
    }

    @Override
    public Bitmap loadBitmap(Context context, int width, int height){
        try {
            Bitmap bitmap =  Glide.with(context).asBitmap().load(coverFile).into(width,height).get();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isVideo() {
        return true;
    }
}
