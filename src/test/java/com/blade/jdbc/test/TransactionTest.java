package com.blade.jdbc.test;

import com.blade.jdbc.Base;
import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 03/07/2017.
 */
public class TransactionTest extends BaseTestCase {

    @Test
    public void test1() {
        Base.atomic(() -> {
            User user = new User();
            user.setPassword("999");
            user.update(42);
//            int a = 1 / 0;
            System.out.println("aasdasd");
            return true;
        });
    }

}
