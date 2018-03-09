package com.blade.jdbc.test;

import com.blade.jdbc.core.Fields;
import com.blade.jdbc.core.Limit;
import com.blade.jdbc.core.OrderBy;
import com.blade.jdbc.test.model.User;
import org.junit.Test;

import java.util.Arrays;
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
        List<User> users = user.findAll(Limit.of(5,5),OrderBy.desc("id"));
        System.out.println(users);
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
        User user  = new User();
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

        List<User> list = user.queryAll("select * from t_user order by id desc");
        System.out.println(list);

        User one = user.query("select * from t_user where id = ?", 1);
        System.out.println(one);
    }

    @Test
    public void test8() {
        User user = new User();
        user.where("username", "jack").or("real_name", "jack");
        long count = user.count();
        System.out.println("count=" + count);
    }

    @Test
    public void testLike() {
        User       user  = new User();
        List<User> users = user.like("username", "%jac%").and("age", ">", 18).findAll();
        System.out.println(users);
    }

    @Test
    public void testIn() {
        User          user  = new User();
        List<Integer> ids   = Arrays.asList(1, 2, 3, 4);
        List<User>    users = user.in("id", ids).findAll();
        System.out.println(users);
    }

    @Test
    public void testIsNull() {
        new User().isNull("username").count();
        new User().gt("age", 10).isNull("username").count();
    }

    @Test
    public void testIsNotNull() {
        new User().isNotNull("username").count();
        new User().gt("age", 10).isNotNull("username").count();
    }

}