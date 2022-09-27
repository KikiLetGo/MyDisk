package com.elexlab.mydisk.ui.gallery;

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elexlab.myalbum.data.DynamicDataSource;
import com.elexlab.myalbum.managers.AlbumManager;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverInterf;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.DistAlbums;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.manager.CloudAlbumManager;
import com.elexlab.mydisk.manager.MultiDeviceManager;
import com.elexlab.mydisk.utils.EasyLog;

import java.util.Collections;
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
public class AlbumsFragment extends Fragment implements View.OnClickListener, ObserverInterf, DynamicDataSource.DataListener {
    private final static String TAG =AlbumsFragment.class.getSimpleName();
    private RecyclerView rcvAlbums;
    private View ivAddAlbum;
    private View rlAlbumLoadBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_albums, container, false);
        ivAddAlbum = contentView.findViewById(R.id.ivAddAlbum);
        ivAddAlbum.setOnClickListener(this);
        rlAlbumLoadBar = contentView.findViewById(R.id.rlAlbumLoadBar);
        rcvAlbums = (RecyclerView) contentView.findViewById(R.id.rcvAlbums);
        rcvAlbums.setLayoutManager(new LinearLayoutManager(getContext()));
        albumsAdapter = new AlbumsAdapter(getContext(),distAlbums);
        rcvAlbums.setAdapter(albumsAdapter);
        initRecyclerViewDrag();
        ObserverManager.getInstance().regist(this);
        AlbumManager.getInstance().getLocalAlbumDataSource().registerAlbumsListener(this);
        return contentView;
    }



    private AlbumsAdapter albumsAdapter;

    private List<Album> albumList;
    private DistAlbums distAlbums;


    private void loadData(){
        Observable.create((ObservableOnSubscribe<DistAlbums>) e -> {
            try {
                if(MultiDeviceManager.getInstance().isNativeDevice()){
                    AlbumManager.getInstance().loadAlbumsWithDist(getActivity(), distAlbums -> {
                        AlbumsFragment.this.distAlbums = distAlbums;
                        e.onNext(distAlbums);
                        e.onComplete();
                    });
                }else {
                    CloudAlbumManager.getInstance().loadAlbum(new DataSourceCallback<DistAlbums>() {
                        @Override
                        public void onSuccess(DistAlbums distAlbums, String... extraParams) {
                            AlbumsFragment.this.distAlbums = distAlbums;
                            e.onNext(distAlbums);
                            e.onComplete();
                        }
                        @Override
                        public void onFailure(String errMsg, int code) {

                        }
                    });
                }

            }catch (Exception exception){
                exception.printStackTrace();
                e.onError(exception);
            }

        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<DistAlbums>() {

            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(DistAlbums value) {

                rlAlbumLoadBar.setVisibility(View.GONE);
                albumsAdapter.resetAlbums(distAlbums);
                AlbumsFragment.this.albumList = distAlbums.getAllAlbumList();
                ObserverManager.getInstance().notify(EventType.ALBUM_LOAD_FINISH,0,0,null);
            }

            @Override
            public void onError(Throwable e) {
                ObserverManager.getInstance().notify(EventType.ALBUM_LOAD_FINISH,0,0,null);
            }

            @Override
            public void onComplete() {

            }
        });




    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ObserverManager.getInstance().unregist(this);
        AlbumManager.getInstance().getLocalAlbumDataSource().unregisterAlbumsListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v == ivAddAlbum){
            //CreateAlbumActivity.startActivity(getActivity());
        }
    }

    @Override
    public boolean onDataChange(int type, long param1, long param2, Object obj) {
        switch (type){
            case EventType.PHOTO_DELETED:{
                Media media = (Media) obj;

                distAlbums.removeMedia(media);
                albumsAdapter.resetAlbums(distAlbums);

                break;
            }
            case EventType.PHOTO_ADDED:{

                albumsAdapter.resetAlbums(distAlbums);

                break;
            }
            case EventType.ALBUM_CREATED:{
                albumsAdapter.resetAlbums(distAlbums);
                break;
            }
            case EventType.ALBUM_DELETED:{
                Album album = (Album) obj;
                distAlbums.removeAlbum(album);
                albumsAdapter.resetAlbums(distAlbums);
                break;
            }

            case EventType.ALBUM_UPDATE:{
                Album album = (Album) obj;
                distAlbums.updateAlbum(album);
                albumsAdapter.resetAlbums(distAlbums);
                break;
            }

            case EventType.DEVICE_SWITCHING:{
                loadData();
                break;
            }
        }
        return false;
    }

    private void initRecyclerViewDrag(){
        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchCallback(albumsAdapter));
        itemTouchHelper.attachToRecyclerView(rcvAlbums);
        albumsAdapter.setOnItemLongClickListener(new AlbumsAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() == AlbumsAdapter.ItemViewType.MY_ALBUM) {
                    itemTouchHelper.startDrag(viewHolder);
                }
            }
        });
        //albumsAdapter.setHasStableIds(true);
    }

    @Override
    public void onDataAdded(Object o) {
        ThreadManager.getInstance().getMainHandler().post(()->{AlbumManager.getInstance().loadAlbumsWithDist(getActivity(), allAlbum -> albumsAdapter.resetAlbums(allAlbum));});

    }

    @Override
    public void onDataDeleted(Object o) {
        ThreadManager.getInstance().getMainHandler().post(()->{AlbumManager.getInstance().loadAlbumsWithDist(getActivity(), allAlbum -> albumsAdapter.resetAlbums(allAlbum));});

    }

    @Override
    public void onDataChanged(Object o) {
        ThreadManager.getInstance().getMainHandler().post(()->{AlbumManager.getInstance().loadAlbumsWithDist(getActivity(), allAlbum -> albumsAdapter.resetAlbums(allAlbum));});
    }


    private class ItemTouchCallback extends ItemTouchHelper.Callback{

        private RecyclerView.Adapter<?> adapter;

        public ItemTouchCallback(RecyclerView.Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            final int dragFlags;
            final int swipeFlags;
            //DeviceUtils.vibrate(context,100);
            if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN ;
                swipeFlags = 0;
            } else {
                // dragFlags = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;

                //swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            }
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder source, RecyclerView.ViewHolder target) {
            if (source.getItemViewType() != target.getItemViewType()) {
                return false;
            }
            int fromPosition = source.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            //data.add(toPosition, data.remove(fromPosition));
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public void onChildDrawOver(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDrawOver(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }

        RecyclerView.ViewHolder currentSelectedViewHolder;
        private Handler handler = new Handler();
        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if(viewHolder != null){
                currentSelectedViewHolder = viewHolder;
            }

            if(currentSelectedViewHolder == null){
                return;
            }
            float scale = 1.0f;
            float rotate = 0f;
            if(actionState == ItemTouchHelper.ACTION_STATE_DRAG){
                scale = 1.2f;
                rotate = -10f;
            }
            currentSelectedViewHolder.itemView.setScaleX(scale);
            currentSelectedViewHolder.itemView.setScaleY(scale);
            currentSelectedViewHolder.itemView.setRotation(rotate);
            if(actionState == ItemTouchHelper.ACTION_STATE_IDLE){
                //pictureAdapter.notifyDataSetChanged();
                //rcvAlbums.setAdapter(pictureAdapter);
                albumsAdapter.resetAlbums(distAlbums);

            }

        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
            EasyLog.d(TAG,"fromPos:"+fromPos+" toPos:"+toPos);


            int type = albumsAdapter.getItemViewType(fromPos);
            switch (type){
                case AlbumsAdapter.ItemViewType.MY_ALBUM:{
                    int index = albumsAdapter.getMyAlbumFirstIndex();
                    swapAlbums(distAlbums.getMyAlbumList(),fromPos-index,toPos-index);
                    break;
                }
                case AlbumsAdapter.ItemViewType.SYS_ALBUM:{
                    int index = albumsAdapter.getSystemAlbumFirstIndex();
                    swapAlbums(distAlbums.getSysAlbumList(),fromPos-index,toPos-index);

                    break;
                }
            }

            //albumsAdapter.notifyItemMoved(fromPos,toPos);
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }
    }

    private void swapAlbums(List<Album> albumList,int fromPos,int toPos){

        Collections.swap(albumList,fromPos,toPos);
        Album fromAlbum = albumList.get(fromPos);
        Album toAlbum = albumList.get(toPos);
        fromAlbum.exchangeSort(toAlbum);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
        //MobclickAgent.onPageStart("AlbumsFragment"); //统计页面，"MainScreen"为页面名称，可自定义
    }

    @Override
    public void onPause() {
        super.onPause();
        //MobclickAgent.onPageEnd("AlbumsFragment");
    }
}
