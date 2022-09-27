package com.elexlab.mydisk.datasource;

/**
 * Created by BruceYoung on 4/15/17.
 */
public abstract class DataSource {
    public interface DataSourceStatusCode{
        public interface StatusCode{
            //system
            int SYS_ERROR = 0;

            //default/unknown
            int UNKNOWN = 100;
            //success
            int SUCCESS = 200;
            //client issues
            int BAD_PARAMS = 400;
            int PRIVILEGE_FORBIDDEN = 401;

            int NOT_FOUND = 404;

            //service issues
        }
    }
    protected String url = "";

    public String getUrl() {
        return url;
    }

    public DataSource setUrl(String url) {
        this.url = url;
        return this;
    }
}
