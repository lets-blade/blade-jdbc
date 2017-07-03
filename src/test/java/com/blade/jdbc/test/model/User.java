package com.blade.jdbc.test.model;

import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.annotation.Table;
import lombok.Data;

/**
 * Created by biezhi on 2016/12/25.
 */
@Table(value = "t_user")
@Data
public class User extends ActiveRecord {

    private Integer id;
    private String username;
    private String password;
    private String real_name;

}
