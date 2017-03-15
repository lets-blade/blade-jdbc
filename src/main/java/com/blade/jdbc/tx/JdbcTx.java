package com.blade.jdbc.tx;

import org.sql2o.Connection;


/**
 * Created by biezhi on 2017/3/15.
 */
public class JdbcTx {

    private Connection connection;
    private boolean autoCommit;

    public JdbcTx(Connection connection, boolean autoCommit) {
        this.connection = connection;
        this.autoCommit = autoCommit;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }
}