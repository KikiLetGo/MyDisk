package com.elexlab.mydisk.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;


import androidx.core.content.FileProvider;

import com.elexlab.myalbum.pojos.Media;
import com.elexlab.mydisk.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/21/17.
 */
public class SocialUtils {

    public static void goToFacebookPersonalHomePage(Context context,String homePageUrl){
        Intent intent = newFacebookIntent(context,homePageUrl);
        context.startActivity(intent);
    }

    public static Intent newFacebookIntent(Context context, String url) {
        PackageManager packageManager = context.getPackageManager();
        Uri uri = null;//Uri.parse(url);
        if(CommonUtil.isAppInstalled(context,"com.facebook.katana")){
            try {
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0);
                if (applicationInfo.enabled) {
                    uri = Uri.parse("fb://facewebmodal/f?href=" + url);
                }
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }else{
            uri = Uri.parse(url);
        }

        return new Intent(Intent.ACTION_VIEW, uri);
    }

    public static void shareMediaToMultiPlatform(Context context, Media media){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        if(media.isVideo()){
            intent.setType("video/*");

        }else{
            intent.setType("image/*");

        }

        Uri photoUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                media.getFile());
        intent.putExtra(Intent.EXTRA_STREAM, photoUri);
        //context.startActivity(intent);
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_to)));
    }

    public static void shareMediaToMultiPlatform(Context context, List<Media> mediaList){
        if(mediaList.isEmpty()){
            return;
        }
        if(mediaList.size() == 1){
            shareMediaToMultiPlatform(context,mediaList.get(0));
            return;
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);

        intent.setType("image/*");

        ArrayList<Uri> imageUris = new ArrayList<Uri>();
        for (Media media : mediaList) {
            imageUris.add(Uri.fromFile(media.getFile()));
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
        //context.startActivity(intent);
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.share_to)));
    }

    public static void jumpToMarket(Context context,String pkgName){
        Uri uri = Uri.parse("market://details?id=" + pkgName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try { context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
}
