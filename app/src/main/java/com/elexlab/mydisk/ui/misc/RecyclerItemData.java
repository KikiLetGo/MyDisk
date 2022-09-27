package com.elexlab.mydisk.ui.misc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/19/17.
 */
public class RecyclerItemData<T> {
    private T data;
    private int datatype;

    public RecyclerItemData(T data, int datatype) {
        this.data = data;
        this.datatype = datatype;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getDatatype() {
        return datatype;
    }

    public void setDatatype(int datatype) {
        this.datatype = datatype;
    }

    public static <T> RecyclerItemData wrapData(T data,int dataType){
        RecyclerItemData listItemData = new RecyclerItemData(data,dataType);
        return listItemData;
    }
    public static <T> T unWrapData(RecyclerItemData<T> data){
        return data.getData();
    }

    public static <T> List<RecyclerItemData<T>> wrapDataList(List<T> dataList, int dataType){
        List<RecyclerItemData<T>> listItemDatas = new ArrayList<RecyclerItemData<T>>();
        if(dataList == null){
            return null;
        }
        for(T data:dataList){
            listItemDatas.add(wrapData(data,dataType));
        }
        return listItemDatas;
    }

    public static <T> List<T> unWrapDataList(List<RecyclerItemData<T>> dataList){
        List<T >listItemDatas = new ArrayList<T>();
        if(dataList == null){
            return null;
        }
        for(RecyclerItemData<T> data:dataList){
            listItemDatas.add(unWrapData(data));
        }
        return listItemDatas;
    }


}
