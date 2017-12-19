package com.blade.jdbc.dialect;

import com.blade.jdbc.page.PageRow;

/**
 * @author biezhi
 * @date 2017/12/19
 */
public class MySQLDialect implements Dialect {

    @Override
    public String getPagingSql(String sql, PageRow pageRow) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 14);
        sqlBuilder.append(sql);
        if (pageRow.getOffset() == 0) {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(pageRow.getLimit());
        } else {
            sqlBuilder.append(" LIMIT ");
            sqlBuilder.append(pageRow.getOffset());
            sqlBuilder.append(",");
            sqlBuilder.append(pageRow.getLimit());
        }
        return sqlBuilder.toString();
    }

}
