package com.blade.jdbc.test;

import com.blade.jdbc.Base;
import org.junit.Before;

/**
 * Created by biezhi on 03/07/2017.
 */
public abstract class BaseTestCase {

    @Before
    public void before() {
        Base.open("jdbc:mysql://localhost:3306/demo", "root", "123456");
    }

}
