package com.elexlab.mydisk.datasource;

import com.litesuits.orm.LiteOrm;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Young on 2016/12/13.
 */
public class DBDataSource<T> extends AsyncDataSource<T>{
    protected static LiteOrm liteOrm = LiteOrmHelper.getLiteOrmInstance();
    @Override
    public void addData(T t,DataSourceCallback<T> dataSourceCallback) {
        long result = liteOrm.save(t);
        if(result > 0){
            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(t);
            }
        }else{
            if(dataSourceCallback != null){
                dataSourceCallback.onFailure("wrong", (int) result);
            }
        }
    }

    @Override
    public void addData(DataCondition dataCondition, DataSourceCallback<T> dataSourceCallback) {

    }

    @Override
    public void deleteData(T t,DataSourceCallback<T> dataSourceCallback) {
        long result = liteOrm.delete(t);
        if(result > 0){
            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(t);
            }
        }else{
            if(dataSourceCallback != null){
                dataSourceCallback.onFailure("wrong", (int) result);
            }
        }
    }

    @Override
    public void deleteData(DataCondition dataCondition, DataSourceCallback<T> dataSourceCallback) {

    }

    @Override
    public void updateData(T t,DataSourceCallback<T> dataSourceCallback) {
        long result = liteOrm.update(t);
        if(result > 0){
            if(dataSourceCallback != null){
                dataSourceCallback.onSuccess(t);
            }
        }else{
            if(dataSourceCallback != null){
                dataSourceCallback.onFailure("wrong", (int) result);
            }
        }
    }

    @Override
    public void updateData(DataCondition dataCondition, DataSourceCallback<T> dataSourceCallback) {

    }

    @Override
    public void getData(DataSourceCallback<T> dataSourceCallback, DataCondition dataCondition, Class<T> clazz) {
        List<T> list = null;
        QueryBuilder queryBuilder = QueryBuilder.create(clazz);

        if(dataCondition != null){
            Map<String,Object> map = dataCondition.getParamMap();
            Set<String> keySet = map.keySet();
            int i = 0;
            for(String key:keySet){
                Object value = map.get(key);
                if( i==0 ){//first param
                    queryBuilder.where(key+" = ?",new String[]{value.toString()});
                }else{
                    queryBuilder.whereAppendAnd()
                                .whereAppend(key+" = ?",new Object[]{value});
                }
                i++;
            }
        }

        list = liteOrm.query(queryBuilder);

        if(dataSourceCallback != null){
            dataSourceCallback.onSuccess((list==null||list.size()==0)?null:list.get(0));
        }
    }

    @Override
    public void getDatas(DataSourceCallback<List<T>> dataSourceCallback, DataCondition dataCondition, Class<T> clazz) {
        List<T> list = null;
        QueryBuilder queryBuilder = QueryBuilder.create(clazz);

        if(dataCondition != null){
            Map<String,Object> map = dataCondition.getParamMap();
            Set<String> keySet = map.keySet();
            for(String key:keySet){
                queryBuilder.whereEquals(key,map.get(key));
            }
        }

        if(dataCondition != null && dataCondition.getOrderBy() != null){
            if(dataCondition.getOrderBy().getAscorDesc() == DataCondition.AscOrDesc.ASC){
                queryBuilder.appendOrderAscBy(dataCondition.getOrderBy().getOrderByColum());
            }else{
                queryBuilder.appendOrderDescBy(dataCondition.getOrderBy().getOrderByColum());
            }
        }
        list = liteOrm.query(queryBuilder);

        if(dataSourceCallback != null){
            dataSourceCallback.onSuccess(list);
        }
    }

    @Override
    public void cancelRequest(String tag) {

    }

    @Override
    public void cancelAllRequest() {

    }

}
