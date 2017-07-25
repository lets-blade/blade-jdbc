package com.blade.jdbc.test;

import com.blade.jdbc.core.ActiveRecord;
import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 2016/12/25.
 */
public class UpdateTest extends BaseTestCase {

    @Test
    public void test1() {
        User user = new User();
        user.setUsername("jack_up");
        // update t_user set username = ? where id = ?
        user.where("id", 43).update();
    }

    @Test
    public void test2() {
        User user = new User();
        user.setAge(19);
        // update t_user set age = ? where id = ?
        user.update(43);
    }

    @Test
    public void test3() {
        User user = new User();
        user.setAge(32);
        // update t_user set age = ? where age < ?
        user.where("age", "<", 20).update();
    }

    @Test
    public void test4() {
        ActiveRecord activeRecord = new ActiveRecord();
        activeRecord.execute("update t_user set age = 22 where age < 20");
    }

}
