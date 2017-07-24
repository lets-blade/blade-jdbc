package com.blade.jdbc.test;

import com.blade.jdbc.core.Fields;
import com.blade.jdbc.core.OrderBy;
import com.blade.jdbc.test.model.User;
import org.junit.Test;

import java.util.List;

/**
 * Created by biezhi on 2016/12/25.
 */
public class ReadTest extends BaseTestCase {

    @Test
    public void test1() {
        User       user  = new User();
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
        User u1   = user.find(1);
        System.out.println(u1);
    }

    @Test
    public void test3() {
        User       user = new User();
        List<User> list = user.where("username", "jack").findAll();
        System.out.println(list);

        list = user.findAll(Fields.of("username"), OrderBy.desc("id"));
        System.out.println(list);

    }

    @Test
    public void test4() {
        User user = new User();

        long count = user.count();
        System.out.println(count);
    }

    @Test
    public void test5() {
        User user = new User();

        System.out.println(user.queryAll("select * from t_user"));
        System.out.println(user.queryAll("select * from t_user where id = ?", 1));
    }

    @Test
    public void test6() {
        User user = new User();
        System.out.println(user.query("select * from t_user order by id desc"));
        System.out.println(user.query("select * from t_user where id = ?", 1));
    }

    @Test
    public void test7() {
        User user = new User();

        System.out.println(user.query("select * from t_user order by id desc"));
        System.out.println(user.query("select * from t_user where id = ?", 1));
    }

    @Test
    public void test8() {
        User user = new User();
        user.where("username", "jack").or("real_name", "jack");
        long count = user.count();
        System.out.println("count=" + count);
    }

}