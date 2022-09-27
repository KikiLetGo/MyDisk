package com.elexlab.myalbum.utils;



import com.elexlab.myalbum.pojos.Album;
import com.elexlab.myalbum.pojos.Media;

import java.util.List;

/**
 * Created by BruceYoung on 10/11/17.
 */
public class AlbumAndPhotoUtils {

    public static void sortAlbumByFileCreateTime(Album album){
        List<Media> mediaList = album.getMediaList();
        for(int i = 0; i< mediaList.size()-1; i++){
            for(int j = 0; j< mediaList.size()-1-i; j++){
                Media media1 = mediaList.get(j);
                Media media2 = mediaList.get(j+1);
                if(media1.getFile() == null || media2.getFile() == null){
                    EasyLog.e("AlbumAndPhotoUtils","mediaFile is null");
                }
                if(media1.getLastModify() < media2.getLastModify()){
                    //swap
                    mediaList.set(j, media2);
                    mediaList.set(j+1, media1);
                }
            }
        }
    }

    public static void sortAlbums(List<Album> albumList){
//        for(int i = 0; i< albumList.size()-1; i++){
//            for(int j = 0; j< albumList.size()-1-i; j++){
//                Album album1 = albumList.get(j);
//                Album album2 = albumList.get(j+1);
//
//                if(album1.getCreateTime() < album2.getCreateTime()){
//                    //swap
//                    albumList.set(j, album2);
//                    albumList.set(j+1, album1);
//                }
//            }
//        }

        for(int i = 0; i< albumList.size()-1; i++){
            for(int j = 0; j< albumList.size()-1-i; j++){
                Album album1 = albumList.get(j);
                Album album2 = albumList.get(j+1);
                if(album1.getProp() == null || album2.getProp() == null){
                    continue;
                }

                if(album1.getProp().getSort() > album2.getProp().getSort()){
                    //swap
                    albumList.set(j, album2);
                    albumList.set(j+1, album1);
                }
            }
        }
    }

    public static void sortSystemAlbums(List<Album> albumList){
        for(int i = 0; i< albumList.size()-1; i++){
            for(int j = 0; j< albumList.size()-1-i; j++){
                Album album1 = albumList.get(j);
                Album album2 = albumList.get(j+1);
                if(album1.getSize() <
                        album2.getSize()){
                    //swap
                    albumList.set(j, album2);
                    albumList.set(j+1, album1);
                }
            }
        }
        //camera
        for(int i=0;i<albumList.size();i++){
            Album album = albumList.get(i);
            if("Camera".equals(album.getName())){
                albumList.remove(i);
                albumList.add(0,album);
                break;
            }
        }
    }
}
