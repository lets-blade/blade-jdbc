package com.blade.jdbc.utils;

/**
 * Created by biezhi on 2016/12/25.
 */
public class StringUtils {

    public static boolean isNotBlank(String str){
        return null != str && !str.equals("");
    }

    public static boolean isBlank(String str) {
        return null == str || str.equals("");
    }

    public static String trim(String str) {
        return null != str ? str.trim() : str;
    }
}
