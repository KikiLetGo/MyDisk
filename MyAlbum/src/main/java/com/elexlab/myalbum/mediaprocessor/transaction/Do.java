package com.elexlab.myalbum.mediaprocessor.transaction;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.elexlab.myalbum.pojos.Media;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by BruceYoung on 10/24/17.
 */
public abstract class Do {
    public interface Action{
        int RE_ENCRYPTION = 1;
    }

    private Media media;
    private int action;
    protected File logFile;
    protected int checkpoint;
    private Context context;


    public Do(){

    }
    public Do(Media media,int action) {
        this.media = media;
        this.action = action;
    }

    public Media getMedia() {
        return media;
    }

    public void setMedia(Media media) {
        this.media = media;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoint(int checkpoint) {
        this.checkpoint = checkpoint;
    }

    public void start(Context context){
        this.context = context;
        saveDo();
    }
    public void updateCheckpoint(int checkpoint){
        this.checkpoint = checkpoint;
        saveDo();
    }

    protected void saveDo(){
        if(logFile == null){
            logFile = new File(getLogFilePath());
        }
        String json = JSON.toJSONString(this);
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(logFile);
            fileWriter.write(json);
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fileWriter != null){
                try {
                    fileWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected String getLogFilePath(){
        String dirPath = getLogPath(context);
        String logPath = dirPath + "/" + System.currentTimeMillis()+".log";
        return logPath;
    }
    public abstract void rollback(Context context);
    public abstract void commit();
    public abstract String getLogPath(Context context);

}
