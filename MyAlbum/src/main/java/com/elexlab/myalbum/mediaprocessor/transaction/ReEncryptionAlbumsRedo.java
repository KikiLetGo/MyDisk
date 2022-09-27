package com.elexlab.myalbum.mediaprocessor.transaction;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

/**
 * Created by BruceYoung on 11/8/17.
 */
public class ReEncryptionAlbumsRedo extends Redo{
    public ReEncryptionAlbumsRedo(){

    }
    public ReEncryptionAlbumsRedo(Media media, int action) {
        super(media, action);
    }

    private String oldPass;
    private String newPass;
    private Map<String,String> fileMappings;

    public String getOldPass() {
        return oldPass;
    }

    public void setOldPass(String oldPass) {
        this.oldPass = oldPass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public Map<String, String> getFileMappings() {
        return fileMappings;
    }

    public void setFileMappings(Map<String, String> fileMappings) {
        this.fileMappings = fileMappings;
    }

    public static ReEncryptionAlbumsRedo readRedo(Context context){
        String dirPath = PathUtils.getRedoLogPath(context);
        String logPath = dirPath + "/re_encryption.log";
        File file = new File(logPath);
        if(!file.exists()){
            return null;
        }
        String json = "";
        BufferedReader fileReader = null;
        try {
            fileReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = fileReader.readLine())!=null){
                json += line;
            }
            ReEncryptionAlbumsRedo redo = JSON.parseObject(json,ReEncryptionAlbumsRedo.class);

            return redo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    protected String getLogFilePath(){
        String dirPath = getLogPath(MyAlbum.getContext());
        String logPath = dirPath + "/re_encryption.log";
        return logPath;
    }
}
