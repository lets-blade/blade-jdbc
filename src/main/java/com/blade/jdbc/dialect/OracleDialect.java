package com.blade.jdbc.dialect;

import com.blade.jdbc.page.PageRow;

/**
 * @author biezhi
 * @date 2017/12/19
 */
public class OracleDialect implements Dialect {

    @Override
    public String getPagingSql(String sql, PageRow pageRow) {
        StringBuilder sqlBuilder = new StringBuilder(sql.length() + 120);
        if (pageRow.getOffset() > 0) {
            sqlBuilder.append("SELECT * FROM ( ");
        }
        if (pageRow.getPosition() > 0) {
            sqlBuilder.append(" SELECT TMP_PAGE.*, ROWNUM ROW_ID FROM ( ");
        }
        sqlBuilder.append(sql);
        if (pageRow.getPosition() > 0) {
            sqlBuilder.append(" ) TMP_PAGE WHERE ROWNUM <= ");
            sqlBuilder.append(pageRow.getPosition());
        }
        if (pageRow.getOffset() > 0) {
            sqlBuilder.append(" ) WHERE ROW_ID > ");
            sqlBuilder.append(pageRow.getOffset());
        }
        return sqlBuilder.toString();
    }
}
