package com.elexlab.mydisk.ui.gallery;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/19/17.
 */
public class ListItemData<T> {
    private T data;
    private int datatype;

    public ListItemData(T data, int datatype) {
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

    public static <T> ListItemData wrapData(T data, int dataType){
       ListItemData listItemData = new ListItemData(data,dataType);
        return listItemData;
    }
    public static <T> T unWrapData(ListItemData<T> data){
        return data.getData();
    }

    public static <T> List<ListItemData<T>> wrapDataList(List<T> dataList, int dataType){
        List<ListItemData<T>> listItemDatas = new ArrayList<ListItemData<T>>();
        if(dataList == null){
            return null;
        }
        for(T data:dataList){
            listItemDatas.add(wrapData(data,dataType));
        }
        return listItemDatas;
    }

    public static <T> List<T> unWrapDataList(List<ListItemData<T>> dataList){
        List<T >listItemDatas = new ArrayList<T>();
        if(dataList == null){
            return null;
        }
        for(ListItemData<T> data:dataList){
            listItemDatas.add(unWrapData(data));
        }
        return listItemDatas;
    }


}
