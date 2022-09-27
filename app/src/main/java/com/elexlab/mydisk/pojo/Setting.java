package com.elexlab.mydisk.pojo;

import android.content.Intent;
import android.text.TextUtils;

import com.elexlab.mydisk.ui.settings.SettingActivity;
import com.litesuits.orm.db.annotation.NotNull;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

public class Setting {
    @NotNull
    @PrimaryKey(AssignType.BY_MYSELF)
    private String id = "ONLY";
    private String endpoint;
    private String ak;
    private String sk;
    private String bucketName;

    public Setting() {
    }

    public Setting(String endpoint, String ak, String sk, String bucketName) {
        this.id = id;
        this.endpoint = endpoint;
        this.ak = ak;
        this.sk = sk;
        this.bucketName = bucketName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public boolean valid(){
        if(TextUtils.isEmpty(endpoint)||
                TextUtils.isEmpty(ak)||
                TextUtils.isEmpty(sk)||
                TextUtils.isEmpty(bucketName)){

            return false;
        }else{
            return true;
        }
    }
}
