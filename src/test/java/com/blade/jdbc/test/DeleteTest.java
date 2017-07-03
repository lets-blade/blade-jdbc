package com.blade.jdbc.test;

import com.blade.jdbc.test.model.User;
import org.junit.Test;

/**
 * Created by biezhi on 2016/12/25.
 */
public class DeleteTest extends BaseTestCase {

    @Test
    public void test1() {
        User user = new User();
        user.setSql2o(sql2o);

        // delete from t_user where id = ?
        user.where("id", 44).delete();
        user.delete("id", 44);
        user.delete(44);

    }

}
