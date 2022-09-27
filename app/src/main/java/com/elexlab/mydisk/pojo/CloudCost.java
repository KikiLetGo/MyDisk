package com.elexlab.mydisk.pojo;

import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

public class CloudCost {
    @NotNull
    @PrimaryKey(AssignType.BY_MYSELF)
    private String id;
    private long volume;
    private long requestCount;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(long requestCount) {
        this.requestCount = requestCount;
    }
}
