package com.elexlab.mydisk.ui.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.managers.PasswordManager;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.CommonUtil;
import com.elexlab.myalbum.utils.DeviceUtils;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.myalbum.utils.PathUtils;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudFileManager;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.HWCloudDataSource;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.BaseActivity;
import com.elexlab.mydisk.ui.wiget.RecyclerViewHeaderAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/3/17.
 */
public class AlbumDetailsAdapter extends RecyclerViewHeaderAdapter<RecyclerView.ViewHolder> {
    private Context context;
    private Album album;
    private boolean isOperationMode = false;
    private List<Media> choosenMedias = new ArrayList<Media>();

    public AlbumDetailsAdapter(Context context, Album album) {
        super(context);
        this.context = context;
        this.album = album;
    }

    public interface ActionListener{
        void onOperationModeChanged(boolean isOperationMode);
        void onMediaSelected(Media media);
        void onMediaUnSelected(Media media);
    }

    private ActionListener actionListener;

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public void resetAlbum(Album album){
        this.album = album;
        notifyDataSetChanged();
    }

    public void addPhoto(Media media){
        this.album.getMediaList().add(media);
        //notifyItemInserted(album.getMediaList().size());
        notifyDataSetChanged();
    }


    public void addPhoto(int index,Media media){
        this.album.getMediaList().add(index, media);
        notifyItemInserted(index+1);
        //notifyDataSetChanged();
    }
//    public void resetPhotoList(List<Media> mediaList){
//        this.album.setMediaList(mediaList);
//        notifyDataSetChanged();
//    }

    @Override
    public RecyclerView.ViewHolder onCreateContentView(ViewGroup parent, int viewType) {
        PhotoViewHolder photoViewHolder = new PhotoViewHolder(LayoutInflater.from(
                context).inflate(R.layout.item_photo, parent,
                false));
        return photoViewHolder;
    }

//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        int viewType = getItemViewType(position);
//        if(viewType == INT_TYPE_HEADER){
//            return;
//        }
//
//    }

