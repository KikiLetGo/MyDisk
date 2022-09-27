package com.elexlab.myalbum.pojos;


import com.elexlab.myalbum.managers.AlbumManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruceyoung on 17-10-18.
 */

public class DistAlbums {
    private List<Album> myAlbumList = new ArrayList<>();
    private List<Album> sysAlbumList = new ArrayList<>();

    public List<Album> getMyAlbumList() {
        return myAlbumList;
    }

    public void setMyAlbumList(List<Album> myAlbumList) {
        this.myAlbumList = myAlbumList;
    }

    public List<Album> getSysAlbumList() {
        return sysAlbumList;
    }

    public void setSysAlbumList(List<Album> sysAlbumList) {
        this.sysAlbumList = sysAlbumList;
    }

    public List<Album> getAllAlbumList(){
        List<Album> albumList = new ArrayList<Album>();
        albumList.addAll(myAlbumList);
        albumList.addAll(sysAlbumList);
        return albumList;
    }

    public void addAlbum(Album album){
        myAlbumList.add(0,album);
    }

    public void removeAlbum(Album album){
        int oldAlbumIndex = -1;
        for(int i=0;i<myAlbumList.size();i++){
            Album oldAlbum = myAlbumList.get(i);
            if(album.equals(oldAlbum)){
                oldAlbumIndex = i;
                break;
            }
        }
        if(oldAlbumIndex != -1){
            myAlbumList.remove(oldAlbumIndex);
        }
    }

    public void updateAlbum(Album album){
        int oldAlbumIndex = -1;
        for(int i=0;i<myAlbumList.size();i++){
            Album oldAlbum = myAlbumList.get(i);
            if(album.equals(oldAlbum)){
                oldAlbumIndex = i;
                break;
            }
        }
        if(oldAlbumIndex != -1){
            myAlbumList.remove(oldAlbumIndex);
            myAlbumList.add(oldAlbumIndex,album);
        }
    }


    public void addMedia(Media media){
//        for(Album album:myAlbumList){
//            if(album.getId() == param1){
//                album.addMedia((int) param2,media);
//            }
//        }
    }
    public void removeMedia(Media media){
        boolean found = removeMediaFormAlbum(media,myAlbumList);
        if(!found){
            removeMediaFormAlbum(media,sysAlbumList);
        }
    }

    private boolean removeMediaFormAlbum(Media media,List<Album> albumList){
        Album albumNeedDelete = null;
        boolean found = false;
        for(Album album:albumList){
            for(Media albumMedia :album.getMediaList()){
                if(albumMedia.equals(media)){
                    album.getMediaList().remove(albumMedia);
                    if(album.getMediaList().size() <= 0){
                        albumNeedDelete = album;
                    }
                    found = true;
                    break;
                }
            }
        }
        if(albumNeedDelete != null){
            if(albumNeedDelete.getType() == Album.AlbumType.MINE){
                try {
                    AlbumManager.getInstance().deleteAlbum(albumNeedDelete,Album.DeletePhotoMode.NO_RECOVRE,null);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            albumList.remove(albumNeedDelete);
        }
        return found;
    }
}
