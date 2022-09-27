package com.elexlab.mydisk.ui.gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.elexlab.myalbum.listeners.ProgressListener;
import com.elexlab.myalbum.managers.AlbumManager;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverInterf;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.pojos.Photo;
import com.elexlab.myalbum.utils.DeviceUtils;
import com.elexlab.myalbum.utils.FileUtils;
import com.elexlab.myalbum.utils.FormatUtils;
import com.elexlab.myalbum.utils.ImageUtils;
import com.elexlab.myalbum.utils.PathUtils;
import com.elexlab.mydisk.R;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudFileManager;
import com.elexlab.mydisk.constants.ActivityTransCodes;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DiskFileLoader;
import com.elexlab.mydisk.manager.FileSystemManager;
import com.elexlab.mydisk.pojo.FileInfo;
import com.elexlab.mydisk.ui.BaseActivity;
import com.elexlab.mydisk.ui.wiget.PullToZoomRecyclerViewEx;
import com.elexlab.mydisk.ui.wiget.RecyclerViewHeaderAdapter;
import com.elexlab.mydisk.ui.wiget.arcmenu.ArcMenu;
import com.elexlab.mydisk.utils.EasyLog;
import com.elexlab.mydisk.utils.SocialUtils;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by BruceYoung on 10/3/17.
 */
public class AlbumDetailsActivity extends BaseActivity implements ObserverInterf {
    private final static String TAG = AlbumDetailsActivity.class.getSimpleName();

    private final static int SPAN_COUNT = 3;
    public static void startActivity(Context context, Album album){
        Intent intent = new Intent(context, AlbumDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("album",album);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    public static void startEncryptionActivity(Context context, Album album,String password){
        Intent intent = new Intent(context, AlbumDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("album",album);
        intent.putExtras(bundle);
        intent.putExtra("password",password);
        context.startActivity(intent);
    }

    private View rlNavbar;
    protected PullToZoomRecyclerViewEx ptzrcvAlbumView;
    protected Album album;
    //protected Album decryptedAlbum;
    protected AlbumDetailsAdapter albumDetailsAdapter;
    private TextView tvPhotoCounts;
    private Disposable disposable;
    private ImageView ivSyncAll;
    protected ImageView ivHeaderZoomView;
    private View rlOperationBoard;
    private TextView tvSelectedMediasCount;
    private TextView tvNoUploadCount;

    private TextView tvAlbumName;
    private View rlSyncAll;

    //protected ImageView ivRotateLogo;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        tvAlbumName = findViewById(R.id.tvAlbumName);
        rlNavbar = findViewById(R.id.rlNavbar);
        ivSyncAll = findViewById(R.id.ivSyncAll);
        rlSyncAll = findViewById(R.id.rlSyncAll);

        tvNoUploadCount = findViewById(R.id.tvNoUploadCount);

        rlOperationBoard = findViewById(R.id.rlOperationBoard);
        tvSelectedMediasCount = (TextView) findViewById(R.id.tvSelectedMediasCount);
        ptzrcvAlbumView = (PullToZoomRecyclerViewEx) findViewById(R.id.ptzrcvAlbumView);
        AbsListView.LayoutParams localObject = new AbsListView.LayoutParams(DeviceUtils.getScreenWidth(this), 600);
        ptzrcvAlbumView.setHeaderLayoutParams(localObject);


        album = (Album) getIntent().getSerializableExtra("album");

        tvAlbumName.setText(album.getShowName());
        ivHeaderZoomView = (ImageView)ptzrcvAlbumView.getZoomView().findViewById(R.id.ivHeaderZoomView);


        albumDetailsAdapter = new AlbumDetailsAdapter(this,album);
        loadPhotosToView();

        final GridLayoutManager manager =  new GridLayoutManager(this, SPAN_COUNT);
        //final StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);

        manager.setOrientation(GridLayoutManager.VERTICAL);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return albumDetailsAdapter.getItemViewType(position) == RecyclerViewHeaderAdapter.INT_TYPE_HEADER ? SPAN_COUNT : 1;
            }
        });
        ptzrcvAlbumView.setAdapterAndLayoutManager(albumDetailsAdapter,manager);
        ptzrcvAlbumView.setOnHeadMissDistChangeListener(new PullToZoomRecyclerViewEx.OnHeadMissDistChangeListener() {
            @Override
            public void onHeadDistChange(float missDist) {
                if(missDist < 0){
                    return;
                }
                float headerHeight = ptzrcvAlbumView.getmHeaderHeight();
                int transparent = (int) ((missDist/headerHeight)*255);
                if(transparent >= 250){//over
                    transparent = 255;
                }
                if(transparent <= 10){//over
                    transparent = 0;
                }
                transparent = transparent<<24;

                EasyLog.d(TAG,"transparent:"+Integer.toHexString(transparent));
                int barColor = getResources().getColor(R.color.colorPrimary);
                barColor = barColor & 0x00ffffff;
                EasyLog.d(TAG,"barColor:"+Integer.toHexString(barColor));
                int colorValue = barColor|transparent;
                EasyLog.d(TAG,"colorValue after:"+Integer.toHexString(colorValue));
                rlNavbar.setBackgroundColor(colorValue);

                tvAlbumName.setTextColor(0x000000|transparent);




                if(arcMenu.isExpanded()){
                    arcMenu.close();
                }
            }

        });

