package com.elexlab.mydisk.ui.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import com.elexlab.myalbum.encryption.FileEncryption;
import com.elexlab.myalbum.managers.EncryptionManager;
import com.elexlab.myalbum.managers.MediaManager;
import com.elexlab.myalbum.managers.PasswordManager;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.myalbum.utils.PathUtils;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.ui.BaseActivity;
import com.elexlab.mydisk.ui.wiget.FileOperationViewBuilder;
import com.elexlab.mydisk.utils.SocialUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/5/17.
 */
public class PhotoDetailsActivity extends BaseActivity {
    public static void startActivity(Context context, Album album, int index){
        Intent intent = new Intent(context, PhotoDetailsActivity.class);
        intent.putExtra("index",index);
        Bundle bundle = new Bundle();
        bundle.putSerializable("album",album);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    private int currentChoosedPosition = 0;
    private Album album;
    private PhotoDetailsAdapter photoDetailsAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);

        //ImageView pivPhoto = (ImageView) findViewById(R.id.pivPhoto);
        album = (Album) getIntent().getSerializableExtra("album");
        currentChoosedPosition = getIntent().getIntExtra("index",0);
        //Glide.with(this).load(photo.getFile()).into(pivPhoto);

        final View llHeader = findViewById(R.id.llHeader);
        final View rlMenu = findViewById(R.id.rlMenu);
        rlMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do noting
            }
        });
        FileOperationViewBuilder fileOperationViewBuilder = new FileOperationViewBuilder(this);

        final ViewPager vpPhotoPager = (ViewPager) findViewById(R.id.vpPhotoPager);
        photoDetailsAdapter = new PhotoDetailsAdapter(album.getMediaList(),this);
        photoDetailsAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llHeader.setVisibility(llHeader.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);
                //rlMenu.setVisibility(llHeader.getVisibility());

                RelativeLayout rlFileOperation = findViewById(R.id.rlFileOperation);
                final Media media = album.getMediaList().get(currentChoosedPosition);
                View fileOperationView = fileOperationViewBuilder
                                        .setMedia(media)
                                        .buildView();
                fileOperationView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                rlFileOperation.addView(fileOperationView);
            }
        });
        vpPhotoPager.setAdapter(photoDetailsAdapter);
        vpPhotoPager.setCurrentItem(currentChoosedPosition);

        vpPhotoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                currentChoosedPosition = position;
                fileOperationViewBuilder.setMedia(album.getMediaList().get(currentChoosedPosition));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });



        vpPhotoPager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                llHeader.setVisibility(View.VISIBLE);
                rlMenu.setVisibility(View.VISIBLE);
            }
        });
    }

}
