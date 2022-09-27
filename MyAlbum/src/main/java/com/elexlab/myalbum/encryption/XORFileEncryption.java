package com.elexlab.myalbum.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.SecretKey;

/**
 * Created by BruceYoung on 10/10/17.
 */
public class XORFileEncryption extends FileEncryption{
    @Override
    public SecretKey createSecureKey(String key) {
        return null;
    }

    @Override
    public void encrypt(String key,File fromFile,File destFile,CodecListener codecListener){
        xorCompute(fromFile,destFile);
    }

    @Override
    public void decrypt(String key,File fromFile, File destFile,CodecListener codecListener) {
        xorCompute(fromFile,destFile);
    }

    @Override
    public boolean detectKeyMatched(String key, File fromFile) {
        return false;
    }

    @Override
    public OutputStream getEncryptedOutputStream(OutputStream outputStream) {
        return null;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream) {
        return null;
    }

    private void xorCompute(File fromFile,File destFile){
        if(fromFile == null || !fromFile.exists()){
            return;
        }
        String password = loadPassword();
        byte[] passwordBytes = password.getBytes();
        try {
            FileInputStream fileInputStream = new FileInputStream(fromFile);
            FileOutputStream fileOutputStream = new FileOutputStream(destFile);
            byte[] bytes = new byte[1024];
            int index;
            while ((index = fileInputStream.read(bytes)) != -1){
                for(int i=0;i<index;i++){
                    for(byte encryptionByte:passwordBytes){
                        bytes[i] = (byte) (bytes[i] ^ encryptionByte);
                    }
                }
                fileOutputStream.write(bytes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
