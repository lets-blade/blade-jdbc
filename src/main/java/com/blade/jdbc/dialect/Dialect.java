package com.blade.jdbc.dialect;

import com.blade.jdbc.Model;

public interface Dialect {
	
	String getSaveSql(Model model);
	
	String getUpdateSql(Model model);
	
	String getDeleteSql(Model model);

	String getQuerySql(Model model);
	
	String getQueryOneSql(Model model);
	
	String getQueryCountSql(Model model);
	
	String getQueryPageSql(Model model);

}
