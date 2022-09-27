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
public class AlbumPhotoTransModeViewBuilder {

    public interface TransModeChosenListener{

        void onModeChosen(int mode);
    }
    private TransModeChosenListener transModeChosenListener;

    public AlbumPhotoTransModeViewBuilder setTransModeChosenListener(TransModeChosenListener transModeChosenListener) {
        this.transModeChosenListener = transModeChosenListener;
        return this;
    }

    public Dialog buildTransModeDialogView(Context context){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        final View contentView = LayoutInflater.from(context).inflate(R.layout.view_albumphoto_transmode,null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();
        View btMove = contentView.findViewById(R.id.btMove);
        View btCopy = contentView.findViewById(R.id.btCopy);
        btMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(transModeChosenListener != null){
                    transModeChosenListener.onModeChosen(Album.TransPhotoMode.MOVE);
                }
            }
        });
        btCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if(transModeChosenListener != null){
                    transModeChosenListener.onModeChosen(Album.TransPhotoMode.COPY);
                }
            }
        });
        return dialog;
    }
}
