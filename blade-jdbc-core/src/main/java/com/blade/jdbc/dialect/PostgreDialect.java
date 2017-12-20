package com.blade.jdbc.dialect;

import com.blade.jdbc.page.PageRow;

/**
 * @author biezhi
 * @date 2017/12/19
 */
public class PostgreDialect implements Dialect {

    @Override
    public String getPagingSql(String sql, PageRow pageRow) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 20);
        sqlBuilder.append(sql);
        sqlBuilder.append(" LIMIT ");
        sqlBuilder.append(pageRow.getOffset());
        sqlBuilder.append("OFFSET ");
        sqlBuilder.append(pageRow.getLimit());
        return sqlBuilder.toString();
    }
}
