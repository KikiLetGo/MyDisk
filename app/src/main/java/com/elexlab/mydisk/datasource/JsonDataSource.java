package com.elexlab.mydisk.datasource;

import com.alibaba.fastjson.JSON;
import com.elexlab.mydisk.utils.EasyLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Young on 2016/12/14.
 */
public abstract class JsonDataSource<T> extends AsyncDataSource<T>{
    private final static String TAG = JsonDataSource.class.getSimpleName();

    protected JSONObject extractJSONObjectFromJSONObject(JSONObject jsonObject){
        try {
            return jsonObject.getJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected org.json.JSONArray extractJSONArrayFromJSONObject(JSONObject jsonObject){
        try {
            return jsonObject.getJSONArray("datas");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    protected String[] extractExtraParams(JSONObject jsonObject){
        return null;
    }


    protected T parseJsonObject(JSONObject jsonObject,Class<T> clazz){
        if(jsonObject == null){
            EasyLog.d(TAG,"jsonObject is null!");
            return null;
        }
        T t = JSON.parseObject(jsonObject.toString(),clazz);
        return t;
    }

    protected T parseJsonObject(String json,Class<T> clazz){
        if(json == null){
            EasyLog.d(TAG,"json is null!");
            return null;
        }
        T t = JSON.parseObject(json,clazz);
        return t;
    }

    protected List<T> parseJsonArray(org.json.JSONArray jsonArray, Class<T> clazz){
        if(jsonArray == null){
            EasyLog.d(TAG,"jsonArray is null!");
            return null;
        }
        List<T> list = JSON.parseArray(jsonArray.toString(),clazz);
        return list;
    }

    protected List<T> parseJsonArray(String json, Class<T> clazz){
        if(json == null){
            EasyLog.d(TAG,"json is null!");
            return null;
        }
        List<T> list = JSON.parseArray(json,clazz);
        return list;
    }

    protected void onJsonLoad(String json){

    }

    protected void onJsonArrayLoad(String json){

    }
}
