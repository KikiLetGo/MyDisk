package com.elexlab.myalbum.exception;

/**
 * Created by bruceyoung on 17-10-11.
 */

public class AlbumDuplicateException extends IllegalStateException{
    public AlbumDuplicateException(){
        super();
    }
    public AlbumDuplicateException(String message){
        super(message);
    }
}
