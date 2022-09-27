package com.elexlab.mydisk.manager;

import android.view.View;

import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.managers.AlbumManager;
import com.elexlab.myalbum.notify.EventType;
import com.elexlab.myalbum.notify.ObserverManager;
import com.elexlab.myalbum.pojos.DistAlbums;
import com.elexlab.myalbum.utils.DeviceUtils;
import com.elexlab.myalbum.utils.EasyLog;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.cloud.CloudFileManager;
import com.elexlab.mydisk.core.ThreadManager;
import com.elexlab.mydisk.datasource.DataSourceCallback;
import com.elexlab.mydisk.datasource.HeroLib;
import com.elexlab.mydisk.pojo.Device;
import com.elexlab.mydisk.ui.gallery.AlbumsFragment;
import com.obs.services.model.ListObjectsRequest;
import com.obs.services.model.ObjectListing;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MultiDeviceManager {
    private static final String TAG = MultiDeviceManager.class.getName();
    private static MultiDeviceManager instance = new MultiDeviceManager();
    public static MultiDeviceManager getInstance(){
        return instance;
    }
    private MultiDeviceManager(){
        this.currentDevice = nativeDevice();
    }
    private Device currentDevice;

    public Device getCurrentDevice() {
        return currentDevice;
    }

    public void switchDevice(Device device){
        if(this.currentDevice.getDeviceCode().equals(device.getDeviceCode())){//device not changed
            ObserverManager.getInstance().notify(EventType.DEVICE_CHANGED,0,0,null);
            return;
        }
        setCurrentDevice(device);
    }

    private void setCurrentDevice(Device device) {
        boolean deviceChanged = false;
        if(!this.currentDevice.getDeviceCode().equals(device.getDeviceCode())){//device changed
            deviceChanged = true;
        }
        this.currentDevice = device;
        if(deviceChanged){
            CloudFileManager.getInstance().onDeviceChanged(()->{
                reloadAlbums();
                ObserverManager.getInstance().notify(EventType.DEVICE_SWITCHING,0,0,null);
            });
        }
    }

    private Device nativeDevice(){
        Device device = new Device();
        device.setName(DeviceUtils.getDeviceName(MyAlbum.getContext()));
        device.setId(DeviceUtils.getDeviceId(MyAlbum.getContext()));

        return device;
    }

    public void listDevices(DataSourceCallback<List<Device>> dataSourceCallback){
        String dir = "";

        com.elexlab.mydisk.utils.EasyLog.d(TAG,"dir:"+dir);
        ListObjectsRequest request = new ListObjectsRequest(Client.BUCKET_NAME);
        request.setPrefix(dir);
        request.setDelimiter("/");
        request.setMaxKeys(1000);
        ThreadManager.getInstance().getHttpHandler().post(()->{
            List<Device> devices = new ArrayList<>();
            ObjectListing result = Client.getInstance().listObjects(request);
            for(String prefix : result.getCommonPrefixes()){
                EasyLog.i("ListObjects", "Objects in folder [" + prefix + "]:");
                Device device = new Device();
                String deviceCode = prefix.replace("/","");
                String[] idName = deviceCode.split("#");
                device.setName(idName[0]);
                device.setId(idName[1]);
                devices.add(device);
            }
            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(devices);
            }
        });
    }

    public boolean isNativeDevice(){
        if(getCurrentDevice().equals(nativeDevice())){
            return true;
        }else{
            return false;
        }
    }

    private void reloadAlbums(){
        Observable.create((ObservableOnSubscribe<DistAlbums>) e -> {
            try {
                if(MultiDeviceManager.getInstance().isNativeDevice()){
                    AlbumManager.getInstance().loadAlbumsWithDist(HeroLib.getInstance().getAppContext(), distAlbums -> {
                        e.onNext(distAlbums);
                        e.onComplete();
                    });
                }else {
                    CloudAlbumManager.getInstance()
                            .reload()
                            .loadAlbum(new DataSourceCallback<DistAlbums>() {
                        @Override
                        public void onSuccess(DistAlbums distAlbums, String... extraParams) {
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
                ObserverManager.getInstance().notify(EventType.DEVICE_CHANGED,0,0,null);
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
}