        List<FileInfo> fileInfos = findNoUploadedFile();
        int size = fileInfos.size();
        tvNoUploadCount.setText(size>=100?"99+":String.valueOf(size));
        rlSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Animation operatingAnim = AnimationUtils.loadAnimation(AlbumDetailsActivity.this, R.anim.rotate_anim);
                LinearInterpolator lin = new LinearInterpolator();
                operatingAnim.setInterpolator(lin);
                ivSyncAll.startAnimation(operatingAnim);



                FileSystemManager.getInstance().uploadFiles(fileInfos,new ProgressListener<FileInfo>() {
                    @Override
                    public void onProgress(final float progress, FileInfo fileInfo) {
                        ThreadManager.getInstance().getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                int all = fileInfos.size();
                                int now = all-(int) (all*progress);
                                tvNoUploadCount.setText(String.valueOf(now));
                                albumDetailsAdapter.notifyDataSetChanged();
//                                String pstr = new DecimalFormat( "0.00" ).format(progress*100);
//                                resetProgress(String.valueOf(pstr+"%"));

                            }
                        });
                    }

                    @Override
                    public void onComplete() {
                        ThreadManager.getInstance().getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                ivSyncAll.clearAnimation();
                                stopProgress();
                                Toast.makeText(AlbumDetailsActivity.this,"文件夹内文件同步成功～",Toast.LENGTH_LONG).show();
                                albumDetailsAdapter.notifyDataSetChanged();
                            }
                        });


                    }

                    @Override
                    public void onError(int code, final String message) {
                        ThreadManager.getInstance().getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                stopProgress();
                                Toast.makeText(AlbumDetailsActivity.this,"同步发生错误:"+message,Toast.LENGTH_LONG).show();
                            }
                        });

                    }
                });

            }
        });
        initArcMenu();

        loadDataToView();




        Slidr.attach(this, getDefaultSlidrConfig(this));

        ObserverManager.getInstance().regist(this);

        initOperationBoard();
    }
    public static SlidrConfig getDefaultSlidrConfig(Context context){
        SlidrConfig config = new SlidrConfig.Builder()
                .primaryColor(context.getResources().getColor(R.color.colorPrimary))
                .secondaryColor(context.getResources().getColor(R.color.colorPrimary))
                .position(SlidrPosition.LEFT)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
                .build();
        return config;
    }
    protected void loadPhotosToView(){
        //decryptedAlbum = album;
        albumDetailsAdapter.resetAlbum(album);
        loadHeadView();
    }

    protected void loadHeadView(){
        bitmapLoadAndComputeTask
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Bitmap value) {
                        ivHeaderZoomView.setImageBitmap(value);
                        //ivRotateLogo.clearAnimation();
                        //ivRotateLogo.setVisibility(View.GONE);

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void loadDataToView(){
        TextView tvAlbumName = (TextView) ptzrcvAlbumView.getHeaderView().findViewById(R.id.tvAlbumName);
        tvPhotoCounts = (TextView) ptzrcvAlbumView.getHeaderView().findViewById(R.id.tvPhotoCounts);

        tvAlbumName.setText(album.getShowName());
        tvPhotoCounts.setText(String.valueOf(album.getPhotosCounts())
                +" "+getResources().getString(R.string.medias));

        if(album.getType() == Album.AlbumType.SYSTEM){
           // rlSyncAll.setVisibility(View.GONE);
            arcMenu.setVisibility(View.GONE);
        }
    }
    protected Observable bitmapLoadAndComputeTask = Observable.create(new ObservableOnSubscribe<Bitmap>() {
        @Override
        public void subscribe(ObservableEmitter<Bitmap> e) throws Exception {
//            File file = album.getCoverMedia().getFile();
//            if(!file.exists()){
//                e.onError(new Throwable("file not exist!"));
//                return;
//            }
            Media media = album.getCoverMedia();
            Bitmap bitmap = null;
            if(media.getFile().exists()){
                bitmap = media.loadBitmap(AlbumDetailsActivity.this,200,200);// Glide.with(AlbumDetailsActivity.this).load(file).asBitmap().into(200,200).get();
            }else{//cloud
                String cloudUrl = FileInfo.media2FileInfo(media).getUrl();
                try {
                    bitmap = Glide.with(AlbumDetailsActivity.this)
                            .asBitmap()
                            .load(cloudUrl)
                            .into(200,200)
                            .get();
                }catch (InterruptedException exception){
                    exception.printStackTrace();
                }

            }

            if(bitmap == null){
                return;
            }
            e.onNext(bitmap);
            Bitmap blurBitmap = ImageUtils.fastblur(AlbumDetailsActivity.this,bitmap,5);
            e.onNext(blurBitmap);
            e.onComplete();
        }
    });

    private void showMoreOperation(){
        final View contentView = LayoutInflater.from(this).inflate(R.layout.view_more_operation, null);
        final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.total_transparent_bg));
        popupWindow.showAsDropDown(rlSyncAll);

        ListView lvOperations = (ListView) contentView.findViewById(R.id.lvOperations);
        String update = getResources().getString(R.string.update);
        final String delete = getResources().getString(R.string.delete);

        String[] operations = new String[]{update,delete};

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(AlbumDetailsActivity.this,
                android.R.layout.simple_list_item_1, operations);
        lvOperations.setAdapter(arrayAdapter);
        lvOperations.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:{
                        //UpdateAlbumActivity.startActivityForResult(AlbumDetailsActivity.this,album);
                        break;
                    }
                    case 1:{
                        Observable.create(new ObservableOnSubscribe<Integer>() {
                            @Override
                            public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                                AlbumPhotoDeleteModeViewBuilder albumPhotoDeleteModeViewBuilder =
                                        new AlbumPhotoDeleteModeViewBuilder();

                                albumPhotoDeleteModeViewBuilder.setDeleteModeChosenListener(
                                        new AlbumPhotoDeleteModeViewBuilder.DeleteModeChosenListener() {
                                    @Override
                                    public void onModeChosen(int mode) {
                                        e.onNext(mode);
                                        e.onComplete();
                                    }
                                });

                                albumPhotoDeleteModeViewBuilder
                                        .buildTransModeDialogView(AlbumDetailsActivity.this)
                                        .show();
                            }
                        })
                        .subscribeOn(AndroidSchedulers.mainThread())

                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(new Function<Integer, ObservableSource<Integer>>() {
                            @Override
                            public ObservableSource<Integer> apply(final Integer integer) throws Exception {
                                if(Album.DeletePhotoMode.NO_RECOVRE == integer){
                                    return Observable.create(new ObservableOnSubscribe<Integer>() {
                                        @Override
                                        public void subscribe(final ObservableEmitter<Integer> e) throws Exception {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(AlbumDetailsActivity.this);
                                            builder
                                            .setMessage(R.string.delete_album_message_tips)
                                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    showProgress(R.string.deleting,true);
                                                    e.onNext(integer);
                                                    e.onComplete();

                                                }
                                            })
                                            .show();
                                        }
                                    });

                                }else{
                                    showProgress(R.string.delete_album_with_recover_tips,true);
                                    return Observable.just(integer);
                                }
                            }
                        })

                        .observeOn(Schedulers.newThread())
                        .flatMap(new Function<Integer, ObservableSource<Float>>() {
                            @Override
                            public ObservableSource<Float> apply(Integer integer) throws Exception {
                                switch (integer){
                                    case Album.DeletePhotoMode.RECOVRE:{
                                        return recoverDeleteTask;
                                    }
                                    case Album.DeletePhotoMode.NO_RECOVRE:{
                                        return noRecoverDeleteTask;
                                    }
                                    default:return null;
                                }
                            }
                        })

                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Float>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Float value) {
                                resetProgress(FormatUtils.floatToPercent(value*100));
                            }

                            @Override
                            public void onError(Throwable e) {
                                stopProgress();
                            }

                            @Override
                            public void onComplete() {
                                stopProgress();
                                onDeleteAlbum();
                            }
                        });
                        break;
                    }
                    default:break;
                }
                popupWindow.dismiss();
            }
        });
    }

    private void onDeleteAlbum(){
        ObserverManager.getInstance().notify(EventType.ALBUM_DELETED,0,0,album);
        finish();
        Toast.makeText(AlbumDetailsActivity.this,R.string.delete_photo_success_tips,Toast.LENGTH_SHORT).show();
    }
    private Observable<Float> recoverDeleteTask = Observable.create(new ObservableOnSubscribe<Float>() {
        @Override
        public void subscribe(final ObservableEmitter<Float> e) throws Exception {
            try {
                AlbumManager.getInstance().deleteAlbum(album, Album.DeletePhotoMode.RECOVRE, new ProgressListener<Album>() {
                    @Override
                    public void onProgress(float progress,Album album) {
                        e.onNext(progress);
                    }

                    @Override
                    public void onComplete() {
                        e.onComplete();
                    }

                    @Override
                    public void onError(int code, String message) {

                    }
                });
            }catch (Exception exception){
                exception.printStackTrace();
                e.onError(exception);
            }
        }
    });

    private Observable<Float> noRecoverDeleteTask = Observable.create(new ObservableOnSubscribe<Float>() {
        @Override
        public void subscribe(final ObservableEmitter<Float> e) throws Exception {
            try {
                AlbumManager.getInstance().deleteAlbum(album,Album.DeletePhotoMode.NO_RECOVRE,new ProgressListener<Album>() {
                    @Override
                    public void onProgress(float progress,Album album) {
                        e.onNext(progress);
                    }

                    @Override
                    public void onComplete() {
                        e.onComplete();
                    }

                    @Override
                    public void onError(int code, String message) {

                    }
                });
            }catch (Exception exception){
                exception.printStackTrace();
                e.onError(exception);
            }
        }
    });

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ObserverManager.getInstance().unregist(this);
        if(disposable != null){
            disposable.dispose();
        }
        System.gc();

        FileUtils.clearFileUnderDir(PathUtils.getPublicDecryptionPhotoSavePath(this));
    }

    private ArcMenu arcMenu;
    private void initArcMenu(){
        arcMenu = (ArcMenu) findViewById(R.id.arc_menu);
        View cameraView = LayoutInflater.from(this).inflate(R.layout.view_camera_opt,null);
        View albumView = LayoutInflater.from(this).inflate(R.layout.view_album_opt,null);

        arcMenu.addItem(cameraView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String state = Environment.getExternalStorageState(); //拿到sdcard是否可用的状态码
                if (state.equals(Environment.MEDIA_MOUNTED)){   //如果可用
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takePictureFile = new File(getPictureSavePath());
                    Uri uri = Uri.fromFile(takePictureFile);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    startActivityForResult(intent,ActivityTransCodes.RequestCode.TAKE_PICTRUE);
                }else {
                    Toast.makeText(AlbumDetailsActivity.this,"sdcard不可用",Toast.LENGTH_SHORT).show();
                }
            }
        });

        arcMenu.addItem(albumView, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //PhotoChooseActivity.startActivityForResult(AlbumDetailsActivity.this);
            }
        });


    }

    private File takePictureFile = null;

    @Override
    public boolean onDataChange(int type, long param1, long param2, Object obj) {
        switch (type){
            case EventType.PHOTO_DELETED:{
                //Album deletedPhotoAlbum = (Album) obj;
                //Media media = deletedPhotoAlbum.getMediaList().get((int) param1);
                Media media = (Media) obj;
                for(Media albumMedia :album.getMediaList()){
                    if(albumMedia.equals(media)){
                        album.getMediaList().remove(albumMedia);
                        break;
                    }
                }
                albumDetailsAdapter.resetAlbum(album);
                tvPhotoCounts.setText(String.valueOf(album.getPhotosCounts())
                        +" "+getResources().getString(R.string.photos));
                break;
            }
            case EventType.ALBUM_UPDATE:{
                this.album = (Album) obj;
                loadDataToView();
                //albumDetailsAdapter.resetPhotoList(album.getMediaList());
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == ActivityTransCodes.RequestCode.PHOTO_CHOOSE) {
            if (resultCode == ActivityTransCodes.ResultCode.PHOTO_CHOOSED) {
                Album dataAlbum = (Album) data.getSerializableExtra("album");
                addPhotosToAlbum(dataAlbum.getMediaList());
            }
        }else if(requestCode == ActivityTransCodes.RequestCode.TAKE_PICTRUE){
            if (data != null && (data.getData() != null|| data.getExtras() != null)){ //防止没有返回结果
                Uri uri =data.getData();
                Bitmap photo = null;
                if (uri != null) {
                    //photo =BitmapFactory.decodeFile(uri.getPath()); //拿到图片
                    onPictureToken(uri);
                }else {
                    Bundle bundle =data.getExtras();
                    if (bundle != null){
                        photo =(Bitmap) bundle.get("data");
                    }else{
                        Toast.makeText(getApplicationContext(),R.string.unknown_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if(photo == null){
                    Toast.makeText(getApplicationContext(),R.string.unknown_error,Toast.LENGTH_SHORT).show();
                }else {
                    onPictureToken(photo);
                }
            }else{
                onPictureToken();
            }
        }else if(requestCode == ActivityTransCodes.RequestCode.UPDATE_ALBUM){
            if(resultCode == ActivityTransCodes.ResultCode.ALBUM_PERMISSION_CHANGED){
                //album = (Album) data.getSerializableExtra("album");
                //albumDetailsAdapter.resetAlbum(album);
                finish();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private String getPictureSavePath(){
        String photoSavePath = PathUtils.getPublicPhotoSavePath(this);
        photoSavePath += File.separator + System.currentTimeMillis()+".jpg";
        return photoSavePath;
    }

    private void onPictureToken(){
        //FileUtils.saveBitmap(photoSavePath,bitmap);
        if(takePictureFile == null || !takePictureFile.exists()){
            Toast.makeText(getApplicationContext(),R.string.cancel,Toast.LENGTH_SHORT).show();

            return;
        }

        addPhotoToAlbum(takePictureFile);

    }
    private void onPictureToken(Uri uri){
        File file = new File(uri.toString());
        //FileUtils.saveBitmap(photoSavePath,bitmap);
        addPhotoToAlbum(file);
    }

    private void onPictureToken(Bitmap bitmap){
        String photoSavePath = getPictureSavePath();
        FileUtils.saveBitmap(photoSavePath,bitmap);
        addPhotoToAlbum(new File(photoSavePath));
    }


    protected void addPhotoToAlbum(File file){
        final Media media = new Photo();
        media.setFile(file);
        media.loadMediaInfo();
        AlbumPhotoTransModeViewBuilder albumPhotoTransModeViewBuilder = new AlbumPhotoTransModeViewBuilder();
        albumPhotoTransModeViewBuilder
                .setTransModeChosenListener(new AlbumPhotoTransModeViewBuilder.TransModeChosenListener() {
                    @Override
                    public void onModeChosen(int mode) {
                        showProgress(R.string.add_medias_into_album_tips,true);
                        album.addPhotoToAlbum(0, media,new Album.PhotoAddListener(){
                            @Override
                            public void onComplete() {
                                stopProgress();
                                //albumDetailsAdapter.notifyDataSetChanged();
                                albumDetailsAdapter.notifyItemInserted(1);
                                albumDetailsAdapter.notifyItemRangeChanged(1,album.getMediaList().size());
                                ObserverManager.getInstance().notify(EventType.ALBUM_UPDATE,0,0,album);
                            }

                            @Override
                            public void onPhotoAdded(Media media,int index,float percent) {

                            }
                        },mode);
                    }
                })
                .buildTransModeDialogView(this)
                .show();
    }



    protected void addPhotosToAlbum(final List<Media> mediaList){
        AlbumPhotoTransModeViewBuilder albumPhotoTransModeViewBuilder = new AlbumPhotoTransModeViewBuilder();

        albumPhotoTransModeViewBuilder
                .setTransModeChosenListener(new AlbumPhotoTransModeViewBuilder.TransModeChosenListener() {
                    @Override
                    public void onModeChosen(int mode) {
                        showProgress(R.string.add_medias_into_album_tips,true);
                        setMediasToAlbum(mediaList,mode);
                    }
                })
                .buildTransModeDialogView(this)
                .show();
    }

    private float mediaAddProgress = 0;
    private int mediaAddIndex = 0;
    protected void setMediasToAlbum(final List<Media> mediaList,final int mode){
        Observable<Media> mediasAddTask = Observable.create(new ObservableOnSubscribe<Media>() {
            @Override
            public void subscribe(final ObservableEmitter<Media> e) throws Exception {
                mediaAddProgress = 0;
                try {
                    album.addPhotosToAlbum(mediaList, new Album.PhotoAddListener() {
                        @Override
                        public void onComplete() {
                            EasyLog.d(TAG,"onComplete:");
                            e.onComplete();
                            //albumDetailsAdapter.resetAlbum(album);
                            //ObserverManager.getInstance().notify(EventType.ALBUM_UPDATE,0,0,album);
                        }

                        @Override
                        public void onPhotoAdded(Media media,int index,float percent) {
                            EasyLog.d(TAG,"onPhotoAdded:"+ media.getFile().getName());
                            mediaAddProgress = percent;
                            mediaAddIndex = index;
                            e.onNext(media);
                            //albumDetailsAdapter.addMedia(media);
                        }
                    },mode);
                }catch (Exception exception){
                    exception.printStackTrace();
                    e.onError(exception);
                }

            }
        });

        mediasAddTask
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Media>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Media value) {
                resetProgress(FormatUtils.floatToPercent(mediaAddProgress*100));
                // albumDetailsAdapter.addPhoto(mediaAddIndex,value);


            }

            @Override
            public void onError(Throwable e) {
                stopProgress();
                Toast.makeText(AlbumDetailsActivity.this,R.string.unknown_error,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                stopProgress();
                //albumDetailsAdapter.resetAlbum(album);
                albumDetailsAdapter.notifyItemRangeInserted(1,mediaList.size());
                albumDetailsAdapter.notifyItemRangeChanged(1,album.getMediaList().size());
                ptzrcvAlbumView.getPullRootView().scrollToPosition(1);
                ObserverManager.getInstance().notify(EventType.ALBUM_UPDATE,0,0,album);
                Toast.makeText(AlbumDetailsActivity.this,R.string.medias_added_tips,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initOperationBoard(){
        albumDetailsAdapter.setActionListener(new AlbumDetailsAdapter.ActionListener() {
            @Override
            public void onOperationModeChanged(boolean isOperationMode) {
                if(isOperationMode){
                    showOperationBoard();
                }else{
                    hiddenOperationBoard();
                }
            }

            @Override
            public void onMediaSelected(Media media) {
                tvSelectedMediasCount.setText(String.format("(%s)",albumDetailsAdapter.getChoosenMedias().size()));

            }

            @Override
            public void onMediaUnSelected(Media media) {
                tvSelectedMediasCount.setText(String.format("(%s)",albumDetailsAdapter.getChoosenMedias().size()));
            }
        });

        View llDelete = findViewById(R.id.llDelete);
        View llShare = findViewById(R.id.llShare);

        llDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MediaDeleteDialogViewBuilder()
                        .appendMedias(albumDetailsAdapter.getChoosenMedias())
                        .setDeleteResultListener(new MediaDeleteDialogViewBuilder.DeleteResultListener() {
                            @Override
                            public void onMediaDelete(Media media) {

                            }

                            @Override
                            public void onMediasDeleteFinish() {
                                albumDetailsAdapter.setOperationMode(false);
                            }
                        })
                        .buildView(AlbumDetailsActivity.this)
                        .show();
            }
        });

        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocialUtils.shareMediaToMultiPlatform(AlbumDetailsActivity.this,albumDetailsAdapter.getChoosenMedias());
                albumDetailsAdapter.setOperationMode(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(albumDetailsAdapter.isOperationMode()){
            albumDetailsAdapter.setOperationMode(false);
        }else{
            super.onBackPressed();
        }
    }

    private void showOperationBoard(){

        tvSelectedMediasCount.setVisibility(View.VISIBLE);
        tvSelectedMediasCount.setText(String.format("(%s)",albumDetailsAdapter.getChoosenMedias().size()));
        rlOperationBoard.setVisibility(View.VISIBLE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0,1);
        alphaAnimation.setDuration(500);
        rlOperationBoard.startAnimation(alphaAnimation);

    }

    private void hiddenOperationBoard(){
        tvSelectedMediasCount.setVisibility(View.GONE);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1,0);
        alphaAnimation.setDuration(500);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rlOperationBoard.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        rlOperationBoard.startAnimation(alphaAnimation);
    }

    private List<FileInfo> findNoUploadedFile(){
        List<FileInfo> fileInfos = new ArrayList<>();
        for(Media media:album.getMediaList()){
            FileInfo fileInfo = FileInfo.media2FileInfo(media);
            if(!CloudFileManager.getInstance().fileInCloud(fileInfo)){
                fileInfos.add(fileInfo);
            }
        }
        return fileInfos;
    }
}
