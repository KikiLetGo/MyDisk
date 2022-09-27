package com.elexlab.mydisk.datasource;

import android.text.TextUtils;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.elexlab.mydisk.core.HttpManager;
import com.elexlab.mydisk.pojo.HttpPojo;
import com.elexlab.mydisk.utils.CommonUtil;
import com.elexlab.mydisk.utils.EasyLog;


import org.json.JSONObject;

import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Young on 2016/12/8.
 */
public abstract class HttpDatasource<T extends HttpPojo> extends JsonDataSource<T>{
    private final static String TAG = HttpDatasource.class.getSimpleName();
    public interface StatusCode{
        //network or system
        int NET_ERROR = 0;

        //default/unknown
        int UNKNOWN = 100;
        //success
        int SUCCESS = 200;
        //client issues
        int BAD_PARAMS = 400;
        int TOKEN_INVALID = 401;

        int NOT_FOUND = 404;

        //service issues
    }

    public interface ServiceCommonKey{
        String STATUS_CODE = "statusCode";
        String MSG = "msg";
    }

    protected boolean usePrivilege = false;

    @Override
    public void addData(final T t,final DataSourceCallback<T> dataSourceCallback) {

        if(!checkPrivilege(dataSourceCallback)) {
            return;
        }
        String request = getRequest(t,dataSourceCallback);

        Map<String,String> paramMap = t.getRequestParams();
        if(paramMap == null){
            paramMap = CommonUtil.getUrlParam(request);
        }
        request = url;
        if(request == null){
            return;
        }

        final String requestTag = String.valueOf(System.currentTimeMillis());//time stamp as requestTag
        final long startTime = System.currentTimeMillis();
        final String finalRequest = request;
        final Map<String, String> finalParamMap = paramMap;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        EasyLog.d(TAG, "response:"+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean isResponseSuccess = checkAndDealResponse(jsonObject,dataSourceCallback);
                            if(!isResponseSuccess){
                                return;
                            }
                            requestTags.remove(requestTag);
                            if (dataSourceCallback != null) {
                                dataSourceCallback.onSuccess(t,extractExtraParams(new JSONObject(response)));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        long endTime = System.currentTimeMillis();
                        long httpRequestTime = endTime - startTime;
//                        //@BW-report
//                        Event event = EventFactory.getInstance().buildHttpRequestEvent(finalRequest,httpRequestTime,true,"success");
//                        EventReporter.reportEvent(event);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestTags.remove(requestTag);
                Toast.makeText(HeroLib.getInstance().appContext,"网络错误",Toast.LENGTH_SHORT).show();
                EasyLog.e(TAG, error.getMessage());
                if(dataSourceCallback!=null){
                    dataSourceCallback.onFailure(error==null?"":error.getMessage(),
                            (error==null||error.networkResponse==null)?StatusCode.NET_ERROR:error.networkResponse.statusCode);
                }
                long endTime = System.currentTimeMillis();
                long httpRequestTime = endTime - startTime;
                reportRequestError(error,finalRequest,httpRequestTime);
            }
        }){

            @Override
            protected Map<String, String> getParams() {
                //在这里设置需要post的参数

                return finalParamMap;
            }
        };

