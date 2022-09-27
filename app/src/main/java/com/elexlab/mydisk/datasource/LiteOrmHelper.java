package com.elexlab.mydisk.datasource;

import com.litesuits.orm.LiteOrm;

/**
 * Created by Young on 2016/12/15.
 */
public class LiteOrmHelper {
    private static final String DB_NAME = "my_disk.db";

    private static volatile LiteOrm sInstance;

    private LiteOrmHelper() {
        // Avoid direct instantiate
    }

    public static LiteOrm getLiteOrmInstance() {
        if (sInstance == null) {
            synchronized (LiteOrmHelper.class) {
                if (sInstance == null) {
                    sInstance = LiteOrm.newCascadeInstance(HeroLib.getInstance().getAppContext(), DB_NAME);
                    sInstance.setDebugged(true);
                }
            }
        }
        return sInstance;
    }
}
