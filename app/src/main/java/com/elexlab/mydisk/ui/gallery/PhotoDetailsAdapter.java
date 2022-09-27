package com.elexlab.mydisk.ui.gallery;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;


import androidx.viewpager.widget.PagerAdapter;

import com.elexlab.myalbum.pojos.Media;

import java.util.List;

/**
 * Created by BruceYoung on 10/7/17.
 */
public class PhotoDetailsAdapter extends PagerAdapter {
    private List<Media> mediaList;
    private Context context;

    public PhotoDetailsAdapter(List<Media> mediaList, Context context) {
        this.mediaList = mediaList;
        this.context = context;
    }

    @Override
    public void destroyItem(ViewGroup container, int arg1, Object object) {
        ((ViewGroup) container).removeView((View) object);
    }

    @Override
    public int getCount() {
        return mediaList == null?0: mediaList.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Media media = mediaList.get(position);
        View photoDetailsView = new PhotoDetailViewBuilder(context)
                .setMedia(media)
                .setOnClickListener(onClickListener)
                .buildView();
        container.addView(photoDetailsView);
        return photoDetailsView;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    boolean isDataChanged = false;
    @Override
    public void notifyDataSetChanged() {
        isDataChanged = true;
        super.notifyDataSetChanged();
    }
    @Override
    public int getItemPosition(Object object)   {
        if (isDataChanged) {
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }

    private View.OnClickListener onClickListener;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
