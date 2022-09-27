package com.elexlab.mydisk.ui.files;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudUtils;
import com.elexlab.mydisk.manager.FileSystemManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.gallery.TimePickerViewBuilder;
import com.elexlab.mydisk.ui.home.FileListFragment;
import com.elexlab.mydisk.ui.misc.RecyclerItemData;
import com.elexlab.mydisk.utils.FileOpenUtils;
import com.elexlab.mydisk.utils.FragmentUtils;
import com.elexlab.mydisk.utils.EasyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/3/17.
 */
public class FilesBrowserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String TAG = FilesBrowserAdapter.class.getSimpleName();
    //private DistAlbums distAlbums;
    private Context context;
    private FileListFragment fragment;

    private Handler handler = new Handler();

    private List<FileInfo> fileInfos;
    private List<RecyclerItemData<FileInfo>> recyclerItems;

    public FilesBrowserAdapter (FileListFragment fragment) {
        //this.distAlbums = distAlbums;
        this.fragment = fragment;
        this.context = fragment.getContext();
    }
    public FilesBrowserAdapter (FileListFragment fragment,List<FileInfo> fileInfos) {
        //this.distAlbums = distAlbums;
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.fileInfos = fileInfos;
    }
    public void resetFileList(List<FileInfo> fileInfos){
        this.fileInfos = fileInfos;
        notifyDataSetChanged();
    }


    private int myAlbumFirstIndex = -1;
    private int systemAlbumFirstIndex = -1;

    public int getSystemAlbumFirstIndex() {
        return systemAlbumFirstIndex;
    }

    public int getMyAlbumFirstIndex() {
        return myAlbumFirstIndex;
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewHolder viewHolder;
        switch (viewType){
            case FileInfo.StoreLocation.LOCAL_MIRROR:{
                viewHolder = new LocalMirrorViewHolder(LayoutInflater.from(
                        context).inflate(R.layout.item_file_local_mirror, parent,
                        false));
                return viewHolder;
            }
            case FileInfo.StoreLocation.MIRROR:{
                viewHolder = new MirrorViewHolder(LayoutInflater.from(
                        context).inflate(R.layout.item_file_mirror, parent,
                        false));
                return viewHolder;
            }
            case FileInfo.StoreLocation.LOCAL:{
                viewHolder = new LocalViewHolder(LayoutInflater.from(
                        context).inflate(R.layout.item_file_local, parent,
                        false));
                return viewHolder;
            }
            case FileInfo.StoreLocation.LOCAL_MIRROR_RECOVERY:{
                viewHolder = new ViewHolder(LayoutInflater.from(
                        context).inflate(R.layout.item_file_recovery, parent,
                        false));
                return viewHolder;
            }
            default:{
                viewHolder = new ViewHolder(LayoutInflater.from(
                        context).inflate(R.layout.item_file_local_mirror, parent,
                        false));
                return viewHolder;
            }
        }


    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        final FileInfo fileInfo = fileInfos.get(position);
        final ViewHolder viewHolder = (ViewHolder) holder;
        final int viewType = getItemViewType(position);

        viewHolder.ivPlay.setVisibility(View.GONE);
        viewHolder.tvFileName.setText(fileInfo.getName());

        View.OnClickListener onClickListener = null;

        setIcon(fileInfo,viewHolder);

        setOnclick(position,fileInfo,viewHolder);

        if(viewType == FileInfo.StoreLocation.MIRROR||
                viewType == FileInfo.StoreLocation.LOCAL_MIRROR){
            setCloudShare(fileInfo, (LocalMirrorViewHolder) viewHolder);
        }

        if(getItemViewType(position) == FileInfo.StoreLocation.LOCAL_MIRROR_RECOVERY){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fileInfo.setStoreLocation(FileInfo.StoreLocation.LOCAL_MIRROR);
                    notifyItemChanged(position);
                }
            },1500);
        }
    }

    private void setIcon(FileInfo fileInfo, ViewHolder viewHolder){
        if(fileInfo.isDir()){
            viewHolder.ivIcon.setImageResource(R.drawable.ic_folder);
        }else {
            viewHolder.ivIcon.setImageResource(R.drawable.ic_file);
            String filePath = fileInfo.getPath();
            if(fileInfo.getStoreLocation() == FileInfo.StoreLocation.MIRROR){
                filePath = fileInfo.getUrl();
            }
            if(FileOpenUtils.isImage(fileInfo.getName())){
                Glide.with(context)
                        .load(filePath)
                        .centerCrop()
                        .into(viewHolder.ivIcon);
            }
            if(FileOpenUtils.isVideo(fileInfo.getName())){
                Glide.with(context)
                        .load(filePath)
                        .centerCrop()
                        .into(viewHolder.ivIcon);
                viewHolder.ivPlay.setVisibility(View.VISIBLE);

            }
        }
    }

    private void setCloudShare(FileInfo fileInfo, LocalMirrorViewHolder mirrorViewHolder){
        if(fileInfo.isDir()){
            mirrorViewHolder.llCloudShare.setVisibility(View.INVISIBLE);
        }else {
            mirrorViewHolder.llCloudShare.setVisibility(View.VISIBLE);

        }
        mirrorViewHolder.llCloudShare.setOnClickListener(v -> {
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
    }

    private void setOnclick(int position,FileInfo fileInfo, ViewHolder viewHolder){
        int viewType = getItemViewType(position);
        View.OnClickListener onClickListener = null;
        if(FileInfo.FileType.DIR.equals(fileInfo.getFileType())){
            onClickListener = v -> {
                FileListFragment fileListFragment = new FileListFragment();
                Bundle args = new Bundle();
                String dir = fileInfo.getPath();
                args.putString("dir", dir);
                fileListFragment.setArguments(args);
                FragmentUtils.switchFragment(fragment.getActivity(),R.id.flContainer,fragment,fileListFragment,true);
            };

        }else{
            if(viewType == FileInfo.StoreLocation.LOCAL_MIRROR||
                    viewType == FileInfo.StoreLocation.LOCAL_MIRROR_RECOVERY||
                    viewType == FileInfo.StoreLocation.LOCAL){

                onClickListener = v -> FileOpenUtils.openFile(context, fileInfo.getPath());

            }else if(viewType == FileInfo.StoreLocation.MIRROR){

                onClickListener = v -> { FileOpenUtils.openUrl(context, fileInfo); };

            }

            if(viewType == FileInfo.StoreLocation.LOCAL){

                final LocalViewHolder localViewHolder = (LocalViewHolder) viewHolder;
                localViewHolder.llLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        final Animation operatingAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_anim);
                        LinearInterpolator lin = new LinearInterpolator();
                        operatingAnim.setInterpolator(lin);
                        localViewHolder.ivLocation.startAnimation(operatingAnim);
                        FileSystemManager.getInstance().uploadFile(fileInfo, new FileSystemManager.FileActionListener() {
                            @Override
                            public void onCompletion(final FileInfo fileInfo,String msg) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        localViewHolder.ivLocation.clearAnimation();
                                        fileInfo.setStoreLocation(FileInfo.StoreLocation.LOCAL_MIRROR);
                                        notifyItemChanged(position);
                                    }
                                });

                            }

                            @Override
                            public void onError(FileInfo fileInfo,final String msg) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        localViewHolder.ivLocation.clearAnimation();
                                        Toast.makeText(fragment.getContext(),"同步发生错误:"+msg,Toast.LENGTH_LONG).show();

                                    }
                                });
                            }
                        });
                    }
                });
            }
        }

        viewHolder.itemView.setOnClickListener(onClickListener);

    }

    @Override
    public int getItemViewType(int position) {
        return fileInfos.get(position).getStoreLocation();
    }

    @Override
    public int getItemCount() {
        return fileInfos == null?0:fileInfos.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView ivIcon;
        public View ivPlay;
        public TextView tvFileName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = (ImageView) itemView.findViewById(R.id.ivIcon);
            tvFileName = (TextView) itemView.findViewById(R.id.tvFileName);
            ivPlay = itemView.findViewById(R.id.ivPlay);

        }
    }
    private class MirrorViewHolder extends LocalMirrorViewHolder{

        public ProgressBar pbRecovering;
        public View llCloudShare;
        public View ivCloudOnly;

        public MirrorViewHolder(@NonNull View itemView) {
            super(itemView);
            pbRecovering = itemView.findViewById(R.id.pbRecovering);
            llCloudShare = itemView.findViewById(R.id.llCloudShare);
            ivCloudOnly = itemView.findViewById(R.id.ivCloudOnly);
        }
    }

    private class LocalMirrorViewHolder extends ViewHolder{

        public ProgressBar pbRecovering;
        public View llCloudShare;

        public LocalMirrorViewHolder(@NonNull View itemView) {
            super(itemView);
            pbRecovering = itemView.findViewById(R.id.pbRecovering);
            llCloudShare = itemView.findViewById(R.id.llCloudShare);
        }
    }
    private class LocalViewHolder extends ViewHolder{
        public ImageView ivLocation;
        public View llLocation;
        public LocalViewHolder(@NonNull View itemView) {
            super(itemView);
            ivLocation = itemView.findViewById(R.id.ivLocation);
            llLocation = itemView.findViewById(R.id.llLocation);
        }
    }
}
