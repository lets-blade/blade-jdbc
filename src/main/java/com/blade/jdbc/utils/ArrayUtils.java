package com.blade.jdbc.utils;

/**
 * @author ifonly
 * @version 1.0 2015-12-06 21:33
 * @since JDK 1.6
 */
public class ArrayUtils {

    public static <T> boolean isNullOrEmpty(T[] arr) {
        return null == arr || arr.length == 0;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] merge(T[] arr1, T[] arr2) {
        if (!isNullOrEmpty(arr1) && !isNullOrEmpty(arr2)) {
            int arr1Len = arr1.length;
            int arr2Len = arr2.length;
            int len = arr1Len + arr2Len;
            T[] arr = (T[]) new Object[len];
            int index = 0;
            for (int i = 0; i < arr1Len; i++) {
                arr[index++] = arr1[i];
            }
            for (int i = 0; i < arr2Len; i++) {
                arr[index++] = arr2[i];
            }

            return arr;
        }

        if (!isNullOrEmpty(arr1)) {
            return arr1;
        }
        if (!isNullOrEmpty(arr2)) {
            return arr2;
        }
        return (T[]) new Object[0];
    }

}
