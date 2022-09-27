package com.elexlab.myalbum.encryption;


import com.elexlab.myalbum.MyAlbum;
import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.managers.PasswordManager;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.SecretKey;

/**
 * Created by bruceyoung on 17-10-9.
 */

public abstract class FileEncryption {
    public abstract SecretKey createSecureKey(String key);
    public interface CodecListener{
        void onProcess(float progress);
    }

    public abstract void encrypt(String key,File fromFile,File destFile,CodecListener codecListener) throws EncryptionKeyMissMatchException;

    public abstract void decrypt(String key,File fromFile,File destFile,CodecListener codecListener) throws EncryptionKeyMissMatchException;


    public abstract boolean detectKeyMatched(String key,File fromFile);
    private static FileEncryption fileEncryption = new DESFileEncryption();
    public static FileEncryption getFileEncryption(){
        return fileEncryption;
    }

    public abstract OutputStream getEncryptedOutputStream(OutputStream outputStream);
    public abstract InputStream getDecryptedInputStream(InputStream inputStream);

    public String loadPassword(){
        String password = PasswordManager.getInstance().readPassword(MyAlbum.getContext());
        if(password == null){
            password = "";
        }
        return password;
    }
}
