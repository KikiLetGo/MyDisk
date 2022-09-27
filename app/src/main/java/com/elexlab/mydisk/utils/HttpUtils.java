package com.elexlab.mydisk.utils;

import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.elexlab.mydisk.core.HttpManager;
import com.elexlab.mydisk.datasource.HeroLib;

import java.util.Map;

public class HttpUtils {
    private static final String TAG = HttpUtils.class.getName();
    public interface HttpRequestListener{
        void onResponse(String s);
        void onErrorResponse(String msg);
    }
    public static void GET(String url,final HttpRequestListener httpRequestListener){
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if(httpRequestListener != null){
                    httpRequestListener.onResponse(s);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(httpRequestListener != null){
                    httpRequestListener.onErrorResponse("error,but I think you have no ideal to deal with it at all");
                }
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HttpManager.getInstance().getRequestQueue(HeroLib.getInstance().getAppContext()).add(request);
    }

    public static void POST(String url,final Map<String,String> form,final HttpRequestListener httpRequestListener){
        EasyLog.d(TAG, url);
        for(String key:form.keySet()){
            EasyLog.d(TAG, key+"="+form.get(key));

        }

        StringRequest request = new StringRequest(Request.Method.POST,url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        EasyLog.d(TAG, response);
                        if(httpRequestListener != null){
                            httpRequestListener.onResponse(response);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //requestTags.remove(requestTag);
                Toast.makeText(HeroLib.getInstance().appContext,"网络错误",Toast.LENGTH_SHORT).show();
                EasyLog.e(TAG, error.getMessage());
                if(httpRequestListener != null){
                    httpRequestListener.onErrorResponse(error.getMessage());
                }

            }
        }){

            @Override
            protected Map<String, String> getParams() {
                //在这里设置需要post的参数

                return form;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        HttpManager.getInstance().getRequestQueue(HeroLib.getInstance().getAppContext()).add(request);
    }

    public static void uploadFile(){
        
    }
}
