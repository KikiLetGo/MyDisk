package com.elexlab.myalbum.exception;

import java.io.File;

/**
 * Created by bruceyoung on 17-10-19.
 */

public class EncryptionKeyMissMatchException extends Exception{
    private File file;


    public File getFile() {
        return file;
    }

    public EncryptionKeyMissMatchException(String s, File file) {
        super(s);
        this.file = file;
    }

    public EncryptionKeyMissMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncryptionKeyMissMatchException(Throwable cause) {
        super(cause);
    }
}
