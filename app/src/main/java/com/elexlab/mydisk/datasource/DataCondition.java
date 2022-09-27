package com.elexlab.mydisk.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Young on 2016/12/8.
 */
public class DataCondition {
    public interface AscOrDesc{
        int ASC = 0;
        int DESC = 1;
    }
    public static class OrderBy{
        private String orderByColum;
        private int ascorDesc;

        public String getOrderByColum() {
            return orderByColum;
        }

        public void setOrderByColum(String orderByColum) {
            this.orderByColum = orderByColum;
        }

        public int getAscorDesc() {
            return ascorDesc;
        }

        public void setAscorDesc(int ascorDesc) {
            this.ascorDesc = ascorDesc;
        }
    }

    private String Url;
    private Map<String,Object> paramMap = new HashMap<String,Object>();
    private Map<String,Object> orParamMap = new HashMap<String,Object>();
    private OrderBy orderBy;

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public Map<String, Object> getOrParamMap() {
        return orParamMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public void addParam(String key,Object value){
        paramMap.put(key,value);
    }

    public void addOrParam(String key,Object value){
        if(orParamMap.containsKey(key)){//the same key
            //make a list for every value
            Object savedValue = orParamMap.get(key);
            List objsList = null;
            if(savedValue instanceof List){//already made a list for them
                objsList = (List) savedValue;
            }else{//first time to make list,reminder adding pre value to list
                objsList = new ArrayList();
                objsList.add(savedValue);
            }
            objsList.add(value);
            orParamMap.put(key,objsList);
        }else{
            orParamMap.put(key,value);
        }

    }

    public OrderBy getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(OrderBy orderBy) {
        this.orderBy = orderBy;
    }
}
