package com.blade.jdbc.dialect;

import com.blade.jdbc.Model;

public interface Dialect {

	String getSaveSql(Model model);

	String getUpdateSql(Model model);

	String getDeleteSql(Model model);

	String getQuerySql(String sql, Model model);

	String getQueryOneSql(String sql, Model model);

	String getQueryCountSql(String sql, Model model);

}