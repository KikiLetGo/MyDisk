package com.elexlab.mydisk.ui.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;


import com.elexlab.myalbum.managers.MediaManager;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.mydisk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 11/17/17.
 */
public class MediaDeleteDialogViewBuilder {
    public interface DeleteResultListener{
        void onMediaDelete(Media media);
        void onMediasDeleteFinish();
    }

    private List<Media> medias = new ArrayList<Media>();

    private DeleteResultListener deleteResultListener;

    public MediaDeleteDialogViewBuilder setDeleteResultListener(DeleteResultListener deleteResultListener) {
        this.deleteResultListener = deleteResultListener;
        return this;
    }

    public MediaDeleteDialogViewBuilder appendMedias(List<Media> medias){
        this.medias.addAll(medias);
        return this;
    }

    public MediaDeleteDialogViewBuilder appendMedia(Media media){
        this.medias.add(media);
        return this;
    }
    public AlertDialog buildView(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                deleteMedias(context);
            }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        })
        .setTitle(R.string.delete_photo_confirm_tips)
        .setMessage(R.string.delete_photo_warning_tips);
        AlertDialog alertDialog = builder.create();
        return alertDialog;
    }

    private void deleteMedias(Context context){
        if(medias.isEmpty()){
            return;
        }

        boolean success = true;
        for(Media media:medias){
            try {
                MediaManager.getInstance().deleteMedia(media,context);
                ObserverManager.getInstance().notify(EventType.PHOTO_DELETED,0,0, media);
            }catch (Exception e){
                e.printStackTrace();
                success = false;
            }
        }
        if(success){
            Toast.makeText(context,
                    context.getResources().getString(R.string.delete_photo_success_tips),Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(context,
                    context.getResources().getString(R.string.delete_photo_fail_tips),Toast.LENGTH_SHORT).show();
        }
        if(deleteResultListener != null){
            deleteResultListener.onMediasDeleteFinish();
        }
    }

}
