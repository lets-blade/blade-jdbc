package com.blade.jdbc.tx;

import org.sql2o.Connection;

/**
 * Created by biezhi on 2016/12/25.
 */
public interface AtomTx {

    void call(Connection connection);

}
