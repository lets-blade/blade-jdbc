package com.blade.jdbc.test;

import com.blade.jdbc.page.Page;
import com.blade.jdbc.page.PageRow;
import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 2016/12/25.
 */
public class PageTest extends BaseTestCase {

    @Test
    public void test1() {
        User       user = new User();
        Page<User> page = user.page(1, 3);
        System.out.println(page);

        page = user.page(1, 3, "age DESC");
        System.out.println(page);
    }

    @Test
    public void test2() {
        User       user = new User();
        Page<User> page = user.page(new PageRow(1, 2));
        System.out.println(page);
    }

    @Test
    public void test3() {
//        User       user = new User();
//        Page<User> page = user.page(new PageRow(1, 2), "select * from t_user where 1 = ? ", 1);
//        System.out.println(page);
    }

    @Test
    public void test4() {
        User         user         = new User();
        Page<User>   page         = user.page(1, 10);
        Page<String> userNamePage = page.map(User::getUsername);
        System.out.println(userNamePage);
    }

}