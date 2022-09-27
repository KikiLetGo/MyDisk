package com.elexlab.myalbum.encryption;

import com.elexlab.myalbum.exception.EncryptionKeyMissMatchException;
import com.elexlab.myalbum.utils.EasyLog;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by BruceYoung on 10/15/17.
 */
public class DESFileEncryption extends FileEncryption{
    private static final String TAG = DESFileEncryption.class.getSimpleName();
    private SecretKey securekey;
    private String password;
    public DESFileEncryption() {
        //password = loadPassword();
        //createSecureKey(password);
    }

    @Override
    public SecretKey createSecureKey(String key){
        try {
            byte[] bytes = key.getBytes("UTF-8");//str2HexStr(key).getBytes();
            EasyLog.d(TAG,"bytes length:"+bytes.length);
            //auto fill
            if(bytes.length<8){
                byte[] keyBytes = new byte[8];
                for(int i=0;i<bytes.length;i++){
                    keyBytes[i] = bytes[i];
                }
                bytes = keyBytes;
            }
            DESKeySpec desKeySpec = new DESKeySpec(bytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            securekey = keyFactory.generateSecret(desKeySpec);
            EasyLog.i(TAG,securekey.toString());
            return securekey;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing SqlMap class. Cause: " + e);
        }
    }

//    private static String str2HexStr(String str){
//        char[] chars = "0123456789ABCDEF".toCharArray();
//        StringBuilder sb = new StringBuilder("");
//        byte[] bs = str.getBytes();
//        int bit;
//        for (int i = 0; i < bs.length; i++)
//        {
//            bit = (bs[i] & 0x0f0) >> 4;
//            sb.append(chars[bit]);
//            bit = bs[i] & 0x0f;
//            sb.append(chars[bit]);
//            sb.append(' ');
//        }
//        String hexStr =  sb.toString().trim();
//        HeroLog.i(TAG,hexStr);
//        return hexStr;
//    }

    @Override
    public void encrypt(String key,File fromFile, File destFile,CodecListener codecListener) throws EncryptionKeyMissMatchException {
        createSecureKey(key);
        CipherOutputStream cos = null;
        InputStream is = null;
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, this.securekey);
            is = new FileInputStream(fromFile);
            OutputStream out = new FileOutputStream(destFile);
            cos = new CipherOutputStream(out, cipher);
            long allSize = fromFile.length();
            long currentSize = 0;
            byte[] buffer = new byte[1024*1024];
            int r;
            while ((r = is.read(buffer)) >= 0) {
                cos.write(buffer, 0, r);
                currentSize += r;
                float progress = (float)currentSize/(float)allSize;
                if(codecListener != null){
                    codecListener.onProcess(progress);
                }
            }
            cos.flush();
        } catch (FileNotFoundException e){
            EasyLog.e(TAG,e.getMessage());
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        }catch (NoSuchPaddingException e) {
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        } finally {
            try {
                if(cos != null){
                    cos.close();
                }
                if(is != null){
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void decrypt(String key,File fromFile, File destFile,CodecListener codecListener) throws EncryptionKeyMissMatchException{
        createSecureKey(key);
        MindInCipherInputStream cis = null;
        OutputStream out = null;
        try {
            Cipher cipher = Cipher.getInstance("DES");
            // cipher.init(Cipher.ENCRYPT_MODE, getKey());
            cipher.init(Cipher.DECRYPT_MODE, this.securekey);
            InputStream is = new FileInputStream(fromFile);
            out = new FileOutputStream(destFile);
            cis = new MindInCipherInputStream(is, cipher);
            long allSize = fromFile.length();
            long currentSize = 0;
            byte[] buffer = new byte[1024*1024];
            int r;
            while ((r = cis.read(buffer)) > 0) {
                out.write(buffer, 0, r);
                currentSize += r;
                float progress = (float)currentSize/(float)allSize;
                if(codecListener != null){
                    codecListener.onProcess(progress);
                }
            }
            out.flush();
        } catch (FileNotFoundException e){
            EasyLog.e(TAG,e.getMessage());
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            throw new EncryptionKeyMissMatchException("Password:'"+password+"' miss match",fromFile);
        }finally {
            try {

                if(cis != null){
                    cis.close();
                }
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean detectKeyMatched(String key, File fromFile) {
        createSecureKey(key);
        CipherInputStream cis = null;
        OutputStream out = null;
        try {
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, this.securekey);
            InputStream is = new FileInputStream(fromFile);
            cis = new CipherInputStream(is, cipher);
            cipher.doFinal();
        } catch (FileNotFoundException e){
            EasyLog.e(TAG,e.getMessage());
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
            return false;
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return false;
        } catch (BadPaddingException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {

                if(cis != null){
                    cis.close();
                }
                if(out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public OutputStream getEncryptedOutputStream(OutputStream outputStream) {
        try {
            Cipher encryptionCipher = Cipher.getInstance("DES");
            encryptionCipher.init(Cipher.ENCRYPT_MODE, this.securekey);
            return new CipherOutputStream(outputStream,encryptionCipher);

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InputStream getDecryptedInputStream(InputStream inputStream) {
        try {
            password = loadPassword();
            createSecureKey(password);
            Cipher decryptionCipher = Cipher.getInstance("DES");
            decryptionCipher.init(Cipher.DECRYPT_MODE,this.securekey);
            InputStream cipherInputStream = new CipherInputStream(inputStream,decryptionCipher);

            return cipherInputStream;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


}
