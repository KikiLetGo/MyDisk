package com.elexlab.mydisk.ui.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.managers.PasswordManager;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.CommonUtil;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.myalbum.utils.PathUtils;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.BaseActivity;
import com.elexlab.mydisk.utils.EasyLog;
import com.elexlab.mydisk.utils.FileOpenUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/7/17.
 */
public class PhotoDetailViewBuilder {
    private final static String TAG = PhotoDetailViewBuilder.class.getSimpleName();
    private Context context;
    private Media media;

    private View.OnClickListener onClickListener;

    public PhotoDetailViewBuilder setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }
    public PhotoDetailViewBuilder(Context context) {
        this.context = context;
    }

    public PhotoDetailViewBuilder setMedia(Media media) {
        this.media = media;
        return this;
    }

    public View buildView(){
        View view = LayoutInflater.from(context).inflate(R.layout.view_photo_details,null);
        ImageView pivPhoto = (ImageView) view.findViewById(R.id.pivPhoto);
        ImageView ivVideoPlay = (ImageView) view.findViewById(R.id.ivVideoPlay);
        //Glide.with(context).load(media.getFile()).into(pivPhoto);
        EasyLog.d(TAG,"load loadThumbnailInto");
        if(media.getFile().exists()){
            media.loadThumbnailInto(context,pivPhoto);

        }else{
            String cloudUrl = FileInfo.media2FileInfo(media).getUrl();
            Glide.with(context)
                    .load(cloudUrl)
                    .centerCrop()
                    .into(pivPhoto);
        }

//        Glide
//                .with(context)
//                .load(media.getFile())
//                .dontAnimate()
//                .dontTransform()
//                .into(pivPhoto);
        pivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyLog.d(PhotoDetailViewBuilder.class.getSimpleName(),"onClick");
                if(onClickListener != null){
                    onClickListener.onClick(v);
                }
            }
        });

        if(media.isVideo()){
            ivVideoPlay.setVisibility(View.VISIBLE);
            ivVideoPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(media.isVideo()) {

                        if(media instanceof EncryptedMedia){
                            //VideoPlayActivity.startActivity(context,media.getFile().getAbsolutePath());
                            ((BaseActivity)context).showProgress(R.string.decrypting_media,true);
                            Observable.create(new ObservableOnSubscribe<Media>(){
                                @Override
                                public void subscribe(ObservableEmitter<Media> e) throws Exception {
                                    try {
                                        String password = PasswordManager.getInstance().readPassword(context);
                                        String tempPath = PathUtils.getPublicTempPath();
                                        tempPath += File.separator + media.getDisplayName();
                                        Media decryptMedia = EncryptionManager.getInstance().decryptMedia(password,media,tempPath,new FileEncryption.CodecListener() {
                                            @Override
                                            public void onProcess(final float progress) {
                                                ThreadManager.getInstance().getMainHandler().post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((BaseActivity)context).resetProgress(FormatUtils.floatToPercent(progress*100));
                                                    }
                                                });
                                            }
                                        });
                                        e.onNext(decryptMedia);
                                    } catch (EncryptionKeyMissMatchException exception) {
                                        e.onError(exception);
                                    }
                                }
                            })
                            .subscribeOn(Schedulers.computation())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Consumer<Media>() {
                                @Override
                                public void accept(Media media) throws Exception {
                                    ((BaseActivity)context).stopProgress();
                                    CommonUtil.playVideo(context,media.getFile().getAbsolutePath());
                                }
                            });


                        }else{
                            if(media.getFile().exists()){
                                CommonUtil.playVideo(context,media.getFile().getAbsolutePath());
                            }else{
                                CommonUtil.playVideo(context, FileInfo.media2FileInfo(media).getUrl());

                            }

                        }
                    }
                }
            });
        }else{
            ivVideoPlay.setVisibility(View.GONE);
        }
        return view;
    }
}
