package com.elexlab.myalbum.pojos;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.elexlab.myalbum.R;

/**
 * Created by BruceYoung on 10/15/17.
 */
public class Video extends Media{
    public Video() {
    }

    public Video(Media media) {
        super(media);
    }


    @Override
    public boolean isVideo(){
        return true;
    }

    @Override
    public void loadThumbnailInto(Context context, ImageView imageView) {
        Glide.with(context)
                .load(getFile())
                //.placeholder(R.mipmap.default_image_holder)
                .into(imageView);
    }

}
