package com.elexlab.myalbum.data;

import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.Media;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class DynamicDataSource<T> {
    public interface DataListener<T>{
        void onDataAdded(T t);
        void onDataDeleted(T t);
        void onDataChanged(T t);

    }
    protected List<DataListener<T>> listeners;
    public void registerAlbumsListener(DataListener<T> dataListener){
        check();
        listeners.add(dataListener);
    }
    public void unregisterAlbumsListener(DataListener<T> dataListener){
        check();
        listeners.remove(dataListener);
    }


    private void check(){
        if(listeners == null){
            listeners = new ArrayList<>();
        }
    }
}
