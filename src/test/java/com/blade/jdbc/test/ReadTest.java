package com.blade.jdbc.test;

import com.blade.jdbc.test.model.User;
import org.junit.Test;

import java.util.List;

/**
 * Created by biezhi on 2016/12/25.
 */
public class ReadTest extends BaseTestCase {

    @Test
    public void test1() {
        User user = new User();
        user.setSql2o(sql2o);

        List<User> users = user.findAll();
        System.out.println(users);
    }

    @Test
    public void test2() {
        User user = new User();
        user.setSql2o(sql2o);

        User u1 = user.find(1);
        System.out.println(u1);
    }

}