    @Override
    public void onBindView(RecyclerView.ViewHolder view, final int position) {
        final Media media = album.getMediaList().get(position);
        PhotoViewHolder viewHolder = (PhotoViewHolder) view;
       // Glide.with(context).load(media.getFile()).into(viewHolder.ivPhoto);
        if(media.getFile().exists()){
            media.loadThumbnailInto(context,viewHolder.ivPhoto);
        }else{//cloud
            String cloudUrl = FileInfo.media2FileInfo(media).getUrl();
            Glide.with(context)
                    .load(cloudUrl)
                    .centerCrop()
                    .into(viewHolder.ivPhoto);

        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoDetailsActivity.startActivity(context, album, position);

            }
        });
        if(media.isVideo()){
            viewHolder.llPlay.setVisibility(View.VISIBLE);
            viewHolder.llPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(media.getFile().exists()){
                        CommonUtil.playVideo(context,media.getFile().getAbsolutePath());
                    }else{
                        CommonUtil.playVideo(context, FileInfo.media2FileInfo(media).getUrl());

                    }
                }
            });
            viewHolder.tvDuration.setText(FormatUtils.parseTimeToDuration(media.getDuration()));
        }else{
            viewHolder.llPlay.setVisibility(View.GONE);
        }

        if(getItemViewType(position) == RecyclerViewHeaderAdapter.INT_TYPE_HEADER && viewHolder.itemView.getLayoutParams() instanceof StaggeredGridLayoutManager.LayoutParams){
            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) viewHolder.itemView.getLayoutParams();
            layoutParams.setFullSpan(true);
            int width = DeviceUtils.getScreenWidth(context);

            int itemWidth = (width - DeviceUtils.dipToPx(getContext(), 10)) / 2;


            ViewGroup.LayoutParams lp= viewHolder.ivPhoto.getLayoutParams();
            lp.width = itemWidth;//media.get.get(arg1);
            lp.height = media.getHeight() * itemWidth / media.getWidth();
            viewHolder.ivPhoto.setLayoutParams(lp);
        }

        if(isOperationMode){
            viewHolder.cbChoosed.setVisibility(View.VISIBLE);
            //scale animation
            ScaleAnimation scaleAnimation =
                    new ScaleAnimation(1.f, 0.8f, 1.0f, 0.8f,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                            ScaleAnimation.RELATIVE_TO_SELF, 0.5f);

            scaleAnimation.setDuration(100);
            scaleAnimation.setRepeatCount(1);
            scaleAnimation.setRepeatMode(Animation.REVERSE);
            scaleAnimation.setFillAfter(false);
            viewHolder.itemView.startAnimation(scaleAnimation);
        }else{
            viewHolder.cbChoosed.setVisibility(View.GONE);
        }
        viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isOperationMode = true;
                notifyDataSetChanged();
                if(actionListener != null){
                    actionListener.onOperationModeChanged(isOperationMode);
                    DeviceUtils.vibrate(context,100);
                }
                return false;
            }
        });
        viewHolder.cbChoosed.setOnCheckedChangeListener(null);
        viewHolder.cbChoosed.setChecked(choosenMedias.contains(media));

        viewHolder.cbChoosed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    choosenMedias.add(media);
                    if(actionListener != null){
                        actionListener.onMediaSelected(media);
                    }
                }else{
                    choosenMedias.remove(media);
                    if(actionListener != null){
                        actionListener.onMediaUnSelected(media);
                    }
                }
            }
        });

        //Cloud storage
        FileInfo fileInfo = FileInfo.media2FileInfo(media);
        if(CloudFileManager.getInstance().fileInCloud(fileInfo)){
            viewHolder.rlCloud.setVisibility(View.GONE);
        }else{
            viewHolder.rlCloud.setVisibility(View.VISIBLE);

        }
        viewHolder.rlCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.ivCloud.setImageResource(R.drawable.ic_sync);
                viewHolder.ivCloud.setImageTintList(ColorStateList.valueOf(context.getColor(R.color.colorPrimaryDark)));
                final Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_anim);
                LinearInterpolator lin = new LinearInterpolator();
                operatingAnim.setInterpolator(lin);
                viewHolder.ivCloud.startAnimation(operatingAnim);
                new HWCloudDataSource().addData(fileInfo, new DataSourceCallback<FileInfo>() {
                    @Override
                    public void onSuccess(FileInfo fileInfo, String... extraParams) {

                        ThreadManager.getInstance().getMainHandler().post(()->{
                            viewHolder.ivCloud.clearAnimation();
                            notifyDataSetChanged();

                        });

                    }

                    @Override
                    public void onFailure(String errMsg, int code) {

                    }
                });

            }
        });
    }


    @Override
    public int getCount() {
        return (album == null || album.getMediaList() == null)?0:album.getMediaList().size();
    }



    private class PhotoViewHolder extends RecyclerView.ViewHolder{
        private ImageView ivPhoto;
        private ImageView ivPlay;
        private View llPlay;
        private TextView tvDuration;
        private CheckBox cbChoosed;
        private View rlCloud;
        private ImageView ivCloud;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = (ImageView) itemView.findViewById(R.id.ivPhoto);
            llPlay = itemView.findViewById(R.id.llPlay);
            tvDuration = (TextView) itemView.findViewById(R.id.tvDuration);
            cbChoosed = (CheckBox) itemView.findViewById(R.id.cbChoosed);
            rlCloud = itemView.findViewById(R.id.rlCloud);
            ivCloud = (ImageView) itemView.findViewById(R.id.ivCloud);

        }
    }

    public List<Media> getChoosenMedias() {
        return choosenMedias;
    }

    public boolean isOperationMode() {
        return isOperationMode;
    }

    public void setOperationMode(boolean operationMode) {
        isOperationMode = operationMode;
        if(! isOperationMode){
            choosenMedias.clear();
        }
        notifyDataSetChanged();
        if(actionListener != null){
            actionListener.onOperationModeChanged(operationMode);
        }
    }
}
