package com.elexlab.mydisk.ui.gallery;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.mydisk.R;


/**
 * Created by BruceYoung on 10/18/17.
 */
public class MediaInfoViewBuilder {
    public Dialog buildView(Context context, Media media){
        AlertDialog.Builder builder =new AlertDialog.Builder(context);
        final View contentView = LayoutInflater.from(context).inflate(R.layout.view_media_info,null);
        builder.setView(contentView);
        final Dialog dialog = builder.create();
        TextView tvName = (TextView) contentView.findViewById(R.id.tvName);
        TextView tvSize = (TextView) contentView.findViewById(R.id.tvSize);
        TextView tvResolution = (TextView) contentView.findViewById(R.id.tvResolution);
        TextView tvLocation = (TextView) contentView.findViewById(R.id.tvLocation);
        TextView tvPath = (TextView) contentView.findViewById(R.id.tvPath);
        TextView tvTime = (TextView) contentView.findViewById(R.id.tvTime);

        tvName.setText(media.getDisplayName());
        tvSize.setText(FormatUtils.bToMB(media.getSize())+ "MB");
        tvPath.setText(media.getFile().getPath());
        tvTime.setText(FormatUtils.timeStampToFormatStr("MM/dd/yyyy HH:mm:ss",media.getLastModify()));
        tvResolution.setText(media.getWidth()+"x"+media.getHeight());

//        ExifInterface exifInterface = null;
//        try {
//            exifInterface = new ExifInterface(media.getFile().getAbsolutePath());
//            String datetime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);// 拍摄时间
//            String deviceName = exifInterface.getAttribute(ExifInterface.TAG_MAKE);// 设备品牌
//            String deviceModel = exifInterface.getAttribute(ExifInterface.TAG_MODEL); // 设备型号
//            String latValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
//            String lngValue = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
//            String latRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
//            String lngRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
//            tvLocation.setText("["+latValue+","+lngValue+"]");
//            tvTime.setText(datetime);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return dialog;
    }

}
