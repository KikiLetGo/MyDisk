package com.elexlab.mydisk.datasource;

/**
 * The call back when we request a datasource
 * Created by BruceYoung on 15/11/12.
 */
public interface DataSourceCallback<T> {
    void onSuccess(T t, String... extraParams);
    void onFailure(String errMsg, int code);
}
