package com.blade.jdbc.utils;

import java.util.Collection;

/**
 * Created by Administrator on 2016/11/29.
 */
public class CollectionUtils {

    public static boolean isEmpty(Collection collection){
        return null == collection || collection.size() == 0;
    }
}
