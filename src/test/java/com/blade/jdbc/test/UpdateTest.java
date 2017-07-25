package com.blade.jdbc.test;

import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 2016/12/25.
 */
public class UpdateTest extends BaseTestCase {

    @Test
    public void test2() {

        User user = new User();
        user.setUsername("jack");
        user.setPassword("123556");
        user.setRealName("杰克65");

        user.where("id", 42).update();
    }

}
