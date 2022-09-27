package com.elexlab.mydisk.ui.wiget;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.managers.MediaManager;
import com.elexlab.myalbum.managers.PasswordManager;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.CommonUtil;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.myalbum.utils.PathUtils;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudFileManager;
import com.elexlab.mydisk.cloud.CloudUtils;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.BaseActivity;
import com.elexlab.mydisk.ui.gallery.MediaInfoViewBuilder;
import com.elexlab.mydisk.ui.gallery.PhotoDetailViewBuilder;
import com.elexlab.mydisk.ui.gallery.PhotoDetailsActivity;
import com.elexlab.mydisk.ui.gallery.TimePickerViewBuilder;
import com.elexlab.mydisk.utils.EasyLog;
import com.elexlab.mydisk.utils.SocialUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class FileOperationViewBuilder {
    private final static String TAG = PhotoDetailViewBuilder.class.getSimpleName();
    private Context context;
    private Media media;

    private View.OnClickListener onClickListener;

    public FileOperationViewBuilder setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }
    public FileOperationViewBuilder(Context context) {
        this.context = context;
    }

    public FileOperationViewBuilder setMedia(Media media) {
        this.media = media;
        return this;
    }

    public View buildView(){
        View view = LayoutInflater.from(context).inflate(R.layout.view_file_operation,null);
        View llDelete = view.findViewById(R.id.llDelete);
        View llShare = view.findViewById(R.id.llShare);
        View llInfo = view.findViewById(R.id.llInfo);
        View llCloudShare = view.findViewById(R.id.llCloudShare);

        llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deletePhoto(media);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                })
                .setTitle(R.string.delete_photo_confirm_tips)
                .setMessage(R.string.delete_photo_warning_tips)
                .show();

            }
        });
        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(media instanceof EncryptedMedia){
                    Observable.create(new ObservableOnSubscribe<Media>() {
                        @Override
                        public void subscribe(ObservableEmitter<Media> e) throws Exception {
                            String password = PasswordManager.getInstance().readPassword(context);
                            String tempPath = PathUtils.getPublicTempPath();
                            tempPath += File.separator + media.getDisplayName();
                            Media decryptedMedia = EncryptionManager.getInstance().decryptMedia(password, media, tempPath, new FileEncryption.CodecListener() {
                                @Override
                                public void onProcess(final float progress) {

                                }
                            });
                            e.onNext(decryptedMedia);
                        }
                    })
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Media>() {
                        @Override
                        public void accept(Media media) throws Exception {
                            SocialUtils.shareMediaToMultiPlatform(context,media);
                        }
                    });
                }else{
                    SocialUtils.shareMediaToMultiPlatform(context,media);
                }

            }
        });
        llInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaInfoViewBuilder mediaInfoViewBuilder = new MediaInfoViewBuilder();
                mediaInfoViewBuilder
                        .buildView(context,media)
                        .show();

            }
        });

        llCloudShare.setOnClickListener((View v)->{


            FileInfo fileInfo = FileInfo.media2FileInfo(media);
            if(!CloudFileManager.getInstance().fileInCloud(fileInfo)){
                Toast.makeText(context,"本文件尚未备份到云端，无法创建云端分享连接",Toast.LENGTH_SHORT).show();

                return;
            }

            new TimePickerViewBuilder().buildView(context, new TimePickerViewBuilder.OnPickListener() {
                @Override
                public void onConfirmed(int day, int hour, int min) {
                    long allSec = min*60 + hour*60*60 + day*60*60*24;

                    String shareUrl = Client.getInstance().tempUrl(CloudUtils.fileInfo2Key(fileInfo),allSec);
                    EasyLog.d(TAG,"cloud share url:"+ shareUrl);
                    ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData mClipData = ClipData.newPlainText("ShareUrl", shareUrl);
                    cm.setPrimaryClip(mClipData);
                    Toast.makeText(context,"分享连接已复制到剪切板",Toast.LENGTH_SHORT).show();

                }
            }).show();


        });
        return view;
    }

    private void deletePhoto(Media media){
        boolean success = MediaManager.getInstance().deleteMedia(media, context);//FileUtils.deleteFileWithMedia(media.getFile(),PhotoDetailsActivity.this);//media.getFile().delete();
        if(success){
            Toast.makeText(context,
                    context.getResources().getString(R.string.delete_photo_success_tips),Toast.LENGTH_SHORT).show();
            ObserverManager.getInstance().notify(EventType.PHOTO_DELETED,0,0, media);


        }else{
            Toast.makeText(context,
                    context.getResources().getString(R.string.delete_photo_fail_tips),Toast.LENGTH_SHORT).show();
        }
    }
}
