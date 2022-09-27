package com.elexlab.myalbum.mediaprocessor.transaction;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.elexlab.myalbum.pojos.Media;
import com.elexlab.myalbum.utils.PathUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by BruceYoung on 10/24/17.
 */
public class Redo extends Do{

    public Redo(){

    }

    public Redo(Media media, int action) {
        super(media, action);
    }

    @Override
    public void rollback(Context context) {

    }

    @Override
    public void commit() {
        if(logFile != null && logFile.exists()){
            logFile.delete();
            logFile.deleteOnExit();
        }
    }

    @Override
    public String getLogPath(Context context) {
        return PathUtils.getRedoLogPath(context);
    }

    public static List<Redo> readRedos(Context context){
        File redosFileDir = new File(PathUtils.getRedoLogPath(context));
        File[] files = redosFileDir.listFiles();
        if(files == null || files.length<=0){
            return null;
        }
        List<Redo> redoList = new ArrayList<>();
        for(File file:files){
            try {
                String json = "";
                BufferedReader fileReader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = fileReader.readLine())!=null){
                    json += line;
                }
                Redo redo = JSON.parseObject(json,Redo.class);
                redoList.add(redo);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return redoList;
    }
}
