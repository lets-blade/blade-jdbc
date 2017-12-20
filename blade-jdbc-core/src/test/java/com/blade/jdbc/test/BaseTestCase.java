package com.blade.jdbc.test;

import com.blade.jdbc.Base;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Before;

/**
 * Created by biezhi on 03/07/2017.
 */
public class BaseTestCase {

    @Before
    public void before() {
//        HikariConfig config =this.mysql();
        HikariConfig     config = this.pgsql();
        HikariDataSource ds     = new HikariDataSource(config);
        Base.open(ds);
    }

    private HikariConfig mysql() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/demo");
        config.setUsername("root");
        config.setPassword("123456");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }

    private HikariConfig pgsql() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://127.0.0.1:5432/demo");
        config.setUsername("biezhi");
        config.setPassword("123456");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return config;
    }

}
