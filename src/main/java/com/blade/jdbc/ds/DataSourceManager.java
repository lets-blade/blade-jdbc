package com.blade.jdbc.ds;

import javax.sql.DataSource;

public class DataSourceManager {
	
	private static DataSource DS = null;
	
	public static void setDataSource(DataSource dataSource){
		DS = dataSource;
	}
	
	public static DataSource getDataSource(){
		return DS;
	}
	
}
