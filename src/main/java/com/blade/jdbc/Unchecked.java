package com.blade.jdbc;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Created by biezhi on 03/07/2017.
 */
public class Unchecked {

    public static <T> T wrap(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void wrap(T t, Consumer<T> consumer) {
        try {
            consumer.accept(t);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
