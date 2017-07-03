package com.blade.jdbc;

/**
 * Created by biezhi on 03/07/2017.
 */
@FunctionalInterface
public interface Tx {

    void run();

}
