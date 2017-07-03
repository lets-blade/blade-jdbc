package com.blade.jdbc.test;

import com.blade.jdbc.OrderBy;
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

        user.setId(1);
        users = user.findAll();
        System.out.println(users);

        user.where("id", 2);
        users = user.findAll();
        System.out.println(users);

        user.where("id", "<", 2);
        users = user.findAll();
        System.out.println(users);
    }

    @Test
    public void test2() {
        User user = new User();
        user.setSql2o(sql2o);

        User u1 = user.find(1);
        System.out.println(u1);
    }

    @Test
    public void test3() {
        User user = new User();
        user.setSql2o(sql2o);

        List<User> list = user.where("username", "jack").findAll();
        System.out.println(list);

        list = user.findAll(OrderBy.desc("id"));
        System.out.println(list);

    }

    @Test
    public void test4() {
        User user = new User();
        user.setSql2o(sql2o);

        long count = user.count();
        System.out.println(count);
    }

    @Test
    public void test5() {
        User user = new User();
        user.setSql2o(sql2o);

        System.out.println(user.queryAll("select * from t_user"));
        System.out.println(user.queryAll("select * from t_user where id = :p1", 1));
    }

    @Test
    public void test6() {
        User user = new User();
        user.setSql2o(sql2o);

        System.out.println(user.query("select * from t_user order by id desc"));
        System.out.println(user.query("select * from t_user where id = :p1", 1));
    }

}
