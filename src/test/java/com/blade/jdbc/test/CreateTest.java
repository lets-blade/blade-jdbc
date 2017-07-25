package com.blade.jdbc.test;

import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 2016/12/25.
 */
public class CreateTest extends BaseTestCase {

    @Test
    public void test1() {
        User user = new User();
        user.setUsername("jack");
        user.setPassword("123556");
        user.setRealName("杰克");
        user.setAge(20);

        // insert into t_user (id, username, password, age, real_name) values (?, ?, ?, ?, ?)
        user.save();
    }


}
