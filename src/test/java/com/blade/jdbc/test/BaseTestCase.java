package com.blade.jdbc.test;

import org.junit.Before;
import org.sql2o.Sql2o;

/**
 * Created by biezhi on 03/07/2017.
 */
public abstract class BaseTestCase {

    protected Sql2o sql2o;

    @Before
    public void before() {
        this.sql2o = new Sql2o("jdbc:mysql://localhost:3306/demo", "root", "123456");
    }

}
