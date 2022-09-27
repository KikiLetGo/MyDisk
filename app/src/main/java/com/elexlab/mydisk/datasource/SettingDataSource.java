package com.elexlab.mydisk.datasource;

import com.elexlab.mydisk.pojo.Setting;
import com.litesuits.orm.db.assit.QueryBuilder;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingDataSource extends DBDataSource<Setting> {

    public Setting querySettingSync(){
        List<Setting> list = null;
        QueryBuilder queryBuilder = QueryBuilder.create(Setting.class);
        DataCondition dataCondition = new DataCondition();
        dataCondition.addParam("id","ONLY");
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

        list = liteOrm.query(queryBuilder);

        return (list==null||list.size()==0)?null:list.get(0);
    }
}