        sendHttpRequestToService(request,stringRequest,paramMap);
    }

    @Override
    public void addData(DataCondition dataCondition, DataSourceCallback<T> dataSourceCallback) {

    }

    @Override
    public void deleteData(T t,DataSourceCallback<T> dataSourceCallback) {
        if(!checkPrivilege(dataSourceCallback)){
            return;
        }
    }

    @Override
    public void deleteData(DataCondition dataCondition, DataSourceCallback<T> dataSourceCallback) {

    }

    @Override
    public void updateData(final T t,final DataSourceCallback<T> dataSourceCallback) {
        if(!checkPrivilege(dataSourceCallback)){
            return;
        }
        String requestStr = getRequest(t,dataSourceCallback);

        Map<String,String> paramMap = t.getRequestParams();
        if(paramMap == null){
            paramMap = CommonUtil.getUrlParam(requestStr);
        }
        Request request = buildRequest(t,requestStr,paramMap,dataSourceCallback);
        sendHttpRequestToService(requestStr,request,paramMap);
    }

    @Override
    public void updateData(DataCondition dataCondition, DataSourceCallback<T> dataSourceCallback) {
        if(!checkPrivilege(dataSourceCallback)){
            return;
        }
        String requestStr = getRequest(dataCondition,dataSourceCallback);

        Map<String,String> paramMap = new HashMap<>();
        for(String key:dataCondition.getParamMap().keySet()){
            String value = String.valueOf(dataCondition.getParamMap().get(key));
            paramMap.put(key,value);
        }
        if(paramMap == null){
            paramMap = CommonUtil.getUrlParam(requestStr);
        }
        Request request = buildRequest(null,requestStr,paramMap,dataSourceCallback);
        sendHttpRequestToService(requestStr,request,paramMap);
    }

    private List<String> requestTags = new ArrayList<String>();

    @Override
    public void getData(final DataSourceCallback<T> dataSourceCallback, final DataCondition dataCondition, final Class<T> clazz) {
        if(!checkPrivilege(dataSourceCallback)){
            return;
        }
        final String request = getRequest(dataCondition,dataSourceCallback);
        if(request == null){
            return;
        }

        final String requestTag = String.valueOf(System.currentTimeMillis());//time stamp as requestTag
        final long startTime = System.currentTimeMillis();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(request, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            boolean isResponseSuccess = checkAndDealResponse(response,dataSourceCallback);
                            if(!isResponseSuccess){
                                return;
                            }
                            requestTags.remove(requestTag);
                            String jsonStr = response.toString();
                            EasyLog.d(TAG, "response json is:"+jsonStr);
                            onJsonLoad(jsonStr);
                            T t = parseJsonObject(extractJSONObjectFromJSONObject(response),clazz);
                            if (dataSourceCallback != null) {
                                dataSourceCallback.onSuccess(t,extractExtraParams(response));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        long endTime = System.currentTimeMillis();
                        long httpRequestTime = endTime - startTime;
                        //@BW-report
//                        Event event = EventFactory.getInstance().buildHttpRequestEvent(request,httpRequestTime,true,"success");
//                        EventReporter.reportEvent(event);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestTags.remove(requestTag);
                EasyLog.e(TAG, error.getMessage());
                if(error.networkResponse != null){
                    int statusCode = error.networkResponse.statusCode;
                    EasyLog.e(TAG,"onErrorResponse->statusCode:"+statusCode);
                }else{
                    Toast.makeText(HeroLib.getInstance().appContext,"网络错误",Toast.LENGTH_SHORT).show();
                }
                if(dataSourceCallback!=null){
                    dataSourceCallback.onFailure(error==null?"":error.getMessage(),
                            (error==null||error.networkResponse==null)?StatusCode.NET_ERROR:error.networkResponse.statusCode);
                }
                long endTime = System.currentTimeMillis();
                long httpRequestTime = endTime - startTime;
                reportRequestError(error,request,httpRequestTime);

            }
        });

        sendHttpRequestToService(request,jsonObjectRequest);
    }


    @Override
    public void getDatas(final DataSourceCallback<List<T>> dataSourceCallback,final DataCondition dataCondition,final Class<T> clazz) {
        if(!checkPrivilege(dataSourceCallback)){
            return;
        }
        final String request = getRequest(dataCondition,dataSourceCallback);
        if(request == null){
            return;
        }

        final long startTime = System.currentTimeMillis();
        final String requestTag = String.valueOf(System.currentTimeMillis());//time stamp as requestTag
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(request, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        requestTags.remove(requestTag);
                        try {
                            boolean isResponseSuccess = checkAndDealResponse(response,dataSourceCallback);
                            if(!isResponseSuccess){
                                return;
                            }

                            String jsonStr = response.toString();
                            EasyLog.d(TAG, "response json is:"+jsonStr);
                            onJsonArrayLoad(jsonStr);

                            List<T> list = parseJsonArray(extractJSONArrayFromJSONObject(response),clazz);
                            if (dataSourceCallback != null) {
                                dataSourceCallback.onSuccess(list,extractExtraParams(response));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        long endTime = System.currentTimeMillis();
                        long httpRequestTime = endTime - startTime;
                        //@BW-report
//                        Event event = EventFactory.getInstance().buildHttpRequestEvent(request,httpRequestTime,true,"success");
//                        EventReporter.reportEvent(event);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        requestTags.remove(requestTag);
                        EasyLog.e(TAG, error.getMessage());
                        Toast.makeText(HeroLib.getInstance().appContext,"网络错误",Toast.LENGTH_SHORT).show();
                        if(dataSourceCallback!=null){
                            dataSourceCallback.onFailure(error==null?"":error.getMessage(),
                                    (error==null||error.networkResponse==null)?StatusCode.NET_ERROR:error.networkResponse.statusCode);
                        }
                        long endTime = System.currentTimeMillis();
                        long httpRequestTime = endTime - startTime;
                        reportRequestError(error,request,httpRequestTime);

                    }
                });

        sendHttpRequestToService(request,jsonObjectRequest);
    }

    private Request buildRequest(final Object obj,String request,Map<String,String> paramMap,final DataSourceCallback dataSourceCallback){

        if(paramMap == null){
            paramMap = CommonUtil.getUrlParam(request);
        }
        if(request == null){
            return  null;
        }

        final String requestTag = String.valueOf(System.currentTimeMillis());//time stamp as requestTag
        final long startTime = System.currentTimeMillis();
        final String finalRequest = request;
        final Map<String, String> finalParamMap = paramMap;
        StringRequest stringRequest = new StringRequest(Request.Method.POST,request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        EasyLog.d(TAG, "response:"+response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean isResponseSuccess = checkAndDealResponse(jsonObject,dataSourceCallback);
                            if(!isResponseSuccess){
                                return;
                            }
                            requestTags.remove(requestTag);
                            if (dataSourceCallback != null) {
                                dataSourceCallback.onSuccess(obj,extractExtraParams(new JSONObject(response)));
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        long endTime = System.currentTimeMillis();
                        long httpRequestTime = endTime - startTime;
                        //@BW-report
//                        Event event = EventFactory.getInstance().buildHttpRequestEvent(finalRequest,httpRequestTime,true,"success");
//                        EventReporter.reportEvent(event);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestTags.remove(requestTag);
                EasyLog.e(TAG, error.getMessage());
                Toast.makeText(HeroLib.getInstance().appContext,"网络错误",Toast.LENGTH_SHORT).show();
                if(dataSourceCallback!=null){
                    dataSourceCallback.onFailure(error==null?"":error.getMessage(),
                            (error==null||error.networkResponse==null)?StatusCode.NET_ERROR:error.networkResponse.statusCode);
                }
                long endTime = System.currentTimeMillis();
                long httpRequestTime = endTime - startTime;
                reportRequestError(error,finalRequest,httpRequestTime);
            }
        }){

            @Override
            protected Map<String, String> getParams() {
                //在这里设置需要post的参数

                return finalParamMap;
            }
        };
        return stringRequest;

    }
    private void sendHttpRequestToService(String requestUrl,Request request,Map<String,String>... paramMaps){
        final String requestTag = String.valueOf(System.currentTimeMillis());//time stamp as requestTag


        EasyLog.d(TAG,"start a http request! url:"+requestUrl);
        if(paramMaps != null && paramMaps.length > 0){
            Map<String,String> paramMap = paramMaps[0];
            EasyLog.d(TAG,"params********************************");
            for(String key:paramMap.keySet()){
                String value = paramMap.get(key);
                EasyLog.d(TAG, key + ":" + value);
            }
            EasyLog.d(TAG,"params********************************");
        }

        requestTags.add(requestTag);
        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setTag(requestTag);
        HttpManager.getInstance().getRequestQueue(HeroLib.getInstance().getAppContext()).add(request);
    }

    protected boolean checkAndDealResponse(JSONObject response,DataSourceCallback dataSourceCallback){
        try {
            int statusCode = response.getInt(ServiceCommonKey.STATUS_CODE);
            if (statusCode != StatusCode.SUCCESS) {//request fail
                if (dataSourceCallback != null) {
                    String msg = response.getString(ServiceCommonKey.MSG);
                    EasyLog.w(TAG,"statusCode:"+statusCode+" msg:"+msg);
                    dataSourceCallback.onFailure(msg, statusCode);
                }
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void cancelAllRequest() {
        for(String tag:requestTags){
            HttpManager.getInstance().getRequestQueue(HeroLib.getInstance().appContext).cancelAll(tag);
        }
        requestTags.clear();
    }

    @Override
    public void cancelRequest(String tag) {
        HttpManager.getInstance().getRequestQueue(HeroLib.getInstance().appContext).cancelAll(tag);
        requestTags.remove(tag);
    }

    private String getRequest(T t, DataSourceCallback dataSourceCallback){
        if(t == null){
            String errorMsg = "pojo is null!";
            EasyLog.e(TAG,errorMsg);
            if(dataSourceCallback != null){
                dataSourceCallback.onFailure(errorMsg,StatusCode.BAD_PARAMS);//-1 is temp use
            }
            return null;
        }
        String request = url;
        List<Field> fieldList = new ArrayList<>() ;
        Class tempClass = t.getClass();
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }

        if(fieldList.size() !=0 ){
            request += "?";
        }
        int i = 0;
        for (Field field : fieldList) {
            String fieldName = field.getName();
            EasyLog.d("getAllFields","fieldName:"+fieldName);

            try {
                field.setAccessible(true);
                Object fieldValue = field.get(t);
                if(!isFieldValueValid(fieldValue)){
                    //String subParams = getSubPojoParams(fieldName,fieldValue);
                    //request += "&" + subParams;
                    continue;
                }
                String fieldValueStr = String.valueOf(fieldValue);
                fieldValue = URLEncoder.encode(fieldValueStr);
                request += fieldName + "=" +fieldValue;
                if(i != fieldList.size()-1){
                    request += "&";
                }
                i++;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        request += getCommonParams(request);
        return request;
    }

    /**
     * be careful of dead loop ref
     * @param subPojo
     * @return
     */
    private String getSubPojoParams(String subPojoName,Object subPojo){
        List<Field> fieldList = new ArrayList<>() ;
        Class tempClass = subPojo.getClass();
        while (tempClass != null) {//当父类为null的时候说明到达了最上层的父类(Object类).
            fieldList.addAll(Arrays.asList(tempClass .getDeclaredFields()));
            tempClass = tempClass.getSuperclass(); //得到父类,然后赋给自己
        }
        String param = "";
        int i = 0;
        for (Field field : fieldList) {
            String fieldName = field.getName();
            EasyLog.d("getAllFields","fieldName:"+fieldName);

            try {
                field.setAccessible(true);
                Object fieldValue = field.get(subPojo);
                if(!isFieldValueValid(fieldValue)){
                    String subParams = getSubPojoParams(fieldName,fieldValue);
                    param += "&" + subParams;
                    continue;
                }
                String fieldValueStr = String.valueOf(fieldValue);
                fieldValue = URLEncoder.encode(fieldValueStr);
                param += subPojoName + "_" + fieldName + "=" +fieldValue;
                if(i != fieldList.size()-1){
                    param += "&";
                }
                i++;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return param;
    }

    /**
     * valid value type
     * int,long,String,boolean
     * @param fieldValue
     * @return
     */
    private boolean isFieldValueValid(Object fieldValue){
        return fieldValue instanceof Integer||
                fieldValue instanceof Long||
                fieldValue instanceof String||
                fieldValue instanceof Boolean;
    }

    private String getRequest(DataCondition dataCondition, DataSourceCallback dataSourceCallback){
        if(dataCondition == null){
            String errorMsg = "dataCondition is null!";
            EasyLog.w(TAG,errorMsg);
//            if(dataSourceCallback != null){
//                dataSourceCallback.onFailure(errorMsg,-1);//-1 is temp use
//            }
//            return null;
        }
        String request = "";
        if(dataCondition == null || TextUtils.isEmpty(dataCondition.getUrl())){
            request = url;
        }else{
            request = dataCondition.getUrl();
        }

        if(dataCondition == null ||
                dataCondition.getParamMap() == null ||
                dataCondition.getParamMap().size() <= 0){
        }else{
            Set<String> keySet = dataCondition.getParamMap().keySet();
            if(keySet.size() !=0 ){
                request += "?";
            }
            int i = 0;
            for(String key:keySet){
                Object value = dataCondition.getParamMap().get(key);
                String fieldValueStr = String.valueOf(value);
                value = URLEncoder.encode(fieldValueStr);
                request += key + "=" +value;
                if(i != dataCondition.getParamMap().size()-1){
                    request += "&";
                }
                i++;
            }
        }

        request += getCommonParams(request);
        return request;
    }

    private String getCommonParams(String originUrl){
        String commonParams = "";
//        if(originUrl.contains("?")){//have param
//            commonParams += "&";
//        }else{
//            commonParams += "?";
//        }
//        commonParams += "countryCode="+ DeviceUtils.getCountryCode();
//        try {
//            int versionCode = HeroLib.getInstance().getAppContext()
//                    .getPackageManager().getPackageInfo(HeroLib.getInstance().getAppContext().getPackageName(),0).versionCode;
//            commonParams += "&versionCode=" + versionCode;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        User user = UserPrivilegeManager.getInstance().getUser();
//        if(user != null){
//            commonParams += "&userId=" + user.getUserId();
//        }
//
//        if(usePrivilege){//need user token here
//            commonParams += "&";
//            if(!UserPrivilegeManager.getInstance().isUserLogin()){
//                return commonParams;
//            }
//            commonParams += "token="+ UserPrivilegeManager.getInstance().getUser().getTokenValue();
//        }

        return commonParams;
    }

    /**
     * Some requests need privilege,like user login or other privilege
     * @param dataSourceCallback
     * @return
     */
    private boolean checkPrivilege(DataSourceCallback<?> dataSourceCallback){
//        if(usePrivilege) {
//            if (!UserPrivilegeManager.getInstance().isUserLogin()) {
//                if (dataSourceCallback != null) {
//                    dataSourceCallback.onFailure("Privilege limit,please login first", StatusCode.TOKEN_INVALID);
//                    return false;
//                }
//            }
//        }
        return true;
    }

    private void reportRequestError(VolleyError error,String url,long httpRequestTime){
        String info = "unknown_error";
//        if (!DeviceUtils.isNetworkConnected(HeroLib.getInstance().getAppContext())) {
//            info = "client_network_not_connected";
//        }
//        try {
//            byte[] htmlBodyBytes = error.networkResponse.data;
//            info = new String(htmlBodyBytes);
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
        EasyLog.e(TAG,info);

        //@BW-report
//        Event event = EventFactory.getInstance().buildHttpRequestEvent(url,httpRequestTime,false,info);
//        EventReporter.reportEvent(event);

    }
}
