package com.elexlab.mydisk.ui.gallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.elexlab.myalbum.pojos.Album;
import com.elexlab.mydisk.R;

/**
 * Created by BruceYoung on 10/14/17.
 */
public class AlbumPhotoDeleteModeViewBuilder {

    public interface DeleteModeChosenListener{

        void onModeChosen(int mode);
    }
    private DeleteModeChosenListener deleteModeChosenListener;

    public AlbumPhotoDeleteModeViewBuilder setDeleteModeChosenListener(DeleteModeChosenListener deleteModeChosenListener) {
        this.deleteModeChosenListener = deleteModeChosenListener;
        return this;
    }

    public Dialog buildTransModeDialogView(Context context){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        final View contentView = LayoutInflater.from(context).inflate(R.layout.view_albumphoto_deletemode,null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();
        View btNoRecover = contentView.findViewById(R.id.btNoRecover);
        View btRecover = contentView.findViewById(R.id.btRecover);
        btNoRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(deleteModeChosenListener != null){
                    deleteModeChosenListener.onModeChosen(Album.DeletePhotoMode.NO_RECOVRE);
                }
            }
        });
        btRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(deleteModeChosenListener != null){
                    deleteModeChosenListener.onModeChosen(Album.DeletePhotoMode.RECOVRE);
                }
            }
        });
        return dialog;
    }
}
