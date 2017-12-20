package com.blade.jdbc.dialect;

import com.blade.jdbc.page.PageRow;

/**
 * @author biezhi
 * @date 2017/12/19
 */
public class Db2Dialect implements Dialect {

    @Override
    public String getPagingSql(String sql, PageRow pageRow) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        sqlBuilder.append("SELECT * FROM (SELECT TMP_PAGE.*,ROWNUMBER() OVER() AS ROW_ID FROM ( ");
        sqlBuilder.append(sql);
        sqlBuilder.append(" ) AS TMP_PAGE) WHERE ROW_ID BETWEEN ");
        sqlBuilder.append(pageRow.getOffset() + 1);
        sqlBuilder.append(" AND ");
        sqlBuilder.append(pageRow.getPosition());
        return sqlBuilder.toString();
    }

}