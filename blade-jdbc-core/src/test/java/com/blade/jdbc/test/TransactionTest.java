package com.blade.jdbc.test;

import com.blade.jdbc.Base;
import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 03/07/2017.
 */
public class TransactionTest extends BaseTestCase {

    @Test
    public void test1() throws Exception {
        Base.atomic(() -> {
            User user = new User();
            user.setUsername("223");
            user.setPassword("999");
            user.setAge(28);
            user.save();
            int a = 1 / 0;
            System.out.println("aasdasd");

            user = new User();
            user.setUsername("2234");
            user.setPassword("9998");
            user.setAge(26);
            user.save();
            return true;
        });
    }

}
