package com.elexlab.mydisk.ui.home;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.manager.FileSystemManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.utils.DownloadTools;
import com.elexlab.mydisk.utils.FileOpenUtils;
import com.elexlab.mydisk.utils.FragmentUtils;
import com.elexlab.mydisk.utils.EasyLog;

import java.util.List;

public class FilesAdapter extends BaseAdapter {
    private List<FileInfo> fileInfos;
    private Fragment fragment;

    public FilesAdapter(Fragment fragment) {
        this.fragment = fragment;
    }

    public FilesAdapter(List<FileInfo> fileInfos, Fragment fragment) {
        this.fileInfos = fileInfos;
        this.fragment = fragment;
    }
    public void reset(List<FileInfo> fileInfos){
        this.fileInfos = fileInfos;
        notifyDataSetChanged();
    }

    private Handler handler = new Handler();
    @Override
    public int getCount() {
        return fileInfos == null ? 0 : fileInfos.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if(convertView == null){
            convertView = LayoutInflater.from(fragment.getContext()).inflate(R.layout.item_file_local_mirror,null);
            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.ivIcon);
            viewHolder.tvFileName = (TextView) convertView.findViewById(R.id.tvFileName);
            viewHolder.ivLocation = convertView.findViewById(R.id.ivLocation);
            viewHolder.ivPlay = convertView.findViewById(R.id.ivPlay);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.ivPlay.setVisibility(View.GONE);

        final FileInfo fileInfo = fileInfos.get(i);
        viewHolder.tvFileName.setText(fileInfo.getName());
        if("dir".equals(fileInfo.getFileType())){
            viewHolder.ivIcon.setImageResource(R.drawable.ic_folder);
        }else {
            viewHolder.ivIcon.setImageResource(R.drawable.ic_file);

            if(FileOpenUtils.isImage(fileInfo.getName())){
                Glide.with(fragment.getContext())
                        .load(fileInfo.getPath())
                        .centerCrop()
                        .into(viewHolder.ivIcon);

            }
            if(FileOpenUtils.isVideo(fileInfo.getName())){
                Glide.with(fragment.getContext())
                        .load(fileInfo.getPath())
                        .centerCrop()
                        .into(viewHolder.ivIcon);
                viewHolder.ivPlay.setVisibility(View.VISIBLE);

            }
        }
        viewHolder.ivLocation.setOnClickListener(null);
        viewHolder.ivIcon.setColorFilter(null);
        viewHolder.ivLocation.setVisibility(View.VISIBLE);

        switch (fileInfo.getStoreLocation()){
            case FileInfo.StoreLocation.LOCAL:{
                viewHolder.ivLocation.setImageResource(R.drawable.ic_sync);
                final ViewHolder finalViewHolder = viewHolder;
                viewHolder.ivLocation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FileSystemManager.getInstance().uploadFile(fileInfo, new FileSystemManager.FileActionListener() {
                            @Override
                            public void onCompletion(FileInfo fileInfo,String msg) {
                                finalViewHolder.ivLocation.setImageResource(R.drawable.ic_done);

                            }

                            @Override
                            public void onError(FileInfo fileInfo,String msg) {
                                Toast.makeText(fragment.getContext(),"同步发生错误:"+msg,Toast.LENGTH_LONG).show();

                            }
                        });
                    }
                });
                break;
            }
            case FileInfo.StoreLocation.LOCAL_MIRROR:{
                viewHolder.ivLocation.setImageResource(R.drawable.ic_done);
                break;
            }
            case FileInfo.StoreLocation.MIRROR:{
                viewHolder.ivLocation.setVisibility(View.GONE);
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0.1f);
                viewHolder.ivIcon.setColorFilter(new ColorMatrixColorFilter(cm));
                break;
            }

        }

        convertView.setOnClickListener(view -> {
            if(FileInfo.FileType.DIR.equals(fileInfo.getFileType())){
                FileListFragment fileListFragment = new FileListFragment();
                Bundle args = new Bundle();
                String dir = fileInfo.getDir();
                args.putString("dir", dir);
                fileListFragment.setArguments(args);
                FragmentUtils.switchFragment(fragment.getActivity(),R.id.flContainer,fragment,fileListFragment,true);
            }else{
                if(FileInfo.StoreLocation.LOCAL == fileInfo.getStoreLocation()||
                        FileInfo.StoreLocation.LOCAL_MIRROR == fileInfo.getStoreLocation()
                ){
                    FileOpenUtils.openFile(fragment.getContext(),
                            Constants.Path.LOCAL_DISK_ROOT+fileInfo.getPath()+"/"+fileInfo.getName());

                }else{
                    String url = Constants.getDownloadFileUrl() + fileInfo.getPath()+fileInfo.getName()+"&filename="+fileInfo.getName();
                    final String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+fileInfo.getName();
                    EasyLog.d("FilesAdapter",path);
                    EasyLog.d("url",url);

                    DownloadTools.DownloadFile(url, path, new DownloadTools.DownLoadCallback() {
                        @Override
                        public void onSuccess() {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {

                                    FileOpenUtils.openFile(fragment.getContext(),path);
                                }
                            });
                        }

                        @Override
                        public void onFail(String msg) {

                        }
                    });
                }

            }
        });

        return convertView;
    }

    private class ViewHolder {
        public ImageView ivIcon;
        public View ivPlay;
        public TextView tvFileName;
        public ImageView ivLocation;
    }
}
