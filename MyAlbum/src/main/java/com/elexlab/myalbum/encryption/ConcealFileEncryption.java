package com.elexlab.myalbum.encryption;//package com.heroescape.mindinalbum.core;
//
//import com.facebook.android.crypto.keychain.AndroidConceal;
//import com.facebook.android.crypto.keychain.SharedPrefsBackedKeyChain;
//import com.facebook.crypto.Crypto;
//import com.facebook.crypto.CryptoConfig;
//import com.facebook.crypto.Entity;
//import com.facebook.crypto.exception.CryptoInitializationException;
//import com.facebook.crypto.exception.KeyChainException;
//import com.facebook.crypto.keychain.KeyChain;
//import com.heroescape.heroescapelib.HeroLib;
//import PasswordManager;
//
//import java.io.BufferedInputStream;
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
///**
// * Created by BruceYoung on 10/15/17.
// */
//public class ConcealFileEncryption extends FileEncryption{
//    @Override
//    public void encrypt(File fromFile, File destFile) {
//        String pass = loadPassword();
//        // Creates a new Crypto object with default implementations of a key chain
//        KeyChain keyChain = new SharedPrefsBackedKeyChain(HeroLib.getInstance().appContext, CryptoConfig.KEY_256);
//        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
//
//        if (!crypto.isAvailable()) {
//            return;
//        }
//
//        try {
//            OutputStream fileStream = new BufferedOutputStream(
//                    new FileOutputStream(destFile));
//
//            // Creates an output stream which encrypts the data as
//            // it is written to it and writes it out to the file.
//            OutputStream outputStream = crypto.getCipherOutputStream(
//                    fileStream,
//                    Entity.create("pass"));
//
//            FileInputStream is = new FileInputStream(fromFile);
//
//            byte[] buffer = new byte[1024];
//            int r;
//            while ((r = is.read(buffer)) >= 0) {
//                outputStream.write(buffer, 0, r);
//            }
//            outputStream.close();
//        }catch (Exception e){
//
//        }
//
//    }
//
//    @Override
//    public void decrypt(File fromFile, File destFile) {
//        String pass = loadPassword();
//        // Creates a new Crypto object with default implementations of a key chain
//        KeyChain keyChain = new SharedPrefsBackedKeyChain(HeroLib.getInstance().appContext, CryptoConfig.KEY_256);
//        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
//
//        if (!crypto.isAvailable()) {
//            return;
//        }
//
//        try {
//            InputStream fileStream = new BufferedInputStream(
//                    new FileInputStream(fromFile));
//
//            // Creates an output stream which encrypts the data as
//            // it is written to it and writes it out to the file.
//            InputStream inputStream = crypto.getCipherInputStream(
//                    fileStream,
//                    Entity.create(pass));
//
//            FileOutputStream outputStream = new FileOutputStream(destFile);
//
//            byte[] buffer = new byte[1024];
//            int r;
//            while ((r = inputStream.read(buffer)) >= 0) {
//                outputStream.write(buffer, 0, r);
//            }
//            outputStream.close();
//        }catch (Exception e){
//
//        }
//    }
//
//    @Override
//    public OutputStream getEncryptedOutputStream(OutputStream outputStream) {
//        return null;
//    }
//
//    @Override
//    public InputStream getDecryptedInputStream(InputStream inputStream) {
//        String pass = loadPassword();
//        // Creates a new Crypto object with default implementations of a key chain
//        KeyChain keyChain = new SharedPrefsBackedKeyChain(HeroLib.getInstance().appContext, CryptoConfig.KEY_256);
//        Crypto crypto = AndroidConceal.get().createDefaultCrypto(keyChain);
//
//        if (!crypto.isAvailable()) {
//            return null;
//        }
//        try {
//            return crypto.getCipherInputStream(
//                    inputStream,
//                    Entity.create(pass));
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (CryptoInitializationException e) {
//            e.printStackTrace();
//        } catch (KeyChainException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
