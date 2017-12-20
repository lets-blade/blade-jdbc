package com.blade.jdbc.dialect;

import com.blade.jdbc.page.PageRow;

/**
 * @author biezhi
 * @date 2017/12/19
 */
public interface Dialect {

    /**
     * 获取分页语句
     *
     * @param sql
     * @param pageRow
     * @return
     */
    String getPagingSql(String sql, PageRow pageRow);

}
