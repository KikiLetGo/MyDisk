package com.elexlab.myalbum.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by BruceYoung on 1/7/17.
 */
public class StringUtils {
    public static boolean isNotNullAndEmpty(String str){
        return str != null && !str.isEmpty();
    }
    public static String utf8Encode(String str){
        try {
            return  URLEncoder.encode(str,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        }
    }
}
