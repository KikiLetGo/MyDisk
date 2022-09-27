package com.elexlab.mydisk.ui.gallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.DistAlbums;
import com.elexlab.myalbum.pojos.EncryptedMedia;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.ImageUtils;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.pojo.FileInfo;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/3/17.
 */
public class AlbumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    public interface ItemViewType{
        int MY_ALBUM_TITLE = 0;
        int MY_ALBUM = 1;
        int SYS_ALBUM_TITLE = 2;
        int SYS_ALBUM = 3;
    }

    //private DistAlbums distAlbums;
    private Context context;

    private List<ListItemData> datas = new ArrayList<ListItemData>();

    public AlbumsAdapter(Context context, DistAlbums distAlbums) {
        //this.distAlbums = distAlbums;
        this.context = context;
        mapToListItemDatas(distAlbums);
    }
    public void resetAlbums(DistAlbums distAlbums){
        //this.distAlbums = distAlbums;
        mapToListItemDatas(distAlbums);
        notifyDataSetChanged();
    }
    public void albumUpdate(Album album){
        Album targetAlbum ;
        int targetPos = -1;
        for(int i=0;i<datas.size();i++){
            ListItemData listItemData = datas.get(i);
            Object obj = listItemData.getData();
            if(album.equals(obj)){
                targetAlbum = (Album) obj;
                targetPos = i;
                break;
            }
        }
        if(targetPos >= 0){
            ListItemData listItemData = ListItemData.wrapData(album,ItemViewType.SYS_ALBUM);
            datas.set(targetPos,listItemData);
        }
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

    private void mapToListItemDatas(DistAlbums distAlbums){
        datas.clear();
        if(distAlbums == null){
            return;
        }
        if(distAlbums.getMyAlbumList().size() > 0){
            ListItemData listItemData
                    = new ListItemData(context.getResources().getString(R.string.my_albums), ItemViewType.MY_ALBUM_TITLE);
            datas.add(0,listItemData);
            myAlbumFirstIndex = 1;
        }
        List<ListItemData<Album>> myAlbumsData = ListItemData.wrapDataList(distAlbums.getMyAlbumList(), ItemViewType.MY_ALBUM);
        datas.addAll(myAlbumsData);
        if(distAlbums.getSysAlbumList().size()>0){
            ListItemData listItemData
                    = new ListItemData(context.getResources().getString(R.string.my_albums), ItemViewType.SYS_ALBUM_TITLE);

            int myAlbumsSize = distAlbums.getMyAlbumList().size();
            myAlbumsSize += myAlbumsSize>0?1:0;
            datas.add(myAlbumsSize,listItemData);
        }
        systemAlbumFirstIndex = datas.size();
        List<ListItemData<Album>> systemAlbumData = ListItemData.wrapDataList(distAlbums.getSysAlbumList(), ItemViewType.SYS_ALBUM);
        datas.addAll(systemAlbumData);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == ItemViewType.MY_ALBUM || viewType == ItemViewType.SYS_ALBUM){
            AlbumViewHolder viewHolder = new AlbumViewHolder(LayoutInflater.from(
                    context).inflate(R.layout.item_ablum, parent,
                    false));
            return viewHolder;
        }else {
            TitleViewHolder viewHolder = new TitleViewHolder(LayoutInflater.from(
                    context).inflate(R.layout.item_albums_title, parent,
                    false));
            return viewHolder;
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == ItemViewType.MY_ALBUM_TITLE){
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.tvTitle.setText(R.string.my_albums);
            return;
        }

        if(getItemViewType(position) == ItemViewType.SYS_ALBUM_TITLE){
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.tvTitle.setText(R.string.system_albums);
            return;
        }

        final AlbumViewHolder viewHolder = (AlbumViewHolder) holder;
        final Album album = (Album) datas.get(position).getData();
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(album.isEncryption()){
                    //GestureLockActivity.startActivity(context,GestureLockActivity.JumpTo.ALBUM_DETAILS,album);
                }else {
                    AlbumDetailsActivity.startActivity(context,album);
                }
            }
        });
        viewHolder.tvAlbumName.setText(album.getShowName());

        if(album.getMediaList() == null || album.getMediaList().size() <=0){
            return;
        }
        viewHolder.tvPhotoCounts.setText(String.valueOf(album.getMediaList().size()));

        final Media media = album.getCoverMedia();
        if(album.isEncryption()){
            viewHolder.ivEncryption.setVisibility(View.VISIBLE);

        }else {
            viewHolder.ivEncryption.setVisibility(View.GONE);
        }
        if(media instanceof EncryptedMedia){
            Observable.create(new ObservableOnSubscribe<Bitmap>() {
                @Override
                public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
                    Bitmap bitmap;
                    if(media.getFile().exists()){
                        bitmap = media.loadBitmap(context,200,200);//EncryptionManager.getInstance().decryptPhotoToBitmap(media,context,10);

                    }else{//cloud
                        String cloudUrl = FileInfo.media2FileInfo(media).getUrl();
                        bitmap = Glide.with(context)
                                        .asBitmap()
                                        .load(cloudUrl)
                                        .into(200,200)
                                        .get();
                    }
                    //e.onNext(bitmap);
                    Bitmap blurBitmap = ImageUtils.fastblur(context,bitmap,30);
                    e.onNext(blurBitmap);
                    e.onComplete();

                }
            })
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<Bitmap>() {
                @Override
                public void onSubscribe(Disposable d) {
                    Glide.with(context).load(R.mipmap.ic_launcher).into(viewHolder.ivCover);
                }

                @Override
                public void onNext(Bitmap value) {
                    viewHolder.ivCover.setImageBitmap(value);

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        }else {
            if(media.getFile().exists()) {
                media.loadThumbnailInto(context,viewHolder.ivCover);
            }else{
                String cloudUrl = FileInfo.media2FileInfo(media).getUrl();
                Glide.with(context)
                        .load(cloudUrl)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop()
                        .into(viewHolder.ivCover);
            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        return datas.get(position).getDatatype();
    }


    private int myAlbumSize;
    private int sysAlbumSize;
    @Override
    public int getItemCount() {
        return datas == null?0:datas.size();
    }

    private class AlbumViewHolder extends RecyclerView.ViewHolder{
        private TextView tvAlbumName;
        private TextView tvPhotoCounts;
        private ImageView ivCover;
        private ImageView ivEncryption;
        public AlbumViewHolder(View itemView) {
            super(itemView);
            tvAlbumName = (TextView) itemView.findViewById(R.id.tvAlbumName);
            tvPhotoCounts = (TextView) itemView.findViewById(R.id.tvPhotoCounts);
            ivCover = (ImageView) itemView.findViewById(R.id.ivCover);
            ivEncryption = (ImageView) itemView.findViewById(R.id.ivEncryption);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onItemLongClickListener != null){
                        onItemLongClickListener.onLongClick(AlbumViewHolder.this);
                    }
                    return false;
                }
            });
        }
    }

    public class TitleViewHolder extends RecyclerView.ViewHolder{
        private TextView tvTitle;
        public TitleViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if(onItemLongClickListener != null){
                        onItemLongClickListener.onLongClick(TitleViewHolder.this);
                    }
                    return false;
                }
            });
        }
    }

    private OnItemLongClickListener onItemLongClickListener;

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public interface OnItemLongClickListener{
        void onLongClick(RecyclerView.ViewHolder viewHolder);
    }
}
