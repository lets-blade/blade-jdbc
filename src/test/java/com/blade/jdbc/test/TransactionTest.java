package com.blade.jdbc.test;

import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 03/07/2017.
 */
public class TransactionTest extends BaseTestCase {

    @Test
    public void test1() {
        User user = new User();

        user.setPassword("51n2(S24cPc");

        user.atomic(() -> {
            user.update(42);
//            int a = 1 / 0;
            System.out.println("aasdasd");
        });
    }

}
