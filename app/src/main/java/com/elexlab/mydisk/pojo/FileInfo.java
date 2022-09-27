package com.elexlab.mydisk.pojo;

import com.elexlab.myalbum.pojos.Media;
import com.elexlab.mydisk.cloud.Client;
import com.elexlab.mydisk.constants.Constants;
import com.elexlab.mydisk.manager.MultiDeviceManager;

import java.io.File;

public class FileInfo extends HttpPojo{
    public interface FileType{
        String DIR = "dir";
        String DOCUMENT = "document";
    }
    public interface StoreLocation{
        int MIRROR = 0;
        int LOCAL = MIRROR+1;
        int LOCAL_MIRROR = LOCAL+1;
        int LOCAL_MIRROR_RECOVERY = LOCAL_MIRROR+1;
    }
    private String name;
    private String dir;
    private int storeLocation;
    private String fileType;

    public FileInfo(){

    }
    public FileInfo(String path){
        File file = new File(path);
        name = file.getName();
        dir = file.getParent();
        if(file.isDirectory()){
            fileType = FileType.DIR;
        }else{
            fileType = FileType.DOCUMENT;
        }
        storeLocation = StoreLocation.LOCAL;

    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public int getStoreLocation() {
        return storeLocation;
    }

    public void setStoreLocation(int storeLocation) {
        this.storeLocation = storeLocation;
    }

    public String getPath(){
        String path = dir;
        if(!dir.endsWith("/")){
            path+=File.separator;
        }
        path += name;
        return path;
    }

    public String getUrl(){
//        String url = Constants.getDownloadFileUrl()+getDir()+"/&filename="+name;;

        String url = Client.getInstance().tempUrl(MultiDeviceManager.getInstance().getCurrentDevice().getDeviceCode()+File.separator+getPath(),3600);
        return url;
    }



    public boolean isDir(){
        return FileType.DIR.equals(fileType);
    }
    public boolean isFile(){
        return FileType.DOCUMENT.equals(fileType);
    }



    @Override
    public boolean equals(Object obj) {
        if(obj == null||!(obj instanceof FileInfo)){
            return super.equals(obj);
        }
        FileInfo outter = (FileInfo) obj;
        return getPath().equals(outter.getPath());
    }


    public static FileInfo media2FileInfo(Media media){
        FileInfo fileInfo = new FileInfo();
        fileInfo.setName(media.getFile().getName());
        fileInfo.setDir(media.getFile().getParent());
        fileInfo.setFileType(FileInfo.FileType.DOCUMENT);
        fileInfo.setStoreLocation(FileInfo.StoreLocation.LOCAL);
        return fileInfo;

    }

    public static Media fileInfo2Media(FileInfo fileInfo){
        Media media = new Media();
        media.setDisplayName(fileInfo.getName());
        media.setFile(new File(fileInfo.getPath()));
        media.setTitle(fileInfo.getName());
        return media;

    }

}
