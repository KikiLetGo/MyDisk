package com.elexlab.mydisk.pojo;

import androidx.annotation.Nullable;

import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.utils.DeviceUtils;

public class Device {
    private String id;
    private String name;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceCode(){
        return name +"#"+ id;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null || !(obj instanceof Device)){
            return super.equals(obj);
        }
        return getDeviceCode().equals(((Device) obj).getDeviceCode());
    }
}
